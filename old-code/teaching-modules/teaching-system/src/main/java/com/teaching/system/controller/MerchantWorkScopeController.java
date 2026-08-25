package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.MerchantWorkScope;
import com.teaching.system.service.IMerchantWorkScopeService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 商户作用范围Controller
 * 
 * @author teaching
 * @date 2025-12-23
 */
@RestController
@RequestMapping("/merchantWorkScope")
public class MerchantWorkScopeController extends BaseController
{
    @Autowired
    private IMerchantWorkScopeService merchantWorkScopeService;

    /**
     * 查询商户作用范围列表
     */
    @RequiresPermissions("system:scope:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantWorkScope merchantWorkScope)
    {
        startPage();
        List<MerchantWorkScope> list = merchantWorkScopeService.selectMerchantWorkScopeList(merchantWorkScope);
        return getDataTable(list);
    }

    /**
     * 导出商户作用范围列表
     */
    @RequiresPermissions("system:scope:export")
    @Log(title = "商户作用范围", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MerchantWorkScope merchantWorkScope)
    {
        List<MerchantWorkScope> list = merchantWorkScopeService.selectMerchantWorkScopeList(merchantWorkScope);
        ExcelUtil<MerchantWorkScope> util = new ExcelUtil<MerchantWorkScope>(MerchantWorkScope.class);
        util.exportExcel(response, list, "商户作用范围数据");
    }

    /**
     * 获取商户作用范围详细信息
     */
    @RequiresPermissions("system:scope:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(merchantWorkScopeService.selectMerchantWorkScopeById(id));
    }

    /**
     * 新增商户作用范围
     */
    @RequiresPermissions("system:scope:add")
    @Log(title = "商户作用范围", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MerchantWorkScope merchantWorkScope)
    {
        return toAjax(merchantWorkScopeService.insertMerchantWorkScope(merchantWorkScope));
    }

    /**
     * 修改商户作用范围
     */
    @RequiresPermissions("system:scope:edit")
    @Log(title = "商户作用范围", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MerchantWorkScope merchantWorkScope)
    {
        return toAjax(merchantWorkScopeService.updateMerchantWorkScope(merchantWorkScope));
    }

    /**
     * 删除商户作用范围
     */
    @RequiresPermissions("system:scope:remove")
    @Log(title = "商户作用范围", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(merchantWorkScopeService.deleteMerchantWorkScopeByIds(ids));
    }
}
