package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 工单转单记录对象 work_order_transfer
 *
 * @author teaching
 * @date 2025-10-30
 */
public class WorkOrderTransfer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 转单记录i的
     */
    private Long id;

    /**
     * 工单id
     */
    @Excel(name = "工单id")
    private Long orderId;

    /**
     * 转单人
     */
    @Excel(name = "转单人")
    private Long transfer;

    /**
     * 接收方（是userId或者是roleId）
     */
    @Excel(name = "接收方", readConverterExp = "是=userId或者是roleId")
    private String recipient;

    /**
     * 转单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "转单时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date transferTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setTransfer(Long transfer) {
        this.transfer = transfer;
    }

    public Long getTransfer() {
        return transfer;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setTransferTime(Date transferTime) {
        this.transferTime = transferTime;
    }

    public Date getTransferTime() {
        return transferTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("orderId", getOrderId())
                .append("transfer", getTransfer())
                .append("recipient", getRecipient())
                .append("transferTime", getTransferTime())
                .toString();
    }
}
