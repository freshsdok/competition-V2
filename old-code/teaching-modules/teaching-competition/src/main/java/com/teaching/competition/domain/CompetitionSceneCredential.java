package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 赛事现场证件实例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneCredential extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long credentialId;
    private Long scheduleId;
    private Long targetId;
    private String credentialNo;
    private String credentialToken;
    private String qrContent;
    private String qrCodeUrl;
    private String credentialFileUrl;
    private String credentialImageUrl;
    private String issueChannel;
    private String scopeType;
    private Long scopeRefId;
    private String activeCoreCredentialKey;
    private String credentialName;
    private String abilityJson;
    private String credentialType;
    private String configDimension;
    private String subjectType;
    private String subjectCode;
    private Long competitionSeriesId;
    private String competitionName;
    private String competitionStageId;
    private String competitionStageName;
    private String competitionTrackId;
    private String competitionTrackName;
    private String secondLevelCode;
    private String secondLevelName;
    private String teamCode;
    private String teamName;
    private Long memberId;
    private Long userId;
    private String userName;
    private String phone;
    private String email;
    private String idCardType;
    private String idCardHash;
    private String idCardSuffix;
    private String school;
    private String schoolName;
    private Long orgId;
    private String orgName;
    private String competitionRoleName;
    private Long leaderTeacherId;
    private String leaderTeacher;
    private String guideTeacher;
    private List<CompetitionSceneCredentialMember> teamMembers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportEndTime;
    private String reportLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date contestStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date contestEndTime;
    private String contestLocation;
    private String contestRoom;
    private String seatNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingEndTime;
    private String waitingLocation;
    private String waitingGroupCode;
    private String waitingGroupName;

    private String materialLocation;
    private String notice;
    private String credentialSnapshotJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validFrom;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validTo;
    private String credentialStatus;
    private Integer verifyCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastVerifyTime;

    private String reportStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;
    private Long reportOperatorId;
    private String reportOperatorName;

    private String materialStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date materialTime;
    private String materialReceiverName;
    private String materialReceiverPhone;
    private String materialReceiverIdSuffix;
    private Long materialOperatorId;
    private String materialOperatorName;

    private String waitingStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingTime;
    private Long waitingOperatorId;
    private String waitingOperatorName;

    private String reportStateStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportStateTime;
    private String materialStateStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date materialStateTime;
    private String materialDelegateName;
    private String materialDelegateRelation;
    private String waitingStateStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingStateTime;

    private Long version;
    private String delFlag;
}
