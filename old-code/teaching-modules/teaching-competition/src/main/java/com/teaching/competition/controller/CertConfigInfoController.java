package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.core.constant.DictConstant;
import com.teaching.competition.domain.CertCompetitionApplyInfo;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.teaching.system.api.domain.CertConfigInfo;
import com.teaching.competition.service.ICertConfigInfoService;

/**
 * 证书配置信息Controller
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/competition/certConfigInfo")
public class CertConfigInfoController extends BaseController {
    @Autowired
    private ICertConfigInfoService certConfigInfoService;

    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    /**
     * 查询证书配置信息列表(赛证互通配置也需要查列表去除权限字符)
     */
//    @RequiresPermissions("competition:certConfigInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CertConfigInfo certConfigInfo) {
        startPage();
        List<CertConfigInfo> list = certConfigInfoService.selectCertConfigInfoList(certConfigInfo);
        return getDataTable(list);
    }

    /**
     * 导出证书配置信息列表
     */
    @RequiresPermissions("competition:certConfigInfo:export")
    @Log(title = "证书配置信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CertConfigInfo certConfigInfo) {
        List<CertConfigInfo> list = certConfigInfoService.selectCertConfigInfoList(certConfigInfo);
        ExcelUtil<CertConfigInfo> util = new ExcelUtil<CertConfigInfo>(CertConfigInfo.class);
        util.exportExcel(response, list, "证书配置信息数据");
    }

    /**
     * 获取证书配置信息详细信息
     */
    @RequiresPermissions("competition:certConfigInfo:query")
    @GetMapping("/getCertConfigInfo/{certConfigId}")
    public AjaxResult getInfo(@PathVariable("certConfigId") Long certConfigId) {
        return success(certConfigInfoService.selectCertConfigInfoById(certConfigId));
    }

    /**
     * 新增证书配置信息
     */
    @RequiresPermissions("competition:certConfigInfo:add")
    @Log(title = "证书配置信息", businessType = BusinessType.INSERT)
    @PostMapping("/addCertConfigInfo")
    public AjaxResult add(@RequestBody CertConfigInfo certConfigInfo) {
        return toAjax(certConfigInfoService.insertCertConfigInfo(certConfigInfo));
    }

    /**
     * 修改证书配置信息
     */
    @RequiresPermissions("competition:certConfigInfo:edit")
    @Log(title = "证书配置信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCertConfigInfo")
    public AjaxResult edit(@RequestBody CertConfigInfo certConfigInfo) {
        return toAjax(certConfigInfoService.updateCertConfigInfo(certConfigInfo));
    }

    /**
     * 删除证书配置信息
     */
    @RequiresPermissions("competition:certConfigInfo:remove")
    @Log(title = "证书配置信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certConfigIds}")
    public AjaxResult remove(@PathVariable Long[] certConfigIds) {
        return toAjax(certConfigInfoService.deleteCertConfigInfoByIds(certConfigIds));
    }

    /**
     * 颁发证书列表人员列表信息
     */
    @RequiresPermissions("competition:certConfigInfo:issue")
    @Log(title = "颁发证书列表人员列表信息", businessType = BusinessType.OTHER)
    @PostMapping("/cert/getCompetitionApplyInfo")
    public TableDataInfo issueCompetitionApplyInfo(@RequestBody CertCompetitionApplyInfo certCompetitionApplyInfo) {
        startPage();
        certCompetitionApplyInfo.setPayStatus(DictConstant.PAID);
        List<CertCompetitionApplyInfo> list = competitionApplyInfoService.selectCertCompetitionApplyInfoList(certCompetitionApplyInfo);
        return getDataTable(list);
    }
}