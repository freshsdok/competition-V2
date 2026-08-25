package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛事现场赛场安排对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneScheduleTarget extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long targetId;
    private Long scheduleId;
    private Long competitionSeriesId;
    private String credentialType;
    private String configDimension;
    private String targetKey;
    private String targetSource;
    private String targetType;
    private Long reviewObjectId;
    private String targetName;
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
    private String certificateCode;
    private String school;
    private String schoolName;
    private Long orgId;
    private String orgName;
    private String competitionRoleName;
    private String competitionTrackId;
    private String competitionTrackName;
    private String secondLevelCode;
    private String secondLevelName;
    private Long leaderTeacherId;
    private String leaderTeacher;
    private String guideTeacher;
    private String seatNo;
    private Integer sequenceNo;
    private String waitingGroupCode;
    private String waitingGroupName;
    private String targetSnapshotJson;
    private String matchStatus;
    private String sourceModule;
    private String sourceBizType;
    private String sourceBizId;
    private String status;
    private Long version;
    private String delFlag;

    /**
     * 手工配置时临时接收证件号，用于生成 hash 和后六位，不写入数据库。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String idCard;
}
