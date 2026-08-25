package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.FileUploadRecord;

/**
 * 文件上传管理Mapper接口
 * 
 * @author teaching
 * @date 2026-01-09
 */
public interface FileUploadRecordMapper 
{
    /**
     * 查询文件上传管理
     * 
     * @param id 文件上传管理主键
     * @return 文件上传管理
     */
    public FileUploadRecord selectFileUploadRecordById(Long id);

    /**
     * 查询文件上传管理列表
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 文件上传管理集合
     */
    public List<FileUploadRecord> selectFileUploadRecordList(FileUploadRecord fileUploadRecord);

    // 获取用户最新的一条上传记录
    public List<FileUploadRecord> selectFileUploadRecordListByUser(FileUploadRecord fileUploadRecord);

    /**
     * 新增文件上传管理
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 结果
     */
    public int insertFileUploadRecord(FileUploadRecord fileUploadRecord);

    /**
     * 修改文件上传管理
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 结果
     */
    public int updateFileUploadRecord(FileUploadRecord fileUploadRecord);

    /**
     * 删除文件上传管理
     * 
     * @param id 文件上传管理主键
     * @return 结果
     */
    public int deleteFileUploadRecordById(Long id);

    /**
     * 批量删除文件上传管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFileUploadRecordByIds(Long[] ids);
}
