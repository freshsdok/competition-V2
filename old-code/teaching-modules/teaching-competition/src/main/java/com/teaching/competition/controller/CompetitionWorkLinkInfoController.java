package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.redis.service.RedisService;
import com.teaching.competition.domain.CompetitionWorkLinkInfo;
import com.teaching.competition.service.ICompetitionWorkLinkInfoService;
import com.teaching.competition.util.ExtractionCodeUtil;
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
 * 作品打分链接信息Controller
 * 
 * @author teaching
 * @date 2025-11-19
 */
@RestController
@RequestMapping("/competitionWorkLink")
public class CompetitionWorkLinkInfoController extends BaseController
{
    @Autowired
    private ICompetitionWorkLinkInfoService competitionWorkLinkInfoService;

    /**
     * 查询作品打分链接信息列表
     */
    @RequiresPermissions("competition:competitionWorkLink:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionWorkLinkInfo competitionWorkLinkInfo)
    {
        startPage();
        List<CompetitionWorkLinkInfo> list = competitionWorkLinkInfoService.selectCompetitionWorkLinkInfoList(competitionWorkLinkInfo);
        return getDataTable(list);
    }

    /**
     * 导出作品打分链接信息列表
     */
    @RequiresPermissions("competition:competitionWorkLink:export")
    @Log(title = "作品打分链接信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionWorkLinkInfo competitionWorkLinkInfo)
    {
        List<CompetitionWorkLinkInfo> list = competitionWorkLinkInfoService.selectCompetitionWorkLinkInfoList(competitionWorkLinkInfo);
        ExcelUtil<CompetitionWorkLinkInfo> util = new ExcelUtil<CompetitionWorkLinkInfo>(CompetitionWorkLinkInfo.class);
        util.exportExcel(response, list, "作品打分链接信息数据");
    }

    /**
     * 获取作品打分链接信息详细信息
     */
    @RequiresPermissions("competition:competitionWorkLink:query")
    @GetMapping(value = "/getDetailInfo/{linkId}")
    public AjaxResult getInfo(@PathVariable("linkId") Long linkId)
    {
        return success(competitionWorkLinkInfoService.selectCompetitionWorkLinkInfoByLinkId(linkId));
    }

    /**
     * 新增作品打分链接信息
     */
    @RequiresPermissions("competition:competitionWorkLink:add")
    @Log(title = "作品打分链接信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionWorkLinkInfo")
    public AjaxResult add(@RequestBody CompetitionWorkLinkInfo competitionWorkLinkInfo)
    {
        return success(competitionWorkLinkInfoService.insertCompetitionWorkLinkInfo(competitionWorkLinkInfo));
    }

    /**
     * 修改作品打分链接信息
     */
    @RequiresPermissions("competition:competitionWorkLink:edit")
    @Log(title = "作品打分链接信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionWorkLinkInfo")
    public AjaxResult edit(@RequestBody CompetitionWorkLinkInfo competitionWorkLinkInfo)
    {
        return toAjax(competitionWorkLinkInfoService.updateCompetitionWorkLinkInfo(competitionWorkLinkInfo));
    }

    /**
     * 删除作品打分链接信息
     */
    @RequiresPermissions("competition:competitionWorkLink:remove")
    @Log(title = "作品打分链接信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{linkIds}")
    public AjaxResult remove(@PathVariable Long[] linkIds)
    {
        return toAjax(competitionWorkLinkInfoService.deleteCompetitionWorkLinkInfoByLinkIds(linkIds));
    }

}
