package com.teaching.competition.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.constant.*;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.*;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.CompetitionApplyInfoCheckService;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.competition.service.ITeamManagerInfoService;
import com.teaching.competition.util.StringNumberUtil;
import com.teaching.competition.util.UUIDUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import net.sf.jsqlparser.statement.select.Distinct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 团队管理Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class TeamManagerInfoServiceImpl implements ITeamManagerInfoService{

private static final Logger log = LoggerFactory.getLogger(TeamManagerInfoServiceImpl.class);
    @Autowired
    private TeamManagerInfoMapper teamManagerInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private TeamMemberRelaMapper teamMemberRelaMapper;

    @Autowired
    private CompetitionApplyInfoCheckService competitionApplyInfoCheckService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private ChangeLogMapper changeLogMapper;

    /**
     * 查询团队管理
     *
     * @param teamCode 团队管理主键
     * @return 团队管理
     */
    @Override
    public TeamManagerInfo selectTeamManagerInfoByTeamCode(Long teamId,String teamCode) {
        TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.selectTeamManagerInfoByTeamCode(teamId, teamCode);
        if (teamManagerInfo != null) {
            TeamMemberRela teamMemberRela = new TeamMemberRela();
            teamMemberRela.setTeamCode(teamManagerInfo.getTeamCode());
            // 团队成员列表
            teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
//            List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
            List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamCode);
            teamManagerInfo.setTeamMemberRelaList(applyInfoList);
            teamManagerInfo.setTeamNum(applyInfoList.size()+"");
            // 带队老师名称
            if(CollectionUtils.isNotEmpty(applyInfoList)){
                R<SysUser> userInfo = userService.getUserCenterInfo(Long.valueOf(teamManagerInfo.getLeaderTeacher()), SecurityConstants.INNER);
                if(userInfo.getCode() == HttpStatus.SUCCESS && userInfo.getData() !=  null){
                    AuthInfo authInfo = userInfo.getData().getAuthInfo();
                    if(authInfo != null){
                        teamManagerInfo.setLeaderTeacherName(authInfo.getRealName());
                    }
                }
            }
            // 团员名称
//            if(CollectionUtils.isNotEmpty(teamMemberRelaList)){
//                teamMemberRelaList.stream().forEach(teamMember -> {
//                    R<SysUser> userInfo = userService.getUserCenterInfo(teamMember.getUserId(), SecurityConstants.INNER);
//                    if(userInfo.getCode() == HttpStatus.SUCCESS && userInfo.getData() !=  null){
//                        AuthInfo authInfo = userInfo.getData().getAuthInfo();
//                        if(authInfo != null){
//                            teamMember.setUserName(authInfo.getRealName());
//                        } else {
//                            teamMember.setUserName(userInfo.getData().getUserName());
//                        }
//                    }
//                });
//            }
        }
        return teamManagerInfo;
    }

    /**
     * 查询团队管理列表
     *
     * @param teamManagerInfo 团队管理
     * @return 团队管理
     */
    @Override
