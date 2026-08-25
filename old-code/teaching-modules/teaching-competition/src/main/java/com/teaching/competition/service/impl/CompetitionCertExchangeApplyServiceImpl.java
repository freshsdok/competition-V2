package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.mapper.CertConfigInfoMapper;
import com.teaching.competition.mapper.UserCertificateMapper;
import com.teaching.competition.mapper.UserCertificateOriginMapper;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import com.teaching.competition.mapper.CompetitionCertExchangeApplyMapper;
import com.teaching.competition.service.ICompetitionCertExchangeApplyService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 赛证互通申请Service业务层处理
 *
 * @author teaching
 */
@Service
public class CompetitionCertExchangeApplyServiceImpl implements ICompetitionCertExchangeApplyService {
    @Autowired
    private CompetitionCertExchangeApplyMapper competitionCertExchangeApplyMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private UserCertificateMapper userCertificateMapper;

    @Autowired
    private UserCertificateOriginMapper userCertificateOriginMapper;

    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    /**
     * 查询赛证互通申请
     *
     * @param applyId 赛证互通申请主键
     * @return 赛证互通申请
     */
    @Override
    public CompetitionCertExchangeApply selectCompetitionCertExchangeApplyById(Long applyId) {
        return competitionCertExchangeApplyMapper.selectCompetitionCertExchangeApplyById(applyId);
    }

    /**
     * 查询赛证互通申请列表
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 赛证互通申请集合
     */
    @Override
    public List<CompetitionCertExchangeApply> selectCompetitionCertExchangeApplyList(CompetitionCertExchangeApply competitionCertExchangeApply) {
        List<CompetitionCertExchangeApply> competitionCertExchangeApplies =
                competitionCertExchangeApplyMapper.selectCompetitionCertExchangeApplyList(competitionCertExchangeApply);
        // 翻译学校名称
        if(CollectionUtils.isNotEmpty(competitionCertExchangeApplies)){
            competitionCertExchangeApplies.stream().forEach(competitionCertExchangeApplyDes -> {
                if(StringUtils.isNotEmpty(competitionCertExchangeApplyDes.getSchool())){
                    R<NationwideCollegeInfo> nationwideCollegeInfoInfo = userService.getNationwideCollegeInfoInfo(competitionCertExchangeApplyDes.getSchool(), SecurityConstants.INNER);
                    if(R.isSuccess(nationwideCollegeInfoInfo) && Objects.nonNull(nationwideCollegeInfoInfo.getData())){
                        competitionCertExchangeApplyDes.setSchoolName(nationwideCollegeInfoInfo.getData().getSchoolName());
                    }
                }
                //获取证书名称
                if(StringUtils.isNotEmpty(competitionCertExchangeApplyDes.getTargetCertId())){
                    List<Long> certIdList = Arrays.stream(competitionCertExchangeApplyDes.getTargetCertId().split(","))
                            .filter(s -> !s.trim().isEmpty())
                            .map(Long::parseLong)
                            .toList();
                    List<CertConfigInfo> targetCertNameList = certConfigInfoMapper.selectCertConfigInfoByIds(certIdList);
                    competitionCertExchangeApplyDes.setTargetCertName(String.join(",",
                            targetCertNameList.stream().map(CertConfigInfo::getCertConfigName).toList()));
                }
                if(StringUtils.isNotEmpty(competitionCertExchangeApplyDes.getOriginCertId())){
                    List<Long> certCodeList = Arrays.stream(competitionCertExchangeApplyDes.getOriginCertId().split(","))
                            .filter(s -> !s.trim().isEmpty())
                            .map(Long::parseLong)
                            .toList();
                    List<CertConfigInfo> originCertNameList = certConfigInfoMapper.selectCertConfigInfoByIds(certCodeList);
                    competitionCertExchangeApplyDes.setOriginCertName(String.join(",",
                            originCertNameList.stream().map(CertConfigInfo::getCertConfigName).toList()));
                }
            });
        }
        return competitionCertExchangeApplies;
    }

    /**
     * 新增赛证互通申请
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 结果
     */
    @Override
    public int insertCompetitionCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply) {
        return competitionCertExchangeApplyMapper.insertCompetitionCertExchangeApply(competitionCertExchangeApply);
    }

    /**
     * 修改赛证互通申请
     *
     * @param competitionCertExchangeApply 赛证互通申请
     * @return 结果
     */
    @Override
    public int updateCompetitionCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply) {
        return competitionCertExchangeApplyMapper.updateCompetitionCertExchangeApply(competitionCertExchangeApply);
    }

    /**
     * 删除赛证互通申请
     *
     * @param applyId 赛证互通申请主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCertExchangeApplyById(Long applyId) {
        return competitionCertExchangeApplyMapper.deleteCompetitionCertExchangeApplyById(applyId);
    }

    /**
     * 批量删除赛证互通申请
     *
     * @param applyIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCompetitionCertExchangeApplyByIds(Long[] applyIds) {
        return competitionCertExchangeApplyMapper.deleteCompetitionCertExchangeApplyByIds(applyIds);
    }
}
