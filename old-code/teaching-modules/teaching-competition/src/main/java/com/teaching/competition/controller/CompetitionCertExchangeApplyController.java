package com.teaching.competition.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.CompetitionCertExchangeApply;
import com.teaching.competition.service.ICompetitionCertExchangeApplyService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 赛证互通申请Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/competitionCertExchangeApply")
public class CompetitionCertExchangeApplyController extends BaseController {
    @Autowired
    private ICompetitionCertExchangeApplyService competitionCertExchangeApplyService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    private static ThreadPoolExecutor exportApplyCertThreadPool = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 查询赛证互通申请列表
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionCertExchangeApply competitionCertExchangeApply) {
        startPage();
        List<CompetitionCertExchangeApply> list = competitionCertExchangeApplyService.selectCompetitionCertExchangeApplyList(competitionCertExchangeApply);
        return getDataTable(list);
    }

    /**
     * 导出赛证互通申请列表
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:export")
    @Log(title = "赛证互通申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, @RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String userName = SecurityUtils.getUsername();
        Map<String,Object> fileParam = new HashMap<>();
        fileParam.put("userId", userId);
        fileParam.put("userName", userName);
        R<Long> longR = remoteUserService.saveOssExportFile(fileParam, SecurityConstants.INNER);
        Long fileMangerId;
        if (R.isSuccess(longR)) {
            fileMangerId = longR.getData();
        } else {
            fileMangerId = null;
        }
        exportApplyCertThreadPool.execute(() -> {
            uploadFileAndUpdateCertExportApplyInfo(response, competitionCertExchangeApply,userName,userId,fileMangerId);
        });
        return AjaxResult.success("赛证互通申请信息导出成功，请稍后在'导出管理'列表查看文件");
    }

    private void uploadFileAndUpdateCertExportApplyInfo(HttpServletResponse response, CompetitionCertExchangeApply competitionCertExchangeApply,
                                                        String userName, Long userId, Long fileMangerId) {
        Map<String,Object> fileParam = new HashMap<>();
        try{
            List<CompetitionCertExchangeApply> list = competitionCertExchangeApplyService.selectCompetitionCertExchangeApplyList(competitionCertExchangeApply);
//            ExcelUtil<CompetitionCertExchangeApply> util = new ExcelUtil<CompetitionCertExchangeApply>(CompetitionCertExchangeApply.class);
//            MultipartFile multipartFile = util.transToMultipartFile(response, list, "赛证互通申请信息");
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("赛证互通申请信息", list);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap, "赛证互通申请信息导出.xlsx");
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
                fileParam.put("id", fileMangerId);
                fileParam.put("userId", userId);
                fileParam.put("userName", userName);
                fileParam.put("status", "2");
                remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            }
        } catch (Exception e) {
            fileParam.put("id", fileMangerId);
            fileParam.put("userId", userId);
            fileParam.put("userName", userName);
            fileParam.put("status", "2");
            remoteUserService.updateExportManageInner(fileParam, SecurityConstants.INNER);
            logger.error("导出文件失败", e);
        }
    }

    /**
     * 获取赛证互通申请详细信息
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:query")
    @GetMapping("/{applyId}")
    public AjaxResult getInfo(@PathVariable("applyId") Long applyId) {
        return success(competitionCertExchangeApplyService.selectCompetitionCertExchangeApplyById(applyId));
    }

    /**
     * 新增赛证互通申请
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:add")
    @Log(title = "赛证互通申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        return toAjax(competitionCertExchangeApplyService.insertCompetitionCertExchangeApply(competitionCertExchangeApply));
    }

    /**
     * 修改赛证互通申请
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:edit")
    @Log(title = "赛证互通申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionCertExchangeApply competitionCertExchangeApply) {
        return toAjax(competitionCertExchangeApplyService.updateCompetitionCertExchangeApply(competitionCertExchangeApply));
    }

    /**
     * 删除赛证互通申请
     */
    @RequiresPermissions("competition:competitionCertExchangeApply:remove")
    @Log(title = "赛证互通申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{applyIds}")
    public AjaxResult remove(@PathVariable Long[] applyIds) {
        return toAjax(competitionCertExchangeApplyService.deleteCompetitionCertExchangeApplyByIds(applyIds));
    }
}
