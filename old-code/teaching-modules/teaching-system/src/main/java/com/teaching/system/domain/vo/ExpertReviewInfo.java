package com.teaching.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 专家评审
 */
@Data
public class ExpertReviewInfo {
    /**
     * 专家任务关联关系记录id
     */
    private Long relaId;
    /**
     * 评审任务id
     */
    private Long reviewId;
    /**
     * 专家id
     */
    private Long userId;
    /**
     * 评审任务名称
     */
    private String reviewName;

    /**
     * 审阅开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    /**
     * 审阅结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    /**
     * pdf处理前后关联关系id
     */
    private Long processedId;
    private String processedStr;
    /**
     * 评审状态 (0未审，1已审）
     */
    private String reviewStatus;
    /**
     * 评审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
    /**
     * 比赛名称
     */
    private String competitionName;
    /**
     * 比赛赛道名称
     */
    private String competitionTrackName;
    /**
     * 二级分类名称/组别
     */
    private String secondLevelName;

    /**
     * 新文件名
     */
    private String newFileName;
    /**
     * 是否可继续进入评审
     */
    private Boolean continueFlag = true;

}
