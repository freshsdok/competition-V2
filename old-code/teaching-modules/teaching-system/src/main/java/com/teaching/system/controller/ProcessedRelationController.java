package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.ProcessedRelation;
import com.teaching.system.domain.ReviewRecord;
import com.teaching.system.service.IProcessedRelationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评审文件处理前后对应关系Controller
 *
 * @author teaching
 * @date 2026-04-23
 */
@RestController
@RequestMapping("/processRelation")
public class ProcessedRelationController extends BaseController {
    @Autowired
    private IProcessedRelationService processedRelationService;

    /**
     * 查询评审文件处理前后对应关系列表
     */
    @RequiresPermissions("system:relation:list")
    @GetMapping("/list")
    public TableDataInfo list(ProcessedRelation processedRelation) {
        startPage();
        List<ProcessedRelation> list = processedRelationService.selectProcessedRelationList(processedRelation);
        return getDataTable(list);
    }

    /**
     * 导出评审文件处理前后对应关系列表
     */
    @RequiresPermissions("system:relation:export")
    @Log(title = "评审文件处理前后对应关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProcessedRelation processedRelation) {
        List<ProcessedRelation> list = processedRelationService.selectProcessedRelationList(processedRelation);
        ExcelUtil<ProcessedRelation> util = new ExcelUtil<ProcessedRelation>(ProcessedRelation.class);
        util.exportExcel(response, list, "评审文件处理前后对应关系数据");
    }

    /**
     * 获取评审文件处理前后对应关系详细信息
     */
    @RequiresPermissions("system:relation:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(processedRelationService.selectProcessedRelationById(id));
    }

    /**
     * 新增评审文件处理前后对应关系
     */
    @RequiresPermissions("system:relation:add")
    @Log(title = "评审文件处理前后对应关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProcessedRelation processedRelation) {
        return toAjax(processedRelationService.insertProcessedRelation(processedRelation));
    }

    /**
     * 修改评审文件处理前后对应关系
     */
/*    @RequiresPermissions("system:relation:edit")
    @Log(title = "评审文件处理前后对应关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProcessedRelation processedRelation) {
        return toAjax(processedRelationService.updateProcessedRelation(processedRelation));
    }*/

    /**
     * 删除评审文件处理前后对应关系
     */
    @RequiresPermissions("system:relation:remove")
    @Log(title = "评审文件处理前后对应关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(processedRelationService.deleteProcessedRelationByIds(ids));
    }

    @GetMapping("/list/byLoginUser")
    public TableDataInfo getListByLoginUser(ProcessedRelation processedRelation) {
        startPage();
        List<ProcessedRelation> list = processedRelationService.selectProcessedRelationList(processedRelation);
        return getDataTable(list);
    }


    /**
     * 修改审阅状态
     *
     * @param reviewRecord file_id
     * @return
     */
    @Log(title = "修改审阅状态", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateReviewStatus(@RequestBody ReviewRecord reviewRecord) {
        reviewRecord.setExpertId(SecurityUtils.getLoginUser().getUserid());
        return toAjax(processedRelationService.updateProcessedRelationReviewStatus(reviewRecord));
    }

    /**
     * 修改最后阅读位置标记
     *
     * @param reviewRecord file_id、last_page
     * @return
     */
//    @Log(title = "修改最后阅读位置标记", businessType = BusinessType.UPDATE)
    @PostMapping("/lastPage")
    public AjaxResult updateLastPage(@RequestBody ReviewRecord reviewRecord) {
        reviewRecord.setExpertId(SecurityUtils.getLoginUser().getUserid());
        return toAjax(processedRelationService.updateLastPageFlagByRelaId(reviewRecord));
    }
}
