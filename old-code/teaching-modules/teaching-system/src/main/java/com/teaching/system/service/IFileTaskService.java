package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.FileTask;

/**
 * 文件分发任务Service接口
 * 
 * @author teaching
 * @date 2026-01-07
 */
public interface IFileTaskService 
{
    /**
     * 查询文件分发任务
     * 
     * @param id 文件分发任务主键
     * @return 文件分发任务
     */
    public FileTask selectFileTaskById(Long id);

    /**
     * 查询文件分发任务列表
     * 
     * @param fileTask 文件分发任务
     * @return 文件分发任务集合
     */
    public List<FileTask> selectFileTaskList(FileTask fileTask);

    /**
     * 新增文件分发任务
     * 
     * @param fileTask 文件分发任务
     * @return 结果
     */
    public int insertFileTask(FileTask fileTask);

    /**
     * 修改文件分发任务
     * 
     * @param fileTask 文件分发任务
     * @return 结果
     */
    public int updateFileTask(FileTask fileTask);

    public int updateFileTaskStatus(FileTask fileTask);

    /**
     * 批量删除文件分发任务
     * 
     * @param ids 需要删除的文件分发任务主键集合
     * @return 结果
     */
    public int deleteFileTaskByIds(Long[] ids);

    /**
     * 删除文件分发任务信息
     * 
     * @param id 文件分发任务主键
     * @return 结果
     */
    public int deleteFileTaskById(Long id);

    /**
     * 查询文件分发任务统计信息
     * @return
     */
//    public List<Map<String,Object>> selectFileTaskStatisticsByUserId();
}
