package com.teaching.competition.service;


import com.teaching.system.api.domain.ChangeLog;

import java.util.List;

/**
 * 参赛信息变动日志Service接口
 *
 * @author teaching
 * @date 2026-01-28
 */
public interface IChangeLogService {
    /**
     * 查询参赛信息变动日志
     *
     * @param id 参赛信息变动日志主键
     * @return 参赛信息变动日志
     */
    public ChangeLog selectChangeLogById(Long id);

    /**
     * 查询参赛信息变动日志列表
     *
     * @param changeLog 参赛信息变动日志
     * @return 参赛信息变动日志集合
     */
    public List<ChangeLog> selectChangeLogList(ChangeLog changeLog);

    /**
     * 新增参赛信息变动日志
     *
     * @param changeLog 参赛信息变动日志
     * @return 结果
     */
    public int insertChangeLog(ChangeLog changeLog);

    /**
     * 修改参赛信息变动日志
     *
     * @param changeLog 参赛信息变动日志
     * @return 结果
     */
    public int updateChangeLog(ChangeLog changeLog);

    /**
     * 批量删除参赛信息变动日志
     *
     * @param ids 需要删除的参赛信息变动日志主键集合
     * @return 结果
     */
    public int deleteChangeLogByIds(Long[] ids);

    /**
     * 删除参赛信息变动日志信息
     *
     * @param id 参赛信息变动日志主键
     * @return 结果
     */
    public int deleteChangeLogById(Long id);
}
