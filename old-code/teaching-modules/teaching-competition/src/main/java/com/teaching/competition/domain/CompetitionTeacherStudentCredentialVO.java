package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 教师个人中心可查看的学生参赛证只读视图。
 */
@Data
public class CompetitionTeacherStudentCredentialVO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_NOT_GENERATED = "NOT_GENERATED";

    private Long competitionId;
    private Long competitionSeriesId;
    private String competitionName;
    private String teamCode;
    private String teamName;
    private String schoolName;
    private String groupCode;
    private String groupName;

    private Long userId;
    private Long memberId;
    private String studentName;
    private String roleCode;
    private String roleName;
    private String studentGroupCode;
    private String studentGroupName;

    private Long credentialId;
    private String credentialName;
    private String credentialNo;
    private String credentialType;
    private String credentialStatus;
    private String qrContent;
    private String qrCodeUrl;
    private String credentialFileUrl;
    private String credentialImageUrl;
    private String reportStatus;
    private String materialStatus;
    private String waitingStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date materialTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingTime;
    private String delegateInfo;

    private Long scheduleId;
    private String scheduleName;
    private String scheduleLocation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduleStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduleEndTime;

    @JsonIgnore
    private String contestLocation;
    @JsonIgnore
    private String contestRoom;
    @JsonIgnore
    private Long scopeRefId;
    @JsonIgnore
    private String scopeType;
    @JsonIgnore
    private String subjectType;
    @JsonIgnore
    private String subjectCode;
    @JsonIgnore
    private String configDimension;
}
