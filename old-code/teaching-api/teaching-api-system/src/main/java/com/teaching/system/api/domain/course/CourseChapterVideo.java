package com.teaching.system.api.domain.course;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 章节视频信息对象 course_chapter_video
 *
 * @author teaching
 * @date 2025-10-23
 */
public class CourseChapterVideo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 章节id
     */
    @Excel(name = "章节id")
    @NotNull(message = "章节id不能为空")
    private Long chapterId;

    /**
     * 视频名称
     */
    @Excel(name = "视频名称")
    @NotBlank(message = "视频名称不能为空")
    private String videoName;

    /**
     * 视频文件地址
     */
    @Excel(name = "视频文件地址")
    @NotBlank(message = "视频文件地址不能为空")
    private String videoFile;

    /**
     * 视频序号
     */
    @Excel(name = "视频序号")
    private String videoNum;

    /**
     * 视频时长(单位秒)
     */
    @Excel(name = "视频时长(单位秒)")
    private Long videoDuration;

    /**
     * 是否免费(字典sys_yes_no
     */
    @Excel(name = "是否免费(字典sys_yes_no")
    private String isFree;

    /**
     * 费用
     */
    private BigDecimal expenses;

    /**
     * 审核状态(字典check_status
     */
    @Excel(name = "审核状态(字典check_status")
    private String checkStatus;
    /**
     * 审核意见
     */
    private String applyReason;

    /**
     * 发布状态
     */
    @Excel(name = "发布状态")
    private String publishStatus;
    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 数据权限用户id
     */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /**
     * 数据权限机构id
     */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public CourseChapterVideo() {
    }

    public CourseChapterVideo(Long videoId, String checkStatus) {
        this.videoId = videoId;
        this.checkStatus = checkStatus;
    }

    public CourseChapterVideo(Long videoId, String checkStatus, String applyReason) {
        this.videoId = videoId;
        this.checkStatus = checkStatus;
        this.applyReason = applyReason;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public CourseChapterVideo(Long chapterId) {
        this.chapterId = chapterId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoNum(String videoNum) {
        this.videoNum = videoNum;
    }

    public String getVideoNum() {
        return videoNum;
    }

    public void setVideoDuration(Long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public Long getVideoDuration() {
        return videoDuration;
    }

    public void setIsFree(String isFree) {
        this.isFree = isFree;
    }

    public String getIsFree() {
        return isFree;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("videoId", getVideoId())
                .append("chapterId", getChapterId())
                .append("videoName", getVideoName())
                .append("videoFile", getVideoFile())
                .append("videoNum", getVideoNum())
                .append("videoDuration", getVideoDuration())
                .append("isFree", getIsFree())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
