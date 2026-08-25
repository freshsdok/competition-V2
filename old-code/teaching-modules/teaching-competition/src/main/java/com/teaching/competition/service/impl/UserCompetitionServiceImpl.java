package com.teaching.competition.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.constant.*;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.ServletUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.*;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.*;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteFlowService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.teaching.common.core.constant.DictConstant.RETIRED;

/**
 * @ClassName UserCompetitionServiceImpl
 * @Description 用户赛事服务
 * @Author LJL
 * @Date 2025/10/23 15:05
 * @Version 1.0
 **/
@Service
public class UserCompetitionServiceImpl implements UserCompetitionService {

    private static final Logger log = LoggerFactory.getLogger(UserCompetitionServiceImpl.class);

    // 只读场景
    private static final List<String> allowedUserTypesTeacher = Arrays.asList("leaderTeacher", "guidTeacher");
    private static final List<String> allowedUserTypesMember = Arrays.asList("teamLeader", "teamMember");

    // 判断用户名是邮箱还是手机号
    public static final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // 手机号正则
    public static final String phoneRegex = "^1\\d{10}$";

    private static final Set<String> TEACHER_COMPETITION_GROUP_NAMES = Set.of(
            "职业组（含中职）",
            "本科组"
    );

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CompetitionApplyInfoCheckService competitionApplyInfoCheckService;

    @Autowired
    private TeamMemberRelaMapper teamMemberRelaMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TeamManagerInfoMapper teamManagerInfoMapper;

    @Autowired
    private CompetitionSeriesInfoMapper competitionSeriesInfoMapper;

    @Autowired
    private CompetitionWorksMapper competitionWorksMapper;

    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    @Autowired
    private CompetitionMainInfoMapper competitionMainInfoMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private ICompetitionCheckDataPackageService competitionCheckDataPackageService;

    @Autowired
    private IOperationTimesService operationTimesService;

    @Autowired
    private IChangeLogService changeLogService;

    @Autowired
    private OperationConfigMapper operationConfigMapper;

    @Autowired
    private RemoteFlowService flowService;

    @Autowired
    private ChangeLogMapper changeLogMapper;

    @Override
    public int userApplyCompetitionInfo(UserApplyCompetitionReq req) {
        // 用户中心拿最新用户信息
        R<SysUser> userCenterInfo = userService.getUserCenterInfo(SecurityUtils.getLoginUser().getSysUser().getUserId(), SecurityConstants.INNER);
        if (!R.isSuccess(userCenterInfo)) {
            throw new GlobalException("用户信息获取失败");
        }
        SysUser sysUserInfo = userCenterInfo.getData();
        // 当前登录信息为带队老师身份
        competitionApplyInfoCheckService.checkLeaderGroupTeacher(sysUserInfo);
        // 获取赛事信息
        CompetitionMainInfoReq competitionMainInfoReq = new CompetitionMainInfoReq();
        competitionMainInfoReq.setCompetitionId(req.getCompetitionId());
        competitionMainInfoReq.setCompetitionSeriesId(req.getCompetitionSeriesId());
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(competitionMainInfoReq);
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            throw new GlobalException("赛事信息不存在");
        }
        // 赛道审核通过后，校验开始生效
        CompetitionTrackInfo competitionTrackInfoReq = new CompetitionTrackInfo();
        competitionTrackInfoReq.setCompetitionTrackId(req.getCompetitionTrackId());
        competitionTrackInfoReq.setCheckStatus(Constants.CHECK_PASS);
        List<CompetitionTrackInfo> competitionTrackInfoList =
                competitionTrackInfoMapper.selectCompetitionTrackInfoByUser(competitionTrackInfoReq);
        if (CollectionUtils.isNotEmpty(competitionTrackInfoList)) {
            // 如果入参为赛道主键可确定只有一条赛道信息
            CompetitionTrackInfo competitionTrackInfo = competitionTrackInfoList.get(0);
            if (Objects.nonNull(competitionTrackInfo) && competitionTrackInfo.getCheckStatus().equals(Constants.CHECK_PASS)) {
                // 获取赛道下二级分类中赛项配置信息
                // 判断当前用户是否参加本赛道二级分类信息
                List<CompetitionTrackConfig> competitionTrackConfigList = competitionTrackInfo.getCompetitionTrackConfigList();
                if (CollectionUtils.isNotEmpty(competitionTrackConfigList)) {
                    for (CompetitionTrackConfig competitionTrackConfig : competitionTrackConfigList) {
                        // 同一个人或者同一个组再同一个赛道下不能重复报名
                        if (req.getCompetitionTrackId().equals(competitionTrackConfig.getCompetitionTrackId())) {
                            CompetitionConfig competitionConfig = competitionTrackConfig.getCompetitionConfig();
                            // 报名校验
                            competitionApplyInfoCheckService.checkApplyCompetition(competitionConfig, req, sysUserInfo);
                            // 获取赛事设置赛事参赛方式
                            String joinType = competitionConfig.getJoinType();
                            if (StringUtils.isEmpty(joinType)) {
                                throw new GlobalException("赛事信息中未设置参赛方式");
                            }
                        }
                    }
                }
            }
        }
        // 带队老师报名
//        // 赛事参赛方式是个人参赛
//        if(Constants.JOIN_TYPE_PERSON.equals(joinType)){
//            userApplyCompetitionInfo(competitionConfig,req,sysUserInfo);
//        }
//        // 赛事参赛方式是团队参赛
//        if(Constants.JOIN_TYPE_TEAM.equals(joinType)){
//            TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.selectTeamMemberRelaByTeamLeaderId(sysUserInfo.getUserId(), req.getCompetitionSeriesId());
//            if(teamManagerInfo == null){
//                throw new GlobalException("当前赛事参赛方式为团队，请加入团队或者创建团队");
//            }
//            req.setTeamCode(teamManagerInfo.getTeamCode());
//            teamApplyCompetitionInfo(competitionConfig,req,sysUserInfo);
//        }
//        // 赛事参赛方式是人数不限(支持个人和团体同时参赛)
//        if (Constants.JOIN_TYPE_NO_LIMIT.equals(joinType)) {
//            // 获取当前用户是否有队伍
//            TeamManagerInfo teamManagerInfo = teamManagerInfoMapper.selectTeamMemberRelaByTeamLeaderId(sysUserInfo.getUserId(), req.getCompetitionSeriesId());
//            if(teamManagerInfo!=null){
//                req.setTeamCode(teamManagerInfo.getTeamCode());
//            }
//            if(StringUtils.isNotEmpty(req.getTeamCode())){
//                teamApplyCompetitionInfo(competitionConfig,req,sysUserInfo);
//            } else {
//                userApplyCompetitionInfo(competitionConfig,req,sysUserInfo);
//            }
//        }
        return 1;
    }

    @Override
    public int agreeJoinTeam(Map<String, String> param) {
        // 团队code
        String teamCode = param.get("teamCode");
        if (StringUtils.isEmpty(teamCode)) {
            throw new GlobalException("团队不能为空");
        }
        String userIds = param.get("userIds");
        if (StringUtils.isEmpty(userIds)) {
            throw new GlobalException("队员不能为空");
        }
        // competitionSeriesId
        if (Objects.isNull(param.get("competitionSeriesId"))) {
            throw new GlobalException("赛事不能为空");
        }
        Long competitionSeriesId = Long.parseLong(param.get("competitionSeriesId"));
        String checkStatus = param.get("checkStatus");
        List<String> userIdList = Arrays.asList(StringUtils.split(userIds, ","));
        List<TeamMemberRela> teamMemberRelaList = new ArrayList<>();
        for (String userId : userIdList) {
            TeamMemberRela teamMemberRela = new TeamMemberRela();
            teamMemberRela.setTeamCode(teamCode);
            teamMemberRela.setUserId(Long.parseLong(userId));
            teamMemberRela.setCheckStatus(checkStatus);
            teamMemberRelaList.add(teamMemberRela);
        }
        // 获取赛事信息
        CompetitionMainInfoReq competitionMainInfoReq = new CompetitionMainInfoReq();
        competitionMainInfoReq.setCompetitionSeriesId(competitionSeriesId);
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(competitionMainInfoReq);
        // 同意校验团队团员是否超过限制
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            throw new GlobalException("赛事信息不存在");
        }
        CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
//        if("2".equals(checkStatus)){
//            if(StringUtils.isNotEmpty(competitionDetailInfo.getMaxPernNum()) &&
//                    Integer.valueOf(competitionDetailInfo.getMaxPernNum()) <= teamMemberRelaMapper.selectTeamMemberRelaCountByTeamCode(teamCode)){
//                throw new GlobalException("团队人数已满");
//            }
//        }
        return teamMemberRelaMapper.updateTeamStatus(teamMemberRelaList);
    }

    @Override
    public List<TeamManagerInfo> selectTeamInfoByUserId(Long competitionSeriesId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        List<TeamManagerInfo> teamManagerInfos = teamManagerInfoMapper.selectTeamInfoByUserId(userId, competitionSeriesId);
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            teamManagerInfos.stream().forEach(teamManagerInfo -> {
                // 如果当前登录人是否为队长
                if (teamManagerInfo.getTeamLeaderId().equals(userId)) {
                    teamManagerInfo.setCaptainFlag(true);
                } else {
                    // 不是团队队长是团员加入团队的状态
                    teamManagerInfo.setCaptainFlag(false);
                }
            });
        }
        return teamManagerInfos;
    }

    @Override
    public CompetitionApplyAllStatus checkCompetitionApplyStatusByUser(UserApplyCompetitionReq userApplyCompetitionReq) {
        CompetitionApplyAllStatus competitionApplyAllStatusRes = new CompetitionApplyAllStatus();
        // 获取赛事赛道配置详情信息
        // 获取赛事报名费用
        CompetitionSeriesInfo competitionSeriesInfo =
                competitionSeriesInfoMapper.selectCompetitionSeriesInfoByCompetitionSeriesId(null, userApplyCompetitionReq.getCompetitionSeriesId());
        List<CompetitionConfig> competitionConfigs = competitionConfigMapper.selectCompetitionConfigList(competitionSeriesInfo.getCompetitionSeriesId());
        // 判断个人还是团队
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        competitionApplyInfo.setUserId(userId);
        competitionApplyInfo.setCompetitionSeriesId(userApplyCompetitionReq.getCompetitionSeriesId());
        CompetitionApplyInfo applyInfo = competitionApplyInfoMapper.selectCompetitionApplyInfoByUserId(competitionApplyInfo);
        competitionApplyAllStatusRes.setCompetitionSeriesId(userApplyCompetitionReq.getCompetitionSeriesId());
        competitionApplyAllStatusRes.setUserId(userId);
        if (Objects.nonNull(applyInfo)) {
            competitionApplyAllStatusRes.setCompetitionTrackName(applyInfo.getCompetitionTrackName());
//            competitionApplyAllStatusRes.setGroupClassify(applyInfo.getGroupClassify());
            // 查询当前是否报名及上传过作品
            CompetitionWorks competitionWorks = new CompetitionWorks();
            competitionWorks.setUserId(userId);
            competitionWorks.setCompetitionSeriesId(userApplyCompetitionReq.getCompetitionSeriesId());
            List<CompetitionWorks> competitionWorkList = competitionWorksMapper.selectCompetitionWorksList(competitionWorks);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(competitionWorkList)) {
                competitionApplyAllStatusRes.setWorksFlag("1");
                competitionWorkList.forEach(competitionWork -> {
                    competitionApplyAllStatusRes.setWorksStatus(competitionWork.getWorksStatus());
                });
            } else {
                competitionApplyAllStatusRes.setWorksFlag("0");
            }
            // 赛事上传作品是否已截至
            if (CollectionUtils.isNotEmpty(competitionConfigs)) {
                CompetitionConfig competitionConfig = competitionConfigs.get(0);
                competitionApplyAllStatusRes.setApplyTimeFlag(true);
                // 报名时间还未开始
                if (competitionConfig.getApplyStartTime() != null && competitionConfig.getApplyStartTime().getTime() > System.currentTimeMillis()) {
                    competitionApplyAllStatusRes.setApplyTimeFlag(false);
                }
                // 报名时间结束
                if (competitionConfig.getApplyEndTime() != null && competitionConfig.getApplyEndTime().getTime() <= System.currentTimeMillis()) {
                    competitionApplyAllStatusRes.setApplyTimeFlag(false);
                }
                competitionApplyAllStatusRes.setWorksSubmitFlag(false);
                if (competitionConfig.getWorksSubmitDate() != null && competitionConfig.getWorksSubmitDate().getTime() < System.currentTimeMillis()) {
                    competitionApplyAllStatusRes.setWorksSubmitFlag(true);
                }
            }
        }
        // 说明是个人
        if (applyInfo != null && StringUtils.isEmpty(applyInfo.getTeamCode())) {
            competitionApplyAllStatusRes.setFlag("1");
            if (applyInfo.getCheckStatus().equals(Constants.CHECK_PASS)) {
//                competitionApplyAllStatusRes.setAmount(competitionSeriesInfo.getFee());
                competitionApplyAllStatusRes.setUserId(userId);
                // 获取支付状态
                R<OrderInfo> orderByUserIdRes = orderService.getOrderByUserIdAndCommodityId(userId, userApplyCompetitionReq.getCompetitionSeriesId() + "", SecurityConstants.INNER);
                if (orderByUserIdRes.getCode() == HttpStatus.SUCCESS && orderByUserIdRes.getData() != null) {
                    OrderInfo orderInfo = orderByUserIdRes.getData();
                    competitionApplyAllStatusRes.setApplyStatus(orderInfo.getPayStatus());
                } else {
                    competitionApplyAllStatusRes.setApplyStatus(applyInfo.getCheckStatus());
                }
            } else {
                competitionApplyAllStatusRes.setApplyStatus(applyInfo.getCheckStatus());
                competitionApplyAllStatusRes.setApplyReason(orderService.innerGetCheckOpinion(TdConstants.AUDIT_FLOW_TYPE_APPLY, applyInfo.getMemberId(), SecurityConstants.INNER).getData());
            }
        }
        // 队长
        else if (applyInfo != null && StringUtils.isNotEmpty(applyInfo.getTeamCode())) {
            competitionApplyAllStatusRes.setFlag("2");
            competitionApplyAllStatusRes.setTeamCode(applyInfo.getTeamCode());
            competitionApplyAllStatusRes.setTeamLeaderId(userId);
            competitionApplyAllStatusRes.setTeamLeaderName(applyInfo.getUserName());
            if (applyInfo.getCheckStatus().equals(Constants.CHECK_PASS)) {
                // 计算支付金额，获取团队团员数量
//                BigDecimal fee = new BigDecimal(competitionSeriesInfo.getFee());
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
                List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
//                competitionApplyAllStatusRes.setAmount(fee.multiply(new BigDecimal(teamMemberRelaList.size())).toString());
                // 获取支付状态
                R<OrderInfo> orderByUserIdRes = orderService.getOrderByUserIdAndCommodityId(userId, userApplyCompetitionReq.getCompetitionSeriesId() + "", SecurityConstants.INNER);
                if (orderByUserIdRes.getCode() == HttpStatus.SUCCESS && orderByUserIdRes.getData() != null) {
                    OrderInfo orderInfo = orderByUserIdRes.getData();
                    competitionApplyAllStatusRes.setApplyStatus(orderInfo.getPayStatus());
                } else {
                    competitionApplyAllStatusRes.setApplyStatus(applyInfo.getCheckStatus());
                }
            } else {
                competitionApplyAllStatusRes.setApplyStatus(applyInfo.getCheckStatus());
                competitionApplyAllStatusRes.setApplyReason(orderService.innerGetCheckOpinion(TdConstants.AUDIT_FLOW_TYPE_APPLY, applyInfo.getMemberId(), SecurityConstants.INNER).getData());
            }
        } else {
            // 是团员,反查队长信息及队长是否缴费
            CompetitionApplyAllStatus competitionApplyAllStatus = new CompetitionApplyAllStatus();
            competitionApplyAllStatus.setUserId(userId);
            competitionApplyAllStatus.setCompetitionSeriesId(userApplyCompetitionReq.getCompetitionSeriesId());
            CompetitionApplyAllStatus competitionApply = teamMemberRelaMapper.selectUserTeamStatus(competitionApplyAllStatus);
            if (competitionApply != null) {
                CompetitionApplyInfo competitionApplyInfoLeader = new CompetitionApplyInfo();
                competitionApplyInfoLeader.setUserId(competitionApply.getTeamLeaderId());
                competitionApplyInfoLeader.setCompetitionSeriesId(competitionApply.getCompetitionSeriesId());
                competitionApplyInfoLeader.setTeamCode(competitionApply.getTeamCode());
                CompetitionApplyInfo applyInfoLeader = competitionApplyInfoMapper.selectCompetitionApplyInfoByUserId(competitionApplyInfoLeader);
                if (applyInfoLeader != null) {
                    competitionApplyAllStatusRes.setFlag("3");
                    competitionApplyAllStatusRes.setTeamLeaderId(applyInfoLeader.getUserId());
                    competitionApplyAllStatusRes.setTeamLeaderName(applyInfoLeader.getUserName());
                    if (applyInfoLeader.getCheckStatus().equals(Constants.CHECK_PASS)) {
                        // 获取支付状态
                        R<OrderInfo> orderByUserIdRes = orderService.getOrderByUserIdAndCommodityId(userId, userApplyCompetitionReq.getCompetitionSeriesId() + "", SecurityConstants.INNER);
                        OrderInfo data = orderByUserIdRes.getData();
                        if (data == null) {
                            competitionApplyAllStatusRes.setApplyStatus(applyInfoLeader.getCheckStatus());
                            competitionApplyAllStatusRes.setApplyReason(orderService.innerGetCheckOpinion(TdConstants.AUDIT_FLOW_TYPE_APPLY, applyInfoLeader.getMemberId(), SecurityConstants.INNER).getData());
                        } else {
                            competitionApplyAllStatusRes.setApplyStatus(data.getPayStatus());
                        }
                    } else {
                        competitionApplyAllStatusRes.setApplyStatus(applyInfoLeader.getCheckStatus());
                        competitionApplyAllStatusRes.setApplyReason(orderService.innerGetCheckOpinion(TdConstants.AUDIT_FLOW_TYPE_APPLY, applyInfoLeader.getMemberId(), SecurityConstants.INNER).getData());
                    }
                }
            }
        }
        return competitionApplyAllStatusRes;
    }

    // 赛事赛道详细信息
    @Override
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoByCompetitionSeriesId(Long competitionSeriesId) {
        List<CompetitionTrackInfo> competitionTrackInfoList =
                competitionTrackInfoMapper.selectCompetitionTrackInfoByCompetitionSeriesId(competitionSeriesId);
        if (CollectionUtils.isEmpty(competitionTrackInfoList)) {
            throw new RuntimeException("赛道还未开放，请耐心等待");
        }
        // 参赛方式翻译
        Map<String, SysDictData> joinTypeMap = new HashMap();
        R<List<SysDictData>> joinTypeListRes = userService.dictType("join_type", SecurityConstants.INNER);
        if (joinTypeListRes.getCode() == HttpStatus.SUCCESS) {
            List<SysDictData> joinTypeList = joinTypeListRes.getData();
            if (CollectionUtils.isNotEmpty(joinTypeList)) {
                joinTypeMap = joinTypeList.stream().
                        collect(Collectors.toMap(SysDictData::getDictValue, SysDictData -> SysDictData));
            }
        }
        Map<String, SysDictData> finalJoinTypeMap = joinTypeMap;
        competitionTrackInfoList.stream().forEach(competitionTrackInfo -> {
            List<CompetitionTrackConfig> competitionTrackConfigList = competitionTrackInfo.getCompetitionTrackConfigList();
            if (CollectionUtils.isNotEmpty(competitionTrackConfigList)) {
                competitionTrackConfigList.stream().forEach(competitionTrackConfig -> {
                    CompetitionConfig competitionConfig = competitionTrackConfig.getCompetitionConfig();
                    if (Objects.nonNull(competitionConfig.getJoinType())) {
                        competitionConfig.setJoinTypeCn(finalJoinTypeMap.get(competitionConfig.getJoinType()).getDictLabel());
                    }
                });
            }
        });
        return competitionTrackInfoList;
    }

    @Override
    public List<CompetitionMainInfo> selectUserCompetitionMainInfoList(CompetitionMainInfoReq req) {
        List<CompetitionMainInfo> competitionMainInfoList = competitionMainInfoMapper.selectCompetitionMainInfoList(req);
        if (CollectionUtils.isNotEmpty(competitionMainInfoList)) {
            competitionMainInfoList.stream().forEach(competitionMainInfo -> {
                // 赛事收藏数量
                Map<String, Object> params = new HashMap();
                params.put("competitionId", competitionMainInfo.getCompetitionId());
                params.put("competitionSeriesId", competitionMainInfo.getCompetitionSeriesId());
                Integer competitionCount = userCollectMapper.selectCollectCompetitionCount(params);
                competitionMainInfo.setCompetitionCollectNum(
                        competitionCount == null ? 0 : competitionCount);
                // 赛事分享数量
                Integer shareNum = redisService.getCacheObject(competitionMainInfo.getCompetitionId() + "");
                competitionMainInfo.setCompetitionShareNum(shareNum == null ? 0 : shareNum);
            });
        }
        return competitionMainInfoList;
    }

    @Override
    public List<CompetitionApplyInfo> importApplyCompetitionData(List<CompetitionApplyInfo> applyInfoList, boolean updateSupport, Long userId, UserApplyCompetitionReq req) throws Exception {
        if (CollectionUtils.isEmpty(applyInfoList)) {
            return new ArrayList<>();
        }
        // 普通赛沿用账号手机号/邮箱校验；教师赛选手允许尚未注册账号。
        List<CompetitionApplyInfo> ordinaryApplyInfoList = applyInfoList.stream()
                .filter(applyInfo -> !ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(
                        applyInfo.getCompetitionTrackName()))
                .toList();
        if (CollectionUtils.isNotEmpty(ordinaryApplyInfoList)) {
            checkUpdatePhoneAndEmail(ordinaryApplyInfoList, req.getCompetitionSeriesId());
        }
        // 如果报名表存在数据则不能二次导入excel
//        int count = competitionApplyInfoMapper.selectCompetitionApplyInfoByExitPayStatus(req.getCompetitionSeriesId(), userId);
//        if (count > 0) {
//            throw new GlobalException("该赛事报名信息存在订单，请勿重复导入");
//        }
        SysUser loginUser = SecurityUtils.getLoginUser().getSysUser();
        SysUser uploaderUser = loginUser;
        NationwideCollegeInfo uploaderCollege = null;
        // 普通比赛仍要求上传者具备教师身份；纯教师赛只要求登录。
        if (CollectionUtils.isNotEmpty(ordinaryApplyInfoList)) {
            R<SysUser> userCenterInfo = userService.getUserCenterInfo(loginUser.getUserId(), SecurityConstants.INNER);
            if (!R.isSuccess(userCenterInfo) || Objects.isNull(userCenterInfo.getData())) {
                throw new GlobalException("用户信息获取失败，请稍后重试");
            }
            uploaderUser = userCenterInfo.getData();
            List<IdentityInfo> identityInfoList = uploaderUser.getIdentityInfoList();
            if (CollectionUtils.isEmpty(identityInfoList)) {
                throw new GlobalException("您未进行身份认证，不能报名普通赛，请先完成教师身份认证");
            }
            IdentityInfo teacherIdentity = identityInfoList.stream()
                    .filter(identityInfo -> Constants.IDENTITY_TYPE_TEACHER.equals(identityInfo.getCertificationType()))
                    .findFirst()
                    .orElseThrow(() -> new GlobalException("您的身份不是带队老师，不能报名普通赛"));
            R<NationwideCollegeInfo> collegeResult = userService.getNationwideCollegeInfoInfo(
                    teacherIdentity.getSchool(), SecurityConstants.INNER);
            if (R.isSuccess(collegeResult)) {
                uploaderCollege = collegeResult.getData();
            }
        }
        final SysUser finalUploaderUser = uploaderUser;
        final NationwideCollegeInfo finalUploaderCollege = uploaderCollege;
        List<String> secondLevelNameList = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getSecondLevelName())).
                map(applyInfo -> applyInfo.getSecondLevelName()).distinct().collect(Collectors.toList());
        // 出现选题处理
        List<String> competitionQuestionList = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getCompetitionQuestion())).
                map(applyInfo -> applyInfo.getCompetitionQuestion()).distinct().collect(Collectors.toList());
        secondLevelNameList.addAll(competitionQuestionList);
