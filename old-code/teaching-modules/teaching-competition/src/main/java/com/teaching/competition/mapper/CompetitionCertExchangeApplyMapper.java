package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionCertExchangeApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛证互通申请Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CompetitionCertExchangeApplyMapper {
    /**
     * 查询赛证互通申请
     *
     * @param applyId 赛证互通申请主键
     * @return 赛证互通申请
     */
    public CompetitionCertExchangeApply selectCompetitionCertExchangeApplyById(Long applyId);

    /**
     * 查询赛证互通申请列表
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 赛证互通申请集合
     */
    public List<CompetitionCertExchangeApply> selectCompetitionCertExchangeApplyList(CompetitionCertExchangeApply competitionCertExchangeApply);

    /**
     * 新增赛证互通申请
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 结果
     */
    public int insertCompetitionCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply);

    /**
     * 修改赛证互通申请
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 结果
     */
    public int updateCompetitionCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply);

    public int updateCompetitionCertExchangeApplyPC(CompetitionCertExchangeApply competitionCertExchangeApply);

    public int updateCompetitionCertExchangeApplyInvoiceStatus(@Param("list") List<CompetitionCertExchangeApply> competitionCertExchangeApplyList);

    /**
     * 删除赛证互通申请
     *
     * @param applyId 赛证互通申请主键
     * @return 结果
     */
    public int deleteCompetitionCertExchangeApplyById(Long applyId);

    /**
     * 批量删除赛证互通申请
     *
     * @param applyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionCertExchangeApplyByIds(Long[] applyIds);
}
