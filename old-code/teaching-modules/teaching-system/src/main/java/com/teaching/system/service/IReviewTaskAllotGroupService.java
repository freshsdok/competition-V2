package com.teaching.system.service;

import com.teaching.system.domain.ReviewTaskAllotGroup;

import java.util.List;

/**
 * 评审任务分配组信息Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewTaskAllotGroupService {
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
     * 批量删除评审任务分配组信息
     *
     * @param reviewGroupIds 需要删除的评审任务分配组信息主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupByReviewGroupIds(Long[] reviewGroupIds);

    /**
     * 删除评审任务分配组信息信息
     *
     * @param reviewGroupId 评审任务分配组信息主键
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupByReviewGroupId(Long reviewGroupId);
}
