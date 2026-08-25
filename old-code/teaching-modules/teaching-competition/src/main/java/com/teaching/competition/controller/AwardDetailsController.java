package com.teaching.competition.controller;

import com.teaching.common.core.constant.DictConstant;
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
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.service.IAwardDetailsService;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import com.teaching.competition.service.ITeamManagerInfoService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
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
 * 获奖公示明细Controller
 *
 * @author teaching
 * @date 2026-05-12
 */
@RestController
@RequestMapping("/details")
public class AwardDetailsController extends BaseController {
    @Autowired
    private IAwardDetailsService awardDetailsService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    @Autowired
    private ITeamManagerInfoService teamManagerInfoService;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 查询获奖公示明细列表
     */
//    @RequiresPermissions("competition:details:list")
//    @GetMapping("/list")
//    public TableDataInfo list(AwardDetails awardDetails) {
//        startPage();
//        List<AwardDetails> list = awardDetailsService.selectAwardDetailsList(awardDetails);
//        return getDataTable(list);
//    }

//    @RequiresPermissions("competition:details:list")
    @GetMapping("/awardDetailsList")
    public TableDataInfo selectAwardDetailsCompetitionApplyInfoList(AwardDetails awardDetails) {
        startPage();
        List<AwardDetails> list = awardDetailsService.selectAwardDetailsCompetitionApplyInfoList(awardDetails);
        Integer totalSum = awardDetailsService.selectAwardDetailsSum(awardDetails.getAwardPublicityId());
        return getDataTable(list, totalSum);
    }

    /**
     * 导出获奖公示明细列表
     */
//    @RequiresPermissions("competition:details:export")
    @Log(title = "获奖公示明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response,@RequestBody AwardDetails awardDetails) {

        //将文件上传到服务器，并保存到导出记录中
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
        try {
            threadPoolExecutor.execute(() -> {
                uploadAwardDetailsExportInfo(response, awardDetails,userName,userId,fileMangerId);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }

    private void uploadAwardDetailsExportInfo(HttpServletResponse response, AwardDetails awardDetails, String userName, Long userId, Long fileMangerId) {
        Map<String,Object> fileParam = new HashMap<>();
        try{
            CompetitionApplyInfo competitionApplyInfoReq = new CompetitionApplyInfo();
            if (!"all".equals(awardDetails.getExportType())) {
                BeanUtils.copyProperties(awardDetails, competitionApplyInfoReq);
            }
            competitionApplyInfoReq.setPayStatus(DictConstant.PAID);
            competitionApplyInfoReq.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
            competitionApplyInfoReq.setAwardPublicityId(awardDetails.getAwardPublicityId());
            List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoService.exportCompetitionApplyInfoAwardsList(competitionApplyInfoReq);
            competitionApplyInfoReq.setCompetitionRoleName(null);
            List<TeamManagerInfoAwardsInfo> teamManagerInfoList = teamManagerInfoService.selectTeamManagerInfoAwardsExportList(competitionApplyInfoReq);
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("获奖名单（按人）", applyInfoList);
            sheetDataMap.put("获奖名单（按团队）", teamManagerInfoList);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap,"获奖名单导出.xlsx");
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
            logger.error("上传文件失败",e);
        }
    }

    /**
     * 获取获奖公示明细详细信息
     */
//    @RequiresPermissions("competition:details:query")
//    @GetMapping(value = "/{id}")
//    public AjaxResult getInfo(@PathVariable("id") Long id) {
//        return success(awardDetailsService.selectAwardDetailsById(id));
//    }

    /**
     * 新增获奖公示明细
     */
//    @RequiresPermissions("competition:details:add")
//    @Log(title = "获奖公示明细", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@RequestBody AwardDetails awardDetails) {
//        return toAjax(awardDetailsService.insertAwardDetails(awardDetails));
//    }

    /**
     * 修改获奖公示明细
     */
//    @RequiresPermissions("competition:details:edit")
    @Log(title = "修改获奖公示明细", businessType = BusinessType.UPDATE)
    @PostMapping("/editAwardDetails")
    public AjaxResult edit(@RequestBody List<AwardDetails> awardDetailsList) {
        return toAjax(awardDetailsService.updateAwardDetails(awardDetailsList));
    }

    /**
     * 删除获奖公示明细
     */
//    @RequiresPermissions("competition:details:remove")
    @Log(title = "获奖公示明细", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(awardDetailsService.deleteAwardDetailsById(id));
    }
}