//        if(R.isSuccess(nationwideCollegeInfoInfo)){
//            NationwideCollegeInfo data = nationwideCollegeInfoInfo.getData();
//            if((!"是".equals(data.getTwoOneOne()) || !"是".equals(data.getNineEightFive())) && secondLevelNameList.contains("本科B")){
//                throw new GlobalException("您选择的赛道组别不符合要求,本科B组普通本科院校可报名");
//            }
//        }
        // 根据名字查赛道组别信息  checkCompetitionTrackInfoByCompetitionSeriesId
        List<String> competitionTrackNameList = applyInfoList.stream().
                map(applyInfo -> applyInfo.getCompetitionTrackName()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(competitionTrackNameList)) {
            // 校验赛道组别有无配置
//            competitionTrackNameList.stream().forEach(competitionTrackName -> {
//                int count = competitionTrackInfoMapper.
//                        checkCompetitionTrackInfoByCompetitionSeriesId(req.getCompetitionSeriesId(), competitionTrackName);
//                if(count <= 0){
//                    throw new GlobalException(applyInfoList.get(0).getCompetitionName()+"赛道组别" + competitionTrackName + "不存在");
//                }
//            });
            String competitionTrackName = StringUtils.join(competitionTrackNameList, ",");
            CompetitionTrackInfo competitionTrackInfoReq = new CompetitionTrackInfo();
            competitionTrackInfoReq.setCompetitionTrackName(competitionTrackName);
            competitionTrackInfoReq.setCompetitionSeriesId(req.getCompetitionSeriesId());
            competitionTrackInfoReq.setCheckStatus(Constants.CHECK_PASS);
            List<CompetitionTrackInfo> competitionTrackInfos = competitionTrackInfoMapper.selectCompetitionTrackInfoByCompetitionTrackName(competitionTrackInfoReq);
            CompetitionMainInfoReq competitionMainInfoReq = new CompetitionMainInfoReq();
            competitionMainInfoReq.setCompetitionSeriesId(req.getCompetitionSeriesId());
            List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoMapper.selectCompetitionDetailInfoByCompetitionId(competitionMainInfoReq);
            String competitionName;
            if (CollectionUtils.isNotEmpty(competitionDetailInfoList)) {
                CompetitionDetailInfo competitionDetailInfo = competitionDetailInfoList.get(0);
                competitionName = competitionDetailInfo.getCompetitionSeriesName() + competitionDetailInfo.getCompetitionName();
            } else {
                competitionName = null;
            }
            if (CollectionUtils.isNotEmpty(competitionTrackInfos)) {
                Map<String, CompetitionTrackInfo> competitionTrackInfoMap = competitionTrackInfos.stream()
                        .collect(Collectors.toMap(CompetitionTrackInfo::getCompetitionTrackName, competitionTrackInfo -> competitionTrackInfo));
                if (CollectionUtils.isNotEmpty(applyInfoList)) {
                    applyInfoList.stream().forEach(applyInfo -> {
                        // 带队老师姓名入库
                        String uploaderName = Objects.nonNull(finalUploaderUser.getAuthInfo())
                                ? finalUploaderUser.getAuthInfo().getRealName()
                                : (StringUtils.isNotBlank(finalUploaderUser.getNickName())
                                ? finalUploaderUser.getNickName() : finalUploaderUser.getUserName());
                        applyInfo.setLeaderTeacher(uploaderName);
                        applyInfo.setLeaderTeacherPhone(finalUploaderUser.getPhonenumber());
                        // 名字处理
                        if (StringUtils.isNotEmpty(applyInfo.getCompetitionQuestion())) {
                            applyInfo.setSecondLevelName(applyInfo.getCompetitionQuestion().split("：")[1]);
                        }
                        if (StringUtils.isNotEmpty(applyInfo.getSecondLevelName()) && applyInfo.getSecondLevelName().contains("：")) {
                            applyInfo.setSecondLevelName(applyInfo.getSecondLevelName().split("：")[1]);
                        }
                        applyInfo.setCompetitionName(competitionName);
                        applyInfo.setCompetitionSeriesId(req.getCompetitionSeriesId());
                        applyInfo.setLeaderTeacherId(loginUser.getUserId());
                        CompetitionTrackInfo competitionTrackInfo = competitionTrackInfoMap.get(applyInfo.getCompetitionTrackName());
                        if (Objects.isNull(competitionTrackInfo) || StringUtils.isEmpty(competitionTrackInfo.getCompetitionTrackId())) {
                            throw new GlobalException(applyInfoList.get(0).getCompetitionName() + "下" + applyInfo.getCompetitionTrackName() + "未配置或未审核通过");
                        }
                        applyInfo.setCompetitionTrackId(competitionTrackInfo == null ? null : competitionTrackInfo.getCompetitionTrackId());
                        applyInfo.setCompetitionTrackType(competitionTrackInfo == null ? null : competitionTrackInfo.getCompetitionTrackType());
                        if (Objects.nonNull(competitionTrackInfo)) {
                            Map<String, CompetitionTrackConfig> competitionTrackConfigMap = competitionTrackInfo.getCompetitionTrackConfigList().
                                    stream().collect(Collectors.toMap(CompetitionTrackConfig::getSecondLevelName, a -> a));
                            CompetitionTrackConfig competitionTrackConfigInfo = competitionTrackConfigMap.get(applyInfo.getSecondLevelName());
                            if (Objects.isNull(competitionTrackConfigInfo) || StringUtils.isEmpty(competitionTrackConfigInfo.getSecondLevelCode())) {
                                throw new GlobalException(applyInfoList.get(0).getCompetitionName() + "下" + applyInfo.getCompetitionTrackName() + "中" + applyInfo.getSecondLevelName() + "未配置");
                            }
                            applyInfo.setSecondLevelCode(competitionTrackConfigInfo == null ? null : competitionTrackConfigInfo.getSecondLevelCode());
                        }
                        applyInfo.setCheckStatus(Constants.CHECK_PASS);
                        // 教师赛选手的组织信息来自 Excel，普通赛仍使用上传者认证学校。
                        if (!ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName())) {
                            applyInfo.setSchool(finalUploaderCollege == null ? null : finalUploaderCollege.getId());
                            applyInfo.setSchoolName(finalUploaderCollege == null ? null : finalUploaderCollege.getSchoolName());
                        }
                        if ("指导教师".equals(applyInfo.getCompetitionRoleName())) {
                            applyInfo.setGuideTeacher(applyInfo.getUserName());
                            applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
                            applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
                        }
                    });
                }
            } else {
                throw new GlobalException(competitionName + "下" + competitionTrackName + "未配置或未审核通过");
            }
        }
        validateAndBindTeacherCompetitionApplyInfo(applyInfoList, loginUser.getUserId());
        // 数据准确性校验 CompetitionApplyInfoServiceImpl
        checkData(applyInfoList, req.getCompetitionSeriesId(), "import");
        // 根据赛事赛道组别配置校验
