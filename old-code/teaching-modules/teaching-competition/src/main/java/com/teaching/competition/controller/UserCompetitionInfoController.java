package com.teaching.competition.controller;

import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.*;
import com.teaching.competition.security.PublicApiResponseSanitizer;
import com.teaching.competition.service.*;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName UserCompetitionInfoController
 * @Description 用户端赛事信息
 * @Date 2025/10/10 15:05
 */
@RestController
@RequestMapping("/userCompetition")
public class UserCompetitionInfoController extends BaseController {

    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    @Autowired
    private IUserCollectService userCollectService;

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private ITeamManagerInfoService teamManagerInfoService;

    @Autowired
    private UserCompetitionService userCompetitionService;

    @Autowired
    private ICompetitionStageConfigService competitionStageConfigService;

    @Autowired
    private ICompetitionStageConfigService stageConfigService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IOperationTimesService operationTimesService;

    @Autowired
    private RemoteUserService userService;
    @Autowired
    private IOperationConfigService operationConfigService;

    @Autowired
    private PublicApiResponseSanitizer publicApiResponseSanitizer;

    /**
     * 获取用户赛事信息
     */
    @PostMapping("/list")
    public AjaxResult list() {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        List<UserCompetitionApplyInfo> userCompetitionApplyInfoList =
                competitionApplyInfoService.selectCompetitionApplyInfoByUserId(sysUser.getUserId());
        return success(userCompetitionApplyInfoList);
    }

    /**
     * 用户查询团队列表
     */
    @PostMapping("/teamList")
    public TableDataInfo selectUserCompetitionApplyInfoTeam(@RequestBody Map params) throws Exception {
        startPage();
        List<UserCompetitionApplyInfoTeam> list = userCompetitionService.selectUserCompetitionApplyInfoList(params);
        return getDataTable(list);
    }

    // 用户获取报名调整页面个人团队信息
//    @RequiresPermissions("competition:getUserCompetitionApplyInfo:query")
    @PostMapping("/getUserCompetitionApplyInfo")
    public AjaxResult selectUserCompetitionApplyInfo(@RequestBody Map params) {
        assertCurrentUserTeam(MapUtils.getString(params, "teamCode"));
        params.put("userId", SecurityUtils.getLoginUser().getSysUser().getUserId());
        List<UserCompetitionApplyInfoDTO> list = userCompetitionService.selectUserCompetitionApplyInfoDetail(params);
        return success(list);
    }

    // pc获取赛事操作配置信息
    @GetMapping("/selectCompetitionOperationConfigInfo/{competitionSeriesId}")
    public AjaxResult selectCompetitionOperationConfigInfo(@PathVariable Long competitionSeriesId) {
        List<OperationConfig> list = userCompetitionService.selectCompetitionOperationConfigInfo(competitionSeriesId);
        return success(list);
    }

    // 获取组别列表
    @GetMapping("/selectCompetitionTrackConfigInfo")
    public AjaxResult selectCompetitionTrackConfigInfo(CompetitionTrackConfig competitionTrackConfig) {
        List<CompetitionTrackConfig> list = userCompetitionService.selectCompetitionTrackConfigInfo(competitionTrackConfig);
        return success(list);
    }

    /**
     * 获取当前系统时间
     */
    @GetMapping("/getSystemDate")
    public AjaxResult getSystemData() {
        return success(DateUtils.getNowDate());
    }

