package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.FileTask;

/**
 * 文件分发任务Mapper接口
 *
 * @author teaching
 * @date 2026-01-07
 */
public interface FileTaskMapper
{
    /**
     * 查询文件分发任务
     *
     * @param id 文件分发任务主键
     * @return 文件分发任务
     */
    public FileTask selectFileTaskById(Long id);

    public List<FileTask> selectFileTaskByIds(Long[] ids);

    public FileTask selectFileTaskByTaskId(Long taskId);

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

    /**
     * 删除文件分发任务
     *
     * @param id 文件分发任务主键
     * @return 结果
     */
    public int deleteFileTaskById(Long id);

    /**
     * 批量删除文件分发任务
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFileTaskByIds(Long[] ids);

    /**
     * 根据用户组id查询任务id
     * @param ids
     * @return
     */
    public Long selectTaskByUserGroupIds(Long[] ids);
}
