package com.teaching.system.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteAuditLogService;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.domain.SysConfig;
import com.teaching.system.mapper.SysConfigMapper;
import com.teaching.system.service.ISysConfigService;

/**
 * 参数配置 服务层实现
 * 
 * @author teaching
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private RemoteAuditLogService remoteAuditLogService;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init()
    {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
        String configValue = Convert.toStr(redisService.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue))
        {
            return configValue;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisService.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
            // 记录配置新增审计日志
            recordConfigAudit("新增系统配置", "新增系统配置参数", null, config, "新增");
        }
        return row;
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        SysConfig temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey()))
        {
            redisService.deleteObject(getCacheKey(temp.getConfigKey()));
        }

        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
            // 记录配置修改审计日志
            recordConfigAudit("修改系统配置", "修改系统配置参数", temp, config, "修改");
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            redisService.deleteObject(getCacheKey(config.getConfigKey()));
            // 记录配置删除审计日志
            recordConfigAudit("删除系统配置", "删除系统配置参数", config, null, "删除");
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisService.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        Collection<String> keys = redisService.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisService.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }

    /**
     * 记录系统配置审计日志
     * 
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param oldConfig 修改前的配置
     * @param newConfig 修改后的配置
     * @param operationType 操作类型
     */
    private void recordConfigAudit(String eventName, String eventDesc, SysConfig oldConfig, SysConfig newConfig, String operationType)
    {
        try
        {
            if (remoteAuditLogService == null)
            {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("配置审计");
            auditLog.setAuditCategory("配置变更");
            auditLog.setEventName(eventName);
            auditLog.setEventDesc(eventDesc);

            // 用户信息
            try
            {
                Long userId = SecurityUtils.getUserId();
                String username = SecurityUtils.getUsername();
                auditLog.setUserId(userId);
                auditLog.setUserName(username);
            }
            catch (Exception e)
            {
                // 获取用户信息失败时继续执行
            }

            // 操作信息
            auditLog.setOperationType(operationType);
            auditLog.setOperationModule("参数配置");
            
            // 获取请求信息
            HttpServletRequest request = getRequest();
            if (request != null)
            {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());

            // 数据变更信息
            SysConfig config = newConfig != null ? newConfig : oldConfig;
            if (config != null)
            {
                auditLog.setDataId(String.valueOf(config.getConfigId()));
                auditLog.setDataType("系统配置");
                
                // 记录变更前后的配置值
                if (oldConfig != null)
                {
                    auditLog.setOldValue(buildConfigDesc(oldConfig));
                }
                if (newConfig != null)
                {
                    auditLog.setNewValue(buildConfigDesc(newConfig));
                }
            }

            // 风险级别 - 系统配置修改属于中等风险
            auditLog.setRiskLevel("MEDIUM");

            // 审计状态
            auditLog.setAuditStatus("0"); // 待审计
            auditLog.setIsAbnormal("0");
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            // 记录审计日志失败不影响主业务
        }
    }

    /**
     * 构建配置描述
     * 
     * @param config 配置信息
     * @return 配置描述
     */
    private String buildConfigDesc(SysConfig config)
    {
        if (config == null)
        {
            return "";
        }
        
        return JSON.toJSONString(new Object() {
            public final String configKey = config.getConfigKey();
            public final String configName = config.getConfigName();
            public final String configValue = config.getConfigValue();
            public final String configType = config.getConfigType();
        });
    }

    /**
     * 获取当前HTTP请求
     * 
     * @return HttpServletRequest
     */
    private HttpServletRequest getRequest()
    {
        try
        {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
