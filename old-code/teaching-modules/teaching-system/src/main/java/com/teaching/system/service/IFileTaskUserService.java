package com.teaching.system.service;

import com.teaching.system.domain.FileTask;

import java.util.List;

public interface IFileTaskUserService {

    /**
     * 查询文件分发任务列表
     *
     * @param fileTask 文件分发任务
     * @return 文件分发任务集合
     */
    public List<FileTask> selectFileTaskUserList(FileTask fileTask);
}
