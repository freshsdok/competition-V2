package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.dto.ReviewAssignmentBatchDTO;
import com.teaching.competition.review.service.IReviewAssignmentService;
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
 * 评审任务分配Controller。
 */
@RestController
@RequestMapping("/review/assignment")
public class ReviewAssignmentController extends BaseController {
    @Autowired
    private IReviewAssignmentService reviewAssignmentService;

    @RequiresPermissions("competition:review:assignment:add")
    @Log(title = "评审任务分配", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewAssignment entity) {
        reviewAssignmentService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:assignment:add")
    @Log(title = "评审任务批量分配", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAssign(@RequestBody ReviewAssignmentBatchDTO dto) {
        return success(reviewAssignmentService.batchAssign(dto));
    }

    @RequiresPermissions("competition:review:assignment:edit")
    @Log(title = "评审任务分配", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewAssignment entity) {
        entity.setId(id);
        return toAjax(reviewAssignmentService.update(entity));
    }

    @RequiresPermissions("competition:review:assignment:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewAssignmentService.selectById(id));
    }

    @RequiresPermissions("competition:review:assignment:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewAssignment query) {
        startPage();
        List<ReviewAssignment> list = reviewAssignmentService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:assignment:remove")
    @Log(title = "评审任务分配", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewAssignmentService.deleteByIds(new Long[]{id}));
    }
}
