package com.teaching.competition.mapper;


import com.teaching.system.api.domain.ChangeLog;

import java.util.List;


/**
 * 参赛信息变动日志Mapper接口
 *
 * @author teaching
 * @date 2026-01-28
 */
public interface ChangeLogMapper {
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

    public List<ChangeLog> selectChangeLogListByTeamCode(String teamCode);

    public List<ChangeLog> selectChangeLogListByMemberId(Long memberId);

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
     * 删除参赛信息变动日志
     *
     * @param id 参赛信息变动日志主键
     * @return 结果
     */
    public int deleteChangeLogById(Long id);

    /**
     * 批量删除参赛信息变动日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteChangeLogByIds(Long[] ids);
}
