package com.teaching.system.service;

import com.teaching.system.domain.FileDownloadRecord;

import java.util.List;

/**
 * 文件下载记录Service接口
 *
 * @author teaching
 * @date 2026-01-09
 */
public interface IFileDownloadRecordService {
    /**
     * 查询文件下载记录
     *
     * @param id 文件下载记录主键
     * @return 文件下载记录
     */
    public FileDownloadRecord selectFileDownloadRecordById(Long id);

    /**
     * 查询文件下载记录列表
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 文件下载记录集合
     */
    public List<FileDownloadRecord> selectFileDownloadRecordList(FileDownloadRecord fileDownloadRecord);

    /**
     * 新增文件下载记录
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 结果
     */
    public int insertFileDownloadRecord(FileDownloadRecord fileDownloadRecord);

    /**
     * 修改文件下载记录
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 结果
     */
    public int updateFileDownloadRecord(FileDownloadRecord fileDownloadRecord);

    /**
     * 批量删除文件下载记录
     *
     * @param ids 需要删除的文件下载记录主键集合
     * @return 结果
     */
    public int deleteFileDownloadRecordByIds(Long[] ids);

    /**
     * 删除文件下载记录信息
     *
     * @param id 文件下载记录主键
     * @return 结果
     */
    public int deleteFileDownloadRecordById(Long id);
}
