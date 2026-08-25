package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.collect.Iterables;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Sets;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.ServletUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPlayerInfo;
import com.teaching.competition.mapper.AwardDetailsMapper;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.TeamMemberRelaMapper;
import com.teaching.competition.service.IAwardDetailsService;
import com.teaching.competition.service.IChangeLogService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

/**
 * 获奖公示明细Service业务层处理
 *
 * @author teaching
 * @date 2026-05-12
 */
@Service
public class AwardDetailsServiceImpl implements IAwardDetailsService {
    @Autowired
    private AwardDetailsMapper awardDetailsMapper;
    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private IChangeLogService changeLogService;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private TeamMemberRelaMapper teamMemberRelaMapper;

    /**
     * 查询获奖公示明细
     *
     * @param id 获奖公示明细主键
     * @return 获奖公示明细
     */
    @Override
    public AwardDetails selectAwardDetailsById(Long id) {
        return awardDetailsMapper.selectAwardDetailsById(id);
    }

    /**
     * 查询获奖公示明细列表
     *
     * @param awardDetails 获奖公示明细
     * @return 获奖公示明细
     */
    @Override
    public List<AwardDetails> selectAwardDetailsList(AwardDetails awardDetails) {
        return awardDetailsMapper.selectAwardDetailsList(awardDetails);
    }

