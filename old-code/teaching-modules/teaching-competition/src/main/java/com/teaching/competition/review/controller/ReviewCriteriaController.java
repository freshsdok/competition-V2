package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.service.IReviewCriteriaService;
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
 * 评分指标Controller。
 */
@RestController
@RequestMapping("/review/criteria")
public class ReviewCriteriaController extends BaseController {
    @Autowired
    private IReviewCriteriaService reviewCriteriaService;

    @RequiresPermissions("competition:review:criteria:add")
    @Log(title = "评分指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewCriteria entity) {
        reviewCriteriaService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:criteria:edit")
    @Log(title = "评分指标", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewCriteria entity) {
        entity.setId(id);
        return toAjax(reviewCriteriaService.update(entity));
    }

    @RequiresPermissions("competition:review:criteria:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewCriteriaService.selectById(id));
    }

    @RequiresPermissions("competition:review:criteria:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewCriteria query) {
        startPage();
        List<ReviewCriteria> list = reviewCriteriaService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:criteria:remove")
    @Log(title = "评分指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewCriteriaService.deleteByIds(new Long[]{id}));
    }
}
