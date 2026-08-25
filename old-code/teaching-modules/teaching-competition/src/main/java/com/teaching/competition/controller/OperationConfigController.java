package com.teaching.competition.controller;

import java.util.List;

import com.teaching.competition.service.IOperationConfigService;
import com.teaching.system.api.domain.OperationConfig;
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
 * 操作权限配置Controller
 * 
 * @author teaching
 * @date 2026-01-24
 */
@RestController
@RequestMapping("/competitionOperationConfig")
public class OperationConfigController extends BaseController
{
    @Autowired
    private IOperationConfigService operationConfigService;

    /**
     * 查询操作权限配置列表
     */
    @RequiresPermissions("competition:competitionOperationConfig:list")
    @GetMapping("/list")
    public TableDataInfo list(OperationConfig operationConfig)
    {
        startPage();
        List<OperationConfig> list = operationConfigService.selectOperationConfigList(operationConfig);
        return getDataTable(list);
    }

    /**
     * 查询操作权限配置列表
     */
    @RequiresPermissions("competition:competitionOperationConfig:query")
    @GetMapping("/operationConfigList/{competitionSeriesId}")
    public AjaxResult getOperationConfigList(@PathVariable Long competitionSeriesId) {
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setCompetitionSeriesId(competitionSeriesId);
        List<OperationConfig> list = operationConfigService.selectOperationConfigList(operationConfig);
        return success(list);
    }

    /**
     * 导出操作权限配置列表
     */
    @RequiresPermissions("competition:competitionOperationConfig:export")
    @Log(title = "操作权限配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperationConfig operationConfig)
    {
        List<OperationConfig> list = operationConfigService.selectOperationConfigList(operationConfig);
        ExcelUtil<OperationConfig> util = new ExcelUtil<OperationConfig>(OperationConfig.class);
        util.exportExcel(response, list, "操作权限配置数据");
    }

    /**
     * 获取操作权限配置详细信息
     */
    @RequiresPermissions("competition:competitionOperationConfig:query")
    @GetMapping(value = "/getDetailInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(operationConfigService.selectOperationConfigById(id));
    }

    /**
     * 新增操作权限配置
     */
    @RequiresPermissions("competition:competitionOperationConfig:query")
    @Log(title = "操作权限配置", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionOperationConfig")
    public AjaxResult add(@RequestBody OperationConfig operationConfig)
    {
        return toAjax(operationConfigService.insertOperationConfig(operationConfig));
    }

    /**
     * 修改操作权限配置
     */
    @RequiresPermissions("competition:competitionOperationConfig:query")
    @Log(title = "操作权限配置", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionOperationConfig")
    public AjaxResult edit(@RequestBody OperationConfig operationConfig)
    {
        return toAjax(operationConfigService.updateOperationConfig(operationConfig));
    }

    /**
     * 删除操作权限配置
     */
    @RequiresPermissions("competition:competitionOperationConfig:query")
    @Log(title = "操作权限配置", businessType = BusinessType.DELETE)
	@GetMapping("/deleteOperationConfig/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(operationConfigService.deleteOperationConfigByIds(ids));
    }

    /**
     * 查询赛事组别报名费用
     * @param secondLevelCode 组别code（二级分类编码）
     * @return
     */
    @GetMapping("/getCompetitionFee")
    public AjaxResult getCompetitionFee(@RequestParam String secondLevelCode)
    {
        return AjaxResult.success("查询成功",operationConfigService.getCompetitionFee(secondLevelCode));
    }
}
