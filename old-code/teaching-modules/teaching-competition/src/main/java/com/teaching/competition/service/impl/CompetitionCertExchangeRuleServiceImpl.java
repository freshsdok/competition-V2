package com.teaching.competition.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.CertConfigInfo;
import com.teaching.competition.domain.CertExchangeRuleDetail;
import com.teaching.competition.domain.CompetitionCertExchangeRule;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.ICompetitionCertExchangeRuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 赛证互通规则Service业务层处理
 *
 * @author teaching
 */
@Service
public class CompetitionCertExchangeRuleServiceImpl implements ICompetitionCertExchangeRuleService {
    @Autowired
    private CompetitionCertExchangeRuleMapper competitionCertExchangeRuleMapper;

    @Autowired
    private CertExchangeRuleDetailMapper certExchangeRuleDetailMapper;

    @Autowired
    private UserCertificateOriginMapper userCertificateOriginMapper;

    @Autowired
    private UserCertificateMapper userCertificateMapper;

    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    /**
     * 查询赛证互通规则
     *
     * @param ruleId 赛证互通规则主键
     * @return 赛证互通规则
     */
    @Override
    public CompetitionCertExchangeRule selectCompetitionCertExchangeRuleById(Long ruleId) {
        CompetitionCertExchangeRule competitionCertExchangeRule = competitionCertExchangeRuleMapper.selectCompetitionCertExchangeRuleById(ruleId);
        if(Objects.nonNull(competitionCertExchangeRule)){
            if(CollectionUtils.isNotEmpty(competitionCertExchangeRule.getDetailList())){
                Map<Long,String> originCertConfigIdMap = competitionCertExchangeRule.getDetailList().stream()
                        .filter(p -> Objects.nonNull(p.getOriginCertConfigId()))
                        .collect(Collectors.toMap(
                                CertExchangeRuleDetail::getOriginCertConfigId,
                                CertExchangeRuleDetail::getOriginCertScore,
                                (existingValue, newValue) -> existingValue
                        ));
                Map<Long,String> originYearLimitMap = competitionCertExchangeRule.getDetailList().stream().
                        filter(p -> StringUtils.isNotEmpty(p.getOriginYearLimit()))
                        .collect(Collectors.toMap(
                                CertExchangeRuleDetail::getOriginCertConfigId,
                                CertExchangeRuleDetail::getOriginYearLimit,
                                (existingValue, newValue) -> existingValue
                        ));
                Map<Long,String> targetCertConfigIdMap = competitionCertExchangeRule.getDetailList().stream()
                        .filter(p -> Objects.nonNull(p.getTargetCertConfigId()))
                        .collect(Collectors.toMap(
                                CertExchangeRuleDetail::getTargetCertConfigId,
                                CertExchangeRuleDetail::getTargetCertScore,
                                (existingValue, newValue) -> existingValue
                        ));
                // 根据规则配置将源证书及目标证书数据进行初始化
                List<Long> certConfigIdList = competitionCertExchangeRule.getDetailList().stream()
                        .map(CertExchangeRuleDetail::getOriginCertConfigId).toList();
                List<CertConfigInfo> userOriginCertConfigList = certConfigInfoMapper.selectCertConfigInfoByIds(certConfigIdList);
                if(CollectionUtils.isNotEmpty(userOriginCertConfigList)){
                    userOriginCertConfigList.stream().forEach(configInfo -> {
                        configInfo.setOriginCertScore(originCertConfigIdMap.get(configInfo.getCertConfigId()));
                        configInfo.setOwnYear(originYearLimitMap.get(configInfo.getCertConfigId()));
                    });
                }
                competitionCertExchangeRule.setOriginCertList(userOriginCertConfigList);
                List<Long> certTargetConfigIdList = competitionCertExchangeRule.getDetailList().stream()
                        .map(CertExchangeRuleDetail::getTargetCertConfigId).toList();
                List<CertConfigInfo> userTargetCertConfigList = certConfigInfoMapper.selectCertConfigInfoByIds(certTargetConfigIdList);
                if (CollectionUtils.isNotEmpty(userTargetCertConfigList)) {
                    userTargetCertConfigList.stream().forEach(targetCertConfig -> {
                        targetCertConfig.setTargetCertScore(targetCertConfigIdMap.get(targetCertConfig.getCertConfigId()));
                    });
                }
                competitionCertExchangeRule.setTargetCertList(userTargetCertConfigList);
            }
        }
        return competitionCertExchangeRule;
    }

