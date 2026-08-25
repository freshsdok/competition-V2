package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.poi.InMemoryMultipartFile;
import com.teaching.competition.domain.CertificateExportRequest;
import com.teaching.competition.domain.CertificateExportTask;
import com.teaching.competition.domain.CertificateImageCache;
import com.teaching.competition.mapper.CertificateImageCacheMapper;
import com.teaching.competition.service.CertificateExportService;
import com.teaching.competition.service.CertificateImageCacheService;
import com.teaching.system.api.RemoteOssUploadService;
import com.teaching.system.api.domain.PackageFileReq;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CertificateExportServiceImpl implements CertificateExportService {
    private static final Logger log = LoggerFactory.getLogger(CertificateExportServiceImpl.class);
    private static final int MAX_SELECTED_CERTIFICATES = 500;
    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger(1);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final CertificateImageCacheMapper cacheMapper;
    private final CertificateImageCacheService cacheService;
    private final RemoteOssUploadService ossUploadService;
    private final RedissonClient redissonClient;
    private final ExecutorService exportExecutor;

    public CertificateExportServiceImpl(CertificateImageCacheMapper cacheMapper,
                                        CertificateImageCacheService cacheService,
                                        RemoteOssUploadService ossUploadService,
                                        RedissonClient redissonClient) {
        this.cacheMapper = cacheMapper;
        this.cacheService = cacheService;
        this.ossUploadService = ossUploadService;
        this.redissonClient = redissonClient;
        this.exportExecutor = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), task -> {
                    Thread thread = new Thread(task,
                            "certificate-export-" + THREAD_SEQUENCE.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    @PostConstruct
    public void recoverInterruptedTasks() {
        for (CertificateExportTask task : cacheMapper.selectRecoverableExportTasks()) {
            try {
                exportExecutor.execute(() -> executeTask(task.getTaskId()));
            } catch (RuntimeException exception) {
                log.warn("恢复证书导出任务入队失败，taskId={}", task.getTaskId(), exception);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        exportExecutor.shutdownNow();
    }

    @Override
    public CertificateExportTask createTask(Long userId, CertificateExportRequest request) {
        validateUserId(userId);
        cacheService.seedGuidedCertificates(userId);
        if (request == null) {
            throw new GlobalException("导出参数不能为空");
        }
        String scope = "SELECTED".equalsIgnoreCase(request.getScope()) ? "SELECTED" : "ALL";
        List<String> codes;
        if ("ALL".equals(scope)) {
            codes = cacheMapper.selectAllAuthorizedCodes(userId);
        } else {
            List<String> requested = normalizeCodes(request.getCertCodes());
            if (requested.isEmpty()) {
                throw new GlobalException("请选择需要导出的证书");
            }
            codes = cacheMapper.selectAuthorizedCodes(userId, requested);
            if (codes.size() != requested.size()) {
                throw new GlobalException("选中项包含无权导出的证书编号");
            }
        }
        if (codes.isEmpty()) {
            throw new GlobalException("当前账号暂无可导出的团队证书");
        }

        CertificateExportTask task = new CertificateExportTask();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setUserId(userId);
        task.setExportScope(scope);
        task.setSelectedCertCodes(JSON.toJSONString(codes));
        task.setTaskStatus(CertificateExportTask.QUEUED);
        task.setPhase("等待处理");
        task.setProgress(0);
        task.setTotalCount((long) codes.size());
        task.setProcessedCount(0L);
        task.setSuccessCount(0L);
        task.setFailureCount(0L);
        cacheMapper.insertExportTask(task);
        try {
            exportExecutor.execute(() -> executeTask(task.getTaskId()));
        } catch (RuntimeException exception) {
            task.setTaskStatus(CertificateExportTask.FAILED);
            task.setPhase("创建任务失败");
            task.setLastError("导出任务队列已满，请稍后重试");
            task.setFinishTime(new Date());
            cacheMapper.updateExportTask(task);
            throw new GlobalException(task.getLastError());
        }
        return task;
    }

    @Override
    public CertificateExportTask getTask(Long userId, String taskId) {
        validateUserId(userId);
        CertificateExportTask task = cacheMapper.selectExportTask(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new GlobalException("导出任务不存在或无权访问");
        }
        return task;
    }

    @Override
    public Map<String, String> getDownload(Long userId, String taskId) {
        CertificateExportTask task = getTask(userId, taskId);
        if (!CertificateExportTask.COMPLETED.equals(task.getTaskStatus())
                && !CertificateExportTask.PARTIAL.equals(task.getTaskStatus())) {
            throw new GlobalException("压缩包尚未生成完成");
        }
        if (task.getExpiresAt() == null || task.getExpiresAt().before(new Date())) {
            throw new GlobalException("压缩包已过期，请重新创建导出任务");
        }
        if (StringUtils.isBlank(task.getZipObjectKey())) {
            throw new GlobalException("压缩包对象不存在");
        }
        R<String> signed = ossUploadService.getPresignedUrl(task.getZipObjectKey());
        if (signed == null || R.isError(signed) || StringUtils.isBlank(signed.getData())) {
            throw new GlobalException("生成压缩包下载地址失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("downloadUrl", signed.getData());
        result.put("fileName", task.getZipFileName());
        return result;
    }

    @Scheduled(cron = "${competition.certificate.image.export-clean-cron:0 20 * * * ?}",
            zone = "Asia/Shanghai")
    public void cleanExpiredExports() {
        List<CertificateExportTask> expired = cacheMapper.selectExpiredExportTasks(new Date(), 100);
        for (CertificateExportTask task : expired) {
            try {
                if (StringUtils.isNotBlank(task.getZipObjectKey())) {
                    R<Boolean> deleted = ossUploadService.deleteFile(task.getZipObjectKey());
                    if (deleted == null || R.isError(deleted)) {
                        log.warn("删除过期证书ZIP失败，taskId={}", task.getTaskId());
                        continue;
                    }
                }
                cacheMapper.markExportExpired(task.getTaskId());
            } catch (RuntimeException exception) {
                log.warn("清理过期证书ZIP异常，taskId={}", task.getTaskId(), exception);
            }
        }
    }

    private void executeTask(String taskId) {
        RLock taskLock = redissonClient.getLock("competition:certificate:export:" + taskId);
        boolean locked = false;
        CertificateExportTask task = cacheMapper.selectExportTask(taskId);
        if (task == null) {
            return;
        }
        String temporaryMissingObjectKey = null;
        try {
            locked = taskLock.tryLock();
            if (!locked) {
                return;
            }
            List<String> codes = JSON.parseObject(task.getSelectedCertCodes(),
                    new TypeReference<List<String>>() { });
            task.setTaskStatus(CertificateExportTask.RESOLVING);
            task.setPhase("补取尚未缓存的证书图片");
            task.setProgress(2);
            cacheMapper.updateExportTask(task);

            long processed = 0;
            for (String code : codes) {
                CertificateImageCache cache = cacheMapper.selectByCertCode(code);
                if (cache == null || !CertificateImageCache.SUCCESS.equals(cache.getCacheStatus())) {
                    cacheService.syncOne(code, true);
                }
                processed++;
                task.setProcessedCount(processed);
                task.setProgress(Math.min(70, 2 + (int) (68L * processed / codes.size())));
                if (processed % 5 == 0 || processed == codes.size()) {
                    cacheMapper.updateExportTask(task);
                }
            }

            List<CertificateImageCache> cached = cacheMapper.selectByCertCodes(codes);
            Map<String, CertificateImageCache> byCode = new HashMap<>();
            for (CertificateImageCache item : cached) {
                byCode.put(item.getCertCode(), item);
            }

            LinkedHashMap<String, String> files = new LinkedHashMap<>();
            List<String> missing = new ArrayList<>();
            for (String code : codes) {
                CertificateImageCache item = byCode.get(code);
                if (item != null && CertificateImageCache.SUCCESS.equals(item.getCacheStatus())
                        && StringUtils.isNotBlank(item.getObjectKey())) {
                    files.put(item.getObjectKey(), StringUtils.isBlank(item.getFileName())
                            ? code + ".jpg" : item.getFileName());
                } else {
                    missing.add(code);
                }
            }

            if (!missing.isEmpty()) {
                String failureText = "以下证书图片暂未获取成功，请稍后重试：\r\n"
                        + String.join("\r\n", missing) + "\r\n";
                R<String> upload = ossUploadService.ossUpload(new InMemoryMultipartFile(
                                failureText.getBytes(StandardCharsets.UTF_8),
                                "未下载证书编号.txt", "text/plain;charset=UTF-8"),
                        "certificate-export-temp", taskId);
                if (upload == null || R.isError(upload) || StringUtils.isBlank(upload.getData())) {
                    throw new IllegalStateException("上传未下载编号清单失败");
                }
                temporaryMissingObjectKey = upload.getData();
                files.put(temporaryMissingObjectKey, "未下载证书编号.txt");
            }

            task.setSuccessCount((long) (codes.size() - missing.size()));
            task.setFailureCount((long) missing.size());
            task.setMissingCertCodes(JSON.toJSONString(missing));
            task.setTaskStatus(CertificateExportTask.PACKAGING);
            task.setPhase("生成并上传压缩包");
            task.setProgress(80);
            cacheMapper.updateExportTask(task);

            PackageFileReq packageRequest = new PackageFileReq();
            packageRequest.setUserId(task.getUserId());
            packageRequest.setUserName(String.valueOf(task.getUserId()));
            packageRequest.setFileDir("");
            packageRequest.setUrlMap(files);
            R<Map<String, Object>> packageResponse = ossUploadService.packageFile(List.of(packageRequest));
            if (packageResponse == null || R.isError(packageResponse) || packageResponse.getData() == null) {
                throw new IllegalStateException("文件服务生成压缩包失败"
                        + (packageResponse == null ? "" : "：" + packageResponse.getMsg()));
            }
            Map<String, Object> packageData = packageResponse.getData();
            if (!"success".equals(String.valueOf(packageData.get("code")))) {
                throw new IllegalStateException("文件服务生成压缩包失败："
                        + packageData.getOrDefault("failReason", "未知错误"));
            }
            String signedUrl = String.valueOf(packageData.get("url"));
            if (StringUtils.isBlank(signedUrl) || "null".equals(signedUrl)) {
                throw new IllegalStateException("文件服务未返回压缩包地址");
            }

            task.setZipObjectKey(extractObjectKey(signedUrl));
            task.setZipFileName("学生获奖证书_" + FILE_TIME.format(Instant.now()) + ".zip");
            task.setTaskStatus(missing.isEmpty()
                    ? CertificateExportTask.COMPLETED : CertificateExportTask.PARTIAL);
            task.setPhase(missing.isEmpty() ? "导出完成" : "导出完成，部分证书未获取");
            task.setProgress(100);
            task.setFinishTime(new Date());
            task.setExpiresAt(Date.from(Instant.now().plus(Duration.ofHours(24))));
            cacheMapper.updateExportTask(task);
        } catch (RuntimeException exception) {
            if (Thread.currentThread().isInterrupted()) {
                task.setPhase("服务重启后继续导出");
                cacheMapper.updateExportTask(task);
                return;
            }
            log.error("负责人证书异步导出失败，taskId={}", taskId, exception);
            task.setTaskStatus(CertificateExportTask.FAILED);
            task.setPhase("导出失败");
            task.setLastError(shortMessage(exception));
            task.setFinishTime(new Date());
            cacheMapper.updateExportTask(task);
        } finally {
            if (StringUtils.isNotBlank(temporaryMissingObjectKey)) {
                try {
                    ossUploadService.deleteFile(temporaryMissingObjectKey);
                } catch (RuntimeException exception) {
                    log.warn("删除导出临时失败清单异常，taskId={}", taskId, exception);
                }
            }
            if (locked && taskLock.isHeldByCurrentThread()) {
                taskLock.unlock();
            }
        }
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String code : codes) {
            if (StringUtils.isNotBlank(code)) {
                unique.add(code.trim());
            }
            if (unique.size() > MAX_SELECTED_CERTIFICATES) {
                throw new GlobalException("单次最多选择" + MAX_SELECTED_CERTIFICATES + "张证书");
            }
        }
        return new ArrayList<>(unique);
    }

    private String extractObjectKey(String signedUrl) {
        try {
            String path = URI.create(signedUrl).getPath();
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (StringUtils.isBlank(path)) {
                throw new IllegalStateException("压缩包对象键为空");
            }
            return path;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("无法解析压缩包对象键", exception);
        }
    }

    private String shortMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (StringUtils.isBlank(message)) {
            message = current.getClass().getSimpleName();
        }
        return message.length() > 900 ? message.substring(0, 900) : message;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new GlobalException("登录状态已失效，请重新登录");
        }
    }
}