//        checkExcelApplyData(applyInfoList, req.getCompetitionSeriesId());
        // 存redis
        redisService.setCacheObject(loginUser.getUserId() + "_" + req.getCompetitionSeriesId() + "", applyInfoList);
        log.info("applyInfoList:{}", redisService.getCacheObject(loginUser.getUserId() + "_" + req.getCompetitionSeriesId() + "").toString());
        return applyInfoList;
    }

    /**
     * 教师赛沿用团队报名表结构：选手可未注册，按身份证号去重，单位信息来自 Excel。
     */
    private void validateAndBindTeacherCompetitionApplyInfo(List<CompetitionApplyInfo> applyInfoList, Long uploaderUserId) {
        List<CompetitionApplyInfo> teacherApplyInfoList = applyInfoList.stream()
                .filter(applyInfo -> ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName()))
                .toList();
        if (CollectionUtils.isEmpty(teacherApplyInfoList)) {
            return;
        }

        for (CompetitionApplyInfo applyInfo : teacherApplyInfoList) {
            if (StringUtils.isBlank(applyInfo.getTeamCode())) {
                throw new GlobalException("教师赛团队编号缺失，请重新下载模板并填写");
            }
        }

        Map<String, List<CompetitionApplyInfo>> teamApplyInfoMap = teacherApplyInfoList.stream()
                .collect(Collectors.groupingBy(
                        CompetitionApplyInfo::getTeamCode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Set<String> contestantIdCards = new HashSet<>();
        Map<String, CompetitionTrackConfig> trackConfigMap = new HashMap<>();
        Date registrationTime = DateUtils.getNowDate();

        for (Map.Entry<String, List<CompetitionApplyInfo>> teamEntry : teamApplyInfoMap.entrySet()) {
            List<CompetitionApplyInfo> teamApplyInfoList = teamEntry.getValue();
            List<CompetitionApplyInfo> contestantList = teamApplyInfoList.stream()
                    .filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                    .toList();
            List<CompetitionApplyInfo> guideTeacherList = teamApplyInfoList.stream()
                    .filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                    .toList();

            if (contestantList.size() != 1) {
                throw new GlobalException("教师赛每个报名团队必须且只能有1名参赛选手");
            }
            CompetitionApplyInfo contestant = contestantList.get(0);
            if (!ApplyConstants.TEAM_LEADER_MEMBER.equals(contestant.getCompetitionRoleName())) {
                throw new GlobalException("教师赛参赛选手必须使用队长角色");
            }
            if (guideTeacherList.size() > 2) {
                throw new GlobalException("教师赛每个报名团队最多填写2名指导教师");
            }

            String secondLevelName = normalizeTeacherText(contestant.getSecondLevelName());
            if (!TEACHER_COMPETITION_GROUP_NAMES.contains(secondLevelName)) {
                throw new GlobalException("教师赛参赛组别只能选择“职业组（含中职）”或“本科组”");
            }
            boolean inconsistentGroup = teamApplyInfoList.stream()
                    .anyMatch(applyInfo -> !Objects.equals(secondLevelName, normalizeTeacherText(applyInfo.getSecondLevelName())));
            if (inconsistentGroup) {
                throw new GlobalException("教师赛同一团队的参赛组别不一致");
            }

            String trackConfigKey = contestant.getCompetitionTrackId() + "_" + secondLevelName;
            CompetitionTrackConfig competitionTrackConfig = trackConfigMap.get(trackConfigKey);
            if (Objects.isNull(competitionTrackConfig)) {
                CompetitionTrackConfig configReq = new CompetitionTrackConfig();
                configReq.setCompetitionSeriesId(contestant.getCompetitionSeriesId());
                configReq.setCompetitionTrackId(contestant.getCompetitionTrackId());
                configReq.setSecondLevelName(secondLevelName);
                List<CompetitionTrackConfig> competitionTrackConfigList =
                        competitionTrackConfigMapper.selectCompetitionTrackConfigByName(configReq);
                if (CollectionUtils.isNotEmpty(competitionTrackConfigList) && competitionTrackConfigList.size() == 1) {
                    competitionTrackConfig = competitionTrackConfigList.get(0);
                    trackConfigMap.put(trackConfigKey, competitionTrackConfig);
                }
            }
            validateTeacherCompetitionConfig(contestant, competitionTrackConfig, registrationTime);

            String idCard = normalizeTeacherIdCard(contestant.getIdCard());
            if (StringUtils.isBlank(idCard)) {
                throw new GlobalException("教师赛参赛选手“" + contestant.getUserName() + "”证件号不能为空");
            }
            if (!contestantIdCards.add(idCard)) {
                throw new GlobalException("教师赛报名表中证件号“" + idCard + "”重复");
            }

            contestant.setUserName(normalizeTeacherText(contestant.getUserName()));
            contestant.setIdCard(idCard);
            contestant.setUserId(null);
            contestant.setRealNameAuthStatus(null);
            populateTeacherContestantOrganizationFromExcel(contestant);
            checkTeacherContestantDuplicateRegistration(contestant);

            for (CompetitionApplyInfo applyInfo : teamApplyInfoList) {
                applyInfo.setLeaderTeacherId(uploaderUserId);
                applyInfo.setJoinType(Constants.JOIN_TYPE_TEAM);
                applyInfo.setRegistrationTime(registrationTime);
                applyInfo.setCheckStatus(Constants.CHECK_PASS);
                if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())) {
                    applyInfo.setGuideTeacher(applyInfo.getUserName());
                    applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
                    applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
                }
            }
        }
    }

    private void validateTeacherCompetitionConfig(CompetitionApplyInfo contestant,
                                                  CompetitionTrackConfig competitionTrackConfig,
                                                  Date registrationTime) {
        if (Objects.isNull(competitionTrackConfig)
                || Objects.isNull(competitionTrackConfig.getCompetitionConfig())
                || !Objects.equals(contestant.getCompetitionTrackId(), competitionTrackConfig.getCompetitionTrackId())) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”未配置或配置无效");
        }
        CompetitionConfig competitionConfig = competitionTrackConfig.getCompetitionConfig();
        if (!Constants.JOIN_TYPE_TEAM.equals(competitionConfig.getJoinType())) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”参赛方式必须配置为团队参赛");
        }
        if ((StringUtils.isNotBlank(competitionConfig.getMinPernNum())
                && !"1".equals(competitionConfig.getMinPernNum()))
                || (StringUtils.isNotBlank(competitionConfig.getMaxPernNum())
                && !"1".equals(competitionConfig.getMaxPernNum()))) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”参赛人数必须配置为1至1人");
        }
        if ((StringUtils.isNotBlank(competitionConfig.getIsTeacherNess())
                && !Constants.IS_NO.equals(competitionConfig.getIsTeacherNess()))
                || (StringUtils.isNotBlank(competitionConfig.getMinTeacherNum())
                && !"0".equals(competitionConfig.getMinTeacherNum()))
                || (StringUtils.isNotBlank(competitionConfig.getMaxTeacherNum())
                && !"2".equals(competitionConfig.getMaxTeacherNum()))) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”指导教师必须配置为非必填、0至2人");
        }
        if (StringUtils.isNotBlank(competitionConfig.getIsStudent())
                && !Constants.IS_NO.equals(competitionConfig.getIsStudent())) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”不能配置为必须是学生");
        }
        Date applyStartTime = competitionConfig.getApplyStartTime();
        Date applyEndTime = competitionConfig.getApplyEndTime();
        if (Objects.isNull(applyStartTime) || Objects.isNull(applyEndTime) || applyStartTime.after(applyEndTime)) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”未正确配置报名时间");
        }
        if (registrationTime.before(applyStartTime)) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”报名尚未开始");
        }
        if (registrationTime.after(applyEndTime)) {
            throw new GlobalException("教师赛组别“" + contestant.getSecondLevelName() + "”报名已结束");
        }
    }

    private void populateTeacherContestantOrganizationFromExcel(CompetitionApplyInfo contestant) {
        contestant.setSchoolName(normalizeTeacherText(contestant.getSchoolName()));
        contestant.setCompanyName(normalizeTeacherText(contestant.getCompanyName()));
        contestant.setOrgNameSnapshot(normalizeTeacherText(contestant.getOrgNameSnapshot()));
        contestant.setEmployeeCode(normalizeTeacherText(contestant.getEmployeeCode()));
        contestant.setDepartmentName(normalizeTeacherText(contestant.getDepartmentName()));
        contestant.setProfession(normalizeTeacherText(contestant.getProfession()));
        contestant.setOrgId(null);
        if (StringUtils.isBlank(contestant.getSchoolName())) {
            throw new GlobalException("教师赛参赛选手“" + contestant.getUserName() + "”的学校不能为空");
        }
        // 学校字典匹配失败不阻断报名，仍保留 Excel 中的学校名称快照。
        R<NationwideCollegeInfo> collegeResult = userService.getNationwideCollegeInfoInfoByName(
                contestant.getSchoolName(), SecurityConstants.INNER);
        if (Objects.nonNull(collegeResult) && R.isSuccess(collegeResult)
                && Objects.nonNull(collegeResult.getData())) {
            contestant.setSchool(collegeResult.getData().getId());
            contestant.setSchoolName(collegeResult.getData().getSchoolName());
        } else {
            contestant.setSchool(null);
        }
    }

    private void checkTeacherContestantDuplicateRegistration(CompetitionApplyInfo contestant) {
        CompetitionApplyInfo existingApplyInfo =
                competitionApplyInfoMapper.selectTeacherContestantActiveRegistration(
                        normalizeTeacherIdCard(contestant.getIdCard()),
                        contestant.getCompetitionSeriesId(),
                        contestant.getCompetitionTrackId(),
                        DictConstant.CANCELLED
                );
        if (Objects.nonNull(existingApplyInfo)) {
            throw new GlobalException("教师赛参赛选手“" + contestant.getUserName() + "”已报名本赛道，请勿重复报名");
        }
    }

    private String normalizeTeacherText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeTeacherIdCard(String value) {
        String normalized = normalizeTeacherText(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private void protectTeacherCompetitionContestantFields(CompetitionApplyInfo requested,
                                                           CompetitionApplyInfo existing) {
        if (!ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(existing.getCompetitionTrackName())
                || ApplyConstants.TEAM_GUIDE_TEACHER.equals(existing.getCompetitionRoleName())) {
            return;
        }
        if (StringUtils.isNotBlank(requested.getUserName())
                && !Objects.equals(normalizeTeacherText(requested.getUserName()),
                normalizeTeacherText(existing.getUserName()))) {
            throw new GlobalException("教师赛参赛选手姓名不能变更");
        }
        if (StringUtils.isNotBlank(requested.getIdCard())
                && !Objects.equals(normalizeTeacherIdCard(requested.getIdCard()),
                normalizeTeacherIdCard(existing.getIdCard()))) {
            throw new GlobalException("教师赛参赛选手证件号不能变更");
        }
        if (requested.getUserId() != null && !Objects.equals(requested.getUserId(), existing.getUserId())) {
            throw new GlobalException("教师赛参赛选手账号不能变更");
        }
        if (StringUtils.isNotBlank(requested.getCompetitionRoleName())
                && !Objects.equals(requested.getCompetitionRoleName(), existing.getCompetitionRoleName())) {
            throw new GlobalException("教师赛参赛选手角色不能变更");
        }
        if (requested.getOrgId() != null && !Objects.equals(requested.getOrgId(), existing.getOrgId())) {
            throw new GlobalException("教师赛参赛选手所属机构不能变更");
        }
        if (StringUtils.isNotBlank(requested.getSchool())
                && !Objects.equals(requested.getSchool(), existing.getSchool())) {
            throw new GlobalException("教师赛参赛选手学校或单位不能变更");
        }
        if (StringUtils.isNotBlank(requested.getSchoolName())
                && !Objects.equals(normalizeTeacherText(requested.getSchoolName()),
                normalizeTeacherText(existing.getSchoolName()))) {
            throw new GlobalException("教师赛参赛选手学校或单位不能变更");
        }

        // 身份证、账号关联和报名时组织快照以原记录为准，空值请求也不能清除。
        requested.setUserName(existing.getUserName());
        requested.setIdCard(existing.getIdCard());
        requested.setIdCardType(existing.getIdCardType());
        requested.setUserId(existing.getUserId());
        requested.setCompetitionRoleName(existing.getCompetitionRoleName());
        requested.setRealNameAuthStatus(existing.getRealNameAuthStatus());
        requested.setOrgId(existing.getOrgId());
        requested.setOrgName(existing.getOrgName());
        requested.setCompanyName(existing.getCompanyName());
        requested.setOrgNameSnapshot(existing.getOrgNameSnapshot());
        requested.setSchool(existing.getSchool());
        requested.setSchoolName(existing.getSchoolName());
        requested.setEmployeeCode(existing.getEmployeeCode());
        requested.setDepartmentName(existing.getDepartmentName());
        requested.setProfession(existing.getProfession());
        requested.setCompetitionTrackName(existing.getCompetitionTrackName());
        requested.setCompetitionTrackId(existing.getCompetitionTrackId());
        requested.setCompetitionTrackType(existing.getCompetitionTrackType());
        requested.setJoinType(existing.getJoinType());
    }

    // 只校验报名表是否存在重复手机号及邮箱
    @Override
    public void checkPhoneAndEmail(List<CompetitionApplyInfo> applyInfoList, Long competitionSeriesId) {
        List<CompetitionApplyInfo> allApplyInfoList = new ArrayList<>();
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        competitionApplyInfo.setCompetitionSeriesId(competitionSeriesId);
        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        competitionApplyInfo.setPayStatus(DictConstant.PAID);
        List<CompetitionApplyInfo> applyInfoOldList = competitionApplyInfoMapper.selectCompetitionApplyInfoList(competitionApplyInfo);
        allApplyInfoList.addAll(applyInfoOldList);
        // 过滤下指导教师
        applyInfoList = applyInfoList.stream().filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
        allApplyInfoList.addAll(applyInfoList);
        // 校验手机号重复
        if (CollectionUtils.isNotEmpty(allApplyInfoList)) {
            Map<String, List<CompetitionApplyInfo>> phoneMap = allApplyInfoList.stream().
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getPhone));
            if (phoneMap.size() > 1) {
                for (Map.Entry<String, List<CompetitionApplyInfo>> entry : phoneMap.entrySet()) {
                    List<CompetitionApplyInfo> value = entry.getValue();
                    List<String> userNameList = value.stream().map(CompetitionApplyInfo::getUserName).collect(Collectors.toList());
                    Set<String> userNameSet = new HashSet<>(userNameList);
                    StringBuffer sb = new StringBuffer();
                    if (userNameSet.size() > 1) {
                        userNameSet.stream().forEach(userName -> sb.append(userName).append("、"));
                        throw new GlobalException(sb.substring(0, sb.length() - 1).toString() + ",手机号重复");
                    }
                }
            }
        }
        //校验邮箱重复
        if (CollectionUtils.isNotEmpty(allApplyInfoList)) {
            Map<String, List<CompetitionApplyInfo>> emailMap = allApplyInfoList.stream().
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getEmail));
            if (emailMap.size() > 1) {
                for (Map.Entry<String, List<CompetitionApplyInfo>> entry : emailMap.entrySet()) {
                    List<CompetitionApplyInfo> value = entry.getValue();
                    List<String> userNameList = value.stream().map(CompetitionApplyInfo::getUserName).collect(Collectors.toList());
                    Set<String> userNameSet = new HashSet<>(userNameList);
                    StringBuffer sb = new StringBuffer();
                    if (userNameSet.size() > 1) {
                        userNameSet.stream().forEach(userName -> sb.append(userName).append("、"));
                        throw new GlobalException(sb.substring(0, sb.length() - 1).toString() + ",邮箱重复");
                    }
                }
            }
        }
    }

    // 一个赛事下全库校验手机号及邮箱是否重复
    @Override
    public void checkUpdatePhoneAndEmail(List<CompetitionApplyInfo> applyInfoList, Long competitionSeriesId) {
//        List<CompetitionApplyInfo> allApplyInfoList = new ArrayList<>();
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        competitionApplyInfo.setCompetitionSeriesId(competitionSeriesId);
//        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        competitionApplyInfo.setPayStatus(DictConstant.PAID);
        List<CompetitionApplyInfo> applyInfoOldList = competitionApplyInfoMapper.selectCompetitionApplyInfoList(competitionApplyInfo);
        // 获取已报名得所有手机号
        List<String> applyInfoOldPhoneList = applyInfoOldList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getPhone())).
                map(CompetitionApplyInfo::getPhone).collect(Collectors.toList());
        // 获取已报名得所有邮箱
        List<String> applyInfoOldEmailList = applyInfoOldList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getEmail())).
                map(CompetitionApplyInfo::getEmail).collect(Collectors.toList());
        // 过滤下指导教师
