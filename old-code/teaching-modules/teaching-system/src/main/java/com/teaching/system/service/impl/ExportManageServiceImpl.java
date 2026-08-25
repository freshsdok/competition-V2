package com.teaching.system.service.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.URLUtil;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.file.utils.FileSizeUtil;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteOssUploadService;
import com.teaching.system.api.domain.PackageFileReq;
import com.teaching.system.api.domain.SysFile;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.service.IFileUploadRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.ExportManageMapper;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.service.IExportManageService;
import org.springframework.web.multipart.MultipartFile;

 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;

import static com.teaching.common.core.constant.DictConstant.*;
import static org.bouncycastle.internal.asn1.cms.CMSObjectIdentifiers.data;

/**
 * 导出管理Service业务层处理
 * 
 * @author teaching
 * @date 2026-01-09
 */
@Service
public class ExportManageServiceImpl implements IExportManageService 
{
    private static final Logger log = LoggerFactory.getLogger(ExportManageServiceImpl.class);

    private static final ExecutorService EXPORT_EXECUTOR = createExportExecutor();

    private static ExecutorService createExportExecutor() {
        int cpu = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.max(2, cpu);
        int maxPoolSize = Math.max(corePoolSize, Math.min(32, cpu * 4));
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("export-manage-" + idx.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    @Autowired
    private ExportManageMapper exportManageMapper;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private RemoteOssUploadService remoteOssUploadService;

    @Autowired
    private IFileUploadRecordService fileUploadRecordService;

    /**
     * 查询导出管理
     * 
     * @param id 导出管理主键
     * @return 导出管理
     */
    @Override
    public ExportManage selectExportManageById(Long id)
    {
        return exportManageMapper.selectExportManageById(id);
    }

    /**
     * 查询导出管理列表
     * 
     * @param exportManage 导出管理
     * @return 导出管理
     */
    @Override
    public List<ExportManage> selectExportManageList(ExportManage exportManage)
    {
        //只查询当前用户自己的数据
        exportManage.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        //筛选条件结束时间设置为当天的最后时间
        if (exportManage.getCreateEndTime() != null) {
            exportManage.setCreateEndTime(DateUtil.endOfDay(exportManage.getCreateEndTime()));
        }
        return exportManageMapper.selectExportManageList(exportManage);
    }

    /**
     * 新增导出管理
     * 
     * @param exportManage 导出管理
     * @return 结果
     */
    @Override
    public int insertExportManage(ExportManage exportManage)
    {
        exportManage.setCreateTime(DateUtils.getNowDate());
        return exportManageMapper.insertExportManage(exportManage);
    }

    /**
     * 修改导出管理
     * 
     * @param exportManage 导出管理
     * @return 结果
     */
    @Override
    public int updateExportManage(ExportManage exportManage)
    {
        exportManage.setUpdateTime(DateUtils.getNowDate());
        return exportManageMapper.updateExportManage(exportManage);
    }

    /**
     * 批量删除导出管理
     * 
     * @param ids 需要删除的导出管理主键
     * @return 结果
     */
    @Override
    public int deleteExportManageByIds(Long[] ids)
    {
        return exportManageMapper.deleteExportManageByIds(ids);
    }

    /**
     * 删除导出管理信息
     * 
     * @param id 导出管理主键
     * @return 结果
     */
    @Override
    public int deleteExportManageById(Long id)
    {
        return exportManageMapper.deleteExportManageById(id);
    }

    @Override
    public String exportFiles(List<PackageFileReq> fileList) {
        //创建导出任务
        ExportManage exportManage = new ExportManage();
        exportManage.setStartTime(new Date());
        exportManage.setStatus(EXPORTING);  //0-导出中
        exportManage.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        exportManage.setUserName(SecurityUtils.getUsername());
        insertExportManage(exportManage);
        //添加异步任务执行文件下载打包
        EXPORT_EXECUTOR.execute(() -> {
            try {
                zipFile(exportManage.getId(), fileList);
            } catch (IOException e) {
                System.err.println("文件导出异常：" + e.getMessage());
                e.printStackTrace();
            }
        });
        return "导出成功，请稍后在'导出管理'列表查看文件";
    }

    /**
     * 执行下载，打包，上传
     * @param id
     * @param fileList
     * @throws IOException
     */
    private void zipFile(Long id,List<PackageFileReq> fileList) throws IOException {
        ExportManage exportManage = exportManageMapper.selectExportManageById(id);
        R<Map<String, Object>> mapR = remoteOssUploadService.packageFile(fileList);
        if (mapR.getCode() != 200) {
            exportManage.setStatus(EXPORT_FAILED);
            exportManage.setRemark(mapR.getMsg());
            exportManage.setUpdateTime(new Date());
            System.out.println("mapR,getCode：" + mapR.getCode());
        }else {
            Map<String, Object> data = mapR.getData();
            System.out.println("文件上传成功返回信息：" + data);
            if (data != null && "success".equals(data.get("code"))) {
                String size = data.get("size").toString();
                String fileName = data.get("fileName").toString();
                String url = data.get("url").toString();
                //url解码
                String decodeUrl = URLUtil.decode(url);
                String substring = decodeUrl.substring(0, decodeUrl.indexOf("?"));
                exportManage.setFileUrl(substring);
                String formatSize = FileSizeUtil.formatDecimalFileSize(Long.parseLong(size));
                exportManage.setSize(formatSize);
                exportManage.setTitle(fileName);
                exportManage.setEndTime(new Date());
                exportManage.setStatus(EXPORTED);  //1-导出完成
            } else if(data != null){  //导出失败
                exportManage.setStatus(EXPORT_FAILED);
                exportManage.setRemark(data.get("failReason").toString());
                exportManage.setUpdateTime(new Date());
            }
        }
        updateExportManage(exportManage);
    }
    @Override
    public void uploadFileToService(MultipartFile multipartFile) {
        ExportManage exportManage = new ExportManage();
        exportManage.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        exportManage.setUserName(SecurityUtils.getUsername());
        exportManage.setStatus(EXPORTING);
        exportManage.setStartTime(new Date());
        exportManage.setSize(FileSizeUtil.formatDecimalFileSize(multipartFile.getSize()));
        exportManage.setTitle(multipartFile.getOriginalFilename());
        insertExportManage(exportManage);
        //添加异步任务执行文件下载打包
        EXPORT_EXECUTOR.execute(() -> uploadFileAndUpdateExportInfo(multipartFile, exportManage.getId()));
    }

    @Override
    public Long saveExportManageInner(Map<String,Object> fileParam) {
        ExportManage exportManage = new ExportManage();
        exportManage.setUserId(Long.valueOf(fileParam.get("userId").toString()));
        exportManage.setUserName(fileParam.get("userName").toString());
        exportManage.setStatus(EXPORTING);
        exportManage.setStartTime(new Date());
        insertExportManage(exportManage);
        return exportManage.getId();
    }

    @Override
    public int updateExportManageInner(Map<String, Object> fileParam) {
        if(Objects.nonNull(fileParam.get("id"))){
            ExportManage selectExport = selectExportManageById(Long.parseLong(fileParam.get("id").toString()));
            selectExport.setEndTime(new Date());
            if(Objects.nonNull(fileParam.get("fileSize"))){
                selectExport.setSize(FileSizeUtil.formatDecimalFileSize(Long.parseLong(fileParam.get("fileSize").toString())));
            }
            if(Objects.nonNull(fileParam.get("fileName"))){
                selectExport.setTitle(fileParam.get("fileName").toString());
            }
            if(Objects.nonNull(fileParam.get("fileUrl"))){
                selectExport.setFileUrl(fileParam.get("fileUrl").toString());
            }
            selectExport.setStatus(fileParam.get("status").toString());
            return updateExportManage(selectExport);
        }
        return 0;
    }

    private void uploadFileAndUpdateExportInfo(MultipartFile multipartFile, Long id) {
        //调用上传附件接口上传文件
        R<String> upload = remoteFileService.ossUpload(multipartFile, "fileExport", null);
        //上传成功后，更新导出文件url、状态、结束时间等信息
        if (upload.getCode() == 200) {
            String url = upload.getData();
            ExportManage selectExport = selectExportManageById(id);
            selectExport.setFileUrl(url);
            selectExport.setStatus(EXPORTED);
            selectExport.setEndTime(new Date());
            updateExportManage(selectExport);
        }
    }
}
