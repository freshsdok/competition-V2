package com.teaching.system.service;

import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.domain.MerchantParamConfig;

import java.util.Map;

/**
 * 支付相关接口定义
 */
public interface IPayService {

    /**
     * 验签
     * @param string
     * @return
     */
    Boolean checkSign(String string,MerchantParamConfig config);

    /**
     * 获取签名
     */
    String getSign(Map<String, String> requestParams,MerchantParamConfig config);

    /**
     * 订单二维码申请
     */
    Map<String,String> applyOrderQrCode(OrderInfo orderInfo);

    /**
     * 关闭订单
     */
    Map<String,String> closeOrder(OrderInfo orderInfo);

    /**
     * 退款操作
     */
    Map<String,String> refundOrder(OrderInfo orderInfo);

    /**
     * 支付结果查询
     */
    Map<String,String> queryOrder(OrderInfo orderInfo);

    /**
     * 退款结果查询
     */
    Map<String,String> queryRefund(OrderInfo orderInfo);

    Map<String, String> wxMiniCreateOrder(OrderInfo orderInfo,String ip);

    /**
     * 接口废弃
     * @param billDate
     * @return
     */
//    Map<String,String> statementUrl(String billDate);

    Map<String,String> statementDownloadUrl(String billDate, MerchantParamConfig config);
}