//        applyInfoList = applyInfoList.stream().filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
//        allApplyInfoList.addAll(applyInfoOldList);
//        allApplyInfoList.addAll(applyInfoList);
        // 校验手机号重复
        //获取用户表未删除得所有手机号及邮箱
        // 注意：数据量大，考虑性能问题后期需要进行修改，将手机号或者邮箱入参形式进行获取
        R<List<SysUser>> listR = userService.selectUserByPhoneCheck(new SysUser(), SecurityConstants.INNER);
        Set<String> userPhoneList = new HashSet<>();
        Set<String> userEmailList = new HashSet<>();
        Map<String, List<SysUser>> phoneUserMap = new HashMap<>();
        Map<String, List<SysUser>> emailUserMap = new HashMap<>();
        if (R.isSuccess(listR) && CollectionUtils.isNotEmpty(listR.getData())) {
            phoneUserMap = listR.getData().stream().filter(sysUser -> StringUtils.isNotEmpty(sysUser.getPhonenumber())).
                    collect(Collectors.groupingBy(SysUser::getPhonenumber));
            userEmailList = listR.getData().stream().filter(sysUser -> StringUtils.isNotEmpty(sysUser.getEmail())).
                    map(SysUser::getEmail).collect(Collectors.toSet());
            userPhoneList = listR.getData().stream().filter(sysUser -> StringUtils.isNotEmpty(sysUser.getPhonenumber())).
                    map(SysUser::getPhonenumber).collect(Collectors.toSet());
            emailUserMap = listR.getData().stream().filter(sysUser -> StringUtils.isNotEmpty(sysUser.getEmail())).
                    collect(Collectors.groupingBy(SysUser::getEmail));
        }
        if (CollectionUtils.isNotEmpty(applyInfoList)) {
            // 入参传手机号自身校验通过后，再看库中是否存在
            List<String> newPhoneList = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getPhone())).
                    map(CompetitionApplyInfo::getPhone).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newPhoneList)) {
                // 查看入参本身是否存在手机号重复
                if (CollectionUtils.isNotEmpty(newPhoneList)) {
                    Set<String> phoneSet = new HashSet<>(newPhoneList);
                    if (phoneSet.size() < newPhoneList.size()) {
                        Map<String, Long> phoneCountMap = newPhoneList.stream()
                                .collect(Collectors.groupingBy(phone -> phone, Collectors.counting()));
                        List<String> duplicatePhones = phoneCountMap.entrySet().stream()
                                .filter(entry -> entry.getValue() > 1)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        for (String duplicatePhone : duplicatePhones) {
                            List<CompetitionApplyInfo> samePhoneList = applyInfoList.stream()
                                    .filter(info -> duplicatePhone.equals(info.getPhone()))
                                    .collect(Collectors.toList());

                            Set<String> userIdentifiers = samePhoneList.stream()
                                    .map(info -> StringUtils.isNotEmpty(info.getIdCard()) ? info.getIdCard() :
                                            (info.getUserId() != null ? info.getUserId().toString() : info.getUserName()))
                                    .collect(Collectors.toSet());

                            if (userIdentifiers.size() > 1) {
                                throw new GlobalException(duplicatePhone + "手机号重复");
                            }
                        }
                    }
                }
                for (CompetitionApplyInfo newApplyInfo : applyInfoList) {
                    if (StringUtils.isNotEmpty(newApplyInfo.getPhone()) && applyInfoOldPhoneList.contains(newApplyInfo.getPhone())) {
                        String newUserIdentifier = StringUtils.isNotEmpty(newApplyInfo.getIdCard()) ? newApplyInfo.getIdCard() :
                                (newApplyInfo.getUserId() != null ? newApplyInfo.getUserId().toString() : newApplyInfo.getUserName());

                        List<CompetitionApplyInfo> oldSamePhoneList = applyInfoOldList.stream()
                                .filter(oldInfo -> newApplyInfo.getPhone().equals(oldInfo.getPhone()))
                                .collect(Collectors.toList());

                        boolean isSamePerson = oldSamePhoneList.stream().anyMatch(oldInfo -> {
                            String oldUserIdentifier = StringUtils.isNotEmpty(oldInfo.getIdCard()) ? oldInfo.getIdCard() :
                                    (oldInfo.getUserId() != null ? oldInfo.getUserId().toString() : oldInfo.getUserName());
                            return newUserIdentifier.equals(oldUserIdentifier);
                        });

                        if (!isSamePerson) {
                            throw new GlobalException(newApplyInfo.getPhone() + "手机号重复");
                        }
                    }
                }
            }
            Map<String, List<CompetitionApplyInfo>> AllPhoneMap = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getPhone())).
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getPhone));
//            Map<String, List<CompetitionApplyInfo>> oldPhoneMap = applyInfoOldList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getPhone())).
//                    collect(Collectors.groupingBy(CompetitionApplyInfo::getPhone));
            if (MapUtils.isNotEmpty(AllPhoneMap)) {
                for (Map.Entry<String, List<CompetitionApplyInfo>> entry : AllPhoneMap.entrySet()) {
                    List<CompetitionApplyInfo> value = entry.getValue();
                    Set<String> userNameSet = value.stream().map(CompetitionApplyInfo::getUserName).collect(Collectors.toSet());
                    if (userPhoneList.contains(entry.getKey())) {
                        List<SysUser> sysUsers = phoneUserMap.get(entry.getKey());
                        if (CollectionUtils.isNotEmpty(sysUsers)) {
                            // 已经实名认证手机号
                            List<String> nameAuthList = sysUsers.stream().filter(sysUser -> Objects.nonNull(sysUser.getAuthInfo()))
                                    .map(sysUser -> sysUser.getAuthInfo().getRealName()).toList();
                            // 未实名认证得手机号
                            List<String> phoneNoAuthList = sysUsers.stream().filter(sysUser -> Objects.isNull(sysUser.getAuthInfo()) && StringUtils.isNotEmpty(sysUser.getPhonenumber()))
                                    .map(SysUser::getPhonenumber).toList();
                            if (CollectionUtils.isNotEmpty(nameAuthList) && nameAuthList.stream().noneMatch(userNameSet::contains)) {
                                log.info("手机号重复已实名认证nameList:" + JSONObject.toJSONString(nameAuthList));
                                log.info("手机号重复userNameSet:" + JSONObject.toJSONString(userNameSet));
                                throw new GlobalException(entry.getKey() + "手机号重复");
                            }
                            if (CollectionUtils.isNotEmpty(phoneNoAuthList)) {
                                log.info("手机号重复未实名认证phoneNoAuthList:" + JSONObject.toJSONString(phoneNoAuthList));
                                log.info("手机号重复userNameSet:" + JSONObject.toJSONString(userNameSet));
                                throw new GlobalException(entry.getKey() + "手机号重复");
                            }
                        }
                    }
                }
            }
        }
        //校验邮箱重复
        if (CollectionUtils.isNotEmpty(applyInfoList)) {
            // 入参传邮箱自身校验通过后，再看库中是否存在
            List<String> newEmailList = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getEmail())).
                    map(CompetitionApplyInfo::getEmail).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newEmailList)) {
                // 查看入参本身是否存在邮箱重复
                // 查看入参本身是否存在邮箱重复
                Set<String> emailSet = new HashSet<>(newEmailList);
                if (emailSet.size() < newEmailList.size()) {
                    Map<String, Long> emailCountMap = newEmailList.stream()
                            .collect(Collectors.groupingBy(email -> email, Collectors.counting()));
                    List<String> duplicateEmails = emailCountMap.entrySet().stream()
                            .filter(entry -> entry.getValue() > 1)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    for (String duplicateEmail : duplicateEmails) {
                        List<CompetitionApplyInfo> sameEmailList = applyInfoList.stream()
                                .filter(info -> duplicateEmail.equals(info.getEmail()))
                                .collect(Collectors.toList());

                        Set<String> userIdentifiers = sameEmailList.stream()
                                .map(info -> StringUtils.isNotEmpty(info.getIdCard()) ? info.getIdCard() :
                                        (info.getUserId() != null ? info.getUserId().toString() : info.getUserName()))
                                .collect(Collectors.toSet());

                        if (userIdentifiers.size() > 1) {
                            throw new GlobalException(duplicateEmail + "邮箱重复");
                        }
                    }
                }
                for (CompetitionApplyInfo newApplyInfo : applyInfoList) {
                    if (StringUtils.isNotEmpty(newApplyInfo.getEmail()) && applyInfoOldEmailList.contains(newApplyInfo.getEmail())) {
                        String newUserIdentifier = StringUtils.isNotEmpty(newApplyInfo.getIdCard()) ? newApplyInfo.getIdCard() :
                                (newApplyInfo.getUserId() != null ? newApplyInfo.getUserId().toString() : newApplyInfo.getUserName());

                        List<CompetitionApplyInfo> oldSameEmailList = applyInfoOldList.stream()
                                .filter(oldInfo -> newApplyInfo.getEmail().equals(oldInfo.getEmail()))
                                .collect(Collectors.toList());

                        boolean isSamePerson = oldSameEmailList.stream().anyMatch(oldInfo -> {
                            String oldUserIdentifier = StringUtils.isNotEmpty(oldInfo.getIdCard()) ? oldInfo.getIdCard() :
                                    (oldInfo.getUserId() != null ? oldInfo.getUserId().toString() : oldInfo.getUserName());
                            return newUserIdentifier.equals(oldUserIdentifier);
                        });

                        if (!isSamePerson) {
                            throw new GlobalException(newApplyInfo.getEmail() + "邮箱重复");
                        }
                    }
                }
            }
            Map<String, List<CompetitionApplyInfo>> AllEmailMap = applyInfoList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getEmail())).
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getEmail));
//            Map<String, List<CompetitionApplyInfo>> oldemailMap = applyInfoOldList.stream().filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getEmail())).
//                    collect(Collectors.groupingBy(CompetitionApplyInfo::getEmail));
            if (AllEmailMap.size() > 1) {
                for (Map.Entry<String, List<CompetitionApplyInfo>> entry : AllEmailMap.entrySet()) {
                    List<CompetitionApplyInfo> value = entry.getValue();
                    Set<String> userNameSet = value.stream().map(CompetitionApplyInfo::getUserName).collect(Collectors.toSet());
                    if (userEmailList.contains(entry.getKey())) {
                        List<SysUser> sysUsers = emailUserMap.get(entry.getKey());
                        if (CollectionUtils.isNotEmpty(sysUsers)) {
                            List<String> nameList = sysUsers.stream().filter(sysUser -> Objects.nonNull(sysUser.getAuthInfo()))
                                    .map(sysUser -> sysUser.getAuthInfo().getRealName()).toList();
                            // 未实名认证得邮箱
                            List<String> emailNoAuthList = sysUsers.stream().filter(sysUser -> Objects.isNull(sysUser.getAuthInfo()) && StringUtils.isNotEmpty(sysUser.getPhonenumber()))
                                    .map(SysUser::getEmail).toList();
                            if (CollectionUtils.isNotEmpty(nameList) && nameList.stream().noneMatch(userNameSet::contains)) {
                                log.info("邮箱重复nameList:" + JSONObject.toJSONString(nameList));
                                log.info("邮箱重复userNameSet:" + JSONObject.toJSONString(userNameSet));
                                throw new GlobalException(entry.getKey() + "邮箱重复");
                            }
                            if (CollectionUtils.isNotEmpty(emailNoAuthList)) {
                                log.info("邮箱重复未实名认证emailNoAuthList:" + JSONObject.toJSONString(emailNoAuthList));
                                log.info("邮箱重复userNameSet:" + JSONObject.toJSONString(userNameSet));
                                throw new GlobalException(entry.getKey() + "邮箱重复");
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkData(List<CompetitionApplyInfo> applyInfoList, Long competitionSeriesId, String operationType) throws Exception {
        // 获取赛事赛道组别配置
        List<CompetitionTrackInfo> competitionTrackInfoList =
                competitionTrackInfoMapper.selectCompetitionTrackInfo(competitionSeriesId, Constants.CHECK_PASS);
        if (CollectionUtils.isNotEmpty(competitionTrackInfoList)) {
            for (CompetitionTrackInfo competitionTrackInfo : competitionTrackInfoList) {
                if (Objects.isNull(competitionTrackInfo.getCheckPackageId())) {
                    continue;
                }
                CompetitionCheckDataPackage competitionCheckDataPackage =
                        competitionCheckDataPackageService.selectCompetitionCheckDataPackageByPackageId(competitionTrackInfo.getCheckPackageId());
                List<CompetitionCheckInfo> checkInfoList = competitionCheckDataPackage.getCheckInfoList();
                if (CollectionUtils.isNotEmpty(checkInfoList)) {
                    List<String> methodNameList = checkInfoList.stream().map(CompetitionCheckInfo::getFunction).collect(Collectors.toList());
                    log.info("methodNameList:{}", JSONObject.toJSONString(methodNameList));
                    log.info("competitionSeriesId:{}", competitionSeriesId);
                    log.info("getCheckPackageId:{}", competitionTrackInfo.getCheckPackageId());
                    for (String methodName : methodNameList) {
                        ICompetitionApplyInfoService competitionApplyInfoService = SpringUtil.getBean(ICompetitionApplyInfoService.class);
                        if ("collegeVerification".equals(methodName)) {
                            String invoke = ReflectUtil.invoke(competitionApplyInfoService, methodName, applyInfoList, competitionTrackInfo.getCompetitionTrackId(), operationType);
                            if (StringUtils.isNotBlank(invoke)) {
                                throw new GlobalException(invoke);
                            }
                        } else {
                            String invoke = ReflectUtil.invoke(competitionApplyInfoService, methodName, applyInfoList, competitionTrackInfo.getCompetitionTrackId());
                            if (StringUtils.isNotBlank(invoke)) {
                                throw new GlobalException(invoke);
                            }
                        }
                    }
                }
            }
        }
    }

    // 反射回去所有类中方法名

    @Override
    @Transactional
    public int saveApplyCompetitionData(UserApplyCompetitionReq req) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        try {
            // 如果存在生成订单的信息则，不刷新数据
//        int count = competitionApplyInfoMapper.selectCompetitionApplyInfoByExitPayStatus(req.getCompetitionSeriesId(), userId);
//        if (count > 0) {
//            log.info("存在生成订单的数据");
//            return 2;
//        }
            // 报名信息入表
            List<CompetitionApplyInfo> competitionApplyInfoList = redisService.getCacheObject(userId + "_" + req.getCompetitionSeriesId() + "");
            if (CollectionUtils.isEmpty(competitionApplyInfoList)) {
                return 1;
            }
            // 导入预览到最终保存之间可能出现并发报名，落库前再按身份证复核一次。
            competitionApplyInfoList.stream()
                    .filter(applyInfo -> ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(
                            applyInfo.getCompetitionTrackName()))
                    .filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(
                            applyInfo.getCompetitionRoleName()))
                    .forEach(this::checkTeacherContestantDuplicateRegistration);

            // 自动注册学生用户
//        syncCompetitionApplyInfo(competitionApplyInfoList);
            // 团队信息及团员信息入表
            Map<String, List<CompetitionApplyInfo>> competitionApplyInfoMap = competitionApplyInfoList.stream().
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
            List<TeamManagerInfo> teamManagerInfoList = new ArrayList<>();
            List<TeamMemberRela> TeamMemberInfoList = new ArrayList<>();
            if (MapUtils.isNotEmpty(competitionApplyInfoMap)) {
                competitionApplyInfoMap.forEach((teamCode, applyInfoList) -> {
                    TeamManagerInfo teamManagerInfo = getTeamManagerInfo(teamCode, applyInfoList, userId);
                    teamManagerInfoList.add(teamManagerInfo);
                    applyInfoList.stream().forEach(applyInfo -> {
                        TeamMemberRela teamMemberInfo = new TeamMemberRela();
                        teamMemberInfo.setTeamCode(teamCode);
                        teamMemberInfo.setUserId(applyInfo.getUserId());
                        teamMemberInfo.setTeamRole(applyInfo.getCompetitionRoleName());
                        teamMemberInfo.setInstructor(applyInfo.getGuideTeacher());
                        teamMemberInfo.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                        teamMemberInfo.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                        teamMemberInfo.setUserName(applyInfo.getUserName());
                        if (ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName())) {
                            teamMemberInfo.setCheckStatus(Constants.JOIN_TEAM_AGREE);
                            teamMemberInfo.setOrgId(applyInfo.getOrgId());
                        }
                        TeamMemberInfoList.add(teamMemberInfo);
                    });
                });
            }
            teamManagerInfoMapper.batchInsertTeamManagerInfo(teamManagerInfoList);
            teamMemberRelaMapper.batchInsertTeamMemberRela(TeamMemberInfoList);
            competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(competitionApplyInfoList);
            redisService.deleteObject(userId + "_" + req.getCompetitionSeriesId() + "");
            return 1;
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存数据异常", e);
            throw new GlobalException("报名保存失败，请稍后重试");
        } finally {
            redisService.deleteObject(userId + "_" + req.getCompetitionSeriesId() + "");
        }
    }

    @Override
    @Transactional
    public int bindTeacherCompetitionUser(Long userId, String phone) {
        if (userId == null || StringUtils.isBlank(phone)) {
            return 0;
        }
        List<CompetitionApplyInfo> candidates =
                competitionApplyInfoMapper.selectUnboundTeacherContestantsByPhone(phone.trim());
        if (CollectionUtils.isEmpty(candidates)) {
            return 0;
        }
        Set<String> idCards = candidates.stream()
                .map(CompetitionApplyInfo::getIdCard)
                .map(this::normalizeTeacherIdCard)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        boolean missingIdCard = candidates.stream()
                .anyMatch(candidate -> StringUtils.isBlank(normalizeTeacherIdCard(candidate.getIdCard())));
        if (missingIdCard || idCards.size() != 1) {
            log.warn("教师赛手机号关联存在歧义，跳过自动关联。phone={}, idCardCount={}", phone, idCards.size());
            return 0;
        }
        int bound = 0;
        for (CompetitionApplyInfo candidate : candidates) {
            int rows = competitionApplyInfoMapper.bindTeacherContestantUser(candidate.getMemberId(), userId);
            if (rows > 0) {
                teamMemberRelaMapper.bindTeacherContestantUser(candidate.getTeamCode(), userId);
                teamManagerInfoMapper.bindTeacherContestantUser(candidate.getTeamCode(), userId);
                bound += rows;
            }
        }
        return bound;
    }

    public void checkExcelApplyData(List<CompetitionApplyInfo> applyInfoList, Long competitionSeriesId) {
        // 获取赛事赛道组别配置
        CompetitionTrackInfo competitionTrackInfoReq = new CompetitionTrackInfo();
        competitionTrackInfoReq.setCompetitionSeriesId(competitionSeriesId);
        competitionTrackInfoReq.setCheckStatus(Constants.CHECK_PASS);
        List<CompetitionTrackInfo> competitionTrackInfoList =
                competitionTrackInfoMapper.selectCompetitionTrackInfoByUser(competitionTrackInfoReq);
        if (CollectionUtils.isNotEmpty(competitionTrackInfoList)) {
            // 一个二级级别一个配置
            Map<String, CompetitionConfig> competitionConfigMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(competitionTrackInfoList)) {
                competitionTrackInfoList.stream().forEach(competitionTrackInfo -> {
                    competitionTrackInfo.getCompetitionTrackConfigList().stream().forEach(competitionTrackConfig -> {
                        competitionConfigMap.put(competitionTrackConfig.getSecondLevelCode(), competitionTrackConfig.getCompetitionConfig());
                    });
                });
            }
            Map<String, List<CompetitionApplyInfo>> applyInfoMap = applyInfoList.stream().
                    filter(applyInfo -> StringUtils.isNotEmpty(applyInfo.getTeamCode())).
                    collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
            for (Map.Entry entry : applyInfoMap.entrySet()) {
                List<CompetitionApplyInfo> applyInfoMapList = (List<CompetitionApplyInfo>) entry.getValue();
                // 计算队员数量
                AtomicInteger teamSize = new AtomicInteger();
                // 计算指导老师数量
                AtomicInteger guideSize = new AtomicInteger();
                applyInfoMapList.stream().forEach(applyInfo -> {
                    if (StringUtils.isEmpty(applyInfo.getGuideTeacher())) {
                        teamSize.getAndIncrement();
                    } else {
                        guideSize.getAndIncrement();
                    }
                });
                CompetitionConfig competitionConfig = competitionConfigMap.get(applyInfoMapList.get(0).getSecondLevelCode());
                if (Objects.nonNull(competitionConfig)) {
                    if (Objects.nonNull(competitionConfig.getMinPernNum()) && teamSize.get() < Integer.valueOf(competitionConfig.getMinPernNum())) {
                        throw new GlobalException(applyInfoMapList.get(0).getCompetitionTrackName() + "下" +
                                applyInfoMapList.get(0).getSecondLevelName() + "组团队人数不足");
                    }
                    if (Objects.nonNull(competitionConfig.getMaxPernNum()) && teamSize.get() > Integer.valueOf(competitionConfig.getMaxPernNum())) {
                        throw new GlobalException(applyInfoMapList.get(0).getCompetitionTrackName() + "下" +
                                applyInfoMapList.get(0).getSecondLevelName() + "组团队人数超出设定范围");
                    }
                    // 是否必须含指导老师 isTeacherNess
                    if (Constants.IS_YES.equals(competitionConfig.getIsTeacherNess())) {
                        // 指导老师数量限制 minTeacherNum  maxTeacherNum
                        if (guideSize.get() == 0) {
                            throw new GlobalException(applyInfoMapList.get(0).getCompetitionTrackName() + "下" +
                                    applyInfoMapList.get(0).getSecondLevelName() + "组不存在指导老师");
                        }
                        if (Objects.nonNull(competitionConfig.getMinTeacherNum()) && Objects.nonNull(competitionConfig.getMaxTeacherNum()) && guideSize.get() < Integer.valueOf(competitionConfig.getMinTeacherNum() == null ? "0" : competitionConfig.getMinTeacherNum()) ||
                                guideSize.get() > Integer.valueOf(competitionConfig.getMaxTeacherNum() == null ? "0" : competitionConfig.getMaxTeacherNum())) {
                            throw new GlobalException(applyInfoMapList.get(0).getCompetitionTrackName() + "下" +
                                    applyInfoMapList.get(0).getSecondLevelName() + "组指导老师数量不在范围内");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void syncCompetitionApplyInfo(String updateSize) {
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        competitionApplyInfo.setPayStatus(DictConstant.PAID);
        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoByNullUserId(competitionApplyInfo);
        // 教师赛由用户注册后的手机号关联任务处理，不进入学生账号补偿任务。
        applyInfoList = applyInfoList.stream()
                .filter(applyInfo -> !ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName()))
                .toList();
        if (CollectionUtils.isNotEmpty(applyInfoList)) {
            // 注册学生用户批处理
//            List<List<CompetitionApplyInfo>> applyInfoListBatch = ListUtils.partition(applyInfoList,100);
            if (StringUtils.isEmpty(updateSize)) {
                updateSize = "100";
            }
            List<List<CompetitionApplyInfo>> applyInfoListBatch = new ArrayList<>();
            if (applyInfoList.size() < Integer.valueOf(updateSize)) {
                applyInfoListBatch = Arrays.asList(applyInfoList);
            } else {
                applyInfoListBatch = Arrays.asList(applyInfoList.subList(0, Integer.valueOf(updateSize)));
            }
            for (List<CompetitionApplyInfo> applyInfoListBatchList : applyInfoListBatch) {
                // 注册学生用户
                List<SysUser> sysUserList = new ArrayList<>();
                applyInfoListBatchList.stream().forEach(applyInfo -> {
                    if (StringUtils.isEmpty(applyInfo.getGuideTeacher())) {
                        SysUser sysUser = new SysUser();
                        sysUser.setUserName(applyInfo.getPhone() == null ? applyInfo.getEmail() : applyInfo.getPhone());
                        sysUser.setPhonenumber(applyInfo.getPhone());
                        sysUser.setEmail(applyInfo.getEmail());
                        if (StringUtils.isNotEmpty(applyInfo.getSex())) {
                            sysUser.setSex("男".equals(applyInfo.getSex()) ? "0" : "1");
                        }
                        // 身份证后6位，如果不是身份证怎样处理
                        String idCard = applyInfo.getIdCard();
                        String idCardLast6 = "";
                        if (idCard != null && idCard.length() >= 6) {
                            idCardLast6 = idCard.substring(idCard.length() - 6);
                        } else if (idCard != null) {
                            // 如果身份证号码长度不足6位，则取全部
                            idCardLast6 = String.format("%-6s", idCard).replace(' ', '0');
                        }
                        sysUser.setPassword(SecurityUtils.encryptPassword(idCardLast6));
                        sysUser.setNickName(applyInfo.getUserName());
                        sysUser.setSchool(applyInfo.getSchool());
                        sysUser.setSchoolName(applyInfo.getSchoolName());
                        sysUser.setUserType("2");
                        sysUser.setStatus("0");
                        sysUser.setAuthStatus(Constants.AUTH_STATUS_PASS);
                        AuthInfo authInfo = new AuthInfo();
                        authInfo.setRealName(applyInfo.getUserName());
                        authInfo.setIdCard(applyInfo.getIdCard());
                        authInfo.setIdCardType("1");
                        authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
                        authInfo.setCountryName("CN");
                        sysUser.setAuthInfo(authInfo);
                        IdentityInfo identityInfo = new IdentityInfo();
                        identityInfo.setCertificationType(Constants.IDENTITY_TYPE_STUDENT);
                        identityInfo.setSchool(applyInfo.getSchool());
                        identityInfo.setPosition("学生");
                        identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                        identityInfo.setSpecialty(applyInfo.getProfession());
                        identityInfo.setClassInfo(applyInfo.getClassInfo());
                        sysUser.setIdentityInfo(identityInfo);
                        sysUserList.add(sysUser);
                    }
                });
                // sysUserList 去重
                Set<String> idCards = sysUserList.stream()
                        .map(sysUser -> sysUser.getAuthInfo().getIdCard())
                        .collect(Collectors.toSet());
                List<SysUser> uniqueUsers = idCards.stream()
                        .map(id -> sysUserList.stream()
                                .filter(user -> user.getAuthInfo().getIdCard().equals(id))
                                .findFirst()
                                .orElse(null))
                        .collect(Collectors.toList());
//                List<SysUser> distinctUsers = sysUserList.stream()
//                        .filter(user -> user.getAuthInfo() != null && StringUtils.isNotEmpty(user.getAuthInfo().getIdCard()))
//                        .filter(user -> distinctUsersSet.add(user.getAuthInfo().getIdCard()))
//                        .collect(Collectors.toList());
                R<Map<String, Map<String, Object>>> sysUserListR = userService.saveStudentUserInfo(uniqueUsers, SecurityConstants.INNER);
                if (R.isSuccess(sysUserListR)) {
                    Map<String, Map<String, Object>> sysUsers = sysUserListR.getData();
                    List<CompetitionApplyInfo> updateApplyInfoList = new ArrayList<>();
                    List<TeamMemberRela> teamMemberRelaList = new ArrayList<>();
                    applyInfoListBatchList.stream().forEach(applyInfo -> {
                        if (sysUsers.containsKey(applyInfo.getIdCard())) {
                            applyInfo.setUserId(Long.valueOf(sysUsers.get(applyInfo.getIdCard()).get("userId").toString()));
                            updateApplyInfoList.add(applyInfo);
                            TeamMemberRela teamMemberRela = new TeamMemberRela();
                            teamMemberRela.setUserId(applyInfo.getUserId());
                            teamMemberRela.setUserName(applyInfo.getUserName());
                            teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                            teamMemberRelaList.add(teamMemberRela);
                        }
                    });
                    if (CollectionUtils.isNotEmpty(updateApplyInfoList)) {
                        competitionApplyInfoMapper.updateUserId(updateApplyInfoList);
                        // 队员信息表更新user_id
                        if (CollectionUtils.isNotEmpty(teamMemberRelaList)) {
                            teamMemberRelaList.stream().forEach(teamMemberRela -> {
                                teamMemberRelaMapper.updateTeamMemberRelaUserId(teamMemberRela);
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<UserCompetitionApplyInfoDTO> selectUserCompetitionApplyInfoDetail(Map params) {
        List<UserCompetitionApplyInfoDTO> userCompetitionApplyInfoDTOList = new ArrayList<>();
//        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
//        params.put("userId", userId);
//        AuthInfo authInfo = SecurityUtils.getLoginUser().getSysUser().getAuthInfo();
//        if(Objects.nonNull(authInfo)){
//            // 指导教师
//            params.put("guidTeacherName", authInfo.getRealName());
//        }
        List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByUserTeamCode(params);
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            Map<String, List<CompetitionApplyInfo>> allGrouped = competitionApplyInfoList.stream()
                    .collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
            allGrouped.forEach((teamCode, applyInfoList) -> {
                UserCompetitionApplyInfoDTO userCompetitionApplyInfoDTO = new UserCompetitionApplyInfoDTO();
                List<CompetitionApplyInfo> memberApplyInfoList = new ArrayList<>();
                List<CompetitionApplyInfo> guideTeacherApplyInfoList = new ArrayList<>();
                competitionApplyInfoList.stream().forEach(applyInfo -> {
                    if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())) {
                        guideTeacherApplyInfoList.add(applyInfo);
                    } else {
                        memberApplyInfoList.add(applyInfo);
                    }
                });
                // 校验队员信息是否一致
                checkUserInfo(memberApplyInfoList);
                //memberApplyInfoList按照teamCode升序排序
//                memberApplyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getTeamSort));
                memberApplyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getTeamSort, Comparator.nullsLast(Comparator.naturalOrder())));
                userCompetitionApplyInfoDTO.setCompetitionSeriesId(applyInfoList.get(0).getCompetitionSeriesId());
                userCompetitionApplyInfoDTO.setCompetitionName(applyInfoList.get(0).getCompetitionName());
                userCompetitionApplyInfoDTO.setTeamCode(teamCode);
                userCompetitionApplyInfoDTO.setCompetitionTrackId(applyInfoList.get(0).getCompetitionTrackId());
                userCompetitionApplyInfoDTO.setCompetitionTrackName(applyInfoList.get(0).getCompetitionTrackName());
                userCompetitionApplyInfoDTO.setSecondLevelCode(applyInfoList.get(0).getSecondLevelCode());
                userCompetitionApplyInfoDTO.setSecondLevelName(applyInfoList.get(0).getSecondLevelName());
                userCompetitionApplyInfoDTO.setTeamName(applyInfoList.get(0).getTeamName());
                userCompetitionApplyInfoDTO.setCompetitionApplyInfoList(memberApplyInfoList);
                userCompetitionApplyInfoDTO.setGuideTeacherApplyInfoList(guideTeacherApplyInfoList);
//                List<OperationConfig> operationConfigs = operationConfigMapper.selectOperationConfigByCompetitionSeriesId(userCompetitionApplyInfoDTO.getCompetitionSeriesId());
//                userCompetitionApplyInfoDTO.setOperationConfigList(operationConfigs);
                String paramsTeamCode = MapUtils.getString(params, "teamCode");
                //只有查看详情时查询
                if (StringUtils.isNotBlank(paramsTeamCode)) {
                    R<Boolean> running = flowService.getRunning(paramsTeamCode, SecurityConstants.INNER);
                    if (R.isSuccess(running)) {
                        //标记是否可以发起流程 一个团队三种流程同时只能有一个是进行中的
                        userCompetitionApplyInfoDTO.setFlag(!running.getData());
                    }
                    R<Map<String, Object>> retired = flowService.getFlowVariables(paramsTeamCode, SecurityConstants.INNER);
                    // 检查有没有进行中的支付流程 返回值 0-没有，1-有，就这两个值
                    R<Integer> integerR = orderService.checkTeamChangePayOrder(paramsTeamCode, SecurityConstants.INNER);
                    if (R.isSuccess(integerR)) {
                        userCompetitionApplyInfoDTO.setOrderPayFlag(integerR.getData());
                    }
                    if (R.isSuccess(retired) && MapUtils.isNotEmpty(retired.getData())) {
                        userCompetitionApplyInfoDTO.setRetiredAuditInfo((Map<String, Object>) MapUtils.getMap(retired.getData(), "retired", null));
                        userCompetitionApplyInfoDTO.setRepaymentAuditInfo((Map<String, Object>) MapUtils.getMap(retired.getData(), "repayment", null));
                        userCompetitionApplyInfoDTO.setChangeAuditInfo((Map<String, Object>) MapUtils.getMap(retired.getData(), "change", null));
                    }
                }
                userCompetitionApplyInfoDTOList.add(userCompetitionApplyInfoDTO);
            });
            userCompetitionApplyInfoDTOList.stream().forEach(userCompetitionApplyInfoDTO -> {
                // 获取报名信息可调整次数
                operationTimesService.calculateRemainingTimes(userCompetitionApplyInfoDTO);
                R<OperationFlow> innerInfo = flowService.getInnerInfo(userCompetitionApplyInfoDTO.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(innerInfo) && Objects.nonNull(innerInfo.getData())) {
                    userCompetitionApplyInfoDTO.setOperationStatus(innerInfo.getData().getFlowType());
                }
            });
        }
        return userCompetitionApplyInfoDTOList;
    }

    @Override
    public List<UserCompetitionApplyInfoTeam> selectUserCompetitionApplyInfoList(Map params) throws Exception {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        params.put("userId", userId);
//        AuthInfo authInfo = SecurityUtils.getLoginUser().getSysUser().getAuthInfo();
//        if (Objects.nonNull(authInfo)) {
//            // 指导教师
////            params.put("guidTeacherName", authInfo.getRealName());
//            // 适配学生注册定时任务不开启状态
//            params.put("idCard", RsaUtils.decryptByPrivateKey(authInfo.getIdCard()));
//        }
        List<UserCompetitionApplyInfoTeam> teamManagerInfos = teamManagerInfoMapper.selectCompetitionTeamInfoListByPCUserId(params);
        if (CollectionUtils.isNotEmpty(teamManagerInfos)) {
            teamManagerInfos.stream().forEach(teamManagerInfo -> {
                R<OperationFlow> innerInfo = flowService.getInnerInfo(teamManagerInfo.getTeamCode(), SecurityConstants.INNER);
                if (R.isSuccess(innerInfo) && Objects.nonNull(innerInfo.getData())) {
                    teamManagerInfo.setOperationStatus(innerInfo.getData().getFlowType());
                }
            });
//             退赛可以查队员信息retired
            teamManagerInfos.stream().forEach(userCompetitionApplyInfoTeam -> {
                Map<String, Object> paramQuerys = new HashMap<>();
                paramQuerys.put("teamCodeList", userCompetitionApplyInfoTeam.getTeamCode());
                paramQuerys.put("operationStatus", userCompetitionApplyInfoTeam.getOperationStatus());
                List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByPCUserId(paramQuerys);
                if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
                    userCompetitionApplyInfoTeam.setCompetitionName(competitionApplyInfoList.get(0).getCompetitionName());
                    checkUserInfo(competitionApplyInfoList);
                    userCompetitionApplyInfoTeam.setCompetitionApplyInfoList(competitionApplyInfoList);
                }
//                // 获取团队变更旧数据
//                ChangeLog changeLog = changeLogMapper.selectChangeLogListByTeamCode(userCompetitionApplyInfoTeam.getTeamCode());
//                if(Objects.nonNull(changeLog) && StringUtils.isNotEmpty(changeLog.getOldData())){
//                    String oldData = changeLog.getOldData();
//                    List<CompetitionApplyInfo> teamManagerInfoOldList = JSON.parseArray(oldData, CompetitionApplyInfo.class);
//                    userCompetitionApplyInfoTeam.setTeamManagerInfoOldList(teamManagerInfoOldList);
//                }
            });
//            Set<String> stringStream = teamManagerInfos.stream().map(UserCompetitionApplyInfoTeam::getTeamCode).collect(Collectors.toSet());
//            String teamCodeList = stringStream.stream().collect(Collectors.joining(","));
//            params.put("teamCodeList", teamCodeList);
//            if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
//                Map<String, List<CompetitionApplyInfo>> allGrouped = competitionApplyInfoList.stream()
//                        .collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
//                teamManagerInfos.stream().forEach(userCompetitionApplyInfoTeam -> {
//                    List<CompetitionApplyInfo> competitionApplyInfoList1 = allGrouped.get(userCompetitionApplyInfoTeam.getTeamCode());
//                    if (CollectionUtils.isNotEmpty(competitionApplyInfoList1)) {
//                        userCompetitionApplyInfoTeam.setCompetitionName(competitionApplyInfoList1.get(0).getCompetitionName());
//                        checkUserInfo(competitionApplyInfoList1);
//                        userCompetitionApplyInfoTeam.setCompetitionApplyInfoList(competitionApplyInfoList1);
//                    }
//                });
//            }
        }
        return teamManagerInfos;
    }

    @Override
    public boolean hasUserTeamAccess(String teamCode, Long userId) {
        if (StringUtils.isEmpty(teamCode) || userId == null) {
            return false;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("teamCode", teamCode);
        params.put("userId", userId);
        return CollectionUtils.isNotEmpty(teamManagerInfoMapper.selectCompetitionTeamInfoListByPCUserId(params));
    }

    @Transactional
    @Override
    public int updateCompetitionApplyInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam, String operationFlag) throws Exception {
        // 校验是否在变更时间范围中
        if (!checkChangeTime(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), operationFlag)) {
            throw new GlobalException("不在变更时间范围中");
        }
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        String userName;
        if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData().getAuthInfo())) {
            userName = userCenterInfoLogin.getData().getAuthInfo().getRealName();
        } else {
            userName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        }
        // 校验是否操作权限
        if (!checkChangeOperator(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), userCenterInfoLogin.getData(), operationFlag)) {
            throw new GlobalException("没有操作权限");
        }
        List<CompetitionApplyInfo> competitionApplyInfoList = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList();
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
                // 数据校验
                checkPhoneAndEmail(competitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId());
//                checkUpdatePhoneAndEmail(competitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId());
                // 数据校验
//                checkData(competitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId());
            }
            competitionApplyInfoList.stream().forEach(competitionApplyInfo -> {
                CompetitionApplyInfo competitionApplyInfoOld =
                        competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(competitionApplyInfo.getMemberId());
                if (Objects.isNull(competitionApplyInfoOld)) {
                    throw new GlobalException("报名信息不存在");
                }
                protectTeacherCompetitionContestantFields(competitionApplyInfo, competitionApplyInfoOld);
                boolean teacherContestant = ApplyConstants.COMPETITION_TRACK_NAME_TEACHER
                        .equals(competitionApplyInfoOld.getCompetitionTrackName())
                        && !ApplyConstants.TEAM_GUIDE_TEACHER
                        .equals(competitionApplyInfoOld.getCompetitionRoleName());
                competitionApplyInfo.setUpdateTime(DateUtils.getNowDate());
                // 修改用户表用户信息
                if (Objects.nonNull(competitionApplyInfo.getUserId())) {
                    SysUser user = new SysUser();
                    user.setUserId(competitionApplyInfo.getUserId());
                    user.setPhonenumber(competitionApplyInfo.getPhone());
                    user.setEmail(competitionApplyInfo.getEmail());
                    user.setUpdateTime(DateUtils.getNowDate());
                    R<SysUser> userCenterInfoStudent = userService.getUserCenterInfo(competitionApplyInfo.getUserId(), SecurityConstants.INNER);
                    if (R.isSuccess(userCenterInfoStudent) && Objects.nonNull(userCenterInfoStudent.getData())) {
                        SysUser data = userCenterInfoStudent.getData();
                        if (data.getUserName().matches(phoneRegex)) {
                            user.setUserName(user.getPhonenumber());
                        }
                        if (data.getUserName().matches(emailRegex)) {
                            user.setUserName(user.getEmail());
                        }
                        R<SysUser> sysUserR = userService.updateApplyInfoUser(user, SecurityConstants.INNER);
                        if (R.isSuccess(sysUserR) && Objects.nonNull(sysUserR.getData())) {
                            // 修改用户表用户信息手机号或邮箱已经注册过了，将已存在手机号或邮箱对应得user_id更新报名表
                            if (teacherContestant
                                    && !Objects.equals(competitionApplyInfoOld.getUserId(), sysUserR.getData().getUserId())) {
                                throw new GlobalException("教师赛参赛选手手机号或邮箱已绑定其他账号，不能变更");
                            }
                            competitionApplyInfo.setUserId(sysUserR.getData().getUserId());
                        }
                    }
                }
                // 记录日志
                ChangeLog changeLog = new ChangeLog();
                changeLog.setChangeType(userCompetitionApplyInfoTeam.getChangeType());
                changeLog.setMemberId(competitionApplyInfo.getMemberId());
                changeLog.setTeamId(competitionApplyInfoOld.getTeamCode());
                changeLog.setNewData(JSONObject.toJSONString(Arrays.asList(competitionApplyInfo)));
                changeLog.setOldData(JSONObject.toJSONString(Arrays.asList(competitionApplyInfoOld)));
                changeLog.setChangeTime(DateUtils.getNowDate());
                changeLog.setIpAddress(ServletUtils.getRequest().getRemoteAddr());
                changeLog.setOperatorUserId(userId);
                changeLog.setResult("成功");
                // 获取带队老师姓名
//                R<SysUser> userCenterInfo = userService.getUserCenterInfo(competitionApplyInfo.getLeaderTeacherId(), SecurityConstants.INNER);
//                String leaderTeacherName = "";
//                if(R.isSuccess(userCenterInfo)){
//                    SysUser sysUser = userCenterInfo.getData();
//                    if(Objects.nonNull(sysUser.getAuthInfo())){
//                        leaderTeacherName = sysUser.getAuthInfo().getRealName();
//                    }
//                }
                // 判断手机号是否更改
                boolean changePhone = false;
                if (StringUtils.isNotEmpty(competitionApplyInfo.getPhone()) &&
                        !competitionApplyInfo.getPhone().equals(competitionApplyInfoOld.getPhone())) {
                    changePhone = true;
                }
                // 判断邮箱是否更改
                boolean changeEmail = false;
                if (StringUtils.isNotEmpty(competitionApplyInfo.getEmail()) &&
                        !competitionApplyInfo.getEmail().equals(competitionApplyInfoOld.getEmail())) {
                    changeEmail = true;
                }
                //查旧数据
                List<CompetitionApplyInfo> competitionApplyInfoListOld = competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
                List<String> guidTeacherNameList = competitionApplyInfoListOld.stream().filter(applyInfo -> Objects.nonNull(applyInfo.getGuideTeacher())).
                        map(applyInfo -> applyInfo.getGuideTeacher()).collect(Collectors.toList());
                String guidTeacherName = StringUtils.join(guidTeacherNameList, ",");
//                String details = DateUtils.getTime()+"、"+userName+"申请人、申请了"+
//                        competitionApplyInfo.getCompetitionName()+"大赛"+competitionApplyInfo.getCompetitionTrackName()+"赛道"+
//                        competitionApplyInfo.getSecondLevelName()+"赛项"+competitionApplyInfo.getTeamName()+"队伍"+leaderTeacherName+"带队老师，"
//                        +guidTeacherName+"指导老师，"+competitionApplyInfo.getTeamName()+"队伍中，"+competitionApplyInfo.getUserName();
                String details = userName + "申请了";
                if (changePhone && !changeEmail) {
                    details = details + competitionApplyInfo.getCompetitionRoleName() + "进行了“信息调整”，数据变动类型为“" + competitionApplyInfo.getCompetitionRoleName() + "信息调整”，更换前手机号数据为" + competitionApplyInfoOld.getPhone() + "，更换后手机号数据为" + competitionApplyInfo.getPhone() +
                            "，变动详情为：（" + competitionApplyInfo.getPhone() + "手机号变更），结果为成功";
                }
                if (changeEmail && !changePhone) {
                    details = details + competitionApplyInfo.getCompetitionRoleName() + "进行了“信息调整”，数据变动类型为“" + competitionApplyInfo.getCompetitionRoleName() + "信息调整”，更换前邮箱数据为" + competitionApplyInfoOld.getEmail() + "，更换后邮箱数据为" + competitionApplyInfo.getEmail() +
                            "，变动详情为：（" + competitionApplyInfo.getEmail() + "邮箱变更），结果为成功";
                }
                if (changePhone && changeEmail) {
                    details = details + competitionApplyInfo.getCompetitionRoleName() + "进行了“信息调整”，数据变动类型为“" + competitionApplyInfo.getCompetitionRoleName() + "信息调整”，更换前手机数据为" + competitionApplyInfoOld.getPhone() + "，更换后数据为" + competitionApplyInfo.getPhone() +
                            "，变动详情为：（" + competitionApplyInfoOld.getPhone() + "手机号变更），更换前邮箱数据为" + competitionApplyInfoOld.getEmail() + "，更换后邮箱数据为" + competitionApplyInfo.getEmail() +
                            "，变动详情为：（" + competitionApplyInfoOld.getEmail() + "邮箱变更），结果为成功";
                }
                changeLog.setChangeDetails(details);
                changeLog.setCreateBy(userName);
                changeLogService.insertChangeLog(changeLog);
                // 指导老师信息修改
                if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(userCompetitionApplyInfoTeam.getCompetitionRoleName())) {
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    teamMemberRela.setUserId(competitionApplyInfo.getUserId());
                    teamMemberRela.setTeamCode(competitionApplyInfo.getTeamCode());
                    teamMemberRela.setUserName(competitionApplyInfo.getUserName());
                    teamMemberRela.setInstructor(competitionApplyInfo.getUserName());
                    teamMemberRela.setInstructorPhone(competitionApplyInfo.getPhone());
                    teamMemberRela.setInstructorEmail(competitionApplyInfo.getEmail());
                    teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
                    competitionApplyInfo.setGuideTeacherEmail(competitionApplyInfo.getEmail());
                    competitionApplyInfo.setGuideTeacherPhone(competitionApplyInfo.getPhone());
                    competitionApplyInfoMapper.updateCompetitionApplyInfo(competitionApplyInfo);
                } else {
                    competitionApplyInfoMapper.updateCompetitionApplyInfo(competitionApplyInfo);
                }
            });
        }
        return 1;
    }

    //团队人员调整
    @Transactional
    @Override
    public int updateAddCompetitionApplyInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam) throws Exception {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        String userName;
        if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData().getAuthInfo())) {
            userName = userCenterInfoLogin.getData().getAuthInfo().getRealName();
        } else {
            userName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        }
        // 校验是否操作权限
        if (!checkChangeOperator(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), userCenterInfoLogin.getData(), ApplyConstants.CHANGE_TYPE_INFO)) {
            throw new GlobalException("没有操作权限");
        }
        if (!checkChangeTime(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), ApplyConstants.CHANGE_TYPE_INFO)) {
            throw new GlobalException("不在变更时间范围中");
        }
        List<CompetitionApplyInfo> competitionApplyInfoList = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList();
        if (CollectionUtils.isEmpty(competitionApplyInfoList)) {
            throw new GlobalException("团队队员或指导教师不能为空");
        }
        String competitionRoleName = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList().get(0).getCompetitionRoleName();
        List<CompetitionApplyInfo> addCompetitionApplyInfoList = competitionApplyInfoList.stream().
                filter(applyInfo -> !"1".equals(applyInfo.getDelFlag())).collect(Collectors.toList());
        // 非删除数据进行校验
        if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
            // 数据校验
            checkPhoneAndEmail(addCompetitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId());
