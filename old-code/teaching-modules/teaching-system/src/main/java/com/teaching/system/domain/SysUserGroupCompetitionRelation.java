package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户组关联赛事关系对象 sys_user_group_competition_relation
 *
 * @author teaching
 * @date 2026-01-07
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysUserGroupCompetitionRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonProperty("relationId")
    private Long id;

    /**
     * 用户组id
     */
    private Long userGroupId;


    /**
     * 编码
     */
    @Excel(name = "编码")
    @JsonProperty("id")
    private String code;

    /**
     * 名称
     */
    @Excel(name = "名称")
    @JsonProperty("label")
    private String name;

    /**
     * 排序
     */
    @Excel(name = "排序")
    private Long sort;

    private String delFlag;

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }


    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public Long getSort() {
        return sort;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("code", getCode())
                .append("name", getName())
                .append("sort", getSort())
                .toString();
    }
}
