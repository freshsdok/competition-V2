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
import com.teaching.competition.domain.CertPlayerInfo;
import com.teaching.competition.service.ICertPlayerInfoService;

/**
 * 证书人员Controller
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/competition/certPlayerInfo")
public class CertPlayerInfoController extends BaseController {
    @Autowired
    private ICertPlayerInfoService certPlayerInfoService;

    /**
     * 查询证书人员列表
     */
    @RequiresPermissions("competition:certPlayerInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CertPlayerInfo certPlayerInfo) {
        startPage();
        List<CertPlayerInfo> list = certPlayerInfoService.selectCertPlayerInfoList(certPlayerInfo);
        return getDataTable(list);
    }

    /**
     * 导出证书人员列表
     */
    @RequiresPermissions("competition:certPlayerInfo:export")
    @Log(title = "证书人员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CertPlayerInfo certPlayerInfo) {
        List<CertPlayerInfo> list = certPlayerInfoService.selectCertPlayerInfoList(certPlayerInfo);
        ExcelUtil<CertPlayerInfo> util = new ExcelUtil<CertPlayerInfo>(CertPlayerInfo.class);
        util.exportExcel(response, list, "证书人员数据");
    }

    /**
     * 获取证书人员详细信息
     */
    @RequiresPermissions("competition:certPlayerInfo:query")
    @GetMapping("/{relaId}")
    public AjaxResult getInfo(@PathVariable("relaId") Long relaId) {
        return success(certPlayerInfoService.selectCertPlayerInfoById(relaId));
    }

    /**
     * 新增证书人员
     */
    @RequiresPermissions("competition:certPlayerInfo:add")
    @Log(title = "证书人员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CertPlayerInfo certPlayerInfo) {
        return toAjax(certPlayerInfoService.insertCertPlayerInfo(certPlayerInfo));
    }

    /**
     * 批量新增证书人员
     */
    @RequiresPermissions("competition:certPlayerInfo:add")
    @Log(title = "证书人员", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<CertPlayerInfo> certPlayerInfoList) {
        return toAjax(certPlayerInfoService.batchInsertCertPlayerInfo(certPlayerInfoList));
    }

    /**
     * 修改证书人员
     */
    @RequiresPermissions("competition:certPlayerInfo:edit")
    @Log(title = "证书人员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CertPlayerInfo certPlayerInfo) {
        return toAjax(certPlayerInfoService.updateCertPlayerInfo(certPlayerInfo));
    }

    /**
     * 删除证书人员
     */
    @RequiresPermissions("competition:certPlayerInfo:remove")
    @Log(title = "证书人员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{relaIds}")
    public AjaxResult remove(@PathVariable Long[] relaIds) {
        return toAjax(certPlayerInfoService.deleteCertPlayerInfoByIds(relaIds));
    }
}
