package com.teaching.competition.domain;

import lombok.Data;

@Data
public class CompetitionSceneCredentialMember {
    private Long memberId;
    private Long userId;
    private String userName;
    private String schoolName;
    private String competitionRoleName;
}
