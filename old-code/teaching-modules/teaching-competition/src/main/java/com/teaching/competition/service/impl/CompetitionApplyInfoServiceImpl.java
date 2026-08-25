package com.teaching.competition.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.CandidateCertInfo;
import com.teaching.competition.domain.CertCompetitionApplyInfo;
import com.teaching.competition.domain.TeamManagerInfoRes;
import com.teaching.competition.domain.UserCompetitionApplyInfo;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.CompetitionApplyInfoCheckService;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.competition.service.UserCompetitionService;
import com.teaching.competition.util.StringNumberUtil;
import com.teaching.competition.util.UUIDUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 赛事申请报名信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class CompetitionApplyInfoServiceImpl implements ICompetitionApplyInfoService {
    private static final Logger log = LoggerFactory.getLogger(CompetitionApplyInfoServiceImpl.class);
    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private TeamMemberRelaMapper teamMemberRelaMapper;

    @Autowired
    private CompetitionWorksMapper competitionWorksMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TeamManagerInfoMapper teamManagerInfoMapper;

    @Autowired
    private CompetitionApplyInfoCheckService competitionApplyInfoCheckService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CompetitionStageConfigMapper competitionStageConfigMapper;

    @Autowired
    private UserCompetitionService userCompetitionService;

    @Autowired
    private ChangeLogMapper changeLogMapper;

    @Autowired
    private CandidateCertInfoMapper candidateCertInfoMapper;

    @Autowired
    private CompetitionPromotedApplyInfoMapper competitionPromotedApplyInfoMapper;

    /**
     * 查询赛事申请报名信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 赛事申请报名信息
     */
    @Override
    public CompetitionApplyInfo selectCompetitionApplyInfoByMemberId(Long memberId) {
        CompetitionApplyInfo competitionApplyInfo = competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(memberId);
        if (Objects.nonNull(competitionApplyInfo)) {
            // 实名认证状态
            if (competitionApplyInfo.getUserId() != null) {
                R<SysUser> userCenterInfo = userService.getUserCenterInfo(competitionApplyInfo.getUserId(), SecurityConstants.INNER);
                if (R.isSuccess(userCenterInfo) && null != userCenterInfo.getData()) {
                    competitionApplyInfo.setRealNameAuthStatus(userCenterInfo.getData().getAuthStatus());
                    competitionApplyInfo.setOrgName(userCenterInfo.getData().getOrg() == null ? null : userCenterInfo.getData().getOrg().getOrgName());
                }
            }
            // 团队成员
            if (StringUtils.isNotEmpty(competitionApplyInfo.getTeamCode())) {
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setTeamCode(competitionApplyInfo.getTeamCode());
                // 团队成员列表
                teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
                List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
                if (CollectionUtils.isNotEmpty(teamMemberRelaList)) {
                    teamMemberRelaList.stream().forEach(teamMember -> {
                        if (teamMember.getUserId() != null) {
                            R<SysUser> memberCenterInfo = userService.getUserCenterInfo(teamMember.getUserId(), SecurityConstants.INNER);
                            if (R.isSuccess(memberCenterInfo) && null != memberCenterInfo.getData()) {
                                teamMember.setUserName(memberCenterInfo.getData().getUserName());
                            }
                        }
                    });
                }
                competitionApplyInfo.setTeamMemberRelaList(teamMemberRelaList);
            }
            // 指导老师翻译出名称
            List<String> teacherNameList = new ArrayList<>();
            if (StringUtils.isNotEmpty(competitionApplyInfo.getGuideTeacher())) {
                List<String> teacherIdList = Arrays.asList(competitionApplyInfo.getGuideTeacher().split(","));
                teacherIdList.stream().forEach(teacherId -> {
                    // 判断字符是春数字还是其他组合
                    if (StringNumberUtil.isNumber(teacherId)) {
                        SysUser sysUser = userService.getUserCenterInfo(Long.valueOf(teacherId), SecurityConstants.INNER).getData();
                        if (null != sysUser) {
                            if (sysUser.getAuthInfo() != null) {
                                teacherNameList.add(sysUser.getAuthInfo().getRealName());
                            } else {
                                teacherNameList.add(sysUser.getUserName());
                            }
                        } else {
                            teacherNameList.add(teacherId);
                        }
                    } else {
                        teacherNameList.add(teacherId);
                    }
                });
            }
            competitionApplyInfo.setGuideTeacherName(String.join(",", teacherNameList));
        }
        return competitionApplyInfo;
    }

    /**
     * 查询赛事申请报名信息列表
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 赛事申请报名信息
     */
    @Override
//    @DataScope(orgAlias = "a", userAlias = "a")
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        if (StringUtils.isNotEmpty(competitionApplyInfo.getLeaderTeacherName())) {
            R<List<AuthInfo>> userCenterTeacherInfo = userService.selectAuthInfoByName(competitionApplyInfo.getLeaderTeacherName(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                List<AuthInfo> authInfoList = userCenterTeacherInfo.getData();
                if (CollectionUtils.isNotEmpty(authInfoList)) {
                    List<Long> teacherIdList = authInfoList.stream().map(AuthInfo::getUserId).collect(Collectors.toList());
                    competitionApplyInfo.setTeacherIdList(teacherIdList);
                } else {
                    competitionApplyInfo.setTeacherIdList(Arrays.asList(0l));
                }
            }
        }
        List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoList(competitionApplyInfo);
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            List<Long> userIdList = competitionApplyInfoList.stream().map(CompetitionApplyInfo::getLeaderTeacherId).distinct().collect(Collectors.toList());
            R<List<SysUser>> userCenterTeacherInfo = userService.getUserCenterInfoList(userIdList, SecurityConstants.INNER);
            Map<Long, List<SysUser>> userCenterTeacherInfoMap;
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                userCenterTeacherInfoMap = userCenterTeacherInfo.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
            } else {
                userCenterTeacherInfoMap = new HashMap<>();
            }
            for (CompetitionApplyInfo applyInfo : competitionApplyInfoList) {
                // 实名认证状态
//                R<SysUser> userCenterInfo = userService.getUserCenterInfo(applyInfo.getUserId(), SecurityConstants.INNER);
//                if (R.isSuccess(userCenterInfo) && null != userCenterInfo.getData()) {
//                    applyInfo.setRealNameAuthStatus(userCenterInfo.getData().getAuthStatus());
//                }
                List<SysUser> sysUsers = userCenterTeacherInfoMap.get(applyInfo.getLeaderTeacherId());
                if (CollectionUtils.isNotEmpty(sysUsers)) {
                    AuthInfo authInfo = sysUsers.get(0).getAuthInfo();
                    applyInfo.setLeaderTeacherName(authInfo == null ? null : authInfo.getRealName());
                }
                // 指导老师翻译出名称
                List<String> teacherNameList = new ArrayList<>();
                if (StringUtils.isNotEmpty(applyInfo.getGuideTeacher())) {
                    List<String> teacherIdList = Arrays.asList(applyInfo.getGuideTeacher().split(","));
                    teacherIdList.stream().forEach(teacherId -> {
                        // 判断字符是春数字还是其他组合
                        if (StringNumberUtil.isNumber(teacherId)) {
                            SysUser sysUser = userService.getUserCenterInfo(Long.valueOf(teacherId), SecurityConstants.INNER).getData();
                            if (null != sysUser) {
                                if (sysUser.getAuthInfo() != null) {
                                    teacherNameList.add(sysUser.getAuthInfo().getRealName());
                                } else {
                                    teacherNameList.add(sysUser.getUserName());
                                }
                            } else {
                                teacherNameList.add(teacherId);
                            }
                        } else {
                            teacherNameList.add(teacherId);
                        }
                    });
                }
                applyInfo.setGuideTeacherName(String.join(",", teacherNameList));
                // 获取支付时间，支付时间即为报名时间
                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(applyInfo.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
                    OrderInfo data = ordersResponse.getData();
                    if (Objects.nonNull(data.getPayTime())) {
                        applyInfo.setPayTime(data.getPayTime());
                        applyInfo.setRegistrationTime(data.getPayTime());
                    }
                }
                // 报名信息变更信息
                List<Map<String, Object>> oldDateList = new ArrayList<>();
                List<ChangeLog> changeLogList = changeLogMapper.selectChangeLogListByMemberId(applyInfo.getMemberId());
                if (CollectionUtils.isNotEmpty(changeLogList)) {
                    changeLogList.forEach(changeLog -> {
                        Map<String, Object> oldDataMap = new HashMap<>();
                        oldDataMap.put("changeType", changeLog.getChangeType());
                        oldDataMap.put("operatorUser", changeLog.getCreateBy());
                        oldDataMap.put("changeTime", DateUtils.getDateFormat(changeLog.getChangeTime()));
                        if(StringUtils.isNotEmpty(changeLog.getOldData())){
                            List<CompetitionApplyInfo> teamManagerInfoOldList = JSONUtil.toList(changeLog.getOldData(), CompetitionApplyInfo.class);
                            List<String> phoneList = teamManagerInfoOldList.stream()
                                    .filter(ca -> StringUtils.isNotEmpty(ca.getPhone()))
                                    .map(CompetitionApplyInfo::getPhone).toList();
                            List<String> emailList = teamManagerInfoOldList.stream()
                                    .filter(ca -> StringUtils.isNotEmpty(ca.getEmail()))
                                    .map(CompetitionApplyInfo::getEmail).toList();
                            String phone = "";
                            if(CollectionUtils.isNotEmpty(phoneList))
                                phone = String.join(",", phoneList);
                            oldDataMap.put("phoneOld", phone);
                            String email = "";
                            if(CollectionUtils.isNotEmpty(emailList))
                                email = String.join(",", emailList);
                            oldDataMap.put("emailOld", email);
                        }
                        if(StringUtils.isNotEmpty(changeLog.getNewData())){
                            List<CompetitionApplyInfo> teamManagerInfoNewList = JSONUtil.toList(changeLog.getNewData(), CompetitionApplyInfo.class);
                            List<String> phoneList = teamManagerInfoNewList.stream()
                                    .filter(ca -> StringUtils.isNotEmpty(ca.getPhone()))
                                    .map(CompetitionApplyInfo::getPhone).toList();
                            List<String> emailList = teamManagerInfoNewList.stream()
                                    .filter(ca -> StringUtils.isNotEmpty(ca.getEmail()))
                                    .map(CompetitionApplyInfo::getEmail).toList();
                            String phone = "";
                            if(CollectionUtils.isNotEmpty(phoneList))
                                phone = String.join(",", phoneList);
                            oldDataMap.put("phoneNew", phone);
                            String email = "";
                            if(CollectionUtils.isNotEmpty(emailList))
                                email = String.join(",", emailList);
                            oldDataMap.put("emailNew", email);
                        }
                        oldDateList.add(oldDataMap);
                    });
                }
                applyInfo.setTeamMemberOldDateList(oldDateList);
            }
        }
        return competitionApplyInfoList;
    }

    @Override
    public List<CertCompetitionApplyInfo> selectCertCompetitionApplyInfoList(CertCompetitionApplyInfo certCompetitionApplyInfo) {
        // 获取带队老师名称
        if (StringUtils.isNotEmpty(certCompetitionApplyInfo.getLeaderTeacherName())) {
            R<List<AuthInfo>> userCenterTeacherInfo = userService.selectAuthInfoByName(certCompetitionApplyInfo.getLeaderTeacherName(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                List<AuthInfo> authInfoList = userCenterTeacherInfo.getData();
                if (CollectionUtils.isNotEmpty(authInfoList)) {
                    List<Long> teacherIdList = authInfoList.stream().map(AuthInfo::getUserId).collect(Collectors.toList());
                    certCompetitionApplyInfo.setTeacherIdList(teacherIdList);
                } else {
                    certCompetitionApplyInfo.setTeacherIdList(Arrays.asList(0l));
                }
            }
        }
        List<CertCompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoMapper.selectCertCompetitionApplyInfoList2(certCompetitionApplyInfo);
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            List<Long> userIdList = competitionApplyInfoList.stream().map(CertCompetitionApplyInfo::getLeaderTeacherId).distinct().collect(Collectors.toList());
            R<List<SysUser>> userCenterTeacherInfo = userService.getUserCenterInfoList(userIdList, SecurityConstants.INNER);
            Map<Long, List<SysUser>> userCenterTeacherInfoMap;
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                userCenterTeacherInfoMap = userCenterTeacherInfo.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
            } else {
                userCenterTeacherInfoMap = new HashMap<>();
            }
            // 判断那个人被候选人选中
//            CandidateCertInfo candidateCertInfo = new CandidateCertInfo();
//            String memberIds = competitionApplyInfoList.stream()
//                    .map(CompetitionApplyInfo::getMemberId)
//                    .map(String::valueOf)
//                    .collect(Collectors.joining(","));
//            candidateCertInfo.setMemberIds(memberIds);
//            candidateCertInfo.setCertConfigId(certCompetitionApplyInfo.getCertConfigId());
//            List<CandidateCertInfo> candidateCertInfos = candidateCertInfoMapper.selectCandidateCertInfoList(candidateCertInfo);
            for (CertCompetitionApplyInfo applyInfo : competitionApplyInfoList) {
                // 实名认证状态
                List<SysUser> sysUsers = userCenterTeacherInfoMap.get(applyInfo.getLeaderTeacherId());
                if (CollectionUtils.isNotEmpty(sysUsers)) {
                    AuthInfo authInfo = sysUsers.get(0).getAuthInfo();
                    applyInfo.setLeaderTeacherName(authInfo == null ? null : authInfo.getRealName());
                }
//                if(CollectionUtils.isNotEmpty(candidateCertInfos)){
//                    List<Long> candidateIds = candidateCertInfos.stream().map(CandidateCertInfo::getMemberId).toList();
//                    if(candidateIds.contains(applyInfo.getMemberId())){
//                        applyInfo.setSelect(true);
//                    } else {
//                        applyInfo.setSelect(false);
//                    }
//                }
            }
//            competitionApplyInfoList = competitionApplyInfoList.stream()
//                    .sorted(Comparator.comparing(CertCompetitionApplyInfo::getSelect, Comparator.reverseOrder()))
//                    .collect(Collectors.toList());
        }
        return competitionApplyInfoList;
    }

    @Override
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoByPayStatus() {
        return competitionApplyInfoMapper.selectCompetitionApplyInfoByPayStatus();
    }

    @Override
    public List<CompetitionApplyInfo> queryTeamMemberInvoiceStatus(CompetitionApplyInfo competitionApplyInfo) {
//        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoMapper.queryTeamMemberInvoiceStatus(competitionApplyInfo);
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            List<Long> userIdList = competitionApplyInfoList.stream()
                    .map(CompetitionApplyInfo::getLeaderTeacherId)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            Map<Long, List<SysUser>> userCenterInfoMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(userIdList)) {
                R<List<SysUser>> userCenterInfo = userService.getUserCenterInfoList(userIdList, SecurityConstants.INNER);
                if (R.isSuccess(userCenterInfo) && null != userCenterInfo.getData()) {
                    userCenterInfoMap.putAll(userCenterInfo.getData().stream()
                            .collect(Collectors.groupingBy(SysUser::getUserId)));
                }
            }
            // 获取带队老师名称
            competitionApplyInfoList.stream().forEach(applyInfo -> {
                List<SysUser> teacherUserList = userCenterInfoMap.get(applyInfo.getLeaderTeacherId());
                if (CollectionUtils.isNotEmpty(teacherUserList)) {
                    AuthInfo authInfo = teacherUserList.get(0).getAuthInfo();
                    SysUser teacherUser = teacherUserList.get(0);
                    applyInfo.setLeaderTeacherName(authInfo == null
                            ? (StringUtils.isNotBlank(teacherUser.getNickName())
                            ? teacherUser.getNickName() : teacherUser.getUserName())
                            : authInfo.getRealName());
                }
            });
        }
        return competitionApplyInfoList;
    }

    @Override
    public List<CompetitionApplyInfo> exportCompetitionApplyInfoList(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        getLeaderTeacherInfo(competitionApplyInfo);
        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectExportCompetitionApplyInfoList(competitionApplyInfo);
        getCompetitionApplyInfoOthers(applyInfoList);
        return applyInfoList;
    }

    @Override
    public List<CompetitionApplyInfo> exportCompetitionApplyInfoAwardsList(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        getLeaderTeacherInfo(competitionApplyInfo);
        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectExportCompetitionApplyInfoAwardsList(competitionApplyInfo);
        getCompetitionApplyInfoOthers(applyInfoList);
        return applyInfoList;
    }

    private void getLeaderTeacherInfo(CompetitionApplyInfo competitionApplyInfo) {
        if (StringUtils.isNotEmpty(competitionApplyInfo.getLeaderTeacherName())) {
            R<List<AuthInfo>> userCenterTeacherInfo = userService.selectAuthInfoByName(competitionApplyInfo.getLeaderTeacherName(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                List<AuthInfo> authInfoList = userCenterTeacherInfo.getData();
                if (CollectionUtils.isNotEmpty(authInfoList)) {
                    List<Long> teacherIdList = authInfoList.stream().map(AuthInfo::getUserId).collect(Collectors.toList());
                    competitionApplyInfo.setTeacherIdList(teacherIdList);
                } else {
                    competitionApplyInfo.setTeacherIdList(Arrays.asList(0l));
                }
            }
        }
    }

    @Nullable
    private List<CompetitionApplyInfo> getCompetitionApplyInfoOthers(List<CompetitionApplyInfo> applyInfoList) {
        if (CollectionUtils.isNotEmpty(applyInfoList)) {
            applyInfoList.stream().forEach(applyInfo -> {
                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(applyInfo.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
                    OrderInfo data = ordersResponse.getData();
                    if (Objects.nonNull(data.getPayTime())) {
                        applyInfo.setPayTime(data.getPayTime());
                    } else {
                        log.warn("团队编码 {} 的订单支付时间为空，订单ID: {}", applyInfo.getTeamCode(), data.getOrderId());
                    }
                } else {
                    log.warn("根据团队编码 {} 查询订单失败或订单不存在，响应: {}", applyInfo.getTeamCode(), ordersResponse);
                }
                // 根据school作为id查询省份
                if (StringUtils.isNotEmpty(applyInfo.getSchool())) {
                    R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(applyInfo.getSchool(), SecurityConstants.INNER);
                    if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                        NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                        if (StringUtils.isNotEmpty(collegeInfo.getProvince())) {
                            applyInfo.setProvinceName(collegeInfo.getProvince());
                        }
//                        applyInfo.setSchoolName(collegeInfo.getSchoolName());
                    } else {
                        log.warn("根据学校ID {} 查询院校信息失败或不存在，响应: {}", applyInfo.getSchool(), collegeInfoResponse);
                    }
                }
            });
            // 排序
//            applyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getPayTime, Comparator.nullsLast(Date::compareTo)).reversed());
            applyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getTeamCode)
                    .thenComparing(CompetitionApplyInfo::getPayTime, Comparator.nullsLast(Comparator.<Date>naturalOrder().reversed())));
        }
        return applyInfoList;
    }

    @Override
    public List<TeamManagerInfoRes> exportTeamManagerInfoList(CompetitionApplyInfo competitionApplyInfo) {
        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        competitionApplyInfo.setPayStatus(DictConstant.PAID);
        List<TeamManagerInfoRes> teamManagerInfoList = competitionApplyInfoMapper.selectTeamManagerInfoListForExport(competitionApplyInfo);
        if (CollectionUtils.isEmpty(teamManagerInfoList)) {
            return new ArrayList<>();
        }
        return teamManagerInfoList;
    }

    /**
     * 个人赛事报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    @Override
    public int insertCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo) {
        competitionApplyInfo.setCreateTime(DateUtils.getNowDate());
        return competitionApplyInfoMapper.insertCompetitionApplyInfo(competitionApplyInfo);
    }

    @Override
    public int batchInsertCompetitionApplyInfo(List<CompetitionApplyInfo> competitionApplyInfoList) {
        // 团队队员信息表添加队员
        if(CollectionUtils.isNotEmpty(competitionApplyInfoList)){
            competitionApplyInfoList.stream().forEach(applyInfo -> {
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setUserId(applyInfo.getUserId());
                teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                teamMemberRela.setTeamRole(applyInfo.getCompetitionRoleName());
                teamMemberRela.setUserName(applyInfo.getUserName());
                teamMemberRela.setInstructor(applyInfo.getGuideTeacher());
                teamMemberRela.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                teamMemberRela.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
            });
        }
        return competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(competitionApplyInfoList);
    }

    @Override
    public int batchInsertAwardsCompetitionApplyInfo(List<CompetitionApplyInfo> competitionApplyInfoList,
                                                     String nickName,Long competitionSeriesId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Map<String, List<CompetitionApplyInfo>> competitionApplyInfoMap = competitionApplyInfoList.stream().
                collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
        List<TeamManagerInfo> teamManagerInfoList = new ArrayList<>();
        List<TeamMemberRela> TeamMemberInfoList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // 新老teamCode对应关系
        Map<String, String> teamCodeMap = new HashMap<>();
        if (MapUtils.isNotEmpty(competitionApplyInfoMap)) {
            competitionApplyInfoMap.forEach((teamCode, applyInfoList) -> {
                String newTeamCode = currentYear+"_"+ IdUtil.getSnowflakeNextId();
                teamCodeMap.put(teamCode, newTeamCode);
                TeamManagerInfo teamManagerInfo = getTeamManagerInfo(newTeamCode, applyInfoList, userId);
                teamManagerInfoList.add(teamManagerInfo);
                applyInfoList.stream().forEach(applyInfo -> {
                    TeamMemberRela teamMemberInfo = new TeamMemberRela();
                    teamMemberInfo.setTeamCode(newTeamCode);
                    teamMemberInfo.setUserId(applyInfo.getUserId());
                    teamMemberInfo.setTeamRole(applyInfo.getCompetitionRoleName());
                    teamMemberInfo.setInstructor(applyInfo.getGuideTeacher());
                    teamMemberInfo.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                    teamMemberInfo.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                    teamMemberInfo.setUserName(applyInfo.getUserName());
                    TeamMemberInfoList.add(teamMemberInfo);
                    // 更换新的teamCode
                    applyInfo.setTeamCode(newTeamCode);
                });
            });
        }
        if(MapUtils.isNotEmpty(teamCodeMap)){
            for(Map.Entry<String, String> entry : teamCodeMap.entrySet()){
                //修改晋级表报名状态为已报名,并将旧团队编号替换新的
                competitionPromotedApplyInfoMapper.updateApplyStatusByCompetitionAndTeamCode
                        (competitionSeriesId, entry.getKey(),entry.getValue(), "1", nickName);
            }
        }
        teamManagerInfoMapper.batchInsertTeamManagerInfo(teamManagerInfoList);
        teamMemberRelaMapper.batchInsertTeamMemberRela(TeamMemberInfoList);
        return competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(competitionApplyInfoList);
    }

    private static TeamManagerInfo getTeamManagerInfo(String teamCode, List<CompetitionApplyInfo> applyInfoList, Long userId) {
        TeamManagerInfo teamManagerInfo = new TeamManagerInfo();
        teamManagerInfo.setTeamCode(teamCode);
        teamManagerInfo.setLeaderTeacher(userId + "");
        teamManagerInfo.setCompetitionTrackId(applyInfoList.get(0).getCompetitionTrackId());
        teamManagerInfo.setCompetitionTrackName(applyInfoList.get(0).getCompetitionTrackName());
        teamManagerInfo.setCompetitionSeriesId(applyInfoList.get(0).getCompetitionSeriesId());
        teamManagerInfo.setCompetitionSeriesName(applyInfoList.get(0).getCompetitionName());
        teamManagerInfo.setSecondLevelCode(applyInfoList.get(0).getSecondLevelCode());
        teamManagerInfo.setSecondLevelName(applyInfoList.get(0).getSecondLevelName());
        teamManagerInfo.setTeamName(applyInfoList.get(0).getTeamName());
        teamManagerInfo.setCheckStatus(Constants.CHECK_PASS);
        // 获取队长信息
        applyInfoList.stream().forEach(applyInfo -> {
            if ("队长".equals(applyInfo.getCompetitionRoleName())) {
                teamManagerInfo.setCaptainName(applyInfo.getUserName());
                teamManagerInfo.setTeamLeaderId(applyInfo.getUserId());
            }
        });
        return teamManagerInfo;
    }

    /**
     * 修改赛事申请报名信息
     *
     * @param competitionApplyInfo 赛事申请报名信息
     * @return 结果
     */
    @Override
    public int updateCompetitionApplyInfo(CompetitionApplyInfo competitionApplyInfo) {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        competitionApplyInfo.setUpdateTime(DateUtils.getNowDate());
        competitionApplyInfo.setUpdateBy(sysUserInfo.getUserId() + "");
        return competitionApplyInfoMapper.updateCompetitionApplyInfo(competitionApplyInfo);
    }

    @Override
    public int updatePayStatus(List<CompetitionApplyInfo> competitionApplyInfoList) {
        return competitionApplyInfoMapper.updatePayStatus(competitionApplyInfoList);
    }

    @Override
    public int updateCompetitionApplyInfoStatus(CompetitionApplyInfo competitionApplyInfo) {
        competitionApplyInfo.setUpdateTime(DateUtils.getNowDate());
        if (Constants.CHECK_PASS.equals(competitionApplyInfo.getCheckStatus())) {
            CompetitionApplyInfo competitionApplyInfoUser =
                    competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(competitionApplyInfo.getMemberId());
            // 当前角色是否实名认证
            R<SysUser> userCenterInfo = userService.getUserCenterInfo(competitionApplyInfoUser.getUserId(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData()) &&
                    Objects.nonNull(userCenterInfo.getData().getAuthInfo())) {
                AuthInfo authInfo = userCenterInfo.getData().getAuthInfo();
                if (authInfo.getAuthStatus().equals(Constants.AUTH_STATUS_PASS)) {
                    UserAuthorization authAuthorization = new UserAuthorization();
                    authAuthorization.setUserId(competitionApplyInfoUser.getUserId());
                    authAuthorization.setAuthFlag(true);
                    userService.saveUserAuthorization(authAuthorization, SecurityConstants.INNER);
                }
            }
            // 单人参赛或者团员参赛则将比赛用户角色付给用户
            //比赛用户(个人或者队长)角色赋权
            UserAuthorization userAuthorization = new UserAuthorization();
            userAuthorization.setUserId(competitionApplyInfoUser.getUserId());
            userAuthorization.setCompetitionFlag(true);
            userService.saveUserAuthorization(userAuthorization, SecurityConstants.INNER);
            if (StringUtils.isNotEmpty(competitionApplyInfoUser.getTeamCode())) {
                UserAuthorization leaderAuthorization = new UserAuthorization();
                // 队长分配比赛队长角色
                leaderAuthorization.setUserId(competitionApplyInfoUser.getUserId());
                leaderAuthorization.setCaptainFlag(true);
                userService.saveUserAuthorization(userAuthorization, SecurityConstants.INNER);
                // 团员赋予比赛用户角色
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setTeamCode(competitionApplyInfoUser.getTeamCode());
                teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
                List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
                if (CollectionUtils.isNotEmpty(teamMemberRelaList)) {
                    teamMemberRelaList.stream().filter(teamMemberUser -> teamMemberUser.getUserId().equals(competitionApplyInfoUser.getMemberId())).
                            forEach(teamMemberUser -> {
                                // 团员是否实名认证
                                R<SysUser> memberCenterInfo = userService.getUserCenterInfo(competitionApplyInfoUser.getUserId(), SecurityConstants.INNER);
                                if (R.isSuccess(memberCenterInfo) && Objects.nonNull(memberCenterInfo.getData()) &&
                                        Objects.nonNull(memberCenterInfo.getData().getAuthInfo())) {
                                    AuthInfo authInfo = memberCenterInfo.getData().getAuthInfo();
                                    if (authInfo.getAuthStatus().equals(Constants.AUTH_STATUS_PASS)) {
                                        UserAuthorization authAuthorization = new UserAuthorization();
                                        authAuthorization.setUserId(competitionApplyInfoUser.getUserId());
                                        authAuthorization.setAuthFlag(true);
                                        userService.saveUserAuthorization(authAuthorization, SecurityConstants.INNER);
                                    }
                                }
                                UserAuthorization memberAuthorization = new UserAuthorization();
                                memberAuthorization.setUserId(teamMemberUser.getUserId());
                                memberAuthorization.setCompetitionFlag(true);
                                userService.saveUserAuthorization(memberAuthorization, SecurityConstants.INNER);
                            });
                }
            }
        }
        // 团队比赛则将比赛用户及团队队长角色付给团队队长
        return competitionApplyInfoMapper.updateCompetitionApplyInfo(competitionApplyInfo);
    }

    /**
     * 批量删除赛事申请报名信息
     *
     * @param memberIds 需要删除的赛事申请报名信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionApplyInfoByMemberIds(Long[] memberIds) {
        // 删除团队队员信息表
        if (CollectionUtils.isNotEmpty(Arrays.asList(memberIds))) {
            Arrays.stream(memberIds).distinct().forEach(memberId -> {
                CompetitionApplyInfo applyInfo = competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(memberId);
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setUserId(applyInfo.getUserId());
                teamMemberRela.setUserName(applyInfo.getUserName());
                teamMemberRela.setDelFlag("1");
                teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
            });
        }
        return competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberIds(memberIds);
    }

    // 退赛删除团队专用
    @Transactional
    @Override
    public int deleteCompetitionApplyInfoByTeamCode(String teamCode) {
        teamMemberRelaMapper.deleteRetaTeamMemberRelaByTeamCode(teamCode);
        teamManagerInfoMapper.deleteRetaTeamManagerInfoByTeamCode(teamCode);
        return competitionApplyInfoMapper.deleteRetaCompetitionApplyInfoByTeamCode(teamCode);
    }

    /**
     * 删除赛事申请报名信息信息
     *
     * @param memberId 赛事申请报名信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionApplyInfoByMemberId(Long memberId) {
        return competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(memberId);
    }

    @Override
    public List<UserCompetitionApplyInfo> selectCompetitionApplyInfoByUserId(Long userId) {
        // 获取用户报名赛事信息
        List<UserCompetitionApplyInfo> userCompetitionApplyInfos = competitionApplyInfoMapper.selectCompetitionApplyByUserId(userId);
//        if (CollectionUtils.isNotEmpty(userCompetitionApplyInfos)) {
//            userCompetitionApplyInfos.forEach(userCompetitionApplyInfo -> {
//                // 获取当前赛事处于那个阶段
//                List<CompetitionStageConfig> competitionStageConfigs = competitionStageConfigMapper.selectCompetitionStageConfigList(userCompetitionApplyInfo.getCompetitionSeriesId());
//                competitionStageConfigs.forEach(competitionStageConfig -> {
//                    if (competitionStageConfig.getStageStartTime().getTime() <= System.currentTimeMillis()
//                            && System.currentTimeMillis() <= competitionStageConfig.getStageEndTime().getTime()) {
//                        userCompetitionApplyInfo.setStageName(competitionStageConfig.getStageName());
//                        userCompetitionApplyInfo.setStageId(competitionStageConfig.getStageId());
//                    }
//                });
//                // 当前赛事报名状态
//                UserApplyCompetitionReq userApplyCompetitionReq = new UserApplyCompetitionReq();
//                userApplyCompetitionReq.setCompetitionSeriesId(userCompetitionApplyInfo.getCompetitionSeriesId());
//                CompetitionApplyAllStatus competitionApplyAllStatus =
//                        userCompetitionService.checkCompetitionApplyStatusByUser(userApplyCompetitionReq);
//                userCompetitionApplyInfo.setApplyStatus(competitionApplyAllStatus.getApplyStatus());
//                userCompetitionApplyInfo.setApplyReason(competitionApplyAllStatus.getApplyReason());
//                userCompetitionApplyInfo.setAmount(competitionApplyAllStatus.getAmount());
//                userCompetitionApplyInfo.setFlag(competitionApplyAllStatus.getFlag());
//                // 如果团队赛获取团队信息,个人或者人数不限跳过
//                userCompetitionApplyInfo.setUploadFlag(true);
//                if (userCompetitionApplyInfo.getJoinType().equals(Constants.JOIN_TYPE_TEAM)) {
//                    TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.
//                            selectTeamMemberRelaByTeamLeaderId(userId, userCompetitionApplyInfo.getCompetitionSeriesId());
//                    // 团队赛只能队长上传作品，不是队长不能上传作品
//                    if (teamManagerInfo != null) {
//                        userCompetitionApplyInfo.setTeamCode(teamManagerInfo.getTeamCode());
//                        userCompetitionApplyInfo.setTeamName(teamManagerInfo.getTeamName());
//                    } else {
//                        userCompetitionApplyInfo.setUploadFlag(false);
//                    }
//                }
//                CompetitionWorks competitionWorks = new CompetitionWorks();
//                competitionWorks.setUserId(userId);
//                competitionWorks.setCompetitionSeriesId(userCompetitionApplyInfo.getCompetitionSeriesId());
//                List<CompetitionWorks> competitionWorkList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
//                if (CollectionUtils.isNotEmpty(competitionWorkList)) {
//                    userCompetitionApplyInfo.setWorksFlag("1");
//                    competitionWorkList.forEach(competitionWork -> {
//                        userCompetitionApplyInfo.setWorksStatus(competitionWork.getWorksStatus());
//                    });
//                } else {
//                    userCompetitionApplyInfo.setWorksFlag("0");
//                }
//                // 赛事收藏数量
//                Integer competitionCount = userCollectMapper.selectCollectCompetitionCount(userCompetitionApplyInfo.getCompetitionId());
//                userCompetitionApplyInfo.setCompetitionCollectNum(
//                        competitionCount == null ? 0 : competitionCount);
//                // 赛事分享数量
//                Integer shareNum = redisService.getCacheObject(userCompetitionApplyInfo.getCompetitionId() + "");
//                userCompetitionApplyInfo.setCompetitionShareNum(shareNum == null ? 0 : shareNum);
//                // 赛事上传作品是否已截至
//                userCompetitionApplyInfo.setWorksSubmitFlag(false);
//                if (userCompetitionApplyInfo.getWorksSubmitDate() != null && userCompetitionApplyInfo.getWorksSubmitDate().getTime() < System.currentTimeMillis()) {
//                    userCompetitionApplyInfo.setWorksSubmitFlag(true);
//                }
//            });
//        }
        return userCompetitionApplyInfos;
    }

    /**
     * 获取团队赛事信息
     *
     * @param registrationInfo
     * @return
     */
    @Override
    public List<CompetitionApplyInfoVO> getTeamCompetitionInfo(RegistrationInfo registrationInfo) {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        registrationInfo.setLeaderTeacherId(String.valueOf(sysUserInfo.getUserId()));
        List<CompetitionApplyInfoVO> list = competitionApplyInfoMapper.selectTeamCompetitionInfo(registrationInfo);
        if (list == null) {
            return Collections.emptyList();
        }
        String keyWord = registrationInfo.getKeyWord();
        if (StringUtils.isNotBlank(keyWord)) {
            list = list.stream()
                    .filter(item -> item.getRegistrationInfoList().stream()
                            .anyMatch(registration ->
                                    (registration.getIdCard() != null && registration.getIdCard().contains(keyWord.trim())) ||
                                            (registration.getUserName() != null && registration.getUserName().contains(keyWord.trim()))))
                    .toList();
        }

        if (!list.isEmpty()) {
            setSubTotal(list);
        }
        return list;
    }


    /**
     * 删除团队信息
     *
     * @param teamCode
     * @return
     */
    @Override
    public int delApplyInfoByTeamCode(String teamCode) {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        R<Integer> countByTeamCodes = orderService.getCountByTeamCodes(new String[]{teamCode}, SecurityConstants.INNER);
        if (countByTeamCodes.getCode() == HttpStatus.SUCCESS && countByTeamCodes.getData() > 0) {
            throw new ServiceException("该团队已生成订单，不允许删除");
        }
        return competitionApplyInfoMapper.delApplyInfoByTeamCode(teamCode, sysUserInfo.getUserId());
    }

    /**
     * 删除团队s信息
     *
     * @param teamCode
     * @return
     */
    @Override
    public int delApplyInfoByTeamCodes(String[] teamCode) {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        R<Integer> countByTeamCodes = orderService.getCountByTeamCodes(teamCode, SecurityConstants.INNER);
        if (countByTeamCodes.getCode() == HttpStatus.SUCCESS && countByTeamCodes.getData() > 0) {
            throw new ServiceException("该团队已生成订单，不允许删除");
        }
        return competitionApplyInfoMapper.delApplyInfoByTeamCodes(teamCode, sysUserInfo.getUserId());
    }

    /**
     * 去结算 生成令牌
     *
     * @param teamCodeList
     * @return
     */
    @Override
    public String settlement(List<String> teamCodeList, Long competitionSeriesId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Map<String, Object> map = new HashMap<>();
        String token = UUIDUtils.getUUID();
        map.put("token", token);
        map.put("teamCodeList", teamCodeList);
        String key = "settlement:" + competitionSeriesId + ":" + userId;
        redisService.deleteObject(key);
        redisService.setCacheObject(key, map, 10L, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 确认订单
     *
     * @return
     */
    @Override
    public Map<String, Object> confirmOrder(String token, Long competitionSeriesId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        //验证令牌
        String key = "settlement:" + competitionSeriesId + ":" + userId;
        Object cacheObject = redisService.getCacheObject(key);
        if (ObjectUtil.isNull(cacheObject)) {
            log.info("结算信息已失效，请重新选择1");
            throw new ServiceException("结算信息已失效，请重新选择", HttpStatus.CONFIRMORDER);
        }
        //验证 token teamCode
        Map<String, Object> map = (Map<String, Object>) cacheObject;
        String string = MapUtils.getString(map, "token");
        if (!token.equals(string)) {
            log.info("结算信息有误，请重新选择2");
            throw new ServiceException("结算信息有误，请重新选择", HttpStatus.CONFIRMORDER);
        }
        List<String> teamCodeList = (List<String>) MapUtils.getObject(map, "teamCodeList");
        List<CompetitionApplyInfoVO> competitionApplyInfoVOS = null;
        try {
            competitionApplyInfoVOS = competitionApplyInfoMapper.selectTeamCompetitionInfoByTeamCodes(competitionSeriesId, teamCodeList, userId);
        } catch (Exception e) {
            log.info("导入数据");
            throw new ServiceException("报名信息有误，请检查");
        }
        if (CollectionUtils.isNotEmpty(teamCodeList)) {
            if (CollectionUtils.isEmpty(competitionApplyInfoVOS) || competitionApplyInfoVOS.size() != teamCodeList.size()) {
                log.info("结算信息有误，请重新选择3");
                throw new ServiceException("结算信息有误，请重新选择", HttpStatus.CONFIRMORDER);
            }
        }
        setSubTotal(competitionApplyInfoVOS);
        result.put("detail", getDetail(competitionApplyInfoVOS));
        result.put("list", competitionApplyInfoVOS);
        result.put("totalFee", competitionApplyInfoVOS.stream().map(CompetitionApplyInfoVO::getSubtotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("commodityName", competitionApplyInfoVOS.get(0).getCompetitionName());
        return result;
    }

    /**
     * 给生成订单使用 给出总金额及团队codes
     *
     * @return
     */
    @Override
    public Map<String, Object> getTeamInfo(Long competitionSeriesId) {
        Map<String, Object> result = new HashMap<>();
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String key = "settlement:" + competitionSeriesId + ":" + userId;
        Object cacheObject = redisService.getCacheObject(key);
        if (ObjectUtil.isNull(cacheObject)) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) cacheObject;
        List<String> teamCodeList = (List<String>) MapUtils.getObject(map, "teamCodeList");
        List<CompetitionApplyInfoVO> competitionApplyInfoVOS = competitionApplyInfoMapper.selectTeamCompetitionInfoByTeamCodesForOrder(competitionSeriesId, teamCodeList, userId);
        setSubTotal(competitionApplyInfoVOS);
        //修改支付状态为待支付
//        Map<String, Object> temp = new HashMap<>();
//        temp.put("competitionSeriesId", competitionSeriesId);
//        temp.put("leaderTeacherId", userId);
//        temp.put("payStatus", DictConstant.PENDING);
//        temp.put("teamCodeList", teamCodeList);
//        competitionApplyInfoMapper.updateTeamPayStatusByTeamCodes(temp);
        result.put("teamCodeList", teamCodeList);
        result.put("totalFee", competitionApplyInfoVOS.stream().map(CompetitionApplyInfoVO::getSubtotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }

    /**
     * 更新支付状态
     *
     * @param map
     * @return
     */
    @Override
    public int updateTeamPayStatusByTeamCodes(Map<String, Object> map) {
        log.info("更新支付状态入参:" + JSONObject.toJSONString(map));
        String payStatus = MapUtils.getString(map, "payStatus");
        List<String> teamCodeList = (List<String>) MapUtils.getObject(map, "teamCodeList");
        if (StringUtils.isBlank(payStatus) || CollectionUtils.isEmpty(teamCodeList)) {
            throw new ServiceException("参数不合法");
        }
        /*Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        map.put("leaderTeacherId", userId);*/
        // 如果团队中都为支付状态为paid，则不更新pending
        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectTeamApplyInfoList(teamCodeList);
        if(CollectionUtils.isNotEmpty(applyInfoList)){
            Map<String,List<CompetitionApplyInfo>> teamMap = applyInfoList.stream()
                    .collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
            teamMap.forEach((teamCode,applyInfoListReq)->{
                List<String> payStatusReq = applyInfoListReq.stream()
                        .filter(ps -> StringUtils.isNotEmpty(ps.getPayStatus()))
                        .map(CompetitionApplyInfo::getPayStatus).distinct().toList();
//                if(!payStatusReq.stream().anyMatch(ps -> ps.equals(DictConstant.PAID))){
//                    teamCodeList.remove(teamCode);
//                }
                // 只能pending到paid,不能反向,团队中存在paid，则不更新pending
                if (CollectionUtils.isNotEmpty(payStatusReq)) {
                    if(payStatusReq.contains(DictConstant.PAID) && payStatus.equals(DictConstant.PENDING)){
                        teamCodeList.remove(teamCode);
                    }
                }
            });
        }
        if(CollectionUtils.isNotEmpty(teamCodeList)){
            return competitionApplyInfoMapper.updateTeamPayStatusByTeamCodes(map);
        }
        return 0;
//        return competitionApplyInfoMapper.updateTeamPayStatusByTeamCodes(map);
    }

    @Override
    public List<Map<String, Object>> selectCompetitionApplyInfoListByTeamCode(Map<String, Object> params) {
        List<Map<String, Object>> competitionApplyInfoResList = new ArrayList<>();
        List<CompetitionApplyInfo> competitionApplyInfoList =
                competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(params);
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            List<String> competitionNameList = competitionApplyInfoList.stream().map(CompetitionApplyInfo::getCompetitionName).distinct().toList();
            if (CollectionUtils.isNotEmpty(competitionNameList)) {
                StringBuffer sb = new StringBuffer();
                Map<String, Object> competitionNameMap = new HashMap<>();
                competitionNameMap.put("label", "赛事名称");
                competitionNameList.stream().forEach(competitionName -> sb.append(competitionName).append(","));
                competitionNameMap.put("value", sb.substring(0, sb.length() - 1));
                competitionApplyInfoResList.add(competitionNameMap);
            }
            List<String> trackNamelist = competitionApplyInfoList.stream().map(CompetitionApplyInfo::getCompetitionTrackName).distinct().toList();
            List<String> secondLevelNameList = competitionApplyInfoList.stream().map(CompetitionApplyInfo::getSecondLevelName).distinct().toList();
            List<String> competitionTrackNamelist = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(trackNamelist)) {
                competitionTrackNamelist.addAll(trackNamelist);
            }
            if (CollectionUtils.isNotEmpty(secondLevelNameList)) {
                competitionTrackNamelist.addAll(secondLevelNameList);
            }
            if (CollectionUtils.isNotEmpty(competitionTrackNamelist)) {
                StringBuffer sb = new StringBuffer();
                competitionTrackNamelist.stream().forEach(competitionTrackName -> sb.append(competitionTrackName).append(","));
                Map<String, Object> competitionTrackNameMap = new HashMap<>();
                competitionTrackNameMap.put("label", "赛道赛组信息");
                competitionTrackNameMap.put("value", sb.substring(0, sb.length() - 1));
                competitionApplyInfoResList.add(competitionTrackNameMap);
            }
            Map<String, Object> schoolNameMap = new HashMap<>();
            List<String> schoolNameList = competitionApplyInfoList.stream().map(CompetitionApplyInfo::getSchoolName).distinct().toList();
            if (CollectionUtils.isNotEmpty(schoolNameList)) {
                StringBuffer sb = new StringBuffer();
                schoolNameList.stream().forEach(schoolName -> sb.append(schoolName).append(","));
                schoolNameMap.put("label", "学校名称");
                schoolNameMap.put("value", sb.substring(0, sb.length() - 1));
                competitionApplyInfoResList.add(schoolNameMap);
            }
            Map<String, Object> guidTeacherMap = new HashMap<>();
            List<String> guidTeacherlist = competitionApplyInfoList.stream().filter(competitionApplyInfo -> StringUtils.isNotEmpty(competitionApplyInfo.getGuideTeacher())).
                    map(CompetitionApplyInfo::getGuideTeacher).distinct().toList();
            if (StringUtils.isNotEmpty(guidTeacherlist)) {
                guidTeacherMap.put("label", "指导老师");
                StringBuffer sb = new StringBuffer();
                guidTeacherlist.stream().forEach(userName -> {
                    sb.append("指导老师:").append(userName).append(",");
                });
                guidTeacherMap.put("value", sb.substring(0, sb.length() - 1).toString());
                competitionApplyInfoResList.add(guidTeacherMap);
            }
            Map<String, Object> userNameMap = new HashMap<>();
            List<String> userNameList = competitionApplyInfoList.stream().filter(competitionApplyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName())).
                    map(CompetitionApplyInfo::getUserName).distinct().toList();
            if (StringUtils.isNotEmpty(userNameList)) {
                StringBuffer sb = new StringBuffer();
                userNameList.stream().forEach(userName -> sb.append(userName).append(","));
                userNameMap.put("label", "队员姓名");
                userNameMap.put("value", "队员:" + sb.substring(0, sb.length() - 1));
                competitionApplyInfoResList.add(userNameMap);
            }

        }
        return competitionApplyInfoResList;
    }

    @Override
    public List<CompetitionApplyInfo> selectCompetitionApplyTeamCode(String teamCode) {
        return competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamCode);
    }

    @Override
    public List<CompetitionApplyInfo> selectTeamInfoByTeamCode(Map<String, Object> params) {
        return competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(params);
    }

    @Override
    public List<CompetitionApplyInfo> selectCompetitionApplyInfoListByUserId(Map<String, Object> params) {
        List<CompetitionApplyInfo> competitionApplyInfos = competitionApplyInfoMapper.selectCompetitionApplyInfoListByUserId(params);
//        if(CollectionUtils.isNotEmpty(competitionApplyInfos)){
//            // 如果是队长或者队员取出所有团队信息
//            //取出competitionApplyInfos中competitionRoleName是指导教师的数据
//            List<CompetitionApplyInfo> guideTeacherInfos = competitionApplyInfos.stream().filter(competitionApplyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName())).toList();
//            if (CollectionUtils.isNotEmpty(guideTeacherInfos)) {
//                params.put("teamCodes",competitionApplyInfos.get(0).getTeamCode());
//                return competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(params);
//            }
//        }
        return competitionApplyInfos;
    }

    @Override
    public List<CompetitionApplyInfo> getInnerApplyUserInfo(CompetitionApplyInfo competitionApplyInfo) {
        return competitionApplyInfoMapper.selectCompetitionApplyInfoListPC(competitionApplyInfo);
    }

    /**
     * 获取订单详情
     *
     * @param map competitionSeriesId赛事id，teamCodeList团队编码列表
     * @return
     */
    @Override
    public List<CompetitionApplyInfoVO> getDetailForOrder(Map<String, Object> map) {
        log.info("获取订单详情入参:" + JSONObject.toJSONString(map));
//        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Long userId = map.get("userId") == null ? null : Long.parseLong(map.get("userId").toString());
        Long competitionSeriesId = MapUtils.getLong(map, "competitionSeriesId");
        List<String> teamCodeList = (List<String>) MapUtils.getObject(map, "teamCodeList");
        List<CompetitionApplyInfoVO> competitionApplyInfoVOS = competitionApplyInfoMapper.selectTeamCompetitionInfoByTeamCodesForOrder(competitionSeriesId, teamCodeList, userId);
        setSubTotal(competitionApplyInfoVOS);
        return competitionApplyInfoVOS;
    }

    /**
     * 设置每个团队人数并计算小计
     *
     * @param list
     */
    private void setSubTotal(List<CompetitionApplyInfoVO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(vo -> {
                //计算队员数量=teamSize，计算总费用totalFee=teamSize*fee
                if (CollectionUtils.isEmpty(vo.getRegistrationInfoList())) {
                    vo.setTeamSize(0);
                    vo.setSubtotal("0");
                    return;
                }
                List<RegistrationInfo> players = vo.getRegistrationInfoList().stream().filter(e -> "队员".equals(e.getCompetitionRoleName()) || "队长".equals(e.getCompetitionRoleName())).toList();
                vo.setPlayersList(players);
                vo.setTeamSize(players.size());
                List<RegistrationInfo> instructor = vo.getRegistrationInfoList().stream().filter(e -> "指导教师".equals(e.getCompetitionRoleName())).toList();
                vo.setInstructorList(instructor);
                BigDecimal fee = new BigDecimal(StringUtils.isNotBlank(vo.getFee()) ? vo.getFee() : "0");
                vo.setSubtotal(fee.multiply(new BigDecimal(vo.getTeamSize())) + "");
                vo.setRegistrationInfoList(null);
            });
        }
    }

    /**
     * 获取 人数×单价 详情
     *
     * @param list
     * @return
     */
    private List<String> getDetail(List<CompetitionApplyInfoVO> list) {
        Map<String, Integer> feeAndSize = new HashMap<>();
        for (CompetitionApplyInfoVO vo : list) {
            String fee = vo.getFee();
            feeAndSize.put(fee, feeAndSize.getOrDefault(fee, 0) + vo.getTeamSize());
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : feeAndSize.entrySet()) {
            result.add(entry.getValue() + "人 × ¥" + Double.valueOf(entry.getKey()) + "/人");
        }
        return result;
    }


    /**
     * 1.学院校验
     * 所有院校都能报名本科A，985/211只能报名本科A，普通院校一般报名本科B，也可报名本科A；
     *
     * @param excelInfoList 表格数据
     * @Param competitionTrackId 赛道ID
     * @Param type 类型:导入/修改(group)
     */
    @Override
    public String collegeVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId, String type) {
        if (CollectionUtils.isNotEmpty(excelInfoList)) {
            String school = SecurityUtils.getLoginUser().getSysUser().getSchool();
            List<CompetitionApplyInfo> list = excelInfoList.stream().filter(info -> competitionTrackId.equals(info.getCompetitionTrackId())).toList();
            // 985/211院校信息
            R<List<NationwideCollegeInfo>> doubleFirstClassUniversityPlan = userService.getDoubleFirstClassUniversityPlan(SecurityConstants.INNER);
            if (R.isSuccess(doubleFirstClassUniversityPlan)) {
                List<NationwideCollegeInfo> collegeInfoList = doubleFirstClassUniversityPlan.getData();
                //如果是985/211院校，则只能报名本科A
                if (collegeInfoList.stream().anyMatch(info -> info.getId().equals(school))) {
                    List<CompetitionApplyInfo> list1 = list.stream().filter(info -> "本科B".equals(info.getSecondLevelName()) || "中职".equals(info.getSecondLevelName()) || "高职".equals(info.getSecondLevelName())).toList();
                    if (CollectionUtils.isNotEmpty(list1)) {
                        CompetitionApplyInfo info = list1.get(0);
                        String msgStart = "985/211院校仅能报名本科A组或研究生组";
                        return ApplyConstants.OPERATION_CHANGE_GROUP.equals(type) ? msgStart : msgStart + "，【" + info.getCompetitionTrackName() + "】中【" + info.getUserName() + "】组别有误";
                    }
                    return null;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    /**
     * 校验学院报名资格
     * 规则：
     * 1. 所有院校都能报名本科A
     * 2. 985/211只能报名本科A
     * 3. 普通院校可报名本科B或本科A
     *
     * @param excelInfoList      表格数据
     * @param competitionTrackId 赛道ID
     * @return 错误信息，校验通过返回null
     */
    @Override
    public String collegeVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId, String type) {
        if (CollectionUtils.isEmpty(excelInfoList)) {
            return null;
        }

        String currentSchoolId = SecurityUtils.getLoginUser().getSysUser().getSchool();
        List<CompetitionApplyInfo> targetApplications = excelInfoList.stream()
                .filter(info -> competitionTrackId.equals(info.getCompetitionTrackId()))
                .toList();

        R<List<NationwideCollegeInfo>> universityPlan = userService.getDoubleFirstClassUniversityPlan(SecurityConstants.INNER);
        if (!R.isSuccess(universityPlan)) {
            return "获取院校信息失败";
        }

        boolean isDoubleFirstClass = universityPlan.getData().stream()
                .anyMatch(info -> info.getId().equals(currentSchoolId));

        //只能报名本科A组或研究生组的院校，不能报名其他组别
        if (isDoubleFirstClass) {
            List<CompetitionApplyInfo> invalidApplications = targetApplications.stream()
                    .filter(info -> !"本科A".equals(info.getSecondLevelName()) && !"研究生".equals(info.getSecondLevelName()))
                    .toList();

            if (!invalidApplications.isEmpty()) {
                CompetitionApplyInfo firstInvalid = invalidApplications.get(0);
                String msgStart = "985/211院校仅能报名本科A组或研究生组";
                if (ApplyConstants.OPERATION_CHANGE_GROUP.equals(type)) {
                    return msgStart;
                }
                return String.format("985/211院校仅能报名本科A组或研究生组，【%s】中【%s】组别有误",
                        firstInvalid.getCompetitionTrackName(),
                        firstInvalid.getUserName());
            }
        }
        return null;
    }

    /**
     * 2.跨校组队校验
     *
     * @param competitionTrackId 赛道
     * @param excelInfoList      表格数据
     */
    @Override
    public String crossSchoolTeamVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        if (CollectionUtils.isNotEmpty(excelInfoList)) {
            List<CompetitionApplyInfo> list = excelInfoList.stream().filter(info -> competitionTrackId.equals(info.getCompetitionTrackId())).toList();
            List<CompetitionApplyInfo> competitionApplyInfos = competitionApplyInfoMapper.selectCompetitionApplyInfos(competitionTrackId, "student");
            if (CollectionUtils.isNotEmpty(competitionApplyInfos)) {
                for (CompetitionApplyInfo excelInfo : list) {
                    //在历史信息找到相同身份证号 且 学校不同的，有表示跨校组队了
                    List<CompetitionApplyInfo> list1 = competitionApplyInfos.stream().filter(applyInfo -> applyInfo.getIdCard().equals(excelInfo.getIdCard()) && !applyInfo.getSchool().equals(excelInfo.getSchool())).toList();
                    if (CollectionUtils.isNotEmpty(list1)) {
                        return "【" + excelInfo.getCompetitionTrackName() + "】中【" + excelInfo.getUserName() + "】存在跨校组队情况";
                    }
                }
                return null;
            }
            return null;
        }
        return null;
    }

    /**
     * 2.跨校组队校验
     *
     * @param competitionTrackId 赛道ID
     * @param excelInfoList      表格数据
     */
    @Override
    public String crossSchoolTeamVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        if (competitionTrackId == null || CollectionUtils.isEmpty(excelInfoList)) {
            return null;
        }
        List<CompetitionApplyInfo> filteredList = excelInfoList.stream()
                .filter(info -> competitionTrackId.equals(info.getCompetitionTrackId()))
                .toList();

        List<CompetitionApplyInfo> existingInfos = competitionApplyInfoMapper.selectCompetitionApplyInfos(
                competitionTrackId, "student");

        if (CollectionUtils.isEmpty(existingInfos)) {
            return null;
        }
        for (CompetitionApplyInfo excelInfo : filteredList) {
            boolean hasCrossSchool = existingInfos.stream()
                    .anyMatch(applyInfo ->
                            applyInfo.getIdCard().equals(excelInfo.getIdCard()) &&
                                    !applyInfo.getSchool().equals(excelInfo.getSchool())
                    );

            if (hasCrossSchool) {
                return String.format("【%s】中【%s】存在跨校组队情况",
                        excelInfo.getCompetitionTrackName(),
                        excelInfo.getUserName());
            }
        }
        return null;
    }


    /**
     * 3.重复组队校验
     *
     * @param competitionTrackId 赛道
     * @param excelInfoList      表格数据
     */
    @Override
    public String repeatTeamVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        if (CollectionUtils.isNotEmpty(excelInfoList)) {
            List<CompetitionApplyInfo> list = excelInfoList.stream().filter(info -> competitionTrackId.equals(info.getCompetitionTrackId())).toList();
            List<CompetitionApplyInfo> list2 = list.stream().filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName())).toList();
            //按身份证号分组
            Map<String, List<CompetitionApplyInfo>> collect = list2.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getIdCard));
            for (Map.Entry<String, List<CompetitionApplyInfo>> entry : collect.entrySet()) {
                List<CompetitionApplyInfo> value = entry.getValue();
                if (CollectionUtils.isNotEmpty(value) && value.size() > 1) {
//                    throw new RuntimeException("【"+value.get(0).getCompetitionTrackName() + "】中【" + value.get(0).getUserName() + "】存在重复组队情况");
                    return "【" + value.get(0).getCompetitionTrackName() + "】中【" + value.get(0).getUserName() + "】存在重复组队情况";
                }
            }

            List<CompetitionApplyInfo> competitionApplyInfos = competitionApplyInfoMapper.selectCompetitionApplyInfos(competitionTrackId, "student");
            if (CollectionUtils.isEmpty(competitionApplyInfos)) {
                return null;
            }
            List<CompetitionApplyInfo> list3 = competitionApplyInfos.stream().filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName())).toList();
            //按身份证号分组
            Map<String, List<CompetitionApplyInfo>> collect2 = list3.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getIdCard));
            for (Map.Entry<String, List<CompetitionApplyInfo>> entry : collect.entrySet()) {
                List<CompetitionApplyInfo> value = entry.getValue();
                List<CompetitionApplyInfo> competitionApplyInfos1 = collect2.get(entry.getKey());
                Set<String> teamCodeSetNew = value.stream().map(CompetitionApplyInfo::getTeamCode).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(competitionApplyInfos1)) {
                    Set<String> teamCodeSetTable = competitionApplyInfos1.stream().map(CompetitionApplyInfo::getTeamCode).collect(Collectors.toSet());
                    if (!teamCodeSetTable.containsAll(teamCodeSetNew)) {
                        value.addAll(competitionApplyInfos1);
                    }
                }
                entry.setValue(value);
            }
            for (Map.Entry<String, List<CompetitionApplyInfo>> entry : collect.entrySet()) {
                List<CompetitionApplyInfo> value = entry.getValue();
                if (CollectionUtils.isNotEmpty(value) && value.size() > 1) {
//                    throw new RuntimeException("【"+value.get(0).getCompetitionTrackName() + "】中【" + value.get(0).getUserName() + "】存在重复组队情况");
                    return "【" + value.get(0).getCompetitionTrackName() + "】中【" + value.get(0).getUserName() + "】存在重复组队情况";
                }
            }
            return null;
        }
        return null;
    }

    /**
     * 3.重复组队校验
     *
     * @param excelInfoList      表格数据
     * @param competitionTrackId 赛道
     */
    @Override
    public String repeatTeamVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        Map<String, List<CompetitionApplyInfo>> allGrouped = new HashMap<>();
        // 处理excel数据
        List<CompetitionApplyInfo> filteredExcel = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(excelInfoList)) {
            filteredExcel = excelInfoList.stream()
                    .filter(info -> competitionTrackId.equals(info.getCompetitionTrackId()))
                    .filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName()))
                    .toList();

            Map<String, List<CompetitionApplyInfo>> excelGrouped = filteredExcel.stream()
                    .collect(Collectors.groupingBy(CompetitionApplyInfo::getIdCard));
            allGrouped.putAll(excelGrouped);
        }
        // 检查重复
        for (Map.Entry<String, List<CompetitionApplyInfo>> entry : allGrouped.entrySet()) {
            List<CompetitionApplyInfo> value = entry.getValue();
            if (CollectionUtils.isNotEmpty(value) && value.size() > 1) {
                return String.format("【%s】中【%s】存在重复组队情况",
                        value.get(0).getCompetitionTrackName(),
                        value.get(0).getUserName());
            }
        }
        List<CompetitionApplyInfo> allList = new ArrayList<>(filteredExcel);

        // 处理数据库数据
        List<CompetitionApplyInfo> dbInfos = competitionApplyInfoMapper.selectCompetitionApplyInfos(competitionTrackId, "student");
        if (CollectionUtils.isNotEmpty(dbInfos)) {
            List<CompetitionApplyInfo> filteredDb = dbInfos.stream()
                    .filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName()))
                    .toList();
            if (CollectionUtils.isEmpty(filteredDb)) {
                return null;
            }
            allList.addAll(filteredDb);
            Map<String, List<CompetitionApplyInfo>> dbGrouped = allList.stream()
                    .collect(Collectors.groupingBy(CompetitionApplyInfo::getIdCard));
            // 检查重复
            for (Map.Entry<String, List<CompetitionApplyInfo>> entry : dbGrouped.entrySet()) {
                List<CompetitionApplyInfo> value = entry.getValue();
                if (CollectionUtils.isNotEmpty(value) && value.size() > 1) {
                    return String.format("【%s】中【%s】存在重复组队情况",
                            value.get(0).getCompetitionTrackName(),
                            value.get(0).getUserName());
                }
            }
        }
        return null;
    }


    /**
     * 4.专业校验  每个团队至少有两个不同专业的队员
     *
     * @param excelInfoList 表格数据
     */
    @Override
    public String professionalVerification(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        if (CollectionUtils.isNotEmpty(excelInfoList)) {
            List<CompetitionApplyInfo> list = excelInfoList.stream().filter(info -> competitionTrackId.equals(info.getCompetitionTrackId())).toList();
            List<CompetitionApplyInfo> list1 = list.stream().filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName())).toList();
            if (CollectionUtils.isNotEmpty(list1)) {
                Map<String, List<CompetitionApplyInfo>> collect = list1.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
                for (Map.Entry<String, List<CompetitionApplyInfo>> entry : collect.entrySet()) {
                    List<CompetitionApplyInfo> value = entry.getValue();
                    Set<String> profession = new HashSet<>();
                    value.forEach(competitionApplyInfo -> profession.add(competitionApplyInfo.getProfession().trim()));
                    if (profession.size() < 2) {
                        String competitionTrackName = value.get(0).getCompetitionTrackName();
                        String userName = value.get(0).getUserName();
                        String teamName = value.get(0).getTeamName();
                        return StringUtils.isNotBlank(teamName) ? "【" + competitionTrackName + "】中，队名为【" + teamName + "】的团队至少需要包含两个不同的专业" : "【" + competitionTrackName + "】中，【" + userName + "】所在的团队至少需要包含两个不同的专业";
                    }
                }
                return null;
            }
            return null;
        }
        return null;
    }

    /**
     * 4.专业校验  每个团队至少有两个不同专业的队员
     *
     * @param excelInfoList 表格数据
     */
    @Override
    public String professionalVerification1(List<CompetitionApplyInfo> excelInfoList, String competitionTrackId) {
        if (CollectionUtils.isEmpty(excelInfoList) || competitionTrackId == null) {
            return null;
        }
        List<CompetitionApplyInfo> filteredList = excelInfoList.stream()
                .filter(info -> competitionTrackId.equals(info.getCompetitionTrackId()))
                .filter(info -> "队员".equals(info.getCompetitionRoleName()) || "队长".equals(info.getCompetitionRoleName()))
                .toList();
        if (CollectionUtils.isEmpty(filteredList)) {
            return null;
        }
        Map<String, List<CompetitionApplyInfo>> teamMap = filteredList.stream()
                .collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
        for (Map.Entry<String, List<CompetitionApplyInfo>> entry : teamMap.entrySet()) {
            List<CompetitionApplyInfo> teamMembers = entry.getValue();
            Set<String> professions = new HashSet<>();

            for (CompetitionApplyInfo member : teamMembers) {
                String profession = member.getProfession() != null ? member.getProfession().trim() : "";
                professions.add(profession);
            }
            if (professions.size() < 2) {
                CompetitionApplyInfo firstMember = teamMembers.get(0);
                String trackName = firstMember.getCompetitionTrackName();
                String teamName = firstMember.getTeamName();
                String userName = firstMember.getUserName();

                String template = StringUtils.isNotBlank(teamName)
                        ? "【%s】中，队名为【%s】的团队至少需要包含两个不同的专业"
                        : "【%s】中，【%s】所在的团队至少需要包含两个不同的专业";

                return String.format(template, trackName,
                        StringUtils.isNotBlank(teamName) ? teamName : userName);
            }
        }
        return null;
    }

    /**
     * 查询某userId已缴费的报名信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<CompetitionApplyInfo> getCompetitionApplyInfoByPayStatusForUserGroup(Long userId) {
        return competitionApplyInfoMapper.selectCompetitionApplyInfoByPayStatusForUserGroup(userId);
    }

    /**
     * 查询匹配规则的报名成功已缴费的人员信息
     *
     * @param param
     * @return
     */
    @Override
    public Set<Long> getUserInfoByCompetitions(Map<String, Object> param) {
        Set<Long> result = new HashSet<>();
        Object certificationTypes = param.get("certificationTypes");
        if (Objects.isNull(certificationTypes)) {
            //没有限制身份
            return competitionApplyInfoMapper.selectInfoByCompetitions(param);
        } else {
            List<String> types = (List<String>) certificationTypes;
            // 规则身份
            if (types.contains("student")) {
                Set<Long> studentIds = competitionApplyInfoMapper.selectStudentInfoByCompetitions(param);
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    result.addAll(studentIds);
                }
            }
            if (types.contains("teacher")) {
                Set<Long> teacherIds = competitionApplyInfoMapper.selectTeacherInfoByCompetitions(param);
                if (CollectionUtils.isNotEmpty(teacherIds)) {
                    result.addAll(teacherIds);
                }
            }
        }
        return result;
    }

    @Override
    public List<CompetitionApplyInfo> selectAllUserInfoByCompetitions(Map<String, Object> param) {
        return competitionApplyInfoMapper.selectAllUserInfoByCompetitions(param);
    }

    /**
     * 根据userId和competitionId查询团队的报名信息
     * @param userId
     * @param competitionId
     * @return
     */
    @Override
    public List<CompetitionApplyInfo> getApplyInfoByUsrIdAndCompetitionId(Long userId,Long competitionId) {
        return competitionApplyInfoMapper.selectApplyInfoByUsrIdAndCompetitionId(userId,competitionId);
    }

    @Override
    public List<CompetitionApplyInfo> getCompetitionApplyInfoListByCompetitionSeriesId(Long seriesId) {
        return competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(seriesId);
    }
}
