package com.teaching.system.controller;

import cn.hutool.core.convert.Convert;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.ReviewSpecialistGroupInfo;
import com.teaching.system.domain.ReviewTaskSpecialistRelation;
import com.teaching.system.domain.SpecialistSysUser;
import com.teaching.system.service.IReviewSpecialistGroupInfoService;
import com.teaching.system.service.IReviewTaskSpecialistRelationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 专家组接口
 *
 * @author teaching
 * @date 2026-04-09
 */
@RestController
@RequestMapping("/reviewSpecialistGroupInfo")
public class ReviewSpecialistGroupInfoController extends BaseController {
    @Autowired
    private IReviewSpecialistGroupInfoService reviewSpecialistGroupInfoService;

    @Autowired
    private IReviewTaskSpecialistRelationService reviewTaskSpecialistRelationService;

    /**
     * 查询专家组列表
     */
//    @RequiresPermissions("system:group:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        startPage();
        List<ReviewSpecialistGroupInfo> list = reviewSpecialistGroupInfoService.selectReviewSpecialistGroupInfoList(reviewSpecialistGroupInfo);
        return getDataTable(list);
    }

    /**
     * 导出专家组列表
     */
//    @RequiresPermissions("system:group:export")
//    @Log(title = "专家组", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
//        List<ReviewSpecialistGroupInfo> list = reviewSpecialistGroupInfoService.selectReviewSpecialistGroupInfoList(reviewSpecialistGroupInfo);
//        ExcelUtil<ReviewSpecialistGroupInfo> util = new ExcelUtil<ReviewSpecialistGroupInfo>(ReviewSpecialistGroupInfo.class);
//        util.exportExcel(response, list, "专家组数据");
//    }

    /**
     * 获取专家组详细信息
     */
//    @RequiresPermissions("system:group:query")
//    @GetMapping(value = "/{groupId}")
//    public AjaxResult getInfo(@PathVariable("groupId") Long groupId) {
//        return success(reviewSpecialistGroupInfoService.selectReviewSpecialistGroupInfoByGroupId(groupId));
//    }

    /**
     * 新增专家组
     */
//    @RequiresPermissions("system:group:add")
    @Log(title = "新增专家组", businessType = BusinessType.INSERT)
    @PostMapping("/addReviewSpecialistGroupInfo")
    public AjaxResult add(@RequestBody ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        return toAjax(reviewSpecialistGroupInfoService.insertReviewSpecialistGroupInfo(reviewSpecialistGroupInfo));
    }

    /**
     * 修改专家组
     */
//    @RequiresPermissions("system:group:edit")
    @Log(title = "修改专家组", businessType = BusinessType.UPDATE)
    @PostMapping("/editReviewSpecialistGroupInfo")
    public AjaxResult edit(@RequestBody ReviewSpecialistGroupInfo reviewSpecialistGroupInfo) {
        return toAjax(reviewSpecialistGroupInfoService.updateReviewSpecialistGroupInfo(reviewSpecialistGroupInfo));
    }

    /**
     * 删除专家组
     */
//    @RequiresPermissions("system:group:remove")
    @Log(title = "删除专家组", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{groupId}")
    public AjaxResult remove(@PathVariable Long groupId) {
        return toAjax(reviewSpecialistGroupInfoService.deleteReviewSpecialistGroupInfoByGroupId(groupId));
    }

    /**
     * 批量从分配任务删除专家
     */
//    @RequiresPermissions("system:group:remove")
    @Log(title = "批量从分配任务删除专家", businessType = BusinessType.DELETE)
    @PostMapping("/remove/specialist")
    public AjaxResult batchRemove(@RequestBody Map<String, Object> params) {
        if(Objects.isNull(params.get("reviewIdList")) && Objects.isNull(params.get("userIdList"))){
            return success();
        }
        List<Long> reviewIdList = Convert.toList(Long.class, params.get("reviewIdList"));
        List<Long> userIdList = Convert.toList(Long.class, params.get("userIdList"));
        return toAjax(reviewTaskSpecialistRelationService.deleteReviewTaskSpecialistRelationByUserId(reviewIdList,userIdList));
    }

    /**
     * 获取专家信息
     */
//    @RequiresPermissions("system:group:list")
    @PostMapping("/getSpecialistInfo")
    public AjaxResult getSpecialistInfo(@RequestBody SpecialistSysUser specialistSysUser) {
        return success(reviewSpecialistGroupInfoService.getSpecialistInfo(specialistSysUser));
    }
}
