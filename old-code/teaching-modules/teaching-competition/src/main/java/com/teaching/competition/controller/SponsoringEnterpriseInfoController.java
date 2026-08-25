package com.teaching.competition.controller;

import java.util.List;

import com.teaching.competition.domain.SponsoringEnterpriseInfo;
import com.teaching.competition.service.ISponsoringEnterpriseInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 赞助企业信息Controller
 * 
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/sponsoringEnterprise")
public class SponsoringEnterpriseInfoController extends BaseController
{
    @Autowired
    private ISponsoringEnterpriseInfoService sponsoringEnterpriseInfoService;

    /**
     * 查询赞助企业信息列表
     */
    @RequiresPermissions("competition:sponsoringEnterprise:list")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        startPage();
        List<SponsoringEnterpriseInfo> list = sponsoringEnterpriseInfoService.selectSponsoringEnterpriseInfoList(sponsoringEnterpriseInfo);
        return getDataTable(list);
    }

    /**
     * 导出赞助企业信息列表
     */
    @RequiresPermissions("competition:sponsoringEnterprise:export")
    @Log(title = "赞助企业信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        List<SponsoringEnterpriseInfo> list = sponsoringEnterpriseInfoService.selectSponsoringEnterpriseInfoList(sponsoringEnterpriseInfo);
        ExcelUtil<SponsoringEnterpriseInfo> util = new ExcelUtil<SponsoringEnterpriseInfo>(SponsoringEnterpriseInfo.class);
        util.exportExcel(response, list, "赞助企业信息数据");
    }

    /**
     * 获取赞助企业信息详细信息
     */
    @RequiresPermissions("competition:sponsoringEnterprise:query")
    @GetMapping(value = "/getSponsoringEnterpriseDetailInfo/{enterpriseId}")
    public AjaxResult getSponsoringEnterpriseDetailInfo(@PathVariable("enterpriseId") Long enterpriseId)
    {
        return success(sponsoringEnterpriseInfoService.selectSponsoringEnterpriseInfoByEnterpriseId(enterpriseId));
    }

    /**
     * 新增赞助企业信息
     */
    @RequiresPermissions("competition:sponsoringEnterprise:add")
    @Log(title = "赞助企业信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveSponsoringEnterpriseInfo")
    public AjaxResult add(@RequestBody SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        return toAjax(sponsoringEnterpriseInfoService.insertSponsoringEnterpriseInfo(sponsoringEnterpriseInfo));
    }

    /**
     * 修改赞助企业信息
     */
    @RequiresPermissions("competition:sponsoringEnterprise:edit")
    @Log(title = "赞助企业信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateSponsoringEnterpriseInfo")
    public AjaxResult edit(@RequestBody SponsoringEnterpriseInfo sponsoringEnterpriseInfo)
    {
        return toAjax(sponsoringEnterpriseInfoService.updateSponsoringEnterpriseInfo(sponsoringEnterpriseInfo));
    }

    /**
     * 删除赞助企业信息
     */
    @RequiresPermissions("competition:sponsoringEnterprise:remove")
    @Log(title = "赞助企业信息", businessType = BusinessType.DELETE)
	@GetMapping("/removeSponsoringEnterpriseInfo/{enterpriseIds}")
    public AjaxResult remove(@PathVariable String enterpriseIds)
    {
        return toAjax(sponsoringEnterpriseInfoService.deleteSponsoringEnterpriseInfoByEnterpriseIds(enterpriseIds));
    }
}
