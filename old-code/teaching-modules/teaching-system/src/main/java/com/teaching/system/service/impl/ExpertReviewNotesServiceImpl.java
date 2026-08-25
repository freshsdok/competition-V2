package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.domain.ExpertReviewNotes;
import com.teaching.system.mapper.ExpertReviewNotesMapper;
import com.teaching.system.service.IExpertReviewNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专家审阅备注信息记录Service业务层处理
 *
 * @author teaching
 * @date 2026-04-24
 */
@Service
public class ExpertReviewNotesServiceImpl implements IExpertReviewNotesService {
    @Autowired
    private ExpertReviewNotesMapper expertReviewNotesMapper;

    /**
     * 查询专家审阅备注信息记录
     *
     * @param id 专家审阅备注信息记录主键
     * @return 专家审阅备注信息记录
     */
    @Override
    public ExpertReviewNotes selectExpertReviewNotesById(Long id) {
        return expertReviewNotesMapper.selectExpertReviewNotesById(id);
    }

    /**
     * 查询专家审阅备注信息记录列表
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 专家审阅备注信息记录
     */
    @Override
    public List<ExpertReviewNotes> selectExpertReviewNotesList(ExpertReviewNotes expertReviewNotes) {
        return expertReviewNotesMapper.selectExpertReviewNotesList(expertReviewNotes);
    }

    /**
     * 新增专家审阅备注信息记录
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 结果
     */
    @Override
    public int insertExpertReviewNotes(ExpertReviewNotes expertReviewNotes) {
        expertReviewNotes.setCreateTime(DateUtils.getNowDate());
        return expertReviewNotesMapper.insertExpertReviewNotes(expertReviewNotes);
    }

    /**
     * 修改专家审阅备注信息记录
     *
     * @param expertReviewNotes 专家审阅备注信息记录
     * @return 结果
     */
    @Override
    public int updateExpertReviewNotes(ExpertReviewNotes expertReviewNotes) {
        expertReviewNotes.setUpdateTime(DateUtils.getNowDate());
        return expertReviewNotesMapper.updateExpertReviewNotes(expertReviewNotes);
    }

    /**
     * 批量删除专家审阅备注信息记录
     *
     * @param ids 需要删除的专家审阅备注信息记录主键
     * @return 结果
     */
    @Override
    public int deleteExpertReviewNotesByIds(Long[] ids) {
        return expertReviewNotesMapper.deleteExpertReviewNotesByIds(ids);
    }

    /**
     * 删除专家审阅备注信息记录信息
     *
     * @param id 专家审阅备注信息记录主键
     * @return 结果
     */
    @Override
    public int deleteExpertReviewNotesById(Long id) {
        return expertReviewNotesMapper.deleteExpertReviewNotesById(id);
    }
}