    /**
     * 查询赛证互通规则列表
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 赛证互通规则集合
     */
    @Override
    public List<CompetitionCertExchangeRule> selectCompetitionCertExchangeRuleList(CompetitionCertExchangeRule competitionCertExchangeRule) {
        List<CompetitionCertExchangeRule> competitionCertExchangeRules = competitionCertExchangeRuleMapper.selectCompetitionCertExchangeRuleList(competitionCertExchangeRule);
        if(CollectionUtils.isNotEmpty(competitionCertExchangeRules)){
            competitionCertExchangeRules.forEach(competitionCertExchangeRuleRes -> {
                if(CollectionUtils.isNotEmpty(competitionCertExchangeRuleRes.getDetailList())){
                    Map<Long,String> originCertConfigIdMap = competitionCertExchangeRuleRes.getDetailList().stream()
                            .filter(p -> Objects.nonNull(p.getOriginCertConfigId()))
                            .collect(Collectors.toMap(
                                    CertExchangeRuleDetail::getOriginCertConfigId,
                                    CertExchangeRuleDetail::getOriginCertScore,
                                    (existingValue, newValue) -> existingValue
                            ));
                    Map<Long,String> originYearLimitMap = competitionCertExchangeRuleRes.getDetailList().stream().
                            filter(p -> StringUtils.isNotEmpty(p.getOriginYearLimit()))
                            .collect(Collectors.toMap(
                                    CertExchangeRuleDetail::getOriginCertConfigId,
                                    CertExchangeRuleDetail::getOriginYearLimit,
                                    (existingValue, newValue) -> existingValue
                            ));
                    Map<Long,String> targetCertConfigIdMap = competitionCertExchangeRuleRes.getDetailList().stream()
                            .filter(p -> Objects.nonNull(p.getTargetCertConfigId()))
                            .collect(Collectors.toMap(
                                    CertExchangeRuleDetail::getTargetCertConfigId,
                                    CertExchangeRuleDetail::getTargetCertScore,
                                    (existingValue, newValue) -> existingValue
                            ));
                    // 根据规则配置将源证书及目标证书数据进行初始化
                    List<Long> certConfigIdList = competitionCertExchangeRuleRes.getDetailList().stream()
                            .map(CertExchangeRuleDetail::getOriginCertConfigId).toList();
                    List<CertConfigInfo> userOriginCertConfigList = certConfigInfoMapper.selectCertConfigInfoByIds(certConfigIdList);
                    if(CollectionUtils.isNotEmpty(userOriginCertConfigList)){
                        userOriginCertConfigList.stream().forEach(userCertificateOrigin -> {
                            userCertificateOrigin.setOriginCertScore(originCertConfigIdMap.get(userCertificateOrigin.getCertConfigId()));
                            userCertificateOrigin.setOwnYear(originYearLimitMap.get(userCertificateOrigin.getCertConfigId()));
                        });
                    }
                    competitionCertExchangeRuleRes.setOriginCertList(userOriginCertConfigList);
                    List<Long> certTargetIdList = competitionCertExchangeRuleRes.getDetailList().stream()
                            .map(CertExchangeRuleDetail::getTargetCertConfigId).toList();
                    List<CertConfigInfo> userCertificates = certConfigInfoMapper.selectCertConfigInfoByIds(certTargetIdList);
                    if (CollectionUtils.isNotEmpty(userCertificates)) {
                        userCertificates.stream().forEach(userCertificate -> {
                            userCertificate.setTargetCertScore(targetCertConfigIdMap.get(userCertificate.getCertConfigId()));
                        });
                    }
                    competitionCertExchangeRuleRes.setTargetCertList(userCertificates);
                }
            });
        }
        return competitionCertExchangeRules;
    }

    @Override
    public List<CompetitionCertExchangeRule> selectCompetitionCertExchangeRuleListSimple(CompetitionCertExchangeRule competitionCertExchangeRule) {
        return competitionCertExchangeRuleMapper.selectCompetitionCertExchangeRuleListSimple(competitionCertExchangeRule);
    }

