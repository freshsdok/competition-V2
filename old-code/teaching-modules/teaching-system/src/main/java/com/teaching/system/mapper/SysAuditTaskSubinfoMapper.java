package com.teaching.system.mapper;

import java.util.List;

import com.teaching.system.domain.SysAuditTaskSubinfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核任务审核信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-16
 */
@Mapper
public interface SysAuditTaskSubinfoMapper {
    /**
     * 查询审核任务审核信息
     *
     * @param subId 审核任务审核信息主键
     * @return 审核任务审核信息
     */
    public SysAuditTaskSubinfo selectSysAuditTaskSubinfoBySubId(Long subId);

    /**
     * 查询审核任务审核信息列表
     *
     * @param sysAuditTaskSubinfo 审核任务审核信息
     * @return 审核任务审核信息集合
     */
    public List<SysAuditTaskSubinfo> selectSysAuditTaskSubinfoList(SysAuditTaskSubinfo sysAuditTaskSubinfo);

    /**
     * 新增审核任务审核信息
     *
     * @param sysAuditTaskSubinfo 审核任务审核信息
     * @return 结果
     */
    public int insertSysAuditTaskSubinfo(SysAuditTaskSubinfo sysAuditTaskSubinfo);

    /**
     * 修改审核任务审核信息
     *
     * @param sysAuditTaskSubinfo 审核任务审核信息
     * @return 结果
     */
    public int updateSysAuditTaskSubinfo(SysAuditTaskSubinfo sysAuditTaskSubinfo);

    /**
     * 删除审核任务审核信息
     *
     * @param subId 审核任务审核信息主键
     * @return 结果
     */
    public int deleteSysAuditTaskSubinfoBySubId(Long subId);

    /**
     * 批量删除审核任务审核信息
     *
     * @param subIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysAuditTaskSubinfoBySubIds(Long[] subIds);
}
