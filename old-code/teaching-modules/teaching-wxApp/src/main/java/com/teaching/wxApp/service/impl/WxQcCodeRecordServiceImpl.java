package com.teaching.wxApp.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.redis.service.RedisLock;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.wxApp.domain.WxQcCodeRecord;
import com.teaching.wxApp.domain.WxSignInInfo;
import com.teaching.wxApp.mapper.WxQcCodeRecordMapper;
import com.teaching.wxApp.mapper.WxSignInInfoMapper;
import com.teaching.wxApp.service.IWxQcCodeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 二维码生成记录Service业务层处理
 *
 * @author teaching
 * @date 2026-04-08
 */
@Slf4j
@Service
public class WxQcCodeRecordServiceImpl implements IWxQcCodeRecordService {

    @Autowired
    private WxQcCodeRecordMapper wxQcCodeRecordMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private WxSignInInfoMapper wxSignInInfoMapper;
    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisLock redisLock;


    /**
     * 查询二维码生成记录
     *
     * @param recordId 二维码生成记录主键
     * @return 二维码生成记录
     */
    @Override
    public WxQcCodeRecord selectWxQcCodeRecordByRecordId(Long recordId) {
        return wxQcCodeRecordMapper.selectWxQcCodeRecordByRecordId(recordId);
    }

    @Override
    public Map<String, Object> getWxQcCodeBaseByRecordId(Long recordId) {
        return wxQcCodeRecordMapper.selectWxQcCodeBaseByRecordId(recordId);
    }

    /**
     * 查询二维码生成记录列表  不含base64字段
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 二维码生成记录
     */
    @Override
    public List<WxQcCodeRecord> selectWxQcCodeRecordList(WxQcCodeRecord wxQcCodeRecord) {
        return wxQcCodeRecordMapper.selectWxQcCodeRecordList(wxQcCodeRecord);
    }

    /**
     * 查询二维码生成记录-含base64字段
     * @param wxQcCodeRecord
     * @return
     */
    @Override
    public List<WxQcCodeRecord> selectWxQcCodeRecordInfosList(WxQcCodeRecord wxQcCodeRecord) {
//        return wxQcCodeRecordMapper.selectWxQcCodeRecordInfosList(wxQcCodeRecord);
        return wxQcCodeRecordMapper.selectWxQcCodeRecordList(wxQcCodeRecord);
    }

    /**
     * 新增二维码生成记录
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 结果
     */
    @Override
    public int insertWxQcCodeRecord(WxQcCodeRecord wxQcCodeRecord) {
        return wxQcCodeRecordMapper.insertWxQcCodeRecord(wxQcCodeRecord);
    }

    /**
     * 修改二维码生成记录
     *
     * @param wxQcCodeRecord 二维码生成记录
     * @return 结果
     */
    @Override
    public int updateWxQcCodeRecord(WxQcCodeRecord wxQcCodeRecord) {
        wxQcCodeRecord.setUpdateTime(DateUtils.getNowDate());
        redisService.deleteObject(SecurityConstants.WX_QC_CODE_RECORD + wxQcCodeRecord.getRecordId());
        return wxQcCodeRecordMapper.updateWxQcCodeRecord(wxQcCodeRecord);
    }

    /**
     * 批量删除二维码生成记录
     *
     * @param recordIds 需要删除的二维码生成记录主键
     * @return 结果
     */
    @Override
    public int deleteWxQcCodeRecordByRecordIds(Long[] recordIds) {
        for (Long recordId : recordIds) {
            redisService.deleteObject(SecurityConstants.WX_QC_CODE_RECORD + recordId);
        }
        return wxQcCodeRecordMapper.deleteWxQcCodeRecordByRecordIds(recordIds);
    }

    /**
     * 删除二维码生成记录
     *
     * @param recordId 二维码生成记录主键
     * @return 结果
     */
    @Override
    public int deleteWxQcCodeRecordByRecordId(Long recordId) {
        redisService.deleteObject(SecurityConstants.WX_QC_CODE_RECORD + recordId);
        return wxQcCodeRecordMapper.deleteWxQcCodeRecordByRecordId(recordId);
    }

