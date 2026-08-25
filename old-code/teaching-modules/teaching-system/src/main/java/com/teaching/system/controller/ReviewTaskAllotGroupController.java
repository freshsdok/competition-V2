package com.teaching.system.controller;

import cn.hutool.core.convert.Convert;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.ReviewTaskAllotGroup;
import com.teaching.system.service.IReviewTaskAllotGroupRelationService;
import com.teaching.system.service.IReviewTaskAllotGroupService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评审任务分配组信息接口
 *
 * @author teaching
 * @date 2026-04-09
 */
@RestController
@RequestMapping("/reviewTaskAllotGroup")
public class ReviewTaskAllotGroupController extends BaseController {
    @Autowired
    private IReviewTaskAllotGroupService reviewTaskAllotGroupService;

    @Autowired
    private IReviewTaskAllotGroupRelationService reviewTaskAllotGroupRelationService;

    /**
     * 查询评审任务分配组信息下拉
     */
//    @RequiresPermissions("system:allotGroup:query")
    @GetMapping("/list")
    public AjaxResult list(ReviewTaskAllotGroup reviewTaskAllotGroup) {
        List<ReviewTaskAllotGroup> list = reviewTaskAllotGroupService.selectReviewTaskAllotGroupList(reviewTaskAllotGroup);
        return success(list);
    }

    /**
     * 导出评审任务分配组信息列表
     */
//    @RequiresPermissions("system:allotGroup:export")
//    @Log(title = "评审任务分配组信息", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, ReviewTaskAllotGroup reviewTaskAllotGroup) {
//        List<ReviewTaskAllotGroup> list = reviewTaskAllotGroupService.selectReviewTaskAllotGroupList(reviewTaskAllotGroup);
//        ExcelUtil<ReviewTaskAllotGroup> util = new ExcelUtil<ReviewTaskAllotGroup>(ReviewTaskAllotGroup.class);
//        util.exportExcel(response, list, "评审任务分配组信息数据");
//    }

    /**
     * 获取评审任务分配组信息详细信息
     */
//    @RequiresPermissions("system:allotGroup:query")
    @GetMapping(value = "/getReviewTaskAllotGroup/{reviewGroupId}")
    public AjaxResult getInfo(@PathVariable("reviewGroupId") Long reviewGroupId) {
        return success(reviewTaskAllotGroupService.selectReviewTaskAllotGroupByReviewGroupId(reviewGroupId));
    }

    /**
     * 新增评审任务分配组信息
     */
//    @RequiresPermissions("system:allotGroup:add")
    @Log(title = "新增评审任务分配组信息", businessType = BusinessType.INSERT)
    @PostMapping("/addReviewTaskAllotGroup")
    public AjaxResult add(@RequestBody ReviewTaskAllotGroup reviewTaskAllotGroup) {
        return toAjax(reviewTaskAllotGroupService.insertReviewTaskAllotGroup(reviewTaskAllotGroup));
    }

    /**
     * 修改评审任务分配组信息
     */
//    @RequiresPermissions("system:allotGroup:edit")
    @Log(title = "修改评审任务分配组信息", businessType = BusinessType.UPDATE)
    @PostMapping("/editReviewTaskAllotGroup")
    public AjaxResult edit(@RequestBody ReviewTaskAllotGroup reviewTaskAllotGroup) {
        return toAjax(reviewTaskAllotGroupService.updateReviewTaskAllotGroup(reviewTaskAllotGroup));
    }

    /**
     * 删除评审任务分配组信息
     */
//    @RequiresPermissions("system:allotGroup:remove")
//    @Log(title = "评审任务分配组信息", businessType = BusinessType.DELETE)
//    @GetMapping("/remove/{reviewGroupId}")
//    public AjaxResult remove(@PathVariable Long reviewGroupId) {
//        return toAjax(reviewTaskAllotGroupService.deleteReviewTaskAllotGroupByReviewGroupId(reviewGroupId));
//    }

    /**
     * 删除评审任务分配组关联关系
     */
//    @RequiresPermissions("system:allotGroup:remove")
    @Log(title = "删除评审任务分配组关联关系", businessType = BusinessType.DELETE)
    @PostMapping("/remove/groupRelation")
    public AjaxResult remove(@RequestBody Map<String,Object> params) {
        if(Objects.isNull(params.get("reviewIdList")) && Objects.isNull(params.get("reviewGroupIdList"))){
            return success();
        }
        List<Long> reviewIdList = Convert.toList(Long.class, params.get("reviewIdList"));
        List<Long> reviewGroupIdList = Convert.toList(Long.class, params.get("reviewGroupIdList"));
        return toAjax(reviewTaskAllotGroupRelationService
                .deleteReviewTaskAllotGroupRelationByReviewIdAndReviewGroupId(reviewIdList,reviewGroupIdList));
    }
}
