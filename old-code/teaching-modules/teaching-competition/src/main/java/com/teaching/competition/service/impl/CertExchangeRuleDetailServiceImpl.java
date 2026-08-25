package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CertExchangeRuleDetail;
import com.teaching.competition.mapper.CertExchangeRuleDetailMapper;
import com.teaching.competition.service.ICertExchangeRuleDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 赛证互通规则明细Service业务层处理
 *
 * @author teaching
 */
@Service
public class CertExchangeRuleDetailServiceImpl implements ICertExchangeRuleDetailService {
    @Autowired
    private CertExchangeRuleDetailMapper certExchangeRuleDetailMapper;

    /**
     * 查询赛证互通规则明细
     *
     * @param detailId 明细id
     * @return 赛证互通规则明细
     */
    @Override
    public CertExchangeRuleDetail selectCertExchangeRuleDetailById(Long detailId) {
        return certExchangeRuleDetailMapper.selectCertExchangeRuleDetailById(detailId);
    }

    /**
     * 查询赛证互通规则明细列表
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 赛证互通规则明细集合
     */
    @Override
    public List<CertExchangeRuleDetail> selectCertExchangeRuleDetailList(CertExchangeRuleDetail certExchangeRuleDetail) {
        return certExchangeRuleDetailMapper.selectCertExchangeRuleDetailList(certExchangeRuleDetail);
    }

    /**
     * 新增赛证互通规则明细
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 结果
     */
    @Override
    public int insertCertExchangeRuleDetail(CertExchangeRuleDetail certExchangeRuleDetail) {
        return certExchangeRuleDetailMapper.insertCertExchangeRuleDetail(certExchangeRuleDetail);
    }

    /**
     * 修改赛证互通规则明细
     *
     * @param certExchangeRuleDetail 赛证互通规则明细
     * @return 结果
     */
    @Override
    public int updateCertExchangeRuleDetail(CertExchangeRuleDetail certExchangeRuleDetail) {
        return certExchangeRuleDetailMapper.updateCertExchangeRuleDetail(certExchangeRuleDetail);
    }

    /**
     * 删除赛证互通规则明细
     *
     * @param detailId 明细id
     * @return 结果
     */
    @Override
    public int deleteCertExchangeRuleDetailById(Long detailId) {
        return certExchangeRuleDetailMapper.deleteCertExchangeRuleDetailById(detailId);
    }

    /**
     * 批量删除赛证互通规则明细
     *
     * @param detailIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCertExchangeRuleDetailByIds(Long[] detailIds) {
        return certExchangeRuleDetailMapper.deleteCertExchangeRuleDetailByIds(detailIds);
    }
}
