package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.domain.ReviewTaskSpecialistRelation;
import com.teaching.system.mapper.ReviewTaskInfoMapper;
import com.teaching.system.mapper.ReviewTaskSpecialistRelationMapper;
import com.teaching.system.service.IReviewTaskSpecialistRelationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务分配专家关联关系Service业务层处理
 *
 * @author teaching
 * @date 2026-04-09
 */
@Service
public class ReviewTaskSpecialistRelationServiceImpl implements IReviewTaskSpecialistRelationService {
    @Autowired
    private ReviewTaskSpecialistRelationMapper reviewTaskSpecialistRelationMapper;

    @Autowired
    private ReviewTaskInfoMapper reviewTaskInfoMapper;

    /**
     * 查询任务分配专家关联关系
     *
     * @param relaId 任务分配专家关联关系主键
     * @return 任务分配专家关联关系
     */
    @Override
    public ReviewTaskSpecialistRelation selectReviewTaskSpecialistRelationByRelaId(Long relaId) {
        return reviewTaskSpecialistRelationMapper.selectReviewTaskSpecialistRelationByRelaId(relaId);
    }

    /**
     * 查询任务分配专家关联关系列表
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 任务分配专家关联关系集合
     */
    @Override
    public List<ReviewTaskSpecialistRelation> selectReviewTaskSpecialistRelationList(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation) {
        return reviewTaskSpecialistRelationMapper.selectReviewTaskSpecialistRelationList(reviewTaskSpecialistRelation);
    }

    /**
     * 新增任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 结果
     */
    @Override
    public int insertReviewTaskSpecialistRelation(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation) {
        reviewTaskSpecialistRelation.setCreateTime(DateUtils.getNowDate());
        return reviewTaskSpecialistRelationMapper.insertReviewTaskSpecialistRelation(reviewTaskSpecialistRelation);
    }

    /**
     * 修改任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 结果
     */
    @Override
    public int updateReviewTaskSpecialistRelation(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation) {
        reviewTaskSpecialistRelation.setUpdateTime(DateUtils.getNowDate());
        return reviewTaskSpecialistRelationMapper.updateReviewTaskSpecialistRelation(reviewTaskSpecialistRelation);
    }

    /**
     * 批量删除任务分配专家关联关系
     *
     * @param relaIds 需要删除的任务分配专家关联关系主键集合
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskSpecialistRelationByRelaIds(Long[] relaIds) {
        return reviewTaskSpecialistRelationMapper.deleteReviewTaskSpecialistRelationByRelaIds(relaIds);
    }

    /**
     * 删除任务分配专家关联关系信息
     *
     * @param relaId 任务分配专家关联关系主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteReviewTaskSpecialistRelationByRelaId(Long relaId) {
        return reviewTaskSpecialistRelationMapper.deleteReviewTaskSpecialistRelationByRelaId(relaId);
    }

    @Override
    public int deleteReviewTaskSpecialistRelationByUserId(List<Long> reviewIdList,List<Long> userIdList) {
        if(CollectionUtils.isNotEmpty(userIdList) && CollectionUtils.isNotEmpty(reviewIdList)){
            userIdList.forEach(userId -> {
                reviewIdList.forEach(reviewId -> {
                    reviewTaskSpecialistRelationMapper.deleteReviewTaskSpecialistRelationByUserId(userId,reviewId);
                });
            });
            // 一个任务中已分配所有专家删除则该任务分配状态改为未分配
            reviewIdList.stream().forEach(reviewId -> {
                List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList = reviewTaskSpecialistRelationMapper.selectReviewTaskSpecialistRelationByReviewId(reviewId);
                if(CollectionUtils.isEmpty(reviewTaskSpecialistRelationList)){
                    ReviewTaskInfo reviewTaskInfo = new ReviewTaskInfo();
                    reviewTaskInfo.setReviewId(reviewId);
                    reviewTaskInfo.setDistributeStatus("0");
                    reviewTaskInfoMapper.updateReviewTaskInfo(reviewTaskInfo);
                }
            });
        }
        return 1;
    }
}
