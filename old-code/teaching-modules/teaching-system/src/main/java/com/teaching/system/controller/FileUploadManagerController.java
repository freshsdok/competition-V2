package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;

import cn.hutool.core.collection.CollUtil;
import com.teaching.common.core.domain.R;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.system.api.domain.FileReviewImportSource;
import com.teaching.system.domain.FileUploadRecord;
import com.teaching.system.service.IExportManageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.service.IFileUploadManagerService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传管理Controller
 *
 * @author teaching
 * @date 2026-01-15
 */
@RestController
@RequestMapping("/fileUploadManager")
public class FileUploadManagerController extends BaseController
{
    @Autowired
    private IFileUploadManagerService fileUploadManagerService;

    @Autowired
    private IExportManageService exportManageService;

    /**
     * 查询文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadManager:list")
    @GetMapping("/list")
    public TableDataInfo list(FileUploadManager fileUploadManager)
    {
        startPage();
        List<FileUploadManager> list = fileUploadManagerService.selectFileUploadManagerList(fileUploadManager);
        return getDataTable(list);
    }

    /**
     * 导出文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadManager:export")
    @Log(title = "文件上传管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, @RequestBody FileUploadManager fileUploadManager)
    {
        List<FileUploadManager> list = fileUploadManagerService.selectFileUploadManagerList(fileUploadManager);
        if (CollUtil.isEmpty(list)) {
            AjaxResult.error("没有需要导出的数据");
        }
        ExcelUtil<FileUploadManager> util = new ExcelUtil<FileUploadManager>(FileUploadManager.class);
        //将要导出的excel表转换为MultipartFile类型，便于使用upload接口上传到服务器
        MultipartFile multipartFile = util.transToMultipartFile(response, list, "上传文件管理");
        //将文件上传到服务器，并保存到导出记录中
        exportManageService.uploadFileToService(multipartFile);
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }

    /**
     * 获取文件上传管理详细信息
     */
    @RequiresPermissions("system:fileUploadManager:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(fileUploadManagerService.selectFileUploadManagerById(id));
    }

    /**
     * 新增文件上传管理
     */
//    @RequiresPermissions("system:fileUploadManager:add")
//    @Log(title = "文件上传管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@RequestBody FileUploadManager fileUploadManager)
//    {
//        return toAjax(fileUploadManagerService.insertFileUploadManager(fileUploadManager));
//    }

    /**
     * 修改文件上传管理
     */
    @RequiresPermissions("system:fileUploadManager:edit")
    @Log(title = "文件上传管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FileUploadManager fileUploadManager)
    {
        return toAjax(fileUploadManagerService.updateFileUploadManager(fileUploadManager));
    }

    /**
     * 删除文件上传管理
     */
    @RequiresPermissions("system:fileUploadManager:remove")
    @Log(title = "文件上传管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(fileUploadManagerService.deleteFileUploadManagerByIds(ids));
    }

    /**
     * 查询文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadManager:fileExport")
    @GetMapping("/exportFiles")
    public AjaxResult exportFiles(FileUploadManager fileUploadManager)
    {
        return fileUploadManagerService.exportZipFile(fileUploadManager);
    }

    /**
     * 查询文件上传管理列表
     */
    @RequiresPermissions("system:fileUploadManager:fileExport")
    @PostMapping("/selectExportFiles")
    public AjaxResult selectExportFiles(@RequestBody List<String> ids)
    {
        return fileUploadManagerService.selectExportFiles(ids);
    }

    /**
     * 根据文件任务id获取文件上传信息，处理pdf
     * @param fileTaskId
     * @return
     */
    @GetMapping(value = "/byFileTaskId/{fileTaskId}")
    public AjaxResult getFileUploadManagerByFileTaskId(@PathVariable("fileTaskId") Long fileTaskId)
    {
        fileUploadManagerService.getFileUploadManagerByFileTaskId(fileTaskId);
        return success();
    }

    /**
     * 内部接口：按文件任务查询可导入评审模块的上传快照。
     */
    @InnerAuth
    @GetMapping("/review-import/by-task/{fileTaskId}")
    public R<List<FileReviewImportSource>> listReviewImportSourcesByTaskId(@PathVariable("fileTaskId") Long fileTaskId,
                                                                           @RequestParam(value = "submittedOnly", required = false) Boolean submittedOnly)
    {
        return R.ok(fileUploadManagerService.listReviewImportSourcesByTaskId(fileTaskId, submittedOnly));
    }

    /**
     * 内部接口：按上传管理id查询可导入评审模块的上传快照。
     */
    @InnerAuth
    @PostMapping("/review-import/by-ids")
    public R<List<FileReviewImportSource>> listReviewImportSourcesByIds(@RequestBody List<Long> ids)
    {
        return R.ok(fileUploadManagerService.listReviewImportSourcesByIds(ids));
    }
}
