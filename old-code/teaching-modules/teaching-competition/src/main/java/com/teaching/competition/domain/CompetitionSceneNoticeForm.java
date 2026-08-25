package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 现场通知新增、修改及状态操作表单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneNoticeForm extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long noticeId;
    private String noticeType;
    private String scopeType;
    private Long competitionSeriesId;
    private Long targetId;
    private String title;
    private String content;

    /**
     * 富文本的UTF-8 Base64传输值。用于避免网关对原始JSON执行HTML清洗后破坏转义引号。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contentBase64;

    private String noticeLevel;
    private String isTop;
    private Integer sortNo;
    private String publishStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private String status;
    private List<Long> scheduleIds;
}
