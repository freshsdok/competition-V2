package com.teaching.competition.controller;

import java.util.List;

import com.teaching.competition.service.ICompetitionTrackConfigService;
import com.teaching.system.api.domain.CompetitionTrackConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 赛道配置Controller
 * 
 * @author teaching
 * @date 2025-12-01
 */
@RestController
@RequestMapping("/competitionTrackConfig")
public class CompetitionTrackConfigController extends BaseController {
    @Autowired
    private ICompetitionTrackConfigService competitionTrackConfigService;

    /**
     * 查询赛道配置列表
     */
    @RequiresPermissions("competition:competitionTrackConfig:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionTrackConfig competitionTrackConfig) {
        startPage();
        List<CompetitionTrackConfig> list = competitionTrackConfigService.selectCompetitionTrackConfigList(competitionTrackConfig);
        return getDataTable(list);
    }

    /**
     * 导出赛道配置列表
     */
    @RequiresPermissions("competition:competitionTrackConfig:export")
    @Log(title = "赛道配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionTrackConfig competitionTrackConfig) {
        List<CompetitionTrackConfig> list = competitionTrackConfigService.selectCompetitionTrackConfigList(competitionTrackConfig);
        ExcelUtil<CompetitionTrackConfig> util = new ExcelUtil<CompetitionTrackConfig>(CompetitionTrackConfig.class);
        util.exportExcel(response, list, "赛道配置数据");
    }

    /**
     * 获取赛道配置详细信息
     */
//    @RequiresPermissions("competition:competitionTrackConfig:query")
    @GetMapping(value = "/getCompetitionTrackConfigInfo/{competitionTrackConfigId}")
    public AjaxResult getCompetitionTrackConfigInfo(@PathVariable("competitionTrackConfigId") Long competitionTrackConfigId) {
        return success(competitionTrackConfigService.selectCompetitionTrackConfigByCompetitionTrackConfigId(competitionTrackConfigId));
    }

    /**
     * 新增赛道赛事规则配置
     */
    @RequiresPermissions("competition:competitionTrackConfig:add")
    @Log(title = "新增赛道赛事规则配置", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionTrackRuleConfig")
    public AjaxResult saveCompetitionTrackRuleConfig(@RequestBody CompetitionTrackConfig competitionTrackConfig) {
        return toAjax(competitionTrackConfigService.insertCompetitionTrackConfig(competitionTrackConfig));
    }

    /**
     * 修改赛道配置
     */
    @RequiresPermissions("competition:competitionTrackConfig:edit")
    @Log(title = "赛道配置", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionTrackConfig")
    public AjaxResult edit(@RequestBody CompetitionTrackConfig competitionTrackConfig) {
        return toAjax(competitionTrackConfigService.updateCompetitionTrackConfig(competitionTrackConfig));
    }

    /**
     * 删除赛道配置
     */
    @RequiresPermissions("competition:competitionTrackConfig:remove")
    @Log(title = "赛道配置", businessType = BusinessType.DELETE)
	@GetMapping("/removeCompetitionTrackConfig/{competitionTrackConfigIds}")
    public AjaxResult remove(@PathVariable Long[] competitionTrackConfigIds) {
        return toAjax(competitionTrackConfigService.deleteCompetitionTrackConfigByCompetitionTrackConfigIds(competitionTrackConfigIds));
    }
}
