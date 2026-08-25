package com.teaching.system.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.WechatIntegration;
import com.teaching.system.service.IWechatIntegrationService;
import com.teaching.common.security.annotation.RequiresPermissions;

/**
 * 微信集成信息操作处理
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/wechatIntegration")
public class WechatIntegrationController extends BaseController
{
    @Autowired
    private IWechatIntegrationService wechatIntegrationService;

    /**
     * 获取微信集成列表
     */
    @RequiresPermissions("system:wechatIntegration:list")
    @GetMapping("/list")
    public TableDataInfo list(WechatIntegration wechatIntegration)
    {
        startPage();
        List<WechatIntegration> list = wechatIntegrationService.selectWechatIntegrationList(wechatIntegration);
        return getDataTable(list);
    }

    /**
     * 根据微信集成编号获取详细信息
     */
    @RequiresPermissions("system:wechatIntegration:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(wechatIntegrationService.selectWechatIntegrationById(id));
    }

    /**
     * 新增微信集成
     */
    // @RequiresPermissions("system:wechatIntegration:add")
    @Log(title = "微信集成", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WechatIntegration wechatIntegration)
    {
        wechatIntegration.setCreateBy(SecurityUtils.getUsername());
        return toAjax(wechatIntegrationService.insertWechatIntegration(wechatIntegration));
    }

    /**
     * 修改微信集成
     */
    // @RequiresPermissions("system:wechatIntegration:edit")
    @Log(title = "微信集成", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WechatIntegration wechatIntegration)
    {
        wechatIntegration.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(wechatIntegrationService.updateWechatIntegration(wechatIntegration));
    }

    /**
     * 删除微信集成
     */
    @RequiresPermissions("system:wechatIntegration:remove")
    @Log(title = "微信集成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wechatIntegrationService.deleteWechatIntegrationByIds(ids));
    }


    /**
     * 重置查询条件
     */
    // @RequiresPermissions("system:wechatIntegration:list")
    @PostMapping("/reset")
    public AjaxResult reset()
    {
        wechatIntegrationService.resetWechatIntegrationQuery();
        return success("查询条件已重置");
    }
}