//            checkUpdatePhoneAndEmail(addCompetitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId());
            checkData(addCompetitionApplyInfoList, userCompetitionApplyInfoTeam.getCompetitionSeriesId(), userCompetitionApplyInfoTeam.getChangeType());
        }
        List<CompetitionApplyInfo> deleteCompetitionApplyInfoList = competitionApplyInfoList.stream().
                filter(applyInfo -> "1".equals(applyInfo.getDelFlag())).collect(Collectors.toList());
        Integer oldTeamCount = 0;
        if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
            String roleName = ApplyConstants.TEAM_MEMBER + "," + ApplyConstants.TEAM_LEADER_MEMBER;
            oldTeamCount = teamMemberRelaMapper.selectTeamMemberRelaCountByTeamCode(userCompetitionApplyInfoTeam.getTeamCode(), roleName);
        } else {
            oldTeamCount = teamMemberRelaMapper.selectTeamMemberRelaCountByTeamCode(userCompetitionApplyInfoTeam.getTeamCode(), ApplyConstants.TEAM_GUIDE_TEACHER);
        }
        //查旧数据
        List<CompetitionApplyInfo> competitionApplyInfoListOld = competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
        // 新数据
        List<CompetitionApplyInfo> competitionApplyInfoListNew = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
            List<Long> memberIds = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getMemberId).toList();
            List<CompetitionApplyInfo> competitionApplyInfos = competitionApplyInfoListOld.stream().
                    filter(applyInfo -> !memberIds.contains(applyInfo.getMemberId())).toList();
            competitionApplyInfoListNew.addAll(competitionApplyInfos);
        } else {
            competitionApplyInfoListNew.addAll(competitionApplyInfoListOld);
        }
        // 拿一个旧队员或指导教师数据，将公有字段值赋值给新增加得队员或指导教师得字段
        CompetitionApplyInfo templateApplyInfo =
                selectChangeTemplateApplyInfo(competitionApplyInfoListOld, competitionRoleName);
        if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList) && Objects.isNull(templateApplyInfo)) {
            throw new GlobalException("团队信息不存在");
        }
        if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
            for (CompetitionApplyInfo addApplyInfo : addCompetitionApplyInfoList) {
                buildChangeCompetitionApplyInfo(addApplyInfo, templateApplyInfo, competitionRoleName);
                competitionApplyInfoListNew.add(addApplyInfo);
            }
        }
        // 团队人员减少
        // 判断队员或指导教师数量是否一致
        List<CompetitionApplyInfo> applyInfoList = null;
        if (CollectionUtils.isEmpty(competitionApplyInfoListNew)) {
            throw new GlobalException("修改参赛者信息数据有误，请检查");
        }
        if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
            applyInfoList = competitionApplyInfoListNew.stream().
                    filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
        } else {
            applyInfoList = competitionApplyInfoListNew.stream().
                    filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
        }
        if (applyInfoList.size() < oldTeamCount) {
            // 人员减少，调用工作流程
            if (CollectionUtils.isEmpty(deleteCompetitionApplyInfoList)) {
                throw new GlobalException("团队人员数量多余修改得队员数量，数量不一致，请检查");
            }
            // 指导老师不调用工作流
            if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("teamCode", userCompetitionApplyInfoTeam.getTeamCode());
                variables.put("operationType", DictConstant.CHANGE);
                variables.put("reason", "队员退出团队自动发起");
                variables.put("teamInfo", userCompetitionApplyInfoTeam.getCompetitionName() + userCompetitionApplyInfoTeam.getCompetitionTrackName() + userCompetitionApplyInfoTeam.getSecondLevelName() + userCompetitionApplyInfoTeam.getTeamName());
                variables.put("ApplicantId", userId);
                variables.put("ApplicantName", userName);
                List<Long> userIds = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getMemberId).toList();
                String userIdStr = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                List<String> userNameList = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getUserName).toList();
                String userNameStr = String.join(",", userNameList);
                variables.put("delUserIds", userIdStr);
                variables.put("delUserNames", userNameStr);
                // 增加人员得信息
                if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                    addCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                        buildChangeCompetitionApplyInfo(applyInfo, templateApplyInfo, competitionRoleName);
                    });
                    variables.put("addUserInfos", JSONObject.toJSONString(addCompetitionApplyInfoList));
                }
                variables.put("oldData", JSONObject.toJSONString(competitionApplyInfoListOld));
                if (CollectionUtils.isEmpty(competitionApplyInfoListOld)) {
                    throw new GlobalException("团队信息不存在");
                }
                variables.put("newData", JSONObject.toJSONString(competitionApplyInfoListNew));
                // 获取组别对应费用
                CompetitionTrackConfig competitionTrackConfig = new CompetitionTrackConfig();
                competitionTrackConfig.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
                // 新组别对应费用
                CompetitionTrackConfig competitionTrackConfigNew = competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfig);
                if (Objects.isNull(competitionTrackConfigNew) || Objects.isNull(competitionTrackConfigNew.getCompetitionConfig())) {
                    throw new GlobalException("组别对应费用不存在");
                }
                int deletePeopleCount = oldTeamCount - applyInfoList.size();
                BigDecimal fee = new BigDecimal(competitionTrackConfigNew.getCompetitionConfig().getFee());
                BigDecimal amount = fee.multiply(BigDecimal.valueOf(deletePeopleCount));
                variables.put("amount", amount);
