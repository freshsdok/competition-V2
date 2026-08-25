package com.teaching.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.domain.MerchantParamConfig;
import com.teaching.system.mapper.MerchantParamConfigMapper;
import com.teaching.system.service.IMerchantParamConfigService;
import com.teaching.system.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import utils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.teaching.common.core.constant.PayConstant.*;

/**
 * 支付相关接口实现
 */
@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    //收款码申请请求地址
    @Value("${pay.qrcodeUrl}")
    private String qrcodeUrl;

    //订单二维码请求地址
    @Value("${pay.orderQrcodeUrl}")
    private String orderQrcodeUrl;

    //关闭订单
    @Value("${pay.closeOrderUrl}")
    private String closeOrderUrl;

    //回调地址
    @Value("${pay.notifyUrl}")
    private String notifyUrl;

    //退款申请
    @Value("${pay.refundUrl}")
    private String refundUrl;

    //退款回调地址
    @Value("${pay.refundNotifyUrl}")
    private String refundNotifyUrl;

    //支付结果查询
    @Value("${pay.queryOrderUrl}")
    private String queryOrderUrl;

    //退款结果查询
    @Value("${pay.refundQueryUrl}")
    private String refundQueryUrl;

    //小程序下单
    @Value("${pay.wxMiniOrderUrl}")
    private String wxMiniOrderUrl;

    //对账单地址获取
    @Value("${pay.statementUrl}")
    private String statementUrl;

    /*@Value("${pay.appId}")
    private String appId;

    @Value("${pay.appSecret}")
    private String appSecret;

    //私钥
    @Value("${pay.privateKey}")
    private String privateKey;

    //公钥
    @Value("${pay.publicKey}")
    private String publicKey;

    //商户号
    @Value("${pay.memId}")
    private String memId;

    //收银员
    @Value("${pay.userId}")
    private String userId;

    //终端号
    @Value("${pay.termId}")
    private String termId;*/

    //超时时长
    @Value("${pay.payValidTime}")
    private String payValidTime;

    @Autowired
    private IMerchantParamConfigService merchantParamConfigService;


    @Override
    public Map<String,String> applyOrderQrCode(OrderInfo orderInfo) {
        MerchantParamConfig config = null;
        if (StringUtil.isNotEmpty(orderInfo.getMerId())) {
            //存在merId，说明是老订单，使用merId查询订单对应的商户配置信息
            config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        }else{
            //读取商户配置信息--没有merId，就是新订单，使用用商品类型和赛事id获取配置
            config = merchantParamConfigService.getConfig(orderInfo.getCommodityType(), orderInfo.getCompetitionSeriesId());
        }
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> requestMap = buildQrcodeRequestParam(orderInfo, config);
        String signResult= signMethod(requestMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(orderQrcodeUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response), config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","获取订单二维码请求异常:"+e.getMessage());
            return resultMap;
        }

    }

    /**
     * 获取订单二维码请求入参
     * @param orderInfo
     * @return
     */
    private Map<String, String> buildQrcodeRequestParam(OrderInfo orderInfo,MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("body", orderInfo.getCommodityName());   //订单描述
        requestTransactionParams.put("currencyCode", CURRENCY_CODE_VALUE);    //交易币种，默认156，目前只支持人民币（156）
        requestTransactionParams.put("merId", config.getMerId());   //商户号(必传)
        requestTransactionParams.put("notifyUrl", notifyUrl);  //交易通知地址(必传)
        requestTransactionParams.put("orderId", orderInfo.getOrderId()); //商户订单号(必传)
        requestTransactionParams.put("payValidTime", payValidTime); //支付有效时间
        requestTransactionParams.put("termId", config.getTermId());  //终端号
        requestTransactionParams.put("txnAmt", orderInfo.getAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString());  //交易金额,单位为分(必传)
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        //requestTransactionParams.put("tradeScene", TRADE_SCENE_VALUE);   //交易场景-线上
        return requestTransactionParams;
    }

    public String signMethod(Map<String, String> requestParams,MerchantParamConfig config){
        Map<String, String> requestPublicParams = new TreeMap<>();
        String requestStr = null;
        try {
            //公共请求参数
            requestPublicParams.put(VERSION, VERSION_VALUE);    //版本号，固定为0.0.1(必传字段)
            requestPublicParams.put(ENCODING, ENCODING_VALUE);   //编码方式，固定为UTF-8(必传)
            requestPublicParams.put(SIGN_METHOD, SIGN_METHOD_VALUE);    //签名方法，固定为02，表示签名方式为国密(必传)

            ObjectMapper mapper = new ObjectMapper();
            requestPublicParams.put(BIZ_CONTENT, mapper.writeValueAsString(requestParams));
            System.out.println("加签前的报文内容：" + mapper.writeValueAsString(requestPublicParams));

            //对待加签内容进行排序拼接
            String signContent= SignatureUtil.getSignContent(requestPublicParams);
            //加签
            requestPublicParams.put(SIGN, PaySM2Util.sm2Sign(signContent, config.getPayPrivateKey()));

            requestStr = mapper.writeValueAsString(requestPublicParams);

            System.out.println("加签后的报文内容：" + requestStr);
            return requestStr;

        }catch (Exception e){
            System.out.println("加签发生异常！");
            e.printStackTrace();
            return requestStr;
        }
    }

    public String getSign(Map<String, String> requestParams,MerchantParamConfig config){
        Map<String, String> requestPublicParams = new TreeMap<>();
        try {
            //公共请求参数
            requestPublicParams.put(VERSION, VERSION_VALUE);    //版本号，固定为0.0.1(必传字段)
            requestPublicParams.put(ENCODING, ENCODING_VALUE);   //编码方式，固定为UTF-8(必传)
            requestPublicParams.put(SIGN_METHOD, SIGN_METHOD_VALUE);    //签名方法，固定为02，表示签名方式为国密(必传)

            ObjectMapper mapper = new ObjectMapper();
            requestPublicParams.put(BIZ_CONTENT, mapper.writeValueAsString(requestParams));
            System.out.println("加签前的报文内容：" + mapper.writeValueAsString(requestPublicParams));

            //对待加签内容进行排序拼接
            String signContent= SignatureUtil.getSignContent(requestPublicParams);
            //获取签名
            String sign = PaySM2Util.sm2Sign(signContent, config.getPayPrivateKey());

            System.out.println("签名内容：" + sign);
            return sign;

        }catch (Exception e){
            System.out.println("生成签名发生异常！");
            e.printStackTrace();
            return null;
        }
    }



    public Boolean checkSign(String string,MerchantParamConfig config){
        System.out.println("要验签的报文内容：" + string);
        try {
            //验签
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseBodyMap = objectMapper.readValue(string, Map.class);
            String sign = responseBodyMap.remove(SIGN);
            String contentStr = SignatureUtil.getSignContent(responseBodyMap);
            boolean result = PaySM2Util.sm2Check(contentStr,sign, config.getPayPublicKey());

            if (result) {
                System.out.println("报文验签成功!");
            } else {
                System.out.println("报文验签失败!");
            }
            return result;
        }catch (Exception e){
            System.out.println("验签发生异常！");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, String> closeOrder(OrderInfo orderInfo) {
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        if (config == null) {
            throw new ServiceException("没有查询到该订单的商户配置信息，请联系管理员配置！");
        }
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> stringStringMap = buildCloseOrderRequestParam(orderInfo,config);
        String signResult= signMethod(stringStringMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(closeOrderUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response),config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","关闭订单请求异常:"+e.getMessage());
            return resultMap;
        }
    }


    /**
     * 获取订单二维码请求入参
     * @param orderInfo
     * @return
     */
    private Map<String, String> buildCloseOrderRequestParam(OrderInfo orderInfo,MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("origOrderId", orderInfo.getOrderId()); //原交易商户订单号
        requestTransactionParams.put("merId", config.getMerId());
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        return requestTransactionParams;
    }

    @Override
    public Map<String, String> refundOrder(OrderInfo orderInfo) {
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        if (config == null) {
            throw new ServiceException("没有查询到该订单的商户配置信息，请联系管理员配置！");
        }
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> stringStringMap = buildRefundOrderRequestParam(orderInfo,config);
        String signResult= signMethod(stringStringMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(refundUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response),config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","退款请求异常:"+e.getMessage());
            return resultMap;
        }
    }


    private Map<String, String> buildRefundOrderRequestParam(OrderInfo orderInfo,MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("orderId",orderInfo.getRefundOrderId()); //退款订单号
        requestTransactionParams.put("origOrderId", orderInfo.getOrderId()); //原交易商户订单号
        requestTransactionParams.put("merId", config.getMerId());     //商户号
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        requestTransactionParams.put("notifyUrl", refundNotifyUrl); //回调地址
        requestTransactionParams.put("txnAmt",orderInfo.getAmount().multiply(new BigDecimal(100)).setScale(0,RoundingMode.HALF_UP).toString()); //原交易金额
        requestTransactionParams.put("refundAmt", orderInfo.getAmount().multiply(new BigDecimal(100)).setScale(0,RoundingMode.HALF_UP).toString());  //退款金额
        return requestTransactionParams;

    }

    @Override
    public Map<String, String> queryOrder(OrderInfo orderInfo) {
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> stringStringMap = buildQueryOrderRequestParam(orderInfo,config);
        String signResult= signMethod(stringStringMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(queryOrderUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response),config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","订单查询请求异常:"+e.getMessage());
            return resultMap;
        }
    }

    private Map<String, String> buildQueryOrderRequestParam(OrderInfo orderInfo,MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("orderId",orderInfo.getOrderId()); //订单号
        requestTransactionParams.put("merId", config.getMerId());     //商户号
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        return requestTransactionParams;
    }

    @Override
    public Map<String, String> queryRefund(OrderInfo orderInfo) {
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> stringStringMap = buildQueryRefundRequestParam(orderInfo,config);
        String signResult= signMethod(stringStringMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(refundQueryUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response),config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","退款查询请求异常:"+e.getMessage());
            return resultMap;
        }
    }

    private Map<String, String> buildQueryRefundRequestParam(OrderInfo orderInfo,MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("orderId",orderInfo.getRefundOrderId()); //退款订单号
        requestTransactionParams.put("merId", config.getMerId());     //商户号
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        return requestTransactionParams;
    }

    @Override
    public Map<String, String> wxMiniCreateOrder(OrderInfo orderInfo,String ip) {
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> requestMap = buildWxMiniRequestParam(orderInfo,ip,config);
        String signResult= signMethod(requestMap,config);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", config.getPayAppId());
            apiSign.put("secret", config.getPayAppSecret());
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", config.getPayAppId());
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(wxMiniOrderUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response),config);
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","微信小程序下单请求异常:"+e.getMessage());
            return resultMap;
        }
    }


    private Map<String, String> buildWxMiniRequestParam(OrderInfo orderInfo,String ip, MerchantParamConfig config) {
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("body", orderInfo.getCommodityName());   //订单描述
        requestTransactionParams.put("currencyCode", CURRENCY_CODE_VALUE);    //交易币种，默认156，目前只支持人民币（156）
        requestTransactionParams.put("merId", config.getMerId());   //商户号(必传)
        requestTransactionParams.put("notifyUrl", notifyUrl);  //交易通知地址(必传)
        requestTransactionParams.put("orderId", orderInfo.getOrderId()); //商户订单号(必传)
        requestTransactionParams.put("spbillCreateIp", ip);  //终端IP,用户端IP
        requestTransactionParams.put("txnAmt", orderInfo.getAmount().multiply(new BigDecimal(100)).toString());  //交易金额,单位为分(必传)
        requestTransactionParams.put("userId", config.getFeeUserId());   //收银员
        requestTransactionParams.put("tradeType", "JSAPI");   //交易类型-小程序支付
        return requestTransactionParams;
    }


    /**
     * 接口废弃
     * @param billDate
     * @return
     */
    /*@Override
    public Map<String, String> statementUrl(String billDate) {
        Map<String,String> resultMap = new HashMap<>();
        // 组装requestBody并加签
        Map<String, String> requestMap = buildStatementRequestParam(billDate);
        String signResult= signMethod(requestMap);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> signResultMap = mapper.readValue(signResult, Map.class);

            long currentTimeMills = System.currentTimeMillis() / 1000;

            // 组apiSign加密Map
            Map<String,String> apiSign = new TreeMap<>();
            apiSign.put("appid", appId);
            apiSign.put("secret", appSecret);
            apiSign.put("sign", signResultMap.get("sign"));
            apiSign.put("timestamp", "" + currentTimeMills);

            // MD5加密
            String MD5Content = SignatureUtil.getSignContent(apiSign);
            String apiSignString = MD5Utils.getMD5Content(MD5Content).toLowerCase();

            // 组request头部Map
            Map<String, String> apiHeader = new HashMap<>();
            apiHeader.put("appid", appId);
            apiHeader.put("timestamp", "" + currentTimeMills);
            apiHeader.put("apisign", apiSignString);

            // 发送HTTP post请求
            Map<String,String> response = Utils.postForEntity(statementUrl, signResult, apiHeader);

            System.out.println(mapper.writeValueAsString(response));
            // 返回结果验签
            Boolean checkResult1 = checkSign(mapper.writeValueAsString(response));
            if(!checkResult1){
                resultMap.put("code",FAIL);
                resultMap.put("msg","验签失败");
                return resultMap;
            }

            String returnCode = response.get("returnCode");
            String respCode = response.get("respCode");
            //返回结果成功
            if (SUCCESS.equals(returnCode) && SUCCESS.equals(respCode)) {
                String bizContent = response.get(BIZ_CONTENT);
                resultMap.put("code",SUCCESS);
                resultMap.put("data",bizContent);
            }else {
                resultMap.put("code",FAIL);
                resultMap.put("msg",response.get("respMsg"));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code",FAIL);
            resultMap.put("msg","获取账单下载地址请求异常:"+e.getMessage());
            return resultMap;
        }
    }*/

    /*Map<String,String> buildStatementRequestParam(String billDate){
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("merId", memId);   //商户号(必传)
        requestTransactionParams.put("billDate", billDate);   //账单日期
        //requestTransactionParams.put("billType", "zip");   //账单类型 下载打包对账单须上送：zip，不上送此字段默认下载Excel格式账单
        return requestTransactionParams;
    }*/

    @Override
    public Map<String, String> statementDownloadUrl(String billDate,MerchantParamConfig config) {
        Map<String,String> resultMap = new HashMap<>();

        //获取10位时间戳
        String currentTimeMills = System.currentTimeMillis() / 1000 + "";
        String verify = "SM3withSM2";
        String channel = "AP";
        String funcCode = "BILLRECORD_GET_FORAPI";
        String sysCode = "AP";

        //加签字符串拼接
        /**
         * 对账单apisign为"appid=value&secret=value&sign=value&timestamp=value"拼接字符串后进行的SM3withSM2签名值
         * 加签参考行外示例
         * https://openapi.cmbchina.com/docs/cwuhAAVrK2un/G4UBagaH76oq
         * 10.2.2 工具类里面的signHexBySm3WithSm2
         */
        StringBuilder builder = new StringBuilder();
        builder.append("appid=").append(config.getPayAppId()).append("&");
        builder.append("secret=").append(config.getPayAppSecret()).append("&");
        builder.append("sign=").append("abcd").append("&");
        builder.append("timestamp=").append(currentTimeMills);

        //发送post请求
        RestTemplate restTemplate = new RestTemplate();
        //添加头信息
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("appid",config.getPayAppId());
        httpHeaders.add("timestamp", currentTimeMills);
        try {
            httpHeaders.add("apisign", OpenApiSmUtil.signHexBySm3WithSm2(config.getPayPrivateKey(),builder.toString()));
        } catch (Exception e) {
            throw new RuntimeException("请求头加签失败！",e);
        }
        httpHeaders.add("verify", verify);
        httpHeaders.add("channel", channel);
        httpHeaders.add("funcCode", funcCode);
        httpHeaders.add("sysCode", sysCode);
        httpHeaders.add("sign","abcd");
        //请求体
        Map<String, String> requestMap = buildStatementDownloadRequestParam(billDate,config);
        Map<String, Map<String, String>> requestBody = new HashMap<>();
        requestBody.put("requestBody", requestMap);
        // 创建请求实体
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        // 发送请求
        ResponseEntity<String> response1 = restTemplate.exchange(
                statementUrl, HttpMethod.POST, requestEntity, String.class);

        System.out.println("接口返回参数：" + response1.getBody());
        String responseBodyStr = response1.getBody();
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> map = null;
        try {
            map = mapper.readValue(responseBodyStr, Map.class);
            String codeValue = map.get("code");
            if(!"SUC000000".equals(codeValue)){
                resultMap.put("code",FAIL);
                resultMap.put("msg",map.get("message"));
                return resultMap;
            }else {
                Object result = map.get("data");
                LinkedHashMap<?, ?> dataMap = new LinkedHashMap<>();
                if (result instanceof LinkedHashMap) {
                    // 根据你的业务需求处理 LinkedHashMap
                    dataMap = (LinkedHashMap<?, ?>) result;
                }
                /**
                 * settleStatus取值S则表示均已清分、取值为F则表示未清分、取值为T则表示部分清分；
                 */
                String settleStatus = dataMap.get("settleStatus").toString();
                if("S".equals(settleStatus)){
                    resultMap.put("code",SUCCESS);
                    resultMap.put("data",dataMap.get("downloadUrl").toString());
                    return resultMap;
                }else {
                    resultMap.put("code",FAIL);
                    resultMap.put("msg","对账单未清分");
                    log.error(billDate+"日的对账数据未完成清分！");
                    return resultMap;
                }
            }
        } catch (Exception e) {
            log.error("返回结果参数解析异常："+responseBodyStr,e);
            throw new RuntimeException("返回结果参数解析异常:"+responseBodyStr);
        }
    }
    Map<String,String> buildStatementDownloadRequestParam(String billDate,MerchantParamConfig config){
        //业务要素
        Map<String, String> requestTransactionParams = new HashMap<>();
        requestTransactionParams.put("merchantNo", config.getMerId());   //商户号(必传)
        requestTransactionParams.put("billDate", billDate);   //账单日期
        requestTransactionParams.put("billType", "JH_JZ");   //账单类型 账单类型，填JH_JZ 普通聚合商户JH_JZ
        return requestTransactionParams;
    }
}
