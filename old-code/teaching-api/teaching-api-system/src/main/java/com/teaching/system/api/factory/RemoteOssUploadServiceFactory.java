package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteOssUploadService;
import com.teaching.system.api.domain.PackageFileReq;
import com.teaching.system.api.domain.SysFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RemoteOssUploadServiceFactory implements FallbackFactory<RemoteOssUploadService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteOssUploadServiceFactory.class);

    @Override
    public RemoteOssUploadService create(Throwable throwable) {
        log.error("文件服务调用失败:{}", throwable.getMessage());
        return new RemoteOssUploadService()
        {
            @Override
            public R<String> ossUpload(MultipartFile file, String bizSign, String bizCode) {
                log.info("oss文件上传失败："+throwable.getMessage());
                return R.fail("oss文件上传失败："+throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> packageFile(List<PackageFileReq> fileList) {
                log.info("压缩文件失败：" + throwable.getMessage());
                return R.fail("压缩文件失败:" + throwable.getMessage());
            }

            @Override
            public R<String> getPresignedUrl(String fileKey) {
                log.info("获取OSS预签名地址失败：" + throwable.getMessage());
                return R.fail("获取OSS预签名地址失败：" + throwable.getMessage());
            }

            @Override
            public R<Boolean> deleteFile(String objectKey) {
                log.info("删除OSS文件失败：" + throwable.getMessage());
                return R.fail("删除OSS文件失败：" + throwable.getMessage());
            }

            @Override
            public R<Boolean> downloadToLocal(String ossUrl, String localPath) {
                log.info("下载文件失败：" + throwable.getMessage());
                return R.fail("下载文件失败：" + throwable.getMessage());
            }

            @Override
            public R<String> uploadLocalFile(String localPath, String bizSign, String bizCode) {
                log.info("上传本地文件失败：" + throwable.getMessage());
                return R.fail("上传本地文件失败：" + throwable.getMessage());
            }

            @Override
            public R<String> processPdfFile(String ossUrl) {
                log.info("处理PDF文件失败：" + throwable.getMessage());
                return R.fail("处理PDF文件失败：" + throwable.getMessage());
            }
        };
    }
}
