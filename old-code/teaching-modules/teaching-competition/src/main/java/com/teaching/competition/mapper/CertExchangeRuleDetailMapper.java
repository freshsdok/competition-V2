package com.teaching.competition.mapper;

import com.teaching.competition.domain.CertExchangeRuleDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛证互通规则明细Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CertExchangeRuleDetailMapper {
    /**
     * 查询赛证互通规则明细
     *
     * @param detailId 明细id
     * @return 赛证互通规则明细
     */
    public CertExchangeRuleDetail selectCertExchangeRuleDetailById(Long detailId);

    /**
     * 查询赛证互通规则明细列表
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 赛证互通规则明细集合
     */
    public List<CertExchangeRuleDetail> selectCertExchangeRuleDetailList(CertExchangeRuleDetail certExchangeRuleDetail);

    /**
     * 新增赛证互通规则明细
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 结果
     */
    public int insertCertExchangeRuleDetail(CertExchangeRuleDetail certExchangeRuleDetail);

    /**
     * 批量新增赛证互通规则明细
     *
     * @param certExchangeRuleDetailList 赛证互通规则明细列表
     * @return 结果
     */
    public int batchInsertCertExchangeRuleDetail(@Param("list") List<CertExchangeRuleDetail> certExchangeRuleDetailList);

    /**
     * 修改赛证互通规则明细
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 结果
     */
    public int updateCertExchangeRuleDetail(CertExchangeRuleDetail certExchangeRuleDetail);

    /**
     * 删除赛证互通规则明细
     *
     * @param detailId 明细id
     * @return 结果
     */
    public int deleteCertExchangeRuleDetailById(Long detailId);

    public int deleteCertExchangeRuleDetailByRulerId(Long ruleId);

    /**
     * 批量删除赛证互通规则明细
     *
     * @param detailIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertExchangeRuleDetailByIds(Long[] detailIds);
}
