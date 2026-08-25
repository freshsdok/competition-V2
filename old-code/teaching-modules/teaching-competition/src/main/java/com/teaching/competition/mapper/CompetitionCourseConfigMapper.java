package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionCourseConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事关联课程配置Mapper接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface CompetitionCourseConfigMapper 
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
     * @param courseConfigList 赛事关联课程配置
     * @return 结果
     */
    public int insertCompetitionCourseConfig(@Param("courseConfigList") List<CompetitionCourseConfig> courseConfigList);

    /**
     * 修改赛事关联课程配置
     * 
     * @param competitionCourseConfig 赛事关联课程配置
     * @return 结果
     */
    public int updateCompetitionCourseConfig(CompetitionCourseConfig competitionCourseConfig);

    /**
     * 删除赛事关联课程配置
     * 
     * @param competitionSeriesId 赛事关联课程配置主键
     * @return 结果
     */
    public int deleteCompetitionCourseConfigBySeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事关联课程配置
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionCourseConfigByCourseConfigIds(Long[] competitionSeriesIds);
}
