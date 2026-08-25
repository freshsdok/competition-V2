package com.teaching.system.service;


import java.util.List;

import com.teaching.system.domain.ProcessedRelation;
import com.teaching.system.domain.ReviewRecord;
import com.teaching.system.domain.ReviewTaskSpecialistRelation;

/**
 * 评审文件处理前后对应关系Service接口
 *
 * @author teaching
 * @date 2026-04-23
 */
public interface IProcessedRelationService {
    /**
     * 查询评审文件处理前后对应关系
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 评审文件处理前后对应关系
     */
    public ProcessedRelation selectProcessedRelationById(Long id);

    /**
     * 查询评审文件处理前后对应关系列表
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 评审文件处理前后对应关系集合
     */
    public List<ProcessedRelation> selectProcessedRelationList(ProcessedRelation processedRelation);

    /**
     * 新增评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    public int insertProcessedRelation(ProcessedRelation processedRelation);

    /**
     * 修改评审文件处理前后对应关系
     *
     * @param processedRelation 评审文件处理前后对应关系
     * @return 结果
     */
    public int updateProcessedRelation(ProcessedRelation processedRelation);

    /**
     * 批量删除评审文件处理前后对应关系
     *
     * @param ids 需要删除的评审文件处理前后对应关系主键集合
     * @return 结果
     */
    public int deleteProcessedRelationByIds(Long[] ids);

    /**
     * 删除评审文件处理前后对应关系信息
     *
     * @param id 评审文件处理前后对应关系主键
     * @return 结果
     */
    public int deleteProcessedRelationById(Long id);

    /**
     * 修改审阅状态
     * @param reviewRecord
     * @return
     */
    public int updateProcessedRelationReviewStatus(ReviewRecord reviewRecord);

    /**
     * 更新最后预览到的页面
     * @param reviewRecord
     * @return
     */
    public int updateLastPageFlagByRelaId(ReviewRecord reviewRecord);

    /**
     * 检查是否可以继续审阅 根据开始和截至时间
     * @param fileId
     * @return
     */
    public boolean checkCanContinue(Long fileId);
}
