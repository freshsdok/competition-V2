package com.teaching.system.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.teaching.system.api.domain.PackageFileReq;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.domain.FileUploadManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导出管理Service接口
 * 
 * @author teaching
 * @date 2026-01-09
 */
public interface IExportManageService 
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
     * 批量删除导出管理
     * 
     * @param ids 需要删除的导出管理主键集合
     * @return 结果
     */
    public int deleteExportManageByIds(Long[] ids);

    /**
     * 删除导出管理信息
     * 
     * @param id 导出管理主键
     * @return 结果
     */
    public int deleteExportManageById(Long id);

    String exportFiles(List<PackageFileReq> urlMap);

    /**
     * 上传附件到服务器，上传保存导出信息
     * @param multipartFile
     */
    void uploadFileToService(MultipartFile multipartFile);

    //
    public Long saveExportManageInner(Map<String,Object> fileParam);

    public int updateExportManageInner(Map<String,Object> fileParam);
}
