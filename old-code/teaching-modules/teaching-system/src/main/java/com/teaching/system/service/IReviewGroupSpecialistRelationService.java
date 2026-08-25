package com.teaching.system.service;

import com.teaching.system.domain.ReviewGroupSpecialistRelation;

import java.util.List;

/**
 * 专家组与专家关联关系Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewGroupSpecialistRelationService {
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
     * 批量删除专家组与专家关联关系
     *
     * @param groupRelaIds 需要删除的专家组与专家关联关系主键集合
     * @return 结果
     */
    public int deleteReviewGroupSpecialistRelationByGroupRelaIds(Long[] groupRelaIds);

    /**
     * 删除专家组与专家关联关系信息
     *
     * @param groupRelaId 专家组与专家关联关系主键
     * @return 结果
     */
    public int deleteReviewGroupSpecialistRelationByGroupRelaId(Long groupRelaId);
}
