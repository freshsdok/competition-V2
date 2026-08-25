package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewPanel;
import com.teaching.competition.review.service.IReviewPanelService;
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
 * 评审专家组Controller。
 */
@RestController
@RequestMapping("/review/panel")
public class ReviewPanelController extends BaseController {
    @Autowired
    private IReviewPanelService reviewPanelService;

    @RequiresPermissions("competition:review:session:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewPanel query) {
        startPage();
        List<ReviewPanel> list = reviewPanelService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:session:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewPanelService.selectById(id));
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "评审专家组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewPanel entity) {
        reviewPanelService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "评审专家组", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewPanel entity) {
        entity.setId(id);
        return toAjax(reviewPanelService.update(entity));
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "评审专家组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewPanelService.deleteByIds(new Long[]{id}));
    }
}
