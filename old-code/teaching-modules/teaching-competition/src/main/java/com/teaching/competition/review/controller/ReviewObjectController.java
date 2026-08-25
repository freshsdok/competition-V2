package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.domain.ReviewObjectExternalRef;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.dto.ReviewObjectImportDTO;
import com.teaching.competition.review.service.IReviewObjectExternalRefService;
import com.teaching.competition.review.service.IReviewObjectMemberService;
import com.teaching.competition.review.service.IReviewObjectService;
import com.teaching.competition.review.service.IReviewSubmissionPermissionService;
import com.teaching.competition.review.vo.ReviewCertificateResolveResultVO;
import com.teaching.competition.review.vo.ReviewObjectImportPreviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评审对象Controller。
 */
@RestController
@RequestMapping("/review/object")
public class ReviewObjectController extends BaseController {
    @Autowired
    private IReviewObjectService reviewObjectService;

    @Autowired
    private IReviewObjectMemberService reviewObjectMemberService;

    @Autowired
    private IReviewSubmissionPermissionService reviewSubmissionPermissionService;

    @Autowired
    private IReviewObjectExternalRefService reviewObjectExternalRefService;

    @RequiresPermissions("competition:review:object:add")
    @Log(title = "评审对象", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewObject entity) {
        reviewObjectService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:object:edit")
    @Log(title = "评审对象", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewObject entity) {
        entity.setId(id);
        return toAjax(reviewObjectService.update(entity));
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewObjectService.selectById(id));
    }

    @RequiresPermissions("competition:review:object:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewObject query) {
        startPage();
        List<ReviewObject> list = reviewObjectService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:object:remove")
    @Log(title = "评审对象", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewObjectService.deleteByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:object:import")
    @PostMapping("/import-preview")
    public AjaxResult importPreview(@RequestBody ReviewObjectImportDTO dto) {
        List<ReviewObjectImportPreviewVO> list = reviewObjectService.importPreview(dto);
        return success(list);
    }

    @RequiresPermissions("competition:review:object:import")
    @Log(title = "外部业务导入评审对象", businessType = BusinessType.INSERT)
    @PostMapping("/import-from-business")
    public AjaxResult importFromBusiness(@RequestBody ReviewObjectImportDTO dto) {
        return success(reviewObjectService.importFromBusiness(dto));
    }

    @RequiresPermissions("competition:review:object:import")
    @Log(title = "同步文件任务材料", businessType = BusinessType.UPDATE)
    @PostMapping("/sync-file-task-materials")
    public AjaxResult syncFileTaskMaterials(@RequestBody ReviewObjectImportDTO dto) {
        return success(reviewObjectService.syncFileTaskMaterials(dto));
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/{id}/members")
    public AjaxResult members(@PathVariable("id") Long id) {
        ReviewObjectMember query = new ReviewObjectMember();
        query.setObjectId(id);
        return success(reviewObjectMemberService.selectList(query));
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/{id}/permissions")
    public AjaxResult permissions(@PathVariable("id") Long id) {
        ReviewSubmissionPermission query = new ReviewSubmissionPermission();
        query.setObjectId(id);
        return success(reviewSubmissionPermissionService.selectList(query));
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/{id}/certificates")
    public AjaxResult certificates(@PathVariable("id") Long id) {
        ReviewObjectCertificateRef query = new ReviewObjectCertificateRef();
        query.setObjectId(id);
        return success(reviewObjectService.selectCertificateRefList(query));
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/{id}/external-refs")
    public AjaxResult externalRefs(@PathVariable("id") Long id) {
        ReviewObjectExternalRef query = new ReviewObjectExternalRef();
        query.setObjectId(id);
        return success(reviewObjectExternalRefService.selectList(query));
    }

    @RequiresPermissions("competition:review:object:add")
    @Log(title = "评审对象参赛证映射", businessType = BusinessType.INSERT)
    @PostMapping("/certificate")
    public AjaxResult addCertificateRef(@RequestBody ReviewObjectCertificateRef ref) {
        reviewObjectService.insertCertificateRef(ref);
        return success(ref);
    }

    @RequiresPermissions("competition:review:object:list")
    @GetMapping("/certificate/list")
    public TableDataInfo certificateList(ReviewObjectCertificateRef query) {
        startPage();
        List<ReviewObjectCertificateRef> list = reviewObjectService.selectCertificateRefList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:object:query")
    @GetMapping("/certificate/resolve")
    public AjaxResult resolveCertificate(@RequestParam("activityId") Long activityId,
                                         @RequestParam("certificateCode") String certificateCode,
                                         @RequestParam(value = "sessionId", required = false) Long sessionId) {
        ReviewCertificateResolveResultVO result = reviewObjectService.resolveCertificate(activityId, certificateCode, sessionId);
        return success(result);
    }
}
