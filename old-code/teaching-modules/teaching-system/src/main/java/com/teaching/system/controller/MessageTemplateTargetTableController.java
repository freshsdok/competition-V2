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
import com.teaching.system.domain.MessageTemplateTargetTable;
import com.teaching.system.service.IMessageTemplateTargetTableService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 短信模板目标Controller
 * 
 * @author teaching
 * @date 2025-12-22
 */
@RestController
@RequestMapping("/table")
public class MessageTemplateTargetTableController extends BaseController
{
    @Autowired
    private IMessageTemplateTargetTableService messageTemplateTargetTableService;

    /**
     * 查询短信模板目标列表
     */
    @RequiresPermissions("system:table:list")
    @GetMapping("/list")
    public TableDataInfo list(MessageTemplateTargetTable messageTemplateTargetTable)
    {
        startPage();
        List<MessageTemplateTargetTable> list = messageTemplateTargetTableService.selectMessageTemplateTargetTableList(messageTemplateTargetTable);
        return getDataTable(list);
    }

    /**
     * 导出短信模板目标列表
     */
    @RequiresPermissions("system:table:export")
    @Log(title = "短信模板目标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MessageTemplateTargetTable messageTemplateTargetTable)
    {
        List<MessageTemplateTargetTable> list = messageTemplateTargetTableService.selectMessageTemplateTargetTableList(messageTemplateTargetTable);
        ExcelUtil<MessageTemplateTargetTable> util = new ExcelUtil<MessageTemplateTargetTable>(MessageTemplateTargetTable.class);
        util.exportExcel(response, list, "短信模板目标数据");
    }

    /**
     * 获取短信模板目标详细信息
     */
    @RequiresPermissions("system:table:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(messageTemplateTargetTableService.selectMessageTemplateTargetTableById(id));
    }

    /**
     * 新增短信模板目标
     */
    @RequiresPermissions("system:table:add")
    @Log(title = "短信模板目标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MessageTemplateTargetTable messageTemplateTargetTable)
    {
        return toAjax(messageTemplateTargetTableService.insertMessageTemplateTargetTable(messageTemplateTargetTable));
    }

    /**
     * 修改短信模板目标
     */
    @RequiresPermissions("system:table:edit")
    @Log(title = "短信模板目标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MessageTemplateTargetTable messageTemplateTargetTable)
    {
        return toAjax(messageTemplateTargetTableService.updateMessageTemplateTargetTable(messageTemplateTargetTable));
    }

    /**
     * 删除短信模板目标
     */
    @RequiresPermissions("system:table:remove")
    @Log(title = "短信模板目标", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(messageTemplateTargetTableService.deleteMessageTemplateTargetTableByIds(ids));
    }
}
