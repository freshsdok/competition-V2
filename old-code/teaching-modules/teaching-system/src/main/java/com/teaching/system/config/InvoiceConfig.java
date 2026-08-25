package com.teaching.system.config;

import com.teaching.common.core.utils.uuid.UUID;
import com.teaching.system.domain.MerchantParamConfig;
import lombok.Data;
import nuonuo.open.sdk.NNOpenSDK;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class InvoiceConfig {

    @Value("${invoice.url}")
    private String URL;

    // 申请开票方法名
    @Value("${invoice.applyMethod}")
    private String APPLY_METHOD;

    private String deliveryMethod = "nuonuo.OpeMplatform.deliveryInvoice";

    private String queryMethod = "nuonuo.OpeMplatform.queryInvoiceResult";

    private String reInvoiceMethod = "nuonuo.OpeMplatform.reInvoice";

    //开票申请回调
    @Value("${invoice.callBack}")
    private String CALLBACK;

    /*@Value("${invoice.accessToken}")
    private String ACCESS_TOKEN;

    @Value("${invoice.appkey}")
    private String APP_KEY;

    @Value("${invoice.appSecret}")
    private String APP_SECRET;

    *//**
     * 税号
     *//*
    @Value("${invoice.taxNum}")
    private String TAX_NUM;

    *//**
     *税率
     *//*
    @Value("${invoice.taxRate}")
    private String TAX_RATE;

    // 开票人
    @Value("${invoice.clerk}")
    private String CLERK;

    *//**
     * 核查人
     *//*
    @Value("${invoice.checker}")
    private String CHECKER;

    // 银行账户信息
    @Value("${invoice.bank}")
    private String BANK;

    @Value("${invoice.account}")
    private String ACCOUNT;

    @Value("${invoice.address}")
    private String ADDRESS;

    *//**
     * 分机号
     *//*
    @Value("${invoice.extension}")
    private String EXTENSION;*/

    /**
     * @param method API方法名
     * @param content API私有请求参数, 标准JSON格式
     * @return
     */
    public String requestApi(String method, String content, String token, String callBack, MerchantParamConfig config) {
        // 参数配置
        String taxnum = config.getTaxNum();//授权企业税号, 商户填""
        String appKey = config.getInvoiceAppKey();
        String appSecret = config.getInvoiceAppSecret();
        String url = URL;// 票据识别请使用https://sdk.nuonuo.com/open/v2/ocr
        String senid = UUID.randomUUID().toString().replace("-", ""); // 唯一标识，由企业自己生成32位随机码

        // 接口调用
        NNOpenSDK sdk = NNOpenSDK.getIntance();
        String json = sdk.sendPostSyncRequest(url, senid, appKey, appSecret, token, taxnum, method, content);

        // 响应报文解析
        System.out.println("\n\n\n【API请求】");
        System.out.println(json);
        return json;
    }


    /**
     * 商户获取授权码(发票系统内部使用)
     *
     * 辅助页面 https://open.nuonuo.com/#/dev-doc/auth-business
     */
    /*public String getMerchantToken() {
        // 参数配置
        String appKey = APP_KEY;
        String appSecret = APP_SECRET;

        // 接口调用
        NNOpenSDK sdk = NNOpenSDK.getIntance();
        String json = sdk.getMerchantToken(appKey, appSecret);

        // 响应报文解析
        System.out.println("\n\n\n【商户获取Token】");
        System.out.println(json);
        return json;
    }*/


    /**
     * ISV获取授权码
     *
     * 辅助页面 https://open.nuonuo.com/#/dev-doc/auth-service
     */
    /*public String getISVToken(String code, String redirect_uri) {
        // 参数配置
        String appKey = APP_KEY;
        String appSecret = APP_SECRET;
        //String code = "临时授权码，请求authorize时返回的code";
        String taxnum = TAX_NUM;
        //String redirect_uri = "回调地址，必传且不能为空";

        // 接口调用
        NNOpenSDK sdk = NNOpenSDK.getIntance();
        String json = sdk.getISVToken(appKey, appSecret, code, taxnum, redirect_uri);

        // 响应报文解析
        System.out.println("\n\n\n【ISV获取Token】");
        System.out.println(json);
        return json;
    }*/

    /**
     * ISV刷新授权码
     *
     * 辅助页面 https://open.nuonuo.com/#/dev-doc/auth-service
     */
    /*public String refreshISVToken(String refreshToken, String userId) {
        // 参数配置
        //String refreshToken = "刷新令牌,由接口getISVToken返回";
        //String userId = "获取access_token时授权商户的userId";
        String appSecret = APP_SECRET;

        // 接口调用
        NNOpenSDK sdk = NNOpenSDK.getIntance();
        String json = sdk.refreshISVToken(refreshToken, userId, appSecret);

        // 响应报文解析
        System.out.println("\n\n\n【ISV刷新Token】");
        System.out.println(json);
        return json;
    }*/
}
