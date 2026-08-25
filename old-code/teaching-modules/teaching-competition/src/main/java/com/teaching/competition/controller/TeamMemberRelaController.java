package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.competition.service.ITeamMemberRelaService;
import com.teaching.system.api.domain.TeamMemberRela;
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
 * 团队关联关系Controller
 * 
 * @author teaching
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/teamMember")
public class TeamMemberRelaController extends BaseController
{
    @Autowired
    private ITeamMemberRelaService teamMemberRelaService;

    /**
     * 查询团队关联关系列表
     */
    @GetMapping("/list")
    public TableDataInfo list(TeamMemberRela teamMemberRela)
    {
        startPage();
        List<TeamMemberRela> list = teamMemberRelaService.selectTeamMemberRelaList(teamMemberRela);
        return getDataTable(list);
    }

    /**
     * 导出团队关联关系列表
     */
    @Log(title = "团队关联关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TeamMemberRela teamMemberRela)
    {
        List<TeamMemberRela> list = teamMemberRelaService.selectTeamMemberRelaList(teamMemberRela);
        ExcelUtil<TeamMemberRela> util = new ExcelUtil<TeamMemberRela>(TeamMemberRela.class);
        util.exportExcel(response, list, "团队关联关系数据");
    }

    /**
     * 获取团队关联关系详细信息
     */
    @GetMapping(value = "/{relaId}")
    public AjaxResult getInfo(@PathVariable("relaId") Long userId,String teamCode)
    {
        return success(teamMemberRelaService.selectTeamMemberRelaByRelaId(userId,teamCode ));
    }

    /**
     * 新增团队关联关系
     */
    @Log(title = "团队关联关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TeamMemberRela teamMemberRela)
    {
        return toAjax(teamMemberRelaService.insertTeamMemberRela(teamMemberRela));
    }

    /**
     * 修改团队关联关系
     */
//    @RequiresPermissions("system:rela:edit")
    @Log(title = "团队关联关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TeamMemberRela teamMemberRela)
    {
        return toAjax(teamMemberRelaService.updateTeamMemberRela(teamMemberRela));
    }

    /**
     * 删除团队关联关系
     */
    @RequiresPermissions("system:rela:remove")
    @Log(title = "团队关联关系", businessType = BusinessType.DELETE)
	@DeleteMapping("/{relaIds}")
    public AjaxResult remove(@PathVariable String[] teamCodes)
    {
        return toAjax(teamMemberRelaService.deleteTeamMemberRelaByRelaIds(teamCodes));
    }
}
