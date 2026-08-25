package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.domain.OperationTimes;
import com.teaching.competition.domain.UserCompetitionApplyInfoDTO;
import com.teaching.competition.mapper.OperationConfigMapper;
import com.teaching.competition.mapper.OperationTimesMapper;
import com.teaching.competition.service.IOperationTimesService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.OperationConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 队伍操作次数Service业务层处理
 *
 * @author teaching
 * @date 2026-01-24
 */
@Service
public class OperationTimesServiceImpl implements IOperationTimesService {
    private static final Logger log = LoggerFactory.getLogger(OperationTimesServiceImpl.class);
    @Autowired
    private OperationTimesMapper operationTimesMapper;
    @Autowired
    private OperationConfigMapper operationConfigMapper;

    /**
     * 查询队伍操作次数
     *
     * @param id 队伍操作次数主键
     * @return 队伍操作次数
     */
    @Override
    public OperationTimes selectOperationTimesById(Long id) {
        return operationTimesMapper.selectOperationTimesById(id);
    }

    /**
     * 查询队伍操作次数列表
     *
     * @param operationTimes 队伍操作次数
     * @return 队伍操作次数
     */
    @Override
    public List<OperationTimes> selectOperationTimesList(OperationTimes operationTimes) {
        return operationTimesMapper.selectOperationTimesList(operationTimes);
    }

    /**
     * 新增队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    @Override
    public int insertOperationTimes(OperationTimes operationTimes) {
        operationTimes.setCreateTime(DateUtils.getNowDate());
        return operationTimesMapper.insertOperationTimes(operationTimes);
    }

    /**
     * 修改队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    @Override
    public int updateOperationTimes(OperationTimes operationTimes) {
        operationTimes.setUpdateTime(DateUtils.getNowDate());
        return operationTimesMapper.updateOperationTimes(operationTimes);
    }

    /**
     * 批量删除队伍操作次数
     *
     * @param ids 需要删除的队伍操作次数主键
     * @return 结果
     */
    @Override
    public int deleteOperationTimesByIds(Long[] ids) {
        return operationTimesMapper.deleteOperationTimesByIds(ids);
    }

    @Override
    public int cancelRepaymentOperationTimes(String teamCode) {
        // 如果退费重缴次数为1则直接删除，不为1则减1
        OperationTimes repayment = operationTimesMapper.selectOperationTimesByTeamCodeAndOperationType(teamCode, "repayment");
        if (Objects.nonNull(repayment) && repayment.getUsedTimes() > 1) {
            repayment.setUsedTimes(repayment.getUsedTimes() - 1);
            return operationTimesMapper.updateOperationTimes(repayment);
        }
        if (Objects.nonNull(repayment) && repayment.getUsedTimes() <= 1) {
            return operationTimesMapper.deleteOperationTimesById(repayment.getId());
        }
        return 1;
    }

    /**
     * 删除队伍操作次数信息
     *
     * @param id 队伍操作次数主键
     * @return 结果
     */
    @Override
    public int deleteOperationTimesById(Long id) {
        return operationTimesMapper.deleteOperationTimesById(id);
    }

