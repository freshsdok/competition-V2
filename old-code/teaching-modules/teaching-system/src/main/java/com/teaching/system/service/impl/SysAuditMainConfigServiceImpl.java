package com.teaching.system.service.impl;

import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysAuditConfig;
import com.teaching.system.domain.SysAuditMainConfig;
import com.teaching.system.domain.SysAuditTask;
import com.teaching.system.mapper.SysAuditMainConfigMapper;
import com.teaching.system.mapper.SysAuditTaskMapper;
import com.teaching.system.service.ISysAuditMainConfigService;
import com.teaching.system.service.SysAsyncService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统审核配置Service业务层处理
 *
 * @author teaching
 * @date 2025-10-15
 */
@Service
public class SysAuditMainConfigServiceImpl implements ISysAuditMainConfigService {
    private static final Logger log = LoggerFactory.getLogger(SysAuditMainConfigServiceImpl.class);
    @Autowired
    private SysAuditMainConfigMapper sysAuditMainConfigMapper;
    @Autowired
    private SysAsyncService sysAsyncService;
    @Autowired
    private SysAuditTaskMapper sysAuditTaskMapper;

    /**
     * 查询系统审核配置
     *
     * @param auditId 系统审核配置主键
     * @return 系统审核配置
     */
    @Override
    public SysAuditMainConfig selectSysAuditMainConfigByAuditId(Long auditId) {
        return sysAuditMainConfigMapper.selectSysAuditMainConfigByAuditId(auditId);
    }

    /**
     * 查询系统审核配置列表
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 系统审核配置
     */
    @Override
    public List<SysAuditMainConfig> selectSysAuditMainCofigList(SysAuditMainConfig sysAuditMainConfig) {
        //此方法返回的有正在使用的数量和被引用的次数，可供前台判断是否隐藏删除、修改、关闭按钮
//        return sysAuditMainConfigMapper.selectSysAuditMainConfigListWithOperatingConditions(sysAuditMainConfig);
        return sysAuditMainConfigMapper.selectSysAuditMainConfigList(sysAuditMainConfig);
    }

    /**
     * 新增系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    @Transactional
    @Override
    public int insertSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig) {
        if (sysAuditMainConfig == null) {
            throw new ServiceException("系统审核配置不能为空");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        sysAuditMainConfig.setCreateBy(loginUser.getSysUser().getNickName());
        sysAuditMainConfig.setUserId(loginUser.getUserid());
        sysAuditMainConfig.setOrgId(loginUser.getSysUser().getOrgId());
        sysAuditMainConfig.setCreateTime(DateUtils.getNowDate());
        String auditType = sysAuditMainConfig.getAuditType();
        Long version = sysAuditMainConfigMapper.selectMaxVersionByType(auditType);
        sysAuditMainConfig.setVersion(version == null ? 1L : version + 1L);
        int rows = sysAuditMainConfigMapper.insertSysAuditMainConfig(sysAuditMainConfig);
        insertSysAuditConfig(sysAuditMainConfig);
        return rows;
    }

    /**
     * 复制系统审核配置
     *
     * @param auditId
     * @return
     */
    @Transactional
    @Override
    public int copySysAuditMainConfig(Long auditId) {
        SysAuditMainConfig sysAuditMainConfig = sysAuditMainConfigMapper.selectSysAuditMainConfigByAuditId(auditId);
        sysAuditMainConfig.setAuditId(null);
        sysAuditMainConfig.getSysAuditConfigList().forEach(sysAuditConfig -> {
            sysAuditConfig.setConfigId(null);
        });
        return insertSysAuditMainConfig(sysAuditMainConfig);
    }

    /**
     * 修改系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    @Transactional
    @Override
    public int updateSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig) {
        SysAuditMainConfig sysAuditMain = sysAuditMainConfigMapper.selectSysAuditMainConfigByAuditId(sysAuditMainConfig.getAuditId());
        //如果是启用状态是不能进行修改的
        if (Objects.isNull(sysAuditMain) || "1".equals(sysAuditMain.getIsEnable())) {
            throw new ServiceException("启用状态是不能进行修改");
        }
        if (!isCanAction(new Long[]{sysAuditMain.getAuditId()}, "update")) {
            throw new ServiceException("此流程已被使用不允许修改");
        }
        sysAuditMainConfig.setUpdateTime(DateUtils.getNowDate());
        updateOrInsertSysAuditConfig(sysAuditMainConfig);
        //修改审核配置时刷新当前登录人可审核的流程节点
        sysAsyncService.getCanAuditInfoByLoginUser();
        return sysAuditMainConfigMapper.updateSysAuditMainConfig(sysAuditMainConfig);
    }

    /**
     * 启用或停用某个审核流程
     *
     * @param sysAuditMainConfig
     * @return
     */
    @Override
    public int enableOrDeactivate(SysAuditMainConfig sysAuditMainConfig) {
        Long auditId = sysAuditMainConfig.getAuditId();
        String isEnable = sysAuditMainConfig.getIsEnable();
        if (StringUtils.isBlank(isEnable) || Objects.isNull(auditId)) {
            throw new ServiceException("传入参数错误");
        }
        if ("0".equals(isEnable) && !isCanAction(new Long[]{auditId}, "close")) {
            throw new ServiceException("此流程正在被使用不允许关闭");
        }
        sysAuditMainConfig.setUpdateTime(DateUtils.getNowDate());
        return sysAuditMainConfigMapper.updateSysAuditMainConfig(sysAuditMainConfig);
    }

