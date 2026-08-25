package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import java.util.Map;

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
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.InvoicePerInfo;
import com.teaching.system.service.IInvoicePerInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 用户端开票信息记录Controller
 * 
 * @author teaching
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/invoicePerInfo")
public class InvoicePerInfoController extends BaseController
{
    @Autowired
    private IInvoicePerInfoService invoicePerInfoService;

    /**
     * 查询开票信息记录列表
     */
//    @RequiresPermissions("system:invoicePerInfo:list")
    @GetMapping("/list")
    public AjaxResult list(InvoicePerInfo invoicePerInfo) {
        List<InvoicePerInfo> list = invoicePerInfoService.selectInvoicePerInfoList(invoicePerInfo);
        return success(list);
    }

    @GetMapping("/selectInvoicePerInfo")
    public AjaxResult selectInvoicePerInfo(InvoicePerInfo invoicePerInfo) {
        invoicePerInfo.setUserId(String.valueOf(SecurityUtils.getUserId()));
        List<Map<String,Object>> list = invoicePerInfoService.selectInvoicePerInfo(invoicePerInfo);
        return success(list);
    }

    /**
     * 导出开票信息记录列表
     */
//    @RequiresPermissions("system:invoicePerInfo:export")
    @Log(title = "开票信息记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvoicePerInfo invoicePerInfo)
    {
        List<InvoicePerInfo> list = invoicePerInfoService.selectInvoicePerInfoList(invoicePerInfo);
        ExcelUtil<InvoicePerInfo> util = new ExcelUtil<InvoicePerInfo>(InvoicePerInfo.class);
        util.exportExcel(response, list, "开票信息记录数据");
    }

    /**
     * 获取开票信息记录详细信息
     */
    @RequiresPermissions("system:invoicePerInfo:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(invoicePerInfoService.selectInvoicePerInfoById(id));
    }

    /**
     * 新增开票信息记录
     */
//    @RequiresPermissions("system:invoicePerInfo:add")
    @Log(title = "开票信息记录", businessType = BusinessType.INSERT)
    @PostMapping("/saveInvoicePerInfo")
    public AjaxResult add(@RequestBody InvoicePerInfo invoicePerInfo)
    {
        return toAjax(invoicePerInfoService.insertInvoicePerInfo(invoicePerInfo));
    }

    /**
     * 修改开票信息记录
     */
//    @RequiresPermissions("system:invoicePerInfo:edit")
    @Log(title = "开票信息记录", businessType = BusinessType.UPDATE)
    @PostMapping("/updateInvoicePerInfo")
    public AjaxResult edit(@RequestBody InvoicePerInfo invoicePerInfo)
    {
        return toAjax(invoicePerInfoService.updateInvoicePerInfo(invoicePerInfo));
    }

    /**
     * 删除开票信息记录
     */
//    @RequiresPermissions("system:invoicePerInfo:remove")
    @Log(title = "开票信息记录", businessType = BusinessType.DELETE)
	@GetMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(invoicePerInfoService.deleteInvoicePerInfoByIds(ids));
    }
}
