package com.teaching.competition.service;

import com.teaching.competition.domain.UserApplyCompetitionReq;
import com.teaching.system.api.domain.*;

import java.util.List;

/**
 * 赛事报名校验服务
 * @ClassName CompetitionApplyInfoService
 * @Description
 * @Author entes
 * @DATE 2025/10/13 15:05
 * @Version 1.0
 **/
public interface CompetitionApplyInfoCheckService {

    void checkApplyCompetition(CompetitionConfig competitionConfig, UserApplyCompetitionReq applyCompetition, SysUser sysUser);

    void checkTeamMemberCompetition(CompetitionConfig competitionConfig, List<TeamMemberRela> teamMemberRelas);

    void checkStudent(CompetitionConfig competitionConfig, SysUser sysUser);

    // 校验带队老师
    void checkLeaderGroupTeacher(SysUser sysUser);

    // 校验是否实名认证
    void checkAuth(CompetitionConfig competitionConfig, SysUser sysUser);

    // 导入数据完整性校验
    void checkExcelIntegralityApplyInfo(List<CompetitionApplyInfo> applyInfoList);

}
