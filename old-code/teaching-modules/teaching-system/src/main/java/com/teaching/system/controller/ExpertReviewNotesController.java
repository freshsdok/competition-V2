package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.ExpertReviewNotes;
import com.teaching.system.service.IExpertReviewNotesService;
import com.teaching.system.service.IProcessedRelationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专家审阅备注信息记录Controller
 *
 * @author teaching
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/notes")
public class ExpertReviewNotesController extends BaseController {
    @Autowired
    private IExpertReviewNotesService expertReviewNotesService;
    @Autowired
    private IProcessedRelationService processedRelationService;

    /**
     * 查询专家审阅备注信息记录列表
     */
//    @RequiresPermissions("system:notes:list")
    @GetMapping("/list")
    public TableDataInfo list(ExpertReviewNotes expertReviewNotes) {
        startPage();
        expertReviewNotes.setExpertId(SecurityUtils.getLoginUser().getUserid());
        List<ExpertReviewNotes> list = expertReviewNotesService.selectExpertReviewNotesList(expertReviewNotes);
        return getDataTable(list);
    }

    @GetMapping("/getList")
    public AjaxResult getList(ExpertReviewNotes expertReviewNotes) {
        expertReviewNotes.setExpertId(SecurityUtils.getLoginUser().getUserid());
        return AjaxResult.success(expertReviewNotesService.selectExpertReviewNotesList(expertReviewNotes));
    }

    /**
     * 导出专家审阅备注信息记录列表
     */
    @RequiresPermissions("system:notes:export")
    @Log(title = "专家审阅备注信息记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExpertReviewNotes expertReviewNotes) {
        List<ExpertReviewNotes> list = expertReviewNotesService.selectExpertReviewNotesList(expertReviewNotes);
        ExcelUtil<ExpertReviewNotes> util = new ExcelUtil<ExpertReviewNotes>(ExpertReviewNotes.class);
        util.exportExcel(response, list, "专家审阅备注信息记录数据");
    }

    /**
     * 获取专家审阅备注信息记录详细信息
     */
    @RequiresPermissions("system:notes:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(expertReviewNotesService.selectExpertReviewNotesById(id));
    }

    /**
     * 新增专家审阅备注信息记录
     */
//    @RequiresPermissions("system:notes:add")
    @Log(title = "专家审阅备注信息记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExpertReviewNotes expertReviewNotes) {
        boolean b = processedRelationService.checkCanContinue(expertReviewNotes.getProcessedRelationId());
        if (!b) {
            return error("当前时间不在审阅范围内！");
        }
        expertReviewNotes.setExpertId(SecurityUtils.getLoginUser().getUserid());
        expertReviewNotes.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        return toAjax(expertReviewNotesService.insertExpertReviewNotes(expertReviewNotes));
    }

    /**
     * 修改专家审阅备注信息记录
     */
    @RequiresPermissions("system:notes:edit")
    @Log(title = "专家审阅备注信息记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExpertReviewNotes expertReviewNotes) {
        return toAjax(expertReviewNotesService.updateExpertReviewNotes(expertReviewNotes));
    }

    /**
     * 删除专家审阅备注信息记录
     */
//    @RequiresPermissions("system:notes:remove")
    @Log(title = "专家审阅备注信息记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileId}/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids, @PathVariable Long fileId) {
        boolean b = processedRelationService.checkCanContinue(fileId);
        if (!b) {
            return error("当前时间不在审阅范围内！");
        }
        return toAjax(expertReviewNotesService.deleteExpertReviewNotesByIds(ids));
    }
}
