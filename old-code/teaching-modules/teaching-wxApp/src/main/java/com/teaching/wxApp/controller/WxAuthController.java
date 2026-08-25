package com.teaching.wxApp.controller;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.wxApp.domain.WxLoginDTO;
import com.teaching.wxApp.domain.WxQcCodeConfig;
import com.teaching.wxApp.service.IWxQcCodeConfigService;
import com.teaching.wxApp.service.IWxQcCodeRecordService;
import com.teaching.wxApp.service.WxAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录接口
 *
 * @author teaching
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/wxAuth")
public class WxAuthController extends BaseController {

    @Autowired
    private WxAuthService wxAuthService;
    @Autowired
    private IWxQcCodeRecordService wxQcCodeRecordService;
    @Autowired
    private IWxQcCodeConfigService wxQcCodeConfigService;
    @Autowired
    private RedisService redisService;

    @PostMapping("/wx-login")
    public AjaxResult wxLogin(HttpServletRequest request, @RequestBody WxLoginDTO wxLoginDTO) {
        String code = wxLoginDTO.getCode();
        String encryptedData = wxLoginDTO.getEncryptedData();
        String iv = wxLoginDTO.getIv();
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(encryptedData) || StringUtils.isEmpty(iv)) {
            return error("参数错误，请检查请求内容");
        }
        String token = wxAuthService.wxLogin(wxLoginDTO);
        Map<String, String> result = Collections.singletonMap("token", token);
        return success(result);
    }

    /**
     * 扫描
     * @param request
     * @param params
     * @return
     */
    @Log(title = "微信扫码", businessType = BusinessType.OTHER)
    @PostMapping("/wx-scanCode")
    public AjaxResult scanCode(HttpServletRequest request,@RequestBody Map<String, String> params) {
        return success(wxQcCodeRecordService.scanCode(params));
    }

    /**
     * 扫码重试 查询
     * @param request
     * @param params
     * @return
     */
    @Log(title = "微信扫码重试", businessType = BusinessType.OTHER)
    @PostMapping("/wx-retry")
    public AjaxResult retry(HttpServletRequest request,@RequestBody Map<String, String> params) {
//        return success("签到成功");
        return success(wxQcCodeRecordService.retry(params));
    }

    /**
     * 微信个人信息
     * @param request
     * @param params
     * @return
     */
    @Log(title = "微信我的信息", businessType = BusinessType.OTHER)
    @PostMapping("/wx-info")
    public AjaxResult weChatMyInfo(HttpServletRequest request, @RequestBody(required = false) Map<String, String> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }
        return success(wxQcCodeRecordService.weChatMyInfo(params));
    }

    /**
     * 获取考场规则，考生承诺
     * @param request
     * @param params
     * @return
     */
    @PostMapping("/wx-ruler")
    public AjaxResult getRuler(HttpServletRequest request,@RequestBody Map<String, String> params) {
        Long configId = MapUtil.getLong(params, "configId");
        if(configId == null){
            return error("参数错误，请检查请求内容");
        }
        //规则，承诺查缓存
        WxQcCodeConfig cacheData = redisService.getCacheObject(SecurityConstants.WX_CODE_CONFIG + configId);
        if(cacheData == null){
            cacheData = wxQcCodeConfigService.selectWxQcCodeConfigByCodeConfigId(configId);
            if(cacheData != null){
                redisService.setCacheObject(SecurityConstants.WX_CODE_CONFIG + configId, cacheData);
            }
        }
        Map<String,Object> result = MapUtil.newHashMap();
        result.put("examinationHallRuler",cacheData.getExaminationHallRuler());
        result.put("examinationHallPromise",cacheData.getExaminationHallPromise());
        return success(result);
    }
}
