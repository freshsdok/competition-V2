package com.teaching.course.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.course.service.ICourseChapterVideoService;
import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节视频信息Controller
 *
 * @author teaching
 * @date 2025-10-23
 */
@RestController
@RequestMapping("/chapterVideo")
public class CourseChapterVideoController extends BaseController {
    @Autowired
    private ICourseChapterVideoService courseChapterVideoService;

    /**
     * 查询章节视频信息列表
     */
    @RequiresPermissions("course:chapterVideo:list")
    @GetMapping("/list")
    public TableDataInfo list(CourseChapterVideo courseChapterVideo) {
        startPage();
        List<CourseChapterVideo> list = courseChapterVideoService.selectCourseChapterVideoList(courseChapterVideo);
        return getDataTable(list);
    }

    /**
     * 导出章节视频信息列表
     */
    @RequiresPermissions("course:chapterVideo:export")
    @Log(title = "章节视频信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseChapterVideo courseChapterVideo) {
        List<CourseChapterVideo> list = courseChapterVideoService.selectCourseChapterVideoList(courseChapterVideo);
        ExcelUtil<CourseChapterVideo> util = new ExcelUtil<CourseChapterVideo>(CourseChapterVideo.class);
        util.exportExcel(response, list, "章节视频信息数据");
    }

    /**
     * 获取章节视频信息详细信息
     */
    @RequiresPermissions("course:chapterVideo:query")
    @GetMapping(value = "/{videoId}")
    public AjaxResult getInfo(@PathVariable("videoId") Long videoId) {
        return success(courseChapterVideoService.selectCourseChapterVideoByVideoId(videoId));
    }

    /**
     * 根据视频ID查询章节视频信息详细信息
     *
     * @param videoIds
     * @return
     */
    @InnerAuth
    @GetMapping(value = "/videos/{videoIds}")
    public AjaxResult getInfo(@PathVariable("videoIds") Long[] videoIds) {
        return success(courseChapterVideoService.selectCourseChapterVideoByVideoIds(videoIds));
    }

    /**
     * 根据章节ID查询视频列表
     */
    @RequiresPermissions("course:chapterVideo:query")
    @GetMapping(value = "/getInfoByChapterId/{chapterId}")
    public AjaxResult getInfoByChapterId(@PathVariable Long chapterId) {
        return success(courseChapterVideoService.selectCourseChapterVideoListByChapterId(chapterId));
    }

    /**
     * 新增章节视频信息
     */
    @RequiresPermissions("course:chapterVideo:add")
    @Log(title = "章节视频信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CourseChapterVideo courseChapterVideo) {
        return toAjax(courseChapterVideoService.insertCourseChapterVideo(courseChapterVideo));
    }

    /**
     * 修改章节视频信息
     */
    @RequiresPermissions("course:chapterVideo:edit")
    @Log(title = "章节视频信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CourseChapterVideo courseChapterVideo) {
        return toAjax(courseChapterVideoService.updateCourseChapterVideo(courseChapterVideo));
    }

    /**
     * 修改章节视频信息
     */
    @RequiresPermissions("course:chapterVideo:edit")
    @Log(title = "章节视频信息", businessType = BusinessType.UPDATE)
    @PostMapping("/editVideo")
    public AjaxResult editVideo(@RequestBody CourseChapterInfo courseChapterInfo) {
        return toAjax(courseChapterVideoService.updateCourseChapterVideo(courseChapterInfo));
    }

    /**
     * 修改章节视频信息审核状态和发布状态  按章节审核 使用章节id处理
     *
     * @param auditResult
     * @return
     */
    @Log(title = "章节视频信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult edit(@RequestBody ChapterAuditResult auditResult) {
        return success(courseChapterVideoService.updateCourseChapterVideoStatusByChapter(auditResult));
    }

    /**
     * 删除章节视频信息
     *
     * @param videoIds  同一个章节的视频ids
     * @param chapterId 章节id
     * @return 结果
     */
    @RequiresPermissions("course:chapterVideo:remove")
    @Log(title = "章节视频信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{videoIds}/{chapterId}")
    public AjaxResult remove(@PathVariable Long[] videoIds, @PathVariable Long chapterId) {
        return toAjax(courseChapterVideoService.deleteCourseChapterVideoByVideoIds(videoIds, chapterId));
    }

}
