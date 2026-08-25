package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 生成现场证件请求。
 */
@Data
public class CompetitionSceneCredentialGenerateReq {
    private Long scheduleId;
    private List<Long> targetIds;
    private Boolean regenerate;
}
