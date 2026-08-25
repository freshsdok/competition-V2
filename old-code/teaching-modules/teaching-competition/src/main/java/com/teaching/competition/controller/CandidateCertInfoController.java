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
import com.teaching.competition.domain.CandidateCertInfoImport;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.teaching.competition.domain.CandidateCertInfo;
import com.teaching.competition.service.ICandidateCertInfoService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 候选人证书Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/candidateCertInfo")
public class CandidateCertInfoController extends BaseController {
    @Autowired
    private ICandidateCertInfoService candidateCertInfoService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    private static ThreadPoolExecutor exportThreadPool = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 查询候选人列表
     */
//    @RequiresPermissions("competition:candidateCertInfo:list")
    @GetMapping("/list")
    public AjaxResult list(CandidateCertInfo candidateCertInfo) {
        List<CandidateCertInfo> list = candidateCertInfoService.selectCandidateCertInfoList(candidateCertInfo);
        return success(list);
    }

    /**
     * 导出候选人列表
     */
    @RequiresPermissions("competition:candidateCertInfo:export")
    @Log(title = "候选人列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response,@RequestBody CandidateCertInfo candidateCertInfo) {
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
        exportThreadPool.execute(() -> {
            uploadFileAndUpdateCertExportInfo(response, candidateCertInfo,userName,userId,fileMangerId);
        });
        return AjaxResult.success("候选人信息导出成功，请稍后在'导出管理'列表查看文件");
    }

    private void uploadFileAndUpdateCertExportInfo(HttpServletResponse response, CandidateCertInfo candidateCertInfo,
                                                   String userName, Long userId, Long fileMangerId) {
        Map<String,Object> fileParam = new HashMap<>();
        try{
            List<CandidateCertInfo> list = candidateCertInfoService.selectCandidateCertInfoList(candidateCertInfo);
//            ExcelUtil<CandidateCertInfo> util = new ExcelUtil<CandidateCertInfo>(CandidateCertInfo.class);
//            MultipartFile multipartFile = util.transToMultipartFile(response, list, "侯选人信息");
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("侯选人信息", list);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap, "侯选人信息导出.xlsx");
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
            logger.error("导出文件失败", e);
        }
    }

    /**
     * 获取候选人证书详细信息
     */
    @RequiresPermissions("competition:candidateCertInfo:query")
    @GetMapping("/{candidateId}")
    public AjaxResult getInfo(@PathVariable("candidateId") Long candidateId) {
        return success(candidateCertInfoService.selectCandidateCertInfoById(candidateId));
    }

    /**
     * 新增候选人证书(不使用)
     */
    @RequiresPermissions("competition:candidateCertInfo:add")
    @Log(title = "候选人证书", businessType = BusinessType.INSERT)
    @PostMapping("/saveCandidateCertInfo")
    public AjaxResult add(@RequestBody @Validated CandidateCertInfo candidateCertInfo) {
        return toAjax(candidateCertInfoService.insertCandidateCertInfo(candidateCertInfo));
    }

    /**
     * 批量新增候选人证书
     */
    @RequiresPermissions("competition:candidateCertInfo:add")
    @Log(title = "候选人证书", businessType = BusinessType.INSERT)
    @PostMapping("/batchInsertCandidateCertInfo/{certConfigId}")
    public AjaxResult batchAdd(@RequestBody @Validated List<CandidateCertInfo> candidateCertInfoList,@PathVariable Long certConfigId) {
        return toAjax(candidateCertInfoService.batchInsertCandidateCertInfo(candidateCertInfoList,certConfigId));
    }

    /**
     * 修改候选人证书
     */
    @RequiresPermissions("competition:candidateCertInfo:edit")
    @Log(title = "候选人证书", businessType = BusinessType.UPDATE)
    @PostMapping
    public AjaxResult edit(@RequestBody CandidateCertInfo candidateCertInfo) {
        return toAjax(candidateCertInfoService.updateCandidateCertInfo(candidateCertInfo));
    }

    /**
     * 导入候选人信息
     */
    @Log(title = "候选人证书", businessType = BusinessType.IMPORT)
    @RequiresPermissions("competition:candidateCertInfo:import")
    @PostMapping("/importCandidateCertInfo")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,Long certConfigId) throws Exception {
        ExcelUtil<CandidateCertInfoImport> util = new ExcelUtil<>(CandidateCertInfoImport.class);
        List<CandidateCertInfoImport> candidateList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getLoginUser().getUsername();
        String message = candidateCertInfoService.importCandidateCertInfo(candidateList, updateSupport, operName,certConfigId);
        return success(message);
    }

    /**
     * 一键拉取获奖公示团队报名信息
     */
    @Log(title = "一键拉取获奖公示团队报名信息", businessType = BusinessType.INSERT)
    @PostMapping("/insertCandidateCertInfoFromAwards")
    public AjaxResult insertCandidateCertInfoFromAwards(@RequestBody CandidateCertInfo candidateCertInfo) {
        return toAjax(candidateCertInfoService.insertCandidateCertInfoFromAwards(candidateCertInfo));
    }

    /**
     * 删除候选人证书
     */
    @RequiresPermissions("competition:candidateCertInfo:remove")
    @Log(title = "候选人证书", businessType = BusinessType.DELETE)
    @GetMapping("/{candidateIds}")
    public AjaxResult remove(@PathVariable Long[] candidateIds) {
        return toAjax(candidateCertInfoService.deleteCandidateCertInfoByIds(candidateIds));
    }
}
