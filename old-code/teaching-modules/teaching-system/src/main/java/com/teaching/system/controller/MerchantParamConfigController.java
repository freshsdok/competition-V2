package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.MerchantParamConfig;
import com.teaching.system.service.IMerchantParamConfigService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 商户参数配置（支付和发票）Controller
 * 
 * @author teaching
 * @date 2025-12-23
 */
@RestController
@RequestMapping("/merchantParamConfig")
public class MerchantParamConfigController extends BaseController
{
    @Autowired
    private IMerchantParamConfigService merchantParamConfigService;

    /**
     * 查询商户参数配置（支付和发票）列表
     */
    @RequiresPermissions("system:config:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantParamConfig merchantParamConfig)
    {
        startPage();
        List<MerchantParamConfig> list = merchantParamConfigService.selectMerchantParamConfigList(merchantParamConfig);
        return getDataTable(list);
    }

    /**
     * 导出商户参数配置（支付和发票）列表
     */
    @RequiresPermissions("system:config:export")
    @Log(title = "商户参数配置（支付和发票）", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MerchantParamConfig merchantParamConfig)
    {
        List<MerchantParamConfig> list = merchantParamConfigService.selectMerchantParamConfigList(merchantParamConfig);
        ExcelUtil<MerchantParamConfig> util = new ExcelUtil<MerchantParamConfig>(MerchantParamConfig.class);
        util.exportExcel(response, list, "商户参数配置（支付和发票）数据");
    }

    /**
     * 获取商户参数配置（支付和发票）详细信息
     */
    @RequiresPermissions("system:config:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(merchantParamConfigService.selectMerchantParamConfigById(id));
    }

    /**
     * 新增商户参数配置（支付和发票）
     */
    @RequiresPermissions("system:config:add")
    @Log(title = "商户参数配置（支付和发票）", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MerchantParamConfig merchantParamConfig)
    {
        return toAjax(merchantParamConfigService.insertMerchantParamConfig(merchantParamConfig));
    }

    /**
     * 修改商户参数配置（支付和发票）
     */
    @RequiresPermissions("system:config:edit")
    @Log(title = "商户参数配置（支付和发票）", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MerchantParamConfig merchantParamConfig)
    {
        return toAjax(merchantParamConfigService.updateMerchantParamConfig(merchantParamConfig));
    }

    /**
     * 删除商户参数配置（支付和发票）
     */
    @RequiresPermissions("system:config:remove")
    @Log(title = "商户参数配置（支付和发票）", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(merchantParamConfigService.deleteMerchantParamConfigByIds(ids));
    }

    @RequiresPermissions("system:config:changeStatus")
    @GetMapping("/changeStatus/{id}")
    public AjaxResult changeStatus(@PathVariable Long id,@RequestParam int status) {
        return toAjax(merchantParamConfigService.changeStatus(id,status));
    }

    /**
     * 根据类型和id获取配置信息
     * @param categoryCode
     * @param eventId
     * @return
     */
    @GetMapping("/getConfig")
    public AjaxResult getConfig(String categoryCode,Long eventId) {
        return success(merchantParamConfigService.getConfig(categoryCode, eventId));
    }

    @GetMapping("merSelect")
    public AjaxResult merSelect(){
        return success(merchantParamConfigService.merSelect());
    }
}
