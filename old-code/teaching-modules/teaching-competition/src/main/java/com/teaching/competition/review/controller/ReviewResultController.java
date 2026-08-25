package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.dto.ReviewResultConclusionDTO;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewResultPublishDTO;
import com.teaching.competition.review.dto.ReviewResultQueryDTO;
import com.teaching.competition.review.dto.ReviewResultRevokeDTO;
import com.teaching.competition.review.service.IReviewResultService;
import com.teaching.competition.review.vo.ReviewResultListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评审结果Controller。
 */
@RestController
@RequestMapping("/review/result")
public class ReviewResultController extends BaseController {
    @Autowired
    private IReviewResultService reviewResultService;

    @RequiresPermissions("competition:review:result:generate")
    @Log(title = "生成评审结果", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody ReviewResultGenerateDTO dto) {
        return success(reviewResultService.generate(dto));
    }

    @RequiresPermissions("competition:review:result:edit")
    @Log(title = "填写评审结果结论", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/conclusion")
    public AjaxResult conclusion(@PathVariable("id") Long id, @RequestBody ReviewResultConclusionDTO dto) {
        return success(reviewResultService.updateConclusion(id, dto));
    }

    @RequiresPermissions("competition:review:result:publish")
    @Log(title = "发布评审结果", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/publish")
    public AjaxResult publish(@PathVariable("id") Long id, @RequestBody ReviewResultPublishDTO dto) {
        return success(reviewResultService.publish(id, dto));
    }

    @RequiresPermissions("competition:review:result:revoke")
    @Log(title = "撤回评审结果发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/revoke")
    public AjaxResult revoke(@PathVariable("id") Long id, @RequestBody(required = false) ReviewResultRevokeDTO dto) {
        return success(reviewResultService.revoke(id, dto));
    }

    @RequiresPermissions("competition:review:result:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewResultService.selectById(id));
    }

    @RequiresPermissions("competition:review:result:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewResultQueryDTO query) {
        startPage();
        List<ReviewResultListVO> list = reviewResultService.selectResultList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:result:record")
    @GetMapping("/records")
    public AjaxResult records(Long activityId, Long roundId, Long objectId) {
        return success(reviewResultService.selectRecordList(activityId, roundId, objectId));
    }

    @RequiresPermissions("competition:review:result:record")
    @GetMapping("/{objectId}/records")
    public AjaxResult objectRecords(@PathVariable("objectId") Long objectId, Long activityId, Long roundId) {
        return success(reviewResultService.selectRecordList(activityId, roundId, objectId));
    }

    @RequiresPermissions("competition:review:result:record")
    @GetMapping("/record/{recordId}/details")
    public AjaxResult scoreDetails(@PathVariable("recordId") Long recordId) {
        return success(reviewResultService.selectScoreDetails(recordId));
    }
}
