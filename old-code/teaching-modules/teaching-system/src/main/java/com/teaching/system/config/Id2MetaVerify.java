package com.teaching.system.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.cloudauth20190307.Client;
import com.aliyun.cloudauth20190307.models.Id2MetaVerifyRequest;
import com.aliyun.cloudauth20190307.models.Id2MetaVerifyResponse;
import com.aliyun.credentials.models.Config;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.teaching.common.core.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import utils.MD5Utils;

import java.util.*;

/**
 * 实名认证类
 *
 * @author Administrator
 */
//@Component
public class Id2MetaVerify {
    @Value("${sms.ali.access-key-id}")
    private String accessKeyId;
    @Value("${sms.ali.access-key-secret}")
    private String accessKeySecret;
    @Value("${sms.ali.endpoints}")
    private String endpoints;


    /**
     * 实名认证服务调用入口
     *
     * @param userName     真实姓名
     * @param identifyNum  身份证号
     * @param isEncryption 是否加密
     * @return code:200接口成功 result:1认证成功 result:2认证失败
     * @throws Exception
     */
    public Map<String, Object> id2MetaVerify(String userName, String identifyNum, Boolean isEncryption) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", "500");
        result.put("result", "2");
        try {
            Id2MetaVerifyRequest request = new Id2MetaVerifyRequest();
            request.paramType = isEncryption ? "md5" : "normal";
            request.userName = isEncryption ? MD5Utils.userNameMd5(userName) : userName;
            request.identifyNum = isEncryption ? MD5Utils.identifyNumMd5(identifyNum) : identifyNum;
            // 自动路由服务。
            Id2MetaVerifyResponse response = id2MetaVerifyAutoRoute(request);
            if (response != null) {
                Map<String, Object> map = Common.toMap(response.getBody());
                String ret = Common.toJSONString(map);
                com.aliyun.teaconsole.Client.log("最终结果（若此处为空，则所有服务点均异常，请逐步调试）：" + ret + "");
                if ("200".equals(MapUtils.getString(map, "Code"))) {
                    JSONObject fastJsonObj = JSON.parseObject(ret);
                    String string = fastJsonObj.getJSONObject("ResultObject").getString("BizCode");
                    System.out.println("二要素实名认证结果：" + ("1".equals(string) ? "认证成功" : "认证失败"));
                    result.put("code", 200);
                    result.put("result", string);
                }
            }
        } catch (TeaException error) {
            com.aliyun.teaconsole.Client.error(error.message);
        } catch (Exception error) {
            TeaException err = new TeaException(error.getMessage(), error);
            com.aliyun.teaconsole.Client.error(err.message);
        }
        return result;
    }


    /**
     * <b>description</b> :
     * <p>主备服务点循环调用，获取到成功结果返回。</p>
     */
    private Id2MetaVerifyResponse id2MetaVerifyAutoRoute(Id2MetaVerifyRequest request) throws Exception {
        List<String> endpointList = new ArrayList<>();
        if (StringUtils.isBlank(endpoints)) {
            endpointList = Arrays.asList(
                    "cloudauth.cn-shanghai.aliyuncs.com",
                    "cloudauth.cn-beijing.aliyuncs.com",
                    "cloudauth.aliyuncs.com"
            );
        }
        String[] split = endpoints.split(",");
        //把split数组转成list集合
        endpointList = Arrays.asList(split);
        Id2MetaVerifyResponse lastResponse = null;
        for (String endpoint : endpointList) {
            try {
                // 调用服务。
                Id2MetaVerifyResponse response = id2MetaVerify(endpoint, request);
                // 节点调用结果
                String ret = com.aliyun.teautil.Common.toJSONString(com.aliyun.teautil.Common.toMap(response));
                com.aliyun.teaconsole.Client.log("节点 " + endpoint + " 结果：" + ret + " ");
                // 有一个服务调用成功即返回。
                if (!com.aliyun.teautil.Common.isUnset(response) && com.aliyun.teautil.Common.equalNumber(response.statusCode, 200)) {
                    if (!com.aliyun.teautil.Common.isUnset(response.body) && com.aliyun.teautil.Common.equalString(response.body.code, "200")) {
                        lastResponse = response;
                        break;
                    }

                }

            } catch (TeaException error) {
                com.aliyun.teaconsole.Client.error("节点 " + endpoint + " 调用异常：" + error.message + "");
            } catch (Exception _error) {
                TeaException error = new TeaException(_error.getMessage(), _error);
                com.aliyun.teaconsole.Client.error("节点 " + endpoint + " 调用异常：" + error.message + "");
            }
        }
        return lastResponse;
    }

    /**
     * <b>description</b> :
     * <p>获取服务Client实例，调用验证方法。</p>
     */
    private Id2MetaVerifyResponse id2MetaVerify(String endpoint, com.aliyun.cloudauth20190307.models.Id2MetaVerifyRequest request) throws Exception {
        // 获取SDK Client实例。
        Client client = createClient(endpoint);
        // 构建RuntimeObject
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        runtime.readTimeout = 5000;
        runtime.connectTimeout = 5000;
        // 连接
        return client.id2MetaVerifyWithOptions(request, runtime);
    }

    /**
     * <b>description</b> :
     * <p>安全创建服务Client实例。</p>
     */
    private Client createClient(String endpoint) throws Exception {
        // 获取Credential工具，此工具会从环境变量中读取AccessKey。
        Config credentialConfig = new Config();
        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client(credentialConfig);
        // 创建SDK Client实例。
        com.aliyun.teaopenapi.models.Config apiConfig = new com.aliyun.teaopenapi.models.Config();
        apiConfig.credential = credential;
        apiConfig.endpoint = endpoint;
        apiConfig.setAccessKeySecret(accessKeySecret);
        apiConfig.setAccessKeyId(accessKeyId);
        return new Client(apiConfig);
    }

}
