package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewTaskSpecialistRelation;

import java.util.List;

/**
 * 任务分配专家关联关系Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewTaskSpecialistRelationMapper {

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
     * 批量新增任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelationList 任务分配专家关联关系列表
     * @return 结果
     */
    public int batchInsertReviewTaskSpecialistRelation(List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList);

    /**
     * 修改任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelation 任务分配专家关联关系
     * @return 结果
     */
    public int updateReviewTaskSpecialistRelation(ReviewTaskSpecialistRelation reviewTaskSpecialistRelation);

    /**
     * 批量更新任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelationList 任务分配专家关联关系列表
     * @return 结果
     */
    public int batchUpdateReviewTaskSpecialistRelation(List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList);

    /**
     * 删除任务分配专家关联关系
     *
     * @param relaId 任务分配专家关联关系主键
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByRelaId(Long relaId);

    /**
     * 批量删除任务分配专家关联关系
     *
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByRelaIds(Long[] relaIds);

    /**
     * 根据评审任务ID查询关联关系列表
     *
     * @param reviewId 评审任务ID
     * @return 任务分配专家关联关系集合
     */
    public List<ReviewTaskSpecialistRelation> selectReviewTaskSpecialistRelationByReviewId(Long reviewId);

    /**
     * 根据评审任务ID删除关联关系
     *
     * @param reviewId 评审任务ID
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByReviewId(Long reviewId);

    // 批量删除专家与任务分配专家关联关系集合
    public int deleteReviewTaskSpecialistRelationByUserId(Long userId,Long reviewId);
}
