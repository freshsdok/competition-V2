package com.teaching.system.service;

import com.teaching.system.domain.ReviewTaskSpecialistRelation;

import java.util.List;

/**
 * 任务分配专家关联关系Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewTaskSpecialistRelationService {
    /**
     * 查询任务分配专家关联关系
     *
     * @param relaId 任务分配专家关联关系主键
     * @return 任务分配专家关联关系
     */
    public ReviewTaskSpecialistRelation selectReviewTaskSpecialistRelationByRelaId(Long relaId);

    /**
     * 查询任务分配专家关联关系列表
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 任务分配专家关联关系集合
     */
    public List<ReviewTaskSpecialistRelation> selectReviewTaskSpecialistRelationList(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation);

    /**
     * 新增任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 结果
     */
    public int insertReviewTaskSpecialistRelation(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation);

    /**
     * 修改任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 结果
     */
    public int updateReviewTaskSpecialistRelation(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation);

    /**
     * 批量删除任务分配专家关联关系
     *
     * @param relaIds 需要删除的任务分配专家关联关系主键集合
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByRelaIds(Long[] relaIds);

    /**
     * 删除任务分配专家关联关系信息
     *
     * @param relaId 任务分配专家关联关系主键
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByRelaId(Long relaId);


    public int deleteReviewTaskSpecialistRelationByUserId(List<Long> reviewIdList,List<Long> userIdList);
}
