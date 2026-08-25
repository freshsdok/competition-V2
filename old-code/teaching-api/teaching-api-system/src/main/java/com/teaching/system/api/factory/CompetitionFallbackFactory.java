package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 赛事服务降级处理
 *
 * @author teaching
 */
@Component
public class CompetitionFallbackFactory implements FallbackFactory<CompetitionService> {

    private static final Logger log = LoggerFactory.getLogger(CompetitionFallbackFactory.class);

    @Override
    public CompetitionService create(Throwable cause) {
        return new CompetitionService() {
            @Override
            public R<CompetitionDetailInfo> getCompetitionDetailInfoById(Long competitionId, String source) {
                return R.fail("获取赛事信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionSeriesInfo>> getNoStartCompetitionInfo(String checkStatus, String source) {
                return R.fail("根据状态获取赛事信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateTaskCompetitionInfoStatus(CompetitionSeriesInfo seriesInfo, String source) {
                return R.fail("定时任务启动赛事失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateCompetitionInfoStatus(CompetitionSeriesInfo seriesInfo, String source) {
                return R.fail("修改赛事状态失败:" + cause.getMessage());
            }

            @Override
            public R<TeamManagerInfo> getInnerTeamDetailInfo(Long teamId, String source) {
                return R.fail("获取团队详情信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<TeamManagerInfo>> getInnerTeamDetailInfo(TeamManagerInfo teamManagerInfo, String source) {
                return R.fail("获取团队信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateTeamManagerStatus(TeamManagerInfo teamManagerInfo, String source) {
                return R.fail("修改团队信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateCompetitionApplyInfoStatus(CompetitionApplyInfo competitionApplyInfo, String source) {
                return R.fail("修改报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<CompetitionApplyInfo> getInnerApplyDetailInfo(Long memberId, String source) {
                return R.fail("获取报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> selectCompetitionApplyInfoListByUserId(Map<String,Object> param, String source) {
                return R.fail("获取个人报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> getInnerApplyUserInfo(CompetitionApplyInfo competitionApplyInfo, String source) {
                return R.fail("根据身份证获取个人报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> selectCompetitionApplyInfoByPayStatus(String source) {
                return R.fail("获取未支付报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updatePayStatus(List<CompetitionApplyInfo> competitionApplyInfoList, String source) {
                return R.fail("修改报名支付信息失败:" + cause.getMessage());
            }

            @Override
            public R<CompetitionTrackInfo> getInnerCompetitionTrackDetail(Long trackId, String source) {
                return R.fail("查询赛道配置信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateCompetitionTrackStatus(CompetitionTrackInfo competitionTrackInfo, String source) {
                return R.fail("修改赛道审核状态失败:" + cause.getMessage());
            }

            @Override
            public R<Map<String, Object>> getTeamInfo(Long competitionSeriesId, String source) {
                return R.fail("获取订单信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateCompetitionTrackStatus(Map<String, Object> map, String source) {
                return R.fail("更新支付状态失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfoVO>> getDetailForOrder(Map<String, Object> map, String source) {
                return R.fail("获取订单详情失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> queryTeamMemberInvoiceStatus(CompetitionApplyInfo competitionApplyInfo, String source) {
                return R.fail("获取订单发票成员信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<Map<String,Object>>> queryCompetitionInfo(String source) {
                return R.fail("获取当前赛事信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> getCompetitionApplyInfoByPayStatusForUserGroup(Long userId, String source) {
                return R.fail("查询已缴费的报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<Void> syncCompetitionApplyInfo(String updateSize,String source) {
                return R.fail("自动注册学生信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> bindTeacherCompetitionUser(Long userId, String phone, String source) {
                return R.fail("关联教师赛报名失败:" + cause.getMessage());
            }

            @Override
            public R<Set<Long>> getUserInfoByCompetitions(Map<String, Object> param, String source) {
                return R.fail("查询已缴费的报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> selectAllUserInfoByCompetitions(Map<String, Object> param, String source) {
                return R.fail("查询赛事下所有已报名的报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<Void> insertChangeLog(ChangeLog changeLog, String source) {
                return R.fail("新增日志信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> deleteCompetitionApplyInfoByMemberId(String memberIds, String source) {
                return R.fail("删除报名人员信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> saveBatchCompetitionApplyInfo(List<CompetitionApplyInfo> competitionApplyInfoList, String source) {
                return R.fail("新增报名人员信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> removeTeam(String teamCode, String source) {
                return R.fail("删除团队信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> selectCompetitionApplyTeamCode(String teamCode, String source) {
                return R.fail("根据code获取团队信息失败:" + cause.getMessage());
            }

            @Override
            public R<Boolean> checkRepaymentTimes(String source, String competitionSeriesId, String teamCode) {
                return R.fail("校验剩余次数失败:" + cause.getMessage());
            }

            @Override
            public R<Boolean> recordUsedTimes(String source, String teamCode, String competitionSeriesId) {
                return R.fail("记录退费重缴使用次数失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> cancelRepaymentOperationTimes(String teamCode, String source) {
                return R.fail("取消团队退费重缴次数失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateUserCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply, String source) {
                return R.fail("更新申请赛证互通支付信息失败:" + cause.getMessage());
            }

            @Override
            public R<Integer> updateUserCertExchangeApplyInvoiceStatus(List<CompetitionCertExchangeApply> competitionCertExchangeApplyList, String source) {
                return R.fail("更新申请赛证互通发票信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> getApplyInfoByUsrIdAndCompetitionId(Long userId, Long competitionId, String source) {
                return R.fail("查询用户所在团队报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<List<CompetitionApplyInfo>> getCompetitionApplyInfoListByCompetitionSeriesId(Long competitionId, String source) {
                return R.fail("查询报名信息失败:" + cause.getMessage());
            }

            @Override
            public R<String> getCompetitionFee(String secondLevelCode, String source) {
                return R.fail("查询组别单价失败:" + cause.getMessage());
            }

            @Override
            public R<List<Map<String, Object>>> listCertExchangeRuleInner(Map<String, Object> param, String source) {
                return R.fail("查询赛证互通规则失败:" + cause.getMessage());
            }
        };
    }
}
