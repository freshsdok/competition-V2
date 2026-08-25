package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionCheckDataPackage;

import java.util.List;

/**
 * 校验包Mapper接口
 *
 * @author teaching
 * @date 2025-12-18
 */
public interface CompetitionCheckDataPackageMapper {
    /**
     * 查询校验包
     *
     * @param packageId 校验包主键
     * @return 校验包
     */
    public CompetitionCheckDataPackage selectCompetitionCheckDataPackageByPackageId(Long packageId);

    /**
     * 查询校验包列表
     *
     * @param competitionCheckDataPackage 校验包
     * @return 校验包集合
     */
    public List<CompetitionCheckDataPackage> selectCompetitionCheckDataPackageList(CompetitionCheckDataPackage competitionCheckDataPackage);

    /**
     * 新增校验包
     *
     * @param competitionCheckDataPackage 校验包
     * @return 结果
     */
    public int insertCompetitionCheckDataPackage(CompetitionCheckDataPackage competitionCheckDataPackage);

    /**
     * 修改校验包
     *
     * @param competitionCheckDataPackage 校验包
     * @return 结果
     */
    public int updateCompetitionCheckDataPackage(CompetitionCheckDataPackage competitionCheckDataPackage);

    /**
     * 删除校验包
     *
     * @param packageId 校验包主键
     * @return 结果
     */
    public int deleteCompetitionCheckDataPackageByPackageId(Long packageId);

    /**
     * 批量删除校验包
     *
     * @param packageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionCheckDataPackageByPackageIds(Long[] packageIds);
}
