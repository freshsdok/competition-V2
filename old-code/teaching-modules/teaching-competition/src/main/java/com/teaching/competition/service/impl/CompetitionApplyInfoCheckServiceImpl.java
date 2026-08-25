package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.domain.UserApplyCompetitionReq;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSeriesInfoMapper;
import com.teaching.competition.service.CompetitionApplyInfoCheckService;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CompetitionApplyInfoCheckServiceImpl implements CompetitionApplyInfoCheckService {

    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CompetitionSeriesInfoMapper competitionSeriesInfoMapper;

    @Override
    public void checkApplyCompetition(CompetitionConfig competitionConfig, UserApplyCompetitionReq applyCompetition, SysUser sysUser) {
        // 报名开始时间及结束时间
        if (competitionConfig.getApplyStartTime().getTime() > System.currentTimeMillis()){
            throw new GlobalException("报名还未开始");
        }
        if (competitionConfig.getApplyEndTime().getTime() < System.currentTimeMillis()){
            throw new GlobalException("报名时间已结束");
        }
        // 校验实名认证
        this.checkAuth(competitionConfig,sysUser);
        // 是否必须是学生
        if(competitionConfig.getIsStudent().equals(Constants.IS_YES)){
            this.checkStudent(competitionConfig, sysUser);
        }
        // 团队校验
        // 参赛方式是团队 校验团队人数 minPernNum maxPernNum
        if (competitionConfig.getJoinType().equals(Constants.JOIN_TYPE_TEAM)) {
            int size = applyCompetition.getTeamMemberRelaList().size();
            if (size < Integer.valueOf(competitionConfig.getMinPernNum())) {
                throw new GlobalException("团队人数不足");
            }
            if (size > Integer.valueOf(competitionConfig.getMaxPernNum())) {
                throw new GlobalException("团队人数超出设定范围");
            }
        }
        // 是否必须含指导老师 isTeacherNess
        if(competitionConfig.getIsTeacherNess().equals(Constants.IS_YES)){
            // 指导老师数量限制 minTeacherNum  maxTeacherNum
            List<String> guideTeachers = Arrays.asList(applyCompetition.getGuideTeacher().split( ","));
            if(CollectionUtils.isEmpty(guideTeachers)){
                throw new GlobalException("不存在指导老师");
            }
            if (guideTeachers.size() < Integer.valueOf(competitionConfig.getMinTeacherNum() == null ? "0" : competitionConfig.getMinTeacherNum()) ||
                    guideTeachers.size() > Integer.valueOf(competitionConfig.getMaxTeacherNum() == null ? "0" : competitionConfig.getMaxTeacherNum())) {
                throw new GlobalException("指导老师数量不在范围内");
            }
        }
    }

    @Override
    public void checkTeamMemberCompetition(CompetitionConfig competitionConfig,List<TeamMemberRela> teamMemberRelaList) {
        if(CollectionUtils.isNotEmpty(teamMemberRelaList)){
            teamMemberRelaList.stream().forEach(teamMember -> {
                R<SysUser> userInfo = userService.getUserCenterInfo(teamMember.getUserId(), SecurityConstants.INNER);
                if(userInfo.getCode() != HttpStatus.SUCCESS && userInfo.getData() ==  null){
                    throw new GlobalException("用户不存在");
                }
                SysUser sysUser = userInfo.getData();
                this.checkStudent(competitionConfig, sysUser);
            });
        }
    }

    @Override
    public void checkStudent(CompetitionConfig competitionConfig, SysUser sysUser) {
        // 是否必须是学生
        if(competitionConfig.getIsStudent().equals(Constants.IS_YES)){
            List<String> identityInfoList = sysUser.getIdentityInfoList().stream().map(IdentityInfo::getCertificationType)
                    .collect(Collectors.toList());
            if(!identityInfoList.contains(Constants.IDENTITY_TYPE_STUDENT)){
                throw new GlobalException("参赛队员必须是学生");
            }
            // 专业是否符合要求 professionRequest
            List<String> professionRequestList= Arrays.asList(competitionConfig.getProfessionRequest().split(","));
            sysUser.getIdentityInfoList().stream().forEach(applyCompetition -> {
                if(applyCompetition.getCertificationType().equals(Constants.IDENTITY_TYPE_STUDENT)){
                    if (!professionRequestList.contains(applyCompetition.getSpecialty())) {
                        throw new GlobalException("参赛队员专业不符合要求");
                    }
                    // 年级是否符合要求 classRequest
                    List<String> classRequestList= Arrays.asList(competitionConfig.getClassRequest().split(","));
                    if (!classRequestList.contains(applyCompetition.getClassInfo())) {
                        throw new GlobalException("参赛队员年级不符合要求");
                    }
                }
            });
        }
    }

    @Override
    public void checkLeaderGroupTeacher(SysUser sysUser) {
        List<IdentityInfo> identityInfoList = sysUser.getIdentityInfoList();
        if(CollectionUtils.isEmpty(identityInfoList)){
            throw new GlobalException("您的身份不是带队老师，不能进行报名，如需要报名请完成教师身份认证");
        }
        List<String> identityTypeList =
                identityInfoList.stream().map(IdentityInfo::getCertificationType).collect(Collectors.toList());
        if(!identityTypeList.contains(Constants.IDENTITY_TYPE_TEACHER)){
            throw new GlobalException("您的身份不是带队老师，不能进行报名，如需要报名请完成教师身份认证");
        }
    }

    @Override
    public void checkAuth(CompetitionConfig competitionConfig, SysUser sysUser) {
        if(competitionConfig.getIsRealNameAuth().equals(Constants.IS_YES)){
            if (!Constants.AUTH_STATUS_PASS.equals(sysUser.getAuthStatus())) {
                throw new GlobalException("未进行实名认证");
            }
        }
    }

    // 数据完整性校验
    @Override
    public void checkExcelIntegralityApplyInfo(List<CompetitionApplyInfo> applyInfoList) {
    }
}
