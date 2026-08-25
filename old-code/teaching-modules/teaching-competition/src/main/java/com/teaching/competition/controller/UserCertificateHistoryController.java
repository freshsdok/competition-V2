package com.teaching.competition.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.UserCertificateHistory;
import com.teaching.competition.service.IUserCertificateHistoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户证书历史Controller
 *
 * @author teaching
 * @date 2026-05-13
 */
@RestController
@RequestMapping("/certificateHistory")
public class UserCertificateHistoryController extends BaseController {
    @Autowired
    private IUserCertificateHistoryService userCertificateHistoryService;

    /**
     * 查询用户证书历史列表
     */
    @RequiresPermissions("competition:certificateHistory:list")
    @GetMapping("/list")
    public TableDataInfo list(UserCertificateHistory userCertificateHistory) {
        startPage();
        List<UserCertificateHistory> list = userCertificateHistoryService.selectUserCertificateHistoryList(userCertificateHistory);
        return getDataTable(list);
    }

    /**
     * 导出用户证书历史列表
     */
    @RequiresPermissions("competition:certificateHistory:export")
    @Log(title = "用户证书历史", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserCertificateHistory userCertificateHistory) {
        List<UserCertificateHistory> list = userCertificateHistoryService.selectUserCertificateHistoryList(userCertificateHistory);
        ExcelUtil<UserCertificateHistory> util = new ExcelUtil<UserCertificateHistory>(UserCertificateHistory.class);
        util.exportExcel(response, list, "用户证书历史数据");
    }

    /**
     * 获取用户证书历史详细信息
     */
    @RequiresPermissions("competition:certificateHistory:query")
    @GetMapping(value = "/{certId}")
    public AjaxResult getInfo(@PathVariable("certId") Long certId) {
        return AjaxResult.success(userCertificateHistoryService.selectUserCertificateHistoryById(certId));
    }

    /**
     * 新增用户证书历史
     */
    @RequiresPermissions("competition:certificateHistory:add")
    @Log(title = "用户证书历史", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserCertificateHistory userCertificateHistory) {
        return toAjax(userCertificateHistoryService.insertUserCertificateHistory(userCertificateHistory));
    }

    /**
     * 修改用户证书历史
     */
    @RequiresPermissions("competition:certificateHistory:edit")
    @Log(title = "用户证书历史", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserCertificateHistory userCertificateHistory) {
        return toAjax(userCertificateHistoryService.updateUserCertificateHistory(userCertificateHistory));
    }

    /**
     * 删除用户证书历史
     */
    @RequiresPermissions("competition:certificateHistory:remove")
    @Log(title = "用户证书历史", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds) {
        return toAjax(userCertificateHistoryService.deleteUserCertificateHistoryByIds(certIds));
    }

    /**
     * 导入用户证书历史列表
     */
    @RequiresPermissions("competition:certificateHistory:import")
    @Log(title = "用户证书历史", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(MultipartFile file) throws Exception {
        ExcelUtil<UserCertificateHistory> util = new ExcelUtil<UserCertificateHistory>(UserCertificateHistory.class);
        List<UserCertificateHistory> list = util.importExcel(file.getInputStream());
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (UserCertificateHistory userCertificateHistory : list) {
            try {
                userCertificateHistoryService.insertUserCertificateHistory(userCertificateHistory);
                successNum++;
            } catch (Exception e) {
                failureNum++;
                failureMsg.append("<br/>" + successNum + "、证书编号 " + userCertificateHistory.getCertCode() + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            return AjaxResult.error(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
            return AjaxResult.success(successMsg.toString());
        }
    }
}
