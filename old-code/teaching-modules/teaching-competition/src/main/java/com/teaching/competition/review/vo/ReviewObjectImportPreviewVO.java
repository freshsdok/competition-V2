package com.teaching.competition.review.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 评审对象导入预览结果。
 */
@Data
public class ReviewObjectImportPreviewVO {
    private String sourceBizId;
    private String sourceBizType;
    private Integer defenseOrder;
    private String inputOrgName;
    private String inputTeamName;
    private String inputLeaderName;
    private String teamCode;
    private String teamName;
    private String objectName;
    private String leaderName;
    private Integer memberCount;
    private Integer certificateCount;
    private Integer materialCount;
    private List<String> permissionUsers = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private Boolean canImport;
}
