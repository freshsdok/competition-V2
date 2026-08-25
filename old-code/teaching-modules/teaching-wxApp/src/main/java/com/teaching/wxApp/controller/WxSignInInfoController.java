package com.teaching.wxApp.controller;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.wxApp.domain.WxSignInInfo;
import com.teaching.wxApp.service.IWxSignInInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 签到信息Controller
 *
 * @author teaching
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/wxSignInInfo")
public class WxSignInInfoController extends BaseController {
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    @Autowired
    private IWxSignInInfoService wxSignInInfoService;

    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private RemoteFileService remoteFileService;

    /**
     * 查询签到信息列表
     */
    @RequiresPermissions("wxApp:wxSignInInfo:list")
    @GetMapping("/list2")
    public TableDataInfo list2(WxSignInInfo wxSignInInfo) {
        startPage();
        List<WxSignInInfo> list = wxSignInInfoService.selectWxSignInInfoList(wxSignInInfo);
        return getDataTable(list);
    }

    @RequiresPermissions("wxApp:wxSignInInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(WxSignInInfo wxSignInInfo) {
        return wxSignInInfoService.getWxSignInInfoList(wxSignInInfo);
    }

    /**
     * 导出签到信息列表
     */
    @RequiresPermissions("wxApp:wxSignInInfo:export")
    @Log(title = "签到信息", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WxSignInInfo wxSignInInfo) {
        List<WxSignInInfo> list = wxSignInInfoService.selectWxSignInInfoList(wxSignInInfo);
        return success(list);
    }

    @RequiresPermissions("wxApp:wxSignInInfo:export")
    @Log(title = "签到信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, @RequestBody WxSignInInfo wxSignInInfo) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String userName = SecurityUtils.getUsername();
        Map<String, Object> fileParam = new HashMap<>();
        fileParam.put("userId", userId);
        fileParam.put("userName", userName);
        R<Long> longR = remoteUserService.saveOssExportFile(fileParam, SecurityConstants.INNER);
        Long fileMangerId;
        if (R.isSuccess(longR)) {
            fileMangerId = longR.getData();
        } else {
            fileMangerId = null;
        }
        try {
            threadPoolExecutor.execute(() -> {
                uploadFileAndUpdateExportInfo(response, wxSignInInfo, userName, userId, fileMangerId);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭线程
//            shutdownExecutor(threadPoolExecutor);
        }
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }


    /**
     * 获取签到信息详细信息
     */
    @RequiresPermissions("wxApp:wxSignInInfo:query")
    @GetMapping(value = "/{signId}")
    public AjaxResult getInfo(@PathVariable("signId") Long signId) {
        return success(wxSignInInfoService.selectWxSignInInfoBySignId(signId));
    }

    /**
     * 新增签到信息
     */
    @RequiresPermissions("wxApp:wxSignInInfo:add")
    @Log(title = "签到信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WxSignInInfo wxSignInInfo) {
        return toAjax(wxSignInInfoService.insertWxSignInInfo(wxSignInInfo));
    }

    /**
     * 修改签到信息
     */
    @RequiresPermissions("wxApp:wxSignInInfo:edit")
    @Log(title = "签到信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WxSignInInfo wxSignInInfo) {
        return toAjax(wxSignInInfoService.updateWxSignInInfo(wxSignInInfo));
    }

    /**
     * 删除签到信息
     */
    @RequiresPermissions("wxApp:wxSignInInfo:remove")
    @Log(title = "签到信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{signIds}")
    public AjaxResult remove(@PathVariable Long[] signIds) {
        return toAjax(wxSignInInfoService.deleteWxSignInInfoBySignIds(signIds));
    }


    public void uploadFileAndUpdateExportInfo(HttpServletResponse response, WxSignInInfo wxSignInInfo,
                                              String userName, Long userId, Long fileMangerId) {
        Map<String, Object> fileParam = new HashMap<>();
        try {
            List<WxSignInInfo> endList = wxSignInInfoService.getEndList(wxSignInInfo);
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("签到记录", endList);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap, "签到记录导出.xlsx");
            //调用上传附件接口上传文件
            R<String> upload = remoteFileService.ossUpload(multipartFile, "fileExport", null);
            //上传成功后，更新导出文件url、状态、结束时间等信息
            if (upload.getCode() == 200) {
                String url = upload.getData();
                fileParam.put("fileName", multipartFile.getOriginalFilename());
                fileParam.put("fileUrl", url);
                fileParam.put("fileSize", multipartFile.getSize());
                fileParam.put("userId", userId);
                fileParam.put("userName", userName);
                fileParam.put("id", fileMangerId);
                fileParam.put("status", "1");
                remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            } else {
                fileParam.put("userId", userId);
                fileParam.put("userName", userName);
                fileParam.put("id", fileMangerId);
                fileParam.put("status", "2");
                remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            }
        } catch (Exception e) {
            fileParam.put("userId", userId);
            fileParam.put("userName", userName);
            fileParam.put("id", fileMangerId);
            fileParam.put("status", "2");
            remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            logger.error("上传文件失败", e);
        }
    }
}
