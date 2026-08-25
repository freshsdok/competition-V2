package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionGradeInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 成绩Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CompetitionGradeInfoMapper {
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
    public List<CompetitionGradeInfo> selectCompetitionGradeInfoList2(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 新增成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    public int insertCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 批量新增成绩
     *
     * @param competitionGradeInfoList 成绩列表
     * @return 结果
     */
    public int batchInsertCompetitionGradeInfo(List<CompetitionGradeInfo> competitionGradeInfoList);

    /**
     * 修改成绩
     *
     * @param competitionGradeInfo 成绩
     * @return 结果
     */
    public int updateCompetitionGradeInfo(CompetitionGradeInfo competitionGradeInfo);

    /**
     * 批量更新成绩（根据条件）
     *
     * @param competitionGradeInfoList 成绩列表
     * @return 结果
     */
    public int batchUpdateCompetitionGradeInfo(List<CompetitionGradeInfo> competitionGradeInfoList);

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
     * 根据身份证号查询成绩
     *
     * @param idCard 身份证号
     * @return 成绩
     */
    public List<CompetitionGradeInfo> selectCompetitionGradeInfoByIdCard(CompetitionGradeInfo competitionGradeInfo);
}
