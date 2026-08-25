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
import com.teaching.competition.domain.CertExchangeRuleDetail;
import com.teaching.competition.service.ICertExchangeRuleDetailService;

/**
 * 赛证互通规则明细Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/certExchangeRuleDetail")
public class CertExchangeRuleDetailController extends BaseController {
    @Autowired
    private ICertExchangeRuleDetailService certExchangeRuleDetailService;

    /**
     * 查询赛证互通规则明细列表
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:list")
    @GetMapping("/list")
    public TableDataInfo list(CertExchangeRuleDetail certExchangeRuleDetail) {
        startPage();
        List<CertExchangeRuleDetail> list = certExchangeRuleDetailService.selectCertExchangeRuleDetailList(certExchangeRuleDetail);
        return getDataTable(list);
    }

    /**
     * 导出赛证互通规则明细列表
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:export")
    @Log(title = "赛证互通规则明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CertExchangeRuleDetail certExchangeRuleDetail) {
        List<CertExchangeRuleDetail> list = certExchangeRuleDetailService.selectCertExchangeRuleDetailList(certExchangeRuleDetail);
        ExcelUtil<CertExchangeRuleDetail> util = new ExcelUtil<CertExchangeRuleDetail>(CertExchangeRuleDetail.class);
        util.exportExcel(response, list, "赛证互通规则明细数据");
    }

    /**
     * 获取赛证互通规则明细详细信息
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:query")
    @GetMapping("/{detailId}")
    public AjaxResult getInfo(@PathVariable("detailId") Long detailId) {
        return success(certExchangeRuleDetailService.selectCertExchangeRuleDetailById(detailId));
    }

    /**
     * 新增赛证互通规则明细
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:add")
    @Log(title = "赛证互通规则明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CertExchangeRuleDetail certExchangeRuleDetail) {
        return toAjax(certExchangeRuleDetailService.insertCertExchangeRuleDetail(certExchangeRuleDetail));
    }

    /**
     * 修改赛证互通规则明细
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:edit")
    @Log(title = "赛证互通规则明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CertExchangeRuleDetail certExchangeRuleDetail) {
        return toAjax(certExchangeRuleDetailService.updateCertExchangeRuleDetail(certExchangeRuleDetail));
    }

    /**
     * 删除赛证互通规则明细
     */
    @RequiresPermissions("competition:certExchangeRuleDetail:remove")
    @Log(title = "赛证互通规则明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{detailIds}")
    public AjaxResult remove(@PathVariable Long[] detailIds) {
        return toAjax(certExchangeRuleDetailService.deleteCertExchangeRuleDetailByIds(detailIds));
    }
}
