package com.teaching.course.mapper;

import com.teaching.system.api.domain.course.CourseChapterVideo;

import java.util.List;

/**
 * 章节视频信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-23
 */
public interface CourseChapterVideoMapper {
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
     * 删除章节视频信息
     *
     * @param videoId 章节视频信息主键
     * @return 结果
     */
    public int deleteCourseChapterVideoByVideoId(Long videoId);

    /**
     * 逻辑删除章节视频信息
     * @param videoId
     * @return
     */
    public int updateFlagChapterVideoByVideoId(Long videoId);

    /**
     * 通过章节IDs删除视频信息
     * @param chapterIds
     * @return
     */
    public int deleteCourseChapterVideoByVideoByChapterIds(List<Long> chapterIds);

    /**
     * 批量删除章节视频信息
     *
     * @param videoIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseChapterVideoByVideoIds(Long[] videoIds);

    /**
     * 批量逻辑删除章节视频信息
     * @param videoIds
     * @return
     */
    public int updateFlagChapterVideoByVideoIds(Long[] videoIds);

    /**
     * 根据视频ID查询课程发布状态
     * @param videoId
     * @return
     */
    public String selectCoursePublishStatusByVideoId(Long videoId);

    /**
     * 根据章节ID查询课程发布状态
     * @param videoId
     * @return
     */
    public String selectCoursePublishStatusByChapterId(Long videoId);
}
