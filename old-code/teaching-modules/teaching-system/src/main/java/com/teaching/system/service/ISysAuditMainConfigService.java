package com.teaching.system.service;

import com.teaching.system.domain.SysAuditMainConfig;

import java.util.List;

/**
 * 系统审核配置Service接口
 *
 * @author teaching
 * @date 2025-10-15
 */
public interface ISysAuditMainConfigService {
    /**
     * 查询系统审核配置
     *
     * @param auditId 系统审核配置主键
     * @return 系统审核配置
     */
    public SysAuditMainConfig selectSysAuditMainConfigByAuditId(Long auditId);

    /**
     * 查询系统审核配置列表
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 系统审核配置集合
     */
    public List<SysAuditMainConfig> selectSysAuditMainCofigList(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 新增系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    public int insertSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 复制系统审核配置
     *
     * @param auditId 被复制的流程主键
     * @return 结果
     */
    public int copySysAuditMainConfig(Long auditId);

    /**
     * 修改系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    public int updateSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 启用停用系统审核流程配置
     *
     * @param sysAuditMainConfig 包含主键id和状态
     * @return 结果
     */
    public int enableOrDeactivate(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 批量删除系统审核配置
     *
     * @param auditIds 需要删除的系统审核配置主键集合
     * @return 结果
     */
    public int deleteSysAuditMainConfigByAuditIds(Long[] auditIds);

    /**
     * 删除系统审核配置信息
     *
     * @param auditId 系统审核配置主键
     * @return 结果
     */
    public int deleteSysAuditMainConfigByAuditId(Long auditId);

    /**
     * 根据类型 获取最新版本的流程信息 开启的不开启的都算
     *
     * @param type 流程类型
     * @return 新增版本的流程信息
     */
    public SysAuditMainConfig getNewVersionAuditMainConfigByType(String type);

}
