package com.teaching.competition.review.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用评审模块实体基类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewBaseEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String delFlag;
}
