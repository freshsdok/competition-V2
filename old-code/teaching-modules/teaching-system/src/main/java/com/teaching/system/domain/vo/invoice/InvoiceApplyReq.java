package com.teaching.system.domain.vo.invoice;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceApplyReq {

    /**
     * 订单id
     */
    private Long id;

    private String merId;

    // 个人/企业
    private String invoiceClass;

    // 抬头(企业信息/个人姓名)
    private String buyerName;

    // 税号
    private String buyerTaxNumber;

    // 邮箱
    private String email;

    // 手机号
    private String phone;

    // 备注信息
    private String remark;

    // 发票类型 1付款 2退费
    private String invoiceType;

    // 物品单位
    private String commodityUnit;

    /**
     * 发票种类
     */
    private String invoiceLine;

    /**
     * 商品编码（服务尼尔）-开票内容-(商品编码&费用类型)
     */
    private String goodsCode;

    /**
     * 开票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 本次开票用户
     */
    private List<Long> userIds;

    /**
     * 本次开票关联订单
     */
    private List<Long> orderIds;

    /**
     * 前端生成uuid作为本次申请发票的订单号（后端生成，如果重复提交，容易多开票）
     */
    private String randomId;

    /**
     * 商品类型
     */
    private String commodityType;
}
