package com.teaching.competition.review.vo;

import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import lombok.Data;

import java.util.List;

/**
 * 评审对象填报详情。
 */
@Data
public class ReviewSubmissionDetailVO {
    private ReviewObject object;

    private List<ReviewObjectMember> members;

    private List<ReviewObjectMaterial> materials;

    private List<ReviewSubmissionPermission> permissions;

    private String currentStatus;

    private Boolean editable;

    private Boolean submittable;

    private Boolean withdrawable;

    private String warningMessage;
}
