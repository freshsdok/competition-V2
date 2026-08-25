package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.dto.ReviewObjectImportDTO;
import com.teaching.competition.review.service.IReviewActivityService;
import com.teaching.competition.review.service.IReviewObjectService;
import com.teaching.competition.review.service.IReviewSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评审活动Controller。
 */
@RestController
@RequestMapping("/review/activity")
public class ReviewActivityController extends BaseController {
    private static final String SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER = "FILE_UPLOAD_MANAGER";

    @Autowired
    private IReviewActivityService reviewActivityService;

    @Autowired
    private IReviewSubmissionService reviewSubmissionService;

    @Autowired
    private IReviewObjectService reviewObjectService;

    @RequiresPermissions("competition:review:activity:add")
    @Log(title = "评审活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewActivity entity) {
        reviewActivityService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:activity:edit")
    @Log(title = "评审活动", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewActivity entity) {
        entity.setId(id);
        return toAjax(reviewActivityService.update(entity));
    }

    @RequiresPermissions("competition:review:activity:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewActivityService.selectById(id));
    }

    @RequiresPermissions("competition:review:activity:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewActivity query) {
        startPage();
        List<ReviewActivity> list = reviewActivityService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:activity:remove")
    @Log(title = "评审活动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewActivityService.deleteByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:object:import")
    @PostMapping("/{activityId}/file-task/{fileTaskId}/import-preview")
    public AjaxResult importFileTaskPreview(@PathVariable("activityId") Long activityId,
                                            @PathVariable("fileTaskId") Long fileTaskId,
                                            @RequestBody(required = false) ReviewObjectImportDTO dto) {
        return success(reviewObjectService.importPreview(buildFileTaskImportDTO(activityId, fileTaskId, dto)));
    }

    @RequiresPermissions("competition:review:object:import")
    @Log(title = "文件任务导入评审对象", businessType = BusinessType.INSERT)
    @PostMapping("/{activityId}/file-task/{fileTaskId}/import")
    public AjaxResult importFileTask(@PathVariable("activityId") Long activityId,
                                     @PathVariable("fileTaskId") Long fileTaskId,
                                     @RequestBody(required = false) ReviewObjectImportDTO dto) {
        return success(reviewObjectService.importFromBusiness(buildFileTaskImportDTO(activityId, fileTaskId, dto)));
    }

    @RequiresPermissions("competition:review:submission:close")
    @Log(title = "关闭评审活动填报", businessType = BusinessType.UPDATE)
    @PostMapping("/{activityId}/close-submission")
    public AjaxResult closeSubmission(@PathVariable("activityId") Long activityId) {
        return success(reviewSubmissionService.closeSubmission(activityId));
    }

    private ReviewObjectImportDTO buildFileTaskImportDTO(Long activityId, Long fileTaskId, ReviewObjectImportDTO dto) {
        ReviewObjectImportDTO importDTO = dto == null ? new ReviewObjectImportDTO() : dto;
        importDTO.setActivityId(activityId);
        importDTO.setFileTaskId(fileTaskId);
        importDTO.setSourceBizIds(null);
        importDTO.setSourceBizType(SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER);
        if (importDTO.getSubmittedOnly() == null) {
            importDTO.setSubmittedOnly(Boolean.TRUE);
        }
        if (importDTO.getSyncCertificate() == null) {
            importDTO.setSyncCertificate(Boolean.FALSE);
        }
        if (importDTO.getSyncMaterial() == null) {
            importDTO.setSyncMaterial(Boolean.FALSE);
        }
        return importDTO;
    }
}
