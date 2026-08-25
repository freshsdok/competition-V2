package com.teaching.common.core.constant;

public class PayConstant {

    public static final String VERSION = "version";

    public static final String ENCODING = "encoding";

    public static final String SIGN = "sign";

    //验签方式
    public static final String SIGN_METHOD = "signMethod";

    public static final String VERSION_VALUE = "0.0.1";

    public static final String ENCODING_VALUE = "UTF-8";

    //加密方式-国密SM2
    public static final String SIGN_METHOD_VALUE = "02";

    //成功是返回的内容json字符串
    public static final String BIZ_CONTENT = "biz_content";

    public static final String SUCCESS = "SUCCESS";

    public static final String FAIL = "FAIL";

    //交易币种，默认156，目前只支持人民币（156）
    public static final String CURRENCY_CODE_VALUE = "156";

    //交易场景，线上-ONLINE
    public static final String TRADE_SCENE_VALUE = "ONLINE";

    //二维码链接地址
    public static final String QR_CODE = "qrCode";

    //银行订单id
    public static final String CMB_ORDER_ID = "cmbOrderId";

    //交易时间
    public static final String TXN_TIME = "txnTime";

}
