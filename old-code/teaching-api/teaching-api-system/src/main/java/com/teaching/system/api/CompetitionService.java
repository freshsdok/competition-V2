package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.factory.CompetitionFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(contextId = "competitionService", value = ServiceNameConstants.COMPETITION_SERVICE, fallbackFactory = CompetitionFallbackFactory.class)
public interface CompetitionService {

    /**
     * 通过赛事id查询赛事详情
     */
    @GetMapping("/competitionManager/getInnerCompetitionDetailInfo")
    public R<CompetitionDetailInfo> getCompetitionDetailInfoById(@RequestParam Long competitionId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过状态查询赛事信息
     */
    @GetMapping("/competitionManager/getNoStartCompetitionInfo")
    public R<List<CompetitionSeriesInfo>> getNoStartCompetitionInfo(@RequestParam String checkStatus, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 定时任务启动赛事
     */
    @PostMapping("/competitionManager/updateTaskCompetitionInfoStatus")
    public R<Integer> updateTaskCompetitionInfoStatus(@RequestBody CompetitionSeriesInfo seriesInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改赛事状态
     */
    @PostMapping("/competitionManager/updateInnerCompetitionInfoStatus")
    public R<Integer> updateCompetitionInfoStatus(@RequestBody CompetitionSeriesInfo seriesInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取团队详情
     *
     * @param teamCode
     * @param source
     * @return
     */
    @GetMapping("/teamManager/getInnerTeamDetailInfo/{teamId}")
    public R<TeamManagerInfo> getInnerTeamDetailInfo(@PathVariable("teamId") Long teamId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取团队信息
     *
     * @param teamManagerInfo
     * @param source
     * @return
     */
    @PostMapping("/teamManager/getInnerTeamManagerInfoList")
    public R<List<TeamManagerInfo>> getInnerTeamDetailInfo(@RequestBody TeamManagerInfo teamManagerInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改团队状态
     *
     * @param teamManagerInfo
     * @param source
     * @return
     */
    @PostMapping("/teamManager/updateTeamManagerStatus")
    public R<Integer> updateTeamManagerStatus(@RequestBody TeamManagerInfo teamManagerInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改报名状态
     *
     * @param competitionApplyInfo
     * @return
     */
    @PostMapping("/competitionApply/updateCompetitionApplyInfoStatus")
    public R<Integer> updateCompetitionApplyInfoStatus(@RequestBody CompetitionApplyInfo competitionApplyInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取报名详情
     *
     * @param memberId
     * @return
     */
    @GetMapping("/competitionApply/getInnerApplyDetailInfo/{memberId}")
    public R<CompetitionApplyInfo> getInnerApplyDetailInfo(@PathVariable("memberId") Long memberId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/competitionApply/getInnerApplyDetailInfo")
    public R<List<CompetitionApplyInfo>> selectCompetitionApplyInfoListByUserId(@RequestBody Map<String,Object> param, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/competitionApply/getInnerApplyUserInfo")
    public R<List<CompetitionApplyInfo>> getInnerApplyUserInfo(@RequestBody CompetitionApplyInfo competitionApplyInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 获取未支付成功的报名信息
    @GetMapping("/competitionApply/selectCompetitionApplyInfoByPayStatus")
    public R<List<CompetitionApplyInfo>> selectCompetitionApplyInfoByPayStatus(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询某userId已缴费的报名信息
     * @param userId
     * @param source
     * @return
     */
    @GetMapping("/competitionApply/getCompetitionApplyInfoByPayStatusForUserGroup/{userId}")
    public R<List<CompetitionApplyInfo>> getCompetitionApplyInfoByPayStatusForUserGroup(@PathVariable Long userId,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询匹配规则的报名成功已缴费的人员信息
     * @param param
     * @param source
     * @return
     */
    @PostMapping("/competitionApply/getUserInfoByCompetitions")
    public R<Set<Long>> getUserInfoByCompetitions(@RequestBody Map<String,Object> param, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    //获取赛事已报名人员信息
    @PostMapping("/competitionApply/selectAllUserInfoByCompetitions")
    public R<List<CompetitionApplyInfo>> selectAllUserInfoByCompetitions(@RequestBody Map<String,Object> param, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改支付状态
     *
     * @param competitionApplyInfoList
     * @return
     */
    @PostMapping("/competitionApply/updatePayStatus")
    public R<Integer> updatePayStatus(@RequestBody List<CompetitionApplyInfo> competitionApplyInfoList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取赛事赛道详情
     *
     * @param competitionTrackId
     * @return
     */
    @GetMapping("/competitionTrackInfo/getInnerCompetitionTrackDetail/{trackId}")
    public R<CompetitionTrackInfo> getInnerCompetitionTrackDetail(@PathVariable("trackId") Long trackId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改赛事赛道审核状态
     *
     * @param competitionTrackInfo
     * @return
     */
    @PostMapping("/competitionTrackInfo/updateCompetitionTrackStatus")
    public R<Integer> updateCompetitionTrackStatus(@RequestBody CompetitionTrackInfo competitionTrackInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 返回订单总额、团队codes
     *
     * @param competitionSeriesId 赛事id
     * @param source
     * @return
     */
    @GetMapping("/userCompetition/getTeamInfo/{competitionSeriesId}")
    public R<Map<String, Object>> getTeamInfo(@PathVariable Long competitionSeriesId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 更新团队支付状态
     *
     * @param map    payStatus支付状态（必传），competitionSeriesId赛事id（不必传），teamCodeList团队编码列表（必传）
     * @param source
     * @return
     */
    @PostMapping("/userCompetition/updateTeamOrderStatus")
    public R<Integer> updateCompetitionTrackStatus(@RequestBody Map<String, Object> map, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取订单团队详情
     *
     * @param map    competitionSeriesId赛事id（不必传），teamCodeList团队编码列表（必传）
     * @param source
     * @return
     */
    @PostMapping("/userCompetition/getDetailForOrder")
    public R<List<CompetitionApplyInfoVO>> getDetailForOrder(@RequestBody Map<String, Object> map, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询团队成员发票状态
     *
     * @param competitionApplyInfo
     * @param source
     * @return
     */
    @PostMapping("/competitionApply/queryTeamMemberInvoiceStatus")
    public R<List<CompetitionApplyInfo>> queryTeamMemberInvoiceStatus(@RequestBody CompetitionApplyInfo competitionApplyInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取赛事信息
     *
     * @param source
     * @return
     */
    @GetMapping("/competitionManager/queryCompetitionInfo")
    public R<List<Map<String,Object>>> queryCompetitionInfo(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

//    /**
//     * 获取赛事所有信息
//     */
//    @GetMapping("/competitionManager/selectAllCompetitionDetailInfo")
//    public R<List<CompetitionDetailInfo>> selectAllCompetitionDetailInfo(CompetitionMainInfoReq req, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/userCompetition/syncCompetitionApplyInfo")
    public R<Void> syncCompetitionApplyInfo(@RequestParam String updateSize,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/userCompetition/bindTeacherCompetitionUser")
    public R<Integer> bindTeacherCompetitionUser(@RequestParam Long userId, @RequestParam String phone,
                                                  @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/competitionOperationConfig/getCompetitionFee")
    public R<String> getCompetitionFee(@RequestParam String secondLevelCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 跨服务调用：查询赛证互通规则最小字段列表
     */
    @PostMapping("/competition/competitionCertExchangeRule/inner/list")
    public R<List<Map<String, Object>>> listCertExchangeRuleInner(@RequestBody(required = false) Map<String, Object> param, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 新增日志信息
     * @param changeLog
     * @param source
     * @return
     */
    @PostMapping("/log/innerAdd")
    public R<Void> insertChangeLog(@RequestBody ChangeLog changeLog, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 删除报名人员信息
    @GetMapping("/competitionApply/removeApplyInfo/{memberIds}")
    public R<Integer> deleteCompetitionApplyInfoByMemberId(@PathVariable String memberIds, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // /saveCompetitionApplyInfo
    @PostMapping("/competitionApply/saveBatchCompetitionApplyInfo")
    public R<Integer> saveBatchCompetitionApplyInfo(@RequestBody List<CompetitionApplyInfo> competitionApplyInfoList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    //删除团队信息
    @GetMapping("/competitionApply/removeTeam/{teamCode}")
    public R<Integer> removeTeam(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/competitionApply/selectCompetitionApplyTeamCode/{teamCode}")
    public R<List<CompetitionApplyInfo>> selectCompetitionApplyTeamCode(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
    /**
     * 校验退费重缴可操作剩余次数
     * @param source
     * @param competitionSeriesId 赛事id
     * @param teamCode 团队code
     * @return
     */
    @GetMapping("/times/checkRepaymentTimes/{competitionSeriesId}/{teamCode}")
    public R<Boolean> checkRepaymentTimes(@RequestHeader(SecurityConstants.FROM_SOURCE) String source, @PathVariable String competitionSeriesId, @PathVariable String teamCode);

    /**
     * 记录退费重缴使用次数
     * @param source
     * @param teamCode 团队code
     * @return
     */
    @GetMapping("/times/recordUsedTimes/{competitionSeriesId}/{teamCode}")
    public R<Boolean> recordUsedTimes(@RequestHeader(SecurityConstants.FROM_SOURCE) String source, @PathVariable String teamCode, @PathVariable String competitionSeriesId);

    /**
     * 记录退费重缴使用次数
     * @param source
     * @param teamCode 团队code
     * @return
     */
    @GetMapping("/times/cancelRepaymentOperationTimes/{teamCode}")
    public R<Integer> cancelRepaymentOperationTimes(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 更新用户赛证互通申请
     */
    @PostMapping("/user/competitionCertExchangeRule/updateUserCertExchangeApply")
    public R<Integer> updateUserCertExchangeApply(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     * 更新用户赛证互通申请发票状态
     */
    @PostMapping("/user/competitionCertExchangeRule/updateUserCertExchangeApplyInvoiceStatus")
    public R<Integer> updateUserCertExchangeApplyInvoiceStatus(@RequestBody List<CompetitionCertExchangeApply> competitionCertExchangeApplyList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
    /**
     * 查询团队的报名信息
     * @param userId 用户id
     * @param competitionId 赛事id
     * @param source
     * @return
     */
    @GetMapping("/competitionApply/getApplyInfoByUsrIdAndCompetitionId/{userId}/{competitionId}")
    public R<List<CompetitionApplyInfo>> getApplyInfoByUsrIdAndCompetitionId(@PathVariable Long userId,@PathVariable Long competitionId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据赛事系列id查询报名信息
     * @param competitionId
     * @param source
     * @return
     */
    @GetMapping("/competitionApply/getApplyInfoByUsrIdAndCompetitionId/{competitionId}")
    public R<List<CompetitionApplyInfo>> getCompetitionApplyInfoListByCompetitionSeriesId(@PathVariable Long competitionId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

}
