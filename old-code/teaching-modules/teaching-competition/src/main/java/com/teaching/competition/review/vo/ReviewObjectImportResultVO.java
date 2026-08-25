package com.teaching.competition.review.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部业务导入评审对象结果。
 */
@Data
public class ReviewObjectImportResultVO {
    private int totalCount;
    private int successCount;
    private int skipCount;
    private int failedCount;
    private int importedCount;
    private int skippedCount;
    private List<Long> createdObjectIds = new ArrayList<>();
    private List<String> skippedItems = new ArrayList<>();
    private List<String> failedItems = new ArrayList<>();
    private String message;
}
