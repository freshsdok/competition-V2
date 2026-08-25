package com.teaching.common.core.constant;

public class DictConstant {

    //支付状态--字典定义
    public static final String PAY_STATUS = "pay_status";  //支付成功
    //支付状态-值
    public static final String PENDING = "pending";  //待支付
    public static final String PAYING = "paying";  //支付中
    public static final String APPROVING = "approving";  //审批中
    public static final String APPROVE_REJECTED = "approve_rejected";  //审批不通过
    public static final String PAID = "paid";  //已支付
    public static final String CANCELLED = "cancelled";  //已取消
    public static final String FAILED = "failed";  //支付失败

    //退费类型
    public static final String REFUNDING = "refunding";  //退款中
    public static final String REFUNDED = "refunded";  //已退款
    public static final String REPAY_REFUNDING = "repay_refunding";  //重缴费退款中
    public static final String REPAY_REFUNDED = "repay_refunded";  //重缴费已退款
    public static final String REFUND_CANCELED = "refund_canceled";  //取消退费流程(审核不通过)


    //开票状态（0、1、2 订单中的发票状态和发票管理中的发票状态通用）
    public static final String INVOICE_PENDING = "0";  //待开票
    public static final String INVOICE_SUCCESS = "1";  //开票成功
    public static final String INVOICE_FAILED = "2";  //开票失败
    //发票管理中的发票状态
    public static final String INVOICE_SIGN_FAILED = "3";  //开票成功，签章失败
    //订单管理中的发票状态
    public static final String INVOICE_NOT_APPLY = "3";  //未开票

    //支付形式，线上支付
    public static final String ONLINE = "online"; //支付形式字典
    //支付形式，线下转账
    public static final String OFFLINE = "offline";

    //商品单位字典
    public static final String COMMODITY_UNIT = "commodity_unit";
    //商品单位字典-赛事
    public static final String COMPETITION = "competition";

    //商品编号字典
    public static final String INVOICE_GOODS_CODE = "invoice_goods_code";

    //收费类型字典
    public static final String FEE_TYPE = "fee_type";

    //开票种类 1-个人，2-企业
    public static final String INVOICE_CLASS_PERSONAL = "1";

    //文件导出 0-导出中
    public static final String EXPORTING = "0";
    //文件导出 1-导出完成
    public static final String EXPORTED = "1";
    //文件导出 2-导出失败
    public static final String EXPORT_FAILED = "2";


    /**
     * 订单类型 pay-支付订单 refund-退款订单
     */
    public static final String ORDER_TYPE_PAY = "pay";
    public static final String ORDER_TYPE_REFUND = "refund";

    /**
     * 报名调整类型 change-变更 repayment-退费重交
     */
    public static final String REPAYMENT = "repayment";  //退费重交
    public static final String CHANGE = "change";  //变更
    public static final String RETIRED = "retired";  //退赛
}
