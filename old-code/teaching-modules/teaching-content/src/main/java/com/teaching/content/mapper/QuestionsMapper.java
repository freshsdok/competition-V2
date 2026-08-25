package com.teaching.content.mapper;

import com.teaching.content.domain.Questions;

import java.util.List;

/**
 * 常见问题Mapper接口
 *
 * @author teaching
 * @date 2025-11-06
 */
public interface QuestionsMapper {
    /**
     * 查询常见问题
     *
     * @param id 常见问题主键
     * @return 常见问题
     */
    public Questions selectQuestionsById(Long id);

    /**
     * 查询常见问题列表
     *
     * @param questions 常见问题
     * @return 常见问题集合
     */
    public List<Questions> selectQuestionsList(Questions questions);

    /**
     * 新增常见问题
     *
     * @param questions 常见问题
     * @return 结果
     */
    public int insertQuestions(Questions questions);

    /**
     * 修改常见问题
     *
     * @param questions 常见问题
     * @return 结果
     */
    public int updateQuestions(Questions questions);

    /**
     * 删除常见问题
     *
     * @param id 常见问题主键
     * @return 结果
     */
    public int deleteQuestionsById(Long id);

    /**
     * 批量删除常见问题
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQuestionsByIds(Long[] ids);
}
