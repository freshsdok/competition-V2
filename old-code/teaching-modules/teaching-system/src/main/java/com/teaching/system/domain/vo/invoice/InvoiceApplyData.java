package com.teaching.system.domain.vo.invoice;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceApplyData {

    // 购方信息
    /** 购方名称（企业名称/个人）最大长度100 */
    private String buyerName;
    /** 购方税号（企业要填，个人可为空；专票、二手车销售统一发票时必填）最大长度20 */
    private String buyerTaxNum;
    /** 购方电话（购方地址+电话总共不超过100字符；二手车销售统一发票时必填）最大长度50 */
    private String buyerTel;
    /** 购方地址（购方地址+电话总共不超过100字符；二手车销售统一发票时必填）最大长度80 */
    private String buyerAddress;
    /** 购方银行开户行及账号 最大长度100 */
    private String buyerAccount;
    /** 购方手机（pushMode为1或2时，此项为必填）最大长度20 */
    private String buyerPhone;
    /** 购买方经办人姓名（数电票管理）最大长度16 */
    private String buyerManagerName;
    /** 经办人证件类型 最大长度40 */
    private String managerCardType;
    /** 经办人证件号码 最大长度20 */
    private String managerCardNo;
    /** 购买方自然人标志：0-否（默认），1-是 最大长度1 */
    private String naturalPersonFlag;

    // 销方信息
    /** 销方税号（使用外籍环境请求时填写3399029999997789113）最大长度20 */
    private String salerTaxNum;
    /** 销方电话（在诺诺工作台配置过的可以不传，以传入的为准）最大长度20 */
    private String salerTel;
    /** 销方地址（在诺诺工作台配置过的可以不传，以传入的为准）最大长度80 */
    private String salerAddress;
    /** 销方银行开户行及账号(二手车销售统一发票时必填) 最大长度100 */
    private String salerAccount;
    /** 销方自然人标志（针对数电二手车发票）0-否（默认），1-是 最大长度1 */
    private String sellerNaturalPersonFlag;

    // 发票基本信息
    /** 订单号（每个企业唯一）最大长度64 */
    private String orderNo;
    /** 订单时间 格式：yyyy-MM-dd HH:mm:ss 最大长度20 */
    private String invoiceDate;
    /** 冲红时填写的对应蓝票发票代码（红票必填 10位或12位，11位时请左补0）最大长度12 */
    private String invoiceCode;
    /** 冲红时填写的对应蓝票发票号码（红票必填，不需5位请左补0）最大长度8 */
    private String invoiceNum;
    /** 冲红原因：1-开票有误 2-销货退回 3-服务中止 4-销售折让（开具红票时且票种为p,c,f需要传）最大长度1 */
    private String redReason;
    /** 红字信息表编号，每票冲红时此项必填 最大长度24 */
    private String billinfoNo;
    /** 开票类型：1-蓝票 2-红票（数电票冲红请对接数电快捷冲红接口）最大长度1 */
    private String invoiceType;
    /** 发票种类 最大长度2 */
    private String invoiceLine;
    /** 数电票类型（数电票时有需要）最大长度20 */
    private String paperinvoiceType;
    /** 特定要素：0普通发票（默认）、1成品油、2稀土等 最大长度2 */
    private String specificFactor;
    /** 是否强制开具标识：0否、1是 最大长度2 */
    private String forceFlag;
    /** 代开标志：0非代开;1代开 最大长度1 */
    private String proxyInvoiceFlag;
    /** 代办退税标记：0否（默认）；1是 最大长度1 */
    private String taxRebateProxy;
    /** 数电发票差额征税开具方式：01全额开票，02差额开票 最大长度2 */
    private String invoiceDifferenceType;
    /** 回传发票信息地址（开票完成、开票失败） */
    private String callBackUrl;
    /** 分机号（只能为空或者数字）最大长度5 */
    private String extensionNumber;
    /** 终端号（开票终端号，只能为空或数字）最大长度4 */
    private String terminalNumber;
    /** 机器编号（12位盘号）最大长度12 */
    private String machineCode;
    /** 是否机动车类专票0-否1是（此字段仅针对税控发票）最大长度1 */
    private String vehicleFlag;
    /** 是否隐藏编码表版本号0-否1是（默认）最大长度1 */
    private String hiddenBmbbbh;
    /** 指定发票代码（票种为01，税控或数电时允许指定卷开具）最大长度12 */
    private String nextInvoiceCode;
    /** 发票起始号码，当指定代码有效时，发票起始号码必填 最大长度8 */
    private String nextInvoiceNum;
    /** 发票终止号码，当指定代码有效时，发票终止号码必填 最大长度8 */
    private String invoiceNumEnd;

    // 发票显示配置
    /** 开户行及账号显示类型：0都不显示；1仅显示销方；2仅显示购方；3都显示（数电票生效）最大长度2 */
    private String showBankAccountType;
    /** 地址电话显示类型：0都不显示；1仅显示销方；2仅显示购方；3都显示（数电票生效）最大长度2 */
    private String showAddressTelType;
    /** 数电票是否显示收款人和复核人，0：不显示 1：显示 最大长度2 */
    private String showCheckerType;

    // 人员信息
    /** 开票员（数电票时需要和开票登录账号对应的开票员姓名）最大长度20 */
    private String clerk;
    /** 开票员Id（诺诺系统中的id）最大长度32 */
    private String clerkId;
    /** 复核人（数电票时若有值，会显示在生成的PDF/OFD备注栏中）最大长度20 */
    private String checker;
    /** 收款人（数电票时若有值，会显示在生成的PDF/OFD备注栏中）最大长度20 */
    private String payee;
    /** 部门门店Id（诺诺系统中的id）最大长度32 */
    private String departmentId;

    // 清单相关
    /** 清单标志：非清单0；清单1，默认0，电票固定为0 最大长度1 */
    private String listFlag;
    /** 清单项目名称：对应发票票面项目名称（listFlag为1时，必填）最大长度92 */
    private String listName;

    // 推送信息
    /** 推送方式：-1不推送 0邮箱 1手机（默认）;2邮箱、手机 最大长度2 */
    private String pushMode;
    /** 推送邮箱（pushMode为0或2时，此项为必填）最大长度50 */
    private String email;
    /** 抄送手机，多个时用英文逗号隔开，最多支持5个 最大长度100 */
    private String ccPhone;
    /** 抄送邮箱，多个时用英文逗号隔开，最多支持5个 最大长度100 */
    private String ccEmail;

    // 备注及其他
    /** 备注信息 最大长度230 */
    private String remark;
    /** 减按征收类型。枚举值为03（销售使用过的固定资产）、05（房屋租赁）最大长度2 */
    private String taxReductionFlag;
    /** 3%、1%税率开具理由 最大长度1 */
    private String surveyAnswerType;
    /** 对购买方税号校验（0-不校验 1-校验，仅对数电票有效）最大长度1 */
    private String taxNumVerifyFlag;
    /** 对购买方名称校验（0-不校验 1-校验，仅对数电票有效）最大长度1 */
    private String naturalPersonVerifyFlag;
    /** 商品匹配规则，默认按照商品名称+规格型号匹配；传入1调取数据后名称匹配 最大长度1 */
    private String isignoreType;

    // 业务自定义字段
    /** 业务方向定义字段1，本应用仅作保存 最大长度255 */
    private String bfield1;
    /** 业务方向定义字段2，本应用仅作保存 最大长度255 */
    private String bfield2;
    /** 业务方向定义字段3，本应用仅作保存 最大长度255 */
    private String bfield3;

    // 发票明细
    /** 发票明细，支持填写商品明细最大2000行（包含折扣行、被折扣行） */
    private List<InvoiceDetail> invoiceDetail;

}
