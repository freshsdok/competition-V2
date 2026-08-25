package com.teaching.system.service;

import cn.hutool.json.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.redis.service.RedisLock;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysAuditMainConfig;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.domain.SysUserGroupCompetitionRelation;
import com.teaching.system.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class SysAsyncService {
    @Autowired
    private SysAuditMainConfigMapper sysAuditMainConfigMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;
    @Autowired
    private IdentityInfoMapper identityInfoMapper;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private RedisLock redisLock;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Async
    public void bindTeacherCompetitionUser(Long userId, String phone) {
        if (userId == null || StringUtils.isBlank(phone)) {
            return;
        }
        R<Integer> result = competitionService.bindTeacherCompetitionUser(
                userId, phone, SecurityConstants.INNER);
        if (!R.isSuccess(result)) {
            log.warn("注册后关联教师赛报名失败，userId={}, message={}", userId, result.getMsg());
        }
    }

    /**
     * 获取当前登录用户可审核的流程节点信息
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void getCanAuditInfoByLoginUser() {
        List<Map<String, Long>> result = new ArrayList<>();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser().isAdmin()) {
            return;
        }
        Long deptId = loginUser.getSysUser().getOrgId();
        Long userid = loginUser.getUserid();
        Set<String> roles = loginUser.getRoles();
        Set<String> permissions = loginUser.getPermissions();
        boolean isHasAuditPermission = false;
        for (String permission : permissions) {
            //有相关审核列表权限xx:task:list
            if (permission.endsWith("task:list")) {
                isHasAuditPermission = true;
                break;
            }
        }
        //判断是否有审核任务列表权限字符串权限
        if (isHasAuditPermission) {
            //查开启的流程
            List<SysAuditMainConfig> sysAuditMainConfigs = sysAuditMainConfigMapper.selectSysAuditMainConfigList(new SysAuditMainConfig("0"));
            sysAuditMainConfigs.forEach(sysAuditMainConfig -> {
                if (Objects.nonNull(sysAuditMainConfig) && Objects.nonNull(sysAuditMainConfig.getSysAuditConfigList())) {
                    sysAuditMainConfig.getSysAuditConfigList().forEach(sysAuditConfig -> {
                        //对开启的环节判断
                        if ("1".equals(sysAuditConfig.getIsEnable())) {
                            String checkPersonType = sysAuditConfig.getCheckPersonType();
                            // org是字符串100,102,108形式的
                            String checkPersonOrg = sysAuditConfig.getCheckPersonOrg();
                            if (StringUtils.isNotBlank(checkPersonOrg) && checkPersonOrg.contains(",")) {
                                String[] split = checkPersonOrg.split(",");
                                checkPersonOrg = split[split.length - 1];
                            }
                            boolean isMatch = switch (checkPersonType) {
                                case "dept" -> String.valueOf(deptId).equals(checkPersonOrg);
                                case "role" -> roles.contains(sysAuditConfig.getCheckPersonRole());
                                case "deptRole" -> String.valueOf(deptId).equals(checkPersonOrg)
                                        && roles.contains(sysAuditConfig.getCheckPersonRole());
                                case "user" -> String.valueOf(userid).equals(sysAuditConfig.getCheckPerson());
                                default -> false;
                            };
                            if (isMatch) {
                                Map<String, Long> temp = new HashMap<>();
                                temp.put("auditId", sysAuditMainConfig.getAuditId());
                                temp.put("configId", sysAuditConfig.getConfigId());
                                result.add(temp);
                            }
                        }
                    });
                }
            });
        }
        if (loginUser.getUserid() != null) {
            redisService.setCacheObject("audit:info:" + loginUser.getUserid(), result);
        }
    }

    /**
     * 判断登录人所在的用户组
     */
    @Async
    public void getUserGroupByLoginUser(Long userId) {
        log.info("判断登录人所在的用户组:{},start", userId);
        if (userId != null && 1L == userId) {
            return;
        }
        Set<Long> userGroupIds = new HashSet<>();
        //得到所有的用户组，同时得到了指定的人和和名单人员
        List<SysUserGroup> sysUserGroups = sysUserGroupMapper.selectSysUserGroupList(new SysUserGroup());
        if (CollectionUtils.isNotEmpty(sysUserGroups)) {
            //如果当前登录人在某个用户组黑名单中直接就不属于当前用户组
            sysUserGroups.forEach(sysUserGroup -> {
                boolean isInBlack = false;
                if (StringUtils.isNotBlank(sysUserGroup.getBlackUserIds())) {
                    String[] split = sysUserGroup.getBlackUserIds().split(",");
                    for (String s : split) {
                        if (s.equals(userId.toString())) {
                            isInBlack = true;
                            break;
                        }
                    }
                }
                if (!isInBlack) {
                    //如果不在黑名单，看是否在指定名单中，如果在就属于当前用户组，如果不在看规则
                    boolean isInUserIds = false;
                    if (StringUtils.isNotBlank(sysUserGroup.getUserIds())) {
                        String[] split1 = sysUserGroup.getUserIds().split(",");
                        for (String s : split1) {
                            if (s.equals(userId.toString())) {
                                isInUserIds = true;
                                break;
                            }
                        }
                    }
                    if (isInUserIds) {
                        //在指定用户列表中就直接属于当前用户组
                        userGroupIds.add(sysUserGroup.getId());
                    } else {
                        String identifyType = sysUserGroup.getIdentifyType();
                        Long groupId = sysUserGroup.getId();
                        List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelations = sysUserGroupMapper.selectSysUserGroupCompetitionRelationList(groupId);
                        //身份和赛事同时满足才算属于当前用户组  身份要求和赛事要求都为空时任何人不匹配
                        if ((StringUtils.isNotBlank(identifyType) || CollectionUtils.isNotEmpty(sysUserGroupCompetitionRelations))
                                && checkIdentityType(identifyType, userId) && checkCompetition(userId, sysUserGroupCompetitionRelations)) {
                            userGroupIds.add(sysUserGroup.getId());
                        }
                        // 允许得参赛角色
                        String allowRoleName = sysUserGroup.getAllowRoleName();
                        if (StringUtils.isNotEmpty(allowRoleName) && checkCompetitionRole(allowRoleName, userId)
                                && checkCompetition(userId, sysUserGroupCompetitionRelations)) {
                            userGroupIds.add(sysUserGroup.getId());
                        }
                    }
                }
            });
        }
        redisService.deleteObject("userGroup:info:" + userId);
        if (CollectionUtils.isNotEmpty(userGroupIds)) {
            //某用户所在的用户组信息
            redisService.setCacheObject("userGroup:info:" + userId, userGroupIds);
        }
        log.info("判断登录人所在的用户组:{},end", userId);
    }

    /**
     * 校验身份类型
     * 规则没有要求直接满足，规则有多个要求满足一个即可
     *
     * @param identifyType 规则定义的类型
     * @param userId       当前登录人
     * @return 是否满足 true满足 false不满足
     */
    private boolean checkIdentityType(String identifyType, Long userId) {
        if (StringUtils.isNotBlank(identifyType)) {
            IdentityInfo identityInfo = new IdentityInfo();
            identityInfo.setUserId(userId);
            identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
            List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfo);
            if (CollectionUtils.isEmpty(identityInfoList)) {
                return false;
            }
            String[] split = identifyType.split(",");
            //split中的任意一个匹配即可
            for (String s : split) {
                if (identityInfoList.stream().anyMatch(item -> item.getCertificationType().equals(s))) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean checkCompetitionRole(String allowRoleName, Long userId){
        AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(userId);
        if(Objects.nonNull(authInfo)){
            // 获取登录人已缴费的报名信息
            CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
            competitionApplyInfo.setIdCard(authInfo.getIdCard());
            R<List<CompetitionApplyInfo>> innerApplyUserInfo = competitionService.getInnerApplyUserInfo(competitionApplyInfo, SecurityConstants.INNER);
            // 获取登录人已缴费的报名信息
            if(R.isSuccess(innerApplyUserInfo) && CollectionUtils.isNotEmpty(innerApplyUserInfo.getData())){
                List<String> competitionRoleNameList = innerApplyUserInfo.getData().stream().map(CompetitionApplyInfo::getCompetitionRoleName).distinct().toList();
                String[] split = allowRoleName.split(",");
                //split中的任意一个匹配即可
                for (String s : split) {
                    if (competitionRoleNameList.stream().anyMatch(item -> item.equals(s))) {
                        return true;
                    }
                }
                return false;
            }
        }
        // 未注册过账号或未实名认证默认无权限
        return false;
    }

    /**
     * 校验赛事
     * 规则没有要求直接满足，规则有多个要求满足一个即可
     *
     * @param userId                           当前登录人
     * @param sysUserGroupCompetitionRelations 用户组id对应的赛事要求
     * @return
     */
    private boolean checkCompetition(Long userId, List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelations) {
        //要求
//        List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelations = sysUserGroupMapper.selectSysUserGroupCompetitionRelationList(groupId);
        if (CollectionUtils.isNotEmpty(sysUserGroupCompetitionRelations)) {
            //获取登录人已缴费的报名信息
            R<List<CompetitionApplyInfo>> competitionApplyInfoByPayStatusForUserGroup = competitionService.getCompetitionApplyInfoByPayStatusForUserGroup(userId, SecurityConstants.INNER);
            if (R.isSuccess(competitionApplyInfoByPayStatusForUserGroup) && CollectionUtils.isNotEmpty(competitionApplyInfoByPayStatusForUserGroup.getData())) {
                List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoByPayStatusForUserGroup.getData();
                //competitionSeriesId 赛事系列id  1:赛事
                List<String> competitionSeriesIds = applyInfoList.stream().map(competitionApplyInfo -> String.valueOf(competitionApplyInfo.getCompetitionSeriesId())).toList();
                //2:阶段 报名表里没有阶段暂不处理
                //competitionTrackId 赛事赛道id 3:赛道
                List<String> competitionTrackIds = applyInfoList.stream().map(CompetitionApplyInfo::getCompetitionTrackId).toList();
                //secondLevelCode 二级赛事编号 4:组别
                List<String> secondLevelCodes = applyInfoList.stream().map(CompetitionApplyInfo::getSecondLevelCode).toList();
                //按照sort分组得到要求的赛事系列、赛道和组别
                Map<Long, List<SysUserGroupCompetitionRelation>> groupBySort = sysUserGroupCompetitionRelations.stream().collect(Collectors.groupingBy(SysUserGroupCompetitionRelation::getSort));
                //如果报名的信息满足要求中的任何一个即可加入用户组
                for (Long key : groupBySort.keySet()) {
                    List<SysUserGroupCompetitionRelation> relations = groupBySort.get(key);
                    for (SysUserGroupCompetitionRelation relation : relations) {
                        switch (key.toString()) {
                            case "1":
                                if (CollectionUtils.isNotEmpty(competitionSeriesIds) && competitionSeriesIds.contains(relation.getCode())) {
                                    return true;
                                }
                                break;
                            case "3":
                                if (CollectionUtils.isNotEmpty(competitionTrackIds) && competitionTrackIds.contains(relation.getCode())) {
                                    return true;
                                }
                                break;
                            case "4":
                                if (CollectionUtils.isNotEmpty(secondLevelCodes) && secondLevelCodes.contains(relation.getCode())) {
                                    return true;
                                }
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 删除用户组后，清除缓存的此用户组
     *
     * @param userGroupId
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delUserGroupId(Long userGroupId) {
        log.info("删除用户组后，清除缓存的此用户组:{},start", userGroupId);
        if (userGroupId == null) {
            return;
        }
        Collection<String> keys = redisService.keys("userGroup:info:*");
        RLock lock = redisLock.getRLock("GLOBAL_DELUSERGROUPID_UPDATE_LOCK");
        try {
            lock.lock();
            for (String key : keys) {
                Set<Long> userGroupIds = redisService.getCacheObject(key);
                if (userGroupIds != null && userGroupIds.contains(userGroupId)) {
                    userGroupIds.remove(userGroupId);
                    redisService.setCacheObject(key, userGroupIds);
                }
            }
            //删除缓存的用户组信息
//        redisService.deleteObject("groupUserIds:info:" + userGroupId);
            String redisKey = "groupUserIds:info:" + userGroupId;

            redisService.deleteObject(redisKey);
        } finally {
            lock.unlock();
        }
        log.info("删除用户组后，清除缓存的此用户组:{},end", userGroupId);
    }

    /**
     * 删除用户组后，清除缓存的此用户组的所有信息
     *
     * @param userGroupIds
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delUserGroupIds(Long[] userGroupIds) {
        log.info("删除用户组后，清除缓存的此用户组的所有信息start");
        if (userGroupIds == null || userGroupIds.length == 0) {
            return;
        }
        Collection<String> keys = redisService.keys("userGroup:info:*");
        RLock lock = redisLock.getRLock("GLOBAL_DELUSERGROUPIDS_UPDATE_LOCK");
        try {
            lock.lock();
            for (Long userGroupId : userGroupIds) {
                for (String key : keys) {
                    Set<Long> ids = redisService.getCacheObject(key);
                    if (ids != null && ids.contains(userGroupId)) {
                        ids.remove(userGroupId);
                        redisService.setCacheObject(key, ids);
                    }
                }
                //删除缓存的用户组信息
//            redisService.deleteObject("groupUserIds:info:" + userGroupId);
                String redisKey = "groupUserIds:info:" + userGroupId;
                redisService.deleteObject(redisKey);
            }
        } finally {
            lock.unlock();
        }
        log.info("删除用户组后，清除缓存的此用户组的所有信息end");
    }

    /**
     * 更新用户组信息后，重新计算包含此用户组的用户的用户组信息
     *
     * @param userGroupId
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCachedUserGroupInfo(Long userGroupId) {
        log.info("更新用户组信息后，重新计算包含此用户组的用户的用户组信息:{},start", userGroupId);
        if (userGroupId == null) {
            return;
        }
        Collection<String> keys = redisService.keys("userGroup:info:*");
        for (String key : keys) {
            Set<Long> userGroupIds = redisService.getCacheObject(key);
            if (userGroupIds != null && userGroupIds.contains(userGroupId)) {
                String userId = key.substring(key.lastIndexOf(":") + 1);
                getUserGroupByLoginUser(Long.valueOf(userId));
            }
        }
        log.info("更新用户组信息后，重新计算包含此用户组的用户的用户组信息:{},end", userGroupId);
    }

    /**
     * 计算某个或全部用户组下的人员Ids
     *
     * @param userGroupId
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void getUserInfosByUserGroups(List<SysUserGroup> groupList, Long userGroupId) {
        String logId = CollectionUtils.isNotEmpty(groupList) ? groupList.get(0).getId() + "" : userGroupId == null ? "全部" : userGroupId + "";
        log.info("开始计算用户组下的人员，用户组:{}", logId);
        //获取所有正常状态的用户组
        List<SysUserGroup> sysUserGroups = CollectionUtils.isNotEmpty(groupList) ? groupList : sysUserGroupMapper.selectSysUserGroupAllInfoByIdOrAll(userGroupId);
        //循环用户组
        /*RLock lock = redisLock.getRLock( "GLOBAL_GETUSERINFOSBYUSERGROUPS_UPDATE_LOCK");
        try {
            lock.lock();*/
        sysUserGroups.forEach(sysUserGroup -> {
            //得到某个用户组下的黑名单、指定人员、指定身份信息、赛事规则
            String userIds = sysUserGroup.getUserIds();
            String blackUserIds = sysUserGroup.getBlackUserIds();
            String identifyType = sysUserGroup.getIdentifyType();
            String allowRoleName = sysUserGroup.getAllowRoleName();
            Long groupId = sysUserGroup.getId();
            Set<Long> groupUserIds = new HashSet<>();
            Map<String, Object> map = new HashMap<>();
            //certificationTypes 规则：身份
            if (StringUtils.isNotEmpty(identifyType)) {
                map.put("certificationTypes", Arrays.asList(identifyType.split(",")));
            }
            List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelations = sysUserGroup.getSysUserGroupCompetitionRelationList();
            if (CollectionUtils.isNotEmpty(sysUserGroupCompetitionRelations)) {
                Map<Long, List<SysUserGroupCompetitionRelation>> groupBySort = sysUserGroupCompetitionRelations.stream().collect(Collectors.groupingBy(SysUserGroupCompetitionRelation::getSort));
                // competitionSeriesId 赛事系列id  1:赛事,competitionTrackId 赛事赛道id 3:赛道,secondLevelCode 二级赛事编号 4:组别
                List<String> competitionSeriesIds = new ArrayList<>();
                List<String> competitionTrackIds = new ArrayList<>();
                List<String> secondLevelCodes = new ArrayList<>();
                groupBySort.forEach((key, relationList) -> {
                    for (SysUserGroupCompetitionRelation relation : relationList) {
                        switch (key.toString()) {
                            case "1":
                                competitionSeriesIds.add(relation.getCode());
                                break;
                            case "3":
                                competitionTrackIds.add(relation.getCode());
                                break;
                            case "4":
                                secondLevelCodes.add(relation.getCode());
                                break;
                        }
                    }
                });
                //competitionSeriesIds 规则：赛事id
                map.put("competitionSeriesIds", competitionSeriesIds);
                //competitionTrackIds 规则：赛道id
                map.put("competitionTrackIds", competitionTrackIds);
                //secondLevelCode 规则：二级赛区编码
                map.put("secondLevelCode", secondLevelCodes);
                map.put("hasValidCondition", !competitionSeriesIds.isEmpty() || !competitionTrackIds.isEmpty() || !secondLevelCodes.isEmpty());
            }
            if (map != null && !map.isEmpty()) {
                R<Set<Long>> userInfoByCompetitions = competitionService.getUserInfoByCompetitions(map, SecurityConstants.INNER);
                if (R.isSuccess(userInfoByCompetitions) && CollectionUtils.isNotEmpty(userInfoByCompetitions.getData())) {
                    //赛事匹配的userId
                    groupUserIds.addAll(userInfoByCompetitions.getData());
                }
                // 获取赛事下根据参赛角色得所有报名人员
                if (StringUtils.isNotEmpty(allowRoleName)) {
                    // 选中角色先清空得身份数据 段飞虎
                    groupUserIds.clear();
                    List<String> competitionRoleNames = Arrays.asList(allowRoleName.split(","));
                    map.put("competitionRoleName", competitionRoleNames);
                    log.info("获取赛事下根据参赛角色得所有报名人员，参数:{}", map);
                    R<List<CompetitionApplyInfo>> allUserInfoByCompetitions = competitionService.selectAllUserInfoByCompetitions(map, SecurityConstants.INNER);
                    if (R.isSuccess(allUserInfoByCompetitions) && CollectionUtils.isNotEmpty(allUserInfoByCompetitions.getData())) {
                        List<CompetitionApplyInfo> memberList = allUserInfoByCompetitions.getData().stream().
                                filter(competitionApplyInfo -> !"指导教师".equals(competitionApplyInfo.getCompetitionRoleName())).toList();
                        List<CompetitionApplyInfo> teacherList = allUserInfoByCompetitions.getData().stream().
                                filter(competitionApplyInfo -> "指导教师".equals(competitionApplyInfo.getCompetitionRoleName())).toList();
                        if (CollectionUtils.isNotEmpty(memberList)) {
                            List<String> idCards = memberList.stream().map(CompetitionApplyInfo::getIdCard).distinct().toList();
                            if (CollectionUtils.isNotEmpty(idCards)) {
                                // 去authInfo表拿userId,只有实名认证通过了才能确定人
                                List<List<String>> partition = Lists.partition(idCards, 200);
                                for (List<String> idCardList : partition) {
                                    List<AuthInfo> authInfos = authInfoMapper.selectAuthInfoListByUserInfo(idCardList);
                                    if (CollectionUtils.isNotEmpty(authInfos)) {
                                        groupUserIds.addAll(authInfos.stream().map(AuthInfo::getUserId).distinct().toList());
                                    }
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(teacherList)) {
                            // 指导老师信息数据原因只能通过手机号和名字匹配，指导教师存在重名，如果指导教师无手机号的情况，则无法匹配
                            teacherList.stream().forEach(teacher -> {
                                if (StringUtils.isNotEmpty(teacher.getPhone()) && StringUtils.isNotEmpty(teacher.getUserName())) {
                                    List<SysUser> sysUsers = sysUserMapper.selectUserByPhoneAndName(teacher.getPhone(), teacher.getUserName());
                                    if (CollectionUtils.isNotEmpty(sysUsers)) {
                                        groupUserIds.addAll(sysUsers.stream().map(SysUser::getUserId).distinct().toList());
                                    }
                                }
                            });
                        }
                    }
                }
            }
            //用户组下的人=指定的人员+（满足指定身份信息且满足赛事规则）-黑名单人员
            if (StringUtils.isNotEmpty(userIds)) {
                Arrays.stream(userIds.split(","))
                        .filter(StringUtils::isNotEmpty)
                        .map(id -> {
                            try {
                                return Long.valueOf(id);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .forEach(groupUserIds::add);
            }

            if (StringUtils.isNotEmpty(blackUserIds) && CollectionUtils.isNotEmpty(groupUserIds)) {
                Set<Long> blackIds = Arrays.stream(blackUserIds.split(","))
                        .filter(StringUtils::isNotEmpty)
                        .map(id -> {
                            try {
                                return Long.valueOf(id);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                groupUserIds.removeAll(blackIds);
            }
            /*redisService.deleteObject("groupUserIds:info:" + groupId);
            if (CollectionUtils.isNotEmpty(groupUserIds)) {
                //某用户所在的用户组信息
                redisService.setCacheObject("groupUserIds:info:" + groupId, groupUserIds);
            }*/
            // Redis操作加锁
            String redisKey = "groupUserIds:info:" + groupId;
            synchronized (redisKey.intern()) {
                redisService.deleteObject(redisKey);
                if (!groupUserIds.isEmpty()) {
                    redisService.setCacheObject(redisKey, groupUserIds);
                }
            }

        });
        log.info("计算用户组下的人员，用户组:{}，结束", logId);
    }
}
