package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.Questions;
import com.teaching.content.mapper.QuestionsMapper;
import com.teaching.content.service.IQuestionsService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 常见问题Service业务层处理
 *
 * @author teaching
 * @date 2025-11-06
 */
@Service
public class QuestionsServiceImpl implements IQuestionsService {
    @Autowired
    private QuestionsMapper questionsMapper;

    /**
     * 查询常见问题
     *
     * @param id 常见问题主键
     * @return 常见问题
     */
    @Override
    public Questions selectQuestionsById(Long id) {
        return questionsMapper.selectQuestionsById(id);
    }

    /**
     * 查询常见问题列表
     *
     * @param questions 常见问题
     * @return 常见问题
     */
    @Override
    public List<Questions> selectQuestionsList(Questions questions) {
        return questionsMapper.selectQuestionsList(questions);
    }

    /**
     * 新增常见问题
     *
     * @param questions 常见问题
     * @return 结果
     */
    @Override
    public int insertQuestions(Questions questions) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        questions.setCreateBy(sysUser.getNickName());
        questions.setUserId(sysUser.getUserId());
        questions.setOrgId(sysUser.getOrgId());
        return questionsMapper.insertQuestions(questions);
    }

    /**
     * 修改常见问题
     *
     * @param questions 常见问题
     * @return 结果
     */
    @Override
    public int updateQuestions(Questions questions) {
        return questionsMapper.updateQuestions(questions);
    }

    /**
     * 批量删除常见问题
     *
     * @param ids 需要删除的常见问题主键
     * @return 结果
     */
    @Override
    public int deleteQuestionsByIds(Long[] ids) {
        return questionsMapper.deleteQuestionsByIds(ids);
    }

    /**
     * 删除常见问题信息
     *
     * @param id 常见问题主键
     * @return 结果
     */
    @Override
    public int deleteQuestionsById(Long id) {
        return questionsMapper.deleteQuestionsById(id);
    }
}