    //校验修改参赛者信息及指导教师有无操作权限
    @GetMapping("/checkChangeOperator")
    public AjaxResult checkChangeOperator(@RequestParam Map<String,Object> params) throws Exception {
        if(MapUtils.isEmpty(params)){
            return error("参数不能为空");
        }
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        if(R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData())) {
            Long competitionSeriesId = MapUtils.getLong(params, "competitionSeriesId");
            String operationFlag = MapUtils.getString(params, "operationFlag");
            boolean data = userCompetitionService.checkChangeOperator(competitionSeriesId, userCenterInfoLogin.getData(), operationFlag);
            if (data && ApplyConstants.CHANGE_TYPE_INFO.equals(operationFlag)) {
                String  modifyScope = operationConfigService.getModifyScopeBySeriesIdAndOperationType(competitionSeriesId, operationFlag);
                return success(true).put("modifyScope", modifyScope);
            }
            return success(data);
        }
        return error("获取用户信息失败");
    }

    /**
     * 修改顺序接口
     * @param competitionSeriesId 赛事id
     * @param teamCode 团队code
     * @param memberId 要修改顺序的记录主键
     * @param change 变量 change up表示上升 down表示下降
     * @return
     */
    @GetMapping("/updateApplyInfoSequence")
    public AjaxResult updateApplyInfoSequence(@RequestParam Map<String,Object> params) {
        //判断入参
        if(MapUtils.isEmpty(params) || Objects.isNull(params.get("competitionSeriesId")) || Objects.isNull(params.get("teamCode"))
                || Objects.isNull(params.get("memberId")) || Objects.isNull(params.get("change"))){
            return error("参数不能为空");
        }
        String teamCode = MapUtils.getString(params, "teamCode");
        assertCurrentUserTeam(teamCode);
        acquireTeamSubmitGuard("sort", Collections.singleton(teamCode), 10);
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(sysUser.getUserId(), SecurityConstants.INNER);
        if (!R.isSuccess(userCenterInfoLogin) || Objects.isNull(userCenterInfoLogin.getData())) {
            throw new GlobalException("获取用户信息失败");
        }
        if (!userCompetitionService.checkChangeOperator(MapUtils.getLong(params,"competitionSeriesId"), userCenterInfoLogin.getData(), ApplyConstants.CHANGE_TYPE_INFO)) {
            throw new GlobalException("没有操作权限");
        }
        userCompetitionService.updateCompetitionApplyInfoTeamSort(params);
        return success();
    }

    // 修改团队信息校验前置，给前端使用
    @PostMapping("/checkChangeCompetitionApplyInfo")
    public AjaxResult checkChangeCompetitionApplyInfo(@RequestBody UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam) throws Exception {
        assertCurrentUserTeam(userCompetitionApplyInfoTeam.getTeamCode());
        if (ApplyConstants.OPERATION_CHANGE_INFO.equals(userCompetitionApplyInfoTeam.getChangeType())) {
            List<CompetitionApplyInfo> currentApplyInfoList = competitionApplyInfoService
                    .selectTeamInfoByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
            validateTeacherCompetitionRosterChange(userCompetitionApplyInfoTeam, currentApplyInfoList);
            userCompetitionService.checkPhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(),userCompetitionApplyInfoTeam.getCompetitionSeriesId());
//            userCompetitionService.checkUpdatePhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(),userCompetitionApplyInfoTeam.getCompetitionSeriesId());
        }
        if (ApplyConstants.OPERATION_CHANGE_GROUP.equals(userCompetitionApplyInfoTeam.getChangeType())) {
            List<CompetitionApplyInfo> competitionApplyInfoList = competitionApplyInfoService
                    .selectTeamInfoByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
            if(CollectionUtils.isNotEmpty(competitionApplyInfoList)){
                competitionApplyInfoList.stream().forEach(competitionApplyInfo -> {
                    competitionApplyInfo.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
                    competitionApplyInfo.setSecondLevelName(userCompetitionApplyInfoTeam.getSecondLevelName());
                });
            }
            userCompetitionService.checkData(competitionApplyInfoList,userCompetitionApplyInfoTeam.getCompetitionSeriesId(),ApplyConstants.OPERATION_CHANGE_GROUP);
        }
        if (ApplyConstants.OPERATION_CHANGE.equals(userCompetitionApplyInfoTeam.getChangeType()) ||
                ApplyConstants.OPERATION_CHANGE_TEACHER.equals(userCompetitionApplyInfoTeam.getChangeType())) {
            if(CollectionUtils.isNotEmpty(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList())){
                if("0".equals(userCompetitionApplyInfoTeam.getOneLineFlag())){
                    userCompetitionService.checkPhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(), userCompetitionApplyInfoTeam.getCompetitionSeriesId());
//                    userCompetitionService.checkUpdatePhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(), userCompetitionApplyInfoTeam.getCompetitionSeriesId());
                } else {
                    List<CompetitionApplyInfo> addCompetitionApplyInfoList = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList().stream().
                            filter(applyInfo -> !"1".equals(applyInfo.getDelFlag())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)) {
                        userCompetitionService.checkPhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(), userCompetitionApplyInfoTeam.getCompetitionSeriesId());
//                        userCompetitionService.checkUpdatePhoneAndEmail(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList(), userCompetitionApplyInfoTeam.getCompetitionSeriesId());
                    }
                    List<CompetitionApplyInfo> deleteCompetitionApplyInfoList = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList().stream().
                            filter(applyInfo -> "1".equals(applyInfo.getDelFlag())).collect(Collectors.toList());
                    List<CompetitionApplyInfo> competitionApplyInfoListOld = competitionApplyInfoService
                            .selectTeamInfoByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
                    validateTeacherCompetitionRosterChange(userCompetitionApplyInfoTeam, competitionApplyInfoListOld);
                    boolean teacherCompetition = ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(
                            resolveCompetitionTrackName(userCompetitionApplyInfoTeam, competitionApplyInfoListOld));
                    List<CompetitionApplyInfo> newCompetitionApplyInfoList = new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(deleteCompetitionApplyInfoList)){
                        List<Long> memberIds = deleteCompetitionApplyInfoList.stream().map(CompetitionApplyInfo::getMemberId).toList();
                        List<CompetitionApplyInfo> newCompetitionApplyInfos = competitionApplyInfoListOld.stream().
                                filter(applyInfo -> !memberIds.contains(applyInfo.getMemberId())).toList();
                        newCompetitionApplyInfoList.addAll(newCompetitionApplyInfos);
                    } else {
                        newCompetitionApplyInfoList.addAll(competitionApplyInfoListOld);
                    }
                    String competitionRoleName = userCompetitionApplyInfoTeam.getCompetitionApplyInfoList().get(0).getCompetitionRoleName();
                    if(CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)){
                        if(ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)){
                            List<CompetitionApplyInfo>  guideTeacherList = competitionApplyInfoListOld.stream().
                                    filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
                            CompetitionApplyInfo templateApplyInfo = CollectionUtils.isNotEmpty(guideTeacherList)
                                    ? guideTeacherList.get(0) : competitionApplyInfoListOld.get(0);
                            addCompetitionApplyInfoList.stream().forEach(applyInfo -> {
                                buildAddCompetitionApplyInfoList(applyInfo, templateApplyInfo);
                                applyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
                                prepareGuideTeacherFields(applyInfo);
                            });
                        } else {
                            List<CompetitionApplyInfo>  memberList = competitionApplyInfoListOld.stream().
                                    filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())).toList();
                            for (int i = 0; i < addCompetitionApplyInfoList.size(); i++){
                                buildAddCompetitionApplyInfoList(addCompetitionApplyInfoList.get(i),memberList.get(0));
                                addCompetitionApplyInfoList.get(i).setCompetitionRoleName(competitionRoleName);
                                addCompetitionApplyInfoList.get(i).setProfession("默认专业"+i);
                                addCompetitionApplyInfoList.get(i).setClassInfo("默认学级"+i);
                            }
                        }
                        newCompetitionApplyInfoList.addAll(addCompetitionApplyInfoList);
                    }
                    // 工程实践赛，ITC队员只能是2，产交融和不能少于4大于6，指教教师最少一名，最多两名
                    String checkMessage = "";
                    if (teacherCompetition) {
                        validateTeacherCompetitionRoster(newCompetitionApplyInfoList);
                    } else if(!ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionRoleName)){
                        List<CompetitionApplyInfo> memberList = newCompetitionApplyInfoList.stream().
                                filter(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                                .toList();
                        List<CompetitionApplyInfo> memberLeaderList = newCompetitionApplyInfoList.stream().
                                filter(applyInfo -> ApplyConstants.TEAM_LEADER_MEMBER.equals(applyInfo.getCompetitionRoleName()))
                                .toList();;
                        switch (userCompetitionApplyInfoTeam.getCompetitionTrackName()) {
                            case ApplyConstants.COMPETITION_TRACK_NAME_ONE, ApplyConstants.COMPETITION_TRACK_NAME_THREE:
                                if (memberList.size() != 2) {
                                    checkMessage = "队员人数只能是2名";
                                }
                                if(CollectionUtils.isNotEmpty(memberLeaderList)){
                                    checkMessage = "工程实践赛或ICT基础通识赛暂无队长角色，只能新增队员角色";
                                    break;
                                }
                                break;
                            case ApplyConstants.COMPETITION_TRACK_NAME_TWO, ApplyConstants.COMPETITION_TRACK_NAME_FOUR:
                                if (memberList.size() < 4) {
                                    checkMessage = "队员人数不能少于4名";
                                    break;
                                }
                                if (memberList.size() > 6) {
                                    checkMessage = "队员人数不能多于6名";
                                    break;
                                }
                                // 队长只能一名
                                if(CollectionUtils.isEmpty(memberLeaderList)){
                                    checkMessage = "队长人数必须存在1名";
                                    break;
                                }
                                if(CollectionUtils.isNotEmpty(memberLeaderList) && memberLeaderList.size()>1){
                                    checkMessage = "队长人数不能多于1名";
                                    break;
                                }
                                break;
                            default:
                                break;
                        }
                        userCompetitionService.checkData(newCompetitionApplyInfoList,userCompetitionApplyInfoTeam.getCompetitionSeriesId(),ApplyConstants.OPERATION_CHANGE_GROUP);
                    } else {
                        // 指教教师数量
                        List<CompetitionApplyInfo> guideTeacherList = newCompetitionApplyInfoList.stream().
                                filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                                .toList();
                        if (guideTeacherList.size() < 1) {
                            checkMessage = "指导教师人数不能少于1名";
                        }
                        if (guideTeacherList.size() > 2) {
                            checkMessage = "指导教师人数不能多于2名";
                        }
                    }
                    if (StringUtils.isNotEmpty(checkMessage)) {
                        throw new GlobalException(checkMessage);
                    }
                }
            }
        }
        return success();
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
//        addApplyInfoListNew.setProfession(OldCompetitionApplyInfo.getProfession());
//        addApplyInfoListNew.setClassInfo(OldCompetitionApplyInfo.getClassInfo());
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

    static void prepareGuideTeacherFields(CompetitionApplyInfo applyInfo) {
        applyInfo.setGuideTeacher(applyInfo.getUserName());
        applyInfo.setGuideTeacherPhone(applyInfo.getPhone());
        applyInfo.setGuideTeacherEmail(applyInfo.getEmail());
    }

    static void validateTeacherCompetitionRoster(List<CompetitionApplyInfo> applyInfoList) {
        List<CompetitionApplyInfo> safeApplyInfoList = Optional.ofNullable(applyInfoList)
                .orElse(Collections.emptyList());
        boolean hasInvalidPlayerRole = safeApplyInfoList.stream()
                .anyMatch(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName())
                        && !ApplyConstants.TEAM_LEADER_MEMBER.equals(applyInfo.getCompetitionRoleName()));
        if (hasInvalidPlayerRole) {
            throw new GlobalException("教师赛参赛选手角色必须为队长");
        }
        long playerCount = safeApplyInfoList.stream()
                .filter(applyInfo -> ApplyConstants.TEAM_LEADER_MEMBER.equals(applyInfo.getCompetitionRoleName()))
                .count();
        if (playerCount != 1) {
            throw new GlobalException("教师赛参赛选手人数必须为1名");
        }
        long guideTeacherCount = safeApplyInfoList.stream()
                .filter(applyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()))
                .count();
        if (guideTeacherCount > 2) {
            throw new GlobalException("教师赛指导教师人数不能多于2名");
        }
    }

    static void validateTeacherCompetitionRosterChange(UserCompetitionApplyInfoTeam applyInfoTeam,
                                                        List<CompetitionApplyInfo> currentApplyInfoList) {
        if (!ApplyConstants.COMPETITION_TRACK_NAME_TEACHER.equals(resolveCompetitionTrackName(
                applyInfoTeam, currentApplyInfoList))) {
            return;
        }
        List<CompetitionApplyInfo> currentRoster = new ArrayList<>(
                Optional.ofNullable(currentApplyInfoList).orElse(Collections.emptyList()));
        List<CompetitionApplyInfo> changes = Optional.ofNullable(applyInfoTeam.getCompetitionApplyInfoList())
                .orElse(Collections.emptyList());
        if (ApplyConstants.OPERATION_CHANGE_INFO.equals(applyInfoTeam.getChangeType())) {
            validateTeacherCompetitionRoster(currentRoster);
            Map<Long, String> currentRoleMap = currentRoster.stream()
                    .filter(applyInfo -> Objects.nonNull(applyInfo.getMemberId()))
                    .collect(Collectors.toMap(CompetitionApplyInfo::getMemberId,
                            CompetitionApplyInfo::getCompetitionRoleName));
            boolean changesRole = changes.stream().anyMatch(applyInfo -> {
                String currentRole = currentRoleMap.get(applyInfo.getMemberId());
                return Objects.isNull(currentRole)
                        || StringUtils.isNotEmpty(applyInfo.getCompetitionRoleName())
                        && !currentRole.equals(applyInfo.getCompetitionRoleName());
            });
            if (changesRole) {
                throw new GlobalException("教师赛参赛人员角色不允许修改");
            }
            return;
        }
        if (!ApplyConstants.OPERATION_CHANGE.equals(applyInfoTeam.getChangeType())
                && !ApplyConstants.OPERATION_CHANGE_TEACHER.equals(applyInfoTeam.getChangeType())) {
            return;
        }

        Set<Long> deletedMemberIds = changes.stream()
                .filter(applyInfo -> "1".equals(applyInfo.getDelFlag()))
                .map(CompetitionApplyInfo::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean deletesPlayer = currentRoster.stream()
                .anyMatch(applyInfo -> deletedMemberIds.contains(applyInfo.getMemberId())
                        && !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()));
        boolean addsPlayer = changes.stream()
                .filter(applyInfo -> !"1".equals(applyInfo.getDelFlag()))
                .anyMatch(applyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(applyInfo.getCompetitionRoleName()));
        if (deletesPlayer || addsPlayer) {
            throw new GlobalException("教师赛参赛选手不支持新增、删除或替换");
        }

        currentRoster.removeIf(applyInfo -> deletedMemberIds.contains(applyInfo.getMemberId()));
        changes.stream()
                .filter(applyInfo -> !"1".equals(applyInfo.getDelFlag()))
                .forEach(currentRoster::add);
        validateTeacherCompetitionRoster(currentRoster);
    }

    private static String resolveCompetitionTrackName(UserCompetitionApplyInfoTeam applyInfoTeam,
                                                      List<CompetitionApplyInfo> currentApplyInfoList) {
        return Optional.ofNullable(currentApplyInfoList)
                .orElse(Collections.emptyList())
                .stream()
                .map(CompetitionApplyInfo::getCompetitionTrackName)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(applyInfoTeam.getCompetitionTrackName());
    }

    // 更改报名团队及队员信息
    @PostMapping("/changeCompetitionApplyInfo")
    public AjaxResult changeCompetitionApplyInfo(@RequestBody List<UserCompetitionApplyInfoTeam> userCompetitionApplyInfoTeamList) throws Exception {
        if (CollectionUtils.isEmpty(userCompetitionApplyInfoTeamList)) {
            return error("参数不能为空");
        }
        Set<String> teamCodes = userCompetitionApplyInfoTeamList.stream()
                .map(UserCompetitionApplyInfoTeam::getTeamCode)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toCollection(TreeSet::new));
        if (teamCodes.size() != userCompetitionApplyInfoTeamList.stream()
                .map(UserCompetitionApplyInfoTeam::getTeamCode).distinct().count()) {
            throw new GlobalException("团队信息不能为空");
        }
        teamCodes.forEach(this::assertCurrentUserTeam);
        acquireTeamSubmitGuard("change", teamCodes, 20);
        if(CollectionUtils.isNotEmpty(userCompetitionApplyInfoTeamList)){
            UserCompetitionApplyInfoDTO applyInfoDTO = new UserCompetitionApplyInfoDTO();
            List<Map<String, String>> operationList = new ArrayList<>();
            List<OperationConfig> list = userCompetitionService.selectCompetitionOperationConfigInfo(userCompetitionApplyInfoTeamList.get(0).getCompetitionSeriesId());
            Map<String, OperationConfig> operationConfigMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(list)){
                operationConfigMap = list.stream().collect(Collectors.toMap(OperationConfig::getOperationType, v -> v));
            }
            logger.info("修改次数1:"+ JSONObject.toJSONString(operationConfigMap));
            for (UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam : userCompetitionApplyInfoTeamList){
                if (ApplyConstants.OPERATION_CHANGE_INFO.equals(userCompetitionApplyInfoTeam.getChangeType())
                        || ApplyConstants.OPERATION_CHANGE.equals(userCompetitionApplyInfoTeam.getChangeType())
                        || ApplyConstants.OPERATION_CHANGE_TEACHER.equals(userCompetitionApplyInfoTeam.getChangeType())) {
                    List<CompetitionApplyInfo> currentApplyInfoList = competitionApplyInfoService
                            .selectTeamInfoByTeamCode(Map.of("teamCodes", userCompetitionApplyInfoTeam.getTeamCode()));
                    validateTeacherCompetitionRosterChange(userCompetitionApplyInfoTeam, currentApplyInfoList);
                }
                applyInfoDTO.setCompetitionSeriesId(userCompetitionApplyInfoTeam.getCompetitionSeriesId());
                applyInfoDTO.setCompetitionTrackId(userCompetitionApplyInfoTeam.getCompetitionTrackId());
                applyInfoDTO.setTeamCode(userCompetitionApplyInfoTeam.getTeamCode());
                if (ApplyConstants.OPERATION_CHANGE_INFO.equals(userCompetitionApplyInfoTeam.getChangeType())) {
                    // 判断是队员还是指导教师修改
                    List<CompetitionApplyInfo> memberApplyInfoList = Optional.ofNullable(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList())
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(competitionApplyInfoList -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfoList.getCompetitionRoleName())).toList();
                    applyInfoDTO.setCompetitionApplyInfoList(memberApplyInfoList);
                    List<CompetitionApplyInfo> guiderApplyInfoList = Optional.ofNullable(userCompetitionApplyInfoTeam.getCompetitionApplyInfoList())
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(competitionApplyInfoList -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfoList.getCompetitionRoleName())).toList();
                    applyInfoDTO.setGuideTeacherApplyInfoList(guiderApplyInfoList);
                    String operationFlag = null;
                    if (CollectionUtils.isNotEmpty(memberApplyInfoList)){
                        operationFlag = ApplyConstants.CHANGE_TYPE_INFO;
                    }
                    if (CollectionUtils.isNotEmpty(guiderApplyInfoList)){
                        operationFlag = ApplyConstants.CHANGE_TYPE_TEACHER;
                    }
                    if (CollectionUtils.isNotEmpty(memberApplyInfoList) && !operationTimesService.checkTeamMembersTimes(applyInfoDTO,
                            Arrays.asList(ApplyConstants.OPERATION_CHANGE_INFO))) {
                        throw new Exception(userCompetitionApplyInfoTeam.getTeamName() + "团队修改人员信息超出修改次数限制");
                    }
                    if (CollectionUtils.isNotEmpty(guiderApplyInfoList) && !operationTimesService.checkTeacherTimes(applyInfoDTO,
                            Arrays.asList(ApplyConstants.OPERATION_CHANGE_INFO))) {
                        throw new Exception(userCompetitionApplyInfoTeam.getTeamName() + "团队修改指导教师信息超出修改次数限制");
                    }
                    userCompetitionService.updateCompetitionApplyInfo(userCompetitionApplyInfoTeam,operationFlag);
                    logger.info("修改次数memberApplyInfoList:"+ JSONObject.toJSONString(memberApplyInfoList));
                    logger.info("修改次数guiderApplyInfoList:"+ JSONObject.toJSONString(guiderApplyInfoList));
                    if(MapUtils.isNotEmpty(operationConfigMap) && CollectionUtils.isNotEmpty(memberApplyInfoList)){
                        Map<String, String> operationMap = new HashMap<>();
                        operationMap.put("operationType", ApplyConstants.OPERATION_CHANGE_INFO);
                        operationMap.put("configId", operationConfigMap.get(ApplyConstants.CHANGE_TYPE_INFO).getId().toString());
                        operationList.add(operationMap);
                    }
                    if(MapUtils.isNotEmpty(operationConfigMap) && CollectionUtils.isNotEmpty(guiderApplyInfoList)){
                        Map<String, String> operationMap = new HashMap<>();
                        operationMap.put("operationType", ApplyConstants.OPERATION_CHANGE_INFO);
                        operationMap.put("configId", operationConfigMap.get(ApplyConstants.CHANGE_TYPE_TEACHER).getId().toString());
                        operationList.add(operationMap);
                    }
                    logger.info("修改次数:"+ JSONObject.toJSONString(operationList));
                }
                if (ApplyConstants.OPERATION_CHANGE.equals(userCompetitionApplyInfoTeam.getChangeType()) ||
                        ApplyConstants.OPERATION_CHANGE_TEACHER.equals(userCompetitionApplyInfoTeam.getChangeType())) {
                    userCompetitionService.updateAddCompetitionApplyInfo(userCompetitionApplyInfoTeam);
                    if(MapUtils.isNotEmpty(operationConfigMap) && ApplyConstants.OPERATION_CHANGE.equals(userCompetitionApplyInfoTeam.getChangeType())){
                        Map<String, String> operationMap = new HashMap<>();
                        operationMap.put("operationType", userCompetitionApplyInfoTeam.getChangeType());
                        operationMap.put("configId", operationConfigMap.get(ApplyConstants.CHANGE_TYPE_INFO).getId().toString());
                        operationList.add(operationMap);
                    }
                    if(MapUtils.isNotEmpty(operationConfigMap) && ApplyConstants.OPERATION_CHANGE_TEACHER.equals(userCompetitionApplyInfoTeam.getChangeType())){
                        Map<String, String> operationMap = new HashMap<>();
                        operationMap.put("operationType", userCompetitionApplyInfoTeam.getChangeType());
                        operationMap.put("configId", operationConfigMap.get(ApplyConstants.CHANGE_TYPE_TEACHER).getId().toString());
                        operationList.add(operationMap);
                    }
                }
                if (ApplyConstants.OPERATION_CHANGE_GROUP.equals(userCompetitionApplyInfoTeam.getChangeType())) {
                    applyInfoDTO.setSecondLevelCode(userCompetitionApplyInfoTeam.getSecondLevelCode());
                    if(MapUtils.isNotEmpty(operationConfigMap)){
                        Map<String, String> operationMap = new HashMap<>();
                        operationMap.put("operationType", ApplyConstants.OPERATION_CHANGE_GROUP);
                        operationMap.put("configId", operationConfigMap.get(ApplyConstants.CHANGE_TYPE_INFO).getId().toString());
                        operationList.add(operationMap);
                    }
                    if(operationTimesService.checkTeamMembersTimes(applyInfoDTO, Arrays.asList(ApplyConstants.OPERATION_CHANGE_GROUP))){
                        userCompetitionService.updateCompetitionApplyTeamInfo(userCompetitionApplyInfoTeam);
                    } else{
                        throw new Exception(userCompetitionApplyInfoTeam.getTeamName()+"团队换组别超出修改次数限制");
                    }
                }
            }
            // 获取报名信息可调整次数
            operationTimesService.recordUsedTimes(applyInfoDTO,operationList);
        }
        return success();
    }

    private void assertCurrentUserTeam(String teamCode) {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if (StringUtils.isEmpty(teamCode) || !userCompetitionService.hasUserTeamAccess(teamCode, currentUserId)) {
            throw new GlobalException("无权访问或修改该团队");
        }
    }

    private void acquireTeamSubmitGuard(String operation, Collection<String> teamCodes, int seconds) {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String resource = String.join(",", teamCodes);
        String key = "personal:submit:team-" + operation + ":" + currentUserId + ":" + Integer.toHexString(resource.hashCode());
        if (!Boolean.TRUE.equals(redisService.setIfAbsent(key, "PROCESSING", seconds, TimeUnit.SECONDS))) {
            throw new GlobalException("请求正在处理中，请勿重复提交");
        }
    }

    /**
     * 获取赛事收藏数量
     */
    @GetMapping("/selectCollectCompetitionCount")
    public AjaxResult selectCollectCompetitionCount(@RequestParam Map<String,Object> params) {

        Integer count = userCollectService.selectCollectCompetitionCount(params);
        return success(count);
    }

    /**
     * 用户赛事分享
     */
    @GetMapping("/shareCompetition")
    public AjaxResult shareCompetition(@RequestParam Map<String,Object> params) {
        // Long competitionId
        Long num = userCollectService.shareCompetition(params);
        return success(num);
    }

    /**
     * 用户赛事分享总数
     */
    @GetMapping("/shareCompetitionCount")
    public AjaxResult shareCompetitionCount(@RequestParam Map<String,Object> params) {
        Integer num = userCollectService.shareCompetitionCount(params);
        return success(num);
    }

    /**
     * 赛事中心获取赛事列表,无需认证校验
     */
    @GetMapping("/pc/competitionList")
    public TableDataInfo list(CompetitionMainInfoReq req) {
        // Anonymous callers may only enumerate public lifecycle states. Ignore
        // caller-supplied status filters so drafts/rejected records cannot be
        // requested through the public route.
        req.setCheckStatus(Constants.COMPETITION_PUBLISH + "," + Constants.COMPETITION_RUNNING + "," + Constants.COMPETITION_END);
        // The shared mapper contains an administrator-only dataScope fragment.
        // Never accept that fragment from anonymous request binding.
        req.getParams().clear();
        startPage();
        List<CompetitionMainInfo> list = userCompetitionService.selectUserCompetitionMainInfoList(req);
        TableDataInfo response = getDataTable(list);
        response.setRows(list.stream().map(publicApiResponseSanitizer::sanitize).collect(Collectors.toList()));
        return response;
    }

    /**
     * 获取赛事数据详细信息,无需认证校验
     */
    @GetMapping(value = "/pc/getUserCompetitionDetailInfoById")
    public AjaxResult getUserCompetitionDetailInfoById(CompetitionMainInfoReq req) {
        // 用户端正在进行赛事详情确定处于那个阶段
        req.setCheckStatus(Constants.COMPETITION_PUBLISH + "," + Constants.COMPETITION_RUNNING + "," + Constants.COMPETITION_END);
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
        CompetitionDetailInfo competitionDetailInfo = null;
        if (CollectionUtils.isNotEmpty(competitionDetailInfoList)) {
            competitionDetailInfo = competitionDetailInfoList.get(0);
            CompetitionStageConfig competitionStageConfig = stageConfigService.selectNowCompetitionStageConfig(req.getCompetitionSeriesId());
            competitionDetailInfo.setCompetitionStageList(Arrays.asList(competitionStageConfig));
        }
        return success(publicApiResponseSanitizer.sanitize(competitionDetailInfo));
    }

    // 获取赛事赛道配置详情信息
    @GetMapping(value = "/selectCompetitionTrackInfoByCompetitionSeriesId/{competitionSeriesId}")
    public AjaxResult selectCompetitionTrackInfoByCompetitionSeriesId(@PathVariable Long competitionSeriesId) {

        return success(userCompetitionService.selectCompetitionTrackInfoByCompetitionSeriesId(competitionSeriesId));
    }

    // 用户赛事报名,由带队老师进行统一报名  根据赛事类型 个人参赛则个人方式参加赛事 团队参赛则团队方式参加赛事
    // 人数不限， 则要不个人参赛要不团队参赛只能一种方式
