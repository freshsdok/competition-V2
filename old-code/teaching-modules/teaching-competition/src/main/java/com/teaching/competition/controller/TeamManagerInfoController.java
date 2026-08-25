package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.system.api.domain.TeamManagerInfo;
import com.teaching.competition.service.ITeamManagerInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 团队管理Controller
 * 
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/teamManager")
public class TeamManagerInfoController extends BaseController
{
    @Autowired
    private ITeamManagerInfoService teamManagerInfoService;

    /**
     * 查询团队管理列表
     */
    @RequiresPermissions("competition:teamManager:list")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody TeamManagerInfo teamManagerInfo)
    {
        startPage();
        List<TeamManagerInfo> list = teamManagerInfoService.selectTeamManagerInfoList(teamManagerInfo);
        return getDataTable(list);
    }

    @InnerAuth
    @PostMapping("/getInnerTeamManagerInfoList")
    public AjaxResult selectInnerTeamManagerInfoList(@RequestBody TeamManagerInfo teamManagerInfo) {
        return success(teamManagerInfoService.selectInnerTeamManagerInfoList(teamManagerInfo));
    }

    /**
     * 导出团队管理列表
     */
    @RequiresPermissions("competition:teamManager:export")
    @Log(title = "团队管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TeamManagerInfo teamManagerInfo)
    {
        List<TeamManagerInfo> list = teamManagerInfoService.selectTeamManagerInfoList(teamManagerInfo);
        ExcelUtil<TeamManagerInfo> util = new ExcelUtil<TeamManagerInfo>(TeamManagerInfo.class);
        util.exportExcel(response, list, "团队管理数据");
    }

    /**
     * 获取团队管理详细信息
     */
    @RequiresPermissions("competition:teamManager:query")
    @GetMapping(value = "/getDetailInfo/{teamCode}")
    public AjaxResult getTeamDetailInfo(@PathVariable("teamCode") String teamCode)
    {
        return success(teamManagerInfoService.selectTeamManagerInfoByTeamCode(null,teamCode));
    }
    /**
     * 修改团队管理
     */
    @RequiresPermissions("competition:teamManager:edit")
    @Log(title = "团队管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateTeamManagerInfo")
    public AjaxResult updateTeamManagerInfo(@RequestBody TeamManagerInfo teamManagerInfo)
    {
        return toAjax(teamManagerInfoService.updateTeamManagerInfo(teamManagerInfo));
    }

    /**
     * 删除团队管理
     */
    @RequiresPermissions("competition:teamManager:remove")
    @Log(title = "团队管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/removeTeamManager/{teamCodes}")
    public AjaxResult remove(@PathVariable String[] teamCodes)
    {
        return toAjax(teamManagerInfoService.deleteTeamManagerInfoByTeamCodes(teamCodes));
    }

    @InnerAuth
    @GetMapping(value = "/getInnerTeamDetailInfo/{teamId}")
    public AjaxResult getInnerTeamDetailInfo(@PathVariable("teamId") Long teamId)
    {
        return success(teamManagerInfoService.selectTeamManagerInfoByTeamCode(teamId,null));
    }

    @InnerAuth
    @PostMapping("/updateTeamManagerStatus")
    public AjaxResult updateTeamManagerStatus(@RequestBody TeamManagerInfo teamManagerInfo)
    {
        return success(teamManagerInfoService.updateTeamManagerStatus(teamManagerInfo));
    }
}
