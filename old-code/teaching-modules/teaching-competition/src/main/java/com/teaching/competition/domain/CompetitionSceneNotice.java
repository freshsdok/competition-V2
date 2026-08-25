package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 赛事现场公告或个人通知。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneNotice extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long noticeId;
    private String noticeType;
    private String scopeType;
    private Long competitionSeriesId;
    private Long competitionId;
    private Long targetId;
    private Long userId;
    private Long memberId;
    private String recipientName;
    private String title;
    private String content;
    private String noticeLevel;
    private String isTop;
    private Integer sortNo;
    private String publishStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private String status;
    private String delFlag;
}

