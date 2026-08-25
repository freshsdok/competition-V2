package com.teaching.course.service;

import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseInfo;

import java.util.List;

/**
 * 课程信息Service接口
 *
 * @author teaching
 * @date 2025-10-22
 */
public interface ICourseInfoService {
    /**
     * 查询课程信息，包括章节和视频详情信息
     *
     * @param courseId 课程信息主键
     * @return 课程信息
     */
    public CourseInfo selectCourseInfoByCourseId(Long courseId);

    /**
     * 根据章节id获取课程信息，包括章节和视频详情信息
     *
     * @param chapterId
     * @return
     */
    public CourseChapterInfo getChapterAndVideoInfoByChapter(Long chapterId);

    /**
     * 查询课程信息列表，包括章节不包含视频信息
     *
     * @param courseInfo 课程信息
     * @return 课程信息集合
     */
    public List<CourseInfo> selectCourseInfoList(CourseInfo courseInfo);

    /**
     * 新增课程信息
     *
     * @param courseInfo 课程信息
     * @return 结果
     */
    public int insertCourseInfo(CourseInfo courseInfo);

    /**
     * 修改课程信息
     *
     * @param courseInfo 课程信息
     * @return 结果
     */
    public int updateCourseInfo(CourseInfo courseInfo);

    /**
     * 修改课程审核状态
     *
     * @param pageInfo
     * @return
     */
    public int updateStatus(PageInfo pageInfo);

    /**
     * 批量删除课程信息
     *
     * @param courseIds 需要删除的课程信息主键集合
     * @return 结果
     */
    public int deleteCourseInfoByCourseIds(Long[] courseIds);

    /**
     * 删除课程信息信息
     *
     * @param courseId 课程信息主键
     * @return 结果
     */
    public int deleteCourseInfoByCourseId(Long courseId);

    /**
     * 课程分类是否被引用
     *
     * @param classifyId
     * @return
     */
    public boolean hasUsedByClassifyId(Long classifyId);
}
