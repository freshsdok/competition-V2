package com.teaching.system.domain.vo.invoice;

import lombok.Data;

import java.util.Date;

/**
 * 发票审请回调content内容
 */
@Data
public class ApplyCallBackContent {
    /**
     * 发票状态（1：开票完成、2：开票失败、3：开票成功签章失败）
     * 注：企业资质-开票失败是否回调为"是"时返回该字段
     */
    private String cStatus;

    /**
     * 开票日期
     */
    private Date cKprq;

    /**
     * 发票代码（全电电票时为空，全电纸票时有值）
     */
    private String cFpdm;

    /**
     * 发票号码（全电电票时为空，全电纸票时有值）
     */
    private String cFphm;

    /**
     * 全电发票号码（全电发票（电票+纸票）时有值）
     */
    private String allElectronicInvoiceNumber;

    /**
     * 红票对应的蓝票发票代码（蓝票为全电电票时为空，全电纸票时有值）
     */
    private String cYfpdm;

    /**
     * 红票对应的蓝票发票号码（蓝票为全电电票时为空，全电纸票时有值）
     */
    private String cYfphm;

    /**
     * 红票对应的蓝票的全电发票号码（蓝票为全电发票（电票+纸票）时返回）
     */
    private String oldEleInvoiceNumber;

    /**
     * 价税合计（保留小数点后2位）
     */
    private String cHjse;

    /**
     * 不含税金额（保留小数点后2位）
     */
    private String cBhsje;

    /**
     * 订单号
     */
    private String cOrderno;

    /**
     * 发票流水号
     */
    private String cFpqqlsh;

    /**
     * 失败原因（只有发票状态c_status为2或3时返回）
     */
    private String cErrorMessage;

    /**
     * 发票PDF地址
     * 注：若同时返回了ofdUrl与pdfUrl，则pdf文件不能做为原始凭证，请用ofd文件做为原始凭证
     */
    private String cUrl;

    /**
     * 发票详情地址
     */
    private String cJpgUrl;

    /**
     * 购方邮箱
     */
    private String email;

    /**
     * 购方手机
     */
    private String phone;
}
