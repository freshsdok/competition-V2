package com.teaching.file.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.file.config.OSSConfig;
import com.teaching.file.utils.OSSClientFactory;
import com.teaching.system.api.domain.PackageFileReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Administrator
 */
//@Primary
@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OSSFileServiceImpl implements IOSSFileService {
    private final OSSClientFactory ossClientFactory;
    private final OSSConfig ossConfig;

    @Value("${oss.allowed-file-types}")
    private String allowedFileTypes;
    @Value("${oss.allowed-video-types:mp4,avi,mov}")
    private String allowedVideoTypes;
    @Value("${oss.retUrl}")
    private String retUrl;

    @Value("${ffmpeg.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Value("${oss.stsendpoint}")
    private String stsEndPoint;
    @Value("${oss.access-key-id}")
    private String accessKeyId;
    @Value("${oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${oss.roleArn}")
    private String roleArn;
    @Value("${oss.bucket-name}")
    private String bucketName;
    @Value("${oss.durationSeconds:3600}")
    private Long durationSeconds;
    @Value("${oss.region}")
    private String region;
    @Value("${pdf.temp-dir}")
    private String tempDirBase;
    @Value("${pdf.watermark}")
    private String watermark;
    @Value("${pdf.front-remove:1}")
    private int frontRemove;
    @Value("${pdf.rear-remove:1}")
    private int rearRemove;

    @Override
    public String uploadFile(MultipartFile file, String bizSign, String bizCode) throws IOException {
        // 文件类型校验
        String fileType = getFileExtension(file.getOriginalFilename());
//        if (!isAllowedFileType(fileType)) {
//            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
//        }

        // 构建对象键
//        String objectKey = generateFileName(file);
        String objectKey = file.getOriginalFilename();
        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            OSS ossClient = ossClientFactory.getOSSClient("default");
            Date nowDate = DateUtils.getNowDate();
            String uri = "";
            if (StringUtils.isEmpty(bizCode)) {
                uri = bizSign + "/" + DateFormatUtils.format(nowDate, "yyyyMM/dd") + "/" + IdUtil.getSnowflakeNextId() + "/" + objectKey;
            } else {
                uri = bizSign + "/" + bizCode + "/" + DateFormatUtils.format(nowDate, "yyyyMM/dd") + "/" + IdUtil.getSnowflakeNextId() + "/" + objectKey;
            }
            ossClient.putObject(ossConfig.getBucketName(), uri, inputStream);
            return retUrl + uri;
        }
    }

    /**
     * 按导出任务总体积上限进行拆分。
     * <p>
     * 背景：大批量导出（例如 40GB+）不做续传的前提下，为了降低单次任务失败重试成本，
     * 将待打包文件按“累计大小每 20GB 一组”拆成多个导出任务。
     * <p>
     * 说明：
     * 1) 这里按输入顺序进行分组（保持目录/文件的原始顺序，不做复杂的最优装箱）。
     * 2) 单个文件本身超过上限时，会被单独分到一组（不会在文件内部拆分）。
     */
    private List<List<DownloadTask>> splitTasksByTotalSize(List<DownloadTask> allTasks, long groupSizeLimit) {
        List<List<DownloadTask>> groups = new ArrayList<>();
        List<DownloadTask> current = new ArrayList<>();
        long currentSize = 0;

        for (DownloadTask task : allTasks) {
            if (!current.isEmpty() && currentSize + task.fileSize > groupSizeLimit) {
                groups.add(current);
                current = new ArrayList<>();
                currentSize = 0;
            }
            current.add(task);
            currentSize += task.fileSize;
        }

        if (!current.isEmpty()) {
            groups.add(current);
        }

        return groups;
    }

    private Map<String, Object> processFilesStreamingAndUpload(List<DownloadTask> tasks, OSS ossClient,
                                                               ExecutorService executor,
                                                               ProcessingStats stats,
                                                               FileSizeCategory category) throws Exception {

        System.out.println("检测到单文件超过500MB，启用边打包边上传（不生成临时ZIP文件）...");

        String exportName = generateExportName();
        String zipFileName = "fileExport/" + IdUtil.getSnowflakeNextIdStr() + "/" + exportName + ".zip";

        String uploadId = ossClient.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(ossConfig.getBucketName(), zipFileName)
        ).getUploadId();

        List<PartETag> partETags = new ArrayList<>();
        int partNumber = 1;
        long totalPiped = 0;

        PipedInputStream pipeIn = new PipedInputStream(PIPE_BUFFER_SIZE);
        PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);

        Object zipLock = new Object();

        CompletableFuture<Void> zipFuture = CompletableFuture.runAsync(() -> {
            try (BufferedOutputStream bos = new BufferedOutputStream(pipeOut, ZIP_BUFFER_SIZE);
                 ZipOutputStream zos = new ZipOutputStream(bos)) {

                zos.setLevel(Deflater.BEST_SPEED);

                processSmallFilesBatch(category.smallFiles, ossClient, zos, zipLock, executor, stats);
                processMediumFiles(category.mediumFiles, ossClient, zos, zipLock, executor, stats);
                processLargeFiles(category.largeFiles, ossClient, zos, zipLock, executor, stats);
                processVeryLargeFiles(category.veryLargeFiles, ossClient, zos, zipLock, executor, stats);

                zos.finish();
                zos.flush();

            } catch (Exception e) {
                throw new RuntimeException("流式ZIP生成失败", e);
            } finally {
                try {
                    pipeOut.close();
                } catch (IOException ignored) {
                }
            }
        }, executor);

        // 流式上传时不落盘：在内存里累计到固定大小(20MB)后作为一个分片上传。
        // 固定分片大小的目的：
        // 1) 控制分片数量，避免触发 OSS 分片数量上限；
        // 2) 降低内存峰值的不可预期变化（不再随总大小动态调整）。
        int partSize = STREAMING_PART_SIZE;
        byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
        ByteArrayOutputStream partBuffer = new ByteArrayOutputStream(partSize);

        try {
            int read;
            while ((read = pipeIn.read(buffer)) != -1) {
                partBuffer.write(buffer, 0, read);
                totalPiped += read;

                if (partBuffer.size() >= partSize) {
                    byte[] data = partBuffer.toByteArray();
                    partBuffer.reset();

                    try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
                        UploadPartRequest request = new UploadPartRequest();
                        request.setBucketName(ossConfig.getBucketName());
                        request.setKey(zipFileName);
                        request.setUploadId(uploadId);
                        request.setInputStream(bis);
                        request.setPartSize(data.length);
                        request.setPartNumber(partNumber);
                        UploadPartResult result = ossClient.uploadPart(request);
                        partETags.add(result.getPartETag());
                        System.out.printf("流式上传分片 %d 完成 (%.2f MB)%n",
                                partNumber, data.length / (1024.0 * 1024.0));
                        partNumber++;
                    }
                }
            }

            if (partBuffer.size() > 0) {
                byte[] data = partBuffer.toByteArray();
                partBuffer.reset();
                try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
                    UploadPartRequest request = new UploadPartRequest();
                    request.setBucketName(ossConfig.getBucketName());
                    request.setKey(zipFileName);
                    request.setUploadId(uploadId);
                    request.setInputStream(bis);
                    request.setPartSize(data.length);
                    request.setPartNumber(partNumber);
                    UploadPartResult result = ossClient.uploadPart(request);
                    partETags.add(result.getPartETag());
                    System.out.printf("流式上传分片 %d 完成 (%.2f MB)%n",
                            partNumber, data.length / (1024.0 * 1024.0));
                }
            }

            zipFuture.get();

            partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));
            ossClient.completeMultipartUpload(
                    new CompleteMultipartUploadRequest(ossConfig.getBucketName(), zipFileName, uploadId, partETags)
            );

            stats.totalSize = totalPiped;

            String downloadUrl = generatePresignedUrl(ossClient, zipFileName, 3600);
            if (downloadUrl.contains("-internal")) {
                downloadUrl = downloadUrl.replace("-internal", "");
            }

            return createSuccessResult(downloadUrl, stats.totalSize, exportName, stats);

        } catch (Exception e) {
            try {
                abortMultipartUpload(ossConfig.getBucketName(), zipFileName, uploadId, ossClient);
            } catch (Exception ignored) {
            }
            throw e;
        } finally {
            try {
                pipeIn.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, long expireTimeInMillis) throws Exception {
        OSS ossClient = ossClientFactory.getOSSClient("default");
        try {
            String fileUrl = normalizeManagedObjectKey(objectKey);
            // 计算过期时间点
            Date expiration = new Date(new Date().getTime() + expireTimeInMillis);
            // 生成预签名URL
//            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), fileUrl, expiration);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossConfig.getBucketName(), fileUrl);
            request.setExpiration(expiration);
//            Map metadata = new HashMap<>();
//            metadata.put("Cache-Control","no-cache");
            // 添加缓存控制头
            request.addHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // 禁止缓存
            request.addHeader("Pragma", "no-cache"); // 兼容特殊请求
            request.addHeader("Expires", "0"); // 立即过期
//            request.setUserMetadata(metadata);
            request.addQueryParameter("r", UUID.randomUUID().toString());
            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("生成预签名URL失败");
        }
    }

    @Override
    public String generatePresignedUrl2(String objectKey, long expireTimeInMillis) throws Exception {
        OSS ossClient = ossClientFactory.getPreviewOSSClient();
        try {
            // 兼容处理：fileKey 可能是完整URL，也可能是相对路径
            String fileUrl;
            if (StringUtils.ishttp(objectKey) && objectKey.startsWith(ossConfig.getDomain())) {
                fileUrl = objectKey.substring(ossConfig.getDomain().length());
            } else {
                fileUrl = objectKey;
            }
            // 去掉可能存在的前导斜杠（OSS objectKey 通常不以 / 开头）
            if (fileUrl.startsWith("/")) {
                fileUrl = fileUrl.substring(1);
            }
            log.info("生成预签名URL -> objectKey={}, retUrl={}, fileUrl={}", objectKey, ossConfig.getDomain(), fileUrl);

            // 计算过期时间点
            Date expiration = new Date(System.currentTimeMillis() + expireTimeInMillis);
            // 生成预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossConfig.getBucketName(), fileUrl);
            request.setExpiration(expiration);
            // 让浏览器默认预览（而不是下载），对图片/PDF/视频等生效
            request.addQueryParameter("response-content-disposition", "inline");
            // 强制返回正确的 Content-Type（兜底：防止上传时未设置或OSS识别错误）
//            request.addQueryParameter("response-content-type", getContentType(fileUrl));
            // 随机参数防止浏览器/CDN缓存
            request.addQueryParameter("r", UUID.randomUUID().toString());
            URL signedUrl = ossClient.generatePresignedUrl(request);
            String resultUrl = signedUrl.toString();
            //如果resultUrl是以http://tianda-bucket.oss.ksup.cn/开头就把http://tianda-bucket.oss.ksup.cn/替换为http://oss.ksup.cn/
            if (StringUtils.isNotBlank(resultUrl) && resultUrl.startsWith("http://tianda-bucket.oss.ksup.cn/")) {
                // 替换为 http://oss.ksup.cn/
                resultUrl = resultUrl.replaceFirst("^http://tianda-bucket\\.oss\\.ksup\\.cn/", ossConfig.getDomain());
            }

            log.info("预签名URL生成成功 -> {}", resultUrl);
            return resultUrl;
        } catch (Exception e) {
            log.error("生成预签名URL失败, objectKey={}", objectKey, e);
            throw new Exception("生成预签名URL失败: " + e.getMessage());
        }
    }

   /* @Override
    public String uploadVideo2(MultipartFile file) throws Exception {
        // 1. 基本校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("无效的文件名");
        }
        String fileType = getFileExtension(originalFilename);
        if (!isAllowedVideoType(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
        // 2. FFmpeg可用性检查
        if (!isFFmpegAvailable()) {
            throw new RuntimeException("系统未安装FFmpeg或不在PATH环境变量中");
        }
        // 3. 路径处理
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }
        Path originalTempPath = tempDirPath.resolve(originalFilename);
        // 4. 保存原始文件
        try {
            file.transferTo(originalTempPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        // 5. 转码参数配置
        Map<String, String> transcodeParams = Map.of(
                "1080p", "-s 1920x1080 -b:v 5000k",
                "720p", "-s 1280x720 -b:v 3000k",
                "480p", "-s 854x480 -b:v 1500k"
        );

        // 6. OSS客户端
        OSS ossClient = ossClientFactory.getOSSClient("default");
        ExecutorService executor = null;
        List<String> allObjectKeys = new ArrayList<>();
        String bucketName = ossConfig.getBucketName();
        try {
            String originalObjectKey = generateFileName(file);
            // 创建线程池（根据CPU核心数设置）
            int poolSize = Runtime.getRuntime().availableProcessors();
            executor = Executors.newFixedThreadPool(poolSize);
            // 记录所有需要清理的文件
            allObjectKeys.add(originalObjectKey);
            // 7. 上传原始文件
            uploadToOSS(ossClient, bucketName, originalObjectKey, originalTempPath);
            // 8. 创建转码任务列表
            List<CompletableFuture<Void>> transcodeTasks = new ArrayList<>();
            for (Map.Entry<String, String> entry : transcodeParams.entrySet()) {
                String quality = entry.getKey();
                String params = entry.getValue();
                String objectKey = originalObjectKey.replace("." + fileType,
                        "_" + quality + "." + fileType);
                allObjectKeys.add(objectKey);
                // 创建转码任务
                CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                    Path outputPath = null;
                    try {
                        // 生成临时输出路径
                        outputPath = tempDirPath.resolve(
                                baseName + "_" + quality + "." + fileType);

                        // 构建FFmpeg命令
                        String[] cmd = {
                                "ffmpeg",
                                "-i", originalTempPath.toString(),
                                params,
                                "-c:v", "libx264",
                                "-c:a", "aac",
                                "-y",
                                outputPath.toString()
                        };

                        // 执行FFmpeg转码
                        int exitCode = executeFFmpegCommand(cmd);
                        if (exitCode != 0) {
                            throw new RuntimeException("FFmpeg转码失败，退出码: " + exitCode);
                        }
                        // 上传转码后的文件
                        uploadToOSS(ossClient, bucketName, objectKey, outputPath);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    } finally {
                        // 删除临时文件
                        if (outputPath != null && Files.exists(outputPath)) {
                            try {
                                Files.deleteIfExists(outputPath);
                            } catch (IOException e) {
                                System.err.println("删除临时文件失败: " + outputPath);
                            }
                        }
                    }
                }, executor);

                transcodeTasks.add(task);
            }
            // 9. 等待所有转码任务完成
            CompletableFuture.allOf(transcodeTasks.toArray(new CompletableFuture[0]))
                    .join();
            return originalObjectKey;
        } catch (Exception e) {
            // 异常处理 - 清理已上传的文件
            if (ossClient != null) {
                for (String key : allObjectKeys) {
                    try {
                        ossClient.deleteObject(bucketName, key);
                    } catch (Exception ex) {
                        System.err.println("删除OSS文件失败: " + key);
                    }
                }
            }
            throw new RuntimeException("视频处理失败", e);
        } finally {
            // 资源清理
            try {
                Files.deleteIfExists(originalTempPath);
            } catch (IOException e) {
                System.err.println("删除原始临时文件失败");
            }
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    System.err.println("关闭OSS客户端失败");
                }
            }
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }*/


    @Override
    public Map<String, Object> uploadVideo(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        // 1. 基本校验
        String originalFilename = validateFile(file);
        String fileType = getFileExtension(originalFilename);
        if (!isAllowedVideoType(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
        // 2. FFmpeg可用性检查
        if (!isFFmpegAvailable()) {
            throw new RuntimeException("系统未安装FFmpeg或不在PATH环境变量中");
        }

        // 3. 路径处理 - 只创建一个临时文件
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }
        Path originalTempPath = tempDirPath.resolve(UUID.randomUUID() + "_" + originalFilename);

        // 4. 保存原始文件 - 只传输一次
        try {
            file.transferTo(originalTempPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        // 5. 获取视频时长
        int time = getVideoDurationWithFFmpeg(originalTempPath.toFile());
        log.info("视频时长为：{}", time);
        result.put("duration", time);
        // 5. 转码参数配置
        Map<String, String> transcodeParams = Map.of(
                "1080p", "-s 1920x1080 -b:v 5000k",
                "720p", "-s 1280x720 -b:v 3000k",
                "480p", "-s 854x480 -b:v 1500k"
        );
        // 6. OSS客户端和线程池初始化
        OSS ossClient = ossClientFactory.getOSSClient("default");
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<String> allObjectKeys = new ArrayList<>();
        String bucketName = ossConfig.getBucketName();

        try {
            String originalObjectKey = "videos/" + generateFileName(file);
            allObjectKeys.add(originalObjectKey);

            // 7. 上传原始文件
            uploadToOSS(ossClient, bucketName, originalObjectKey, originalTempPath);

            // 8. 创建并执行转码任务
            List<CompletableFuture<Void>> transcodeTasks = createTranscodeTasks(
                    executor, ossClient, bucketName, transcodeParams,
                    originalTempPath, baseName, fileType, originalObjectKey,
                    allObjectKeys, tempDirPath);

            // 9. 等待所有转码任务完成
            CompletableFuture.allOf(transcodeTasks.toArray(new CompletableFuture[0])).join();
            result.put("objectKey", originalObjectKey);
            return result;
        } catch (Exception e) {
            cleanupResources(ossClient, bucketName, allObjectKeys);
            throw new RuntimeException("视频处理失败", e);
        } finally {
            cleanupTempFiles(originalTempPath);
            shutdownOSSClient(ossClient);
            shutdownExecutor(executor);
        }
    }

    private int getVideoDurationWithFFmpeg(File videoFile) {
        if (!videoFile.exists() || videoFile.length() == 0) {
            log.error("视频文件不存在或为空: {}", videoFile.getAbsolutePath());
            return 0;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoFile.getAbsolutePath()
            );

            Process process = pb.start();

            // 读取错误输出流
            StringBuilder errorOutput = new StringBuilder();
            Thread errorThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("读取FFprobe错误输出失败", e);
                }
            });
            errorThread.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                errorThread.join();

                if (line != null && !line.isEmpty()) {
                    return (int) Double.parseDouble(line.trim());
                } else {
                    log.error("FFprobe获取时长失败，错误输出: {}", errorOutput.toString());
                }
            }
        } catch (Exception e) {
            log.error("使用FFprobe获取时长失败: {}", e.getMessage());
        }
        return 0;
    }

    private String validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("无效的文件名");
        }
        return originalFilename;
    }

    private List<CompletableFuture<Void>> createTranscodeTasks(
            ExecutorService executor, OSS ossClient, String bucketName,
            Map<String, String> transcodeParams, Path originalTempPath,
            String baseName, String fileType, String originalObjectKey,
            List<String> allObjectKeys, Path tempDirPath) {

        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (Map.Entry<String, String> entry : transcodeParams.entrySet()) {
            String quality = entry.getKey();
            String params = entry.getValue();
            String objectKey = originalObjectKey.replace("." + fileType,
                    "_" + quality + "." + fileType);
            allObjectKeys.add(objectKey);

            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                Path outputPath = null;
                try {
                    // 从原始临时文件路径中提取文件名（不包含UUID前缀）
                    String originalFileName = originalTempPath.getFileName().toString();
                    String cleanBaseName = originalFileName.substring(originalFileName.indexOf("_") + 1);
                    cleanBaseName = cleanBaseName.substring(0, cleanBaseName.lastIndexOf("."));
                    outputPath = tempDirPath.resolve(cleanBaseName + "_" + quality + "." + fileType);

                    // 构建FFmpeg命令，将参数字符串拆分成独立的参数
                    List<String> commandList = new ArrayList<>();
                    commandList.add("ffmpeg");
                    commandList.add("-i");
                    commandList.add(originalTempPath.toString());

                    // 拆分转码参数
                    String[] paramArray = params.split("\\s+");
                    for (String param : paramArray) {
                        if (!param.trim().isEmpty()) {
                            commandList.add(param.trim());
                        }
                    }
                    commandList.add("-c:v");
                    commandList.add("libx264");
                    commandList.add("-c:a");
                    commandList.add("aac");
                    commandList.add("-y");
                    commandList.add(outputPath.toString());

                    String[] cmd = commandList.toArray(new String[0]);

                    int exitCode = executeFFmpegCommand(cmd);
                    if (exitCode != 0) {
                        throw new RuntimeException("FFmpeg转码失败，退出码: " + exitCode);
                    }

                    uploadToOSS(ossClient, bucketName, objectKey, outputPath);
                } catch (Exception e) {
                    throw new CompletionException(e);
                } finally {
                    if (outputPath != null && Files.exists(outputPath)) {
                        try {
                            Files.deleteIfExists(outputPath);
                        } catch (IOException e) {
                            System.err.println("删除临时文件失败: " + outputPath);
                        }
                    }
                }
            }, executor);

            tasks.add(task);
        }
        return tasks;
    }


    private void cleanupResources(OSS ossClient, String bucketName, List<String> allObjectKeys) {
        if (ossClient != null) {
            for (String key : allObjectKeys) {
                try {
                    ossClient.deleteObject(bucketName, key);
                } catch (Exception ex) {
                    System.err.println("删除OSS文件失败: " + key);
                }
            }
        }
    }

    private void cleanupTempFiles(Path originalTempPath) {
        try {
            Files.deleteIfExists(originalTempPath);
        } catch (IOException e) {
            System.err.println("删除原始临时文件失败");
        }
    }

    private void shutdownOSSClient(OSS ossClient) {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
            } catch (Exception e) {
                System.err.println("关闭OSS客户端失败");
            }
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // 原有的OSS上传方法（保持不变）
    private void uploadToOSS(OSS ossClient, String bucketName,
                             String objectKey, Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            ossClient.putObject(bucketName, objectKey, inputStream);
        }
    }

    // 检查FFmpeg是否可用
    private boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows系统：使用cmd执行
                pb = new ProcessBuilder("cmd", "/c", ffmpegPath, "-version");
            } else {
                // Linux/Mac系统：直接执行
                pb = new ProcessBuilder(ffmpegPath, "-version");
            }
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.error("FFmpeg检查失败，路径: {}, 错误: {}", ffmpegPath, e.getMessage());
            return false;
        }
    }

    private int executeFFmpegCommand(String[] cmd) throws Exception {
        ProcessBuilder pb;

        // 改进Windows系统处理
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows系统：将命令数组转换为单个字符串
            List<String> windowsCmd = new ArrayList<>();
            windowsCmd.add("cmd");
            windowsCmd.add("/c");
            windowsCmd.add(String.join(" ", cmd));
            pb = new ProcessBuilder(windowsCmd);
        } else {
            // Linux/Mac系统：直接使用命令数组
            pb = new ProcessBuilder(cmd);
        }

        pb.redirectErrorStream(true);

        // 添加日志记录命令
        log.debug("Executing FFmpeg command: {}", String.join(" ", cmd));

        Process process = pb.start();

        // 记录FFmpeg输出（调试用）
        try (InputStream is = process.getInputStream()) {
            String output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (!output.isEmpty()) {
                log.debug("FFmpeg output: {}", output);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("FFmpeg exited with code {} for command: {}", exitCode, String.join(" ", cmd));
            // 添加更详细的错误信息
            throw new RuntimeException("FFmpeg转码失败，退出码: " + exitCode +
                    ", 请检查输入文件格式和FFmpeg参数");
        }

        return exitCode;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String bizSign, String bizCode) throws IOException {
        List<String> objectKeys = new ArrayList<>();
        for (MultipartFile file : files) {
            objectKeys.add(uploadFile(file, bizSign, bizCode));
        }
        return objectKeys;
    }

    public Resource downloadFile(String objectKey) {
        // 检查文件是否存在
        if (!objectExists(objectKey)) {
            throw new RuntimeException("文件不存在: " + objectKey);
        }

        // 获取OSS对象
        OSS ossClient = ossClientFactory.getOSSClient("default");
        OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectKey);

        // 包装为Spring Resource
        return new InputStreamResource(ossObject.getObjectContent()) {
            @Override
            public String getFilename() {
                return objectKey;
            }
        };
    }

    @Override
    public void deleteFile(String objectKey) {
        String normalizedObjectKey = normalizeManagedObjectKey(objectKey);
        if (objectExists(normalizedObjectKey)) {
            OSS ossClient = ossClientFactory.getOSSClient("default");
            ossClient.deleteObject(ossConfig.getBucketName(), normalizedObjectKey);
        }
    }

    /**
     * 文件服务历史上同时返回过完整 URL 和相对对象键。
     * 这里只接受当前 Bucket 域名下的 URL，避免将预签名/删除接口变成任意对象代理。
     */
    private String normalizeManagedObjectKey(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("OSS对象键不能为空");
        }
        String normalized = value.trim();
        if (StringUtils.ishttp(normalized)) {
            int queryIndex = normalized.indexOf('?');
            String urlWithoutQuery = queryIndex >= 0 ? normalized.substring(0, queryIndex) : normalized;
            String matchedBase = managedUrlBase(retUrl, urlWithoutQuery);
            if (matchedBase == null) {
                matchedBase = managedUrlBase(ossConfig.getDomain(), urlWithoutQuery);
            }
            if (matchedBase == null) {
                throw new IllegalArgumentException("只允许处理当前OSS Bucket的对象");
            }
            normalized = urlWithoutQuery.substring(matchedBase.length());
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (StringUtils.isEmpty(normalized)) {
            throw new IllegalArgumentException("OSS对象键不能为空");
        }
        return normalized;
    }

    private String managedUrlBase(String configuredBase, String candidate) {
        if (StringUtils.isEmpty(configuredBase)) {
            return null;
        }
        String base = configuredBase.trim();
        if (!base.endsWith("/")) {
            base += "/";
        }
        return candidate.startsWith(base) ? base : null;
    }

    @Override
    public String getFilePath() {
        return "";
    }

    public ObjectMetadata getFileMetadata(String objectKey) {
        if (!objectExists(objectKey)) {
            throw new RuntimeException("文件不存在: " + objectKey);
        }

        OSS ossClient = ossClientFactory.getOSSClient("default");
        return ossClient.getObjectMetadata(ossConfig.getBucketName(), objectKey);
    }

    private boolean objectExists(String objectKey) {
        OSS ossClient = ossClientFactory.getOSSClient("default");
        return ossClient.doesObjectExist(ossConfig.getBucketName(), objectKey);
    }

    private boolean isAllowedFileType(String fileType) {
        return Arrays.asList(allowedFileTypes.split(","))
                .contains(fileType.toLowerCase());
    }

    /**
     * 通过OSS地址下载文件到本地
     *
     * @param ossUrl    OSS文件地址
     * @param localPath 本地存储路径
     * @return
     * @throws Exception
     */
    @Override
    public boolean downloadFileToLocal(String ossUrl, String localPath) throws Exception {
        if (StringUtils.isEmpty(ossUrl) || StringUtils.isEmpty(localPath)) {
            throw new IllegalArgumentException("OSS URL和本地路径不能为空");
        }

        File localFile = resolveLocalFile(ossUrl, localPath);
        File parentDir = localFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (isPresignedUrl(ossUrl)) {
            return downloadFromPresignedUrl(ossUrl, localFile);
        } else {
            return downloadFromOssSdk(ossUrl, localFile);
        }
    }

    private File resolveLocalFile(String ossUrl, String localPath) {
        File file = new File(localPath);
        if (file.isDirectory() || localPath.endsWith("/") || localPath.endsWith("\\")) {
            String fileName = extractFileNameFromUrl(ossUrl);
            return new File(file, fileName);
        }
        return file;
    }

    private String extractFileNameFromUrl(String url) {
        try {
            String path = url;
            int queryIndex = path.indexOf("?");
            if (queryIndex > 0) {
                path = path.substring(0, queryIndex);
            }
            int lastSlash = path.lastIndexOf("/");
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                return path.substring(lastSlash + 1);
            }
            return "downloaded_file_" + System.currentTimeMillis();
        } catch (Exception e) {
            return "downloaded_file_" + System.currentTimeMillis();
        }
    }

    /**
     * 判断给定的URL是否为预签名URL
     *
     * @param url
     * @return
     */
    private boolean isPresignedUrl(String url) {
        return url.contains("Expires=") || url.contains("OSSAccessKeyId=") || url.contains("Signature=");
    }

    /**
     * 从预签名URL下载文件到本地
     *
     * @param presignedUrl
     * @param localFile
     * @return
     * @throws Exception
     */
    private boolean downloadFromPresignedUrl(String presignedUrl, File localFile) throws Exception {
        log.info("开始从预签名URL下载文件: {} -> {}", presignedUrl, localFile.getAbsolutePath());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(presignedUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet);
                 InputStream inputStream = response.getEntity().getContent();
                 BufferedInputStream bis = new BufferedInputStream(inputStream);
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile))) {

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new RuntimeException("下载失败，HTTP状态码: " + statusCode);
                }

                long contentLength = response.getEntity().getContentLength();
                log.info("文件大小: {} bytes", contentLength);

                IoUtil.copy(bis, bos, IoUtil.DEFAULT_BUFFER_SIZE);

                log.info("文件下载完成: {}", localFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            log.error("预签名URL下载失败: {}", e.getMessage(), e);
            if (localFile.exists()) {
                FileUtil.del(localFile);
            }
            throw new RuntimeException("预签名URL下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从OSS下载文件到本地
     *
     * @param ossUrl
     * @param localFile
     * @return
     * @throws Exception
     */
    private boolean downloadFromOssSdk(String ossUrl, File localFile) throws Exception {
        String objectKey = extractObjectKeyFromUrl(ossUrl);
        if (StringUtils.isEmpty(objectKey)) {
            throw new IllegalArgumentException("无法从OSS URL中提取文件路径: " + ossUrl);
        }

        if (!objectExists(objectKey)) {
            throw new RuntimeException("文件不存在于OSS: " + objectKey);
        }

        OSS ossClient = ossClientFactory.getOSSClient("default");

        try (OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectKey);
             BufferedInputStream inputStream = new BufferedInputStream(ossObject.getObjectContent());
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile))) {

            long fileSize = ossObject.getObjectMetadata().getContentLength();
            log.info("开始下载文件: {} -> {}, 文件大小: {} bytes", objectKey, localFile.getAbsolutePath(), fileSize);

            IoUtil.copy(inputStream, outputStream, IoUtil.DEFAULT_BUFFER_SIZE);

            log.info("文件下载完成: {} -> {}, 大小: {} bytes", objectKey, localFile.getAbsolutePath(), fileSize);
            return true;

        } catch (Exception e) {
            log.error("文件下载失败: {} -> {}", objectKey, localFile.getAbsolutePath(), e);

            if (localFile.exists()) {
                FileUtil.del(localFile);
            }

            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    private String extractObjectKeyFromUrl(String ossUrl) {
        if (StringUtils.isEmpty(ossUrl)) {
            return null;
        }

        try {
            URL url = new URL(ossUrl);
            String path = url.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;

        } catch (Exception e) {
            log.warn("无法解析OSS URL: " + ossUrl, e);

            if (ossUrl.startsWith(ossConfig.getBucketName())) {
                return ossUrl.substring(ossConfig.getBucketName().length() + 1);
            }

            return null;
        }
    }

    @Override
    public String uploadLocalFile(String localPath, String bizSign, String bizCode) throws Exception {
        if (StringUtils.isEmpty(localPath)) {
            throw new IllegalArgumentException("本地文件路径不能为空");
        }

        File localFile = new File(localPath);
        if (!localFile.exists()) {
            throw new IllegalArgumentException("本地文件不存在: " + localPath);
        }
        if (!localFile.isFile()) {
            throw new IllegalArgumentException("路径不是文件: " + localPath);
        }

        String fileName = localFile.getName();
        String objectKey = buildObjectKey(fileName, bizSign, bizCode);

        OSS ossClient = ossClientFactory.getOSSClient("default");

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(localFile))) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(localFile.length());
            metadata.setContentType(getContentType(fileName));
            metadata.setContentDisposition("inline");

            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);

            String ossUrl = ossConfig.getDomain() + objectKey;
            log.info("上传本地文件成功: {} -> {}", localPath, ossUrl);
            return ossUrl;

        } catch (Exception e) {
            log.error("上传本地文件失败: {}", localPath, e);
            throw new RuntimeException("上传本地文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> uploadLocalDirectory(String localDir, String bizSign, String bizCode, boolean recursive) throws Exception {
        if (StringUtils.isEmpty(localDir)) {
            throw new IllegalArgumentException("本地目录路径不能为空");
        }

        File directory = new File(localDir);
        if (!directory.exists()) {
            throw new IllegalArgumentException("本地目录不存在: " + localDir);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("路径不是目录: " + localDir);
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<File> filesToUpload = new ArrayList<>();

        collectFiles(directory, filesToUpload, recursive);

        log.info("开始上传目录: {}, 共 {} 个文件", localDir, filesToUpload.size());

        for (File file : filesToUpload) {
            try {
                String ossUrl = uploadLocalFile(file.getAbsolutePath(), bizSign, bizCode);
                uploadedUrls.add(ossUrl);
            } catch (Exception e) {
                log.error("上传文件失败: {}", file.getAbsolutePath(), e);
            }
        }

        log.info("目录上传完成: {}, 成功 {} 个, 失败 {} 个",
                localDir, uploadedUrls.size(), filesToUpload.size() - uploadedUrls.size());

        return uploadedUrls;
    }

    private static final String[] PDF_FONT_PATHS = {
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/simsun.ttc",
            "C:/Windows/Fonts/msyh.ttc",
            "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
            "/System/Library/Fonts/PingFang.ttc",
    };

    @Override
    public String processPdfFile(String ossUrl) throws Exception {
        if (StringUtils.isEmpty(ossUrl)) {
            throw new IllegalArgumentException("OSS文件地址不能为空");
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        File sessionTempDir = new File(tempDirBase, "pdf_process_" + sessionId);
        File downloadDir = new File(sessionTempDir, "download");
        File processedDir = new File(sessionTempDir, "processed");

        File downloadedFile = null;
        File processedFile = null;

        try {
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            if (!processedDir.exists()) {
                processedDir.mkdirs();
            }

            log.info("开始处理PDF文件: {}", ossUrl);

            String cleanOssUrl = ossUrl;
            if (ossUrl.contains("?")) {
                cleanOssUrl = ossUrl.substring(0, ossUrl.indexOf("?"));
            }

            String fileExtension = getFileExtension(cleanOssUrl);
            String tempFileName = System.currentTimeMillis() + fileExtension;
            downloadedFile = new File(downloadDir, tempFileName);

            boolean downloadSuccess = downloadFileToLocal(cleanOssUrl, downloadedFile.getAbsolutePath());
            if (!downloadSuccess) {
                throw new RuntimeException("下载文件失败: " + cleanOssUrl);
            }

            if (!downloadedFile.exists() || downloadedFile.length() == 0) {
                throw new RuntimeException("下载的文件不存在或为空");
            }

            processedFile = com.teaching.file.utils.FileUtil.processPdfFile(downloadedFile, frontRemove, rearRemove, processedDir, watermark);

            if (downloadedFile.exists()) {
                downloadedFile.delete();
                log.debug("已删除原始下载文件: {}", downloadedFile.getName());
            }
            downloadedFile = null;

            if (processedFile == null || !processedFile.exists()) {
                throw new RuntimeException("PDF处理失败, 输出文件不存在");
            }

            String newOssUrl = uploadLocalFile(processedFile.getAbsolutePath(), "processed", null);

            if (processedFile.exists()) {
                processedFile.delete();
                log.debug("已删除处理后文件: {}", processedFile.getName());
            }
            processedFile = null;

            log.info("PDF处理完成: {} -> {}", ossUrl, newOssUrl);
            return newOssUrl;

        } catch (Exception e) {
            log.error("处理PDF文件失败: {}", ossUrl, e);
            throw e;
        } finally {
            if (downloadedFile != null && downloadedFile.exists()) {
                downloadedFile.delete();
            }
            if (processedFile != null && processedFile.exists()) {
                processedFile.delete();
            }
            cleanupDirectory(sessionTempDir);
        }
    }


    private void cleanupDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanupDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
            log.info("清理临时目录完成: {}", directory.getAbsolutePath());
        } catch (Exception e) {
            log.warn("清理临时目录异常: {}", directory.getAbsolutePath(), e);
        }
    }

    private void collectFiles(File directory, List<File> files, boolean recursive) {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isFile()) {
                files.add(child);
            } else if (child.isDirectory() && recursive) {
                collectFiles(child, files, recursive);
            }
        }
    }

    private String buildObjectKey(String fileName, String bizSign, String bizCode) {
        Date nowDate = DateUtils.getNowDate();
        String newFileName = generateTimestampFileName(fileName);
        if (StringUtils.isEmpty(bizCode)) {
            return bizSign + "/" + DateFormatUtils.format(nowDate, "yyyyMM") + "/" + newFileName;
        } else {
            return bizSign + "/" + bizCode + "/" + DateFormatUtils.format(nowDate, "yyyyMM") + "/" + newFileName;
        }
    }

    private String generateTimestampFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String timestamp = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        int randomNum = 100 + (int) (Math.random() * 900);
        return timestamp + randomNum + extension;
    }

    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }

    private boolean isAllowedVideoType(String fileType) {
        return Arrays.asList(allowedVideoTypes.split(","))
                .contains(fileType.toLowerCase());
    }

    private String generateFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String fileType = getFileExtension(originalName);
        //在文件名里拼上唯一标识
        String prefix = originalName.substring(0, originalName.lastIndexOf("."));
        if (fileType == null || fileType.isEmpty()) {
            fileType = "unknown";
        }
        return prefix + "_" + IdUtil.getSnowflakeNextId() + "." + fileType;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public Map<String, String> generateSTSToken(String bizSign, String bizCode) {
        Map<String, String> result = new HashMap<>();
        // STS服务接入点，例如sts.cn-hangzhou.aliyuncs.com。您可以通过公网或者VPC接入STS服务。
        // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
        String roleSessionName = "SessionTest";
        // 临时访问凭证将获得角色拥有的所有权限。
        String policy = null;
        try {
            // 发起STS请求所在的地域。建议保留默认值，默认值为空字符串（""）。
            String regionId = "";
            // 添加endpoint。适用于Java SDK 3.12.0及以上版本。
            DefaultProfile.addEndpoint(regionId, "Sts", stsEndPoint);
            // 添加endpoint。适用于Java SDK 3.12.0以下版本。
            // DefaultProfile.addEndpoint("",regionId, "Sts", endpoint);
            // 构造default profile。
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            // 构造client。
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            // 适用于Java SDK 3.12.0及以上版本。
            request.setSysMethod(MethodType.POST);
            // 适用于Java SDK 3.12.0以下版本。
            // request.setMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds);
            final AssumeRoleResponse response = client.getAcsResponse(request);
            result.put("expiration", response.getCredentials().getExpiration());
            result.put("accessKeySecret", response.getCredentials().getAccessKeySecret());
            result.put("securityToken", response.getCredentials().getSecurityToken());
            result.put("accessKeyId", response.getCredentials().getAccessKeyId());
            result.put("requestId", response.getRequestId());
            Date nowDate = DateUtils.getNowDate();
            String uri = "";
            if (StringUtils.isEmpty(bizCode)) {
                uri = bizSign + "/" + DateFormatUtils.format(nowDate, "yyyyMM/dd") + "/" + IdUtil.getSnowflakeNextId() + "/";
            } else {
                uri = bizSign + "/" + bizCode + "/" + DateFormatUtils.format(nowDate, "yyyyMM/dd") + "/" + IdUtil.getSnowflakeNextId() + "/";
            }
            result.put("uri", uri);
            result.put("bucketName", bucketName);
            result.put("region", region);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 文件在阿里云在线打包，并返回url
     *
     * @param fileList 文件信息
     * @return
     */
//    @Override
//    public Map<String, Object> packageFile(List<PackageFileReq> fileList) {
//        String accessKeyId = ossConfig.getAccessKeyId();
//        String accessKeySecret = ossConfig.getAccessKeySecret();
//        String securityToken = "";  // 可选，使用STS时需要提供
//
//        //method参数请求方式要求必须大写，如：POST、GET，如果您的请求方式为GET，需要将下方HttpPost改为HttpGet
//        String method = "POST";
//
//
////        String url = "https://packageturn-url-wokdhjiwhj.cn-wulanchabu.fcapp.run";  // 你的HTTP触发器地址
//        String url = "https://packageturn-url-wokdhjiwhj.cn-wulanchabu-vpc.fcapp.run";  //内网地址
//        Map<String, String> headers = new HashMap<String, String>();
//        String date = Instant.now().toString();
//
//        headers.put("Content-Type", "application/json");
//        headers.put("Accept", "application/json");
//        headers.put("x-acs-date", date);
//        headers.put("x-acs-security-token", securityToken);
//
//
//        URI uri = null;
//        try {
//            uri = new URI(url);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        Map<String, String> query = new HashMap<String, String>();
//        for (NameValuePair pair : URLEncodedUtils.parse(uri, StandardCharsets.UTF_8)) {
//            query.put(pair.getName(), pair.getValue());
//        }
//        TeaRequest req = new TeaRequest();
//        req.method = method;
//        req.pathname = uri.getPath().replace("$", "%24");
//        req.headers = headers;
//        req.query = query;
//
//        String auth = null;
//        try {
//            auth = com.aliyun.openapiutil.Client.getAuthorization(
//                    req, "ACS3-HMAC-SHA256", "", accessKeyId, accessKeySecret);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        headers.put("authorization", auth);
//
//        //如果method请求方式为GET，需要将下方HttpPost改为HttpGet
//        HttpPost request = new HttpPost(url);
//        for (Map.Entry<String, String> entry : headers.entrySet()) {
//            request.setHeader(entry.getKey(), entry.getValue());
//        }
//
//        //入参转换为json字符串
//        String requestBody = JSONUtil.toJsonStr(fileList);
//        StringEntity entity = new StringEntity(JSONUtil.toJsonStr(requestBody), "UTF-8");
//        request.setEntity(entity);
//
//        // Execute the request
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            //解析返回结果
//            org.apache.http.HttpResponse response = httpClient.execute(request);
//            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//            System.out.println(responseString);
//            if ("Internal Server Error".equals(responseString)) {
//                Map<String, Object> resultMap = new HashMap<>();
//                log.error("文件打包服务出错，请在FC函数服务器查看报错原因");
//                resultMap.put("code", "fail");
//                resultMap.put("failReason", "文件打包服务出错，请在FC函数服务器查看报错原因");
//                return resultMap;
//            }
//            return JSONUtil.parseObj(responseString);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public Map<String, Object> packageFile(List<PackageFileReq> fileList) {
        //入参转换为json字符串
        String requestBody = JSONUtil.toJsonStr(fileList);
        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("body", requestBody);
        Map<String, Object> stringStringMap = handleRequest(mapObject);
        return stringStringMap;
    }

    // 优化配置常量
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 1000;
    private static final int MAX_CONCURRENT_DOWNLOADS = 32; // 增加并发数
    private static final long DOWNLOAD_TIMEOUT_MINUTES = 30; // 增加超时时间
    private static final int OSS_PART_SIZE = 20 * 1024 * 1024;
    private static final int MAX_CONCURRENT_PART_UPLOADS = 16; // 增加分片上传并发

    /**
     * 流式边打包边上传的固定分片大小。
     * <p>
     * 说明：流式模式不允许生成临时ZIP文件，因此每个分片会先累计在内存中，再调用 OSS uploadPart 上传。
     * 设置为 20MB：20GB 上限的任务约 1024 个分片，低于 OSS 10000 分片上限，
     * 同时避免单个导出任务固定占用约 200MB 堆内存。
     */
    private static final int STREAMING_PART_SIZE = 20 * 1024 * 1024;

    /**
     * 单个导出任务的文件总量上限（20GB）。
     * 超过则按 20GB 为粒度拆分为多个导出任务，用户下载中断时可只重试某一份。
     */
    private static final long EXPORT_TASK_SIZE_LIMIT = 20L * 1024 * 1024 * 1024;

    // 缓冲区优化
    private static final int DOWNLOAD_BUFFER_SIZE = 8192 * 16; // 128KB
    private static final int ZIP_BUFFER_SIZE = 8192 * 32; // 256KB
    private static final int PIPE_BUFFER_SIZE = 8192 * 64; // 512KB
    private static final boolean USE_DIRECT_BUFFER = true;

    // 内存管理
    private static final long MAX_MEMORY_FILE_SIZE = 50 * 1024 * 1024; // 50MB以下小文件
    private static final int SMALL_FILE_BATCH_SIZE = 20; // 小文件批量处理数量

    // 性能监控
    private static final int MONITOR_INTERVAL = 1000; // 监控间隔1秒

    /**
     * 阿里云函数-文件打包代码
     * 此处代码非真是代码，只是和阿里云函数上传保持代码内容一致，本地调试使用
     *
     * @param mapObject
     * @return
     */
    public Map<String, Object> handleRequest(Map<String, Object> mapObject) {
        Map<String, Object> resultMap = new HashMap<>();

        // 初始化统计信息
        ProcessingStats stats = new ProcessingStats();
        stats.startTime = System.currentTimeMillis();

        OSS ossClient = null;
        ExecutorService downloadExecutor = null;
        ScheduledExecutorService monitorExecutor = null;
        File tempDir = null;
        File zipFile = null;

        try {
            // 1. 创建优化的OSS客户端
            ossClient = createOptimizedOssClient();

            // 2. 预热连接
            warmUpConnection(ossClient);

            // 3. 解析请求参数
            JSONArray jsonArray = JSONUtil.parseArray(mapObject.get("body"));
            List<PackageFileReq> fileList = jsonArray.toList(PackageFileReq.class);

            // 4. 分析文件信息
            List<DownloadTask> allTasks = analyzeFiles(fileList, ossClient, stats);

            /*if (stats.totalFileSize > 20L * 1024 * 1024 * 1024) {
                resultMap.put("code", "fail");
                resultMap.put("failReason", "打包文件总大小超过20GB，不允许创建临时文件");
                resultMap.put("processedStats", stats != null ? stats.toMap() : new HashMap<>());
                return resultMap;
            }*/

            if (allTasks.isEmpty()) {
                return createEmptyResult();
            }

            // 5. 按每20GB拆分导出任务，避免单次任务过大
            // 规则：超过20GB就拆分；每多20GB多一个任务。
            // 示例：
            // - 39GB -> 2个任务
            // - 42GB -> 3个任务
            List<List<DownloadTask>> exportTaskGroups = splitTasksByTotalSize(allTasks, EXPORT_TASK_SIZE_LIMIT);

            if (exportTaskGroups.size() == 1) {
                // 单任务：保持返回结构兼容（url/size/fileName/processedStats）
                FileSizeCategory category = categorizeFiles(allTasks);
                System.out.println("文件分类: " + category.toString());

                downloadExecutor = createDynamicExecutor(allTasks, category);
                monitorExecutor = Executors.newSingleThreadScheduledExecutor();
                startPerformanceMonitor(downloadExecutor, stats, monitorExecutor);

                return processFilesStreamingAndUpload(
                        allTasks, ossClient, downloadExecutor, stats, category
                );
            }

            // 多任务：逐个任务生成ZIP并返回列表
            // 注意：这里不做“续传”，也不做“并行导出多个任务”，而是串行执行多个任务。
            // 这样便于控制资源占用（线程/带宽/OSS连接），并且用户若下载中断，可只重试对应任务。
            List<Map<String, Object>> exports = new ArrayList<>();
            for (int i = 0; i < exportTaskGroups.size(); i++) {
                List<DownloadTask> groupTasks = exportTaskGroups.get(i);

                // 每个任务独立统计（避免多个任务共用同一个 stats 导致数据混乱）
                ProcessingStats groupStats = new ProcessingStats();
                groupStats.startTime = System.currentTimeMillis();
                for (DownloadTask t : groupTasks) {
                    groupStats.totalFiles++;
                    groupStats.totalFileSize += t.fileSize;
                }

                FileSizeCategory groupCategory = categorizeFiles(groupTasks);
                System.out.println("任务" + (i + 1) + "/" + exportTaskGroups.size() + " 文件分类: " + groupCategory.toString());

                ExecutorService groupExecutor = null;
                ScheduledExecutorService groupMonitorExecutor = null;
                try {
                    groupExecutor = createDynamicExecutor(groupTasks, groupCategory);
                    groupMonitorExecutor = Executors.newSingleThreadScheduledExecutor();
                    startPerformanceMonitor(groupExecutor, groupStats, groupMonitorExecutor);

                    // 单任务导出：生成一个 ZIP 并上传 OSS，返回一个下载地址
                    Map<String, Object> exportResult = processFilesStreamingAndUpload(
                            groupTasks, ossClient, groupExecutor, groupStats, groupCategory
                    );
                    exports.add(exportResult);
                } finally {
                    // 注意：此处不关闭 ossClient（外层 finally 统一关闭），只关闭任务自身的线程池与监控线程。
                    cleanupResources(null, groupExecutor, groupMonitorExecutor, null, groupStats, null);
                }
            }

            // 多任务统一返回：exports 为多个任务的结果列表。
            // 单任务仍沿用老字段(url/size/fileName/processedStats)，避免影响已有调用方。
            Map<String, Object> multiResult = new HashMap<>();
            multiResult.put("code", "success");
            multiResult.put("exports", exports);
            return multiResult;
        } catch (Exception e) {
            System.err.println("文件打包处理失败: " + e.getMessage());
            e.printStackTrace();
            resultMap.put("code", "fail");
            resultMap.put("failReason", e.getMessage());
            resultMap.put("processedStats", stats != null ? stats.toMap() : new HashMap<>());
            return resultMap;
        } finally {
            // 清理资源：不再需要 tempDir 和 zipFile
            cleanupResources(ossClient, downloadExecutor, monitorExecutor, null, stats, null);
        }
    }

    /**
     * 分析文件信息并创建任务列表
     */
    private List<DownloadTask> analyzeFiles(List<PackageFileReq> fileList, OSS ossClient,
                                            ProcessingStats stats) throws Exception {
        List<DownloadTask> tasks = new ArrayList<>();

        for (PackageFileReq fileInfo : fileList) {
            Map<String, String> urlMap = fileInfo.getUrlMap();
            if (CollUtil.isEmpty(urlMap)) {
                continue;
            }

            for (Map.Entry<String, String> entry : urlMap.entrySet()) {
                String ossFilePath = normalizeManagedObjectKey(entry.getKey());

                // 获取文件大小（带重试）
                long fileSize = executeWithRetry(
                        () -> ossClient.getObjectMetadata(ossConfig.getBucketName(), ossFilePath).getContentLength(),
                        "获取文件大小: " + ossFilePath,
                        MAX_RETRIES,
                        BASE_RETRY_DELAY_MS
                );

                String fileDir = fileInfo.getFileDir();
                String fileName = entry.getValue();
                String zipEntryName;
                if (StringUtils.isNotEmpty(fileDir)) {
                    String normalizedDir = fileDir.replace("\\\\", "/");
                    while (normalizedDir.startsWith("/")) {
                        normalizedDir = normalizedDir.substring(1);
                    }
                    while (normalizedDir.endsWith("/")) {
                        normalizedDir = normalizedDir.substring(0, normalizedDir.length() - 1);
                    }
                    zipEntryName = normalizedDir + "/" + fileName;
                } else {
                    zipEntryName = fileName;
                }
                tasks.add(new DownloadTask(
                        ossFilePath,
                        zipEntryName,
                        fileSize
                ));

                stats.totalFiles++;
                stats.totalFileSize += fileSize;
            }
        }

        System.out.printf("分析完成: %d个文件, 总大小: %.2f GB%n",
                tasks.size(), stats.totalFileSize / (1024.0 * 1024.0 * 1024.0));

        return tasks;
    }

    /**
     * 根据文件大小分类
     */
    private FileSizeCategory categorizeFiles(List<DownloadTask> tasks) {
        FileSizeCategory category = new FileSizeCategory();

        for (DownloadTask task : tasks) {
            if (task.fileSize < 1024 * 1024) { // < 1MB
                category.smallFiles.add(task);
            } else if (task.fileSize < 10 * 1024 * 1024) { // 1-10MB
                category.mediumFiles.add(task);
            } else if (task.fileSize < 100 * 1024 * 1024) { // 10-100MB
                category.largeFiles.add(task);
            } else { // > 100MB
                category.veryLargeFiles.add(task);
            }
        }

        return category;
    }

    /**
     * 优化的文件处理方法
     */
    private Map<String, Object> processFilesOptimized(List<DownloadTask> tasks, OSS ossClient,
                                                      File zipFile, ExecutorService executor,
                                                      ProcessingStats stats, FileSizeCategory category)
            throws Exception {

        System.out.println("开始并行处理文件...");

        // 创建ZIP输出流
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos, ZIP_BUFFER_SIZE);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            zos.setLevel(Deflater.BEST_SPEED); // 使用最快压缩

            // 创建同步锁
            Object zipLock = new Object();

            // 1. 首先处理小文件（批量并行）
            processSmallFilesBatch(category.smallFiles, ossClient, zos, zipLock, executor, stats);

            // 2. 处理中等文件
            processMediumFiles(category.mediumFiles, ossClient, zos, zipLock, executor, stats);

            // 3. 处理大文件（串行或少量并行）
            processLargeFiles(category.largeFiles, ossClient, zos, zipLock, executor, stats);

            // 4. 处理超大文件（单个处理）
            processVeryLargeFiles(category.veryLargeFiles, ossClient, zos, zipLock, executor, stats);

            // 确保所有数据写入
            zos.flush();
            bos.flush();
            fos.getFD().sync();
        }

        // 获取最终文件大小
        stats.totalSize = zipFile.length();
        System.out.printf("ZIP文件创建完成，大小: %.2f GB%n",
                stats.totalSize / (1024.0 * 1024.0 * 1024.0));

        // 并发上传到OSS
        String downloadUrl = uploadToOssConcurrent(zipFile, ossClient, stats);
        //生成的地址是内网地址，替换为外网域名
        if (downloadUrl.contains("-internal")) {
            downloadUrl = downloadUrl.replace("-internal", "");
        }
        return createSuccessResult(downloadUrl, stats.totalSize, generateExportName(), stats);
    }

    /**
     * 批量处理小文件
     */
    private void processSmallFilesBatch(List<DownloadTask> smallFiles, OSS ossClient,
                                        ZipOutputStream zos, Object zipLock,
                                        ExecutorService executor, ProcessingStats stats) {

        if (smallFiles.isEmpty()) return;

        System.out.println("批量处理小文件: " + smallFiles.size() + "个");

        // 使用CompletableFuture实现批量并行处理
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < smallFiles.size(); i += SMALL_FILE_BATCH_SIZE) {
            int end = Math.min(i + SMALL_FILE_BATCH_SIZE, smallFiles.size());
            List<DownloadTask> batch = smallFiles.subList(i, end);

            CompletableFuture<Void> batchFuture = CompletableFuture.runAsync(() -> {
                for (DownloadTask task : batch) {
                    try {
                        downloadAndZipDirect(ossClient, task, zos, zipLock, stats);
                    } catch (Exception e) {
                        System.err.println("小文件处理失败: " + task.zipEntryName);
                        stats.failedFiles++;
                    }
                }
            }, executor);

            futures.add(batchFuture);
        }

        // 等待所有批次完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("小文件批量处理完成");
    }

    /**
     * 处理中等文件
     */
    private void processMediumFiles(List<DownloadTask> mediumFiles, OSS ossClient,
                                    ZipOutputStream zos, Object zipLock,
                                    ExecutorService executor, ProcessingStats stats) {

        if (mediumFiles.isEmpty()) return;

        System.out.println("处理中等文件: " + mediumFiles.size() + "个");

        CountDownLatch latch = new CountDownLatch(mediumFiles.size());

        for (DownloadTask task : mediumFiles) {
            executor.submit(() -> {
                try {
                    downloadAndZipWithPipe(ossClient, task, zos, zipLock, stats);
                    stats.successfulFiles++;
                } catch (Exception e) {
                    System.err.println("中等文件处理失败: " + task.zipEntryName);
                    stats.failedFiles++;
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(DOWNLOAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理大文件
     */
    private void processLargeFiles(List<DownloadTask> largeFiles, OSS ossClient,
                                   ZipOutputStream zos, Object zipLock,
                                   ExecutorService executor, ProcessingStats stats) {

        if (largeFiles.isEmpty()) return;

        System.out.println("处理大文件: " + largeFiles.size() + "个");

        // 限制大文件并发数
        Semaphore semaphore = new Semaphore(Math.max(2, Runtime.getRuntime().availableProcessors()));

        List<Future<?>> futures = new ArrayList<>();

        for (DownloadTask task : largeFiles) {
            futures.add(executor.submit(() -> {
                try {
                    semaphore.acquire();
                    downloadAndZipStreaming(ossClient, task, zos, zipLock, stats);
                    stats.successfulFiles++;
                } catch (Exception e) {
                    System.err.println("大文件处理失败: " + task.zipEntryName);
                    stats.failedFiles++;
                } finally {
                    semaphore.release();
                }
            }));
        }

        // 等待所有大文件完成
        for (Future<?> future : futures) {
            try {
                future.get(DOWNLOAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                future.cancel(true);
            }
        }
    }

    /**
     * 处理超大文件
     */
    private void processVeryLargeFiles(List<DownloadTask> veryLargeFiles, OSS ossClient,
                                       ZipOutputStream zos, Object zipLock,
                                       ExecutorService executor, ProcessingStats stats) {

        if (veryLargeFiles.isEmpty()) return;

        System.out.println("处理超大文件: " + veryLargeFiles.size() + "个");

        // 超大文件串行处理，避免内存问题
        for (DownloadTask task : veryLargeFiles) {
            try {
                downloadAndZipStreaming(ossClient, task, zos, zipLock, stats);
                stats.successfulFiles++;
                // 处理完一个超大文件后强制GC
                System.gc();
                Thread.sleep(100); // 给GC一点时间
            } catch (Exception e) {
                System.err.println("超大文件处理失败: " + task.zipEntryName);
                stats.failedFiles++;
            }
        }
    }

    /**
     * 直接下载并压缩（适合小文件）
     */
    private void downloadAndZipDirect(OSS ossClient, DownloadTask task,
                                      ZipOutputStream zos, Object zipLock,
                                      ProcessingStats stats) throws Exception {

        try (OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), task.ossFilePath);
             InputStream input = ossObject.getObjectContent()) {

            // 读取完整内容
            byte[] content = readFully(input, (int) Math.min(task.fileSize, Integer.MAX_VALUE));

            synchronized (zipLock) {
                zos.putNextEntry(new ZipEntry(task.zipEntryName));
                zos.write(content);
                zos.closeEntry();
            }

            stats.totalDownloaded.addAndGet(content.length);

        } catch (Exception e) {
            throw new Exception("处理文件失败: " + task.zipEntryName, e);
        }
    }

    /**
     * 使用管道流式处理
     */
    private void downloadAndZipWithPipe(OSS ossClient, DownloadTask task,
                                        ZipOutputStream zos, Object zipLock,
                                        ProcessingStats stats) throws Exception {

        synchronized (zipLock) {
            zos.putNextEntry(new ZipEntry(task.zipEntryName));

            try (OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), task.ossFilePath);
                 InputStream input = ossObject.getObjectContent()) {

                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                    stats.totalDownloaded.addAndGet(bytesRead);
                }

            } catch (Exception e) {
                throw new Exception("处理文件失败: " + task.zipEntryName, e);
            } finally {
                zos.closeEntry();
            }
        }
    }

    /**
     * 流式处理（适合大文件）
     */
    private void downloadAndZipStreaming(OSS ossClient, DownloadTask task,
                                         ZipOutputStream zos, Object zipLock,
                                         ProcessingStats stats) throws Exception {

        synchronized (zipLock) {
            zos.putNextEntry(new ZipEntry(task.zipEntryName));

            try (OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), task.ossFilePath);
                 InputStream input = ossObject.getObjectContent()) {

                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                int bytesRead;
                long totalWritten = 0;
                long lastPrint = 0;

                while ((bytesRead = input.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                    totalWritten += bytesRead;
                    stats.totalDownloaded.addAndGet(bytesRead);

                    // 每10MB打印一次进度
                    if (totalWritten - lastPrint > 10 * 1024 * 1024) {
                        System.out.printf("文件 %s 进度: %.1f%% (%.2f/%.2f MB)%n",
                                task.zipEntryName,
                                (totalWritten * 100.0) / task.fileSize,
                                totalWritten / (1024.0 * 1024.0),
                                task.fileSize / (1024.0 * 1024.0));
                        lastPrint = totalWritten;

                        // 定期刷新和检查内存
                        zos.flush();
                        checkMemoryUsage();
                    }
                }

                zos.closeEntry();
                zos.flush();

                System.out.printf("文件 %s 处理完成，大小: %.2f MB%n",
                        task.zipEntryName, totalWritten / (1024.0 * 1024.0));

            } catch (Exception e) {
                throw new Exception("流式处理失败: " + task.zipEntryName, e);
            }
        }
    }

    /**
     * 并发上传到OSS
     */
    private String uploadToOssConcurrent(File zipFile, OSS ossClient, ProcessingStats stats) throws Exception {
        System.out.println("开始并发上传到OSS...");

        String exportName = generateExportName();
        String zipFileName = "fileExport/" + IdUtil.getSnowflakeNextIdStr() + "/" + exportName + ".zip";

        long fileSize = zipFile.length();
        long uploadStartTime = System.currentTimeMillis();

        // 根据文件大小选择上传方式
        if (fileSize > 100 * 1024 * 1024) { // 大于100MB使用分片上传
            uploadMultipartConcurrent(ossConfig.getBucketName(), zipFileName, zipFile, ossClient, stats);
        } else {
            // 直接上传
            uploadSingleFile(ossConfig.getBucketName(), zipFileName, zipFile, ossClient);
        }

        long uploadTime = System.currentTimeMillis() - uploadStartTime;
        double uploadSpeed = fileSize / (1024.0 * 1024.0) / (uploadTime / 1000.0);

        System.out.printf("上传完成，耗时: %.1fs, 速度: %.2f MB/s%n",
                uploadTime / 1000.0, uploadSpeed);

        // 生成下载链接
        return generatePresignedUrl(ossClient, zipFileName, 3600);
    }

    /**
     * 并发分片上传
     */
    private void uploadMultipartConcurrent(String bucketName, String objectName,
                                           File zipFile, OSS ossClient,
                                           ProcessingStats stats) throws Exception {

        // 1. 初始化分片上传
        String uploadId = ossClient.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(bucketName, objectName)
        ).getUploadId();

        long fileLength = zipFile.length();
        int optimalPartSize = calculateOptimalPartSize(fileLength);
        int partCount = (int) ((fileLength + optimalPartSize - 1) / optimalPartSize);

        System.out.printf("并发分片上传，分片数: %d, 分片大小: %d MB, 总大小: %.2f GB%n",
                partCount, optimalPartSize / (1024 * 1024),
                fileLength / (1024.0 * 1024.0 * 1024.0));

        // 2. 创建并发上传线程池
        ThreadPoolExecutor uploadExecutor = new ThreadPoolExecutor(
                Math.min(partCount, MAX_CONCURRENT_PART_UPLOADS),
                Math.min(partCount, MAX_CONCURRENT_PART_UPLOADS),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try {
            // 3. 提交所有分片上传任务
            List<Future<PartETag>> futures = new ArrayList<>(partCount);

            try (RandomAccessFile raf = new RandomAccessFile(zipFile, "r")) {
                for (int i = 0; i < partCount; i++) {
                    final int partNumber = i + 1;
                    final long startPos = i * (long) optimalPartSize;
                    final int curPartSize = (partNumber == partCount) ?
                            (int) (fileLength - startPos) : optimalPartSize;

                    futures.add(uploadExecutor.submit(() -> {
                        try {
                            // 读取分片数据
                            byte[] partData = new byte[curPartSize];
                            synchronized (raf) {
                                raf.seek(startPos);
                                raf.readFully(partData);
                            }

                            // 上传分片
                            try (ByteArrayInputStream bis = new ByteArrayInputStream(partData)) {
                                UploadPartRequest request = new UploadPartRequest();
                                request.setBucketName(bucketName);
                                request.setKey(objectName);
                                request.setUploadId(uploadId);
                                request.setInputStream(bis);
                                request.setPartSize(curPartSize);
                                request.setPartNumber(partNumber);

                                UploadPartResult result = ossClient.uploadPart(request);

                                System.out.printf("分片 %d/%d 上传完成 (%.2f MB)%n",
                                        partNumber, partCount, curPartSize / (1024.0 * 1024.0));

                                return result.getPartETag();
                            }

                        } catch (Exception e) {
                            throw new RuntimeException("上传分片失败: " + partNumber, e);
                        }
                    }));
                }

                // 4. 收集所有分片结果
                List<PartETag> partETags = new ArrayList<>(partCount);
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        PartETag partETag = futures.get(i).get(5, TimeUnit.MINUTES);
                        partETags.add(partETag);

                        // 更新进度
                        if ((i + 1) % 10 == 0 || i + 1 == futures.size()) {
                            System.out.printf("上传进度: %d/%d (%.1f%%)%n",
                                    i + 1, futures.size(), (i + 1) * 100.0 / futures.size());
                        }

                    } catch (Exception e) {
                        // 取消所有任务并中止上传
                        futures.forEach(f -> f.cancel(true));
                        abortMultipartUpload(bucketName, objectName, uploadId, ossClient);
                        throw e;
                    }
                }

                // 按分片号排序
                partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));

                // 5. 完成分片上传
                CompleteMultipartUploadRequest completeRequest =
                        new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
                ossClient.completeMultipartUpload(completeRequest);

                System.out.println("分片上传完成");

            }

        } finally {
            shutdownExecutor(uploadExecutor, "上传线程池");
        }
    }

    /**
     * 计算最优分片大小
     */
    private int calculateOptimalPartSize(long fileSize) {
        if (fileSize <= 100 * 1024 * 1024) { // <= 100MB
            return 5 * 1024 * 1024; // 5MB
        } else if (fileSize <= 1024 * 1024 * 1024) { // <= 1GB
            return 10 * 1024 * 1024; // 10MB
        } else if (fileSize <= 5 * 1024 * 1024 * 1024L) { // <= 5GB
            return 20 * 1024 * 1024; // 20MB
        } else {
            return 50 * 1024 * 1024; // 50MB
        }
    }

    /**
     * 创建优化的OSS客户端
     */
    private OSS createOptimizedOssClient() {
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();

        // 关键优化参数
        config.setMaxConnections(300);           // 增加最大连接数
        config.setMaxErrorRetry(3);              // 重试次数
        config.setConnectionTimeout(30000);      // 连接超时30秒
        config.setSocketTimeout(300000);         // Socket超时5分钟（大文件需要）
        config.setIdleConnectionTime(10000);
        config.setConnectionRequestTimeout(10000);
        config.setSupportCname(false);
        config.setUseSystemPropertyValues(false);

        // 开启连接池和压缩
        System.setProperty("http.maxConnections", "200");

        return new OSSClientBuilder().build(
                ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret(), config);
    }

    /**
     * 预热连接
     */
    private void warmUpConnection(OSS ossClient) {
        try {
            // 执行一个简单的操作来预热连接池
            ossClient.getObjectMetadata(ossConfig.getBucketName(), "warmup-test");
            System.out.println("OSS连接预热完成");
        } catch (Exception e) {
            // 预热失败不影响正常流程
            System.out.println("OSS连接预热失败: " + e.getMessage());
        }
    }

    /**
     * 创建动态线程池
     */
    private ExecutorService createDynamicExecutor(List<DownloadTask> tasks, FileSizeCategory category) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // 根据文件特征动态调整线程数
        int corePoolSize;
        if (category.veryLargeFiles.size() > 0) {
            // 有超大文件，减少并发避免内存溢出
            corePoolSize = Math.max(4, availableProcessors);
        } else if (category.largeFiles.size() > tasks.size() * 0.3) {
            // 大文件较多
            corePoolSize = Math.max(8, availableProcessors * 2);
        } else {
            // 小文件为主，提高并发
            corePoolSize = Math.max(16, availableProcessors * 4);
        }

        corePoolSize = Math.min(corePoolSize, MAX_CONCURRENT_DOWNLOADS);

        System.out.printf("创建动态线程池: 核心线程数=%d, CPU核心数=%d%n",
                corePoolSize, availableProcessors);

        return new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 启动性能监控
     */
    private void startPerformanceMonitor(ExecutorService executor,
                                         ProcessingStats stats,
                                         ScheduledExecutorService monitorExecutor) {

        monitorExecutor.scheduleAtFixedRate(() -> {
            try {
                if (executor instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;

                    long elapsed = System.currentTimeMillis() - stats.startTime;
                    long downloadedMB = stats.totalDownloaded.get() / (1024 * 1024);
                    double speed = elapsed > 0 ? downloadedMB / (elapsed / 1000.0) : 0;

                    System.out.printf("[性能监控] 活跃线程: %d, 队列: %d, 完成: %d, " +
                                    "下载速度: %.2f MB/s, 内存使用: %.1f%%%n",
                            tpe.getActiveCount(),
                            tpe.getQueue().size(),
                            tpe.getCompletedTaskCount(),
                            speed,
                            getMemoryUsagePercentage());

                    // 动态调整（如果速度过慢）
                    if (speed < 10.0 && elapsed > 30000) { // 30秒后速度仍低于10MB/s
                        int currentSize = tpe.getCorePoolSize();
                        if (currentSize < MAX_CONCURRENT_DOWNLOADS) {
                            tpe.setCorePoolSize(currentSize + 2);
                            tpe.setMaximumPoolSize(currentSize + 2);
                            System.out.println("[动态调整] 增加线程数至: " + (currentSize + 2));
                        }
                    }
                }
            } catch (Exception e) {
                // 监控失败不影响主流程
                System.err.println("性能监控异常: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private int calculateStreamingPartSize(long totalFileSize) {
        return STREAMING_PART_SIZE;
    }

    /**
     * 创建内存优化的临时文件
     */
    private File createMemoryOptimizedZipFile(File tempDir) throws IOException {
        String fileName = "package_" + System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID() + ".zip";
        File zipFile = new File(tempDir, fileName);

        // 预分配空间（可选，可以提高性能）
        if (tempDir.getFreeSpace() > 10L * 1024 * 1024 * 1024) { // 磁盘空间充足
            try (RandomAccessFile raf = new RandomAccessFile(zipFile, "rw")) {
                // 预分配100MB空间（可以根据实际情况调整）
                raf.setLength(100L * 1024 * 1024);
            }
        }

        return zipFile;
    }

    /**
     * 读取完整输入流
     */
    private byte[] readFully(InputStream input, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        return baos.toByteArray();
    }

    /**
     * 检查内存使用
     */
    private void checkMemoryUsage() {
        double usage = getMemoryUsagePercentage();

        if (usage > 80.0) {
            System.out.printf("[内存警告] 使用率: %.1f%%，触发GC%n", usage);
            System.gc();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            double afterGC = getMemoryUsagePercentage();
            System.out.printf("[内存警告] GC后使用率: %.1f%%%n", afterGC);
        }
    }

    /**
     * 获取内存使用百分比
     */
    private double getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        return (usedMemory * 100.0) / maxMemory;
    }

    /**
     * 带重试的执行
     */
    private <T> T executeWithRetry(Supplier<T> task, String operationName,
                                   int maxRetries, long baseDelayMs) {
        int retryCount = 0;
        while (true) {
            try {
                return task.get();
            } catch (Exception e) {
                retryCount++;
                if (retryCount > maxRetries || !isRetryableException(e)) {
                    throw new RuntimeException(operationName + " 失败，已达最大重试次数: " + retryCount, e);
                }

                long delay = baseDelayMs * (1L << (retryCount - 1));
                delay = Math.min(delay, 30000);

                System.out.printf("%s 失败，第 %d 次重试，等待 %d ms%n",
                        operationName, retryCount, delay);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(operationName + " 重试被中断", ie);
                }
            }
        }
    }

    /**
     * 判断是否可重试异常
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof IOException ||
                e instanceof InterruptedException ||
                e.getMessage() != null && (
                        e.getMessage().contains("timeout") ||
                                e.getMessage().contains("Timeout") ||
                                e.getMessage().contains("连接") ||
                                e.getMessage().contains("网络") ||
                                e.getMessage().contains("Socket")
                );
    }

    /**
     * 清理资源
     */
    private void cleanupResources(OSS ossClient, ExecutorService downloadExecutor,
                                  ScheduledExecutorService monitorExecutor,
                                  File tempDir, ProcessingStats stats,
                                  File zipFile) {

        // 1. 停止监控
        if (monitorExecutor != null) {
            monitorExecutor.shutdownNow();
        }

        // 2. 关闭下载线程池
        if (downloadExecutor != null) {
            shutdownExecutor(downloadExecutor, "下载线程池");
        }

        // 3. 关闭OSS客户端
        if (ossClient != null) {
            try {
                ossClient.shutdown();
            } catch (Exception e) {
                System.err.println("关闭OSS客户端失败: " + e.getMessage());
            }
        }

        // 4. 删除临时文件
        if (zipFile != null && zipFile.exists()) {
            try {
                boolean deleted = zipFile.delete();
                if (deleted) {
                    System.out.println("已删除临时ZIP文件: " + zipFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("删除临时ZIP文件失败: " + e.getMessage());
            }
        }

        // 5. 清理临时目录
        if (tempDir != null && tempDir.exists()) {
            try {
                FileUtil.del(tempDir);
                System.out.println("已清理临时目录: " + tempDir.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("清理临时目录失败: " + e.getMessage());
            }
        }

        // 6. 输出统计信息
        if (stats != null) {
            stats.endTime = System.currentTimeMillis();
            stats.printStats();
        }

        // 7. 强制GC
        System.gc();
    }

    /**
     * 优雅关闭线程池
     */
    private void shutdownExecutor(ExecutorService executor, String poolName) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.out.println(poolName + " 关闭超时，强制关闭");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.out.println("关闭 " + poolName + " 时被中断");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 辅助方法（与原始代码相同）
     */
    private File createTempDirectory() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"),
                "zip_temp_" + System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID());
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("创建临时目录失败: " + tempDir.getAbsolutePath());
        }
        System.out.println("创建临时目录: " + tempDir.getAbsolutePath());
        return tempDir;
    }

    private String generateExportName() {
        String currentTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "文件导出" + currentTime;
    }

    private String generatePresignedUrl(OSS ossClient, String objectName, int expireSeconds) {
        return executeWithRetry(() -> {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(), objectName);
            req.setExpiration(expiration);
            String url = ossClient.generatePresignedUrl(req).toString();
            System.out.println("生成url：" + url);
            return url;
        }, "生成预签名URL", MAX_RETRIES, BASE_RETRY_DELAY_MS);
    }

    private void abortMultipartUpload(String bucketName, String objectName,
                                      String uploadId, OSS ossClient) {
        try {
            AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(
                    bucketName, objectName, uploadId);
            ossClient.abortMultipartUpload(abortRequest);
            System.out.println("已取消分片上传: " + objectName);
        } catch (Exception e) {
            System.err.println("取消分片上传失败: " + e.getMessage());
        }
    }

    private void uploadSingleFile(String bucketName, String objectName,
                                  File zipFile, OSS ossClient) throws Exception {
        executeWithRetry(() -> {
            try (FileInputStream fis = new FileInputStream(zipFile)) {
                ossClient.putObject(bucketName, objectName, fis);
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "上传ZIP文件到OSS", MAX_RETRIES, BASE_RETRY_DELAY_MS);
    }

    private Map<String, Object> createSuccessResult(String url, long size,
                                                    String fileName, ProcessingStats stats) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "success");
        result.put("url", url);
        result.put("size", Long.toString(size));
        result.put("fileName", fileName);
        result.put("processedStats", stats.toMap());
        return result;
    }

    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "success");
        result.put("url", "");
        result.put("size", "0");
        result.put("fileName", "");
        return result;
    }

    /**
     * 内部类：下载任务
     */
    private static class DownloadTask {
        String ossFilePath;
        String zipEntryName;
        long fileSize;

        DownloadTask(String ossFilePath, String zipEntryName, long fileSize) {
            this.ossFilePath = ossFilePath;
            this.zipEntryName = zipEntryName;
            this.fileSize = fileSize;
        }
    }

    /**
     * 内部类：文件大小分类
     */
    private static class FileSizeCategory {
        List<DownloadTask> smallFiles = new ArrayList<>();     // < 1MB
        List<DownloadTask> mediumFiles = new ArrayList<>();    // 1-10MB
        List<DownloadTask> largeFiles = new ArrayList<>();     // 10-100MB
        List<DownloadTask> veryLargeFiles = new ArrayList<>(); // > 100MB

        @Override
        public String toString() {
            return String.format("小文件(%d), 中文件(%d), 大文件(%d), 超大文件(%d)",
                    smallFiles.size(), mediumFiles.size(),
                    largeFiles.size(), veryLargeFiles.size());
        }
    }

    /**
     * 内部类：处理统计
     */
    private static class ProcessingStats {
        long startTime;
        long endTime;
        long totalSize;
        long totalFileSize;
        int totalFiles = 0;
        AtomicLong totalDownloaded = new AtomicLong(0);
        int successfulFiles = 0;
        int failedFiles = 0;

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("totalTimeMs", endTime - startTime);
            map.put("totalSize", totalSize);
            map.put("totalFileSize", totalFileSize);
            map.put("totalFiles", totalFiles);
            map.put("totalDownloaded", totalDownloaded.get());
            map.put("successfulFiles", successfulFiles);
            map.put("failedFiles", failedFiles);
            return map;
        }

        void printStats() {
            long totalTime = endTime - startTime;
            System.out.printf(
                    "\n========== 处理统计 ==========\n" +
                            "总耗时: %.1f秒\n" +
                            "总文件数: %d个\n" +
                            "成功文件: %d个\n" +
                            "失败文件: %d个\n" +
                            "总文件大小: %.2f GB\n" +
                            "总ZIP大小: %.2f GB\n" +
                            "总下载量: %.2f GB\n" +
                            "下载速度: %.2f MB/s\n" +
                            "压缩率: %.1f%%\n" +
                            "============================\n\n",
                    totalTime / 1000.0,
                    totalFiles,
                    successfulFiles,
                    failedFiles,
                    totalFileSize / (1024.0 * 1024.0 * 1024.0),
                    totalSize / (1024.0 * 1024.0 * 1024.0),
                    totalDownloaded.get() / (1024.0 * 1024.0 * 1024.0),
                    totalTime > 0 ?
                            (totalDownloaded.get() / (1024.0 * 1024.0)) / (totalTime / 1000.0) : 0,
                    totalFileSize > 0 ?
                            (1.0 - (double) totalSize / totalFileSize) * 100 : 0
            );
        }
    }
}
