package com.teaching.wxApp.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.wxApp.domain.WxQcCodeConfig;
import com.teaching.wxApp.mapper.WxQcCodeConfigMapper;
import com.teaching.wxApp.service.WxQcCodeRecordUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class WxQcCodeRecordUserServiceImpl implements WxQcCodeRecordUserService {

    @Autowired
    private WxQcCodeConfigMapper wxQcCodeConfigMapper;

    @Autowired
    private RemoteUserService userService;
    @Autowired
    private RedisService redisService;

    @Override
    public List<WxQcCodeConfig> selectWxQcCodeConfigList(WxQcCodeConfig wxQcCodeConfig) {
        // 获取当前用户身份
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfo = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        if(R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData())){
            SysUser data = userCenterInfo.getData();
            List<IdentityInfo> identityInfoList = data.getIdentityInfoList();
            if(CollectionUtils.isNotEmpty(identityInfoList)){
                List<String> certificationType = identityInfoList.stream().map(IdentityInfo::getCertificationType).toList();
                if(certificationType.contains(Constants.IDENTITY_TYPE_TEACHER)){
                    return wxQcCodeConfigMapper.selectWxQcCodeConfigList(wxQcCodeConfig);
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * 按用户组查询二维码生成记录用户列表
     * @param wxQcCodeConfig
     * @return
     */
    @Override
    public List<WxQcCodeConfig> selectWxQcCodeConfigPcList(WxQcCodeConfig wxQcCodeConfig) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Set<Long> userGroupIdList = redisService.getCacheObject("userGroup:info:"+userId);
        if(CollectionUtils.isNotEmpty(userGroupIdList)){
            List<String> groupList = userGroupIdList.stream().map(String::valueOf).toList();
            wxQcCodeConfig.setGroupIds(groupList);
        }
        return wxQcCodeConfigMapper.selectWxQcCodeConfigPcList(wxQcCodeConfig);
    }
}
