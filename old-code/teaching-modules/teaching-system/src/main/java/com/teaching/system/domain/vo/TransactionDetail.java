package com.teaching.system.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDetail {
    // 基础信息
    private String storeNo;              // 门店编号
    private String storeName;            // 门店名称
    private String merchantName;         // 商户名称
    private String thirdPartyMerchantNo; // 第三方商户号
    private String merchantNo;           // 商户号
    private String merchantOrderNo;      // 商户订单号
    private String bankFlowNo;           // 银行流水
    private String transactionDate;      // 交易日期
    private String transactionTime;      // 交易时间
    private String productName;          // 商品名称

    // 金额信息
    private BigDecimal transactionAmount;    // 交易金额
    private BigDecimal discountAmount;       // 优惠金额
    private String currency;                 // 交易币种
    private String rate;                     // 费率
    private String paymentBank;              // 付款银行
    private String paymentMethod;            // 支付方式
    private String transactionType;          // 交易类型
    private String transactionStatus;        // 交易状态

    // 钱包信息
    private String payerWalletId;        // 付款钱包id
    private String payerOperator;        // 付款运营机构
    private String merchantWalletId;     // 商户钱包id
    private String payeeOperator;        // 收款运营机构

    // 优惠信息
    private BigDecimal merchantDiscount;     // 商户出资优惠金额
    private BigDecimal issuerDiscount;       // 发卡方出资优惠金额
    private BigDecimal unionPayDiscount;     // 银联出资优惠金额

    // 结算信息
    private BigDecimal settlementAmount;     // 结算金额
    private BigDecimal serviceFee;           // 手续费
    private String thirdPartyOrderNo;        // 第三方订单号
    private BigDecimal enterpriseRedPacket;  // 企业红包金额
    private BigDecimal enterpriseRefund;     // 企业红包退款金额
    private String appId;                    // AppID

    // 清分信息
    private String clearingResult;       // 清分结果
    private String clearingDate;         // 清分日期
    private String clearingAccount;      // 清分账号
    private String billDate;             // 账单日期

    // 退款信息
    private String originalBankFlowNo;   // 原交易银行流水
    private BigDecimal refundAmount;     // 退款金额
    private String refundType;           // 退款类型
    private String originalMerchantOrderNo;  // 原交易商户订单号
    private String refundRemark;         // 退款备注

    // 其他信息
    private String cashier;              // 收银员
    private String terminalType;         // 终端类型
    private String terminalNo;           // 终端号
    private String merchantDataPacket;   // 商户数据包
    private String merchantReserved;     // 商户保留域
    private String payeeRemark;          // 收款方备注
    private String payerRemark;          // 付款方备注
    private String payerInfo;            // 付款人信息
    private String completeDate;         // 完成日期
    private String payerUserName;        // 付款人用户名
    private String accountFlowNo;        // 入账流水
}
