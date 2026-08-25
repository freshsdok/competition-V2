package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionCertExchangeRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 赛证互通规则Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CompetitionCertExchangeRuleMapper {
    /**
     * 查询赛证互通规则
     *
     * @param ruleId 赛证互通规则主键
     * @return 赛证互通规则
     */
    public CompetitionCertExchangeRule selectCompetitionCertExchangeRuleById(Long ruleId);

    /**
     * 查询赛证互通规则列表
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 赛证互通规则集合
     */
    public List<CompetitionCertExchangeRule> selectCompetitionCertExchangeRuleList(CompetitionCertExchangeRule competitionCertExchangeRule);

    /**
     * 查询赛证互通规则列表（仅主表字段，不加载明细）
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 赛证互通规则集合
     */
    public List<CompetitionCertExchangeRule> selectCompetitionCertExchangeRuleListSimple(CompetitionCertExchangeRule competitionCertExchangeRule);

    /**
     * 新增赛证互通规则
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 结果
     */
    public int insertCompetitionCertExchangeRule(CompetitionCertExchangeRule competitionCertExchangeRule);

    /**
     * 修改赛证互通规则
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 结果
     */
    public int updateCompetitionCertExchangeRule(CompetitionCertExchangeRule competitionCertExchangeRule);

    /**
     * 删除赛证互通规则
     *
     * @param ruleId 赛证互通规则主键
     * @return 结果
     */
    public int deleteCompetitionCertExchangeRuleById(Long ruleId);

    /**
     * 批量删除赛证互通规则
     *
     * @param ruleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionCertExchangeRuleByIds(Long[] ruleIds);
}
