package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.MessageTemplateSource;
import com.teaching.system.service.IMessageTemplateSourceService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 短信模板Controller
 * 
 * @author teaching
 * @date 2025-12-22
 */
@RestController
@RequestMapping("/messageTemplate")
public class MessageTemplateSourceController extends BaseController
{
    @Autowired
    private IMessageTemplateSourceService messageTemplateSourceService;

    /**
     * 查询短信模板列表
     */
    @RequiresPermissions("system:source:list")
    @GetMapping("/list")
    public TableDataInfo list(MessageTemplateSource messageTemplateSource)
    {
        startPage();
        List<MessageTemplateSource> list = messageTemplateSourceService.selectMessageTemplateSourceList(messageTemplateSource);
        return getDataTable(list);
    }

    /**
     * 导出短信模板列表
     */
    @RequiresPermissions("system:source:export")
    @Log(title = "短信模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MessageTemplateSource messageTemplateSource)
    {
        List<MessageTemplateSource> list = messageTemplateSourceService.selectMessageTemplateSourceList(messageTemplateSource);
        ExcelUtil<MessageTemplateSource> util = new ExcelUtil<MessageTemplateSource>(MessageTemplateSource.class);
        util.exportExcel(response, list, "短信模板数据");
    }

    /**
     * 获取短信模板详细信息
     */
    @RequiresPermissions("system:source:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(messageTemplateSourceService.selectMessageTemplateSourceById(id));
    }

    // getTemplateAttributeValue
    @GetMapping(value = "/getTemplateAttributeValue")
    public AjaxResult getTemplateAttributeValue(@RequestParam Map<String, Object> params) {
        return success(messageTemplateSourceService.getTemplateAttributeValue(params.get("templateCode").toString(),
                Long.valueOf(params.get("userId").toString())));
    }

    /**
     * 新增短信模板
     */
    @RequiresPermissions("system:source:add")
    @Log(title = "短信模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MessageTemplateSource messageTemplateSource)
    {
        return toAjax(messageTemplateSourceService.insertMessageTemplateSource(messageTemplateSource));
    }

    /**
     * 修改短信模板
     */
    @RequiresPermissions("system:source:edit")
    @Log(title = "短信模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MessageTemplateSource messageTemplateSource)
    {
        return toAjax(messageTemplateSourceService.updateMessageTemplateSource(messageTemplateSource));
    }

    /**
     * 删除短信模板
     */
    @RequiresPermissions("system:source:remove")
    @Log(title = "短信模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(messageTemplateSourceService.deleteMessageTemplateSourceByIds(ids));
    }

    /**
     * 获取短信模板配置
     * @return
     */
    @GetMapping("/getTemplateSource")
    public AjaxResult getTemplateSource()
    {
        return success(messageTemplateSourceService.getTemplateSource());
    }
}
