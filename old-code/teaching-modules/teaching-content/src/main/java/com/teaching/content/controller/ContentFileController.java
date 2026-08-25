package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.annotation.RequiresLogin;
import com.teaching.content.domain.ContentFile;
import com.teaching.content.service.IContentFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 内容文件Controller
 *
 * @author teaching
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/contentFile")
public class ContentFileController extends BaseController {
    @Autowired
    private IContentFileService contentFileService;

    /**
     * 查询内容文件列表
     */
    @RequiresPermissions("content:file:list")
    @GetMapping("/list")
    public TableDataInfo list(ContentFile contentFile) {
        startPage();
        List<ContentFile> list = contentFileService.selectContentFileList(contentFile);
        return getDataTable(list);
    }

    /**
     * 导出内容文件列表
     */
    @RequiresPermissions("content:file:export")
    @Log(title = "内容文件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ContentFile contentFile) {
        List<ContentFile> list = contentFileService.selectContentFileList(contentFile);
        ExcelUtil<ContentFile> util = new ExcelUtil<ContentFile>(ContentFile.class);
        util.exportExcel(response, list, "内容文件数据");
    }

    /**
     * 获取内容文件详细信息
     */
    @RequiresPermissions("content:file:query")
    @GetMapping(value = "/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId) {
        return success(contentFileService.selectContentFileByFileId(fileId));
    }

    /**
     * 根据栏目ID获取文件列表（前端页面使用，无需权限验证）
     */
    @GetMapping("/getByColumnId/{columnId}")
    public AjaxResult getByColumnId(@PathVariable("columnId") Long columnId) {
        List<ContentFile> files = contentFileService.selectContentFileListByColumnId(columnId);
        return success(files);
    }

    /**
     * 根据栏目ID获取文件（前端用户直接下载使用，无需权限验证）
     * 一个栏目只有一个文件，返回该文件信息
     */
    @GetMapping("/getFileByColumnId/{columnId}")
    public AjaxResult getFileByColumnId(@PathVariable("columnId") Long columnId) {
        List<ContentFile> files = contentFileService.selectContentFileListByColumnId(columnId);
        if (files != null && files.size() > 0) {
            return success(files.get(0));
        }
        return success(null);
    }

    /**
     * 新增内容文件
     */
    @RequiresPermissions("content:file:add")
    @Log(title = "内容文件", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Validated @RequestBody ContentFile contentFile) {
        return toAjax(contentFileService.insertContentFile(contentFile));
    }

    /**
     * 修改内容文件
     */
    @RequiresPermissions("content:file:add")
    @Log(title = "内容文件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ContentFile contentFile) {
        return toAjax(contentFileService.updateContentFile(contentFile));
    }

    /**
     * 删除内容文件
     */
    @RequiresPermissions("content:file:remove")
    @Log(title = "内容文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds) {
        return toAjax(contentFileService.deleteContentFileByFileIds(fileIds));
    }

    /**
     * 上传文件
     */
    @RequiresLogin
    @Log(title = "内容文件", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("columnId") Long columnId,
                             @RequestParam("fileName") String fileName,
                             @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return error("上传文件不能为空");
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 文件保存路径（可配置）
            String uploadDir = System.getProperty("user.dir") + "/upload/content/files/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File savedFile = new File(uploadDir + uniqueFileName);
            file.transferTo(savedFile);

            // 构建文件URL（根据实际部署情况调整）
            String fileUrl = "/upload/content/files/" + uniqueFileName;

            // 创建 ContentFile 对象
            ContentFile contentFile = new ContentFile();
            contentFile.setColumnId(columnId);
            contentFile.setFileName(fileName);
            contentFile.setFileUrl(fileUrl);
            contentFile.setFileType(file.getContentType());
            contentFile.setFileSize(file.getSize());
            contentFile.setStatus("0");  // 默认正常
            contentFile.setDelFlag("0"); // 默认未删除
            contentFile.setOrderNum(0);  // 默认顺序

            // 保存到数据库
            contentFileService.insertContentFile(contentFile);

            return success(contentFile);
        } catch (IOException e) {
            return error("文件上传失败：" + e.getMessage());
        }
    }
}
