package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.dto.ReviewMyReviewScoreDTO;
import com.teaching.competition.review.service.IReviewMyReviewService;
import com.teaching.competition.review.vo.ReviewMyReviewTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 专家PC端我的评审任务Controller。
 */
@RestController
@RequestMapping("/review/my-review")
public class ReviewMyReviewController extends BaseController {
    @Autowired
    private IReviewMyReviewService reviewMyReviewService;

    @RequiresPermissions("competition:review:my-review:list")
    @GetMapping("/activity-rounds")
    public AjaxResult activityRounds() {
        return success(reviewMyReviewService.myActivityRounds());
    }

    @RequiresPermissions("competition:review:my-review:list")
    @GetMapping("/session/{sessionId}/current-object")
    public AjaxResult currentObject(@PathVariable("sessionId") Long sessionId) {
        return success(reviewMyReviewService.currentObject(sessionId));
    }

    @RequiresPermissions("competition:review:my-review:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "activityId", required = false) Long activityId,
                              @RequestParam(value = "roundId", required = false) Long roundId,
                              @RequestParam(value = "objectName", required = false) String objectName,
                              @RequestParam(value = "objectCode", required = false) String objectCode,
                              @RequestParam(value = "assignmentStatus", required = false) String assignmentStatus,
                              @RequestParam(value = "keywords", required = false) String keywords,
                              @RequestParam(value = "sessionId", required = false) Long sessionId,
                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        List<ReviewMyReviewTaskVO> list = reviewMyReviewService.myList(activityId, roundId, objectName, objectCode,
                assignmentStatus, keywords, sessionId);
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);
        long requestedFromIndex = (long) (safePageNum - 1) * safePageSize;
        int fromIndex = (int) Math.min(requestedFromIndex, list.size());
        int toIndex = Math.min(fromIndex + safePageSize, list.size());
        List<ReviewMyReviewTaskVO> pageRows = new ArrayList<>(list.subList(fromIndex, toIndex));
        TableDataInfo tableData = getDataTable(pageRows);
        tableData.setTotal(list.size());
        return tableData;
    }

    @RequiresPermissions("competition:review:my-review:query")
    @GetMapping("/{assignmentId}")
    public AjaxResult detail(@PathVariable("assignmentId") Long assignmentId) {
        return success(reviewMyReviewService.detail(assignmentId));
    }

    @RequiresPermissions("competition:review:my-review:query")
    @GetMapping("/{assignmentId}/criteria")
    public AjaxResult criteria(@PathVariable("assignmentId") Long assignmentId) {
        return success(reviewMyReviewService.criteria(assignmentId));
    }

    @RequiresPermissions("competition:review:my-review:edit")
    @Log(title = "专家保存评分草稿", businessType = BusinessType.UPDATE)
    @PostMapping("/{assignmentId}/draft")
    public AjaxResult draft(@PathVariable("assignmentId") Long assignmentId,
                            @RequestBody ReviewMyReviewScoreDTO dto) {
        return success(reviewMyReviewService.saveDraft(assignmentId, dto));
    }

    @RequiresPermissions("competition:review:my-review:submit")
    @Log(title = "专家提交评分", businessType = BusinessType.UPDATE)
    @PostMapping("/{assignmentId}/submit")
    public AjaxResult submit(@PathVariable("assignmentId") Long assignmentId,
                             @RequestBody ReviewMyReviewScoreDTO dto) {
        return success(reviewMyReviewService.submit(assignmentId, dto));
    }
}
