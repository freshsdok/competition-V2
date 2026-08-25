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
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import com.teaching.competition.service.ITeamManagerInfoService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 赛事申请报名信息Controller
 *
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/competitionApply")
public class CompetitionApplyInfoController extends BaseController {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    @Autowired
    private ITeamManagerInfoService teamManagerInfoService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    /**
     * 查询赛事申请报名信息列表
     */
    @RequiresPermissions("competition:competitionApply:list")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        startPage();
        List<CompetitionApplyInfo> list = competitionApplyInfoService.selectCompetitionApplyInfoList(competitionApplyInfo);
        return getDataTable(list);
    }

    @InnerAuth
    @GetMapping("/selectCompetitionApplyInfoByPayStatus")
    public AjaxResult selectCompetitionApplyInfoByPayStatus() {
        List<CompetitionApplyInfo> list = competitionApplyInfoService.selectCompetitionApplyInfoByPayStatus();
        return success(list);
    }

    /**
     * 导出赛事申请报名信息列表
     */
    @RequiresPermissions("competition:competitionApply:export")
    @Log(title = "赛事申请报名信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(HttpServletResponse response, @RequestBody CompetitionApplyInfo competitionApplyInfo) {
        //将要导出的excel表转换为MultipartFile类型，便于使用upload接口上传到服务器
//        MultipartFile multipartFile = excelUtil.transToMultipartFile(response, list, "上传文件日志");
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
                uploadFileAndUpdateExportInfo(response, competitionApplyInfo,userName,userId,fileMangerId);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭线程
//            shutdownExecutor(threadPoolExecutor);
        }
        return AjaxResult.success("导出成功，请稍后在'导出管理'列表查看文件");
    }

    public void uploadFileAndUpdateExportInfo(HttpServletResponse response,CompetitionApplyInfo competitionApplyInfo,
                                              String userName,Long userId,Long fileMangerId) {
        Map<String,Object> fileParam = new HashMap<>();
        try{
            CompetitionApplyInfo competitionApplyInfoReq = new CompetitionApplyInfo();
            if (!"all".equals(competitionApplyInfo.getExportType())) {
                BeanUtils.copyProperties(competitionApplyInfo, competitionApplyInfoReq);
            }
            competitionApplyInfoReq.setPayStatus(DictConstant.PAID);
            competitionApplyInfoReq.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
            List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoService.exportCompetitionApplyInfoList(competitionApplyInfoReq);
            competitionApplyInfoReq.setCompetitionRoleName(null);
            List<TeamManagerInfo> teamManagerInfoList = teamManagerInfoService.selectTeamManagerInfoListExport(competitionApplyInfoReq);
            Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
            sheetDataMap.put("参赛表", applyInfoList);
            sheetDataMap.put("总表", teamManagerInfoList);
            ExcelUtil excelUtil = new ExcelUtil<>();
            MultipartFile multipartFile = excelUtil.exportExcelData(response, sheetDataMap,"报名信息导出.xlsx");
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
     * 关闭线程池
     */
    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 获取赛事申请报名信息详细信息
     */
    @RequiresPermissions("competition:competitionApply:query")
    @GetMapping(value = "/getApplyDetailInfo/{memberId}")
    public AjaxResult getApplyDetailInfo(@PathVariable("memberId") Long memberId) {
        return success(competitionApplyInfoService.selectCompetitionApplyInfoByMemberId(memberId));
    }

    /**
     * 获取赛事申请报名信息详细信息内部调用
     */
    @InnerAuth
    @GetMapping(value = "/getInnerApplyDetailInfo/{memberId}")
    public AjaxResult getInnerApplyDetailInfo(@PathVariable("memberId") Long memberId) {
        return success(competitionApplyInfoService.selectCompetitionApplyInfoByMemberId(memberId));
    }


    @InnerAuth
    @PostMapping(value = "/getInnerApplyDetailInfo")
    public AjaxResult selectCompetitionApplyInfoListByUserId(@RequestBody Map<String, Object> param) {
        return success(competitionApplyInfoService.selectCompetitionApplyInfoListByUserId(param));
    }

    // 查用户参赛信息根据身份证
    @InnerAuth
    @PostMapping(value = "/getInnerApplyUserInfo")
    public AjaxResult getInnerApplyUserInfo(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        return success(competitionApplyInfoService.getInnerApplyUserInfo(competitionApplyInfo));
    }

    /**
     * 新增赛事申请报名信息
     */
    @RequiresPermissions("system:info:add")
    @Log(title = "个人赛事申请报名信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionApplyInfo")
    public AjaxResult add(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        return toAjax(competitionApplyInfoService.insertCompetitionApplyInfo(competitionApplyInfo));
    }

    /**
     * 修改赛事申请报名信息
     */
    @RequiresPermissions("competition:competitionApply:edit")
    @Log(title = "修改赛事申请报名信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionApplyInfo")
    public AjaxResult edit(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        return toAjax(competitionApplyInfoService.updateCompetitionApplyInfo(competitionApplyInfo));
    }

    // 远程获取个人发票信息
    @InnerAuth
    @PostMapping("/queryTeamMemberInvoiceStatus")
    public AjaxResult queryTeamMemberInvoiceStatus(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        List<CompetitionApplyInfo> list = competitionApplyInfoService.queryTeamMemberInvoiceStatus(competitionApplyInfo);
        return success(list);
    }

    // 远程修改支付状态或发票状态
    @InnerAuth
    @PostMapping("/updatePayStatus")
    public AjaxResult updatePayStatus(@RequestBody List<CompetitionApplyInfo> competitionApplyInfoList) {
        return toAjax(competitionApplyInfoService.updatePayStatus(competitionApplyInfoList));
    }

    /**
     * 修改赛事申请报名信息状态
     */
    @InnerAuth
    @PostMapping("/updateCompetitionApplyInfoStatus")
    public AjaxResult updateCompetitionApplyInfoStatus(@RequestBody CompetitionApplyInfo competitionApplyInfo) {
        return success(competitionApplyInfoService.updateCompetitionApplyInfoStatus(competitionApplyInfo));
    }

    /**
     * 删除赛事申请报名信息
     */
    @RequiresPermissions("competition:competitionApply:remove")
    @Log(title = "赛事申请报名信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberIds}")
    public AjaxResult remove(@PathVariable Long[] memberIds) {
        return toAjax(competitionApplyInfoService.deleteCompetitionApplyInfoByMemberIds(memberIds));
    }

    // 退赛删除团队
    @InnerAuth
    @GetMapping("/removeTeam/{teamCode}")
    public AjaxResult removeTeam(@PathVariable String teamCode) {
        return toAjax(competitionApplyInfoService.deleteCompetitionApplyInfoByTeamCode(teamCode));
    }

    @InnerAuth
    @PostMapping("/saveBatchCompetitionApplyInfo")
    public AjaxResult batchInsertCompetitionApplyInfo(@RequestBody List<CompetitionApplyInfo> competitionApplyInfoList) {
        return toAjax(competitionApplyInfoService.batchInsertCompetitionApplyInfo(competitionApplyInfoList));
    }

    //删除报名人员
    @InnerAuth
    @GetMapping("/removeApplyInfo/{memberIds}")
    public AjaxResult remove(@PathVariable String memberIds) {
        Long[] memberIdsLong = Arrays.stream(memberIds.split(",")).map(Long::parseLong).toArray(Long[]::new);
        return toAjax(competitionApplyInfoService.deleteCompetitionApplyInfoByMemberIds(memberIdsLong));
    }

    @InnerAuth
    @GetMapping("/selectCompetitionApplyTeamCode/{teamCode}")
    public AjaxResult selectCompetitionApplyTeamCode(@PathVariable String teamCode) {
        return success(competitionApplyInfoService.selectCompetitionApplyTeamCode(teamCode));
    }

    /**
     * 查询某userId已缴费的报名信息
     *
     * @param userId
     * @return
     */
    @InnerAuth
    @GetMapping("/getCompetitionApplyInfoByPayStatusForUserGroup/{userId}")
    public AjaxResult selectCompetitionApplyInfoByPayStatus(@PathVariable Long userId) {
        List<CompetitionApplyInfo> list = competitionApplyInfoService.getCompetitionApplyInfoByPayStatusForUserGroup(userId);
        return success(list);
    }

    /**
     * 查询匹配规则的报名成功已缴费的人员信息
     *
     * @param map
     * @return
     */
    @InnerAuth
    @PostMapping("/getUserInfoByCompetitions")
    public AjaxResult getUserInfoByCompetitions(@RequestBody Map<String, Object> map) {
        Set<Long> list = competitionApplyInfoService.getUserInfoByCompetitions(map);
        return success(list);
    }

    @InnerAuth
    @PostMapping("/selectAllUserInfoByCompetitions")
    public AjaxResult selectAllUserInfoByCompetitions(@RequestBody Map<String, Object> map) {
        List<CompetitionApplyInfo> list= competitionApplyInfoService.selectAllUserInfoByCompetitions(map);
        return success(list);
    }

    /**
     * 根据userId和competitionId查询团队的报名信息
     * @param userId
     * @param competitionId
     * @return
     */
    @InnerAuth
    @GetMapping("/getApplyInfoByUsrIdAndCompetitionId/{userId}/{competitionId}")
    public AjaxResult getApplyInfoByUsrIdAndCompetitionId(@PathVariable Long userId,@PathVariable Long competitionId) {
        List<CompetitionApplyInfo> list= competitionApplyInfoService.getApplyInfoByUsrIdAndCompetitionId(userId,competitionId);
        return success(list);
    }

    /**
     * 根据赛事系列id查询报名信息
     * @param competitionId
     * @return
     */
    @InnerAuth
    @GetMapping("/getApplyInfoByUsrIdAndCompetitionId/{competitionId}")
    public AjaxResult getCompetitionApplyInfoListByCompetitionSeriesId(@PathVariable Long competitionId) {
        List<CompetitionApplyInfo> list= competitionApplyInfoService.getCompetitionApplyInfoListByCompetitionSeriesId(competitionId);
        return success(list);
    }
}
