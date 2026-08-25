package com.teaching.course.mapper;

import com.teaching.system.api.domain.course.CourseChapterInfo;
import com.teaching.system.api.domain.course.CourseInfo;

import java.util.List;

/**
 * 课程信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-22
 */
public interface CourseInfoMapper {
    /**
     * 查询课程信息 包含章节信息包含视频信息
     *
     * @param courseId 课程信息主键
     * @return 课程信息
     */
    public CourseInfo selectCourseInfoByCourseId(Long courseId);

    /**
     * 查询含章节信息 包含视频信息
     *
     * @param courseId
     * @return
     */
    public List<CourseChapterInfo> selectCourseChapterInfoAndVideosList(Long courseId);


    /**
     * 查询课程章节信息列表 不包含视频信息
     * @param courseId
     * @return
     */
    public List<CourseChapterInfo> selectCourseChapterInfoList(Long courseId);

    /**
     * 查询课程信息列表 包含章节信息不包含视频信息
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
     * 修改章节信息信息
     *
     * @param courseChapterInfo
     * @return
     */
    public int updateCourseChapterInfo(CourseChapterInfo courseChapterInfo);

    /**
     * 删除课程信息
     *
     * @param courseId 课程信息主键
     * @return 结果
     */
    public int deleteCourseInfoByCourseId(Long courseId);

    /**
     * 批量删除课程信息
     *
     * @param courseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseInfoByCourseIds(Long[] courseIds);

    /**
     * 批量删除章节信息
     *
     * @param courseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseChapterInfoByCourseIds(Long[] courseIds);

    /**
     * 批量新增章节信息
     *
     * @param courseChapterInfoList 章节信息列表
     * @return 结果
     */
    public int batchCourseChapterInfo(List<CourseChapterInfo> courseChapterInfoList);

    /**
     * 批量删除章节信息
     *
     * @param ids
     * @return
     */
    public int batchDeleteCourseChapterInfoByIds(List<Long> ids);

    /**
     * 新增章节信息信息
     *
     * @param courseChapterInfo
     * @return
     */
    public int insertCourseChapterInfo(CourseChapterInfo courseChapterInfo);


    /**
     * 通过课程信息主键删除章节信息信息
     *
     * @param courseId 课程信息ID
     * @return 结果
     */
    public int deleteCourseChapterInfoByCourseId(Long courseId);

    /**
     * 课程分类是否被引用
     *
     * @param menuId
     * @return
     */
    public int hasUsedByClassifyId(Long menuId);

    /**
     * 更新章节学分学时
     *
     * @param chapterId
     */
    public void updateChapterCreditHourByChapterId(Long chapterId);

    /**
     * 更新课程学分学时
     *
     * @param chapterId
     */
    public void updateCourseCreditHourByChapterId(Long chapterId);

    /**
     * 查询章节信息 包含审核中的视频信息
     * @param chapterId
     * @return
     */
    public CourseChapterInfo selectChapterAndVideoInfoByChapterId(Long chapterId);
}