//    @DataScope(orgAlias = "a", userAlias = "a")
    public List<TeamManagerInfo> selectTeamManagerInfoList(TeamManagerInfo teamManagerInfo) {
        // 获取带队老师名称
        if (StringUtils.isNotEmpty(teamManagerInfo.getLeaderTeacherName())) {
            R<List<AuthInfo>> userCenterTeacherInfo = userService.selectAuthInfoByName(teamManagerInfo.getLeaderTeacherName(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                List<AuthInfo> authInfoList = userCenterTeacherInfo.getData();
                if(CollectionUtils.isNotEmpty(authInfoList)){
                    List<Long> teacherIdList = authInfoList.stream().map(AuthInfo::getUserId).collect(Collectors.toList());
                    teamManagerInfo.setTeacherIdList(teacherIdList);
                }else {
                    teamManagerInfo.setTeacherIdList(Arrays.asList(0l));
                }
            }
        }
        List<TeamManagerInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamManagerInfoList(teamManagerInfo);
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            teamManagerInfos.stream().forEach(teamManager -> {
//                TeamMemberRela teamMemberRela = new TeamMemberRela();
//                teamMemberRela.setTeamCode(teamManager.getTeamCode());
//                // 团队成员列表
////                teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
//                List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
                List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamManager.getTeamCode());
                List<CompetitionApplyInfo> teamMemberList = new ArrayList<>();
                List<CompetitionApplyInfo> guidTeacherList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(applyInfoList)){
                    applyInfoList.stream().forEach(teamMember -> {
                        if(ApplyConstants.TEAM_MEMBER.equals(teamMember.getCompetitionRoleName())){
                            teamMemberList.add(teamMember);
                        }
                        if(ApplyConstants.TEAM_LEADER_MEMBER.equals(teamMember.getCompetitionRoleName())){
                            teamMemberList.add(teamMember);
                        }
                        if(ApplyConstants.TEAM_GUIDE_TEACHER.equals(teamMember.getCompetitionRoleName())){
                            guidTeacherList.add(teamMember);
                        }
                    });
                }
                teamManager.setTeamMemberRelaList(teamMemberList);
                teamManager.setTeamNum(teamMemberList.size()+"");
                teamManager.setGuidTeacherList(guidTeacherList);
                // 带队老师名称
                if(CollectionUtils.isNotEmpty(applyInfoList)){
                    R<SysUser> userInfo = userService.getUserCenterInfo(Long.valueOf(teamManager.getLeaderTeacher()), SecurityConstants.INNER);
                    if(userInfo.getCode() == HttpStatus.SUCCESS && userInfo.getData() !=  null){
                        AuthInfo authInfo = userInfo.getData().getAuthInfo();
                        if(authInfo != null){
                            teamManager.setLeaderTeacherName(authInfo.getRealName());
                        }
                    }
                }
                // 指导老师翻译出名称
//                List<String> teacherNameList = new ArrayList<>();
//                if(StringUtils.isNotEmpty(teamManager.getGuideTeacher())){
//                    List<String> teacherIdList = Arrays.asList(teamManager.getGuideTeacher().split(","));
//                    teacherIdList.stream().forEach(teacherId -> {
//                        // 判断字符是春数字还是其他组合
//                        if(StringNumberUtil.isNumber(teacherId)){
//                            SysUser sysUser = userService.getUserCenterInfo(Long.valueOf(teacherId), SecurityConstants.INNER).getData();
//                            if(null != sysUser){
//                                if (sysUser.getAuthInfo() != null) {
//                                    teacherNameList.add(sysUser.getAuthInfo().getRealName());
//                                }else {
//                                    teacherNameList.add(sysUser.getUserName());
//                                }
//                            }else {
//                                teacherNameList.add(teacherId);
//                            }
//                        } else {
//                            teacherNameList.add(teacherId);
//                        }
//                    });
//                }
//                teamManager.setGuideTeacherName(String.join(",", teacherNameList));
                // 获取团队变更旧数据
                List<Map<String, Object>> oldDateList = new ArrayList<>();
                List<ChangeLog> changeLogList = changeLogMapper.selectChangeLogListByTeamCode(teamManager.getTeamCode());
                if (CollectionUtils.isNotEmpty(changeLogList)) {
                    List<ChangeLog> teamChangeLogList = changeLogList.stream().
                            filter(changeLog -> "change".equals(changeLog.getChangeType()) || "changeTeacher".equals(changeLog.getChangeType()))
                            .toList();
                    if(CollectionUtils.isNotEmpty(teamChangeLogList)){
                        teamChangeLogList.forEach(changeLog -> {
                            Map<String, Object> oldDateMap = new HashMap<>();
                            String oldData = changeLog.getOldData();
                            if(StringUtils.isNotEmpty(oldData)){
                                oldDateMap.put("changeType", changeLog.getChangeType());
                                oldDateMap.put("operatorUser", changeLog.getCreateBy());
                                oldDateMap.put("changeTime", DateUtils.getDateFormat(changeLog.getChangeTime()));
                                List<CompetitionApplyInfo> teamManagerInfoOldList = JSONUtil.toList(oldData, CompetitionApplyInfo.class);
                                List<String> memberNameList = teamManagerInfoOldList.stream()
                                                .filter(teamMember -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(teamMember.getCompetitionRoleName()))
                                                .map(CompetitionApplyInfo::getUserName).toList();
                                List<String> teacherNameList = teamManagerInfoOldList.stream()
                                        .filter(teamMember -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(teamMember.getCompetitionRoleName()))
                                        .map(CompetitionApplyInfo::getUserName).toList();
                                String memberName = "";
                                if("change".equals(changeLog.getChangeType()) && StringUtils.isNotEmpty(memberNameList))
                                        memberName = String.join(",", memberNameList);
                                oldDateMap.put("memberNameOld", memberName);
                                String teacherName = "";
                                if("changeTeacher".equals(changeLog.getChangeType()) && StringUtils.isNotEmpty(teacherNameList))
                                    teacherName = String.join(",", teacherNameList);
                                oldDateMap.put("teacherNameOld", teacherName);
                            }
                            String newData = changeLog.getNewData();
                            if(StringUtils.isNotEmpty(newData)){
                                List<CompetitionApplyInfo> teamManagerInfoNewList = JSONUtil.toList(newData, CompetitionApplyInfo.class);
                                List<String> memberNameList = teamManagerInfoNewList.stream()
                                        .filter(teamMember -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(teamMember.getCompetitionRoleName()))
                                        .map(CompetitionApplyInfo::getUserName).toList();
                                List<String> teacherNameList = teamManagerInfoNewList.stream()
                                        .filter(teamMember -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(teamMember.getCompetitionRoleName()))
                                        .map(CompetitionApplyInfo::getUserName).toList();
                                String memberName = "";
                                if("change".equals(changeLog.getChangeType()) && StringUtils.isNotEmpty(memberNameList))
                                    memberName = String.join(",", memberNameList);
                                oldDateMap.put("memberNameNew", memberName);
                                String teacherName = "";
                                if("changeTeacher".equals(changeLog.getChangeType()) && StringUtils.isNotEmpty(teacherNameList))
                                    teacherName = String.join(",", teacherNameList);
                                oldDateMap.put("teacherNameNew", teacherName);
                            }
                            oldDateList.add(oldDateMap);
                        });
                    }
                }
                teamManager.setTeamManagerInfoOldData(oldDateList);
            });
        }
        return teamManagerInfos;
    }

    @Override
    public List<TeamManagerInfo> selectTeamManagerInfoListExport(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        getLeaderTeacherInfo(competitionApplyInfo);
        List<TeamManagerInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamManagerInfoExportList(competitionApplyInfo);
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            List<String> userIdList = teamManagerInfos.stream().map(TeamManagerInfo::getLeaderTeacher).distinct().collect(Collectors.toList());
            List<Long> userIdLongList = userIdList.stream().map(Long::valueOf).collect(Collectors.toList());
            R<List<SysUser>> userCenterTeacherInfo = userService.getUserCenterInfoList(userIdLongList, SecurityConstants.INNER);
            Map<Long,List<SysUser>> userCenterTeacherInfoMap;
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                userCenterTeacherInfoMap = userCenterTeacherInfo.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
            } else {
                userCenterTeacherInfoMap = new HashMap<>();
            }
            teamManagerInfos.stream().forEach(teamManager -> {
                List<SysUser> SysUserList= userCenterTeacherInfoMap.get(Long.valueOf(teamManager.getLeaderTeacher()));
                if(CollectionUtils.isNotEmpty(SysUserList)){
                    AuthInfo authInfo = SysUserList.get(0).getAuthInfo();
                    teamManager.setLeaderTeacherName(authInfo == null ? null : authInfo.getRealName());
                    teamManager.setLeaderTeacherPhone(SysUserList.get(0) == null ? null : SysUserList.get(0).getPhonenumber());
                    teamManager.setLeaderTeacherEmail(SysUserList.get(0) == null ? null : SysUserList.get(0).getEmail());
                }
                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(teamManager.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
                    OrderInfo data = ordersResponse.getData();
                    if (Objects.nonNull(data.getPayTime())) {
                        teamManager.setPayTime(data.getPayTime());
                    } else {
                        log.warn("团队编码 {} 的订单支付时间为空，订单ID: {}", teamManager.getTeamCode(), data.getOrderId());
                    }
                } else {
                    log.warn("根据团队编码 {} 查询订单失败或订单不存在，响应: {}", teamManager.getTeamCode(), ordersResponse);
                }
                List<CompetitionApplyInfo> applyInfoList = teamManager.getApplyInfoList();
                List<CompetitionApplyInfo> memberApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                List<CompetitionApplyInfo> teacherApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(memberApplyInfoList)){
                    // 按照主键id排序
//                    memberApplyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getMemberId));
                    memberApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTwo(applyInfo);
                                    break;
                                case 3:
                                    teamManager.setCompetitionApplyInfoThree(applyInfo);
                                    break;
                                case 4:
                                    teamManager.setCompetitionApplyInfoFour(applyInfo);
                                    break;
                                case 5:
                                    teamManager.setCompetitionApplyInfoFive(applyInfo);
                                    break;
                                case 6:
                                    teamManager.setCompetitionApplyInfoSix(applyInfo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                if(CollectionUtils.isNotEmpty(teacherApplyInfoList)){
//                    teacherApplyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getMemberId));
                    teacherApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoTeacherOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTeacherTwo(applyInfo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            });
            // 排序
//            teamManagerInfos = teamManagerInfos.stream()
//                    .sorted(Comparator.comparing(TeamManagerInfo::getPayTime, Comparator.nullsFirst(Date::compareTo))
//                            .reversed()).collect(Collectors.toList());
//            teamManagerInfos.sort(Comparator.comparing(TeamManagerInfo::getPayTime, Comparator.nullsLast(Date::compareTo)).reversed());
            teamManagerInfos.sort(Comparator.comparing(TeamManagerInfo::getTeamCode)
                    .thenComparing(TeamManagerInfo::getPayTime, Comparator.nullsLast(Comparator.<Date>naturalOrder().reversed())));
        }
        return teamManagerInfos;
    }

    @Override
    public List<TeamManagerInfoAwardsInfo> selectTeamManagerInfoAwardsExportList(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        getLeaderTeacherInfo(competitionApplyInfo);
        List<TeamManagerInfoAwardsInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamManagerInfoAwardsExportList(competitionApplyInfo);
        teamManagerInfos = getTeamManagerInfoAwardsInfos(teamManagerInfos);
        return teamManagerInfos;
    }

    @Override
    public List<TeamManagerInfoAwardsUserInfo> selectTeamManagerInfoAwardsExportPCList(CompetitionApplyInfo competitionApplyInfo) {
        // 获取带队老师名称
        getLeaderTeacherInfo(competitionApplyInfo);
        List<TeamManagerInfoAwardsUserInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamManagerInfoAwardsExportUserList(competitionApplyInfo);
        teamManagerInfos = getTeamManagerInfoAwardsUserInfos(teamManagerInfos);
        return teamManagerInfos;
    }

    private List<TeamManagerInfoAwardsUserInfo> getTeamManagerInfoAwardsUserInfos(List<TeamManagerInfoAwardsUserInfo> teamManagerInfos) {
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            List<String> userIdList = teamManagerInfos.stream().map(TeamManagerInfoAwardsUserInfo::getLeaderTeacher).distinct().collect(Collectors.toList());
            List<Long> userIdLongList = userIdList.stream().map(Long::valueOf).collect(Collectors.toList());
            R<List<SysUser>> userCenterTeacherInfo = userService.getUserCenterInfoList(userIdLongList, SecurityConstants.INNER);
            Map<Long,List<SysUser>> userCenterTeacherInfoMap;
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                userCenterTeacherInfoMap = userCenterTeacherInfo.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
            } else {
                userCenterTeacherInfoMap = new HashMap<>();
            }
            teamManagerInfos.stream().forEach(teamManager -> {
                List<SysUser> SysUserList= userCenterTeacherInfoMap.get(Long.valueOf(teamManager.getLeaderTeacher()));
                if(CollectionUtils.isNotEmpty(SysUserList)){
                    AuthInfo authInfo = SysUserList.get(0).getAuthInfo();
                    teamManager.setLeaderTeacherName(authInfo == null ? null : authInfo.getRealName());
                    teamManager.setLeaderTeacherPhone(SysUserList.get(0) == null ? null : SysUserList.get(0).getPhonenumber());
                    teamManager.setLeaderTeacherEmail(SysUserList.get(0) == null ? null : SysUserList.get(0).getEmail());
                }
                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(teamManager.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
                    OrderInfo data = ordersResponse.getData();
                    if (Objects.nonNull(data.getPayTime())) {
                        teamManager.setPayTime(data.getPayTime());
                    } else {
                        log.warn("团队编码 {} 的订单支付时间为空，订单ID: {}", teamManager.getTeamCode(), data.getOrderId());
                    }
                } else {
                    log.warn("根据团队编码 {} 查询订单失败或订单不存在，响应: {}", teamManager.getTeamCode(), ordersResponse);
                }
                List<CompetitionApplyInfo> applyInfoList = teamManager.getApplyInfoList();
                List<CompetitionApplyInfo> memberApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                List<CompetitionApplyInfo> teacherApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(memberApplyInfoList)){
                    memberApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTwo(applyInfo);
                                    break;
                                case 3:
                                    teamManager.setCompetitionApplyInfoThree(applyInfo);
                                    break;
                                case 4:
                                    teamManager.setCompetitionApplyInfoFour(applyInfo);
                                    break;
                                case 5:
                                    teamManager.setCompetitionApplyInfoFive(applyInfo);
                                    break;
                                case 6:
                                    teamManager.setCompetitionApplyInfoSix(applyInfo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                if(CollectionUtils.isNotEmpty(teacherApplyInfoList)){
                    teacherApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoTeacherOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTeacherTwo(applyInfo);
                                    break;
                            }
                        }
                    });
                }
            });
            // 排序
