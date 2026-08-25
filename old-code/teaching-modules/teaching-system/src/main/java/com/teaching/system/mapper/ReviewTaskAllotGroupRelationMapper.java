package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewTaskAllotGroupRelation;

import java.util.List;

/**
 * 评审任务分配组关联关系Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewTaskAllotGroupRelationMapper {

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
     * 删除评审任务分配组关联关系
     *
     * @param relationId 评审任务分配组关联关系主键
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByRelationId(Long relationId);

    /**
     * 批量删除评审任务分配组关联关系
     *
     * @param relationIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByRelationIds(Long[] relationIds);

    /**
     * 根据分配组ID查询关联关系列表
     *
     * @param reviewGroupId 分配组ID
     * @return 评审任务分配组关联关系集合
     */
    public List<ReviewTaskAllotGroupRelation> selectReviewTaskAllotGroupRelationByReviewGroupId(Long reviewGroupId);

    /**
     * 根据评审任务ID删除关联关系
     *
     * @param reviewId 评审任务ID
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByReviewId(Long reviewId);

    /**
     * 根据任务ID和分配组ID删除关联关系
     *
     * @param reviewId 评审任务ID
     * @param reviewGroupId 分配组ID
     * @return 删除结果
     */
    public int deleteReviewTaskAllotGroupRelationByReviewIdAndReviewGroupId(Long reviewId,Long reviewGroupId);
}
