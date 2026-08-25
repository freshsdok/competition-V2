package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.dto.ReviewRoundBindRuleDTO;
import com.teaching.competition.review.service.IReviewRoundService;
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
 * 评审轮次Controller。
 */
@RestController
@RequestMapping("/review/round")
public class ReviewRoundController extends BaseController {
    @Autowired
    private IReviewRoundService reviewRoundService;

    @RequiresPermissions("competition:review:round:add")
    @Log(title = "评审轮次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewRound entity) {
        reviewRoundService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:round:edit")
    @Log(title = "评审轮次", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewRound entity) {
        entity.setId(id);
        return toAjax(reviewRoundService.update(entity));
    }

    @RequiresPermissions("competition:review:round:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewRoundService.selectById(id));
    }

    @RequiresPermissions("competition:review:round:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewRound query) {
        startPage();
        List<ReviewRound> list = reviewRoundService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:round:remove")
    @Log(title = "评审轮次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewRoundService.deleteByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:round:edit")
    @Log(title = "评审轮次绑定评分规则", businessType = BusinessType.UPDATE)
    @PostMapping("/{roundId}/bind-rule")
    public AjaxResult bindRule(@PathVariable("roundId") Long roundId,
                               @RequestBody ReviewRoundBindRuleDTO dto) {
        return toAjax(reviewRoundService.bindRule(roundId, dto == null ? null : dto.getRuleId()));
    }
}
