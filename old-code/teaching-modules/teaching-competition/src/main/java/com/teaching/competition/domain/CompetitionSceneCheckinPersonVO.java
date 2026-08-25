package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 赛场人员签到明细。
 */
@Data
public class CompetitionSceneCheckinPersonVO {
    private Long targetId;
    private Long userId;
    private Long memberId;
    private Long credentialId;
    private Long teamId;
    private String teamCode;
    private String teamName;
    private String personName;
    private String roleCode;
    private String roleName;
    private String checkinStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;

    private String checkinMethod;
}
