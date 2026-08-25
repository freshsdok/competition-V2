package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewTaskAllotGroup;
import com.teaching.system.domain.ReviewTaskAllotGroupRelation;

import java.util.List;

/**
 * 评审任务分配组信息Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewTaskAllotGroupMapper {

    /**
     * 查询评审任务分配组信息
     *
     * @param reviewGroupId 评审任务分配组信息主键
     * @return 评审任务分配组信息
     */
    public ReviewTaskAllotGroup selectReviewTaskAllotGroupByReviewGroupId(Long reviewGroupId);

    /**
     * 查询评审任务分配组信息列表
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 评审任务分配组信息集合
     */
    public List<ReviewTaskAllotGroup> selectReviewTaskAllotGroupList(ReviewTaskAllotGroup reviewTaskAllotGroup);

    /**
     * 新增评审任务分配组信息
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 结果
     */
    public int insertReviewTaskAllotGroup(ReviewTaskAllotGroup reviewTaskAllotGroup);

    /**
     * 修改评审任务分配组信息
     *
     * @param reviewTaskAllotGroup 评审任务分配组信息
     * @return 结果
     */
    public int updateReviewTaskAllotGroup(ReviewTaskAllotGroup reviewTaskAllotGroup);

    /**
     * 删除评审任务分配组信息
     *
     * @param reviewGroupId 评审任务分配组信息主键
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupByReviewGroupId(Long reviewGroupId);

    /**
     * 批量删除评审任务分配组信息
     *
     * @param reviewGroupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupByReviewGroupIds(Long[] reviewGroupIds);

    /**
     * 批量删除评审任务分配组关联关系
     *
     * @param reviewGroupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByReviewGroupIds(Long[] reviewGroupIds);

    /**
     * 批量新增评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelationList 评审任务分配组关联关系列表
     * @return 结果
     */
    public int batchReviewTaskAllotGroupRelation(List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList);
}
