package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.domain.UserGradeInfo;
import com.teaching.system.api.domain.CompetitionAwardsConfig;
import com.teaching.system.api.domain.CompetitionStageConfig;

import java.util.List;

/**
 * 用户成绩信息Service接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface IUserGradeInfoService 
{
    /**
     * 查询用户成绩信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 用户成绩信息
     */
    public UserGradeInfo selectUserGradeInfoByGradeId(Long gradeId);

    /**
     * 查询用户成绩信息列表
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 用户成绩信息集合
     */
    public List<UserGradeInfo> selectUserGradeInfoList(UserGradeInfo userGradeInfo);

    /**
     * 新增用户成绩信息
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    public int insertUserGradeInfo(List<UserGradeInfo> userGradeInfoList);

    public int updateUserGradeCompetitionStageConfig(CompetitionStageConfig competitionStageConfig);

    public int updateCompetitionAwardsConfig(CompetitionAwardsConfig competitionAwardsConfig);

    /**
     * 修改用户成绩信息
     * 
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    public int updateUserGradeInfo(List<UserGradeInfo> userGradeInfoList);

    /**
     * 批量删除用户成绩信息
     * 
     * @param gradeIds 需要删除的用户成绩信息主键集合
     * @return 结果
     */
    public int deleteUserGradeInfoByGradeIds(Long[] gradeIds);

    /**
     * 删除用户成绩信息信息
     * 
     * @param gradeId 用户成绩信息主键
     * @return 结果
     */
    public int deleteUserGradeInfoByGradeId(Long gradeId);

    /**
     * 生成用户晋级信息
     *
     * @param userGradeInfo 用户成绩信息
     * @return 结果
     */
    public List<CompetitionWorks> createAdvanceUserGradeInfo(UserGradeInfo userGradeInfo);

    /**
     * 生成用户成绩信息
     *
     * @param userGradeInfo 用户成绩信息
     * @return 批量生成结果
     */
    public List<UserGradeInfo> createUserGradeInfo(UserGradeInfo userGradeInfo);

    public int saveAdvanceUserGradeInfo(List<CompetitionWorks> competitionWorksList);
}
