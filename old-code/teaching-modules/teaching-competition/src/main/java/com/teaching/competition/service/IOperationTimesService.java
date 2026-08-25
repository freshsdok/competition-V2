package com.teaching.competition.service;

import com.teaching.competition.domain.OperationTimes;
import com.teaching.competition.domain.UserCompetitionApplyInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * 队伍操作次数Service接口
 *
 * @author teaching
 * @date 2026-01-24
 */
public interface IOperationTimesService {
    /**
     * 查询队伍操作次数
     *
     * @param id 队伍操作次数主键
     * @return 队伍操作次数
     */
    public OperationTimes selectOperationTimesById(Long id);

    /**
     * 查询队伍操作次数列表
     *
     * @param operationTimes 队伍操作次数
     * @return 队伍操作次数集合
     */
    public List<OperationTimes> selectOperationTimesList(OperationTimes operationTimes);

    /**
     * 新增队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    public int insertOperationTimes(OperationTimes operationTimes);

    /**
     * 修改队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    public int updateOperationTimes(OperationTimes operationTimes);

    /**
     * 批量删除队伍操作次数
     *
     * @param ids 需要删除的队伍操作次数主键集合
     * @return 结果
     */
    public int deleteOperationTimesByIds(Long[] ids);


    public int cancelRepaymentOperationTimes(String teamCode);

    /**
     * 删除队伍操作次数信息
     *
     * @param id 队伍操作次数主键
     * @return 结果
     */
    public int deleteOperationTimesById(Long id);

    /**
     * 1.查询详情时计算剩余次数
     */
    public void calculateRemainingTimes(UserCompetitionApplyInfoDTO applyInfoDTO);

    /**
     * 2.校验参赛信息修改剩余次数是否足够
     *
     * @param applyInfoDTO   参赛信息列表
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更）
     */
    public Boolean checkTeamMembersTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes);

    /**
     * 3.校验指导老师信息修改剩余次数
     *
     * @param applyInfoDTO   参赛信息列表
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
     * @return
     */
    public Boolean checkTeacherTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes);

    /**
     * 4.校验退费重缴剩余次数
     *
     * @param applyInfoDTO   参赛信息列表
     * @param operationTypes 传入真实的操作类型（group:更换组别，info:修改信息，change人员变更，repayment退费重缴费）
     * @return
     */
    public Boolean checkRepaymentTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<String> operationTypes);


    /**
     * 5.可以提交时记录已使用次数
     */
    public int recordUsedTimes(UserCompetitionApplyInfoDTO applyInfoDTO, List<Map<String,String>> operationTypes);
}