//    @RequiresPermissions("apply:user:add")
    @PostMapping(value = "/userSaveApplyCompetitionInfo")
    public AjaxResult userSaveApplyCompetitionInfo(@RequestBody UserApplyCompetitionReq req) {
        return success(userCompetitionService.userApplyCompetitionInfo(req));
    }

    // 报名信息导入
//    @RequiresPermissions("apply:user:import")
    @PostMapping("/importData/{competitionSeriesId}")
    public AjaxResult importApplyCompetitionData(MultipartFile file, boolean updateSupport, @PathVariable Long competitionSeriesId) throws Exception {
//        ExcelUtil<CompetitionApplyInfo> util = new ExcelUtil<CompetitionApplyInfo>(CompetitionApplyInfo.class);
//        List<CompetitionApplyInfo> applyInfoList = util.importExcelForManySheet(file.getInputStream(), 1);
        if(Objects.isNull(competitionSeriesId)){
            throw new GlobalException("未获取到赛事信息");
        }
        // 每次导入先清空之前历史缓存数据
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        redisService.deleteObject(userId+"_"+competitionSeriesId+"");
        ExcelTeamReader excelTeamReader = new ExcelTeamReader();
        List<CompetitionApplyInfo> applyInfoList = excelTeamReader.readCompetitionApplyInfoFromExcel(file.getInputStream());
        UserApplyCompetitionReq req = new UserApplyCompetitionReq();
        req.setCompetitionSeriesId(competitionSeriesId);
        return success(userCompetitionService.importApplyCompetitionData(applyInfoList, updateSupport, userId, req));
    }

    @PostMapping("/saveApplyCompetitionData")
    public AjaxResult saveApplyCompetitionData(@RequestBody UserApplyCompetitionReq req) throws Exception {

        return success(userCompetitionService.saveApplyCompetitionData(req));
    }

    // 同步报名学生数据
    @InnerAuth
    @GetMapping("/syncCompetitionApplyInfo")
    public R<Void> syncCompetitionApplyInfo(@RequestParam String updateSize) throws Exception {
        userCompetitionService.syncCompetitionApplyInfo(updateSize);
        return R.ok();
    }

    @InnerAuth
    @PostMapping("/bindTeacherCompetitionUser")
    public R<Integer> bindTeacherCompetitionUser(@RequestParam Long userId, @RequestParam String phone) {
        return R.ok(userCompetitionService.bindTeacherCompetitionUser(userId, phone));
    }

    /**
     * 获取用户赛事报名状态
     */
    @PostMapping(value = "/checkCompetitionApplyStatusByUser")
    public AjaxResult checkCompetitionApplyStatusByUser(@RequestBody UserApplyCompetitionReq req) {
        return success(userCompetitionService.checkCompetitionApplyStatusByUser(req));
    }

    /**
     * 新增团队信息
     */
