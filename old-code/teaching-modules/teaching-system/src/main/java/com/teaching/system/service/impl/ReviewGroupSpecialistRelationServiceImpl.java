package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.domain.ReviewGroupSpecialistRelation;
import com.teaching.system.mapper.ReviewGroupSpecialistRelationMapper;
import com.teaching.system.service.IReviewGroupSpecialistRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 专家组与专家关联关系Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Service
public class ReviewGroupSpecialistRelationServiceImpl implements IReviewGroupSpecialistRelationService {
    @Autowired
    private ReviewGroupSpecialistRelationMapper reviewGroupSpecialistRelationMapper;

    /**
     * 查询专家组与专家关联关系
     *
     * @param groupRelaId 专家组与专家关联关系主键
     * @return 专家组与专家关联关系
     */
    @Override
    public ReviewGroupSpecialistRelation selectReviewGroupSpecialistRelationByGroupRelaId(Long groupRelaId) {
        return reviewGroupSpecialistRelationMapper.selectReviewGroupSpecialistRelationByGroupRelaId(groupRelaId);
    }

    /**
     * 查询专家组与专家关联关系列表
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 专家组与专家关联关系集合
     */
    @Override
    public List<ReviewGroupSpecialistRelation> selectReviewGroupSpecialistRelationList(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation) {
        return reviewGroupSpecialistRelationMapper.selectReviewGroupSpecialistRelationList(reviewGroupSpecialistRelation);
    }

    /**
     * 新增专家组与专家关联关系
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 结果
     */
    @Override
    public int insertReviewGroupSpecialistRelation(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation) {
        reviewGroupSpecialistRelation.setCreateTime(DateUtils.getNowDate());
        return reviewGroupSpecialistRelationMapper.insertReviewGroupSpecialistRelation(reviewGroupSpecialistRelation);
    }

    /**
     * 修改专家组与专家关联关系
     *
     * @param reviewGroupSpecialistRelation 专家组与专家关联关系
     * @return 结果
     */
    @Override
    public int updateReviewGroupSpecialistRelation(ReviewGroupSpecialistRelation reviewGroupSpecialistRelation) {
        reviewGroupSpecialistRelation.setUpdateTime(DateUtils.getNowDate());
        return reviewGroupSpecialistRelationMapper.updateReviewGroupSpecialistRelation(reviewGroupSpecialistRelation);
    }

    /**
     * 批量删除专家组与专家关联关系
     *
     * @param groupRelaIds 需要删除的专家组与专家关联关系主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewGroupSpecialistRelationByGroupRelaIds(Long[] groupRelaIds) {
        return reviewGroupSpecialistRelationMapper.deleteReviewGroupSpecialistRelationByGroupRelaIds(groupRelaIds);
    }

    /**
     * 删除专家组与专家关联关系信息
     *
     * @param groupRelaId 专家组与专家关联关系主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewGroupSpecialistRelationByGroupRelaId(Long groupRelaId) {
        return reviewGroupSpecialistRelationMapper.deleteReviewGroupSpecialistRelationByGroupRelaId(groupRelaId);
    }
}
