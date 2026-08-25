package com.teaching.system.service;

import com.teaching.system.domain.ExpertReviewNotes;

import java.util.List;

/**
 * 专家审阅备注信息记录Service接口
 *
 * @author teaching
 * @date 2026-04-24
 */
public interface IExpertReviewNotesService {
    /**
     * 查询专家审阅备注信息记录
     *
     * @param id 专家审阅备注信息记录主键
     * @return 专家审阅备注信息记录
     */
    public ExpertReviewNotes selectExpertReviewNotesById(Long id);

    /**
     * 查询专家审阅备注信息记录列表
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 专家审阅备注信息记录集合
     */
    public List<ExpertReviewNotes> selectExpertReviewNotesList(ExpertReviewNotes expertReviewNotes);

    /**
     * 新增专家审阅备注信息记录
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 结果
     */
    public int insertExpertReviewNotes(ExpertReviewNotes expertReviewNotes);

    /**
     * 修改专家审阅备注信息记录
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 结果
     */
    public int updateExpertReviewNotes(ExpertReviewNotes expertReviewNotes);

    /**
     * 批量删除专家审阅备注信息记录
     *
     * @param ids 需要删除的专家审阅备注信息记录主键集合
     * @return 结果
     */
    public int deleteExpertReviewNotesByIds(Long[] ids);

    /**
     * 删除专家审阅备注信息记录信息
     *
     * @param id 专家审阅备注信息记录主键
     * @return 结果
     */
    public int deleteExpertReviewNotesById(Long id);
}