    /**
     * 新增赛证互通规则
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCompetitionCertExchangeRule(CompetitionCertExchangeRule competitionCertExchangeRule) {
        competitionCertExchangeRule.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        competitionCertExchangeRule.setCreateTime(DateUtils.getNowDate());
        competitionCertExchangeRuleMapper.insertCompetitionCertExchangeRule(competitionCertExchangeRule);
        List<CertConfigInfo> originCertList = competitionCertExchangeRule.getOriginCertList();
        List<CertConfigInfo> targetCertList = competitionCertExchangeRule.getTargetCertList();
        List<CertExchangeRuleDetail> detailList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(originCertList) && CollectionUtils.isNotEmpty(targetCertList)){
            originCertList.stream().forEach(originCert -> {
                targetCertList.stream().forEach(targetCert -> {
                    CertExchangeRuleDetail certExchangeRuleDetail = new CertExchangeRuleDetail();
                    certExchangeRuleDetail.setRuleId(competitionCertExchangeRule.getRuleId());
                    certExchangeRuleDetail.setOriginCertConfigId(originCert.getCertConfigId());
                    certExchangeRuleDetail.setOriginCertName(originCert.getCertConfigName());
                    certExchangeRuleDetail.setOriginCertScore(originCert.getOriginCertScore());
                    certExchangeRuleDetail.setOriginYearLimit(originCert.getOwnYear());
                    certExchangeRuleDetail.setTargetCertConfigId(targetCert.getCertConfigId());
                    certExchangeRuleDetail.setTargetCertName(targetCert.getCertConfigName());
                    certExchangeRuleDetail.setTargetCertScore(targetCert.getTargetCertScore());
                    certExchangeRuleDetail.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                    certExchangeRuleDetail.setCreateTime(DateUtils.getNowDate());
                    detailList.add(certExchangeRuleDetail);
                });
            });
        }
        return certExchangeRuleDetailMapper.batchInsertCertExchangeRuleDetail(detailList);
    }

    /**
     * 修改赛证互通规则
     *
     * @param competitionCertExchangeRule 赛证互通规则
     * @return 结果
     */
    @Override
    @Transactional
    public int updateCompetitionCertExchangeRule(CompetitionCertExchangeRule competitionCertExchangeRule) {
        competitionCertExchangeRule.setUpdateBy(SecurityUtils.getLoginUser().getUsername());
        competitionCertExchangeRule.setUpdateTime(DateUtils.getNowDate());
        // 先删除历史规则配置信息
        certExchangeRuleDetailMapper.deleteCertExchangeRuleDetailByRulerId(competitionCertExchangeRule.getRuleId());
        List<CertConfigInfo> originCertList = competitionCertExchangeRule.getOriginCertList();
        List<CertConfigInfo> targetCertList = competitionCertExchangeRule.getTargetCertList();
        List<CertExchangeRuleDetail> detailList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(originCertList) && CollectionUtils.isNotEmpty(targetCertList)){
            originCertList.stream().forEach(originCert -> {
                targetCertList.stream().forEach(targetCert -> {
                    CertExchangeRuleDetail certExchangeRuleDetail = new CertExchangeRuleDetail();
                    certExchangeRuleDetail.setRuleId(competitionCertExchangeRule.getRuleId());
                    certExchangeRuleDetail.setOriginCertConfigId(originCert.getCertConfigId());
                    certExchangeRuleDetail.setOriginCertName(originCert.getCertConfigName());
                    certExchangeRuleDetail.setOriginCertScore(originCert.getOriginCertScore());
                    certExchangeRuleDetail.setOriginYearLimit(originCert.getOwnYear());
                    certExchangeRuleDetail.setTargetCertConfigId(targetCert.getCertConfigId());
                    certExchangeRuleDetail.setTargetCertName(targetCert.getCertConfigName());
                    certExchangeRuleDetail.setTargetCertScore(targetCert.getTargetCertScore());
                    certExchangeRuleDetail.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                    certExchangeRuleDetail.setCreateTime(DateUtils.getNowDate());
                    detailList.add(certExchangeRuleDetail);
                });
            });
        }
        certExchangeRuleDetailMapper.batchInsertCertExchangeRuleDetail(detailList);
        return competitionCertExchangeRuleMapper.updateCompetitionCertExchangeRule(competitionCertExchangeRule);
    }

    @Override
    public int updateCompetitionCertExchangeRuleMain(CompetitionCertExchangeRule competitionCertExchangeRule) {
        competitionCertExchangeRule.setUpdateTime(DateUtils.getNowDate());
        return competitionCertExchangeRuleMapper.updateCompetitionCertExchangeRule(competitionCertExchangeRule);
    }

    /**
     * 删除赛证互通规则
     *
     * @param ruleId 赛证互通规则主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCompetitionCertExchangeRuleById(Long ruleId) {
        certExchangeRuleDetailMapper.deleteCertExchangeRuleDetailByRulerId(ruleId);
        return competitionCertExchangeRuleMapper.deleteCompetitionCertExchangeRuleById(ruleId);
    }

    /**
     * 批量删除赛证互通规则
     *
     * @param ruleIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCompetitionCertExchangeRuleByIds(Long[] ruleIds) {
        return competitionCertExchangeRuleMapper.deleteCompetitionCertExchangeRuleByIds(ruleIds);
    }
}
