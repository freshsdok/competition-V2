package com.teaching.course.mapper;

import com.teaching.course.domain.CourseRecommendInfo;
import com.teaching.course.domain.CourseRecommendRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程推荐信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-23
 */
public interface CourseRecommendInfoMapper {
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
     * 删除课程推荐信息
     *
     * @param remdId 课程推荐信息主键
     * @return 结果
     */
    public int deleteCourseRecommendInfoByRemdId(Long remdId);

    /**
     * 批量删除课程推荐信息
     *
     * @param remdIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseRecommendInfoByRemdIds(Long[] remdIds);

    /**
     * 批量删除课程推荐关联关系
     *
     * @param remdIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseRecommendRelaByRemdIds(Long[] remdIds);

    /**
     * 批量删除课程推荐关联关系
     * @param ids 关联关系id
     * @return
     */
    public int batchDeleteCourseRecommendRelaByIds(List<Long> ids);

    /**
     * 批量新增课程推荐关联关系
     *
     * @param courseRecommendRelaList 课程推荐关联关系列表
     * @return 结果
     */
    public int batchCourseRecommendRela(List<CourseRecommendRela> courseRecommendRelaList);


    /**
     * 通过课程推荐信息主键删除课程推荐关联关系信息
     *
     * @param remdId 课程推荐信息ID
     * @return 结果
     */
    public int deleteCourseRecommendRelaByRemdId(Long remdId);

    /**
     * 批量更新课程推荐关系（分批处理）
     *
     * @param list 待更新的课程推荐关系列表
     * @return 更新记录数
     */
    int batchUpdateCourseRecommendRela(@Param("list") List<CourseRecommendRela> list);

    /**
     * 根据ID更新单个课程推荐关系
     *
     * @param record 待更新的课程推荐关系
     * @return 更新记录数
     */
    int updateCourseRecommendRela(@Param("record") CourseRecommendRela record);

    /**
     * 根据推荐ID查询课程推荐关系列表
     * @param remdId
     * @return
     */
    List<CourseRecommendRela>selectCourseRecommendRelaByRemdId(Long remdId);
}