//                variables.put("changType", );
                //发起减少人员退费流程
                flowService.startByCategory(variables, RETIRED, userCompetitionApplyInfoTeam.getTeamCode(), SecurityConstants.INNER);
                return 1;
            }
            // 指导教师直接删除报名表
//            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
//                List<Long> memberIds = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getMemberId).toList();
//                Long[] memberIdsArray = memberIds.toArray(Long[]::new);
//                competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberIds(memberIdsArray);
//            }
            // 指导教师删除新增人
            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                deleteCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    if ("1".equals(applyInfo.getDelFlag())) {
                        competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(applyInfo.getMemberId());
                        teamMemberRela.setUserId(applyInfo.getUserId());
                        teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                        teamMemberRela.setUserName(applyInfo.getUserName());
                        teamMemberRela.setDelFlag("1");
                        teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                addCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    buildChangeCompetitionApplyInfo(applyInfo, templateApplyInfo, competitionRoleName);
                    if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
                        applyInfo.setPayStatus(DictConstant.PENDING);
                    } else {
                        applyInfo.setPayStatus(DictConstant.PAID);
                        applyInfo.setInvoiceStatus(null);
                        applyInfo.setGuideTeacher(applyInfo.getUserName());
                        applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
                        applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
                    }
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    teamMemberRela.setUserId(applyInfo.getUserId());
                    teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                    teamMemberRela.setTeamRole(applyInfo.getCompetitionRoleName());
                    teamMemberRela.setUserName(applyInfo.getUserName());
                    teamMemberRela.setInstructor(applyInfo.getGuideTeacher());
                    teamMemberRela.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                    teamMemberRela.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                    teamMemberRela.setCreateTime(new Date());
                    fillTeacherCompetitionTeamMemberRela(teamMemberRela, applyInfo);
                    teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
                });
                competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(addCompetitionApplyInfoList);
            }
            addChangeInfo(userCompetitionApplyInfoTeam, competitionApplyInfoListOld, userId, competitionApplyInfoListNew, userName, deleteCompetitionApplyInfoList, addCompetitionApplyInfoList);
            return 1;
        }
        // 团队人员或指导教师增加
        if (applyInfoList.size() > oldTeamCount) {
            if (CollectionUtils.isEmpty(addCompetitionApplyInfoList)) {
                throw new GlobalException("团队人员数量少于修改得人员数量，数量不一致，请检查");
            }
            //人员增加计算需要缴纳费用
            // 获取组别对应费用
            CompetitionTrackConfig competitionTrackConfig = new CompetitionTrackConfig();
            competitionTrackConfig.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
            // 新组别对应费用
            CompetitionTrackConfig competitionTrackConfigNew = competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfig);
            if (Objects.isNull(competitionTrackConfigNew) || Objects.isNull(competitionTrackConfigNew.getCompetitionConfig())) {
                throw new GlobalException("组别对应费用不存在");
            }
            int addPeopleCount = applyInfoList.size() - oldTeamCount;
            BigDecimal fee = new BigDecimal(competitionTrackConfigNew.getCompetitionConfig().getFee());
            BigDecimal amount = fee.multiply(BigDecimal.valueOf(addPeopleCount));
            // 删除人
//            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
//                List<Long> memberIds = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getMemberId).toList();
//                Long[] memberIdsArray = memberIds.toArray(Long[]::new);
//                competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberIds(memberIdsArray);
//            }
            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                deleteCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    if ("1".equals(applyInfo.getDelFlag())) {
                        competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(applyInfo.getMemberId());
                        teamMemberRela.setUserId(applyInfo.getUserId());
                        teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                        teamMemberRela.setUserName(applyInfo.getUserName());
                        teamMemberRela.setDelFlag("1");
                        teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
                    }
                });
            }
            // 指导教师不用缴费
            // 新增人
            if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                addCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    buildChangeCompetitionApplyInfo(applyInfo, templateApplyInfo, competitionRoleName);
                    if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
                        applyInfo.setPayStatus(DictConstant.PENDING);
                    } else {
                        applyInfo.setPayStatus(DictConstant.PAID);
                        applyInfo.setInvoiceStatus(null);
                        applyInfo.setGuideTeacher(applyInfo.getUserName());
                        applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
                        applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
                    }
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    teamMemberRela.setUserId(applyInfo.getUserId());
                    teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                    teamMemberRela.setTeamRole(applyInfo.getCompetitionRoleName());
                    teamMemberRela.setUserName(applyInfo.getUserName());
                    teamMemberRela.setInstructor(applyInfo.getGuideTeacher());
                    teamMemberRela.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                    teamMemberRela.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                    teamMemberRela.setCreateTime(new Date());
                    fillTeacherCompetitionTeamMemberRela(teamMemberRela, applyInfo);
                    teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
                });
                // 增加人算出人数，随机将队员发票状态改为1
                if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)
                        && CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                    // 获取开票未开票人数
                    // 未开票人数
                    List<String> invoiceNoStatusList = deleteCompetitionApplyInfoList.stream()
                            .filter(applyInfoOld -> applyInfoOld.getInvoiceStatus().equals("0"))
                            .map(applyInfoOld -> applyInfoOld.getInvoiceStatus()).toList();
                    // 已开票人数
                    List<String> invoiceStatusList = deleteCompetitionApplyInfoList.stream()
                            .filter(applyInfoOld -> applyInfoOld.getInvoiceStatus().equals("1"))
                            .map(applyInfoOld -> applyInfoOld.getInvoiceStatus()).toList();
                    int invCount = 0;
                    if (CollectionUtils.isNotEmpty(invoiceNoStatusList)) {
                        for (int i = 0; i < invoiceNoStatusList.size(); i++) {
                            addCompetitionApplyInfoList.get(i).setInvoiceStatus("0");
                            invCount++;
                        }
                    }
                    if (CollectionUtils.isNotEmpty(invoiceStatusList)) {
                        for (int i = 0; i < invoiceStatusList.size(); i++) {
                            addCompetitionApplyInfoList.get(invCount + i).setInvoiceStatus("1");
                        }
                    }
