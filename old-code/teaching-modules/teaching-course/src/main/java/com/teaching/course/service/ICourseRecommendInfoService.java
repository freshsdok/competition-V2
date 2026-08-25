package com.teaching.course.service;

import com.teaching.course.domain.CourseRecommendInfo;

import java.util.List;

/**
 * 课程推荐信息Service接口
 *
 * @author teaching
 * @date 2025-10-23
 */
public interface ICourseRecommendInfoService {
    /**
     * 查询课程推荐信息
     *
     * @param remdId 课程推荐信息主键
     * @return 课程推荐信息
     */
    public CourseRecommendInfo selectCourseRecommendInfoByRemdId(Long remdId);

    /**
     * 查询课程推荐信息列表
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 课程推荐信息集合
     */
    public List<CourseRecommendInfo> selectCourseRecommendInfoList(CourseRecommendInfo courseRecommendInfo);

    /**
     * 新增课程推荐信息
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 结果
     */
    public int insertCourseRecommendInfo(CourseRecommendInfo courseRecommendInfo);

    /**
     * 修改课程推荐信息
     *
     * @param courseRecommendInfo 课程推荐信息
     * @return 结果
     */
    public int updateCourseRecommendInfo(CourseRecommendInfo courseRecommendInfo);

    /**
     * 批量删除课程推荐信息
     *
     * @param remdIds 需要删除的课程推荐信息主键集合
     * @return 结果
     */
    public int deleteCourseRecommendInfoByRemdIds(Long[] remdIds);

    /**
     * 删除课程推荐信息信息
     *
     * @param remdId 课程推荐信息主键
     * @return 结果
     */
    public int deleteCourseRecommendInfoByRemdId(Long remdId);
}
