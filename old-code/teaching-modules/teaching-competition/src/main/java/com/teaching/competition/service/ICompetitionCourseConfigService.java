package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionCourseConfig;

import java.util.List;

/**
 * 赛事关联课程配置Service接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface ICompetitionCourseConfigService 
{
    /**
     * 查询赛事关联课程配置
     * 
     * @param courseConfigId 赛事关联课程配置主键
     * @return 赛事关联课程配置
     */
    public CompetitionCourseConfig selectCompetitionCourseConfigByCourseConfigId(Long courseConfigId);

    /**
     * 查询赛事关联课程配置列表
     * 
     * @param competitionSeriesId 赛事关联课程配置
     * @return 赛事关联课程配置集合
     */
    public List<CompetitionCourseConfig> selectCompetitionCourseConfigList(Long competitionSeriesId);

    /**
     * 新增赛事关联课程配置
     * 
     * @param competitionCourseConfig 赛事关联课程配置
     * @return 结果
     */
    public int insertCompetitionCourseConfig(List<CompetitionCourseConfig> competitionCourseConfig);

    /**
     * 修改赛事关联课程配置
     * 
     * @param competitionCourseConfig 赛事关联课程配置
     * @return 结果
     */
    public int updateCompetitionCourseConfig(CompetitionCourseConfig competitionCourseConfig);

    /**
     * 批量删除赛事关联课程配置
     * 
     * @param courseConfigIds 需要删除的赛事关联课程配置主键集合
     * @return 结果
     */
    public int deleteCompetitionCourseConfigByCourseConfigIds(Long[] courseConfigIds);

    /**
     * 删除赛事关联课程配置信息
     * 
     * @param courseConfigId 赛事关联课程配置主键
     * @return 结果
     */
    public int deleteCompetitionCourseConfigByCourseConfigId(Long courseConfigId);
}