    /**
     * 1.查询详情时计算并设置剩余次数
     *
     * @param applyInfoDTO 一个团队信息
     */
    @Override
    public void calculateRemainingTimes(UserCompetitionApplyInfoDTO applyInfoDTO) {
        OperationConfig operationConfig = new OperationConfig(applyInfoDTO.getCompetitionSeriesId());
        //获取配置信息列表
        List<OperationConfig> operationConfigs = operationConfigMapper.selectOperationConfigList(operationConfig);
        if (CollectionUtils.isNotEmpty(operationConfigs)) {
            //operationConfigs根据operationType分组 配置类型：配置信息（每个配置只有一条）
            Map<String, List<OperationConfig>> collect = operationConfigs.stream().collect(Collectors.groupingBy(OperationConfig::getOperationType));
            for (Map.Entry<String, List<OperationConfig>> entry : collect.entrySet()) {
                //配置类型 1:参赛信息修改 2:指导教师修改 3:退费重缴 4:退赛申请
                String key = entry.getKey();
                List<OperationConfig> value = entry.getValue();
                if (CollectionUtils.isNotEmpty(value)) {
                    OperationConfig config = value.get(0);
                    // -1不限制 0不让修改 没用配置信息默认是-1不限制
                    long maxTimes = config.getMaxTimes() == null ? -1L : config.getMaxTimes();
                    if (maxTimes == -1L || maxTimes == 0L) {
                        setEasyCountNum(applyInfoDTO, maxTimes, null);
                    }
                    if (maxTimes > 0L) {
                        //根据配置id查询操作次数  同一个配置id对应多个操作次数记录（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
                        List<OperationTimes> operationTimes = operationTimesMapper.selectOperationTimesByConfigId(config.getId(), applyInfoDTO.getTeamCode());
                        if (CollectionUtils.isNotEmpty(operationTimes)) {
                            //根据操作类型分组 操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
                            Map<String, List<OperationTimes>> operationTimesMap = operationTimes.stream().collect(Collectors.groupingBy(OperationTimes::getOperationType));
                            //配置类型1:参赛信息修改(修改组别，修改队员信息，队员人员变更) 2:指导教师修改(修改指导老师信息，指导老师人员变更) 3:退费重缴(申请退费重缴) 4:退赛申请(退赛申请)
                            List<OperationTimes> groupTimes = operationTimesMap.get("group");
                            List<OperationTimes> changeTimes = operationTimesMap.get("change");
                            List<OperationTimes> changeTeacherTimes = operationTimesMap.get("changeTeacher");
                            List<OperationTimes> infoTimes = operationTimesMap.get("info");
                            List<OperationTimes> repaymentTimes = operationTimesMap.get("repayment");
                            switch (key) {
                                case "1":
                                    //更换组别剩余次数
                                    long groupUsed = CollectionUtils.isNotEmpty(groupTimes) ? groupTimes.get(0).getUsedTimes() : 0L;
                                    applyInfoDTO.setSecondLevelOperateCount((maxTimes < groupUsed) ? 0L : (maxTimes - groupUsed));
                                    //队员人员变更剩余次数
                                    long changeUsed = CollectionUtils.isNotEmpty(changeTimes) ? changeTimes.get(0).getUsedTimes() : 0L;
                                    applyInfoDTO.setMemberOperateCount((maxTimes < changeUsed) ? 0L : (maxTimes - changeUsed));
                                    //队员信息
                                    List<CompetitionApplyInfo> competitionApplyInfoList = applyInfoDTO.getCompetitionApplyInfoList();
                                    setApplyInfoListTimes(maxTimes, infoTimes, competitionApplyInfoList);
                                    break;
                                case "2":
                                    //指导老师人员变更剩余次数
                                    long teacherUsed = CollectionUtils.isNotEmpty(changeTeacherTimes) ? changeTeacherTimes.get(0).getUsedTimes() : 0L;
                                    applyInfoDTO.setGuideTeacherOperateCount((maxTimes < teacherUsed) ? 0L : (maxTimes - teacherUsed));
                                    //指导老师信息
                                    List<CompetitionApplyInfo> guideTeacherApplyInfoList = applyInfoDTO.getGuideTeacherApplyInfoList();
                                    setApplyInfoListTimes(maxTimes, infoTimes, guideTeacherApplyInfoList);
                                    break;
                                case "3":
                                    //退费重缴
                                    long repayUsed = CollectionUtils.isNotEmpty(repaymentTimes) ? repaymentTimes.get(0).getUsedTimes() : 0L;
                                    applyInfoDTO.setRepaymentOperateCount((maxTimes < repayUsed) ? 0L : (maxTimes - repayUsed));
                                    break;
                                case "4":
                                    //退赛
                                    break;
                            }
                        } else {
                            setEasyCountNum(applyInfoDTO, maxTimes, key);
                        }
                    }
                }
            }
        } else {
            setEasyCountNum(applyInfoDTO, -1L, null);
        }
    }