//                    for (int i = 0; i < addPeopleCount; i++) {
//                        CompetitionApplyInfo competitionApplyInfo = addCompetitionApplyInfoList.get(i);
//                        competitionApplyInfo.setInvoiceStatus("0");
//                    }
                }
                competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(addCompetitionApplyInfoList);
            }
            if (!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
                TeamChangeDto teamChangeDto = new TeamChangeDto();
                teamChangeDto.setTeamCode(userCompetitionApplyInfoTeam.getTeamCode());
                teamChangeDto.setAmount(amount);
                teamChangeDto.setCommodityType("competition");
                teamChangeDto.setChangeType("change");
                teamChangeDto.setCompetitionSeriesId(userCompetitionApplyInfoTeam.getCompetitionSeriesId().toString());
                teamChangeDto.setEventId(userCompetitionApplyInfoTeam.getCompetitionSeriesId());
                teamChangeDto.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
                // 获取缴费订单团队信息
                ICompetitionApplyInfoService competitionApplyInfoService = SpringUtil.getBean(ICompetitionApplyInfoService.class);
                Map<String, Object> map = new HashMap<>();
                map.put("teamCodeList", Arrays.asList(userCompetitionApplyInfoTeam.getTeamCode()));
                List<CompetitionApplyInfoVO> detailForOrder = competitionApplyInfoService.getDetailForOrder(map);
                if (CollectionUtils.isNotEmpty(detailForOrder)) {
                    List<RegistrationInfo> playersList = new ArrayList<>();
                    Map<String, CompetitionApplyInfoVO> teamMap = detailForOrder.stream().
                            collect(Collectors.toMap(CompetitionApplyInfoVO::getTeamCode, competitionApplyInfoVO -> competitionApplyInfoVO));
                    CompetitionApplyInfoVO competitionApplyInfoVO = teamMap.get(teamChangeDto.getTeamCode());
                    int teamMemberNum = 0;
                    if (Objects.nonNull(competitionApplyInfoVO)) {
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                            deleteCompetitionApplyInfoList.stream().forEach(delUserInfo -> {
                                RegistrationInfo registrationInfo = new RegistrationInfo();
                                BeanUtils.copyProperties(delUserInfo, registrationInfo);
                                registrationInfo.setMemberId(delUserInfo.getMemberId() == null ? null : delUserInfo.getMemberId().toString());
                                playersList.add(registrationInfo);
                            });
                            teamMemberNum = teamMemberNum - deleteCompetitionApplyInfoList.size();
                        }
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                            addCompetitionApplyInfoList.stream().forEach(addUserInfo -> {
                                RegistrationInfo registrationInfo = new RegistrationInfo();
                                BeanUtils.copyProperties(addUserInfo, registrationInfo);
                                playersList.add(registrationInfo);
                            });
                            teamMemberNum = teamMemberNum + addCompetitionApplyInfoList.size();
                        }
                        competitionApplyInfoVO.setPlayersList(playersList);
//                        competitionApplyInfoVO.setInstructorList(null);
                        // 修改团队人数及费用
                        if (StringUtils.isNotEmpty(competitionApplyInfoVO.getFee())) {
                            BigDecimal subtotal = new BigDecimal(competitionApplyInfoVO.getFee()).multiply(BigDecimal.valueOf(Math.abs(teamMemberNum)));
                            competitionApplyInfoVO.setSubtotal(subtotal.toString());
                        }
                        competitionApplyInfoVO.setTeamSize(Math.abs(teamMemberNum));
                    }
                    teamChangeDto.setTeamNewInfo(JSONObject.toJSONString(detailForOrder));
                }
                orderService.createPayOrderByTeamChange(teamChangeDto, SecurityConstants.INNER);
            }
            // 记录日志
            addChangeInfo(userCompetitionApplyInfoTeam, competitionApplyInfoListOld, userId, competitionApplyInfoListNew, userName, deleteCompetitionApplyInfoList, addCompetitionApplyInfoList);
            return 1;
        }
        // 获取带队老师姓名
//        R<SysUser> userCenterInfo = userService.getUserCenterInfo(competitionApplyInfoListOld.get(0).getLeaderTeacherId(), SecurityConstants.INNER);
//        String leaderTeacherName = "";
//        if(R.isSuccess(userCenterInfo)){
//            SysUser sysUser = userCenterInfo.getData();
//            if(Objects.nonNull(sysUser.getAuthInfo())){
//                leaderTeacherName = sysUser.getAuthInfo().getRealName();
//            }
//        }
        // 指导老师人数修改
        if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                deleteCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    if ("1".equals(applyInfo.getDelFlag())) {
                        competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(applyInfo.getMemberId());
                        teamMemberRela.setUserId(applyInfo.getUserId());
                        teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                        teamMemberRela.setUserName(applyInfo.getUserName());
                        teamMemberRela.setDelFlag("1");
                        teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                addCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    buildChangeCompetitionApplyInfo(applyInfo, templateApplyInfo, competitionRoleName);
                    applyInfo.setPayStatus(DictConstant.PAID);
                    applyInfo.setInvoiceStatus(null);
                    applyInfo.setGuideTeacher(applyInfo.getUserName());
                    applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
                    applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
                    applyInfo.setCreateTime(DateUtils.getNowDate());
                    competitionApplyInfoMapper.insertCompetitionApplyInfo(applyInfo);
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    teamMemberRela.setUserId(applyInfo.getUserId());
                    teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                    teamMemberRela.setTeamRole(applyInfo.getCompetitionRoleName());
                    teamMemberRela.setUserName(applyInfo.getUserName());
                    teamMemberRela.setInstructor(applyInfo.getGuideTeacher());
                    teamMemberRela.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                    teamMemberRela.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                    teamMemberRela.setCreateTime(DateUtils.getNowDate());
                    fillTeacherCompetitionTeamMemberRela(teamMemberRela, applyInfo);
                    teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
//                    this.syncCompetitionApplyInfo();
                });
            }
        } else {
            // 走团队总人数不变逻辑
            if (CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)) {
                deleteCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    if ("1".equals(applyInfo.getDelFlag())) {
                        competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(applyInfo.getMemberId());
                        teamMemberRela.setUserId(applyInfo.getUserId());
                        teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                        teamMemberRela.setUserName(applyInfo.getUserName());
                        teamMemberRela.setDelFlag("1");
                        teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                for (int i = 0; i < addCompetitionApplyInfoList.size(); i++) {
                    CompetitionApplyInfo applyInfo = addCompetitionApplyInfoList.get(i);
                    buildChangeCompetitionApplyInfo(applyInfo, templateApplyInfo, competitionRoleName);
                    applyInfo.setPayStatus(DictConstant.PAID);
                    if (Objects.nonNull(deleteCompetitionApplyInfoList.get(i))) {
                        applyInfo.setInvoiceStatus(deleteCompetitionApplyInfoList.get(i).getInvoiceStatus());
                    }
                    competitionApplyInfoMapper.insertCompetitionApplyInfo(applyInfo);
                    TeamMemberRela teamMemberRela = new TeamMemberRela();
                    teamMemberRela.setUserId(applyInfo.getUserId());
                    teamMemberRela.setTeamCode(applyInfo.getTeamCode());
                    teamMemberRela.setTeamRole(applyInfo.getCompetitionRoleName());
                    teamMemberRela.setUserName(applyInfo.getUserName());
                    teamMemberRela.setInstructor(applyInfo.getGuideTeacher());
                    teamMemberRela.setInstructorPhone(applyInfo.getGuideTeacherPhone());
                    teamMemberRela.setInstructorEmail(applyInfo.getGuideTeacherEmail());
                    teamMemberRela.setCreateTime(DateUtils.getNowDate());
                    fillTeacherCompetitionTeamMemberRela(teamMemberRela, applyInfo);
                    teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
                }
            }
        }
        // 记录日志
        addChangeInfo(userCompetitionApplyInfoTeam, competitionApplyInfoListOld, userId, competitionApplyInfoListNew, userName, deleteCompetitionApplyInfoList, addCompetitionApplyInfoList);

        return 1;
    }

    //记录日志
    private void addChangeInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam,
                               List<CompetitionApplyInfo> competitionApplyInfoListOld, Long userId,
                               List<CompetitionApplyInfo> competitionApplyInfoListNew, String userName,
                               List<CompetitionApplyInfo> deleteCompetitionApplyInfoList,
                               List<CompetitionApplyInfo> addCompetitionApplyInfoList) {
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(userCompetitionApplyInfoTeam.getChangeType());
        changeLog.setTeamId(userCompetitionApplyInfoTeam.getTeamCode());
        changeLog.setOldData(JSONObject.toJSONString(competitionApplyInfoListOld));
        changeLog.setChangeTime(DateUtils.getNowDate());
        changeLog.setIpAddress(ServletUtils.getRequest().getRemoteAddr());
        changeLog.setOperatorUserId(userId);
        changeLog.setNewData(JSONObject.toJSONString(competitionApplyInfoListNew));
        String details = getChangeDetail(userName, userCompetitionApplyInfoTeam, competitionApplyInfoListNew, competitionApplyInfoListOld, deleteCompetitionApplyInfoList, addCompetitionApplyInfoList);
        changeLog.setChangeDetails(details);
        changeLog.setCreateBy(userName);
        changeLog.setResult("成功");
        changeLogService.insertChangeLog(changeLog);
    }

    private String getChangeDetail(String userName, UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam, List<CompetitionApplyInfo> competitionApplyInfoListNew, List<CompetitionApplyInfo> competitionApplyInfoListOld,
                                   List<CompetitionApplyInfo> deleteCompetitionApplyInfoList, List<CompetitionApplyInfo> addCompetitionApplyInfoList) {
        List<String> memberNameOldList = competitionApplyInfoListOld.stream().
                map(applyInfo -> applyInfo.getUserName()).collect(Collectors.toList());
        List<String> memberNameNewList = competitionApplyInfoListNew.stream().
                map(applyInfo -> applyInfo.getUserName()).collect(Collectors.toList());
        String memberNameStr = StringUtils.join(memberNameOldList, ",");
        String memberNameNewStr = StringUtils.join(memberNameNewList, ",");
        List<String> deleteMemberNameList = deleteCompetitionApplyInfoList.stream().
                map(applyInfo -> applyInfo.getUserName()).collect(Collectors.toList());
        String deleteMemberName = StringUtils.join(deleteMemberNameList, ",");
        List<String> addMemberNameList = addCompetitionApplyInfoList.stream().
                map(applyInfo -> applyInfo.getUserName()).collect(Collectors.toList());
        String addMemberName = StringUtils.join(addMemberNameList, ",");
        List<String> guidTeacherNameOldList = competitionApplyInfoListOld.stream().filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).
                map(applyInfo -> applyInfo.getGuideTeacher()).collect(Collectors.toList());
        List<String> guidTeacherNameNewList = competitionApplyInfoListNew.stream().filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).
                map(applyInfo -> applyInfo.getGuideTeacher()).collect(Collectors.toList());
        if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(userCompetitionApplyInfoTeam.getCompetitionRoleName())) {
//            String details = DateUtils.getTime()+"、"+userName+"申请人、申请了"+
//                    userCompetitionApplyInfoTeam.getCompetitionName()+"大赛"+userCompetitionApplyInfoTeam.getCompetitionTrackName()+"赛道"+
//                    userCompetitionApplyInfoTeam.getSecondLevelName()+"赛项"+userCompetitionApplyInfoTeam.getTeamName()+"队伍，"+leaderTeacherName+"带队老师，"
//                    +guidTeacherName+"指导老师，";
            String guidTeacherNameOld = StringUtils.join(guidTeacherNameOldList, ",");
            String guidTeacherNameNew = StringUtils.join(guidTeacherNameNewList, ",");
            String details = userName + "申请了" + userCompetitionApplyInfoTeam.getTeamName() + "队伍中，进行了“指导教师变更”，数据变动类型为“指导教师变更”，" +
                    "变更前数据为" + guidTeacherNameOld + "，变更后数据为" + guidTeacherNameNew + "，" +
                    "变动详情为：（删除" + deleteMemberName + "指导教师，新增" + addMemberName + "指导教师），结果为成功";
            return details;
        } else {
            String details = userName + "申请了" + userCompetitionApplyInfoTeam.getTeamName() + "队伍中，进行了“队员变更”，数据变动类型为“队员变更”，" +
                    "变更前数据为" + memberNameStr + "，变更后数据为" + memberNameNewStr + "，" +
                    "变动详情为：（删除" + deleteMemberName + "队员，新增" + addMemberName + "队员），结果为成功";
            return details;
        }
    }

    private void buildChangeCompetitionApplyInfo(CompetitionApplyInfo addApplyInfo,
                                                CompetitionApplyInfo templateApplyInfo,
                                                String competitionRoleName) {
        buildAddCompetitionApplyInfoList(addApplyInfo, templateApplyInfo);
        // 从其他角色记录复制公共字段时，必须恢复本次新增人员的目标角色。
        restoreChangeRoleFields(addApplyInfo, competitionRoleName);
    }

    static CompetitionApplyInfo selectChangeTemplateApplyInfo(List<CompetitionApplyInfo> existingApplyInfoList,
                                                              String competitionRoleName) {
        return existingApplyInfoList.stream()
                .filter(applyInfo -> Objects.equals(competitionRoleName, applyInfo.getCompetitionRoleName()))
                .findFirst()
                .orElseGet(() -> existingApplyInfoList.stream().findFirst().orElse(null));
    }

    static void restoreChangeRoleFields(CompetitionApplyInfo addApplyInfo, String competitionRoleName) {
        addApplyInfo.setCompetitionRoleName(competitionRoleName);
        if (ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)) {
            addApplyInfo.setGuideTeacher(addApplyInfo.getUserName());
            addApplyInfo.setGuideTeacherPhone(addApplyInfo.getPhone());
            addApplyInfo.setGuideTeacherEmail(addApplyInfo.getEmail());
        }
    }

    private void fillTeacherCompetitionTeamMemberRela(TeamMemberRela teamMemberRela,
                                                      CompetitionApplyInfo applyInfo) {
        if (ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName())) {
            teamMemberRela.setCheckStatus(Constants.JOIN_TEAM_AGREE);
            teamMemberRela.setOrgId(applyInfo.getOrgId());
        }
    }

    private void buildAddCompetitionApplyInfoList(CompetitionApplyInfo addApplyInfoListNew, CompetitionApplyInfo OldCompetitionApplyInfo) {
        addApplyInfoListNew.setCompetitionSeriesId(OldCompetitionApplyInfo.getCompetitionSeriesId());
        addApplyInfoListNew.setCompetitionTrackId(OldCompetitionApplyInfo.getCompetitionTrackId());
        addApplyInfoListNew.setCompetitionName(OldCompetitionApplyInfo.getCompetitionName());
        addApplyInfoListNew.setCompetitionRoleName(OldCompetitionApplyInfo.getCompetitionRoleName());
        addApplyInfoListNew.setCompetitionTrackName(OldCompetitionApplyInfo.getCompetitionTrackName());
        addApplyInfoListNew.setCompetitionTrackType(OldCompetitionApplyInfo.getCompetitionTrackType());
        addApplyInfoListNew.setTeamName(OldCompetitionApplyInfo.getTeamName());
        addApplyInfoListNew.setSecondLevelCode(OldCompetitionApplyInfo.getSecondLevelCode());
        addApplyInfoListNew.setSecondLevelName(OldCompetitionApplyInfo.getSecondLevelName());
        addApplyInfoListNew.setLeaderTeacherId(OldCompetitionApplyInfo.getLeaderTeacherId());
        addApplyInfoListNew.setLeaderTeacher(OldCompetitionApplyInfo.getLeaderTeacher());
        addApplyInfoListNew.setLeaderTeacherPhone(OldCompetitionApplyInfo.getLeaderTeacherPhone());
        addApplyInfoListNew.setTeamCode(OldCompetitionApplyInfo.getTeamCode());
        addApplyInfoListNew.setInvoiceStatus("0");
        addApplyInfoListNew.setSchool(OldCompetitionApplyInfo.getSchool());
        addApplyInfoListNew.setSchoolName(OldCompetitionApplyInfo.getSchoolName());
        addApplyInfoListNew.setProfession(OldCompetitionApplyInfo.getProfession());
        addApplyInfoListNew.setClassInfo(OldCompetitionApplyInfo.getClassInfo());
        addApplyInfoListNew.setDepartmentName(OldCompetitionApplyInfo.getDepartmentName());
        addApplyInfoListNew.setCheckStatus(Constants.CHECK_PASS);
        addApplyInfoListNew.setNationality(OldCompetitionApplyInfo.getNationality());
        addApplyInfoListNew.setCreateTime(DateUtils.getNowDate());
        addApplyInfoListNew.setRegistrationTime(DateUtils.getNowDate());
        addApplyInfoListNew.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getNickName() + "");
        // 进行实名认证
        // 不是身份证不掉实名认证接口
        if (StringUtils.isNotEmpty(addApplyInfoListNew.getIdCard()) && addApplyInfoListNew.getIdCard().length() >= 15) {
            addApplyInfoListNew.setIdCardType("1");
            AuthInfo authInfo = new AuthInfo();
            authInfo.setRealName(addApplyInfoListNew.getUserName());
            authInfo.setIdCard(addApplyInfoListNew.getIdCard());
            R<Map<String, Object>> authenticationMapR = userService.saveInnerAuthInfo(authInfo, SecurityConstants.INNER);
            if (R.isSuccess(authenticationMapR)) {
                if (!Boolean.parseBoolean(String.valueOf(authenticationMapR.getData().get("isok")))) {
                    throw new GlobalException(addApplyInfoListNew.getUserName() + "实名认证失败");
                } else {
                    Map<String, Object> authenticationMap = authenticationMapR.getData();
                    if (Objects.nonNull(authenticationMap.get("IdCardInfor"))) {
                        Map idCardInfor = (Map) authenticationMap.get("IdCardInfor");
                        addApplyInfoListNew.setSex(idCardInfor.get("sex").toString());
                    }
                }
            }
        } else {
            addApplyInfoListNew.setIdCardType("2");
        }
    }

    // 换组别
    @Override
    public int updateCompetitionApplyTeamInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam) throws Exception {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        String userName;
        if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData().getAuthInfo())) {
            userName = userCenterInfoLogin.getData().getAuthInfo().getRealName();
        } else {
            userName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        }
        if (!checkChangeTime(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), ApplyConstants.CHANGE_TYPE_INFO)) {
            throw new GlobalException("不在变更时间范围中");
        }
        // 校验是否操作权限
        if (!checkChangeOperator(userCompetitionApplyInfoTeam.getCompetitionSeriesId(), userCenterInfoLogin.getData(), ApplyConstants.CHANGE_TYPE_INFO)) {
            throw new GlobalException("没有操作权限");
        }
        // 获取组别对应费用
        CompetitionTrackConfig competitionTrackConfig = new CompetitionTrackConfig();
        competitionTrackConfig.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
        // 新组别对应费用
        CompetitionTrackConfig competitionTrackConfigNew = competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfig);
        // 旧组别对应费用
        List<CompetitionApplyInfo> competitionApplyInfoListOld = competitionApplyInfoMapper.selectCompetitionApplyInfoListByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
        competitionTrackConfig.setSecondLevelCode(competitionApplyInfoListOld.get(0).getSecondLevelCode());
        CompetitionTrackConfig competitionTrackConfigOld = competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfig);
        if (!competitionTrackConfigNew.getCompetitionConfig().getFee().equals(competitionTrackConfigOld.getCompetitionConfig().getFee())) {
            throw new GlobalException("组别更换费用不一致,暂不支持报名费用不一致情况更换组别");
        }
        List<CompetitionApplyInfo> competitionApplyInfoListNew = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(competitionApplyInfoListOld)) {
            competitionApplyInfoListOld.stream().forEach(competitionApplyInfoOld -> {
                CompetitionApplyInfo competitionApplyInfoNew = new CompetitionApplyInfo();
                BeanUtils.copyProperties(competitionApplyInfoOld, competitionApplyInfoNew);
                competitionApplyInfoNew.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
                competitionApplyInfoNew.setSecondLevelName(userCompetitionApplyInfoTeam.getSecondLevelName());
                competitionApplyInfoNew.setUpdateBy(SecurityUtils.getUserId() + "");
                competitionApplyInfoNew.setUpdateTime(DateUtils.getNowDate());
                competitionApplyInfoListNew.add(competitionApplyInfoNew);
            });
        }
        // 数据进行校验
        if (CollectionUtils.isNotEmpty(competitionApplyInfoListNew)) {
            // 数据校验
            checkData(competitionApplyInfoListNew, userCompetitionApplyInfoTeam.getCompetitionSeriesId(), ApplyConstants.OPERATION_CHANGE_GROUP);
        }
        // 记录日志
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(userCompetitionApplyInfoTeam.getChangeType());
        changeLog.setTeamId(userCompetitionApplyInfoTeam.getTeamCode());
        changeLog.setNewData(JSONObject.toJSONString(competitionApplyInfoListNew));
        changeLog.setOldData(JSONObject.toJSONString(competitionApplyInfoListOld));
        changeLog.setChangeTime(DateUtils.getNowDate());
        changeLog.setIpAddress(ServletUtils.getRequest().getRemoteAddr());
        changeLog.setOperatorUserId(userId);
        // 获取带队老师姓名
