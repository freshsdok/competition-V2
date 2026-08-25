package com.teaching.course.service;

import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseChapterVideo;

import java.util.List;

/**
 * 章节视频信息Service接口
 *
 * @author teaching
 * @date 2025-10-23
 */
public interface ICourseChapterVideoService {
    /**
     * 查询章节视频信息
     *
     * @param videoId 章节视频信息主键
     * @return 章节视频信息
     */
    public CourseChapterVideo selectCourseChapterVideoByVideoId(Long videoId);

    /**
     * 根据视频ID数组查询章节视频信息列表
     * @param videoIds
     * @return
     */
    public List<CourseChapterVideo> selectCourseChapterVideoByVideoIds(Long[] videoIds);

    /**
     * 查询章节视频信息列表
     *
     * @param courseChapterVideo 章节视频信息
     * @return 章节视频信息集合
     */
    public List<CourseChapterVideo> selectCourseChapterVideoList(CourseChapterVideo courseChapterVideo);

    /**
     * 根据章节ID查询视频列表
     * @param chapterId
     * @return
     */
    public List<CourseChapterVideo> selectCourseChapterVideoListByChapterId(Long chapterId);

    /**
     * 新增章节视频信息
     *
     * @param courseChapterVideo 章节视频信息
     * @return 结果
     */
    public int insertCourseChapterVideo(CourseChapterVideo courseChapterVideo);

    /**
     * 修改章节视频信息
     *
     * @param courseChapterVideo 章节视频信息
     * @return 结果
     */
    public int updateCourseChapterVideo(CourseChapterVideo courseChapterVideo);

    /**
     * 修改章节视频信息
     * @param courseChapterInfo
     * @return
     */
    public int updateCourseChapterVideo(CourseChapterInfo courseChapterInfo);

    /**
     * 更新章节视频信息 审核状态、发布状态 根据视频id
     * @param pageInfo
     * @return
     */
    public int updateCourseChapterVideoStatus(PageInfo pageInfo);

    /**
     * 更新章节视频信息 审核状态、发布状态 根据章节id
     * @param auditResult
     * @return
     */
    public int updateCourseChapterVideoStatusByChapter(ChapterAuditResult auditResult);

    /**
     * 批量删除章节视频信息
     *
     * @param videoIds 需要删除的章节视频信息主键集合
     * @return 结果
     */
    public int deleteCourseChapterVideoByVideoIds(Long[] videoIds,Long chapterId);

    /**
     * 删除章节视频信息信息
     *
     * @param videoId 章节视频信息主键
     * @return 结果
     */
    public int deleteCourseChapterVideoByVideoId(Long videoId,Long chapterId);
}
