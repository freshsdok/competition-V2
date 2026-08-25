package com.teaching.competition.service;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.competition.domain.CompetitionCertExchangeRule;
import com.teaching.system.api.domain.CompetitionCertExchangeApply;
import com.teaching.competition.domain.CompetitionCertExchangeRuleUser;
import com.teaching.competition.domain.CompetitionCertExchangeRuleUserApply;
import com.teaching.system.api.domain.UserCertificate;

import java.util.List;
import java.util.Map;

public interface ICompetitionCertExchangeUserService {

    public CompetitionCertExchangeRuleUser queryUserCertExchangeApplyDetail(Long rulerId) throws Exception;

    public CompetitionCertExchangeRule queryUserCertExchangeApplyDetailNoAuth(Long rulerId);

    public CompetitionCertExchangeRuleUserApply queryUserCertExchangeApply(CompetitionCertExchangeRuleUserApply apply);

    public AjaxResult saveUserCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply);
    public Map<String,Object> saveUserCertExchangeApplyBeforeCheck(CompetitionCertExchangeApply competitionCertExchangeApply);

    public int updateUserCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply);

    public int updateCompetitionCertExchangeApplyInvoiceStatus(List<CompetitionCertExchangeApply> competitionCertExchangeApplyList);

    public List<UserCertificate> selectUserCertificateByUserId(UserCertificate userCertificate) throws Exception;
}