    /**
     * 批量删除系统审核配置
     *
     * @param auditIds 需要删除的系统审核配置主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteSysAuditMainConfigByAuditIds(Long[] auditIds) {
        if (!isCanAction(auditIds, "del")) {
            throw new ServiceException("此流程已被使用不允许删除");
        }
        sysAuditMainConfigMapper.deleteSysAuditConfigByAuditIds(auditIds);
        return sysAuditMainConfigMapper.deleteSysAuditMainConfigByAuditIds(auditIds);
    }

    /**
     * 删除系统审核配置信息
     *
     * @param auditId 系统审核配置主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteSysAuditMainConfigByAuditId(Long auditId) {
        if (!isCanAction(new Long[]{auditId}, "del")) {
            throw new ServiceException("此流程已被使用不允许删除");
        }
        sysAuditMainConfigMapper.deleteSysAuditConfigByAuditId(auditId);
        return sysAuditMainConfigMapper.deleteSysAuditMainConfigByAuditId(auditId);
    }

    /**
     * 检查审核流程是否可以被操作
     *
     * @param auditIds   审核流程ids
     * @param actionType 操作类型 del删除 update修改 close关闭
     * @return true 可以操作 false 不可以操作
     */
    public Boolean isCanAction(Long[] auditIds, String actionType) {
        //查auditId被使用的task
        List<SysAuditTask> sysAuditTasks = sysAuditTaskMapper.selectSysAuditTaskListByIds(auditIds);
        if ("del".equals(actionType) || "update".equals(actionType)) {
            //不让删除，不让修改
            return CollectionUtils.isEmpty(sysAuditTasks);
        }
        if ("close".equals(actionType)) {
            //从sysAuditTasks中筛查checkStatus是3审核中
            List<SysAuditTask> collect = sysAuditTasks.stream().filter(sysAuditTask -> TdConstants.CHECK_STATUS_SHZ.equals(sysAuditTask.getCheckStatus())).collect(Collectors.toList());
            //不让关闭，不让删除，不让修改
            return CollectionUtils.isEmpty(collect);
        }
        return true;
    }


    /**
     * 新增审核流程环节配置信息
     *
     * @param sysAuditMainConfig 系统审核配置对象
     */
    public void insertSysAuditConfig(SysAuditMainConfig sysAuditMainConfig) {
        List<SysAuditConfig> sysAuditConfigList = sysAuditMainConfig.getSysAuditConfigList();
        if (StringUtils.isNotNull(sysAuditConfigList)) {
            Long auditId = sysAuditMainConfig.getAuditId();
            String createBy = sysAuditMainConfig.getCreateBy();
            Date createTime = sysAuditMainConfig.getCreateTime();
            Long userId = sysAuditMainConfig.getUserId();
            Long orgId = sysAuditMainConfig.getOrgId();
            List<SysAuditConfig> list = new ArrayList<SysAuditConfig>();
            for (SysAuditConfig sysAuditConfig : sysAuditConfigList) {
                sysAuditConfig.setAuditId(auditId);
                sysAuditConfig.setCreateBy(createBy);
                sysAuditConfig.setCreateTime(createTime);
                sysAuditConfig.setUserId(userId);
                sysAuditConfig.setOrgId(orgId);
                list.add(sysAuditConfig);
            }
            if (!list.isEmpty()) {
                sysAuditMainConfigMapper.batchSysAuditConfig(list);
            }
        }
    }

    /**
     * 修改或新增环节配置信息
     * 不支持删除
     *
     * @param sysAuditMainConfig 系统审核配置对象
     */
    public void updateOrInsertSysAuditConfig(SysAuditMainConfig sysAuditMainConfig) {
        List<SysAuditConfig> sysAuditConfigList = sysAuditMainConfig.getSysAuditConfigList();
        if (StringUtils.isNotNull(sysAuditConfigList)) {
            Long auditId = sysAuditMainConfig.getAuditId();
            String createBy = sysAuditMainConfig.getCreateBy();
            Date createTime = sysAuditMainConfig.getCreateTime();
            Long userId = sysAuditMainConfig.getUserId();
            Long orgId = sysAuditMainConfig.getOrgId();
            for (SysAuditConfig sysAuditConfig : sysAuditConfigList) {
                sysAuditConfig.setAuditId(auditId);
                sysAuditConfig.setCreateBy(createBy);
                sysAuditConfig.setCreateTime(createTime);
                sysAuditConfig.setUserId(userId);
                sysAuditConfig.setOrgId(orgId);
                if (Objects.isNull(sysAuditConfig.getConfigId())) {
                    // 新增审核流程环节配置信息
                    sysAuditMainConfigMapper.insertSysAuditConfig(sysAuditConfig);
                } else {
                    // 修改审核流程环节配置信息
                    sysAuditMainConfigMapper.updateSysAuditConfig(sysAuditConfig);
                }
            }
        }
    }

    /**
     * 根据类型 获取最新版本的流程信息
     *
     * @param type
     * @return
     */
    @Override
    public SysAuditMainConfig getNewVersionAuditMainConfigByType(String type) {
        return sysAuditMainConfigMapper.selectNewVersionAuditMainConfigByType(type);
    }
}
