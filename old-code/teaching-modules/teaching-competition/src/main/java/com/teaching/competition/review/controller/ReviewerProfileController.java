package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewerProfile;
import com.teaching.competition.review.service.IReviewerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评审人Controller。
 */
@RestController
@RequestMapping("/review/reviewer")
public class ReviewerProfileController extends BaseController {
    @Autowired
    private IReviewerProfileService reviewerProfileService;

    @RequiresPermissions("competition:review:reviewer:add")
    @Log(title = "评审人", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewerProfile entity) {
        reviewerProfileService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:reviewer:edit")
    @Log(title = "评审人", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewerProfile entity) {
        entity.setId(id);
        return toAjax(reviewerProfileService.update(entity));
    }

    @RequiresPermissions("competition:review:reviewer:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewerProfileService.selectById(id));
    }

    @RequiresPermissions("competition:review:reviewer:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewerProfile query) {
        startPage();
        List<ReviewerProfile> list = reviewerProfileService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:reviewer:remove")
    @Log(title = "评审人", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewerProfileService.deleteByIds(new Long[]{id}));
    }
}
