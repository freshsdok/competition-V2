package com.teaching.competition.domain;

import lombok.Data;

/**
 * 现场证件第一阶段固定能力结构。
 */
@Data
public class CompetitionSceneCredentialAbility {
    private Boolean report = false;
    private Boolean material = false;
    private Boolean waiting = false;
    private Boolean review = false;
    private Boolean resourceReservation = false;
    private Boolean vipAccess = false;
}