    @Override
    public Map<String, Object> getRecordAndConfig(Long id) {
        return wxQcCodeRecordMapper.selectRecordAndConfig(id);
    }

    /**
     * 加载并缓存赛事报名信息（按 teamCode 分组为 Hash 结构），同时构建 userId -> teamCodes 索引
     * 高并发防护：分布式锁 + 双重检查 + 空值缓存
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<CompetitionApplyInfo>> loadAndCacheApplyMap(Long competitionSeriesId) {
        String teamKey = SecurityConstants.COMPETITION_APPLY_TEAM_INFO_BY_SERIESID + competitionSeriesId;
        Map<String, List<CompetitionApplyInfo>> applyMap = null;
        // 第1次检查：用 hasKey 判断，因为 opsForHash().entries() 在 key 不存在时返回空 Map（非 null）
        if (Boolean.TRUE.equals(redisService.hasKey(teamKey))) {
            return redisService.getCacheMap(teamKey);
        }

        // 未命中，尝试获取分布式锁（按 seriesId 粒度，避免全局串行）
        String lockKey = "lock:competition:apply:seriesId:" + competitionSeriesId;
        boolean locked = false;
        try {
            locked = redisLock.tryLock(lockKey, 5, 30, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取分布式锁失败，跳过加载，seriesId:{}" , competitionSeriesId);
                return new HashMap<>();
            }

            // 第2次检查：拿到锁后再判断一次，防止其他线程已加载完毕
            if (Boolean.TRUE.equals(redisService.hasKey(teamKey))) {
                return redisService.getCacheMap(teamKey);
            }

            log.warn("加载赛事报名信息，缓存不存在，从数据库查询，seriesId:{}" , competitionSeriesId);
            R<List<CompetitionApplyInfo>> applyInfoResult = competitionService.getCompetitionApplyInfoListByCompetitionSeriesId(competitionSeriesId, SecurityConstants.INNER);
            if (applyInfoResult != null && R.isSuccess(applyInfoResult) && applyInfoResult.getData() != null) {
                List<CompetitionApplyInfo> list = applyInfoResult.getData();
                if (!CollectionUtils.isEmpty(list)) {
                    applyMap = list.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
                    redisService.setCacheMap(teamKey, applyMap);

                    // 构建 userId -> teamCodes 索引
                    String indexKey = SecurityConstants.COMPETITION_APPLY_USER_TEAM_INDEX + competitionSeriesId;
                    Map<String, String> userIndex = new HashMap<>();
                    Map<Long, Set<String>> userTeamMap = new HashMap<>();
                    for (CompetitionApplyInfo info : list) {
                        if (info.getUserId() != null && StringUtils.isNotBlank(info.getTeamCode())) {
                            userTeamMap.computeIfAbsent(info.getUserId(), k -> new HashSet<>()).add(info.getTeamCode());
                        }
                    }
                    for (Map.Entry<Long, Set<String>> entry : userTeamMap.entrySet()) {
                        userIndex.put(String.valueOf(entry.getKey()), String.join(",", entry.getValue()));
                    }
                    if (!userIndex.isEmpty()) {
                        redisService.setCacheMap(indexKey, userIndex);
                    }

                    // 构建 retry 索引：schoolName|userName|idCard后6位 -> teamCodes
                    String retryIndexKey = SecurityConstants.COMPETITION_APPLY_RETRY_INDEX + competitionSeriesId;
                    Map<String, Set<String>> retryIndexMap = new HashMap<>();
                    for (CompetitionApplyInfo info : list) {
                        if (StringUtils.isNotBlank(info.getIdCard())
                                && StringUtils.isNotBlank(info.getSchoolName())
                                && StringUtils.isNotBlank(info.getUserName())
                                && StringUtils.isNotBlank(info.getTeamCode())) {
                            String idCardSuffix = info.getIdCard().toUpperCase();
                            if (idCardSuffix.length() > 6) {
                                idCardSuffix = idCardSuffix.substring(idCardSuffix.length() - 6);
                            }
                            String field = info.getSchoolName() + "|" + info.getUserName() + "|" + idCardSuffix;
                            retryIndexMap.computeIfAbsent(field, k -> new HashSet<>()).add(info.getTeamCode());
                        }
                    }
                    Map<String, String> retryIndexToSave = new HashMap<>();
                    for (Map.Entry<String, Set<String>> entry : retryIndexMap.entrySet()) {
                        retryIndexToSave.put(entry.getKey(), String.join(",", entry.getValue()));
                    }
                    if (!retryIndexToSave.isEmpty()) {
                        redisService.setCacheMap(retryIndexKey, retryIndexToSave);
                    }

                    log.warn("加载赛事报名信息，数据库有数据，已按团队缓存，共{}个团队" , applyMap.size());
                } else {
                    // 空值缓存：防止缓存穿透，设置空 Map 并设置较短的过期时间（5分钟）
                    redisService.setCacheMap(teamKey, new HashMap<>());
                    redisService.expire(teamKey, 300);
                }
            } else {
                // 接口异常也做空值缓存，避免频繁穿透到下游服务
                redisService.setCacheMap(teamKey, new HashMap<>());
                redisService.expire(teamKey, 300);
            }
        } finally {
            if (locked) {
                redisLock.unlock(lockKey);
            }
        }
        return applyMap == null ? new HashMap<>() : applyMap;
    }

    @Override
    public Map<String, Object> scanCode(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        Long recordId = MapUtil.getLong(params, "recordId");
        log.warn("进入扫码接口，recordId:{}", recordId);
        if (recordId == null) {
            throw new RuntimeException("参数错误！");
        }
        Long selectedUserId = MapUtil.getLong(params, "selectedUserId", null);
        Long userid = null;
        WxSignInInfo wxSignInInfo = new WxSignInInfo();
        try {
            //获取二维码生成记录和配置信息  配置信息查缓存
            Map<String, Object> recordAndConfig = redisService.getCacheObject(SecurityConstants.WX_QC_CODE_RECORD + recordId);
            if (recordAndConfig == null || recordAndConfig.isEmpty()) {
                log.debug("进入扫码接口，redis中没有，去数据库查询");
                recordAndConfig = getRecordAndConfig(recordId);
                if (recordAndConfig != null && !recordAndConfig.isEmpty()) {
                    redisService.setCacheObject(SecurityConstants.WX_QC_CODE_RECORD + recordId, recordAndConfig);
                }
            }
            result.put("checkInFlag", false);
            if(recordAndConfig == null || recordAndConfig.isEmpty()){
                throw new GlobalException("二维码有误或配置信息不存在！");
            }
            Long competitionSeriesId = MapUtil.getLong(recordAndConfig, "competitionSeriesId");
            //获取登录人id
            userid = selectedUserId == null ? SecurityUtils.getLoginUser().getUserid() : selectedUserId;
            log.debug("进入扫码接口，登录人id：{}", userid);
            //1有报名信息，2没报名信息，3接口错误
            wxSignInInfo.setResultType("2");
            result.putAll(recordAndConfig);

            // 从用户索引获取团队编号，避免全量加载报名数据到内存
            String indexKey = SecurityConstants.COMPETITION_APPLY_USER_TEAM_INDEX + competitionSeriesId;
            String teamCodesStr = redisService.getCacheMapValue(indexKey, String.valueOf(userid));
            Set<String> teamCodes = new HashSet<>();

            if (StringUtils.isBlank(teamCodesStr)) {
                log.debug("进入扫码接口，用户索引不存在，加载全量数据构建索引");
                Map<String, List<CompetitionApplyInfo>> applyMap = loadAndCacheApplyMap(competitionSeriesId);
                for (List<CompetitionApplyInfo> team : applyMap.values()) {
                    for (CompetitionApplyInfo member : team) {
                        if (userid.equals(member.getUserId()) && StringUtils.isNotBlank(member.getTeamCode())) {
                            teamCodes.add(member.getTeamCode());
                        }
                    }
                }
            } else {
                teamCodes = Arrays.stream(teamCodesStr.split(","))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());
            }

            List<CompetitionApplyInfo> applyInfos = new ArrayList<>();
            if (!teamCodes.isEmpty()) {
                log.debug("进入扫码接口，有报名信息了，用户所在团队:{}" , teamCodes);
                String teamKey = SecurityConstants.COMPETITION_APPLY_TEAM_INFO_BY_SERIESID + competitionSeriesId;
                for (String teamCode : teamCodes) {
                    List<CompetitionApplyInfo> team = redisService.getCacheMapValue(teamKey, teamCode);
                    if (team != null) {
                        applyInfos.addAll(team);
                    }
                }
            }
            //查询报名信息成功，并且有数据
            if (!CollectionUtils.isEmpty(applyInfos)) {
                log.debug("进入扫码接口，有报名信息");
                wxSignInInfo.setResultType("1");
                Long codeConfigId = MapUtil.getLong(recordAndConfig, "codeConfigId");
                result.put("codeConfigId", codeConfigId);
                result.put("checkInFlag", true);
                //从applyInfos筛选出当前用户的user_name
                Long finalUserid1 = userid;
                String userName = applyInfos.stream().filter(applyInfo -> finalUserid1.equals(applyInfo.getUserId())).map(CompetitionApplyInfo::getUserName).findFirst().orElse("");
                result.put("userName", userName);
                //大赛名称
                result.put("competitionName", applyInfos.get(0).getCompetitionName());
                //applyInfos根据teamCode分组
                Map<String, List<CompetitionApplyInfo>> applyInfosMap = applyInfos.stream().collect(Collectors.groupingBy(CompetitionApplyInfo::getTeamCode));
                List<Map<String, Object>> teams = new ArrayList<>();
                applyInfosMap.forEach((key, value) -> {
                    Map<String, Object> team = new HashMap<>();
                    //团队名称
                    team.put("teamName", value.get(0).getTeamName());
                    //队员 value过滤competitionRoleName是队长或是队员的
                    team.put("userNames", value.stream().filter(applyInfo -> "队长".equals(applyInfo.getCompetitionRoleName()) || "队员".equals(applyInfo.getCompetitionRoleName()))
                            .map(CompetitionApplyInfo::getUserName).collect(Collectors.joining(",")));
                    //带队老师
                    team.put("leaderTeacher", value.get(0).getLeaderTeacher());
                    //指导老师 过滤掉null的
                    team.put("guideTeacher", value.stream().filter(applyInfo -> StringUtils.isNotBlank(applyInfo.getGuideTeacher())).map(CompetitionApplyInfo::getGuideTeacher).collect(Collectors.joining(",")));
                    //赛道
                    team.put("track", value.get(0).getCompetitionTrackName());
                    //组别
                    team.put("group", value.get(0).getSecondLevelName());
                    teams.add(team);
                });
                result.put("teams", teams);
            }
        } catch (GlobalException e) {
            wxSignInInfo.setResultType("3");
            log.debug("进入扫码接口，业务异常：{}", e.getMessage());
            wxSignInInfo.setRemark(selectedUserId == null ? "接口异常" : "扫码重试异常");
            throw e;
        } catch (Exception e) {
            wxSignInInfo.setResultType("3");
            log.debug("进入扫码接口，接口异常");
            wxSignInInfo.setRemark(selectedUserId == null ? "接口异常" : "扫码重试异常");
            throw new RuntimeException(e);
        } finally {
            log.debug("进入扫码接口，新增扫描记录,userid:{},resultType:{},checkInType:{}", userid, wxSignInInfo.getResultType(),selectedUserId == null ? "正常扫码签到" : "信息查询签到");
            Date signTime = MapUtil.getDate(params, "signTime");
            result.put("checkInTime", DateUtils.getDateFormat(signTime));
            //签到记录入库
            wxSignInInfo.setCreateBy(SecurityUtils.getLoginUser().getUserid()+"");
            wxSignInInfo.setUserId(userid);
            wxSignInInfo.setRecordId(recordId);
            wxSignInInfo.setSignTime(signTime);
            wxSignInInfo.setCreateTime(DateUtils.getNowDate());
            wxSignInInfo.setIp(IpUtils.getIpAddr());
            wxSignInInfo.setCheckInType(selectedUserId == null ? "正常扫码签到" : "信息查询签到");
            wxSignInInfoMapper.insertWxSignInInfo(wxSignInInfo);
        }
        return result;
    }

    @Override
    public Map<String, Object> retry(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("参数错误！");
        }
        Long recordId = MapUtil.getLong(params, "recordId");
        if (recordId == null) {
            throw new RuntimeException("参数错误！");
        }
        String schoolName = MapUtil.getStr(params, "schoolName").trim();
        String idCard = MapUtil.getStr(params, "idCard").trim();
        String studentName = MapUtil.getStr(params, "studentName").trim();
        if (StringUtils.isBlank(schoolName) || StringUtils.isBlank(idCard) || StringUtils.isBlank(studentName)) {
            throw new RuntimeException("参数错误！");
        }
        Map<String, Object> result = new HashMap<>();
        //获取二维码生成记录和配置信息  配置信息查缓存
        Map<String, Object> recordAndConfig = redisService.getCacheObject(SecurityConstants.WX_QC_CODE_RECORD + recordId);
        if (recordAndConfig == null || recordAndConfig.isEmpty()) {
            log.warn("进入扫码重试接口，redis中没有，去数据库查询");
            recordAndConfig = getRecordAndConfig(recordId);
            if (recordAndConfig != null && !recordAndConfig.isEmpty()) {
                log.warn("进入扫码重试接口，数据库中有");
                redisService.setCacheObject(SecurityConstants.WX_QC_CODE_RECORD + recordId, recordAndConfig);
            }
        }
        if(recordAndConfig == null || recordAndConfig.isEmpty()){
            throw new GlobalException("二维码有误或配置信息不存在！");
        }
        Long competitionSeriesId = MapUtil.getLong(recordAndConfig, "competitionSeriesId");
        result.put("checkInFlag", false);
        String msg = "根据查询信息暂未查询到报名信息，请检查输入信息是否正确";
        result.put("msg", msg);
        result.putAll(recordAndConfig);
        // 从索引获取 teamCodes，避免全量加载报名数据到内存
        String retryIndexKey = SecurityConstants.COMPETITION_APPLY_RETRY_INDEX + competitionSeriesId;
        String idCardSuffix = idCard.toUpperCase();
        String indexField = schoolName + "|" + studentName + "|" + idCardSuffix;
        String teamCodesStr = redisService.getCacheMapValue(retryIndexKey, indexField);

        Set<String> teamCodes = new HashSet<>();
        Map<String, List<CompetitionApplyInfo>> teamCache = new HashMap<>();

        if (StringUtils.isBlank(teamCodesStr)) {
            log.warn("进入扫码重试接口，索引不存在，加载全量数据构建索引");
            Map<String, List<CompetitionApplyInfo>> applyMap = loadAndCacheApplyMap(competitionSeriesId);
            for (List<CompetitionApplyInfo> team : applyMap.values()) {
                for (CompetitionApplyInfo member : team) {
                    if (StringUtils.isNotBlank(member.getIdCard())
                            && member.getIdCard().toUpperCase().endsWith(idCard.toUpperCase())
                            && schoolName.equals(member.getSchoolName())
                            && studentName.equals(member.getUserName())
                            && StringUtils.isNotBlank(member.getTeamCode())) {
                        teamCodes.add(member.getTeamCode());
                        teamCache.putIfAbsent(member.getTeamCode(), team);
                    }
                }
            }
        } else {
            teamCodes = Arrays.stream(teamCodesStr.split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            String teamKey = SecurityConstants.COMPETITION_APPLY_TEAM_INFO_BY_SERIESID + competitionSeriesId;
            for (String teamCode : teamCodes) {
                List<CompetitionApplyInfo> team = redisService.getCacheMapValue(teamKey, teamCode);
                if (team != null) {
                    teamCache.put(teamCode, team);
                }
            }
        }

        // 从 teamCache 中筛选出匹配的个人记录
        List<CompetitionApplyInfo> applyInfos = new ArrayList<>();
        if (!teamCache.isEmpty()) {
            log.warn("进入扫码重试接口，有报名信息了");
            for (List<CompetitionApplyInfo> team : teamCache.values()) {
                for (CompetitionApplyInfo member : team) {
                    if (StringUtils.isNotBlank(member.getIdCard())
                            && member.getIdCard().toUpperCase().endsWith(idCard.toUpperCase())
                            && schoolName.equals(member.getSchoolName())
                            && studentName.equals(member.getUserName())) {
                        applyInfos.add(member);
                    }
                }
            }
        }
        //查询报名信息成功，并且有数据
        if (!CollectionUtils.isEmpty(applyInfos)) {
            log.warn("进入扫码重试接口，有报名信息{}", applyInfos);
            msg = "查询报名信息成功";
//            Set<String> collect = applyInfos.stream().map(CompetitionApplyInfo::getTeamCode).collect(Collectors.toSet());
            //再从cachelist中teamCode在collect中的所有记录
//            applyInfos = cacheList.stream().filter(applyInfo -> collect.contains(applyInfo.getTeamCode())).toList();

            Long codeConfigId = MapUtil.getLong(recordAndConfig, "codeConfigId");
            String notMeMsg = MapUtil.getStr(recordAndConfig, "notMeMsg");
            result.put("notMeMsg", notMeMsg);
            result.put("codeConfigId", codeConfigId);
            result.put("checkInFlag", true);
            result.put("userName", studentName);
            //大赛名称
            result.put("competitionName", applyInfos.get(0).getCompetitionName());
            //applyInfos根据teamCode分组
            List<Map<String, Object>> teams = new ArrayList<>();
            for (CompetitionApplyInfo info : applyInfos) {
                log.warn("进入扫码重试接口，有报名信息,info:{}", info);
                List<CompetitionApplyInfo> list = teamCache.get(info.getTeamCode());
                if (list == null) {
                    list = Collections.emptyList();
                }
                Map<String, Object> team = new HashMap<>();
                team.put("userId", info.getUserId());
                team.put("userName", info.getUserName());
                String email = info.getEmail();
                team.put("email", StrUtil.hide(email, 3, StrUtil.indexOf(email, '@')));
                //团队名称
                team.put("teamName", info.getTeamName());
                //队员
                team.put("userNames", list.stream().filter(applyInfo -> "队长".equals(applyInfo.getCompetitionRoleName()) || "队员".equals(applyInfo.getCompetitionRoleName()))
                        .map(CompetitionApplyInfo::getUserName).collect(Collectors.joining(",")));
                //带队老师
                team.put("leaderTeacher", info.getLeaderTeacher());
                //指导老师 过滤掉null的
                team.put("guideTeacher", list.stream().filter(applyInfo -> StringUtils.isNotBlank(applyInfo.getGuideTeacher())).map(CompetitionApplyInfo::getGuideTeacher).collect(Collectors.joining(",")));
                //赛道
                team.put("track", info.getCompetitionTrackName());
                //组别
                team.put("group", info.getSecondLevelName());
                teams.add(team);
            }
            result.put("teams", teams);
        }
        result.put("msg", msg);
        return result;
    }

    @Override
    public Map<String, Object> weChatMyInfo(Map<String, String> params) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userid = loginUser.getUserid();
        if (userid == null) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("roles", loginUser.getRoles());
        result.put("permissions", loginUser.getPermissions());
        R<SysUser> userInfoById = userService.getUserInfoById(userid, SecurityConstants.INNER);
        if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
            SysUser data = userInfoById.getData();
            result.put("phone", DesensitizedUtil.mobilePhone(data.getPhonenumber()));
            result.put("avatar", data.getAvatar());
            result.put("authStatus", data.getAuthStatus());
            String userName = data.getNickName();
            result.put("userName", PhoneUtil.isPhone(userName) ? DesensitizedUtil.mobilePhone(userName) : userName);
        }
        return result;
    }

}
