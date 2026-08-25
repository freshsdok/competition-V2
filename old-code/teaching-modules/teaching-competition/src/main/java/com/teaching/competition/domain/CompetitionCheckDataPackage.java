package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 校验包对象 competition_check_data_package
 *
 * @author teaching
 * @date 2025-12-18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompetitionCheckDataPackage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 校验项目包id
     */
    private Long packageId;

    /**
     * 校验项目包名称
     */
    @Excel(name = "校验项目包名称")
    private String packageName;

    /**
     * 校验内容
     */
    @Excel(name = "校验内容")
    private String packageJson;

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
     * 校验项列表
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CompetitionCheckInfo> checkInfoList;

    public List<CompetitionCheckInfo> getCheckInfoList() {
        return checkInfoList;
    }

    public void setCheckInfoList(List<CompetitionCheckInfo> checkInfoList) {
        this.checkInfoList = checkInfoList;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageJson(String packageJson) {
        this.packageJson = packageJson;
    }

    public String getPackageJson() {
        return packageJson;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("packageId", getPackageId())
                .append("packageName", getPackageName())
                .append("packageJson", getPackageJson())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