    @Override
    public List<AwardDetails> selectAwardDetailsCompetitionApplyInfoList(AwardDetails awardDetails) {
        List<AwardDetails> awardDetailsList = awardDetailsMapper.selectAwardDetailsCompetitionApplyInfoList(awardDetails);
        if(CollectionUtils.isNotEmpty(awardDetailsList)){
            awardDetailsList.stream().forEach(awardDetailsRes -> {
                // 根据school作为id查询省份
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(awardDetailsRes.getSchoolId())) {
                    R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(awardDetailsRes.getSchoolId(), SecurityConstants.INNER);
                    if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                        NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                        awardDetailsRes.setSchoolName(collegeInfo.getSchoolName());
                    }
                }
            });
        }
        return awardDetailsList;
    }

    @Override
    public Integer selectAwardDetailsSum(Long awardPublicityId) {
        return awardDetailsMapper.selectAwardDetailsSum(awardPublicityId);
    }

    /**
     * 新增获奖公示明细
     *
     * @param awardDetails 获奖公示明细
     * @return 结果
     */
    @Override
    public int insertAwardDetails(AwardDetails awardDetails) {
        awardDetails.setCreateTime(DateUtils.getNowDate());
        return awardDetailsMapper.insertAwardDetails(awardDetails);
    }

    /**
     * 批量新增获奖公示明细
     *
     * @param detailList
     */
    @Override
    public Map<String,Object> insertAwardDetailsBatch(List<AwardDetails> detailList) {
        Map<String,Object> result = new HashMap<>();
        if(CollectionUtils.isEmpty(detailList)){
            result.put("importSuccess", false);
            result.put("msg", "导入数据为空");
            return result;
        }
        List<List<AwardDetails>> partition = Lists.partition(detailList, 400);
        Long insertedCount = 0L;
        List<AwardDetails> waitInsert ;
        Set<String> failTeamCodes = new HashSet<>();
        for (List<AwardDetails> awardDetails : partition) {
            waitInsert = new ArrayList<>();
            //根据teamCode查学校名称及团队名称
            //从awardDetails中获取teamCode并去重
            Set<String> teamCodeSet = awardDetails.stream()
                    .map(AwardDetails::getTeamCode)
                    .collect(Collectors.toSet());
            List<Map<String, String>> maps = competitionApplyInfoMapper.selectTeamNameAndSchoolNameByTeamCodes(teamCodeSet);
            if(CollectionUtils.isEmpty(maps)){
                failTeamCodes.addAll(teamCodeSet);
                continue;
            }
           //将maps转换成key是teamCode，value是Map<String,String>的Map
            Map<String, Map<String, String>> teamCodeToName = maps.stream()
                    .collect(Collectors.toMap(map -> map.get("teamCode"), map -> map));
            //将awardDetails中的teamCode替换为学校名称及团队名称
            for (AwardDetails detail : awardDetails) {
                if(teamCodeToName.containsKey(detail.getTeamCode())){
                    Map<String, String> nameMap = teamCodeToName.get(detail.getTeamCode());
                    detail.setSchoolName(nameMap.get("schoolName"));
                    detail.setTeamName(nameMap.get("teamName"));
                    waitInsert.add(detail);
                }else{
                    failTeamCodes.add(detail.getTeamCode());
                }
            }
            awardDetailsMapper.insertAwardDetailsBatch(waitInsert);
            insertedCount += waitInsert.size();
        }
        result.put("importSuccess", true);
        result.put("msg", "导入成功");
        result.put("successCount", insertedCount);
        result.put("failTeamCodes", failTeamCodes);
        return result;
    }

    /**
     * 根据teamCode批量删除获奖公示明细
     *
     * @param teamCodes
     */
    @Override
    public void batchLogicDeleteByTeamCodes(Set<String> teamCodes,Long awardPublicityId) {
        if(CollectionUtils.isEmpty(teamCodes)){
            return;
        }
        String username = SecurityUtils.getLoginUser().getUsername();
        Iterable<List<String>> partition = Iterables.partition(teamCodes, 200);
        for (List<String> teamCode : partition) {
            awardDetailsMapper.batchLogicDeleteByTeamCodes(teamCode, username,awardPublicityId);
        }
    }

    /**
     * 修改获奖公示明细
     *
     * @param awardDetailsList 获奖公示明细
     * @return 结果
     */
    @Override
    @Transactional
    public int updateAwardDetails(List<AwardDetails> awardDetailsList) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        String userName;
        if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData().getAuthInfo())) {
            userName = userCenterInfoLogin.getData().getAuthInfo().getRealName();
        } else {
            userName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        }
        // 报名信息更新
        List<CompetitionApplyInfo> competitionApplyInfoList = new ArrayList<>();
        List<CompetitionApplyInfo> addCompetitionApplyInfoList = new ArrayList<>();
        awardDetailsList.stream().forEach(awardDetails -> {
            if(!CollectionUtils.isEmpty(awardDetails.getPlayerList())){
                awardDetails.getPlayerList().stream().forEach(player -> {
                    CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                    competitionApplyInfo.setMemberId(player.getMemberId());
                    competitionApplyInfo.setTeamSort(player.getTeamSort());
                    competitionApplyInfo.setUpdateBy(userName);
                    competitionApplyInfoList.add(competitionApplyInfo);
                });
            }
            if(!CollectionUtils.isEmpty(awardDetails.getGuiderTeacherList())){
                List<CompetitionApplyInfo> newApplyInfoList = new ArrayList<>();
                // 根据teamCode查询旧报名信息
                List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(awardDetails.getTeamCode());
                if(CollectionUtils.isNotEmpty(applyInfoList)){
                    List<CompetitionApplyInfo> oldApplyInfoListForLog = applyInfoList.stream()
                            .map(info -> {
                                CompetitionApplyInfo copy = new CompetitionApplyInfo();
                                BeanUtils.copyProperties(info, copy);
                                return copy;
                            }).toList();
                    newApplyInfoList.addAll(oldApplyInfoListForLog);
                }
                awardDetails.getGuiderTeacherList().stream().forEach(guiderTeacher -> {
                    CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                    if(Objects.nonNull(guiderTeacher.getMemberId()) && !StringUtils.isEmpty(guiderTeacher.getUserName())){
                        // 根据memberId查询报名信息
                        Map<Long,CompetitionApplyInfo> applyInfoMap = applyInfoList.stream().collect(Collectors.toMap(CompetitionApplyInfo::getMemberId,applyInfo -> applyInfo));
                        CompetitionApplyInfo guideTeacherInfo = applyInfoMap.get(guiderTeacher.getMemberId());
                        if(Objects.nonNull(guideTeacherInfo) && !guideTeacherInfo.getUserName().equals(guiderTeacher.getUserName())){
                            String details = userName + "申请了" + guideTeacherInfo.getCompetitionRoleName() + "进行了“信息调整”，数据变动类型为“" + guideTeacherInfo.getCompetitionRoleName() + "信息调整”，变更前指导教师姓名数据为:"+ guideTeacherInfo.getUserName()+",变更后指导教师姓名数据为:" + guiderTeacher.getUserName() +
                                    "，变动详情为：（" + guiderTeacher.getUserName() + "指导教师姓名变更），结果为成功";
                            BeanUtils.copyProperties(guideTeacherInfo, competitionApplyInfo);
                            competitionApplyInfo.setUserName(guiderTeacher.getUserName());
                            competitionApplyInfo.setPhone(guiderTeacher.getPhone());
                            competitionApplyInfo.setEmail(guiderTeacher.getEmail());
                            competitionApplyInfo.setGuideTeacher(guiderTeacher.getUserName());
                            competitionApplyInfo.setGuideTeacherPhone(guiderTeacher.getPhone());
                            competitionApplyInfo.setGuideTeacherEmail(guiderTeacher.getEmail());
                            competitionApplyInfo.setUpdateBy(userName);
                            competitionApplyInfoList.add(competitionApplyInfo);
                            newApplyInfoList.stream().forEach(applyInfo -> {
                                if(applyInfo.getMemberId().equals(guiderTeacher.getMemberId())){
                                    applyInfo.setUserName(guiderTeacher.getUserName());
                                    applyInfo.setGuideTeacher(guiderTeacher.getUserName());
                                }
                            });
                            insertChangeLog(guiderTeacher, competitionApplyInfo, newApplyInfoList, applyInfoList, userId, userName,details);
                        }
                        // 更新入团队团员关联关联关系表
                        Map<String,Object> teamMemberRelaMap = new HashMap<>();
                        teamMemberRelaMap.put("teamCode",awardDetails.getTeamCode());
                        teamMemberRelaMap.put("userName",guideTeacherInfo.getUserName());
                        teamMemberRelaMap.put("userNameNew",guiderTeacher.getUserName());
                        teamMemberRelaMap.put("instructorNew",guiderTeacher.getUserName());
                        teamMemberRelaMap.put("instructorPhoneNew",guiderTeacher.getPhone());
                        teamMemberRelaMap.put("instructorEmailNew",guiderTeacher.getEmail());
                        teamMemberRelaMapper.updateTeamMemberRelaByUserName(teamMemberRelaMap);
                    } else if(Objects.isNull(guiderTeacher.getMemberId()) && StringUtils.isNotEmpty(guiderTeacher.getUserName())){
                        BeanUtils.copyProperties(awardDetails, competitionApplyInfo);
                        competitionApplyInfo.setUserName(guiderTeacher.getUserName());
                        competitionApplyInfo.setSchool(awardDetails.getSchoolId());
                        competitionApplyInfo.setPhone(guiderTeacher.getPhone());
                        competitionApplyInfo.setEmail(guiderTeacher.getEmail());
                        competitionApplyInfo.setGuideTeacher(guiderTeacher.getUserName());
                        competitionApplyInfo.setGuideTeacherPhone(guiderTeacher.getPhone());
                        competitionApplyInfo.setGuideTeacherEmail(guiderTeacher.getEmail());
                        competitionApplyInfo.setPayStatus(DictConstant.PAID);
                        competitionApplyInfo.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
                        competitionApplyInfo.setCheckStatus("4");
                        competitionApplyInfo.setCompetitionTrackType("1");
                        if(CollectionUtils.isNotEmpty(applyInfoList)){
                            competitionApplyInfo.setLeaderTeacherId(applyInfoList.get(0).getLeaderTeacherId());
                            competitionApplyInfo.setLeaderTeacherPhone(applyInfoList.get(0).getLeaderTeacherPhone());
                            competitionApplyInfo.setLeaderTeacher(applyInfoList.get(0).getLeaderTeacher());
                        }
                        competitionApplyInfo.setCreateBy(userName);
                        competitionApplyInfo.setTeamSort(guiderTeacher.getTeamSort());
                        addCompetitionApplyInfoList.add(competitionApplyInfo);
                        newApplyInfoList.add(competitionApplyInfo);
                        // 新增报名信息记录变更日志
                        String details = userName + "申请了" + competitionApplyInfo.getCompetitionRoleName() + "进行了“信息调整”，数据变动类型为“" + competitionApplyInfo.getCompetitionRoleName() + "信息调整”，新增指导教师姓名数据为" + guiderTeacher.getUserName() +
                                "，变动详情为：（" +guiderTeacher.getUserName() + "姓名指导教师新增），结果为成功";
                        insertChangeLog(guiderTeacher, competitionApplyInfo, newApplyInfoList, applyInfoList, userId, userName,details);
                    } else if(Objects.nonNull(guiderTeacher.getMemberId()) && StringUtils.isEmpty(guiderTeacher.getUserName())){
                        // 如果修改指导教师名称为空则删除指导教师  段飞虎
                        newApplyInfoList.clear();
                        applyInfoList.stream().forEach(applyInfo -> {
                            if(applyInfo.getMemberId().equals(guiderTeacher.getMemberId())){
                                competitionApplyInfo.setMemberId(applyInfo.getMemberId());
                                competitionApplyInfo.setTeamCode(applyInfo.getTeamCode());
                            } else {
                                newApplyInfoList.add(applyInfo);
                            }
                        });
                        Map<Long,CompetitionApplyInfo> applyInfoMap = applyInfoList.stream().collect(Collectors.toMap(CompetitionApplyInfo::getMemberId,applyInfo -> applyInfo));
                        CompetitionApplyInfo guideTeacherInfo = applyInfoMap.get(guiderTeacher.getMemberId());
                        if(Objects.nonNull(guideTeacherInfo)){
                            String details = userName + "申请了指导教师进行了“信息调整”，数据变动类型为“指导教师信息调整”，删除指导教师姓名数据为" + guideTeacherInfo.getUserName() +
                                    "，变动详情为：（" +guideTeacherInfo.getUserName() + "姓名指导教师删除），结果为成功";
                            insertChangeLog(guiderTeacher, competitionApplyInfo, newApplyInfoList, applyInfoList, userId, userName,details);
                            competitionApplyInfoMapper.deleteCompetitionApplyInfoByMemberId(guiderTeacher.getMemberId());
                            teamMemberRelaMapper.deleteTeamMemberRelaByTeamCodeAndUserName(awardDetails.getTeamCode(),guiderTeacher.getUserName());
                        }
                    }
                });
            }
        });
        if(CollectionUtils.isNotEmpty(addCompetitionApplyInfoList)){
            competitionApplyInfoMapper.batchInsertCompetitionApplyInfo(addCompetitionApplyInfoList);
            // 新增入团队团员关联关联关系表
            addCompetitionApplyInfoList.stream().forEach(competitionApplyInfo -> {
                TeamMemberRela teamMemberRela = new TeamMemberRela();
                teamMemberRela.setTeamCode(competitionApplyInfo.getTeamCode());
                teamMemberRela.setTeamRole(competitionApplyInfo.getCompetitionRoleName());
                teamMemberRela.setUserName(competitionApplyInfo.getUserName());
                teamMemberRela.setCreateTime(DateUtils.getNowDate());
                teamMemberRela.setInstructor(competitionApplyInfo.getGuideTeacher());
                teamMemberRela.setInstructorPhone(competitionApplyInfo.getGuideTeacherPhone());
                teamMemberRela.setInstructorEmail(competitionApplyInfo.getGuideTeacherEmail());
                teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
            });
        }
        if(CollectionUtils.isNotEmpty(competitionApplyInfoList)){
            competitionApplyInfoMapper.batchUpdateCompetitionApplyInfo(competitionApplyInfoList);
        }
        return awardDetailsMapper.batchUpdateAwardDetails(awardDetailsList);
    }

    private void insertChangeLog(AwardPlayerInfo guiderTeacher,
                           CompetitionApplyInfo competitionApplyInfo,
                           List<CompetitionApplyInfo> newApplyInfoList,
                           List<CompetitionApplyInfo> applyInfoList,
                           Long userId, String userName,
                           String details) {
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ApplyConstants.OPERATION_CHANGE_TEACHER_AWARDS);
        changeLog.setMemberId(competitionApplyInfo.getMemberId());
        changeLog.setTeamId(competitionApplyInfo.getTeamCode());
        changeLog.setNewData(JSONObject.toJSONString(newApplyInfoList));
        changeLog.setOldData(JSONObject.toJSONString(applyInfoList));
        changeLog.setChangeTime(DateUtils.getNowDate());
        changeLog.setIpAddress(ServletUtils.getRequest().getRemoteAddr());
        changeLog.setOperatorUserId(userId);
        changeLog.setResult("成功");
        changeLog.setChangeDetails(details);
        changeLog.setCreateBy(userName);
        changeLogService.insertChangeLog(changeLog);
    }

    /**
     * 批量删除获奖公示明细
     *
     * @param ids 需要删除的获奖公示明细主键
     * @return 结果
     */
    @Override
    public int deleteAwardDetailsByIds(Long[] ids) {
        return awardDetailsMapper.deleteAwardDetailsByIds(ids);
    }

    /**
     * 根据获奖公示ID批量逻辑删除获奖公示明细
     * @param awardPublicityIds
     * @param updateBy
     * @return
     */
    @Override
    public int batchLogicDeleteByAwardPublicityId(Long[] awardPublicityIds,String updateBy) {
        return awardDetailsMapper.batchLogicDeleteByAwardPublicityId(awardPublicityIds,updateBy);
    }

    /**
     * 删除获奖公示明细信息
     *
     * @param id 获奖公示明细主键
     * @return 结果
     */
    @Override
    public int deleteAwardDetailsById(Long id) {
        return awardDetailsMapper.deleteAwardDetailsById(id);
    }
}
