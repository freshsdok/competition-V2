package com.teaching.competition.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.ServletUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.CompetitionPromotedApplyInfo;
import com.teaching.competition.domain.CompetitionPromotedApplyPcInfo;
import com.teaching.competition.domain.CompetitionPromotedInfo;
import com.teaching.competition.domain.PromotedPlayerInfo;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.IChangeLogService;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import com.teaching.competition.service.ICompetitionPromotedApplyInfoService;
import com.teaching.system.api.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 赛事晋级申请报名信息Service业务层处理
 *
 * @author teaching
 * @date 2026-05-19
 */
@Slf4j
@Service
public class CompetitionPromotedApplyInfoServiceImpl implements ICompetitionPromotedApplyInfoService {
    @Autowired
    private CompetitionPromotedApplyInfoMapper competitionPromotedApplyInfoMapper;
    @Autowired
    private CompetitionPromotedInfoMapper competitionPromotedInfoMapper;
    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private CompetitionMainInfoMapper competitionMainInfoMapper;

    @Autowired
    private IChangeLogService changeLogService;


    /**
     * 查询赛事晋级申请报名信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 赛事晋级申请报名信息
     */
    @Override
    public CompetitionPromotedApplyInfo selectCompetitionPromotedApplyInfoByApplyId(Long applyId) {
        return competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoByApplyId(applyId);
    }

    /**
     * 查询赛事晋级申请报名信息列表
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 赛事晋级申请报名信息
     */
    @Override
    public List<CompetitionPromotedApplyInfo> selectCompetitionPromotedApplyInfoList(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        return competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoList(competitionPromotedApplyInfo);
    }

