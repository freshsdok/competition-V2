package com.teaching.system.service;

import com.teaching.system.domain.ReviewTaskInfo;
import com.teaching.system.domain.ReviewTaskInfoReq;
import com.teaching.system.domain.vo.ExpertReviewInfo;

import java.util.List;
import java.util.Map;

/**
 * 评审任务分配信息Service接口
 *
 * @author teaching
 * @date 2026-04-09
 */
public interface IReviewTaskInfoService {
    /**
     * 查询评审任务分配信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 评审任务分配信息
     */
    public ReviewTaskInfo selectReviewTaskInfoByReviewId(Long reviewId);

    /**
     * 查询评审任务分配信息列表
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 评审任务分配信息集合
     */
    public List<ReviewTaskInfo> selectReviewTaskInfoList(ReviewTaskInfo reviewTaskInfo);

    /**
     * 新增评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    public int insertReviewTaskInfo(ReviewTaskInfo reviewTaskInfo);

    public int batchInsertReviewTaskInfo(ReviewTaskInfoReq reviewTaskInfoReq);

    public int batchInsertSpecialistReviewTaskInfo(List<Long> reviewIdList,List<Long> userIdList);

    public int saveSpecialistGroupReviewTaskInfo(List<Long> reviewIdList,List<Long> groupIdList);

    /**
     * 修改评审任务分配信息
     *
     * @param reviewTaskInfo 评审任务分配信息
     * @return 结果
     */
    public int updateReviewTaskInfo(ReviewTaskInfo reviewTaskInfo);

    /**
     * 批量删除评审任务分配信息
     *
     * @param reviewIds 需要删除的评审任务分配信息主键集合
     * @return 结果
     */
    public int deleteReviewTaskInfoByReviewIds(Long[] reviewIds);

    /**
     * 删除评审任务分配信息信息
     *
     * @param reviewId 评审任务分配信息主键
     * @return 结果
     */
    public int deleteReviewTaskInfoByReviewId(Long reviewId);

    public List<ExpertReviewInfo> getExpertList(ExpertReviewInfo param);
    public Map<String,Object> getTaskInfoByProcessedId(Long processedId);
}
