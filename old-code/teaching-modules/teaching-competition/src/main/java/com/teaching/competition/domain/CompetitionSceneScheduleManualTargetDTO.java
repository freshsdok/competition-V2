package com.teaching.competition.domain;

import lombok.Data;

/**
 * 赛场安排手工对象入参。
 */
@Data
public class CompetitionSceneScheduleManualTargetDTO {
    private String targetName;
    private String orgName;
    private String contactPhone;
    private String remark;
}
