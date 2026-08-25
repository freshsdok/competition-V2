package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewGroupSpecialistRelation;

import java.util.List;

/**
 * 专家组与专家关联关系Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewGroupSpecialistRelationMapper {

    /**
     * 查询专家组与专家关联关系
     *
     * @param groupRelaId 专家组与专家关联关系主键
     * @return 专家组与专家关联关系
     */
    public ReviewGroupSpecialistRelation selectReviewGroupSpecialistRelationByGroupRelaId(Long groupRelaId);

    /**
     * 查询专家组与专家关联关系列表
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 专家组与专家关联关系集合
     */
    public List<ReviewGroupSpecialistRelation> selectReviewGroupSpecialistRelationList(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation);

    /**
     * 新增专家组与专家关联关系
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 结果
     */
    public int insertReviewGroupSpecialistRelation(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation);

    /**
     * 修改专家组与专家关联关系
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 结果
     */
    public int updateReviewGroupSpecialistRelation(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation);

    /**
     * 删除专家组与专家关联关系
     *
     * @param groupRelaId 专家组与专家关联关系主键
     * @return 结果
     */
    public int deleteReviewGroupSpecialistRelationByGroupRelaId(Long groupRelaId);

    /**
     * 批量删除专家组与专家关联关系
     *
     * @param groupRelaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewGroupSpecialistRelationByGroupRelaIds(Long[] groupRelaIds);

    /**
     * 根据专家组ID查询关联关系列表
     *
     * @param groupId 专家组ID
     * @return 专家组与专家关联关系集合
     */
    public List<ReviewGroupSpecialistRelation> selectReviewGroupSpecialistRelationByGroupId(Long groupId);

    public List<ReviewGroupSpecialistRelation> selectReviewGroupSpecialistRelationByGroupIds(List<Long> groupIdList);
}
