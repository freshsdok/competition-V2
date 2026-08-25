package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.dto.ReviewSecretarySessionObjectStatusDTO;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.service.IReviewSecretaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评审秘书现场控制台Controller。
 */
@RestController
@RequestMapping("/review/secretary")
public class ReviewSecretaryController extends BaseController {
    @Autowired
    private IReviewSecretaryService reviewSecretaryService;

    @RequiresPermissions("competition:review:secretary:query")
    @GetMapping("/session/my-list")
    public AjaxResult listMySessions(ReviewSession query) {
        return success(reviewSecretaryService.listMySessions(query));
    }

    @RequiresPermissions("competition:review:secretary:query")
    @GetMapping("/session/{sessionId}")
    public AjaxResult getSessionDetail(@PathVariable("sessionId") Long sessionId) {
        return success(reviewSecretaryService.getSessionDetail(sessionId));
    }

    @RequiresPermissions("competition:review:secretary:query")
    @GetMapping("/session/{sessionId}/objects")
    public AjaxResult listSessionObjects(@PathVariable("sessionId") Long sessionId) {
        return success(reviewSecretaryService.listSessionObjects(sessionId));
    }

    @RequiresPermissions("competition:review:secretary:edit")
    @Log(title = "秘书设置现场当前对象", businessType = BusinessType.UPDATE)
    @PostMapping("/session/{sessionId}/current-object")
    public AjaxResult setCurrentObject(@PathVariable("sessionId") Long sessionId,
                                       @RequestBody ReviewSessionCurrentObjectDTO dto) {
        return success(reviewSecretaryService.setCurrentObject(sessionId, dto));
    }

    @RequiresPermissions("competition:review:secretary:edit")
    @Log(title = "秘书切换下一位现场对象", businessType = BusinessType.UPDATE)
    @PostMapping("/session/{sessionId}/next-object")
    public AjaxResult nextObject(@PathVariable("sessionId") Long sessionId) {
        return success(reviewSecretaryService.nextObject(sessionId));
    }

    @RequiresPermissions("competition:review:secretary:edit")
    @Log(title = "秘书更新现场对象状态", businessType = BusinessType.UPDATE)
    @PostMapping("/session-object/{sessionObjectId}/status")
    public AjaxResult updateSessionObjectStatus(@PathVariable("sessionObjectId") Long sessionObjectId,
                                                @RequestBody ReviewSecretarySessionObjectStatusDTO dto) {
        return success(reviewSecretaryService.updateSessionObjectStatus(sessionObjectId, dto));
    }
}
