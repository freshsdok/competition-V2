package com.teaching.competition.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 赛场安排自动匹配结果。
 */
@Data
public class CompetitionSceneMatchResult {
    private Integer totalCount = 0;
    private Integer matchedCount = 0;
    private Integer skippedCount = 0;
    private Integer failedCount = 0;
    private String message;
    private List<String> warnings = new ArrayList<>();
}