//    @RequiresPermissions(value = {"apply:leader:add","apply:user:add"})
    @PostMapping("/saveTeamManagerInfo")
    public AjaxResult add(@RequestBody TeamManagerInfo teamManagerInfo) {
        return toAjax(teamManagerInfoService.insertTeamManagerInfo(teamManagerInfo));
    }

    /**
     * 修改团队信息
     */
//    @RequiresPermissions("apply:leader:edit")
    @PostMapping("/updateTeamManagerInfo")
    public AjaxResult updateTeamManagerInfo(@RequestBody TeamManagerInfo teamManagerInfo) {
        return toAjax(teamManagerInfoService.updateTeamManagerInfo(teamManagerInfo));
    }

    /**
     * 申请加入团队
     */
//    @RequiresPermissions("apply:user:add")
    @PostMapping("/applyJoinTeam")
    public AjaxResult applyJoinTeam(@RequestBody UserApplyTeam userApplyTeam) {
        return toAjax(teamManagerInfoService.applyJoinTeam(userApplyTeam));
    }

    // 获取团队详情信息
    @GetMapping(value = "/getTeamMemberList")
    public AjaxResult getTeamMemberList(@RequestParam Long competitionSeriesId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(teamManagerInfoService.selectTeamMemberList(userId, competitionSeriesId));
    }

    // 获取赛事团队列表
    @GetMapping(value = "/getCompetitionTeamList")
    public AjaxResult getCompetitionTeamList(TeamManagerInfo teamManagerInfo) {
        return success(teamManagerInfoService.selectTeamManagerInfo(teamManagerInfo));
    }

    // 团长同意或拒绝团员申请
