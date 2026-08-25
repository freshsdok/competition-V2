package com.teaching.competition.review.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 参赛证解析聚合结果。
 */
@Data
public class ReviewCertificateResolveResultVO {
    private Long activityId;
    private String certificateCode;
    private Integer matchedCount;
    private List<ReviewCertificateResolveVO> candidates = new ArrayList<>();
    private String warningMessage;
}
