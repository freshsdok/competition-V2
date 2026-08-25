package com.teaching.system.controller;

import java.util.Date;
import java.util.List;
import java.io.IOException;

import cn.hutool.core.collection.CollUtil;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.file.utils.FileSizeUtil;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.domain.SysFile;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.service.IExportManageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.FileUploadRecord;
import com.teaching.system.service.IFileUploadRecordService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传管理Controller
 * 
 * @author teaching
 * @date 2026-01-09
 */
@RestController
@RequestMapping("/fileUploadRecord")
public class FileUploadRecordController extends BaseController
{
    @Autowired
    private IFileUploadRecordService fileUploadRecordService;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private IExportManageService exportManageService;

    /**
     * 查询文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(FileUploadRecord fileUploadRecord)
    {
        startPage();
        List<FileUploadRecord> list = fileUploadRecordService.selectFileUploadRecordList(fileUploadRecord);
        return getDataTable(list);
    }

    /**
     * 导出文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadRecord:export")
    @Log(title = "文件上传管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, FileUploadRecord fileUploadRecord)
    {
        List<FileUploadRecord> list = fileUploadRecordService.selectFileUploadRecordList(fileUploadRecord);
        ExcelUtil<FileUploadRecord> util = new ExcelUtil<FileUploadRecord>(FileUploadRecord.class);
        //将要导出的excel表转换为MultipartFile类型，便于使用upload接口上传到服务器
        MultipartFile multipartFile = util.transToMultipartFile(response, list, "上传文件日志");
        //将文件上传到服务器，并保存到导出记录中
        exportManageService.uploadFileToService(multipartFile);
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }


    /**
     * 获取文件上传管理详细信息
     */
    @RequiresPermissions("system:fileUploadRecord:query")
    @GetMapping(value = "/getFileUploadRecordDetail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(fileUploadRecordService.selectFileUploadRecordById(id));
    }

    /**
     * 新增文件上传管理
     */
//    @RequiresPermissions("system:fileUploadRecord:add")
//    @Log(title = "文件上传管理", businessType = BusinessType.INSERT)
//    @PostMapping("/saveFileUploadRecord")
//    public AjaxResult add(@RequestBody FileUploadRecord fileUploadRecord)
//    {
//        return toAjax(fileUploadRecordService.insertFileUploadRecord(fileUploadRecord));
//    }

    /**
     * 修改文件上传管理
     */
    @RequiresPermissions("system:fileUploadRecord:edit")
    @Log(title = "文件上传管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateFileUploadRecord")
    public AjaxResult edit(@RequestBody FileUploadRecord fileUploadRecord)
    {
        return toAjax(fileUploadRecordService.updateFileUploadRecord(fileUploadRecord));
    }

    /**
     * 删除文件上传管理
     */
    @RequiresPermissions("system:fileUploadRecord:remove")
    @Log(title = "文件上传管理", businessType = BusinessType.DELETE)
	@GetMapping("/removeFileUploadRecord/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(fileUploadRecordService.deleteFileUploadRecordByIds(ids));
    }

    /**
     * 查询文件上传管理列表
     */
    /*@RequiresPermissions("system:fileUploadRecord:export")
    @GetMapping("/exportFiles")
    public AjaxResult exportFiles(FileUploadRecord fileUploadRecord)
    {
        return fileUploadRecordService.exportZipFile(fileUploadRecord);
    }*/
}
