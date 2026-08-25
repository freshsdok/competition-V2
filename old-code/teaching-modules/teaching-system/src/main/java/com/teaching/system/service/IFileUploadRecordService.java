package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.system.domain.FileUploadRecord;
import com.teaching.system.domain.FileUploadReq;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文件上传管理Service接口
 * 
 * @author teaching
 * @date 2026-01-09
 */
public interface IFileUploadRecordService 
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

    /**
     * 新增文件上传管理
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 结果
     */
    public int insertFileUploadRecord(FileUploadRecord fileUploadRecord);

    /**
     * 新增文件上传管理
     *
     * @param fileUploadReq 文件上传管理
     * @return 结果
     */
    public int insertFileUploadManager(FileUploadReq fileUploadReq);

    /**
     * 修改文件上传管理
     * 
     * @param fileUploadRecord 文件上传管理
     * @return 结果
     */
    public int updateFileUploadRecord(FileUploadRecord fileUploadRecord);

    /**
     * 批量删除文件上传管理
     * 
     * @param ids 需要删除的文件上传管理主键集合
     * @return 结果
     */
    public int deleteFileUploadRecordByIds(Long[] ids);

    /**
     * 删除文件上传管理信息
     * 
     * @param id 文件上传管理主键
     * @return 结果
     */
    public int deleteFileUploadRecordById(Long id);

    /**
     * 导出文件
     * @param urls
     * @return
     */
//    Map<String,Object> exportZipFile(List<String> urls, Long id,Map<String,String> urlMap);

//    AjaxResult exportZipFile(FileUploadRecord fileUploadRecord);
}
