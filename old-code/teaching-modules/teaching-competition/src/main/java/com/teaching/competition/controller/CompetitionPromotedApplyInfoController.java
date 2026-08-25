package com.teaching.competition.controller;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionPromotedApplyInfo;
import com.teaching.competition.domain.CompetitionPromotedApplyPcInfo;
import com.teaching.competition.domain.CompetitionPromotedInfo;
import com.teaching.competition.service.ICompetitionPromotedApplyInfoService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 赛事晋级申请报名信息Controller
 *
 * @author teaching
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/promotedApplyInfo")
public class CompetitionPromotedApplyInfoController extends BaseController {
    @Autowired
    private ICompetitionPromotedApplyInfoService competitionPromotedApplyInfoService;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private RemoteFileService remoteFileService;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 查询赛事晋级申请报名信息列表  根据赛事id分页查晋级包括总记录数
     */
//    @RequiresPermissions("competition:promotedApplyInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        if (Objects.isNull(competitionPromotedApplyInfo.getCompetitionSeriesId())) {
            throw new GlobalException("赛事系列id不能为空");
        }
        startPage();
        List<CompetitionPromotedApplyInfo> list = competitionPromotedApplyInfoService.getPromotedPlayerInfoListByCompetitionSeriesId(competitionPromotedApplyInfo);
        Integer totalSum = competitionPromotedApplyInfoService.countCompetitionPromotedApplyInfoList(competitionPromotedApplyInfo.getCompetitionSeriesId());
        return getDataTable(list, totalSum);
    }

    /**
     * C端根据赛事第查晋级信息
     *
     * @param competitionPromotedApplyInfo
     * @return
     */
    @GetMapping("/pcList")
    public TableDataInfo getPcListInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        if (Objects.isNull(competitionPromotedApplyInfo.getCompetitionSeriesId())) {
            throw new GlobalException("赛事系列id不能为空");
        }
        startPage();
        List<CompetitionPromotedApplyInfo> promotedPlayerInfoListPcByCompetitionSeriesId = competitionPromotedApplyInfoService.getPromotedPlayerInfoListPcByCompetitionSeriesId(competitionPromotedApplyInfo);
        TableDataInfo dataTable = getDataTable(promotedPlayerInfoListPcByCompetitionSeriesId);
        dataTable.setTotal(promotedPlayerInfoListPcByCompetitionSeriesId.size());
        return dataTable;
    }

    /**
     * 导出赛事晋级申请报名信息列表
     */
//    @RequiresPermissions("competition:promotedApplyInfo:export")
//    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
//        List<CompetitionPromotedApplyInfo> list = competitionPromotedApplyInfoService.selectCompetitionPromotedApplyInfoList(competitionPromotedApplyInfo);
//        ExcelUtil<CompetitionPromotedApplyInfo> util = new ExcelUtil<CompetitionPromotedApplyInfo>(CompetitionPromotedApplyInfo.class);
//        util.exportExcel(response, list, "赛事晋级申请报名信息数据");
//    }

    /**
     * 导入赛事晋级申请报名信息列表
     */
