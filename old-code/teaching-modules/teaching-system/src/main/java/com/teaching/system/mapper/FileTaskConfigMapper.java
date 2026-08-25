package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.FileTaskConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 文件配置Mapper接口
 * 
 * @author teaching
 * @date 2026-01-07
 */
public interface FileTaskConfigMapper 
{
    /**
     * 查询文件配置
     * 
     * @param id 文件配置主键
     * @return 文件配置
     */
    public FileTaskConfig selectFileTaskConfigById(Long id);

    /**
     * 查询文件配置列表
     * 
     * @param fileTaskConfig 文件配置
     * @return 文件配置集合
     */
    public List<FileTaskConfig> selectFileTaskConfigList(FileTaskConfig fileTaskConfig);

    /**
     * 新增文件配置
     * 
     * @param fileTaskConfig 文件配置
     * @return 结果
     */
    public int insertFileTaskConfig(FileTaskConfig fileTaskConfig);

    public int batchInsertFileTaskConfig(@Param("fileTaskConfigList") List<FileTaskConfig> fileTaskConfigList);

    /**
     * 修改文件配置
     * 
     * @param fileTaskConfig 文件配置
     * @return 结果
     */
    public int updateFileTaskConfig(FileTaskConfig fileTaskConfig);


    public int batchUpdateFileTaskConfig(@Param("list") List<FileTaskConfig> fileTaskConfigList);

    /**
     * 删除文件配置
     * 
     * @param id 文件配置主键
     * @return 结果
     */
    public int deleteFileTaskConfigById(Long id);

    /**
     * 批量删除文件配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFileTaskConfigByIds(Long[] taskIds);

    public int deleteFileTaskConfigByTaskIds(Long[] ids);
}