    @Override
    public List<CompetitionPromotedApplyPcInfo> selectCompetitionPromotedApplyInfoPcList(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        List<CompetitionPromotedApplyPcInfo> competitionPromotedApplyInfos = competitionPromotedApplyInfoMapper.selectPromotedPlayerInfoListByCompetitionSeriesPc(competitionPromotedApplyInfo);
        if (CollectionUtils.isNotEmpty(competitionPromotedApplyInfos)) {
            competitionPromotedApplyInfos.forEach(applyInfo -> {
                applyInfo.setRegistrationTime("1".equals(applyInfo.getApplyStatus()) ? applyInfo.getRegistrationTime() : null);
                List<PromotedPlayerInfo> promotedPlayerInfoList = applyInfo.getPromotedPlayerInfoList();
                //指导教师
                List<PromotedPlayerInfo> guideTeacher = promotedPlayerInfoList.stream().filter(temp -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(temp.getCompetitionRoleName())).toList();
                //队员
                List<PromotedPlayerInfo> playerInfos = promotedPlayerInfoList.stream().filter(temp -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(temp.getCompetitionRoleName())).toList();
                if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(playerInfos)) {
                    playerInfos.forEach(player -> {
                        if (Objects.nonNull(player.getTeamSort())) {
                            switch (player.getTeamSort()) {
                                case 1:
                                    applyInfo.setPromotedPlayerInfoOne(player);
                                    break;
                                case 2:
                                    applyInfo.setPromotedPlayerInfoTwo(player);
                                    break;
                                case 3:
                                    applyInfo.setPromotedPlayerInfoThree(player);
                                    break;
                                case 4:
                                    applyInfo.setPromotedPlayerInfoFour(player);
                                    break;
                                case 5:
                                    applyInfo.setPromotedPlayerInfoFive(player);
                                    break;
                                case 6:
                                    applyInfo.setPromotedPlayerInfoSix(player);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(guideTeacher)) {
                    guideTeacher.forEach(teacher -> {
                        if (Objects.nonNull(teacher.getTeamSort())) {
                            switch (teacher.getTeamSort()) {
                                case 1:
                                    applyInfo.setPromotedTeacherOne(teacher);
                                    break;
                                case 2:
                                    applyInfo.setPromotedTeacherTwo(teacher);
                                    break;
                            }
                        }
                    });
                }
            });
        }
        return competitionPromotedApplyInfos;
    }

    /**
     * 新增赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfoList 赛事晋级申请报名信息列表
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCompetitionPromotedApplyInfo(List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfoList) {
        String userName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        if (CollectionUtils.isEmpty(competitionPromotedApplyInfoList)) {
            return 0;
        }
        // 获取所有teamCode
        List<String> teamCodeList = competitionPromotedApplyInfoList.stream()
                .map(CompetitionPromotedApplyInfo::getTeamCode)
                .filter(StringUtils::isNotEmpty)
                .map(code -> code.replaceAll("\\s+", ""))
                .distinct()
                .toList();
        Long competitionSeriesId = competitionPromotedApplyInfoList.get(0).getCompetitionSeriesId();
        // 查询已存在的teamCode
        Set<String> existTeamCodes = competitionPromotedApplyInfoMapper.selectExistTeamCodes(competitionSeriesId);
        // 查询老报名表是否存在
        Set<String> existOldTeamCodes = competitionApplyInfoMapper.selectApplyInfoCompetitionId(competitionSeriesId);
        // 赛事信息
        CompetitionMainInfoReq req = new CompetitionMainInfoReq();
        req.setCompetitionSeriesId(competitionSeriesId);
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoMapper.selectCompetitionDetailInfoByCompetitionId(req);
        // 通过competitionSeriesId肯定确定唯一结果
        String competitionName = competitionDetailInfoList.get(0).getCompetitionSeriesName() + competitionDetailInfoList.get(0).getCompetitionName();
        List<CompetitionTrackInfo> competitionTrackInfos = competitionTrackInfoMapper.selectCompetitionTrackInfoByCompetitionSeriesId(competitionSeriesId);
        // 赛事下赛道code及名字的map
        Map<String, String> competitionTrackConfigMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(competitionTrackInfos)) {
            competitionTrackConfigMap = competitionTrackInfos.stream()
                    .collect(Collectors.toMap(
                            CompetitionTrackInfo::getCompetitionTrackName,
                            CompetitionTrackInfo::getCompetitionTrackId,
                            (existing, replacement) -> existing
                    ));
        }
        List<String> competitionTrackIdList = competitionTrackInfos.stream()
                .map(CompetitionTrackInfo::getCompetitionTrackId)
                .toList();
        List<CompetitionTrackConfig> competitionTrackConfigList =
                competitionTrackConfigMapper.selectCompetitionTrackConfigAllList(competitionTrackIdList);
        Map<String, String> secondLevelMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(competitionTrackInfos)) {
            secondLevelMap = competitionTrackConfigList.stream()
                    .collect(Collectors.toMap(
                            CompetitionTrackConfig::getSecondLevelName,
                            CompetitionTrackConfig::getSecondLevelCode,
                            (existing, replacement) -> existing
                    ));
        }
        // 所有赛道对应组别的map
        // 转换为CompetitionPromotedApplyInfo对象并批量插入
        List<CompetitionPromotedApplyInfo> promotedApplyInfoList = new ArrayList<>();
        for (String teamCode : teamCodeList) {
            // 如果teamCode已存在，跳过
            if (existTeamCodes.contains(teamCode)) {
                continue;
            }
            if (existOldTeamCodes.contains(teamCode)) {
                continue;
            }
            // 根据teamCode从competition_apply_info表查询所有未删除信息
            List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamCode);
            if (CollectionUtils.isNotEmpty(applyInfoList)) {
                for (CompetitionApplyInfo applyInfo : applyInfoList) {
                    CompetitionPromotedApplyInfo promotedApplyInfo = new CompetitionPromotedApplyInfo();
                    // 使用BeanUtils复制属性
                    BeanUtils.copyProperties(applyInfo, promotedApplyInfo);
                    //赛事信息重新匹配
                    if (MapUtils.isNotEmpty(competitionTrackConfigMap)) {
                        promotedApplyInfo.setCompetitionTrackId(competitionTrackConfigMap.get(applyInfo.getCompetitionTrackName()));
                    }
                    if (MapUtils.isNotEmpty(secondLevelMap)) {
                        promotedApplyInfo.setSecondLevelCode(secondLevelMap.get(applyInfo.getSecondLevelName()));
                    }
                    promotedApplyInfo.setCompetitionSeriesId(competitionSeriesId);
                    promotedApplyInfo.setCompetitionName(competitionName);
                    // 设置额外字段
                    promotedApplyInfo.setCreateBy(userName);
                    promotedApplyInfoList.add(promotedApplyInfo);
                }
            } else {
                // 如果teamCode不存在，则插入一条数据
                CompetitionPromotedApplyInfo promotedApplyInfo = new CompetitionPromotedApplyInfo();
                promotedApplyInfo.setCompetitionName(competitionName);
                promotedApplyInfo.setCompetitionSeriesId(competitionSeriesId);
                promotedApplyInfo.setTeamCode(teamCode);
                promotedApplyInfo.setCreateBy(userName);
                promotedApplyInfoList.add(promotedApplyInfo);
            }
        }

        if (promotedApplyInfoList.isEmpty()) {
            return 1;
        }
        if (competitionPromotedInfoMapper.selectCompetitionPromotedInfoByCompetitionSeriesId(competitionSeriesId) <= 0) {
            // 新增赛事晋级信息
            CompetitionPromotedInfo competitionPromotedInfo = new CompetitionPromotedInfo();
            competitionPromotedInfo.setCompetitionSeriesId(competitionSeriesId);
            competitionPromotedInfoMapper.insertCompetitionPromotedInfo(competitionPromotedInfo);
        }
        // 批量插入
        return competitionPromotedApplyInfoMapper.batchInsertCompetitionPromotedApplyInfo(promotedApplyInfoList);
    }

    /**
     * 修改赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 结果
     */
    @Override
    public int updateCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        competitionPromotedApplyInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionPromotedApplyInfoMapper.updateCompetitionPromotedApplyInfo(competitionPromotedApplyInfo);
    }

    /**
     * 批量删除赛事晋级申请报名信息
     *
     * @param applyIds 需要删除的赛事晋级申请报名信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionPromotedApplyInfoByApplyIds(Long[] applyIds) {
        return competitionPromotedApplyInfoMapper.deleteCompetitionPromotedApplyInfoByApplyIds(applyIds);
    }

    /**
     * 删除赛事晋级申请报名信息信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionPromotedApplyInfoByApplyId(Long applyId) {
        return competitionPromotedApplyInfoMapper.deleteCompetitionPromotedApplyInfoByApplyId(applyId);
    }

    /**
     * 查询赛事晋级申请报名信息总数
     *
     * @param competitionSeriesId 赛事系列赛id
     * @return
     */
    @Override
    public Integer countCompetitionPromotedApplyInfoList(Long competitionSeriesId) {
        return competitionPromotedApplyInfoMapper.countCompetitionPromotedApplyInfoList(competitionSeriesId);
    }

    /**
     * 根据赛事系列查询晋级报名信息
     *
     * @param competitionPromotedApplyInfo
     * @return
     */
    @Override
    public List<CompetitionPromotedApplyInfo> getPromotedPlayerInfoListByCompetitionSeriesId(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfos = competitionPromotedApplyInfoMapper.selectPromotedPlayerInfoListByCompetitionSeries(competitionPromotedApplyInfo);
        if (CollectionUtils.isNotEmpty(competitionPromotedApplyInfos)) {
            competitionPromotedApplyInfos.forEach(e -> {
                List<PromotedPlayerInfo> promotedPlayerInfoList = e.getPromotedPlayerInfoList();
                List<PromotedPlayerInfo> guideTeacher = new ArrayList<>(promotedPlayerInfoList.stream().filter(temp -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(temp.getCompetitionRoleName())).toList());
                //guideTeacher按照teamSort升序排序
                guideTeacher.sort(Comparator.comparing(PromotedPlayerInfo::getTeamSort));
                e.setGuideTeacherInfoList(guideTeacher);
                List<PromotedPlayerInfo> playerInfos = new ArrayList<>(promotedPlayerInfoList.stream().filter(temp -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(temp.getCompetitionRoleName())).toList());
                playerInfos.sort(Comparator.comparing(PromotedPlayerInfo::getTeamSort));
                e.setPlayerInfoList(playerInfos);
                e.setPromotedPlayerInfoList(null);
            });
        }
        return competitionPromotedApplyInfos;
    }

    @Override
    public List<CompetitionPromotedApplyInfo> getPromotedPlayerInfoListPcByCompetitionSeriesId(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        competitionPromotedApplyInfo.setLeaderTeacherId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return getPromotedPlayerInfoListByCompetitionSeriesId(competitionPromotedApplyInfo);
    }


    @Override
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodes(String[] teamCodes) {
        return competitionPromotedApplyInfoMapper.logicalDelCompetitionPromotedApplyInfoByTeamCodes(teamCodes);
    }

    /**
     * 根据teamCode和赛事系列删除赛事晋级申请报名信息
     *
     * @param teamCode
     * @param competitionSeriesId
     * @return
     */
    @Override
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodeAndCompetitionSeriesId(String teamCode, Long competitionSeriesId) {
        String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        String applyInfo = competitionPromotedApplyInfoMapper.selectApplyInfoByCompetitionSeriesIdAndTeamCode(competitionSeriesId, teamCode);
        if (StringUtils.isBlank(applyInfo)) {
            throw new GlobalException("当前团队已报名成功或已被删除，不能进行删除操作！");
        }
        return competitionPromotedApplyInfoMapper.logicalDelCompetitionPromotedApplyInfoByTeamCodeAndCompetitionSeriesId(teamCode, competitionSeriesId, nickName);
    }

    /**
     * 更新赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfos
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitionPromotedApplyInfos(List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfos) {
        if (CollectionUtils.isEmpty(competitionPromotedApplyInfos)) {
            throw new GlobalException("赛事晋级申请报名信息不能为空");
        }
        List<PromotedPlayerInfo> updateTeamSortList = new ArrayList<>();
        List<PromotedPlayerInfo> delGuideTeacherInfoList = new ArrayList<>();
        List<PromotedPlayerInfo> updateGuideTeacherInfoList = new ArrayList<>();
        List<CompetitionPromotedApplyInfo> addGuideTeacherInfoList = new ArrayList<>();
        String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        competitionPromotedApplyInfos.forEach(promoted -> {
            // 要修改顺序的队员
            List<PromotedPlayerInfo> playerInfoList = promoted.getPlayerInfoList();
            if (CollectionUtils.isNotEmpty(playerInfoList)) {
                playerInfoList.forEach(info -> info.setUpdateBy(nickName));
                updateTeamSortList.addAll(playerInfoList);
            }
            // 一次性遍历指导老师列表，按条件分类处理
            List<PromotedPlayerInfo> guideTeacherInfoList = promoted.getGuideTeacherInfoList();
            if (CollectionUtils.isNotEmpty(guideTeacherInfoList)) {
                Long applyId = playerInfoList.get(0).getApplyId();
                CompetitionPromotedApplyInfo competitionPromotedApplyInfo = competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoByApplyId(applyId);
                guideTeacherInfoList.forEach(info -> {
                    info.setTeamCode(competitionPromotedApplyInfo.getTeamCode());
                    info.setCompetitionSeriesId(competitionPromotedApplyInfo.getCompetitionSeriesId());
                    if (StringUtils.isBlank(info.getUserName()) && info.getApplyId() != null) {
                        // 要删除的指导老师（用户名为空且有applyId）
                        delGuideTeacherInfoList.add(info);
                    } else if (StringUtils.isNotBlank(info.getUserName()) && info.getApplyId() != null) {
                        // 要修改的指导老师（用户名不为空且有applyId）
                        info.setUpdateBy(nickName);
                        updateGuideTeacherInfoList.add(info);
                    } else if (StringUtils.isNotBlank(info.getUserName()) && info.getApplyId() == null) {
                        // 要新增的指导老师（用户名不为空但无applyId）
                        CompetitionPromotedApplyInfo promotedApplyInfo = new CompetitionPromotedApplyInfo();
                        promotedApplyInfo.setSchoolName(competitionPromotedApplyInfo.getSchoolName());
                        promotedApplyInfo.setSchool(competitionPromotedApplyInfo.getSchool());
                        promotedApplyInfo.setCompetitionTrackName(competitionPromotedApplyInfo.getCompetitionTrackName());
                        promotedApplyInfo.setCompetitionTrackId(competitionPromotedApplyInfo.getCompetitionTrackId());
                        promotedApplyInfo.setCompetitionTrackType(competitionPromotedApplyInfo.getCompetitionTrackType());
                        promotedApplyInfo.setSecondLevelCode(competitionPromotedApplyInfo.getSecondLevelCode());
                        promotedApplyInfo.setSecondLevelName(competitionPromotedApplyInfo.getSecondLevelName());
                        promotedApplyInfo.setTeamCode(competitionPromotedApplyInfo.getTeamCode());
                        promotedApplyInfo.setTeamName(competitionPromotedApplyInfo.getTeamName());
                        promotedApplyInfo.setCompetitionSeriesId(competitionPromotedApplyInfo.getCompetitionSeriesId());
                        promotedApplyInfo.setCompetitionName(competitionPromotedApplyInfo.getCompetitionName());
                        promotedApplyInfo.setApplyStatus(competitionPromotedApplyInfo.getApplyStatus());
                        promotedApplyInfo.setRegistrationTime(competitionPromotedApplyInfo.getRegistrationTime());
                        promotedApplyInfo.setPayStatus(competitionPromotedApplyInfo.getPayStatus());
                        promotedApplyInfo.setInvoiceStatus(competitionPromotedApplyInfo.getInvoiceStatus());
                        promotedApplyInfo.setLeaderTeacherId(competitionPromotedApplyInfo.getLeaderTeacherId());
                        promotedApplyInfo.setCompetitionRoleName("指导教师");
                        promotedApplyInfo.setGuideTeacher(info.getUserName());
                        promotedApplyInfo.setUserName(info.getUserName());
                        promotedApplyInfo.setTeamSort(info.getTeamSort());
                        promotedApplyInfo.setCreateBy(nickName);
                        addGuideTeacherInfoList.add(promotedApplyInfo);
                    }
                });
            }
        });
        updateTeamSort(updateTeamSortList);
        delGuideTeacherInfoList(delGuideTeacherInfoList);
        updateGuideTeacherInfoList(updateGuideTeacherInfoList);
        addGuideTeacherInfoList(addGuideTeacherInfoList);
        return 1;
    }

    /**
     * C端修改赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pcUpdateCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        if (competitionPromotedApplyInfo == null || StringUtils.isBlank(competitionPromotedApplyInfo.getTeamCode()) || competitionPromotedApplyInfo.getCompetitionSeriesId() == null) {
            throw new GlobalException("参数错误！");
        }
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        boolean canModify = competitionPromotedInfoMapper.canModify(competitionPromotedApplyInfo.getTeamCode(), competitionPromotedApplyInfo.getCompetitionSeriesId(), userId);
        if (!canModify) {
            throw new GlobalException("仅可对未报名的团队且在报名时间范围内修改！");
        }
        return updateCompetitionPromotedApplyInfos(Lists.newArrayList(competitionPromotedApplyInfo));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> pcApply(Long promotedId, List<String> teamCodes) {
        Map<String, Object> result = new HashMap<>();
        if (Objects.isNull(promotedId) || CollectionUtils.isEmpty(teamCodes)) {
            throw new GlobalException("参数错误！");
        }
        CompetitionPromotedInfo competitionPromotedInfo = competitionPromotedInfoMapper.selectCompetitionPromotedInfoByPromotedId(promotedId);
        if (competitionPromotedInfo == null) {
            throw new GlobalException("赛事不存在！");
        }
        Long competitionSeriesId = competitionPromotedInfo.getCompetitionSeriesId();
        //判断当前时间是否在报名时间范围内
        if (!DateUtil.isIn(new Date(), competitionPromotedInfo.getApplyStartTime(), competitionPromotedInfo.getApplyEndTime())) {
            throw new GlobalException("当前时间不在报名时间内！");
        }
        String fee = competitionPromotedInfo.getFee();
        if (StringUtils.isNotBlank(fee) && !"0".equals(fee)) {
            //不免费前端需要跳转页面走其他接口
            result.put("free", false);
            return result;
        }
        result.put("free", true);
        List<CompetitionPromotedApplyInfo> promotedApplyInfoList = competitionPromotedApplyInfoMapper.selectPromotedPlayerAllInfoListByCompetitionSeries(competitionSeriesId, teamCodes);
        if (CollectionUtils.isNotEmpty(promotedApplyInfoList)) {
            List<CompetitionPromotedApplyInfo> list = promotedApplyInfoList.stream().filter(info -> "1".equals(info.getApplyStatus())).toList();
            if (!list.isEmpty()) {
                throw new GlobalException("存在已报名团队，请重新选择！");
            }
            List<CompetitionApplyInfo> competitionApplyInfoList = new ArrayList<>();
            String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
            promotedApplyInfoList.forEach(promoted -> {
                List<PromotedPlayerInfo> promotedPlayerInfoList = promoted.getPromotedPlayerInfoList();
                promotedPlayerInfoList.forEach(info -> {
                    CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                    BeanUtils.copyProperties(info, competitionApplyInfo);
                    competitionApplyInfo.setPayStatus(DictConstant.PAID);
                    competitionApplyInfo.setCompetitionSeriesId(promoted.getCompetitionSeriesId());
                    competitionApplyInfo.setCompetitionName(promoted.getCompetitionName());
                    competitionApplyInfo.setTeamCode(promoted.getTeamCode());
                    competitionApplyInfo.setTeamName(promoted.getTeamName());
                    competitionApplyInfo.setSchoolName(promoted.getSchoolName());
                    competitionApplyInfo.setCompetitionTrackName(promoted.getCompetitionTrackName());
                    competitionApplyInfo.setDelFlag("0");
                    competitionApplyInfo.setCreateBy(nickName);
                    competitionApplyInfo.setCreateTime(new Date());

                    competitionApplyInfoList.add(competitionApplyInfo);
                });
            });
            if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
                Lists.partition(competitionApplyInfoList, 100).forEach(subList -> {
                    //将团队信息插入到报名信息表中
                    competitionApplyInfoService.batchInsertAwardsCompetitionApplyInfo(subList, nickName, competitionSeriesId);
                });
            }
            result.put("msg", "报名成功！");
            return result;
        }
        throw new GlobalException("未查询到团队信息！");
    }

    /**
     * 更新teamSort
     *
     * @param updateTeamSortList
     */
    private void updateTeamSort(List<PromotedPlayerInfo> updateTeamSortList) {
        if (CollectionUtils.isNotEmpty(updateTeamSortList)) {
            List<List<PromotedPlayerInfo>> partition = Lists.partition(updateTeamSortList, 200);
            for (List<PromotedPlayerInfo> playerInfos : partition) {
                competitionPromotedApplyInfoMapper.updateCompetitionPromotedApplyInfos(playerInfos);
            }
        }
    }

    /**
     * 逻辑删除指导老师
     *
     * @param delGuideTeacherInfoList
     */
    private void delGuideTeacherInfoList(List<PromotedPlayerInfo> delGuideTeacherInfoList) {
        if (CollectionUtils.isNotEmpty(delGuideTeacherInfoList)) {
            Long competitionSeriesId = delGuideTeacherInfoList.get(0).getCompetitionSeriesId();
            List<Long> applyIdList = delGuideTeacherInfoList.stream().map(PromotedPlayerInfo::getApplyId).toList();
            List<String> teamCodes = delGuideTeacherInfoList.stream().map(PromotedPlayerInfo::getTeamCode).toList();
            List<CompetitionPromotedApplyInfo> oldInfoLists = competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoByTeamCodesAndSeriesId(teamCodes, competitionSeriesId);
            String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
            Lists.partition(applyIdList, 200).forEach(subList -> {
                competitionPromotedApplyInfoMapper.logicalDeleteCompetitionPromotedApplyInfoByApplyIds(subList.toArray(new Long[0]), nickName);
            });
            delGuideTeacherInfoList.forEach(e -> {
                CompetitionPromotedApplyInfo info = oldInfoLists.stream().filter(o -> e.getApplyId().equals(o.getApplyId())).findFirst().get();
                List<CompetitionPromotedApplyInfo> old = oldInfoLists.stream().filter(o -> e.getTeamCode().equals(o.getTeamCode()) && competitionSeriesId.equals(o.getCompetitionSeriesId())).toList();
                String details = "申请了指导教师进行了“信息调整”，数据变动类型为“指导教师信息调整”，删除指导教师姓名数据为" + info.getUserName() + "，变动详情为：（" + info.getUserName() + "姓名指导教师删除），结果为成功";
                List<CompetitionPromotedApplyInfo> newInfo = old.stream().filter(o -> !e.getApplyId().equals(o.getApplyId())).toList();
                insertChangeLog(e.getTeamCode(), e.getApplyId(), newInfo, old, details);
            });
        }
    }

    /**
     * 更新指导老师名称
     *
     * @param updateGuideTeacherInfoList
     */
    private void updateGuideTeacherInfoList(List<PromotedPlayerInfo> updateGuideTeacherInfoList) {
        if (CollectionUtils.isNotEmpty(updateGuideTeacherInfoList)) {
            Long competitionSeriesId = updateGuideTeacherInfoList.get(0).getCompetitionSeriesId();
            List<String> teamCodes = updateGuideTeacherInfoList.stream().map(PromotedPlayerInfo::getTeamCode).toList();
            List<CompetitionPromotedApplyInfo> oldInfoLists = competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoByTeamCodesAndSeriesId(teamCodes, competitionSeriesId);
            List<List<PromotedPlayerInfo>> partition = Lists.partition(updateGuideTeacherInfoList, 200);
            for (List<PromotedPlayerInfo> playerInfos : partition) {
                competitionPromotedApplyInfoMapper.updateCompetitionPromotedApplyInfoGuideTeacher(playerInfos);
            }
            updateGuideTeacherInfoList.forEach(e -> {
                CompetitionPromotedApplyInfo info = oldInfoLists.stream().filter(o -> e.getApplyId().equals(o.getApplyId())).findFirst().get();
                if (e.getUserName().equals(info.getUserName())) {
                    return;
                }
                List<CompetitionPromotedApplyInfo> old = oldInfoLists.stream().filter(o -> e.getTeamCode().equals(o.getTeamCode()) && competitionSeriesId.equals(o.getCompetitionSeriesId())).toList();
                String details = "申请了指导教师进行了“信息调整”，数据变动类型为“指导教师信息调整”，更前指导教师姓名数据为:" + info.getUserName() + ",变更后指导教师姓名数据为:" + e.getUserName() + ",变动详情为：（" + e.getUserName() + "指导教师姓名变更），结果为成功";
                List<CompetitionPromotedApplyInfo> newInfos = new ArrayList<>(old.stream().filter(o -> !e.getApplyId().equals(o.getApplyId())).toList());
                CompetitionPromotedApplyInfo newInfo = new CompetitionPromotedApplyInfo();
                BeanUtils.copyProperties(info, newInfo);
                newInfo.setUserName(e.getUserName());
                newInfos.add(newInfo);
                insertChangeLog(e.getTeamCode(), e.getApplyId(), newInfos, old, details);
            });
        }
    }

    /**
     * 新增指导老师
     *
     * @param addGuideTeacherInfoList
     */
    private void addGuideTeacherInfoList(List<CompetitionPromotedApplyInfo> addGuideTeacherInfoList) {
        if (CollectionUtils.isNotEmpty(addGuideTeacherInfoList)) {
            Long competitionSeriesId = addGuideTeacherInfoList.get(0).getCompetitionSeriesId();
            List<String> teamCodes = addGuideTeacherInfoList.stream().map(CompetitionPromotedApplyInfo::getTeamCode).toList();
            List<CompetitionPromotedApplyInfo> oldInfoLists = competitionPromotedApplyInfoMapper.selectCompetitionPromotedApplyInfoByTeamCodesAndSeriesId(teamCodes, competitionSeriesId);
            List<List<CompetitionPromotedApplyInfo>> partition = Lists.partition(addGuideTeacherInfoList, 200);
            for (List<CompetitionPromotedApplyInfo> promotedApplyInfos : partition) {
                competitionPromotedApplyInfoMapper.insertCompetitionPromotedApplyInfos(promotedApplyInfos);
            }
            addGuideTeacherInfoList.forEach(e -> {
                List<CompetitionPromotedApplyInfo> old = oldInfoLists.stream().filter(o -> e.getTeamCode().equals(o.getTeamCode()) && competitionSeriesId.equals(o.getCompetitionSeriesId())).toList();
                String details = "申请了指导教师进行了“信息调整”，数据变动类型为“指导教师信息调整”，新增指导教师姓名数据为" + e.getUserName() + "，变动详情为：（" + e.getUserName() + "姓名指导教师新增），结果为成功";
                List<CompetitionPromotedApplyInfo> newInfo = new ArrayList<>(old);
                newInfo.add(e);
                insertChangeLog(e.getTeamCode(), e.getApplyId(), newInfo, old, details);
            });
        }
    }

    /**
     * 插入变更日志
     *
     * @param newInfo
     * @param oldInfo
     * @param details
     */
    private void insertChangeLog(String teamCode, Long memberId,
                                 List<CompetitionPromotedApplyInfo> newInfo,
                                 List<CompetitionPromotedApplyInfo> oldInfo,
                                 String details) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long userId = sysUser.getUserId();
        String userName = sysUser.getNickName();
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ApplyConstants.OPERATION_CHANGE_TEACHER_PROMOTED);
        changeLog.setMemberId(memberId);
        changeLog.setTeamId(teamCode);
        changeLog.setNewData(JSONObject.toJSONString(newInfo));
        changeLog.setOldData(JSONObject.toJSONString(oldInfo));
        changeLog.setChangeDetails(userName + details);
        changeLog.setChangeTime(DateUtils.getNowDate());
        changeLog.setIpAddress(ServletUtils.getRequest().getRemoteAddr());
        changeLog.setResult("成功");
        changeLog.setOperatorUserId(userId);
        changeLog.setCreateBy(userName);
        changeLogService.insertChangeLog(changeLog);
    }
}
