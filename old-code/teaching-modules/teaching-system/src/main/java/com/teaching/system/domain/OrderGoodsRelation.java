package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 订单商品关联对象 order_goods_relation
 * 
 * @author teaching
 * @date 2025-12-08
 */
public class OrderGoodsRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 逐渐 */
    private Long id;

    /** 订单id */
    @Excel(name = "订单id")
    private Long orderId;

    /** 关联商品id */
    @Excel(name = "关联商品id")
    private String commodityId;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /**
     * 团队管理人员id（多个逗号分割存储）
     */
    private String users;

    /**
     * 调整类型
     * change-报名团队人员变更
     * retired--退赛
     * repayment-退费重缴费
     */
    private String changeType;

    /**
     * 退费重缴/报名增加人员，产生的支付订单，支付状态
     */
    private String payStatus;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setCommodityId(String commodityId) 
    {
        this.commodityId = commodityId;
    }

    public String getCommodityId() 
    {
        return commodityId;
    }

    public void setVersion(Long version) 
    {
        this.version = version;
    }

    public Long getVersion() 
    {
        return version;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getChangeType() {
        return changeType;
    }
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("commodityId", getCommodityId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("orgId", getOrgId())
            .toString();
    }
}
