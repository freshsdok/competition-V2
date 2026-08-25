package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 工单信息对象 work_order
 *
 * @author teaching
 * @date 2025-10-30
 */
public class WorkOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 工单id
     */
    private Long orderId;

    /**
     * 工单来源id
     */
    @Excel(name = "工单来源id")
    private String orderSourceId;

    /**
     * 工单来源（字典work_order_source）
     */
    @Excel(name = "工单来源", readConverterExp = "字=典work_order_source")
    private String orderSource;

    /**
     * 工单内容
     */
    @Excel(name = "工单内容")
    private String orderContent;

    /**
     * 紧急等级（字典emergency_level）
     */
    @Excel(name = "紧急等级", readConverterExp = "字=典emergency_level")
    private String emergencyLevel;

    /**
     * 工单处理状态（0处理中 /1 已完成）
     */
    @Excel(name = "工单处理状态", readConverterExp = "0=处理中,/=1,已=完成")
    private String orderDealStatus;

    /**
     * 最后处理人员
     */
    @Excel(name = "最后处理人员")
    private Long dealPern;

    /**
     * 最后处理内容
     */
    @Excel(name = "最后处理内容")
    private String replyContent;

    /**
     * 创建人id
     */
    @Excel(name = "创建人id")
    private Long createUser;

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

    /**
     * 工单转单记录信息
     */
    private List<WorkOrderTransfer> workOrderTransferList;

    /**
     * 接收方（userId和roleId）
     */
    private List<String> recipients;

    public WorkOrder() {
    }

    public WorkOrder(Long orderId) {
        this.orderId = orderId;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderSourceId(String orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public String getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderContent(String orderContent) {
        this.orderContent = orderContent;
    }

    public String getOrderContent() {
        return orderContent;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setOrderDealStatus(String orderDealStatus) {
        this.orderDealStatus = orderDealStatus;
    }

    public String getOrderDealStatus() {
        return orderDealStatus;
    }

    public void setDealPern(Long dealPern) {
        this.dealPern = dealPern;
    }

    public Long getDealPern() {
        return dealPern;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getCreateUser() {
        return createUser;
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

    public List<WorkOrderTransfer> getWorkOrderTransferList() {
        return workOrderTransferList;
    }

    public void setWorkOrderTransferList(List<WorkOrderTransfer> workOrderTransferList) {
        this.workOrderTransferList = workOrderTransferList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("orderSourceId", getOrderSourceId())
                .append("orderSource", getOrderSource())
                .append("orderContent", getOrderContent())
                .append("emergencyLevel", getEmergencyLevel())
                .append("orderDealStatus", getOrderDealStatus())
                .append("dealPern", getDealPern())
                .append("replyContent", getReplyContent())
                .append("createUser", getCreateUser())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("workOrderTransferList", getWorkOrderTransferList())
                .toString();
    }
}
