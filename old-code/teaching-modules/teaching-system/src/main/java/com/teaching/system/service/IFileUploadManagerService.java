package com.teaching.system.service;

import java.util.List;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.FileReviewImportSource;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.FileUploadReq;

/**
 * 文件上传管理Service接口
 *
 * @author teaching
 * @date 2026-01-15
 */
public interface IFileUploadManagerService
{
    /**
     * 查询文件上传管理
     *
     * @param id 文件上传管理主键
     * @return 文件上传管理
     */
    public FileUploadManager selectFileUploadManagerById(Long id);

    /**
     * 查询文件上传管理列表
     *
     * @param fileUploadManager 文件上传管理
     * @return 文件上传管理集合
     */
    public List<FileUploadManager> selectFileUploadManagerList(FileUploadManager fileUploadManager);

    /**
     * 新增文件上传管理
     *
     * @param fileUploadManager 文件上传管理
     * @return 结果
     */
    public int insertFileUploadManager(FileUploadManager fileUploadManager);

    /**
     * 修改文件上传管理
     *
     * @param fileUploadManager 文件上传管理
     * @return 结果
     */
    public int updateFileUploadManager(FileUploadManager fileUploadManager);

    public int updateFileUploadManagerByTaskId(FileUploadManager fileUploadManager);

    /**
     * 批量删除文件上传管理
     *
     * @param ids 需要删除的文件上传管理主键集合
     * @return 结果
     */
    public int deleteFileUploadManagerByIds(Long[] ids);

    /**
     * 删除文件上传管理信息
     *
     * @param id 文件上传管理主键
     * @return 结果
     */
    public int deleteFileUploadManagerById(Long id);

    AjaxResult exportZipFile(FileUploadManager fileUploadManager);

    AjaxResult selectExportFiles(List<String> ids);

    /**
     * 根据文件任务id获取文件上传信息，处理pdf
     *
     * @param fileTaskId 文件任务id
     * @return 文件上传管理列表
     */
    public void getFileUploadManagerByFileTaskId(Long fileTaskId);

    /**
     * 查询文件任务下可导入评审模块的上传快照。
     *
     * @param fileTaskId 文件任务id
     * @param submittedOnly 是否只返回已提交上传
     * @return 上传快照列表
     */
    List<FileReviewImportSource> listReviewImportSourcesByTaskId(Long fileTaskId, Boolean submittedOnly);

    /**
     * 根据上传管理id查询可导入评审模块的上传快照。
     *
     * @param ids 上传管理id集合
     * @return 上传快照列表
     */
    List<FileReviewImportSource> listReviewImportSourcesByIds(List<Long> ids);
}
