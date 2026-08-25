package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.FileUploadManager;
import org.apache.ibatis.annotations.Param;

/**
 * 文件上传管理Mapper接口
 *
 * @author teaching
 * @date 2026-01-15
 */
public interface FileUploadManagerMapper
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

    public List<FileUploadManager> selectFileUploadListByUser(FileUploadManager fileUploadManager);

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
     * 删除文件上传管理
     *
     * @param id 文件上传管理主键
     * @return 结果
     */
    public int deleteFileUploadManagerById(Long id);

    /**
     * 批量删除文件上传管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFileUploadManagerByIds(Long[] ids);

    /**
     * 根据文件任务id查询文件上传管理
     *
     * @param fileTaskId 文件任务id
     * @return 文件上传管理集合
     */
    public List<FileUploadManager> selectFileUploadManagerByFileTaskId(Long fileTaskId);

    /**
     * 查询每个任务、用户最新的一条上传管理记录，由服务层统一判定当前是否有效。
     */
    List<FileUploadManager> selectLatestByTaskIds(@Param("taskIds") List<Long> taskIds);
}
