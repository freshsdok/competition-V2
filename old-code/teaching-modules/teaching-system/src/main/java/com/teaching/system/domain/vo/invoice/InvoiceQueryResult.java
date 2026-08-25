package com.teaching.system.domain.vo.invoice;

import lombok.Data;

import java.util.List;

/**
 * 发票查询结果
 */
@Data
public class InvoiceQueryResult {
    /** 发票请求流水号 */
    private String serialNo;
    /** 订单编号 */
    private String orderNo;
    /** 发票状态 */
    private String status;
    /** 发票状态描述 */
    private String statusMsg;
    /** 失败原因 */
    private String failCause;
    /** 发票pdf地址 */
    private String pdfUrl;
    /** 发票图片地址 */
    private String pictureUrl;
    /** 开票时间 */
    private Long invoiceTime;
    /** 发票代码 */
    private String invoiceCode;
    /** 发票号码 */
    private String invoiceNo;
    /** 数电票号码 */
    private String allElectronicInvoiceNumber;
    /** 不含税金额 */
    private String exTaxAmount;
    /** 合计税额 */
    private String taxAmount;
    /** 价税合计 */
    private String orderAmount;
    /** 购方名称 */
    private String payerName;
    /** 购方税号 */
    private String payerTaxNo;
    /** 购方地址 */
    private String address;
    /** 购方电话 */
    private String telephone;
    /** 购方开户行及账号 */
    private String bankAccount;
    /** 发票种类 */
    private String invoiceKind;
    /** 校验码 */
    private String checkCode;
    /** 二维码 */
    private String qrCode;
    /** 税控设备号 */
    private String machineCode;
    /** 发票密文 */
    private String cipherText;
    /** 含底图纸票pdf地址 */
    private String paperPdfUrl;
    /** 发票Ofd地址 */
    private String ofdUrl;
    /** 发票Xml地址 */
    private String xmlUrl;
    /** 开票员 */
    private String clerk;
    /** 收款人 */
    private String payee;
    /** 复核人 */
    private String checker;
    /** 销方银行账号 */
    private String salerAccount;
    /** 销方电话 */
    private String salerTel;
    /** 销方地址 */
    private String salerAddress;
    /** 销方税号 */
    private String salerTaxNum;
    /** 销方名称 */
    private String saleName;
    /** 备注 */
    private String remark;
    /** 成品油标志 */
    private String productOilFlag;
    /** 图片地址 */
    private String imgUrls;
    /** 分机号 */
    private String extensionNumber;
    /** 终端号 */
    private String terminalNumber;
    /** 部门门店Id */
    private String deptld;
    /** 开票员Id */
    private String clerkld;
    /** 对应蓝票发票代码 */
    private String oldInvoiceCode;
    /** 对应蓝票发票号码 */
    private String oldInvoiceNo;
    /** 对应蓝票数电票号码 */
    private String oldEleInvoiceNumber;
    /** 清单标志 */
    private String listFlag;
    /** 清单项目名称 */
    private String listName;
    /** 购方手机 */
    private String phone;
    /** 购方邮箱 */
    private String notifyEmail;
    /** 是否机动车类专票 */
    private String vehicleFlag;
    /** 数据创建时间 */
    private String createTime;
    /** 数据更新时间 */
    private String updateTime;
    /** 发票状态更新时间 */
    private String stateUpdateTime;
    /** 代开标志 */
    private String proxynvoiceFlag;
    /** 用于开票的订单的时间 */
    private String invoiceDate;
    /** 开票类型 */
    private String invoiceType;
    /** 冲红原因 */
    private String redReason;
    /** 作废时间 */
    private String invalidTime;
    /** 作废来源 */
    private String invalidSource;
    /** 数电纸票作废原因 */
    private String invalidReason;
    /** 其他作废原因详情 */
    private String specificReason;
    /** 发票特定要素 */
    private String specificFactor;
    /** 数电发票差额征税开具方式 */
    private String invoiceDifferenceType;
    /** 邮箱交付状态 */
    private String emailNotifyStatus;
    /** 手机交付状态 */
    private String phoneNotifyStatus;
    /** 购买方经办人姓名 */
    private String buyerManagerName;
    /** 经办人证件类型 */
    private String managerCardType;
    /** 经办人证件号码 */
    private String managerCardNo;
    /** 业务方向定义字段1 */
    private String bField1;
    /** 业务方向定义字段2 */
    private String bfield2;
    /** 业务方向定义字段3 */
    private String bfield3;
    /** 购买方自然人标志 */
    private String naturalPersonFlag;
    /** 发票明细集合 */
    private List<InvoiceItem> invioetems;

    // getters and setters...

    /**
     * 发票明细
     */
    @Data
    public static class InvoiceItem {
        /** 商品名称 */
        private String itemName;
        /** 简称 */
        private String itemCodeAbb;
        /** 单位 */
        private String itemUnit;
        /** 单价 */
        private String itemPrice;
        /** 税率 */
        private String itemTaxRate;
        /** 数量 */
        private String itemNum;
        /** 金额 */
        private String itemAmount;
        /** 税额 */
        private String itemTaxAmount;
        /** 规格型号 */
        private String itemSpec;
        /** 商品编码 */
        private String itemCode;
        /** 自行编码 */
        private String itemSelfCode;
        /** 含税标识 */
        private String isIncludeTax;
        /** 发票行性质 */
        private String invioetLineProperty;
        /** 零税率标识 */
        private String zeroRateFlag;
        /** 优惠政策名称 */
        private String favouredPolicyName;
        /** 优惠政策标识 */
        private String favouredPolicyFlag;
        /** 扣除额 */
        private String deduction;
        /** 业务方明细自定义字段1 */
        private String dfield1;
        /** 业务方明细自定义字段2 */
        private String dfield2;
        /** 业务方明细自定义字段3 */
        private String dfield3;
        /** 业务方明细自定义字段4 */
        private String dfield4;
        /** 业务方明细自定义字段5 */
        private String dfield5;
        /** 明细序号 */
        private Integer itemindex;
    }
}