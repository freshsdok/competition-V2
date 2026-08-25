package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.dto.ReviewSubmissionActionDTO;
import com.teaching.competition.review.dto.ReviewSubmissionDraftDTO;
import com.teaching.competition.review.dto.ReviewSubmissionMaterialDTO;
import com.teaching.competition.review.service.IReviewSubmissionService;
import com.teaching.competition.review.vo.ReviewSubmissionTaskVO;
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
 * 被评审人填报Controller。
 */
@RestController
@RequestMapping("/review/submission")
public class ReviewSubmissionController extends BaseController {
    @Autowired
    private IReviewSubmissionService reviewSubmissionService;

    @RequiresPermissions("competition:review:submission:list")
    @GetMapping("/my-list")
    public TableDataInfo myList() {
        startPage();
        List<ReviewSubmissionTaskVO> list = reviewSubmissionService.myList();
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:submission:query")
    @GetMapping("/{objectId}")
    public AjaxResult detail(@PathVariable("objectId") Long objectId) {
        return success(reviewSubmissionService.detail(objectId));
    }

    @RequiresPermissions("competition:review:submission:query")
    @GetMapping("/{objectId}/result")
    public AjaxResult publishedResult(@PathVariable("objectId") Long objectId) {
        return success(reviewSubmissionService.publishedResult(objectId));
    }

    @RequiresPermissions("competition:review:submission:edit")
    @Log(title = "评审对象填报草稿", businessType = BusinessType.UPDATE)
    @PutMapping("/{objectId}/draft")
    public AjaxResult saveDraft(@PathVariable("objectId") Long objectId,
                                @RequestBody ReviewSubmissionDraftDTO dto) {
        return success(reviewSubmissionService.saveDraft(objectId, dto));
    }

    @RequiresPermissions("competition:review:submission:edit")
    @Log(title = "评审对象材料", businessType = BusinessType.INSERT)
    @PostMapping("/{objectId}/material")
    public AjaxResult addMaterial(@PathVariable("objectId") Long objectId,
                                  @RequestBody ReviewSubmissionMaterialDTO dto) {
        return success(reviewSubmissionService.addMaterial(objectId, dto));
    }

    @RequiresPermissions("competition:review:submission:query")
    @GetMapping("/{objectId}/materials")
    public AjaxResult materials(@PathVariable("objectId") Long objectId) {
        List<ReviewObjectMaterial> list = reviewSubmissionService.listMaterials(objectId);
        return success(list);
    }

    @RequiresPermissions("competition:review:submission:edit")
    @Log(title = "评审对象材料", businessType = BusinessType.DELETE)
    @DeleteMapping("/material/{materialId}")
    public AjaxResult deleteMaterial(@PathVariable("materialId") Long materialId) {
        return toAjax(reviewSubmissionService.deleteMaterial(materialId));
    }

    @RequiresPermissions("competition:review:submission:submit")
    @Log(title = "提交评审资料", businessType = BusinessType.UPDATE)
    @PostMapping("/{objectId}/submit")
    public AjaxResult submit(@PathVariable("objectId") Long objectId) {
        return success(reviewSubmissionService.submit(objectId));
    }

    @RequiresPermissions("competition:review:submission:withdraw")
    @Log(title = "申请撤回评审资料", businessType = BusinessType.UPDATE)
    @PostMapping("/{objectId}/withdraw-request")
    public AjaxResult withdrawRequest(@PathVariable("objectId") Long objectId,
                                      @RequestBody(required = false) ReviewSubmissionActionDTO dto) {
        return success(reviewSubmissionService.withdrawRequest(objectId, dto));
    }

    @RequiresPermissions("competition:review:submission:approve")
    @Log(title = "审批撤回评审资料", businessType = BusinessType.UPDATE)
    @PostMapping("/{objectId}/withdraw-approve")
    public AjaxResult withdrawApprove(@PathVariable("objectId") Long objectId,
                                      @RequestBody(required = false) ReviewSubmissionActionDTO dto) {
        return success(reviewSubmissionService.withdrawApprove(objectId, dto));
    }

    @RequiresPermissions("competition:review:submission:approve")
    @Log(title = "驳回撤回评审资料", businessType = BusinessType.UPDATE)
    @PostMapping("/{objectId}/withdraw-reject")
    public AjaxResult withdrawReject(@PathVariable("objectId") Long objectId,
                                     @RequestBody(required = false) ReviewSubmissionActionDTO dto) {
        return success(reviewSubmissionService.withdrawReject(objectId, dto));
    }
}
