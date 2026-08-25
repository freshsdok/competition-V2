package com.teaching.competition.controller;

import java.util.List;
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
import com.teaching.competition.domain.CertOrgInfo;
import com.teaching.competition.service.ICertOrgInfoService;

/**
 * 证书颁发机构Controller
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/competition/certOrgInfo")
public class CertOrgInfoController extends BaseController {
    @Autowired
    private ICertOrgInfoService certOrgInfoService;

    /**
     * 查询证书颁发机构列表
     */
//    @RequiresPermissions("competition:certOrgInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CertOrgInfo certOrgInfo) {
        startPage();
        List<CertOrgInfo> list = certOrgInfoService.selectCertOrgInfoList(certOrgInfo);
        return getDataTable(list);
    }

    /**
     * 导出证书颁发机构列表
     */
    @RequiresPermissions("competition:certOrgInfo:export")
    @Log(title = "证书颁发机构", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CertOrgInfo certOrgInfo) {
        List<CertOrgInfo> list = certOrgInfoService.selectCertOrgInfoList(certOrgInfo);
        ExcelUtil<CertOrgInfo> util = new ExcelUtil<CertOrgInfo>(CertOrgInfo.class);
        util.exportExcel(response, list, "证书颁发机构数据");
    }

    /**
     * 获取证书颁发机构详细信息
     */
    @RequiresPermissions("competition:certOrgInfo:query")
    @GetMapping("/{orgId}")
    public AjaxResult getInfo(@PathVariable("orgId") Long orgId) {
        return success(certOrgInfoService.selectCertOrgInfoById(orgId));
    }

    /**
     * 新增证书颁发机构
     */
    @RequiresPermissions("competition:certOrgInfo:add")
    @Log(title = "证书颁发机构", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CertOrgInfo certOrgInfo) {
        return toAjax(certOrgInfoService.insertCertOrgInfo(certOrgInfo));
    }

    /**
     * 修改证书颁发机构
     */
    @RequiresPermissions("competition:certOrgInfo:edit")
    @Log(title = "证书颁发机构", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CertOrgInfo certOrgInfo) {
        return toAjax(certOrgInfoService.updateCertOrgInfo(certOrgInfo));
    }

    /**
     * 删除证书颁发机构
     */
    @RequiresPermissions("competition:certOrgInfo:remove")
    @Log(title = "证书颁发机构", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orgIds}")
    public AjaxResult remove(@PathVariable Long[] orgIds) {
        return toAjax(certOrgInfoService.deleteCertOrgInfoByIds(orgIds));
    }
}