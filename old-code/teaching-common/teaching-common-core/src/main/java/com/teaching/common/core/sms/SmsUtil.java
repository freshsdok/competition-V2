package com.teaching.common.core.sms;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.SpringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信
 *
 * @author Administrator
 */
public class SmsUtil {

    /**
     * 发送验证码
     *
     * @param phone 手机号码
     * @return
     */
    public static R<Map<String, Object>> sendVerificationCode(String phone) {
        SmsConfigInfo smsConfigInfo = SpringUtils.getBean(SmsConfigInfo.class);
        Client smsClient = SpringUtils.getBean(Client.class);
        try {
            String code = generateCode(6);

            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsConfigInfo.getSignName())
                    .setTemplateCode(smsConfigInfo.getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = smsClient.sendSmsWithOptions(request, new RuntimeOptions());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("requestId", response.getBody().getRequestId());
            result.put("bizId", response.getBody().getBizId());
            result.put("verificationCode", code);

            return R.ok(result);

        } catch (Exception e) {
            return R.fail("短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送短信通知
     *
     * @param phone 手机号码
     * @return
     */
    public static R<Map<String, Object>> sendNoticeCode(String templateCode,String phone,Map<String, Object> smsParam) {
        SmsConfigInfo smsConfigInfo = SpringUtils.getBean(SmsConfigInfo.class);
        Client smsClient = SpringUtils.getBean(Client.class);
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsConfigInfo.getSignName())
                    .setTemplateCode(templateCode)
                    .setTemplateParam(JSONObject.toJSONString(smsParam));

            SendSmsResponse response = smsClient.sendSmsWithOptions(request, new RuntimeOptions());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("requestId", response.getBody().getRequestId());
            result.put("bizId", response.getBody().getBizId());
            return R.ok(result);

        } catch (Exception e) {
            return R.fail("短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 生成验证码
     *
     * @param length 验证码长度
     * @return
     */
    private static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }
}
