package com.teaching.wxApp.service.impl;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteUserService;
import com.teaching.wxApp.domain.WxQcCodeConfig;
import com.teaching.wxApp.mapper.WxQcCodeConfigMapper;
import com.teaching.wxApp.service.IWxQcCodeConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 二维码配置Service业务层处理
 *
 * @author teaching
 * @date 2026-04-08
 */
@Service
public class WxQcCodeConfigServiceImpl implements IWxQcCodeConfigService {

    @Autowired
    private WxQcCodeConfigMapper wxQcCodeConfigMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RemoteUserService remoteUserService;

    /**
     * 查询二维码配置
     *
     * @param codeConfigId 二维码配置主键
     * @return 二维码配置
     */
    @Override
    public WxQcCodeConfig selectWxQcCodeConfigByCodeConfigId(Long codeConfigId) {
        return wxQcCodeConfigMapper.selectWxQcCodeConfigByCodeConfigId(codeConfigId);
    }

    /**
     * 查询二维码配置列表
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 二维码配置
     */
    @Override
    public List<WxQcCodeConfig> selectWxQcCodeConfigList(WxQcCodeConfig wxQcCodeConfig) {
        List<WxQcCodeConfig> wxQcCodeConfigs = wxQcCodeConfigMapper.selectWxQcCodeConfigList(wxQcCodeConfig);
        if (CollectionUtils.isNotEmpty(wxQcCodeConfigs)) {
            wxQcCodeConfigs.forEach(item -> {
                if(StringUtils.isNotBlank(item.getUserGroupIds())){
                    List<String> ids = List.of(item.getUserGroupIds().split(","));
                    List<Long> userGroupIds = ids.stream().map(Long::parseLong).toList();
                    //对userGroupIds进行升序排序
                    R<String> groupNames = remoteUserService.getGroupNames(userGroupIds, SecurityConstants.INNER);
                    if (R.isSuccess(groupNames) ) {
                        item.setUserGroupNames(groupNames.getData());
                    }
                }
            });
        }
        return wxQcCodeConfigs;
    }

    /**
     * 新增二维码配置
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 结果
     */
    @Override
    public int insertWxQcCodeConfig(WxQcCodeConfig wxQcCodeConfig) {
        wxQcCodeConfig.setCreateTime(DateUtils.getNowDate());
        wxQcCodeConfig.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        return wxQcCodeConfigMapper.insertWxQcCodeConfig(wxQcCodeConfig);
    }

    /**
     * 修改二维码配置
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 结果
     */
    @Override
    public int updateWxQcCodeConfig(WxQcCodeConfig wxQcCodeConfig) {
        Long codeConfigId = wxQcCodeConfig.getCodeConfigId();
        redisService.deleteObject(SecurityConstants.WX_CODE_CONFIG + codeConfigId);
        redisService.setCacheObject(SecurityConstants.WX_CODE_CONFIG + codeConfigId, wxQcCodeConfig);
        //修改提示语时校验提示语不能为空
        if(wxQcCodeConfig.getMsgFlag()){
            clearRelatedRecords(codeConfigId);
            String examinationHallPromise = wxQcCodeConfig.getExaminationHallPromise();
            String examinationHallRuler = wxQcCodeConfig.getExaminationHallRuler();
            String improperDesc = wxQcCodeConfig.getImproperDesc();
            String improperTitle = wxQcCodeConfig.getImproperTitle();
            //去掉sucessHintDesc和sucessHintTitle的非空校验，因为前端已隐藏配置
//            String successHintDesc = wxQcCodeConfig.getSuccessHintDesc();
//            String successHintTitle = wxQcCodeConfig.getSuccessHintTitle();
            if(StringUtils.isBlank(examinationHallPromise)||StringUtils.isBlank(examinationHallRuler)||
                    StringUtils.isBlank(improperDesc)||StringUtils.isBlank(improperTitle)){
                throw new GlobalException("提示语配置不能为空");
            }
        }
        wxQcCodeConfig.setUpdateTime(DateUtils.getNowDate());
        wxQcCodeConfig.setUpdateBy(SecurityUtils.getLoginUser().getUsername());
        return wxQcCodeConfigMapper.updateWxQcCodeConfig(wxQcCodeConfig);
    }

    /**
     * 清除相关记录缓存
     */
    private void clearRelatedRecords(Long codeConfigId) {
        Collection<String> keysToDelete = new ArrayList<>();
        // 使用keys()查找所有相关记录，然后批量删除
        redisService.keys(SecurityConstants.WX_QC_CODE_RECORD + "*").forEach(key -> {
            Map<String, Object> recordAndConfig = redisService.getCacheObject(key);
            if (recordAndConfig != null) {
                Long configId = MapUtil.getLong(recordAndConfig, "codeConfigId");
                if (codeConfigId.equals(configId)) {
                    keysToDelete.add(key);
                }
            }
        });
        // 批量删除相关记录
        if (!keysToDelete.isEmpty()) {
            redisService.deleteObject(keysToDelete);
        }
    }

    /**
     * 批量删除二维码配置
     *
     * @param codeConfigIds 需要删除的二维码配置主键
     * @return 结果
     */
    @Override
    public int deleteWxQcCodeConfigByCodeConfigIds(Long[] codeConfigIds) {
        // 批量删除Redis缓存
        List<String> keysToDelete = new ArrayList<>();
        for (Long codeConfigId : codeConfigIds) {
            keysToDelete.add(SecurityConstants.WX_CODE_CONFIG + codeConfigId);
            clearRelatedRecords(codeConfigId);
        }
        redisService.deleteObject(keysToDelete);
        return wxQcCodeConfigMapper.deleteWxQcCodeConfigByCodeConfigIds(codeConfigIds);
    }

    /**
     * 删除二维码配置信息
     *
     * @param codeConfigId 二维码配置主键
     * @return 结果
     */
    @Override
    public int deleteWxQcCodeConfigByCodeConfigId(Long codeConfigId) {
        redisService.deleteObject(SecurityConstants.WX_CODE_CONFIG + codeConfigId);
        return wxQcCodeConfigMapper.deleteWxQcCodeConfigByCodeConfigId(codeConfigId);
    }
}
