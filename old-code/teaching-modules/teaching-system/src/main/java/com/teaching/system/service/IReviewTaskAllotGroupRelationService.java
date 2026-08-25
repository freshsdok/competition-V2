package com.teaching.system.service;

import com.teaching.system.domain.ReviewTaskAllotGroupRelation;

import java.util.List;

/**
 * 评审任务分配组关联关系Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewTaskAllotGroupRelationService {
    /**
     * 查询评审任务分配组关联关系
     *
     * @param relationId 评审任务分配组关联关系主键
     * @return 评审任务分配组关联关系
     */
    public ReviewTaskAllotGroupRelation selectReviewTaskAllotGroupRelationByRelationId(Long relationId);

    /**
     * 查询评审任务分配组关联关系列表
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 评审任务分配组关联关系集合
     */
    public List<ReviewTaskAllotGroupRelation> selectReviewTaskAllotGroupRelationList(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation);

    /**
     * 新增评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 结果
     */
    public int insertReviewTaskAllotGroupRelation(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation);

    /**
     * 修改评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelation 评审任务分配组关联关系
     * @return 结果
     */
    public int updateReviewTaskAllotGroupRelation(ReviewTaskAllotGroupRelation reviewTaskAllotGroupRelation);

    /**
     * 批量删除评审任务分配组关联关系
     *
     * @param relationIds 需要删除的评审任务分配组关联关系主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByRelationIds(Long[] relationIds);

    /**
     * 删除评审任务分配组关联关系信息
     *
     * @param relationId 评审任务分配组关联关系主键
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByRelationId(Long relationId);

    public int deleteReviewTaskAllotGroupRelationByReviewIdAndReviewGroupId(List<Long> reviewIdList,List<Long> reviewGroupIdList);
}
