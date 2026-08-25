package com.teaching.system.mapper;

import com.teaching.system.domain.ExpertReviewNotes;

import java.util.List;

/**
 * 专家审阅备注信息记录Mapper接口
 *
 * @author teaching
 * @date 2026-04-24
 */
public interface ExpertReviewNotesMapper {
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
     * 删除专家审阅备注信息记录
     *
     * @param id 专家审阅备注信息记录主键
     * @return 结果
     */
    public int deleteExpertReviewNotesById(Long id);

    /**
     * 批量删除专家审阅备注信息记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExpertReviewNotesByIds(Long[] ids);
}
