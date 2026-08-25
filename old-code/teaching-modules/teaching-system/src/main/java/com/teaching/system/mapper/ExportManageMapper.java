package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.domain.FileUploadManager;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导出管理Mapper接口
 * 
 * @author teaching
 * @date 2026-01-09
 */
@Mapper
public interface ExportManageMapper 
{
    /**
     * 查询导出管理
     * 
     * @param id 导出管理主键
     * @return 导出管理
     */
    public ExportManage selectExportManageById(Long id);

    /**
     * 查询导出管理列表
     * 
     * @param exportManage 导出管理
     * @return 导出管理集合
     */
    public List<ExportManage> selectExportManageList(ExportManage exportManage);

    /**
     * 新增导出管理
     * 
     * @param exportManage 导出管理
     * @return 结果
     */
    public int insertExportManage(ExportManage exportManage);

    /**
     * 修改导出管理
     * 
     * @param exportManage 导出管理
     * @return 结果
     */
    public int updateExportManage(ExportManage exportManage);

    /**
     * 删除导出管理
     * 
     * @param id 导出管理主键
     * @return 结果
     */
    public int deleteExportManageById(Long id);

    /**
     * 批量删除导出管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExportManageByIds(Long[] ids);
}