//    @RequiresPermissions("competition:promotedApplyInfo:import")
    @Log(title = "赛事晋级申请报名信息导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(MultipartFile file, @RequestParam Long competitionSeriesId) throws Exception {
        ExcelUtil<CompetitionPromotedApplyInfo> util = new ExcelUtil<CompetitionPromotedApplyInfo>(CompetitionPromotedApplyInfo.class);
        List<CompetitionPromotedApplyInfo> list = util.importExcel(file.getInputStream());
        for (CompetitionPromotedApplyInfo item : list) {
            item.setCompetitionSeriesId(competitionSeriesId);
        }
        try {
            competitionPromotedApplyInfoService.insertCompetitionPromotedApplyInfo(list);
            return AjaxResult.success("恭喜您，数据已全部导入成功！");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return AjaxResult.error("导入失败");
        }
    }

    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, @RequestBody CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        if (Objects.isNull(competitionPromotedApplyInfo.getCompetitionSeriesId())) {
            throw new GlobalException("赛事系列id不能为空");
        }
        //将文件上传到服务器，并保存到导出记录中
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
                uploadAwardDetailsExportInfo(response, competitionPromotedApplyInfo, userName, userId, fileMangerId);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }

    private void uploadAwardDetailsExportInfo(HttpServletResponse response, CompetitionPromotedApplyInfo competitionPromotedApplyInfo, String userName, Long userId, Long fileMangerId) {
        Map<String, Object> fileParam = new HashMap<>();
        try {
            CompetitionPromotedApplyInfo competitionApplyInfoReq = new CompetitionPromotedApplyInfo();
            if (!"all".equals(competitionPromotedApplyInfo.getExportType())) {
                BeanUtils.copyProperties(competitionPromotedApplyInfo, competitionApplyInfoReq);
            }
            competitionApplyInfoReq.setCompetitionSeriesId(competitionPromotedApplyInfo.getCompetitionSeriesId());
            List<CompetitionPromotedApplyInfo> teamManagerInfoList = competitionPromotedApplyInfoService.selectCompetitionPromotedApplyInfoList(competitionApplyInfoReq);
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("晋级名单", teamManagerInfoList);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap, "晋级名单导出.xlsx");
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

    /**
     * 用户端导出
     */
    @Log(title = "获奖公示明细", businessType = BusinessType.EXPORT)
    @PostMapping("/pcExport")
    public void pcExport(HttpServletResponse response, CompetitionPromotedApplyInfo competitionPromotedApplyInfo) {
        if (Objects.isNull(competitionPromotedApplyInfo.getCompetitionSeriesId())) {
            throw new GlobalException("赛事系列id不能为空");
        }
        Long userId = null;
        try {
            userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        } catch (Exception e) {
            throw new GlobalException("用户未登录，请登录后再试");
        }
        CompetitionPromotedApplyInfo competitionApplyInfoReq = new CompetitionPromotedApplyInfo();
        if (!"all".equals(competitionPromotedApplyInfo.getExportType())) {
            BeanUtils.copyProperties(competitionPromotedApplyInfo, competitionApplyInfoReq);
        }
        competitionApplyInfoReq.setLeaderTeacherId(userId);
        competitionApplyInfoReq.setCompetitionSeriesId(competitionPromotedApplyInfo.getCompetitionSeriesId());
        List<CompetitionPromotedApplyPcInfo> teamManagerInfoList = competitionPromotedApplyInfoService.selectCompetitionPromotedApplyInfoPcList(competitionApplyInfoReq);
        Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
        sheetDataMap.put("晋级名单", teamManagerInfoList);
        ExcelUtil excelUtil = new ExcelUtil<>();
        excelUtil.exportExcelByteData(response, sheetDataMap);
    }

    /**
     * 获取赛事晋级申请报名信息详细信息
     */
    @RequiresPermissions("competition:promotedApplyInfo:query")
    @GetMapping(value = "/{applyId}")
    public AjaxResult getInfo(@PathVariable("applyId") Long applyId) {
        return success(competitionPromotedApplyInfoService.selectCompetitionPromotedApplyInfoByApplyId(applyId));
    }

    /**
     * 新增赛事晋级申请报名信息
     */
//    @RequiresPermissions("competition:promotedApplyInfo:add")
//    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.INSERT)
//    @PostMapping("/addCompetitionPromotedApplyInfo")
//    public AjaxResult add(@RequestBody List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfoList) {
//        return toAjax(competitionPromotedApplyInfoService.insertCompetitionPromotedApplyInfo(competitionPromotedApplyInfoList));
//    }

    /**
     * 修改赛事晋级申请报名信息
     */
//    @RequiresPermissions("competition:promotedApplyInfo:edit")
    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfos) {
        //已报名状态的还可以修改  管理端修改不做限制
        return toAjax(competitionPromotedApplyInfoService.updateCompetitionPromotedApplyInfos(competitionPromotedApplyInfos));
    }

    /**
     * C端修改赛事晋级申请报名信息 在报名的起止时间内且未报名可修改
     *
     * @param competitionPromotedApplyInfos
     * @return
     */
    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.UPDATE)
    @PutMapping("/pcEdit")
    public AjaxResult pcEdit(@RequestBody CompetitionPromotedApplyInfo competitionPromotedApplyInfos) {
        //在报名的起止时间内且未报名可修改
        return success(competitionPromotedApplyInfoService.pcUpdateCompetitionPromotedApplyInfo(competitionPromotedApplyInfos));
    }

    /**
     * 根据teamCode逻辑删除赛事晋级申请报名信息
     */
//    @RequiresPermissions("competition:promotedApplyInfo:remove")
    @Log(title = "赛事晋级申请报名信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{competitionSeriesId}/{teamCode}")
    public AjaxResult logicalRemove(@PathVariable Long competitionSeriesId, @PathVariable String teamCode) {
        // 已报名状态的是不可以删除
        return toAjax(competitionPromotedApplyInfoService.logicalDelCompetitionPromotedApplyInfoByTeamCodeAndCompetitionSeriesId(teamCode, competitionSeriesId));
    }

    /**
     * C端报名
     *
     * @param competitionPromotedInfo
     * @return
     */
    @PostMapping("/pcApply")
    public AjaxResult pcApply(@RequestBody CompetitionPromotedInfo competitionPromotedInfo) {
        return success(competitionPromotedApplyInfoService.pcApply(competitionPromotedInfo.getPromotedId(), competitionPromotedInfo.getTeamCodes()));
    }
}