//            teamManagerInfos = teamManagerInfos.stream()
//                    .sorted(Comparator.comparing(TeamManagerInfoAwardsUserInfo::getPayTime, Comparator.nullsFirst(Date::compareTo))
//                            .reversed()).collect(Collectors.toList());
            teamManagerInfos.sort(Comparator.comparing(TeamManagerInfoAwardsUserInfo::getTeamCode)
                    .thenComparing(TeamManagerInfoAwardsUserInfo::getPayTime, Comparator.nullsLast(Comparator.<Date>naturalOrder().reversed())));
        }
        return teamManagerInfos;
    }

    @Nullable
    private List<TeamManagerInfoAwardsInfo> getTeamManagerInfoAwardsInfos(List<TeamManagerInfoAwardsInfo> teamManagerInfos) {
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            List<String> userIdList = teamManagerInfos.stream().map(TeamManagerInfoAwardsInfo::getLeaderTeacher).distinct().collect(Collectors.toList());
            List<Long> userIdLongList = userIdList.stream().map(Long::valueOf).collect(Collectors.toList());
            R<List<SysUser>> userCenterTeacherInfo = userService.getUserCenterInfoList(userIdLongList, SecurityConstants.INNER);
            Map<Long,List<SysUser>> userCenterTeacherInfoMap;
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                userCenterTeacherInfoMap = userCenterTeacherInfo.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
            } else {
                userCenterTeacherInfoMap = new HashMap<>();
            }
            teamManagerInfos.stream().forEach(teamManager -> {
                List<SysUser> SysUserList= userCenterTeacherInfoMap.get(Long.valueOf(teamManager.getLeaderTeacher()));
                if(CollectionUtils.isNotEmpty(SysUserList)){
                    AuthInfo authInfo = SysUserList.get(0).getAuthInfo();
                    teamManager.setLeaderTeacherName(authInfo == null ? null : authInfo.getRealName());
                    teamManager.setLeaderTeacherPhone(SysUserList.get(0) == null ? null : SysUserList.get(0).getPhonenumber());
                    teamManager.setLeaderTeacherEmail(SysUserList.get(0) == null ? null : SysUserList.get(0).getEmail());
                }
                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(teamManager.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
                    OrderInfo data = ordersResponse.getData();
                    if (Objects.nonNull(data.getPayTime())) {
                        teamManager.setPayTime(data.getPayTime());
                    } else {
                        log.warn("团队编码 {} 的订单支付时间为空，订单ID: {}", teamManager.getTeamCode(), data.getOrderId());
                    }
                } else {
                    log.warn("根据团队编码 {} 查询订单失败或订单不存在，响应: {}", teamManager.getTeamCode(), ordersResponse);
                }
                List<CompetitionApplyInfo> applyInfoList = teamManager.getApplyInfoList();
                List<CompetitionApplyInfo> memberApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                List<CompetitionApplyInfo> teacherApplyInfoList = applyInfoList.stream().
                        filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(memberApplyInfoList)){
                    memberApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTwo(applyInfo);
                                    break;
                                case 3:
                                    teamManager.setCompetitionApplyInfoThree(applyInfo);
                                    break;
                                case 4:
                                    teamManager.setCompetitionApplyInfoFour(applyInfo);
                                    break;
                                case 5:
                                    teamManager.setCompetitionApplyInfoFive(applyInfo);
                                    break;
                                case 6:
                                    teamManager.setCompetitionApplyInfoSix(applyInfo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                if(CollectionUtils.isNotEmpty(teacherApplyInfoList)){
                    teacherApplyInfoList.stream().forEach(applyInfo -> {
                        if(Objects.nonNull(applyInfo.getTeamSort())){
                            switch (applyInfo.getTeamSort()){
                                case 1:
                                    teamManager.setCompetitionApplyInfoTeacherOne(applyInfo);
                                    break;
                                case 2:
                                    teamManager.setCompetitionApplyInfoTeacherTwo(applyInfo);
                                    break;
                            }
                        }
                    });
                }
            });
            // 排序
//            teamManagerInfos = teamManagerInfos.stream()
//                    .sorted(Comparator.comparing(TeamManagerInfoAwardsInfo::getPayTime, Comparator.nullsFirst(Date::compareTo))
//                            .reversed()).collect(Collectors.toList());
            teamManagerInfos.sort(Comparator.comparing(TeamManagerInfoAwardsInfo::getTeamCode)
                    .thenComparing(TeamManagerInfoAwardsInfo::getPayTime, Comparator.nullsLast(Comparator.<Date>naturalOrder().reversed())));
        }
        return teamManagerInfos;
    }

    private void getLeaderTeacherInfo(CompetitionApplyInfo competitionApplyInfo) {
        if (StringUtils.isNotEmpty(competitionApplyInfo.getLeaderTeacherName())) {
            R<List<AuthInfo>> userCenterTeacherInfo = userService.selectAuthInfoByName(competitionApplyInfo.getLeaderTeacherName(), SecurityConstants.INNER);
            if (R.isSuccess(userCenterTeacherInfo) && null != userCenterTeacherInfo.getData()) {
                List<AuthInfo> authInfoList = userCenterTeacherInfo.getData();
                if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(authInfoList)){
                    List<Long> teacherIdList = authInfoList.stream().map(AuthInfo::getUserId).collect(Collectors.toList());
                    competitionApplyInfo.setTeacherIdList(teacherIdList);
                } else {
                    competitionApplyInfo.setTeacherIdList(Arrays.asList(0l));
                }
            }
        }
    }

    // 内部调用查询团队信息
    @Override
    public List<TeamManagerInfo> selectInnerTeamManagerInfoList(TeamManagerInfo teamManagerInfo) {
        return teamManagerInfoMapper.selectTeamManagerInfoList(teamManagerInfo);
    }

    @Override
    public List<TeamManagerInfo> selectTeamManagerInfoListByUserId(TeamManagerInfo teamManagerInfo) {
        // 带队老师或者指导老师查报名所有团队，队员差自己所属团队
        teamManagerInfo.setLeaderTeacher(SecurityUtils.getLoginUser().getSysUser().getUserId().toString());
        List<TeamManagerInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamManagerInfoList(teamManagerInfo);
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            teamManagerInfos.stream().forEach(teamManager -> {
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setTeamCode(teamManager.getTeamCode());
                List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
                teamManager.setTeamNum(teamMemberRelaList.size()+"");
                // 团员名称
                if(CollectionUtils.isNotEmpty(teamMemberRelaList)){
                    teamMemberRelaList.stream().forEach(teamMember -> {
                        R<SysUser> userInfo = userService.getUserCenterInfo(teamMember.getUserId(), SecurityConstants.INNER);
                        if(userInfo.getCode() == HttpStatus.SUCCESS && userInfo.getData() !=  null){
                            AuthInfo authInfo = userInfo.getData().getAuthInfo();
                            if(authInfo != null){
                                teamMember.setUserName(authInfo.getRealName());
                            }
                        }
                    });
                }
//                teamManager.setTeamMemberRelaList(teamMemberRelaList);
                // 指导老师翻译出名称
//                List<String> teacherNameList = new ArrayList<>();
//                if(StringUtils.isNotEmpty(teamManager.getGuideTeacher())){
//                    List<String> teacherIdList = Arrays.asList(teamManager.getGuideTeacher().split(","));
//                    teacherIdList.stream().forEach(teacherId -> {
//                        // 判断字符是春数字还是其他组合
//                        if(StringNumberUtil.isNumber(teacherId)){
//                            SysUser sysUser = userService.getUserCenterInfo(Long.valueOf(teacherId), SecurityConstants.INNER).getData();
//                            if(Objects.nonNull(sysUser)){
//                                if (sysUser.getAuthInfo() != null) {
//                                    teacherNameList.add(sysUser.getAuthInfo().getRealName());
//                                }else {
//                                    teacherNameList.add(sysUser.getUserName());
//                                }
//                            }else {
//                                teacherNameList.add(teacherId);
//                            }
//                        } else {
//                            teacherNameList.add(teacherId);
//                        }
//                    });
//                }
//                teamManager.setGuideTeacherName(String.join(",", teacherNameList));
            });
        }
        return teamManagerInfos;
    }

    /**
     * 新增团队管理
     *
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    @Override
    public int insertTeamManagerInfo(TeamManagerInfo teamManagerInfo) {

        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        teamManagerInfo.setTeamLeaderId(sysUserInfo.getUserId());
        teamManagerInfo.setUserId(sysUserInfo.getUserId());
        teamManagerInfo.setOrgId(sysUserInfo.getOrgId());
        teamManagerInfo.setCreateBy(sysUserInfo.getUserId()+"");
        // 创建团队，校验队长是否实名认证或身份认证 isRealNameAuth
        R<SysUser> userInfo = userService.getUserCenterInfo(teamManagerInfo.getTeamLeaderId(), SecurityConstants.INNER);
        if(userInfo.getCode() != HttpStatus.SUCCESS && userInfo.getData() ==  null){
            throw new GlobalException("用户不存在");
        }
        SysUser sysUser = userInfo.getData();
        // 获取赛道配置信息,到新建组别赛道一定都是审核通过的
        CompetitionTrackConfig competitionTrackConfigRes = new CompetitionTrackConfig();
        competitionTrackConfigRes.setSecondLevelCode(teamManagerInfo.getSecondLevelCode());
        competitionTrackConfigRes.setCheckStatus(Constants.CHECK_PASS);
        CompetitionTrackConfig competitionTrackConfig =
                competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfigRes);
        // 同一个赛道同一个组别不能重复创建团队，同一个赛道内不能重复创建多个团队
        TeamManagerInfo teamManagerInfoRes = new TeamManagerInfo();
        teamManagerInfoRes.setTeamLeaderId(teamManagerInfo.getTeamLeaderId());
        teamManagerInfoRes.setCompetitionSeriesId(teamManagerInfo.getCompetitionSeriesId());
        teamManagerInfoRes.setCompetitionTrackId(competitionTrackConfig.getCompetitionTrackId());
        List<TeamManagerInfo> teamOldManagerInfo = teamManagerInfoMapper.selectTeamManagerInfoList(teamManagerInfoRes);
        if(CollectionUtils.isNotEmpty(teamOldManagerInfo)){
            throw new GlobalException("该用户已创建团队");
        }
        if(Objects.nonNull(competitionTrackConfig) && Objects.nonNull(competitionTrackConfig.getCompetitionConfig())){
            competitionApplyInfoCheckService.checkAuth(competitionTrackConfig.getCompetitionConfig(),sysUser);
            competitionApplyInfoCheckService.checkStudent(competitionTrackConfig.getCompetitionConfig(),sysUser);
        }
        // 团队信息入库
        teamManagerInfo.setCreateTime(DateUtils.getNowDate());
        teamManagerInfo.setTeamCode(UUIDUtils.getUUID());
        teamManagerInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        if (sysUser.getAuthInfo() != null) {
            teamManagerInfo.setCaptainName(sysUser.getAuthInfo().getRealName());
        } else {
            teamManagerInfo.setCaptainName(sysUser.getUserName());
        }
        teamManagerInfo.setOrgId(SecurityUtils.getLoginUser().getSysUser().getOrgId());
        teamManagerInfo.setCheckStatus(Constants.NO_CHECK);
        teamManagerInfoMapper.insertTeamManagerInfo(teamManagerInfo);
        // 队长和团队入关联关系表
        TeamMemberRela teamMemberRela = new TeamMemberRela();
        teamMemberRela.setTeamCode(teamManagerInfo.getTeamCode());
        teamMemberRela.setUserId(teamManagerInfo.getTeamLeaderId());
        teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
        // 新增审核任务
//        orderService.innerAddAuditTask(TdConstants.AUDIT_FLOW_TYPE_TEAM, teamManagerInfo.getTeamId(),SecurityConstants.INNER);
        return teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
    }

    @Override
    public int applyJoinTeam(UserApplyTeam userApplyTeam) {
        TeamMemberRela teamMemberRela = new TeamMemberRela();
        BeanUtils.copyProperties(userApplyTeam, teamMemberRela);
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        teamMemberRela.setUserId(sysUserInfo.getUserId());
        teamMemberRela.setOrgId(sysUserInfo.getOrgId());
        teamMemberRela.setCreateBy(sysUserInfo.getUserId()+"");
        // 校验是否实名认证或身份认证
        R<SysUser> userInfo = userService.getUserCenterInfo(teamMemberRela.getUserId(), SecurityConstants.INNER);
        if(userInfo.getCode() != HttpStatus.SUCCESS && userInfo.getData() ==  null){
            throw new GlobalException("用户不存在");
        }
        SysUser sysUser = userInfo.getData();
        // 团队审核通过
        TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.selectTeamManagerInfoByTeamCode(null,teamMemberRela.getTeamCode());
        if(!Constants.CHECK_PASS.equals(teamManagerInfo.getCheckStatus())){
            throw new GlobalException("团队审核通过够可加入");
        }
        // 团队是否已经报名成功
        CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
        applyInfo.setCompetitionSeriesId(userApplyTeam.getCompetitionSeriesId());
        // 团队报名队长userId和团队code
        applyInfo.setUserId(teamManagerInfo.getTeamLeaderId());
        applyInfo.setCheckStatus(Constants.CHECK_PASS);
        applyInfo.setTeamCode(userApplyTeam.getTeamCode());
        applyInfo.setCheckStatusContain("Y");
        CompetitionApplyInfo competitionApplyInfo = competitionApplyInfoMapper.selectCompetitionApplyInfoByUserId(applyInfo);
        if(competitionApplyInfo!=null){
            throw new GlobalException("该团队已报名，不允许再加入团队");
        }
        // 团队人数是否达到赛事设置人数
        // 获取赛事信息
        CompetitionMainInfoReq competitionMainInfoReq = new CompetitionMainInfoReq();
        competitionMainInfoReq.setCompetitionId(userApplyTeam.getCompetitionId());
        competitionMainInfoReq.setCompetitionSeriesId(userApplyTeam.getCompetitionSeriesId());
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(competitionMainInfoReq);
        if(CollectionUtils.isEmpty(competitionDetailInfoList)){
            throw new GlobalException("不存在赛事信息");
        }
        CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
//        if (competitionDetailInfo.getIsRealNameAuth().equals(Constants.IS_YES)) {
//            if (!Constants.AUTH_STATUS_PASS.equals(sysUser.getAuthStatus())) {
//                throw new GlobalException("未进行实名认证");
//            }
//        }
//        // 是否是学生
//        if (competitionDetailInfo.getIsStudent().equals(Constants.IS_YES)) {
//            IdentityInfo identityInfo = sysUserInfo.getIdentityInfoList().get(0);
//            if (!Constants.IDENTITY_AUTH_PASS.equals(identityInfo.getCheckStatus())) {
//                throw new GlobalException("未进行身份认证或身份认证正在审核");
//            }
//            List<String> identityInfoList = sysUser.getIdentityInfoList().stream().map(IdentityInfo::getCertificationType)
//                    .collect(Collectors.toList());
//            if (!identityInfoList.contains(Constants.IDENTITY_TYPE_STUDENT)) {
//                throw new GlobalException("参赛人员必须是学生");
//            }
//        }
//        if(StringUtils.isNotEmpty(competitionDetailInfo.getMaxPernNum()) &&
//            Integer.valueOf(competitionDetailInfo.getMaxPernNum()) <= teamMemberRelaMapper.selectTeamMemberRelaCountByTeamCode(userApplyTeam.getTeamCode())){
//            throw new GlobalException("团队人数已满");
//        }
        // 已经是团队成员不允许加入
        TeamMemberRela team = teamMemberRelaMapper.selectTeamMemberRelaByRelaId(teamMemberRela.getUserId(),teamMemberRela.getTeamCode());
        if(team !=null){
            throw new GlobalException("您已经申请该团队或者已经是该团队成员");
        }
        // 如果已拒绝再申请，状态变更为等待
        teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_REJECT);
        List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
        if(CollectionUtils.isNotEmpty(teamMemberRelaList)){
            TeamMemberRela teamMemberRela1 = teamMemberRelaList.get(0);
            teamMemberRela1.setCheckStatus(Constants.JOIN_TEAM_WAIT);
            teamMemberRela1.setUpdateTime(DateUtils.getNowDate());
            return teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela1);
        }
        teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_WAIT);
        teamMemberRela.setCreateTime(DateUtils.getNowDate());
        return teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
    }

    /**
     * 修改团队管理
     *
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    @Override
    public int updateTeamManagerInfo(TeamManagerInfo teamManagerInfo)
    {
        SysUser sysUserInfo = SecurityUtils.getLoginUser().getSysUser();
        teamManagerInfo.setUpdateBy(sysUserInfo.getUserId()+"");
        teamManagerInfo.setUpdateTime(DateUtils.getNowDate());
        teamManagerInfo.setCheckStatus(Constants.NO_CHECK);
        teamManagerInfoMapper.updateTeamManagerInfo(teamManagerInfo);
        // 新增审核任务
        orderService.innerAddAuditTask(TdConstants.AUDIT_FLOW_TYPE_TEAM, teamManagerInfo.getTeamId(),SecurityConstants.INNER);
        return 1;
    }

    /**
     * 批量删除团队管理
     *
     * @param teamCodes 需要删除的团队管理主键
     * @return 结果
     */
    @Override
    public int deleteTeamManagerInfoByTeamCodes(String[] teamCodes)
    {
        return teamManagerInfoMapper.deleteTeamManagerInfoByTeamCodes(teamCodes);
    }

    /**
     * 删除团队管理信息
     *
     * @param teamCode 团队管理主键
     * @return 结果
     */
    @Override
    public int deleteTeamManagerInfoByTeamCode(String teamCode)
    {
        return teamManagerInfoMapper.deleteTeamManagerInfoByTeamCode(teamCode);
    }

    @Override
    public TeamManagerInfo selectTeamMemberList(Long userId, Long competitionSeriesId) {
        // 获取创建团队
        TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.selectTeamMemberRelaByTeamLeaderId(userId, competitionSeriesId);
        if(teamManagerInfo != null){
            TeamMemberRela teamMemberRela = new TeamMemberRela();
            teamMemberRela.setTeamCode(teamManagerInfo.getTeamCode());
//            List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
            List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamManagerInfo.getTeamCode());
            // 团队成员列表(去除队长)
            List<CompetitionApplyInfo> teamMemberList = new ArrayList<>();
            // 获取队长或者团团员真实姓名
            if(CollectionUtils.isNotEmpty(applyInfoList)){
                applyInfoList.stream().forEach(teamMember -> {
                    if(!(Objects.equals(teamManagerInfo.getTeamLeaderId(), teamMember.getUserId()))){
                        R<SysUser> userCenterInfo = userService.getUserCenterInfo(teamMember.getUserId(), SecurityConstants.INNER);
                        if (userCenterInfo.getCode() == HttpStatus.SUCCESS) {
                            SysUser data = userCenterInfo.getData();
                            if(Objects.nonNull(data)){
                                teamMember.setPhone(data.getPhonenumber());
                                teamMember.setEmail(data.getEmail());
                                teamMember.setSex(data.getSex());
                                teamMember.setOrgName(data.getOrg()==null?null:data.getOrg().getOrgName());
                                teamMember.setUserName(data.getUserName());
                                if (Objects.nonNull(data.getAuthInfo())) {
                                    teamMember.setUserName(data.getAuthInfo().getRealName());
                                }
                            }
                        }
                        teamMemberList.add(teamMember);
                    }
                });
            }
            teamManagerInfo.setTeamMemberRelaList(teamMemberList);
        }
        return teamManagerInfo;
    }

    @Override
    public List<TeamManagerInfo> selectTeamManagerInfo(TeamManagerInfo teamManagerInfo) {
        return teamManagerInfoMapper.selectCompetitionTeam(teamManagerInfo);
    }

    @Override
    public int updateTeamManagerStatus(TeamManagerInfo teamManagerInfo) {
        teamManagerInfo.setUpdateTime(DateUtils.getNowDate());
        return teamManagerInfoMapper.updateTeamManagerInfo(teamManagerInfo);
    }
}
