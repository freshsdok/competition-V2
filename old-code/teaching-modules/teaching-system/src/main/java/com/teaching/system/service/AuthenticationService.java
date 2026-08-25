package com.teaching.system.service;

import com.alibaba.fastjson.JSONObject;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.redis.service.RedisService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import utils.HttpUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${auth.host}")
    private String host;

    @Value("${auth.path}")
    private String path;

    @Value("${auth.appcode}")
    private String appcode;

    @Autowired
    private RedisService redisService;

    public Map<String, Object> authentication(String realName, String idCard) {
        if (redisService.hasKey("idCard:" + realName + idCard)) {
            return redisService.getCacheObject("idCard:" + realName + idCard);
        }
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("cardNo", idCard.trim());
        bodys.put("realName", realName.trim());
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            //获取response的body
            String resTr = EntityUtils.toString(response.getEntity());
            Map<String, Object> resultMap = JSONObject.parseObject(resTr, Map.class);
            logger.info("实名认证结果:" + resultMap);
            Map<String, Object> result = JSONObject.parseObject(resultMap.get("result").toString(), Map.class);
            if (Boolean.parseBoolean(String.valueOf(result.get("isok")))) {
                redisService.setCacheObject("idCard:" + realName + idCard, result);
            } else {
                redisService.deleteObject("idCard:" + realName + idCard);
            }
            return result;
        } catch (Exception e) {
            logger.info("实名认证失败:" + e);
            redisService.deleteObject("idCard:" + realName + idCard);
            throw new GlobalException("认证失败");
        }
    }
}
