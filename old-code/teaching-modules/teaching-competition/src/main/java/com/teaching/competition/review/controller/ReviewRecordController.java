package com.teaching.competition.review.controller;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.dto.ReviewRecordDraftDTO;
import com.teaching.competition.review.dto.ReviewRecordSubmitDTO;
import com.teaching.competition.review.service.IReviewRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 专家评审记录Controller。
 */
@RestController
@RequestMapping("/review/record")
public class ReviewRecordController extends BaseController {
    @Autowired
    private IReviewRecordService reviewRecordService;

    @RequiresPermissions("competition:review:record:add")
    @Log(title = "评审记录草稿", businessType = BusinessType.INSERT)
    @PostMapping("/draft")
    public AjaxResult draft(@RequestBody ReviewRecordDraftDTO dto) {
        throw new ServiceException("旧评分记录接口已禁用，请使用专家端 /review/my-review/{assignmentId}/draft 接口");
    }

    @RequiresPermissions("competition:review:record:submit")
    @Log(title = "提交评审记录", businessType = BusinessType.UPDATE)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ReviewRecordSubmitDTO dto) {
        throw new ServiceException("旧评分记录接口已禁用，请使用专家端 /review/my-review/{assignmentId}/submit 接口");
    }

    @RequiresPermissions("competition:review:record:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        throw new ServiceException("旧评分记录查询接口已禁用，请使用 /review/result/records 或专家端 /review/my-review 接口只读查看");
    }

    @RequiresPermissions("competition:review:record:list")
    @GetMapping("/my-list")
    public TableDataInfo myList(ReviewRecord query) {
        throw new ServiceException("旧评分记录列表接口已禁用，请使用 /review/result/records 或专家端 /review/my-review 接口只读查看");
    }
}
