package com.teaching.competition.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionTitleNotice;
import com.teaching.competition.service.ICompetitionTitleNoticeService;

/**
 * 提示信息Controller
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/competition/competitionTitleNotice")
public class CompetitionTitleNoticeController extends BaseController {
    @Autowired
    private ICompetitionTitleNoticeService competitionTitleNoticeService;

    /**
     * 查询提示信息列表
     */
    @RequiresPermissions("competition:competitionTitleNotice:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionTitleNotice competitionTitleNotice) {
        startPage();
        List<CompetitionTitleNotice> list = competitionTitleNoticeService.selectCompetitionTitleNoticeList(competitionTitleNotice);
        return getDataTable(list);
    }

    /**
     * 导出提示信息列表
     */
    @RequiresPermissions("competition:competitionTitleNotice:export")
    @Log(title = "提示信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionTitleNotice competitionTitleNotice) {
        List<CompetitionTitleNotice> list = competitionTitleNoticeService.selectCompetitionTitleNoticeList(competitionTitleNotice);
        ExcelUtil<CompetitionTitleNotice> util = new ExcelUtil<CompetitionTitleNotice>(CompetitionTitleNotice.class);
        util.exportExcel(response, list, "提示信息数据");
    }

    /**
     * 获取提示信息详细信息
     */
    @RequiresPermissions("competition:competitionTitleNotice:query")
    @GetMapping("/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return success(competitionTitleNoticeService.selectCompetitionTitleNoticeById(noticeId));
    }

    /**
     * 新增提示信息
     */
    @RequiresPermissions("competition:competitionTitleNotice:add")
    @Log(title = "提示信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionTitleNotice competitionTitleNotice) {
        return toAjax(competitionTitleNoticeService.insertCompetitionTitleNotice(competitionTitleNotice));
    }

    /**
     * 修改提示信息
     */
    @RequiresPermissions("competition:competitionTitleNotice:edit")
    @Log(title = "提示信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionTitleNotice competitionTitleNotice) {
        return toAjax(competitionTitleNoticeService.updateCompetitionTitleNotice(competitionTitleNotice));
    }

    /**
     * 删除提示信息
     */
    @RequiresPermissions("competition:competitionTitleNotice:remove")
    @Log(title = "提示信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(competitionTitleNoticeService.deleteCompetitionTitleNoticeByIds(noticeIds));
    }
}
