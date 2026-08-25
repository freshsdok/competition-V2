package com.teaching.competition.controller;

import java.util.Arrays;
import java.util.List;

import com.teaching.competition.service.ICompetitionAwardsConfigService;
import com.teaching.system.api.domain.CompetitionAwardsConfig;
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

/**
 * 赛事奖项设置Controller
 * 
 * @author teaching
 * @date 2025-11-22
 */
@RestController
@RequestMapping("/competitionAwardsConfig")
public class CompetitionAwardsConfigController extends BaseController
{
    @Autowired
    private ICompetitionAwardsConfigService competitionAwardsConfigService;

    /**
     * 查询赛事奖项设置列表
     */
    @RequiresPermissions("competition:competitionAwardsConfig:list")
    @GetMapping("/list")
    public AjaxResult list(CompetitionAwardsConfig competitionAwardsConfig) {
        List<CompetitionAwardsConfig> list = competitionAwardsConfigService.selectCompetitionAwardsConfigList(competitionAwardsConfig);
        return success(list);
    }

    /**
     * 导出赛事奖项设置列表
     */
    @RequiresPermissions("competition:competitionAwardsConfig:export")
    @Log(title = "赛事奖项设置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionAwardsConfig competitionAwardsConfig)
    {
        List<CompetitionAwardsConfig> list = competitionAwardsConfigService.selectCompetitionAwardsConfigList(competitionAwardsConfig);
        ExcelUtil<CompetitionAwardsConfig> util = new ExcelUtil<CompetitionAwardsConfig>(CompetitionAwardsConfig.class);
        util.exportExcel(response, list, "赛事奖项设置数据");
    }

    /**
     * 获取赛事奖项设置详细信息
     */
    @RequiresPermissions("competition:competitionAwardsConfig:query")
    @GetMapping(value = "/{awardsId}")
    public AjaxResult getInfo(@PathVariable("awardsId") Long awardsId)
    {
        return success(competitionAwardsConfigService.selectCompetitionAwardsConfigByAwardsId(awardsId));
    }

    /**
     * 新增赛事奖项设置
     */
    @RequiresPermissions("competition:competitionAwardsConfig:add")
    @Log(title = "赛事奖项设置", businessType = BusinessType.INSERT)
    @PostMapping("/addCompetitionAwardsConfig")
    public AjaxResult addCompetitionAwardsConfig(@RequestBody CompetitionAwardsConfig competitionAwardsConfig) {
        return toAjax(competitionAwardsConfigService.insertCompetitionAwardsConfig(Arrays.asList(competitionAwardsConfig)));
    }

    /**
     * 修改赛事奖项设置
     */
    @RequiresPermissions("competition:competitionAwardsConfig:edit")
    @Log(title = "赛事奖项设置", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionAwardsConfig")
    public AjaxResult edit(@RequestBody CompetitionAwardsConfig competitionAwardsConfig)
    {
        return toAjax(competitionAwardsConfigService.updateCompetitionAwardsConfig(competitionAwardsConfig));
    }

    /**
     * 删除赛事奖项设置
     */
    @RequiresPermissions("competition:competitionAwardsConfig:remove")
    @Log(title = "赛事奖项设置", businessType = BusinessType.DELETE)
	@GetMapping("/removeCompetitionAwardsConfig/{awardsIds}")
    public AjaxResult remove(@PathVariable Long[] awardsIds)
    {
        return toAjax(competitionAwardsConfigService.deleteCompetitionAwardsConfigByAwardsIds(awardsIds));
    }
}