//        R<SysUser> userCenterInfo = userService.getUserCenterInfo(competitionApplyInfoListOld.get(0).getLeaderTeacherId(), SecurityConstants.INNER);
//        String leaderTeacherName = "";
//        if(R.isSuccess(userCenterInfo)){
//            SysUser sysUser = userCenterInfo.getData();
//            if(Objects.nonNull(sysUser.getAuthInfo())){
//                leaderTeacherName = sysUser.getAuthInfo().getRealName();
//            }
//        }
        List<String> guidTeacherNameList = competitionApplyInfoListOld.stream().filter(applyInfo -> Objects.nonNull(applyInfo.getGuideTeacher()))
                .map(applyInfo -> applyInfo.getGuideTeacher()).collect(Collectors.toList());
        String guidTeacherName = StringUtils.join(guidTeacherNameList, ",");
//        String detail = DateUtils.getTime()+"、"+userName+"申请人、申请了"+
//                userCompetitionApplyInfoTeam.getCompetitionName()+"大赛"+userCompetitionApplyInfoTeam.getCompetitionTrackName()+"赛道"+
//                competitionApplyInfoListOld.get(0).getSecondLevelName()+"赛项";
        String details = userName + "申请了" + userCompetitionApplyInfoTeam.getTeamName() + "队伍中，“更换组别”，数据变动类型为“更换组别”，" +
                "更换前组别数据为" + competitionApplyInfoListOld.get(0).getSecondLevelName() + "，更换后组别数据为" + competitionApplyInfoListNew.get(0).getSecondLevelName() + "，" +
                "变动详情为：（由" + competitionApplyInfoListOld.get(0).getSecondLevelName() + "组别变更为" + competitionApplyInfoListNew.get(0).getSecondLevelName() + "组别），结果为成功";
//                userCompetitionApplyInfoTeam.getTeamName()+"队伍"+leaderTeacherName+"带队老师，"
//                +guidTeacherName+"指导老师，"+
        changeLog.setChangeDetails(details);
        changeLog.setCreateBy(userName);
        changeLog.setResult("成功");
        changeLogService.insertChangeLog(changeLog);
        // 是否需要退费补费(暂不做)
        TeamManagerInfo teamManagerInfo = new TeamManagerInfo();
        teamManagerInfo.setTeamCode(userCompetitionApplyInfoTeam.getTeamCode());
        teamManagerInfo.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
        teamManagerInfo.setSecondLevelName(userCompetitionApplyInfoTeam.getSecondLevelName());
        teamManagerInfoMapper.updateTeamManagerInfo(teamManagerInfo);
        return competitionApplyInfoMapper.updateSecondLevel(competitionApplyInfoListNew);
    }

    @Override
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigInfo(CompetitionTrackConfig competitionTrackConfig) {
        return competitionTrackConfigMapper.selectCompetitionTrackConfigByName(competitionTrackConfig);
    }

    @Override
    public List<OperationConfig> selectCompetitionOperationConfigInfo(Long competitionSeriesId) {
        return operationConfigMapper.selectOperationConfigByCompetitionSeriesId(competitionSeriesId);
    }

    private boolean checkChangeTime(Long competitionSeriesId, String operationFlag) {
        AtomicBoolean result = new AtomicBoolean(false);
        List<OperationConfig> operationConfigs =
                operationConfigMapper.selectOperationConfigByCompetitionSeriesId(competitionSeriesId);
        if (CollectionUtils.isNotEmpty(operationConfigs)) {
            operationConfigs.stream().forEach(operationConfig -> {
                if (operationFlag.equals(operationConfig.getOperationType())) {
                    List<Map> allowedTimeRanges = JSONUtil.toList(operationConfig.getAllowedTimeRanges(), Map.class);
                    // 如果一次在时间范围内即满足条件
                    if (CollectionUtils.isNotEmpty(allowedTimeRanges)) {

                        allowedTimeRanges.stream().forEach(allowedTimeRange -> {
                            if (DateUtils.isInTimeRange(allowedTimeRange.get("start").toString(), allowedTimeRange.get("end").toString())) {
                                result.set(true);
                            }
                        });
                    }
                }
            });
        }
        return result.get();
    }

    @Override
    public boolean checkChangeOperator(Long competitionSeriesId, SysUser sysUser, String operationFlag) {
        AtomicBoolean result = new AtomicBoolean(false);
        List<OperationConfig> operationConfigs =
                operationConfigMapper.selectOperationConfigByCompetitionSeriesId(competitionSeriesId);
        if (CollectionUtils.isNotEmpty(operationConfigs)) {
            // 当前用户操作身份
            List<IdentityInfo> identityInfoList = sysUser.getIdentityInfoList();
            if (CollectionUtils.isNotEmpty(identityInfoList)) {
                List<String> identityTypeList = identityInfoList.stream().map(IdentityInfo::getCertificationType).collect(Collectors.toList());
                // allowedUserTypes
                operationConfigs.stream().forEach(operationConfig -> {
                    if (operationFlag.equals(operationConfig.getOperationType())) {
                        List<String> allowedUserTypes = List.of(operationConfig.getAllowedUserTypes().split(","));
                        // 如果一次在时间范围内即满足条件
                        if (CollectionUtils.isNotEmpty(allowedUserTypes)) {
                            // leaderTeacher,guidTeacher
                            for (String type : identityTypeList) {
                                if (type.equals(Constants.IDENTITY_TYPE_TEACHER) && allowedUserTypesTeacher.stream().anyMatch(allowedUserTypes::contains)) {
                                    result.set(true);
                                }
                                if (type.equals(Constants.IDENTITY_TYPE_STUDENT) && allowedUserTypesMember.stream().anyMatch(allowedUserTypes::contains)) {
                                    result.set(true);
                                }
                            }
                        }
                    }
                });
            }
        }
        return result.get();
    }

    // 校验学生用户信息是否与报名信息一致
    private void checkUserInfo(List<CompetitionApplyInfo> competitionApplyInfoList1) {
        List<Long> userIdList = competitionApplyInfoList1.stream()
                .filter(applyInfo -> Objects.nonNull(applyInfo.getUserId()) && !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                .map(CompetitionApplyInfo::getUserId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIdList)) {
            R<List<SysUser>> userCenterInfoList = userService.getUserCenterInfoList(userIdList, SecurityConstants.INNER);
            if (R.isSuccess(userCenterInfoList)) {
                Map<Long, List<SysUser>> userCenterInfoListMap =
                        userCenterInfoList.getData().stream().collect(Collectors.groupingBy(SysUser::getUserId));
                competitionApplyInfoList1.stream().forEach(applyInfo -> {
                    List<SysUser> sysUserList = userCenterInfoListMap.get(applyInfo.getUserId());
                    if (CollectionUtils.isNotEmpty(sysUserList)) {
                        SysUser sysUser = sysUserList.get(0);
                        AuthInfo authInfo = sysUser.getAuthInfo();
                        StringBuffer userInfoFlagSB = new StringBuffer();
                        Map userInfoDateMap = new HashMap();
                        if (Objects.nonNull(authInfo)) {
                            if (!applyInfo.getUserName().equals(authInfo.getRealName()) && !applyInfo.getIdCard().equals(authInfo.getIdCard())) {
                                // 报名信息与用户信息不一致
                                userInfoFlagSB.append("3").append(",");
                            }
                            if (StringUtils.isNotEmpty(applyInfo.getPhone()) && !applyInfo.getPhone().equals(sysUser.getPhonenumber())) {
                                userInfoFlagSB.append("2").append(",");
                                userInfoDateMap.put("phone", sysUser.getPhonenumber());
                            }
                            if (StringUtils.isNotEmpty(applyInfo.getEmail()) && !applyInfo.getEmail().equals(sysUser.getEmail())) {
                                userInfoFlagSB.append("1").append(",");
                                userInfoDateMap.put("email", sysUser.getEmail());
                            }
                            if (StringUtils.isEmpty(userInfoFlagSB)) {
                                userInfoFlagSB.append("0").append(",");
                            }
                            applyInfo.setUserInfoFlag(userInfoFlagSB.substring(0, userInfoFlagSB.length() - 1).toString());
                            applyInfo.setUserInfoDateList(userInfoDateMap);
                        } else {
                            applyInfo.setUserInfoFlag("0");
                        }
                    } else {
                        applyInfo.setUserInfoFlag("0");
                    }
                });
            }
        }
    }

    @NotNull
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
                if (ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(applyInfo.getCompetitionTrackName())) {
                    teamManagerInfo.setUserId(applyInfo.getUserId());
                    teamManagerInfo.setOrgId(applyInfo.getOrgId());
                }
            }
        });
        return teamManagerInfo;
    }

    // 个人参赛报名
    public int userApplyCompetitionInfo(CompetitionConfig competitionConfig, UserApplyCompetitionReq req, SysUser sysUserInfo) {
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        BeanUtils.copyProperties(req, competitionApplyInfo);
        competitionApplyInfo.setUserId(sysUserInfo.getUserId());
        competitionApplyInfo.setOrgId(sysUserInfo.getOrgId());
        competitionApplyInfo.setJoinType(Constants.JOIN_TYPE_PERSON);
        R<SysUser> userInfo = userService.getUserCenterInfo(competitionApplyInfo.getUserId(), SecurityConstants.INNER);
        if (userInfo.getCode() != HttpStatus.SUCCESS && userInfo.getData() == null) {
            throw new RuntimeException("用户不存在");
        }
        SysUser sysUser = userInfo.getData();
        setUserInfo(sysUser, competitionApplyInfo, req);
        competitionApplyInfo.setCheckStatus(Constants.NO_CHECK);
        competitionApplyInfo.setRegistrationTime(DateUtils.getNowDate());
        // 同一赛事，同一个用户，不能重复报名
        competitionApplyInfo.setCheckStatusContain("Y");
        CompetitionApplyInfo competitionApply = competitionApplyInfoMapper.selectCompetitionApplyInfoByUserId(competitionApplyInfo);
        if (competitionApply != null) {
            throw new GlobalException("用户已报名,不能重复报名");
        }
        // 新增审核任务记录
        competitionApplyInfoMapper.insertCompetitionApplyInfo(competitionApplyInfo);
        insertSysAuditTask(competitionApplyInfo);
        return 1;
    }

    // 团队参赛报名
    public int teamApplyCompetitionInfo(CompetitionConfig competitionConfig, UserApplyCompetitionReq req, SysUser sysUserInfo) {

        R<SysUser> userInfo = userService.getUserCenterInfo(sysUserInfo.getUserId(), SecurityConstants.INNER);
        if (userInfo.getCode() != HttpStatus.SUCCESS && userInfo.getData() == null) {
            throw new GlobalException("用户不存在");
        }
        SysUser sysUser = userInfo.getData();
        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
        BeanUtils.copyProperties(req, competitionApplyInfo);
        competitionApplyInfo.setUserId(sysUserInfo.getUserId());
        competitionApplyInfo.setOrgId(sysUserInfo.getOrgId());
        competitionApplyInfo.setCreateBy(sysUserInfo.getUserId() + "");
        competitionApplyInfo.setJoinType(Constants.JOIN_TYPE_TEAM);
        // 获取团队成员
        TeamMemberRela teamMemberRela = new TeamMemberRela();
        teamMemberRela.setTeamCode(req.getTeamCode());
        // 团队成员列表
        List<TeamMemberRela> teamMemberRelaList = teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
        req.setTeamMemberRelaList(teamMemberRelaList);
        setUserInfo(sysUser, competitionApplyInfo, req);
        // 队长报名校验
        competitionApplyInfoCheckService.checkApplyCompetition(competitionConfig, req, sysUser);
        // 团员报名校验
        competitionApplyInfoCheckService.checkTeamMemberCompetition(competitionConfig, teamMemberRelaList);
        // 队长信息id及团队code入赛事申请表
        competitionApplyInfo.setRegistrationTime(DateUtils.getNowDate());
        // 同一赛事，同一个用户，不能重复报名
        competitionApplyInfo.setCheckStatusContain("Y");
        CompetitionApplyInfo competitionApply = competitionApplyInfoMapper.selectCompetitionApplyInfoByUserId(competitionApplyInfo);
        if (competitionApply != null) {
            throw new GlobalException("用户已报名,不能重复报名");
        }
        competitionApplyInfo.setCheckStatus(Constants.NO_CHECK);
        // 新增审核任务记录
        competitionApplyInfoMapper.insertCompetitionApplyInfo(competitionApplyInfo);
        insertSysAuditTask(competitionApplyInfo);
        return 1;
    }

    private void insertSysAuditTask(CompetitionApplyInfo competitionApplyInfo) {
        orderService.innerAddAuditTask(TdConstants.AUDIT_FLOW_TYPE_APPLY, competitionApplyInfo.getMemberId(), SecurityConstants.INNER);
    }

    // 设置个人信息
    private void setUserInfo(SysUser sysUser, CompetitionApplyInfo competitionApplyInfo, UserApplyCompetitionReq req) {
        List<IdentityInfo> identityInfoList = sysUser.getIdentityInfoList();
        if (CollectionUtils.isNotEmpty(identityInfoList)) {
            for (IdentityInfo identityInfo : identityInfoList) {
                if (identityInfo.getCertificationType().equals(Constants.IDENTITY_TYPE_STUDENT)) {
                    competitionApplyInfo.setClassInfo(identityInfo.getClassInfo());
                    competitionApplyInfo.setEmployeeCode(identityInfo.getEmployeeCode());
                    competitionApplyInfo.setProfession(identityInfo.getSpecialty());
                    req.setClassInfo(identityInfo.getClassInfo());
                    req.setEmployeeCode(identityInfo.getEmployeeCode());
                    req.setProfession(identityInfo.getSpecialty());
                }
            }
        }
        competitionApplyInfo.setCreateTime(DateUtils.getNowDate());
        competitionApplyInfo.setPhone(sysUser.getPhonenumber());
        competitionApplyInfo.setEmail(sysUser.getEmail());
        AuthInfo authInfo = sysUser.getAuthInfo();
        if (authInfo != null) {
            competitionApplyInfo.setIdCard(authInfo.getIdCard());
            competitionApplyInfo.setUserName(authInfo.getRealName());
        } else {
            competitionApplyInfo.setUserName(sysUser.getUserName());
        }
    }

    /**
     * 更新团队成员顺序
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitionApplyInfoTeamSort(Map<String,Object> params) {
        Long memberId = MapUtils.getLong(params,"memberId");
        String type = MapUtils.getString(params,"change");
        params.remove("memberId");
        params.remove("change");
        //只用teamCode和competitionSeriesId查
        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher(params);
        if (CollectionUtils.isEmpty(applyInfoList)) {
            throw new GlobalException("未找到该团队成员");
        }
        List<CompetitionApplyInfo> updateCompetitionApplyInfo = new ArrayList<>();
        // 按teamSort升序
        applyInfoList.sort(Comparator.comparing(CompetitionApplyInfo::getTeamSort));
        int index = IntStream.range(0, applyInfoList.size())
                .filter(i -> memberId.equals(applyInfoList.get(i).getMemberId()))
                .findFirst()
                .orElseThrow(() -> new GlobalException("未找到该团队成员信息"));
        if (ApplyConstants.TEAM_SORT_UP.equals(type)) {
            if (index == 0) {
                throw new GlobalException("已位于队列第一位，无法继续向上调整");
            }
            updateCompetitionApplyInfo = swapTeamSort(applyInfoList.get(index), applyInfoList.get(index - 1));
        } else if (ApplyConstants.TEAM_SORT_DOWN.equals(type)) {
            if (index == applyInfoList.size() - 1) {
                throw new GlobalException("已位于队列最后一位，无法继续向下调整");
            }
            updateCompetitionApplyInfo = swapTeamSort(applyInfoList.get(index), applyInfoList.get(index + 1));
        } else {
            throw new GlobalException("type参数错误");
        }
        return competitionApplyInfoMapper.batchUpdateCompetitionApplyInfoTeamSort(updateCompetitionApplyInfo);
    }

    /**
     * 交换两个对象的teamSort值
     */
    private List<CompetitionApplyInfo> swapTeamSort(CompetitionApplyInfo a, CompetitionApplyInfo b) {
        List<CompetitionApplyInfo> updateCompetitionApplyInfo = new ArrayList<>();
        Integer temp = a.getTeamSort();
        a.setTeamSort(b.getTeamSort());
        b.setTeamSort(temp);
        updateCompetitionApplyInfo.add(a);
        updateCompetitionApplyInfo.add(b);
        return updateCompetitionApplyInfo;
    }
}
