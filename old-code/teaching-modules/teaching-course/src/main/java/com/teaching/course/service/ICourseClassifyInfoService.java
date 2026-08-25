package com.teaching.course.service;

import com.teaching.course.domain.CourseClassifyInfo;

import java.util.List;

/**
 * 课程分类Service接口
 *
 * @author teaching
 * @date 2025-10-22
 */
public interface ICourseClassifyInfoService {
    /**
     * 查询课程分类
     *
     * @param classifyId 课程分类主键
     * @return 课程分类
     */
    public CourseClassifyInfo selectCourseClassifyInfoByClassifyId(Long classifyId);

    /**
     * 查询课程分类列表
     *
     * @param courseClassifyInfo 课程分类
     * @return 课程分类集合
     */
    public List<CourseClassifyInfo> selectCourseClassifyInfoList(CourseClassifyInfo courseClassifyInfo);

    /**
     * 新增课程分类
     *
     * @param courseClassifyInfo 课程分类
     * @return 结果
     */
    public int insertCourseClassifyInfo(CourseClassifyInfo courseClassifyInfo);

    /**
     * 修改课程分类
     *
     * @param courseClassifyInfo 课程分类
     * @return 结果
     */
    public int updateCourseClassifyInfo(CourseClassifyInfo courseClassifyInfo);

    /**
     * 批量删除课程分类
     *
     * @param classifyIds 需要删除的课程分类主键集合
     * @return 结果
     */
    public int deleteCourseClassifyInfoByClassifyIds(Long[] classifyIds);

    /**
     * 删除课程分类信息
     *
     * @param classifyId 课程分类主键
     * @return 结果
     */
    public int deleteCourseClassifyInfoByClassifyId(Long classifyId);

    /**
     * 判断是否有子节点
     * @param classifyId
     * @return
     */
    public boolean hasChildByClassifyId(Long classifyId);
}
