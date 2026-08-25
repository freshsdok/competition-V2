package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionGradeInfo;
import com.teaching.competition.domain.CompetitionGradeInfoImport;
import com.teaching.competition.domain.CompetitionGradeInfoImportReq;

import java.util.List;

/**
 * 成绩Service接口
 *
 * @author teaching
 */
public interface ICompetitionGradeInfoService {
    /**
     * 查询成绩
     *
     * @param gradeId 成绩主键
     * @return 成绩
     */
    public CompetitionGradeInfo selectCompetitionGradeInfoById(Long gradeId);

    /**
     * 查询成绩列表
     *
     * @param competitionGradeInfo 成绩
     * @return 成绩集合
     */
    public List<CompetitionGradeInfo> selectCompetitionGradeInfoList(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 新增成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    public int insertCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 修改成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    public int updateCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 删除成绩
     *
     * @param gradeId 成绩主键
     * @return 结果
     */
    public int deleteCompetitionGradeInfoById(Long gradeId);

    /**
     * 批量删除成绩
     *
     * @param gradeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionGradeInfoByIds(Long[] gradeIds);

    /**
     * 导入成绩数据
     *
     * @param gradeList 成绩数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public List<CompetitionGradeInfo> importGradeInfo(List<CompetitionGradeInfoImport> gradeList, Boolean isUpdateSupport, String operName, CompetitionGradeInfoImportReq req);

    /**
     * 更新成绩数据
     *
     * @param gradeList 成绩数据列表
     * @return 结果
     */
    public int updateGradeInfo(List<CompetitionGradeInfo> gradeList);
}
