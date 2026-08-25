package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewSpecialistGroupInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家组Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewSpecialistGroupInfoMapper {

    /**
     * 查询专家组
     *
     * @param groupId 专家组主键
     * @return 专家组
     */
    public ReviewSpecialistGroupInfo selectReviewSpecialistGroupInfoByGroupId(Long groupId);

    /**
     * 查询专家组列表
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 专家组集合
     */
    public List<ReviewSpecialistGroupInfo> selectReviewSpecialistGroupInfoList(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 新增专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    public int insertReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 修改专家组
     *
     * @param reviewSpecialistGroupInfo 专家组
     * @return 结果
     */
    public int updateReviewSpecialistGroupInfo(ReviewSpecialistGroupInfo reviewSpecialistGroupInfo);

    /**
     * 删除专家组
     *
     * @param groupId 专家组主键
     * @return 结果
     */
    public int deleteReviewSpecialistGroupInfoByGroupId(Long groupId);

    /**
     * 批量删除专家组
     *
     * @param groupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewSpecialistGroupInfoByGroupIds(Long[] groupIds);

    /**
     * 批量删除专家组与专家关联关系
     *
     * @param groupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewGroupSpecialistRelationByGroupIds(Long[] groupIds);

    /**
     * 批量新增专家组与专家关联关系
     *
     * @param reviewGroupSpecialistRelationList 专家组与专家关联关系列表
     * @return 结果
     */
    public int batchReviewGroupSpecialistRelation(List<com.teaching.system.domain.ReviewGroupSpecialistRelation> reviewGroupSpecialistRelationList);
}
