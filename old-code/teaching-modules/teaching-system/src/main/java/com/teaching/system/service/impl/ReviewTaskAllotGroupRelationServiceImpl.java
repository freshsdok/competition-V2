package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.domain.ReviewTaskAllotGroupRelation;
import com.teaching.system.mapper.ReviewTaskAllotGroupMapper;
import com.teaching.system.mapper.ReviewTaskAllotGroupRelationMapper;
import com.teaching.system.service.IReviewTaskAllotGroupRelationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评审任务分配组关联关系Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Service
public class ReviewTaskAllotGroupRelationServiceImpl implements IReviewTaskAllotGroupRelationService {
    @Autowired
    private ReviewTaskAllotGroupRelationMapper reviewTaskAllotGroupRelationMapper;

    @Autowired
    private ReviewTaskAllotGroupMapper reviewTaskAllotGroupMapper;

    /**
     * 查询评审任务分配组关联关系
     *
     * @param relationId 评审任务分配组关联关系主键
     * @return 评审任务分配组关联关系
     */
    @Override
    public ReviewTaskAllotGroupRelation selectReviewTaskAllotGroupRelationByRelationId(Long relationId) {
        return reviewTaskAllotGroupRelationMapper.selectReviewTaskAllotGroupRelationByRelationId(relationId);
    }

    /**
     * 查询评审任务分配组关联关系列表
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 评审任务分配组关联关系集合
     */
    @Override
    public List<ReviewTaskAllotGroupRelation> selectReviewTaskAllotGroupRelationList(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation) {
        return reviewTaskAllotGroupRelationMapper.selectReviewTaskAllotGroupRelationList(reviewTaskAllotGroupRelation);
    }

    /**
     * 新增评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 结果
     */
    @Override
    public int insertReviewTaskAllotGroupRelation(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation) {
        reviewTaskAllotGroupRelation.setCreateTime(DateUtils.getNowDate());
        return reviewTaskAllotGroupRelationMapper.insertReviewTaskAllotGroupRelation(reviewTaskAllotGroupRelation);
    }

    /**
     * 修改评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 结果
     */
    @Override
    public int updateReviewTaskAllotGroupRelation(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation) {
        reviewTaskAllotGroupRelation.setUpdateTime(DateUtils.getNowDate());
        return reviewTaskAllotGroupRelationMapper.updateReviewTaskAllotGroupRelation(reviewTaskAllotGroupRelation);
    }

    /**
     * 批量删除评审任务分配组关联关系
     *
     * @param relationIds 需要删除的评审任务分配组关联关系主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskAllotGroupRelationByRelationIds(Long[] relationIds) {
        return reviewTaskAllotGroupRelationMapper.deleteReviewTaskAllotGroupRelationByRelationIds(relationIds);
    }

    /**
     * 删除评审任务分配组关联关系信息
     *
     * @param relationId 评审任务分配组关联关系主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskAllotGroupRelationByRelationId(Long relationId) {
        return reviewTaskAllotGroupRelationMapper.deleteReviewTaskAllotGroupRelationByRelationId(relationId);
    }

    @Override
    public int deleteReviewTaskAllotGroupRelationByReviewIdAndReviewGroupId(List<Long> reviewIdList,List<Long> reviewGroupIdList) {
        if(CollectionUtils.isNotEmpty(reviewIdList) && CollectionUtils.isNotEmpty(reviewGroupIdList)){
            reviewIdList.stream().forEach(reviewId -> {
                reviewGroupIdList.stream().forEach(reviewGroupId -> {
                    reviewTaskAllotGroupRelationMapper.deleteReviewTaskAllotGroupRelationByReviewIdAndReviewGroupId(reviewId,reviewGroupId);
                });
            });
            // 所在组下的所有任务都删除则该组则删除
            reviewGroupIdList.stream().forEach(reviewGroupId -> {
                if(CollectionUtils.isEmpty(reviewTaskAllotGroupRelationMapper.selectReviewTaskAllotGroupRelationByReviewGroupId(reviewGroupId))){
                    reviewTaskAllotGroupMapper.deleteReviewTaskAllotGroupByReviewGroupId(reviewGroupId);
                }
            });
        }
        return 1;
    }
}