//    @RequiresPermissions("apply:leader:edit")
    @PostMapping(value = "/agreeJoinTeam")
    public AjaxResult agreeJoinTeam(@RequestBody Map<String, String> param) {

        return toAjax(userCompetitionService.agreeJoinTeam(param));
    }

    // 查询用户再一个赛事中申请团队信息
    @GetMapping(value = "/userJoinTeam")
    public AjaxResult userJoinTeam(@RequestParam Long competitionSeriesId) {

        return success(userCompetitionService.selectTeamInfoByUserId(competitionSeriesId));
    }

    /**
     * 获取赛事团队信息
     *
     * @param registrationInfo
     * @return
     */
    @GetMapping("/getTeamCompetitionInfo")
    public AjaxResult getTeamCompetitionInfo(RegistrationInfo registrationInfo) {
        return success(competitionApplyInfoService.getTeamCompetitionInfo(registrationInfo));
    }

    /**
     * 删除团队信息
     *
     * @param teamCode
     * @return
     */
    @PostMapping("/deleteTeam")
    public AjaxResult deleteTeam(@RequestBody Map<String, Object> param) {
        String teamCode = MapUtils.getString(param, "teamCode");
        if (StringUtils.isEmpty(teamCode)) {
            return success();
        }
        return toAjax(competitionApplyInfoService.delApplyInfoByTeamCode(teamCode));
    }

    /**
     * 删除团队s信息
     *
     * @param teamCodes
     * @return
     */
    @DeleteMapping("/deleteTeam/{teamCodes}")
    public AjaxResult deleteTeams(@PathVariable String[] teamCodes) {
        return toAjax(competitionApplyInfoService.delApplyInfoByTeamCodes(teamCodes));
    }

    /**
     * 去结算
     *
     * @param param
     * @return
     */
    @PostMapping("/settlement")
    public AjaxResult settlement(@RequestBody Map<String, Object> param) {
        List<String> teamCodeList = (List<String>) param.get("teamCodeList");
        Long competitionSeriesId = MapUtils.getLong(param, "competitionSeriesId");
        return success(competitionApplyInfoService.settlement(teamCodeList, competitionSeriesId));
    }

    /**
     * 确认订单 页面
     *
     * @param param
     * @return
     */
    @PostMapping("/confirmOrder")
    public AjaxResult confirmOrder(@RequestBody Map<String, Object> param) {
        String token = MapUtils.getString(param, "token");
        Long competitionSeriesId = MapUtils.getLong(param, "competitionSeriesId");
        return success(competitionApplyInfoService.confirmOrder(token, competitionSeriesId));
    }

    /**
     * 返回订单总额、团队codes
     *
     * @return
     */
    @InnerAuth
    @GetMapping("/getTeamInfo/{competitionSeriesId}")
    public AjaxResult getTeamInfo(@PathVariable Long competitionSeriesId) {
        return success(competitionApplyInfoService.getTeamInfo(competitionSeriesId));
    }

    /**
     * 更新团队支付状态
     *
     * @param param  payStatus支付状态（必传），competitionSeriesId赛事id（不必传），teamCodeList团队编码列表（必传）
     * @return
     */
    @InnerAuth
    @PostMapping("/updateTeamOrderStatus")
    public AjaxResult updateTeamPayStatusByTeamCodes(@RequestBody Map<String, Object> param) {
        return success(competitionApplyInfoService.updateTeamPayStatusByTeamCodes(param));
    }

    /**
     * 获取订单详情
     *
     * @param param competitionSeriesId赛事id（不必传），teamCodeList团队编码列表（必传）
     * @return
     */
    @InnerAuth
    @PostMapping("/getDetailForOrder")
    public AjaxResult getDetailForOrder(@RequestBody Map<String, Object> param) {
        return success(competitionApplyInfoService.getDetailForOrder(param));
    }

    /**
     * 根据teamcode获取团队信息
     *
     * @param params 团队编码列表
     * @return
     */
    @PostMapping("/selectCompetitionApplyInfoListByTeamCode")
    public AjaxResult selectCompetitionApplyInfoListByTeamCode(@RequestBody Map<String, Object> params) {
        return success(competitionApplyInfoService.selectCompetitionApplyInfoListByTeamCode(params));
    }
}
