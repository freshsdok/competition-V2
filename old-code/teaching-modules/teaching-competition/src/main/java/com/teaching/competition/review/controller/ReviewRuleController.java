package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.service.IReviewRuleService;
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
 * 评审规则Controller。
 */
@RestController
@RequestMapping("/review/rule")
public class ReviewRuleController extends BaseController {
    @Autowired
    private IReviewRuleService reviewRuleService;

    @RequiresPermissions("competition:review:rule:add")
    @Log(title = "评审规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewRule entity) {
        reviewRuleService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:rule:edit")
    @Log(title = "评审规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewRule entity) {
        entity.setId(id);
        return toAjax(reviewRuleService.update(entity));
    }

    @RequiresPermissions("competition:review:rule:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewRuleService.selectById(id));
    }

    @RequiresPermissions("competition:review:rule:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewRule query) {
        startPage();
        List<ReviewRule> list = reviewRuleService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:rule:remove")
    @Log(title = "评审规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewRuleService.deleteByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:rule:query")
    @PostMapping("/{id}/validate")
    public AjaxResult validate(@PathVariable("id") Long id) {
        return success(reviewRuleService.validateRule(id));
    }

    @RequiresPermissions("competition:review:rule:edit")
    @Log(title = "启用评分规则", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/enable")
    public AjaxResult enable(@PathVariable("id") Long id) {
        return toAjax(reviewRuleService.enable(id));
    }

    @RequiresPermissions("competition:review:rule:edit")
    @Log(title = "停用评分规则", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable("id") Long id) {
        return toAjax(reviewRuleService.disable(id));
    }

    @RequiresPermissions("competition:review:rule:add")
    @Log(title = "复制评分规则", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/copy")
    public AjaxResult copy(@PathVariable("id") Long id) {
        return success(reviewRuleService.copy(id));
    }
}
