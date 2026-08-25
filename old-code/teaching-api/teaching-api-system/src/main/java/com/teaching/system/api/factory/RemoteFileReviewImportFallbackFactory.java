package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteFileReviewImportService;
import com.teaching.system.api.domain.FileReviewImportSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件任务导入评审模块内部服务降级处理。
 */
@Component
public class RemoteFileReviewImportFallbackFactory implements FallbackFactory<RemoteFileReviewImportService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteFileReviewImportFallbackFactory.class);

    @Override
    public RemoteFileReviewImportService create(Throwable cause) {
        log.error("文件任务导入评审模块内部服务调用失败:{}", cause.getMessage());
        return new RemoteFileReviewImportService() {
            @Override
            public R<List<FileReviewImportSource>> listByFileTaskId(Long fileTaskId, Boolean submittedOnly, String source) {
                return R.fail("按文件任务查询上传快照失败:" + cause.getMessage());
            }

            @Override
            public R<List<FileReviewImportSource>> listByIds(List<Long> ids, String source) {
                return R.fail("按上传记录查询上传快照失败:" + cause.getMessage());
            }
        };
    }
}
