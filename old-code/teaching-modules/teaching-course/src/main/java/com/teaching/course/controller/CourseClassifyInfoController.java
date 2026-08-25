package com.teaching.course.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.course.domain.CourseClassifyInfo;
import com.teaching.course.service.ICourseClassifyInfoService;
import com.teaching.course.service.ICourseInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程分类Controller
 *
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/classify")
public class CourseClassifyInfoController extends BaseController {
    @Autowired
    private ICourseClassifyInfoService courseClassifyInfoService;
    @Autowired
    private ICourseInfoService courseInfoService;

    /**
     * 查询课程分类列表
     */
    @RequiresPermissions("course:classify:list")
    @GetMapping("/list")
    public TableDataInfo list(CourseClassifyInfo courseClassifyInfo) {
        startPage();
        List<CourseClassifyInfo> list = courseClassifyInfoService.selectCourseClassifyInfoList(courseClassifyInfo);
        return getDataTable(list);
    }

    /**
     * 查询课程分类列表 不分页
     */
    @GetMapping("/getList")
    public AjaxResult getList(CourseClassifyInfo courseClassifyInfo) {
        List<CourseClassifyInfo> list = courseClassifyInfoService.selectCourseClassifyInfoList(courseClassifyInfo);
        return success(list);
    }

    /**
     * 导出课程分类列表
     */
    @RequiresPermissions("course:classify:export")
    @Log(title = "课程分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseClassifyInfo courseClassifyInfo) {
        List<CourseClassifyInfo> list = courseClassifyInfoService.selectCourseClassifyInfoList(courseClassifyInfo);
        ExcelUtil<CourseClassifyInfo> util = new ExcelUtil<CourseClassifyInfo>(CourseClassifyInfo.class);
        util.exportExcel(response, list, "课程分类数据");
    }

    /**
     * 获取课程分类详细信息
     */
    @RequiresPermissions("course:classify:query")
    @GetMapping(value = "/{classifyId}")
    public AjaxResult getInfo(@PathVariable("classifyId") Long classifyId) {
        return success(courseClassifyInfoService.selectCourseClassifyInfoByClassifyId(classifyId));
    }

    /**
     * 新增课程分类
     */
    @RequiresPermissions("course:classify:add")
    @Log(title = "课程分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CourseClassifyInfo courseClassifyInfo) {
        return toAjax(courseClassifyInfoService.insertCourseClassifyInfo(courseClassifyInfo));
    }

    /**
     * 修改课程分类
     */
    @RequiresPermissions("course:classify:edit")
    @Log(title = "课程分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseClassifyInfo courseClassifyInfo) {
        return toAjax(courseClassifyInfoService.updateCourseClassifyInfo(courseClassifyInfo));
    }

    /**
     * 删除课程分类
     */
    @RequiresPermissions("course:classify:remove")
    @Log(title = "课程分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{classifyId}")
    public AjaxResult remove(@PathVariable Long classifyId) {
        if (courseClassifyInfoService.hasChildByClassifyId(classifyId)) {
            return warn("存在子分类,不允许删除");
        }
        if (courseInfoService.hasUsedByClassifyId(classifyId)) {
            return warn("分类已被使用,不允许删除");
        }
        return toAjax(courseClassifyInfoService.deleteCourseClassifyInfoByClassifyId(classifyId));
    }
}
