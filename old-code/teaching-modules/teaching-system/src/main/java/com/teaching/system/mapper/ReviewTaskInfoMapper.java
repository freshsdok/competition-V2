package com.teaching.system.mapper;

import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.domain.vo.ExpertReviewInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 评审任务分配信息Mapper接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface ReviewTaskInfoMapper {

    /**
     * 查询评审任务分配信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 评审任务分配信息
     */
    public ReviewTaskInfo selectReviewTaskInfoByReviewId(Long reviewId);

    // 获取专家锁分配的评审任务
    public List<ReviewTaskInfo> selectReviewTaskInfoByUserId(Long userId);

    /**
     * 查询评审任务分配信息列表
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 评审任务分配信息集合
     */
    public List<ReviewTaskInfo> selectReviewTaskInfoList(ReviewTaskInfo reviewTaskInfo);

    public List<ReviewTaskInfo> selectFileUploadReviewTaskList(ReviewTaskInfo reviewTaskInfo);

    /**
     * 新增评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    public int insertReviewTaskInfo(ReviewTaskInfo reviewTaskInfo);

    /**
     * 批量新增评审任务分配信息
     *
     * @param reviewTaskInfoList 评审任务分配信息列表
     * @return 结果
     */
    public int batchInsertReviewTaskInfo(List<ReviewTaskInfo> reviewTaskInfoList);

    /**
     * 修改评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    public int updateReviewTaskInfo(ReviewTaskInfo reviewTaskInfo);

    /**
     * 批量更新评审任务分配信息
     *
     * @param reviewTaskInfoList 评审任务分配信息列表
     * @return 结果
     */
    public int batchUpdateReviewTaskInfo(List<ReviewTaskInfo> reviewTaskInfoList);

    /**
     * 删除评审任务分配信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 结果
     */
    public int deleteReviewTaskInfoByReviewId(Long reviewId);

    /**
     * 批量删除评审任务分配信息
     *
     * @param reviewIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskInfoByReviewIds(Long[] reviewIds);

    /**
     * 批量删除评审任务分配组关联关系
     *
     * @param reviewIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskAllotGroupRelationByReviewIds(Long[] reviewIds);

    /**
     * 批量删除任务分配专家关联关系
     *
     * @param reviewIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewTaskSpecialistRelationByReviewIds(Long[] reviewIds);

    /**
     * 批量新增评审任务分配组关联关系
     *
     * @param reviewTaskAllotGroupRelationList 评审任务分配组关联关系列表
     * @return 结果
     */
    public int batchReviewTaskAllotGroupRelation(List<com.teaching.system.domain.ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList);

    /**
     * 批量新增任务分配专家关联关系
     *
     * @param reviewTaskSpecialistRelationList 任务分配专家关联关系列表
     * @return 结果
     */
    public int batchReviewTaskSpecialistRelation(List<com.teaching.system.domain.ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList);
    /**
     * 查询专家的评审列表
     * @param expertReviewInfo
     * @return
     */
    public List<ExpertReviewInfo> selectExpertList (ExpertReviewInfo expertReviewInfo);

    /**
     * 根据id查询评审任务
     * @param reviewId
     * @return
     */
    public Map<String,Object> selectTaskInfoByProcessedId(@Param("reviewId") Long reviewId,@Param("expertId") Long expertId);

    /**
     * 根据关系表id查询评审任务起止时间
     * @param relationId
     * @return
     */
    public Map<String,Object> selectTaskInfoByRelationId(@Param("relationId") Long relationId,@Param("expertId") Long expertId);

    /**
     * 根据processedId查询评审任务分配给专家的id
     * @param processedId
     * @return
     */
    public List<Long> selectExpertIdsByProcessedId(@Param("processedId") Long processedId,@Param("expertId") Long expertId);
}
