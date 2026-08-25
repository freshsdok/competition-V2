package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import com.teaching.system.api.domain.course.CourseInfo;
import com.teaching.system.api.factory.RemoteCourseServiceFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程服务接口
 *
 * @author Administrator
 */
@FeignClient(contextId = "courseService", value = ServiceNameConstants.COURSE_SERVICE, fallbackFactory = RemoteCourseServiceFactory.class)
public interface RemoteCourseService {

    /**
     * 通过id查询详情
     */
    @GetMapping("/courseInfo/{courseId}")
    public R<CourseInfo> getCourseDetailInfoById(@PathVariable("courseId") Long courseId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     * 修改课程审核状态
     */
    @PostMapping("/courseInfo/updateStatus")
    public R<Integer> updateCourseInfoStatus(@RequestBody PageInfo pageinfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过id查询章节视频详情
     */
    @GetMapping("/courseInfo/getChapterInfo/{chapterId}")
    public R<CourseChapterInfo> getChapterVideoDetailInfoById(@PathVariable("chapterId") Long chapterId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * /** 修改章节视频审核状态
     */
    @PostMapping("/chapterVideo/updateStatus")
    public R<Integer> updateChapterVideoStatus(@RequestBody ChapterAuditResult auditResult, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/chapterVideo/videos/{videoIds}")
    public R<List<CourseChapterVideo>> getChapterVideoDetailInfoByIds(@PathVariable("videoIds") Long[] videoIds, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
