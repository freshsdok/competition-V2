package com.teaching.competition.service;

import com.teaching.competition.domain.*;
import com.teaching.system.api.domain.*;

import java.util.List;
import java.util.Map;

public interface UserCompetitionService {

    /**
     * 用户申请赛事进行报名
     *
     * @param userApplyCompetitionReq 赛事id
     */
    public int userApplyCompetitionInfo(UserApplyCompetitionReq userApplyCompetitionReq);


    public int agreeJoinTeam(Map<String, String> param);

    public List<TeamManagerInfo> selectTeamInfoByUserId(Long competitionSeriesId);

    /**
     * 获取用户申请赛事的报名状态
     *
     * @param userApplyCompetitionReq 赛事id
     * @return 0-未报名 1-已报名 2-已通过 3-未通过
     */
    public CompetitionApplyAllStatus checkCompetitionApplyStatusByUser(UserApplyCompetitionReq userApplyCompetitionReq);

    /**
     * 获取赛事赛道信息
     *
     * @param competitionSeriesId 赛事id
     * @return 赛事赛道信息
     */
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 获取PC端赛事列表
     *
     * @param
     * @return 赛事信息
     */
    public List<CompetitionMainInfo> selectUserCompetitionMainInfoList(CompetitionMainInfoReq req);

    // 报名信息导入
    public List<CompetitionApplyInfo> importApplyCompetitionData(List<CompetitionApplyInfo> applyInfoList, boolean updateSupport, Long userId,UserApplyCompetitionReq req) throws Exception;

    // 报名信息入库
    public int saveApplyCompetitionData(UserApplyCompetitionReq req);

    // 同步报名成功学生信息
    public void syncCompetitionApplyInfo(String updateSize);

    /** 账号注册后按手机号关联历史教师赛报名。 */
    public int bindTeacherCompetitionUser(Long userId, String phone);

    // 获取用户报名信息
    public List<UserCompetitionApplyInfoDTO> selectUserCompetitionApplyInfoDetail(Map params);

    // 获取用户报名信息列表
    public List<UserCompetitionApplyInfoTeam> selectUserCompetitionApplyInfoList(Map params) throws Exception;

    /**
     * 校验当前用户是否可以访问指定团队。
     */
    public boolean hasUserTeamAccess(String teamCode, Long userId);

    // 修改报名信息
    public int updateCompetitionApplyInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam,String operationFlag) throws Exception;

    public void checkUpdatePhoneAndEmail(List<CompetitionApplyInfo> applyInfoList,Long competitionSeriesId);

    public void checkPhoneAndEmail(List<CompetitionApplyInfo> applyInfoList,Long competitionSeriesId);

    public void checkData(List<CompetitionApplyInfo> applyInfoList,Long competitionSeriesId,String operationFlag) throws Exception;

    // 增删报名信息
    public int updateAddCompetitionApplyInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam) throws Exception;

    // 报名换组别
    public int updateCompetitionApplyTeamInfo(UserCompetitionApplyInfoTeam userCompetitionApplyInfoTeam) throws Exception;

    // 获取赛事下所有组别
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigInfo(CompetitionTrackConfig competitionTrackConfig);

    // 获取赛事下所有操作权限配置信息
    public List<OperationConfig> selectCompetitionOperationConfigInfo(Long competitionSeriesId);

    public  boolean checkChangeOperator(Long competitionSeriesId,SysUser sysUser,String operationFlag);

    public int updateCompetitionApplyInfoTeamSort(Map<String,Object> params);

}
