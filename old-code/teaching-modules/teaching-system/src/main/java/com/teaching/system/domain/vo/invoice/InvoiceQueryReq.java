package com.teaching.system.domain.vo.invoice;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceQueryReq {

    /**
     * 发票流水号，两字段二选一，同时存在以流水号为准（最多查50个订单号）
     */
    private List<String> serialNos;

    /**
     * 订单编号（最多查50个订单号）
     */
    private List<String> orderNos;

    /**
     * 是否需要提供明细 1-是, 0-否(不填默认 0)
     */
    private String isOfferInvoiceDetail;
}
