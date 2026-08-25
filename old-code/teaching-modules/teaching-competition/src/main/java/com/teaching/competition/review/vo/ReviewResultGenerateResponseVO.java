package com.teaching.competition.review.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 结果生成返回。
 */
@Data
public class ReviewResultGenerateResponseVO {
    private Integer totalCount = 0;
    private Integer generatedCount = 0;
    private Integer skippedCount = 0;
    private Integer warningCount = 0;
    private List<String> warnings = new ArrayList<>();
    private List<ReviewResultListVO> results = new ArrayList<>();

    public void addWarning(String warning) {
        warnings.add(warning);
        warningCount = warnings.size();
    }
}
