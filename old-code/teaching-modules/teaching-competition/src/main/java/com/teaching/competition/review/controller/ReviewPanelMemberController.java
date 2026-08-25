package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewPanelMember;
import com.teaching.competition.review.service.IReviewPanelMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评审专家组成员Controller。
 */
@RestController
@RequestMapping("/review/panel-member")
public class ReviewPanelMemberController extends BaseController {
    @Autowired
    private IReviewPanelMemberService reviewPanelMemberService;

    @RequiresPermissions("competition:review:session:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewPanelMember query) {
        startPage();
        List<ReviewPanelMember> list = reviewPanelMemberService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "评审专家组成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewPanelMember entity) {
        reviewPanelMemberService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "评审专家组成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewPanelMemberService.deleteByIds(new Long[]{id}));
    }
}
