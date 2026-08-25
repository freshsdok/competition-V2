package com.teaching.system.api.factory;

import com.teaching.system.api.domain.PackageFileReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.domain.SysFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件服务降级处理
 * 
 * @author teaching
 */
@Component
public class RemoteFileFallbackFactory implements FallbackFactory<RemoteFileService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteFileFallbackFactory.class);

    @Override
    public RemoteFileService create(Throwable throwable)
    {
        log.error("文件服务调用失败:{}", throwable.getMessage());
        return new RemoteFileService()
        {
            @Override
            public R<SysFile> upload(MultipartFile file)
            {
                return R.fail("上传文件失败:" + throwable.getMessage());
            }

            @Override
            public R<String> ossUpload(MultipartFile file, String bizSign, String bizCode) {
                return R.fail("上传文件失败：" + throwable.getMessage());
            }

            @Override
            public R<Boolean> delete(String fileUrl)
            {
                return R.fail("删除文件失败:" + throwable.getMessage());
            }

            @Override
            public R<String> getFilePath() {
                return R.fail("获取文件路径失败:" + throwable.getMessage());
            }

            @Override
            public void resourceDownload(@RequestParam String resource){

            }

            @Override
            public R<Map<String,Object>> zipFile(List<String> urls) throws IOException {
                log.info("导出文件失败："+throwable.getMessage());
                return R.fail("导出文件失败："+throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> packageFile(List<PackageFileReq> fileList) {
                log.info("压缩文件失败：" + throwable.getMessage());
                return R.fail("压缩文件失败:" + throwable.getMessage());
            }
        };
    }
}
