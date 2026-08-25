package com.teaching.course.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.course.domain.CourseRecommendInfo;
import com.teaching.course.service.ICourseRecommendInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程推荐信息Controller
 *
 * @author teaching
 * @date 2025-10-23
 */
@RestController
@RequestMapping("/recommendInfo")
public class CourseRecommendInfoController extends BaseController {
    @Autowired
    private ICourseRecommendInfoService courseRecommendInfoService;

    /**
     * 查询课程推荐信息列表
     */
    @RequiresPermissions("course:recommendInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CourseRecommendInfo courseRecommendInfo) {
        startPage();
        List<CourseRecommendInfo> list = courseRecommendInfoService.selectCourseRecommendInfoList(courseRecommendInfo);
        return getDataTable(list);
    }

    /**
     * 导出课程推荐信息列表
     */
    @RequiresPermissions("course:recommendInfo:export")
    @Log(title = "课程推荐信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseRecommendInfo courseRecommendInfo) {
        List<CourseRecommendInfo> list = courseRecommendInfoService.selectCourseRecommendInfoList(courseRecommendInfo);
        ExcelUtil<CourseRecommendInfo> util = new ExcelUtil<CourseRecommendInfo>(CourseRecommendInfo.class);
        util.exportExcel(response, list, "课程推荐信息数据");
    }

    /**
     * 获取课程推荐信息详细信息
     */
    @RequiresPermissions("course:recommendInfo:query")
    @GetMapping(value = "/{remdId}")
    public AjaxResult getInfo(@PathVariable("remdId") Long remdId) {
        return success(courseRecommendInfoService.selectCourseRecommendInfoByRemdId(remdId));
    }

    /**
     * 新增课程推荐信息
     */
    @RequiresPermissions("course:recommendInfo:add")
    @Log(title = "课程推荐信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CourseRecommendInfo courseRecommendInfo) {
        return toAjax(courseRecommendInfoService.insertCourseRecommendInfo(courseRecommendInfo));
    }

    /**
     * 修改课程推荐信息
     */
    @RequiresPermissions("course:recommendInfo:edit")
    @Log(title = "课程推荐信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseRecommendInfo courseRecommendInfo) {
        return toAjax(courseRecommendInfoService.updateCourseRecommendInfo(courseRecommendInfo));
    }

    /**
     * 删除课程推荐信息 单个删除
     */
    @RequiresPermissions("course:recommendInfo:remove")
    @Log(title = "课程推荐信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{remdId}")
    public AjaxResult remove(@PathVariable Long remdId) {
        return toAjax(courseRecommendInfoService.deleteCourseRecommendInfoByRemdId(remdId));
    }
}
