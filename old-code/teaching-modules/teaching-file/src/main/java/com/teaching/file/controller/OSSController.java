package com.teaching.file.controller;

import com.teaching.common.core.domain.R;
import com.teaching.file.service.IOSSFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.teaching.system.api.domain.PackageFileReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
@RefreshScope
public class OSSController {
    private static final Logger log = LoggerFactory.getLogger(OSSController.class);
    private final IOSSFileService ossFileService;

    @Value("${oss.expireTime}")
    private long expireTime;


    /**
     * 上传文件
     *
     * @param file
     * @param bizSign
     * @param bizCode
     * @return
     */
    @PostMapping("/upload")
    public R<String> uploadFile(
            @RequestParam("file") MultipartFile file, String bizSign, String bizCode) {

        try {
            String objectKey = ossFileService.uploadFile(file, bizSign, bizCode);
            return R.ok(objectKey);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/presignedUrl")
    public R<String> getPresignedUrl(@RequestParam String fileKey, HttpServletRequest request) {
        // 判断当前用户是否有权下载fileKey对应的文件
//        if (!hasPermission(SecurityUtils.getLoginUser().getUsername(), fileKey)) {
//            return "无权访问";
//        }
        // 生成一个有效期为1小时的预签名URL
        String presignedUrl = null;
        try {
            presignedUrl = ossFileService.generatePresignedUrl(fileKey, expireTime * 1000L);

        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            return R.fail(e.getMessage());
        }
        return R.ok(presignedUrl);
    }



    @GetMapping("/previewUrl")
    public R<String> previewUrl(@RequestParam String fileKey, HttpServletRequest request) {
        String presignedUrl = null;
        try {
            presignedUrl = ossFileService.generatePresignedUrl2(fileKey, expireTime * 60 *1000L);

        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            return R.fail(e.getMessage());
        }
        return R.ok(presignedUrl);
    }

    /**
     * 上传多个文件
     *
     * @param files
     * @return
     */
    @PostMapping("/upload-multiple")
    public R<List<String>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files, String bizSign, String bizCode) {

        List<String> objectKeys = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                objectKeys.add(ossFileService.uploadFile(file, bizSign, bizCode));
            } catch (Exception e) {
                log.error("部分上传文件失败", e);
                return R.fail(e.getMessage());
            }
        }
        return R.ok(objectKeys);
    }


    /**
     * 删除文件
     *
     * @param objectKey
     * @return
     */
    @DeleteMapping("/delete")
    public R<Boolean> deleteFile(@RequestParam String objectKey) {
        try {
            ossFileService.deleteFile(objectKey);
            return R.ok(true);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 获取STS临时凭证
     *
     * @return
     */
    @GetMapping("/temporaryVoucher")
    public R<Map<String, String>> getSTSToken(@RequestParam String bizSign, String bizCode) {
        Map<String, String> data = ossFileService.generateSTSToken(bizSign, bizCode);
        if (data == null || data.isEmpty()) {
            return R.fail("获取STS临时凭证失败");
        }
        return R.ok(data, "获取STS临时凭证成功");
    }

    @PostMapping("/packageFile")
    public R<Map<String,Object>> packageFile(@RequestBody List<PackageFileReq> fileList) throws Exception {
        return R.ok(ossFileService.packageFile(fileList));
    }

    /**
     * 下载OSS文件到本地
     *
     * @param ossUrl OSS文件地址
     * @param localPath 本地存储路径
     * @return 下载结果
     */
    @PostMapping("/downloadToLocal")
    public R<Boolean> downloadOssFileToLocal(@RequestParam String ossUrl, @RequestParam String localPath) {
        try {
            boolean success = ossFileService.downloadFileToLocal(ossUrl, localPath);
            if (success) {
                return R.ok(true, "文件下载成功");
            } else {
                return R.fail("文件下载失败");
            }
        } catch (Exception e) {
            log.error("下载OSS文件到本地失败: ossUrl={}, localPath={}", ossUrl, localPath, e);
            return R.fail("下载失败: " + e.getMessage());
        }
    }

    /**
     * 上传本地文件到OSS
     *
     * @param localPath 本地文件路径
     * @param bizSign 业务标识
     * @param bizCode 业务编码
     * @return OSS文件访问地址
     */
    @PostMapping("/uploadLocalFile")
    public R<String> uploadLocalFile(
            @RequestParam String localPath,
            @RequestParam String bizSign,
            @RequestParam(required = false) String bizCode) {
        try {
            String ossUrl = ossFileService.uploadLocalFile(localPath, bizSign, bizCode);
            return R.ok(ossUrl, "文件上传成功");
        } catch (Exception e) {
            log.error("上传本地文件到OSS失败: localPath={}", localPath, e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传本地目录下所有文件到OSS
     *
     * @param localDir 本地目录路径
     * @param bizSign 业务标识
     * @param bizCode 业务编码
     * @param recursive 是否递归上传子目录，默认false
     * @return OSS文件访问地址列表
     */
    @PostMapping("/uploadLocalDirectory")
    public R<List<String>> uploadLocalDirectory(
            @RequestParam String localDir,
            @RequestParam String bizSign,
            @RequestParam(required = false) String bizCode,
            @RequestParam(defaultValue = "false") boolean recursive) {
        try {
            List<String> ossUrls = ossFileService.uploadLocalDirectory(localDir, bizSign, bizCode, recursive);
            return R.ok(ossUrls, "目录上传完成，成功" + ossUrls.size() + "个文件");
        } catch (Exception e) {
            log.error("上传本地目录到OSS失败: localDir={}", localDir, e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 处理PDF文件（下载、裁剪页面、添加水印、上传）
     *
     * @param ossUrl OSS文件地址
     * @return 处理后的OSS文件地址
     */
    @PostMapping("/processPdfFile")
    public R<String> processPdfFile(
            @RequestParam String ossUrl) {
        try {
            String newOssUrl = ossFileService.processPdfFile(ossUrl);
            return R.ok(newOssUrl, "PDF处理成功");
        } catch (Exception e) {
            log.error("处理PDF文件失败: ossUrl={}", ossUrl, e);
            return R.fail("处理失败: " + e.getMessage());
        }
    }
}
