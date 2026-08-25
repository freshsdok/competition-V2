package com.teaching.system.mapper;

import com.teaching.system.domain.SysAuditConfig;
import com.teaching.system.domain.SysAuditMainConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统审核配置Mapper接口
 *
 * @author teaching
 * @date 2025-10-15
 */
@Mapper
public interface SysAuditMainConfigMapper {
    /**
     * 查询系统审核配置
     *
     * @param auditId 系统审核配置主键
     * @return 系统审核配置
     */
    public SysAuditMainConfig selectSysAuditMainConfigByAuditId(Long auditId);

    /**
     * 查找最大版本号根据类型
     *
     * @param auditIdType
     * @return
     */
    public Long selectMaxVersionByType(String auditIdType);

    /**
     * 查询系统审核配置列表
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 系统审核配置集合
     */
    public List<SysAuditMainConfig> selectSysAuditMainConfigList(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 查询系统审核配置列表 带操作判断条件
     * @param sysAuditMainConfig
     * @return
     */
    public List<SysAuditMainConfig> selectSysAuditMainConfigListWithOperatingConditions(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 根据类型 获取最新版本的流程信息  启用的不启用的都算
     *
     * @param type
     * @return
     */
    public SysAuditMainConfig selectNewVersionAuditMainConfigByType(String type);

    /**
     * 新增系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    public int insertSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 修改系统审核配置
     *
     * @param sysAuditMainConfig 系统审核配置
     * @return 结果
     */
    public int updateSysAuditMainConfig(SysAuditMainConfig sysAuditMainConfig);

    /**
     * 修改审核流程环节配置信息
     *
     * @param sysAuditConfig
     * @return
     */
    public int updateSysAuditConfig(SysAuditConfig sysAuditConfig);

    /**
     * 删除系统审核配置
     *
     * @param auditId 系统审核配置主键
     * @return 结果
     */
    public int deleteSysAuditMainConfigByAuditId(Long auditId);

    /**
     * 批量删除系统审核配置
     *
     * @param auditIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysAuditMainConfigByAuditIds(Long[] auditIds);

    /**
     * 批量删除审核流程环节配置
     *
     * @param auditIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysAuditConfigByAuditIds(Long[] auditIds);

    /**
     * 批量新增审核流程环节配置
     *
     * @param sysAuditConfigList 审核流程环节配置列表
     * @return 结果
     */
    public int batchSysAuditConfig(List<SysAuditConfig> sysAuditConfigList);

    /**
     * 新增审核流程环节配置信息
     *
     * @param sysAuditConfigList
     * @return
     */
    public int insertSysAuditConfig(SysAuditConfig sysAuditConfigList);


    /**
     * 通过系统审核配置主键删除审核流程环节配置信息
     *
     * @param auditId 系统审核配置ID
     * @return 结果
     */
    public int deleteSysAuditConfigByAuditId(Long auditId);
}
