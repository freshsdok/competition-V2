package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.FileReviewImportSource;
import com.teaching.system.api.factory.RemoteFileReviewImportFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文件任务导入评审模块内部服务。
 */
@FeignClient(contextId = "remoteFileReviewImportService",
        value = ServiceNameConstants.SYSTEM_SERVICE,
        fallbackFactory = RemoteFileReviewImportFallbackFactory.class)
public interface RemoteFileReviewImportService {

    @GetMapping("/fileUploadManager/review-import/by-task/{fileTaskId}")
    R<List<FileReviewImportSource>> listByFileTaskId(@PathVariable("fileTaskId") Long fileTaskId,
                                                     @RequestParam(value = "submittedOnly", required = false) Boolean submittedOnly,
                                                     @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/fileUploadManager/review-import/by-ids")
    R<List<FileReviewImportSource>> listByIds(@RequestBody List<Long> ids,
                                              @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