    /**
     * 校验参赛信息列表剩余次数
     *
     * @param applyInfoDTO   参赛信息
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更）
     */
    @Override
    public Boolean checkTeamMembersTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes) {
        return checkTimes(applyInfoDTO, operationTypes, "1");
    }

    /**
     * 校验指导老师信息修改剩余次数
     *
     * @param applyInfoDTO   指导老师信息
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
     * @return
     */
    @Override
    public Boolean checkTeacherTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes) {
        return checkTimes(applyInfoDTO, operationTypes, "2");
    }

    /**
     * 校验退费重缴剩余次数
     *
     * @param applyInfoDTO   参赛信息主要是teamCoe
     * @param operationTypes 退费重缴可以不传
     * @return
     */
    @Override
    public Boolean checkRepaymentTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes) {
        return checkTimes(applyInfoDTO, operationTypes, "3");
    }

    /**
     * 可以提交时记录已使用次数
     *
     * @param applyInfoDTO 参赛信息列表
     * @param operationMap 传入真实的操作类型operationType（group:更换组别，info:修改信息，change队员人员变更，changeTeacher指导教师人员变更，repayment退费重缴费）和对应的configId
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int recordUsedTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<Map<String, String>> operationMap) {
        if (CollectionUtils.isEmpty(operationMap) || applyInfoDTO == null) {
            throw new ServiceException("参数有误");
        }
        String teamCode = applyInfoDTO.getTeamCode();
        List<OperationTimes> operationTimes = operationTimesMapper.selectOperationTimesByTeamCode(teamCode);
        operationMap.forEach(operation -> {
            String operationType = MapUtils.getString(operation, "operationType");
            Long configId = MapUtils.getLong(operation, "configId");
            switch (operationType) {
                case "repayment":
                    //退费重缴
                    OperationTimes repayment = Optional.ofNullable(operationTimes)
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(optionTimes -> "repayment".equals(optionTimes.getOperationType())).findFirst().orElse(null);
                    if (repayment != null) {
                        repayment.setUsedTimes(repayment.getUsedTimes() + 1);
                        repayment.setLastOperationTime(DateUtils.getNowDate());
                        operationTimesMapper.updateOperationTimes(repayment);
                    } else {
                        repayment = new OperationTimes();
                        repayment.setConfigId(configId);
                        repayment.setTeamCode(teamCode);
                        repayment.setOperationType("repayment");
                        repayment.setLastOperationTime(DateUtils.getNowDate());
                        repayment.setUsedTimes(1L);
                        operationTimesMapper.insertOperationTimes(repayment);
                    }
                    break;
                case "group":
                    //更换组别
                    OperationTimes group = Optional.ofNullable(operationTimes)
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(optionTimes -> "group".equals(optionTimes.getOperationType())).findFirst().orElse(null);
                    if (group != null) {
                        group.setUsedTimes(group.getUsedTimes() + 1);
                        group.setLastOperationTime(DateUtils.getNowDate());
                        operationTimesMapper.updateOperationTimes(group);
                    } else {
                        group = new OperationTimes();
                        group.setConfigId(configId);
                        group.setTeamCode(teamCode);
                        group.setOperationType("group");
                        group.setLastOperationTime(DateUtils.getNowDate());
                        group.setUsedTimes(1L);
                        operationTimesMapper.insertOperationTimes(group);
                    }
                    break;
                case "change":
                    //人员变更
                    OperationTimes change = Optional.ofNullable(operationTimes)
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(optionTimes -> "change".equals(optionTimes.getOperationType())).findFirst().orElse(null);
                    if (change != null) {
                        change.setUsedTimes(change.getUsedTimes() + 1);
                        change.setLastOperationTime(DateUtils.getNowDate());
                        operationTimesMapper.updateOperationTimes(change);
                    } else {
                        change = new OperationTimes();
                        change.setConfigId(configId);
                        change.setTeamCode(teamCode);
                        change.setOperationType("change");
                        change.setLastOperationTime(DateUtils.getNowDate());
                        change.setUsedTimes(1L);
                        operationTimesMapper.insertOperationTimes(change);
                    }
                    break;
                case "changeTeacher":
                    //人员变更
                    OperationTimes changeTeacher = Optional.ofNullable(operationTimes)
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(optionTimes -> "changeTeacher".equals(optionTimes.getOperationType())).findFirst().orElse(null);
                    if (changeTeacher != null) {
                        changeTeacher.setUsedTimes(changeTeacher.getUsedTimes() + 1);
                        changeTeacher.setLastOperationTime(DateUtils.getNowDate());
                        operationTimesMapper.updateOperationTimes(changeTeacher);
                    } else {
                        changeTeacher = new OperationTimes();
                        changeTeacher.setConfigId(configId);
                        changeTeacher.setTeamCode(teamCode);
                        changeTeacher.setOperationType("changeTeacher");
                        changeTeacher.setLastOperationTime(DateUtils.getNowDate());
                        changeTeacher.setUsedTimes(1L);
                        operationTimesMapper.insertOperationTimes(changeTeacher);
                    }
                    break;
                case "info":
                    // 修改信息时传值只给需要修改的记录
                    //参赛队员
                    List<CompetitionApplyInfo> competitionApplyInfoList = applyInfoDTO.getCompetitionApplyInfoList();
                    updateInfoTimes(teamCode, operationTimes, configId, competitionApplyInfoList);
                    //指导老师
                    List<CompetitionApplyInfo> guideTeacherApplyInfoList = applyInfoDTO.getGuideTeacherApplyInfoList();
                    updateInfoTimes(teamCode, operationTimes, configId, guideTeacherApplyInfoList);
                    break;
            }
        });
        return 1;
    }

    /**
     * 修改信息时记录已使用次数
     *
     * @param teamCode
     * @param operationTimes
     * @param configId
     * @param competitionApplyInfoList
     */
    private void updateInfoTimes(String teamCode, List<OperationTimes> operationTimes, Long configId, List<CompetitionApplyInfo> competitionApplyInfoList) {
        if (CollectionUtils.isNotEmpty(competitionApplyInfoList)) {
            competitionApplyInfoList.forEach(competitionApplyInfo -> {
                Long memberId = competitionApplyInfo.getMemberId();
                //修改信息
                OperationTimes info = Optional.ofNullable(operationTimes)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(optionTimes -> memberId.equals(optionTimes.getMemberId())).findFirst().orElse(null);
                if (info != null) {
                    info.setUsedTimes(info.getUsedTimes() + 1);
                    info.setLastOperationTime(DateUtils.getNowDate());
                    operationTimesMapper.updateOperationTimes(info);
                } else {
                    info = new OperationTimes();
                    info.setMemberId(memberId);
                    info.setConfigId(configId);
                    info.setTeamCode(teamCode);
                    info.setOperationType("info");
                    info.setLastOperationTime(DateUtils.getNowDate());
                    info.setUsedTimes(1L);
                    operationTimesMapper.insertOperationTimes(info);
                }
            });
        }
    }

    /**
     * 校验参赛信息列表剩余次数
     *
     * @param applyInfoDTO   参赛信息
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
     * @param operationType  传入真实的配置类型（1:参赛信息修改 2:指导教师修改 3:退费重缴 4:退赛申请）
     */
    private Boolean checkTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes, String
            operationType) {
        //1:参赛信息修改 2:指导教师修改 3:退费重缴 4:退赛申请
        OperationConfig operationConfig = new OperationConfig(applyInfoDTO.getCompetitionSeriesId(), operationType);
        //获取配置信息列表
        List<OperationConfig> operationConfigs = operationConfigMapper.selectOperationConfigList(operationConfig);
        //没用配置 默认-1不限制
        if (CollectionUtils.isEmpty(operationConfigs)) {
            return true;
        }
        OperationConfig configInfo = operationConfigs.get(0);
        //最大操作次数
        Long maxTimes = configInfo.getMaxTimes() == null ? -1L : configInfo.getMaxTimes();
        //配置不限制
        if (maxTimes == -1) {
            return true;
        }
        if (maxTimes == 0) {
            return false;
        }
        List<OperationTimes> operationTimes = operationTimesMapper.selectOperationTimesByConfigId(configInfo.getId(), applyInfoDTO.getTeamCode());
        //有配置次数大于0，任何类型都未修改
        if (CollectionUtils.isEmpty(operationTimes)) {
            return true;
        }
        //有配置次数大于0，有修改记录
        //根据操作类型分组 操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
        Map<String, List<OperationTimes>> operationTimesMap = operationTimes.stream().collect(Collectors.groupingBy(OperationTimes::getOperationType));
        //配置类型1:参赛信息修改(修改组别，修改队员信息，队员人员变更) 2:指导教师修改(修改指导老师信息，指导老师人员变更) 3:退费重缴(申请退费重缴) 4:退赛申请(退赛申请)
        //具体操作类型group:更换组别，info:修改信息，change人员变更，repayment退费重缴费
        if ("3".equals(operationType)) {
            List<OperationTimes> repaymentTimes = operationTimesMap.get("repayment");
            if (CollectionUtils.isNotEmpty(repaymentTimes) && repaymentTimes.get(0).getUsedTimes() >= maxTimes) {
                return false;
            }
            return true;
        }
        if (operationTypes.contains("group")) {
            List<OperationTimes> groupTimes = operationTimesMap.get("group");
            if (CollectionUtils.isNotEmpty(groupTimes) && groupTimes.get(0).getUsedTimes() >= maxTimes) {
                return false;
            }
        }
        if (operationTypes.contains("change")) {
            List<OperationTimes> changeTimes = operationTimesMap.get("change");
            if (CollectionUtils.isNotEmpty(changeTimes) && changeTimes.get(0).getUsedTimes() >= maxTimes) {
                return false;
            }
        }
        if (operationTypes.contains("changeTeacher")) {
            List<OperationTimes> changeTimes = operationTimesMap.get("changeTeacher");
            if (CollectionUtils.isNotEmpty(changeTimes) && changeTimes.get(0).getUsedTimes() >= maxTimes) {
                return false;
            }
        }
        if (operationTypes.contains("info")) {
            List<OperationTimes> infoTimes = operationTimesMap.get("info");
            if (CollectionUtils.isEmpty(infoTimes)) {
                return true;
            }
            List<CompetitionApplyInfo> competitionApplyInfoList = "1".equals(operationType) ? applyInfoDTO.getCompetitionApplyInfoList() : applyInfoDTO.getGuideTeacherApplyInfoList();
            for (CompetitionApplyInfo competitionApplyInfo : competitionApplyInfoList) {
                OperationTimes memberTimes = Optional.ofNullable(infoTimes)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(infoTime -> infoTime.getMemberId().equals(competitionApplyInfo.getMemberId())).findFirst().orElse(null);
                if (memberTimes != null && memberTimes.getUsedTimes() >= maxTimes) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 设置指导老师/队员信息信息修改剩余次数
     *
     * @param maxTimes                  配置的值
     * @param infoTimes                 操作次数集合
     * @param guideTeacherApplyInfoList 指导老师/队员信息集合
     */
    private void setApplyInfoListTimes(Long maxTimes, List<OperationTimes> infoTimes, List<CompetitionApplyInfo> guideTeacherApplyInfoList) {
        guideTeacherApplyInfoList.forEach(guideTeacherApplyInfo -> {
            OperationTimes memberTimes = Optional.ofNullable(infoTimes)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(infoTime -> guideTeacherApplyInfo.getMemberId().equals(infoTime.getMemberId())).findFirst().orElse(null);
            long used = memberTimes != null ? memberTimes.getUsedTimes() : 0L;
            guideTeacherApplyInfo.setApplyInfoChangeOperateCount((maxTimes < used) ? 0L : (maxTimes - used));
        });
    }

    /**
     * 设置剩余次数为配置值
     *
     * @param applyInfoDTO 队伍信息
     * @param maxTimes     配置的值
     */
    private void setEasyCountNum(UserCompetitionApplyInfoDTO applyInfoDTO, Long maxTimes, String type) {
        if (StringUtils.isNull(type)) {
            applyInfoDTO.setRepaymentOperateCount(maxTimes);
            //全部
            applyInfoDTO.setSecondLevelOperateCount(maxTimes);
            //队员人员变更剩余次数
            applyInfoDTO.setMemberOperateCount(maxTimes);
            List<CompetitionApplyInfo> competitionApplyInfoList = applyInfoDTO.getCompetitionApplyInfoList();
            competitionApplyInfoList.forEach(competitionApplyInfo -> {
                competitionApplyInfo.setApplyInfoChangeOperateCount(maxTimes);
            });
            //指导老师人员变更剩余次数
            applyInfoDTO.setGuideTeacherOperateCount(maxTimes);
            List<CompetitionApplyInfo> guideTeacherApplyInfoList = applyInfoDTO.getGuideTeacherApplyInfoList();
            guideTeacherApplyInfoList.forEach(guideTeacherApplyInfo -> {
                guideTeacherApplyInfo.setApplyInfoChangeOperateCount(maxTimes);
            });
        } else {
            //单个 //配置类型 1:参赛信息修改 2:指导教师修改 3:退费重缴 4:退赛申请
            if ("1".equals(type)) {
                //参赛信息修改剩余次数
                applyInfoDTO.setSecondLevelOperateCount(maxTimes);
                applyInfoDTO.setMemberOperateCount(maxTimes);
                List<CompetitionApplyInfo> competitionApplyInfoList = applyInfoDTO.getCompetitionApplyInfoList();
                competitionApplyInfoList.forEach(competitionApplyInfo -> {
                    competitionApplyInfo.setApplyInfoChangeOperateCount(maxTimes);
                });
            } else if ("2".equals(type)) {
                //指导老师修改剩余次数
                applyInfoDTO.setGuideTeacherOperateCount(maxTimes);
                List<CompetitionApplyInfo> guideTeacherApplyInfoList = applyInfoDTO.getGuideTeacherApplyInfoList();
                guideTeacherApplyInfoList.forEach(guideTeacherApplyInfo -> {
                    guideTeacherApplyInfo.setApplyInfoChangeOperateCount(maxTimes);
                });
            } else if ("3".equals(type)) {
                //退费重缴剩余次数
                applyInfoDTO.setRepaymentOperateCount(maxTimes);
            }
        }

    }
}
