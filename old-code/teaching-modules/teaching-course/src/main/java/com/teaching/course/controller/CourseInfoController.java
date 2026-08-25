package com.teaching.course.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.course.CourseInfo;
import com.teaching.course.service.ICourseInfoService;
import com.teaching.system.api.domain.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程信息Controller
 *
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/courseInfo")
public class CourseInfoController extends BaseController {
    @Autowired
    private ICourseInfoService courseInfoService;

    /**
     * 查询课程信息列表，包括章节不包含视频信息
     */
//    @RequiresPermissions("course:courseInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CourseInfo courseInfo) {
        startPage();
        List<CourseInfo> list = courseInfoService.selectCourseInfoList(courseInfo);
        return getDataTable(list);
    }

    /**
     * 导出课程信息列表
     */
    @RequiresPermissions("course:courseInfo:export")
    @Log(title = "课程信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseInfo courseInfo) {
        List<CourseInfo> list = courseInfoService.selectCourseInfoList(courseInfo);
        ExcelUtil<CourseInfo> util = new ExcelUtil<CourseInfo>(CourseInfo.class);
        util.exportExcel(response, list, "课程信息数据");
    }

    /**
     * 获取课程信息详细信息，包括章节和视频详情
     */
    @RequiresPermissions("course:courseInfo:query")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId) {
        return success(courseInfoService.selectCourseInfoByCourseId(courseId));
    }

    /**
     * 获取章节信息，包含审核中的视频信息 根据章节id
     */
    @InnerAuth
    @GetMapping(value = "/getChapterInfo/{chapterId}")
    public AjaxResult getChapterAndVideoInfoByChapter(@PathVariable("chapterId") Long chapterId) {
        return success(courseInfoService.getChapterAndVideoInfoByChapter(chapterId));
    }

    /**
     * 新增课程信息
     */
    @RequiresPermissions("course:courseInfo:add")
    @Log(title = "课程信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CourseInfo courseInfo) {
        return toAjax(courseInfoService.insertCourseInfo(courseInfo));
    }

    /**
     * 修改课程信息
     */
    @RequiresPermissions("course:courseInfo:edit")
    @Log(title = "课程信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseInfo courseInfo) {
        return toAjax(courseInfoService.updateCourseInfo(courseInfo));
    }

    /**
     * 修改课程审核状态
     */
    @Log(title = "课程信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody PageInfo pageInfo) {
        return success(courseInfoService.updateStatus(pageInfo));
    }

    /**
     * 删除课程信息 单个删
     */
    @RequiresPermissions("course:courseInfo:remove")
    @Log(title = "课程信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseId}")
    public AjaxResult remove(@PathVariable Long courseId) {
        return toAjax(courseInfoService.deleteCourseInfoByCourseId(courseId));
    }
}
