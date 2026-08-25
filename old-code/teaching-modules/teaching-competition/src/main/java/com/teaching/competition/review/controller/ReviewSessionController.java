package com.teaching.competition.review.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.service.IReviewSessionService;
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
 * 现场评审场次Controller。
 */
@RestController
@RequestMapping("/review/session")
public class ReviewSessionController extends BaseController {
    @Autowired
    private IReviewSessionService reviewSessionService;

    @RequiresPermissions("competition:review:session:add")
    @Log(title = "现场评审场次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewSession entity) {
        reviewSessionService.insert(entity);
        return success(entity);
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "现场评审场次", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable("id") Long id, @RequestBody ReviewSession entity) {
        entity.setId(id);
        return toAjax(reviewSessionService.update(entity));
    }

    @RequiresPermissions("competition:review:session:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(reviewSessionService.selectById(id));
    }

    @RequiresPermissions("competition:review:session:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewSession query) {
        startPage();
        List<ReviewSession> list = reviewSessionService.selectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:session:remove")
    @Log(title = "现场评审场次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(reviewSessionService.deleteByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "设置现场当前评审对象", businessType = BusinessType.UPDATE)
    @PostMapping("/{sessionId}/current-object")
    public AjaxResult setCurrentObject(@PathVariable("sessionId") Long sessionId,
                                       @RequestBody ReviewSessionCurrentObjectDTO dto) {
        return success(reviewSessionService.setCurrentObject(sessionId, dto));
    }

    @RequiresPermissions("competition:review:session:query")
    @GetMapping("/{sessionId}/current-object")
    public AjaxResult getCurrentObject(@PathVariable("sessionId") Long sessionId) {
        return success(reviewSessionService.getCurrentObject(sessionId));
    }

    @RequiresPermissions("competition:review:session:add")
    @Log(title = "现场评审对象顺序", businessType = BusinessType.INSERT)
    @PostMapping("/object")
    public AjaxResult addSessionObject(@RequestBody ReviewSessionObject sessionObject) {
        reviewSessionService.insertSessionObject(sessionObject);
        return success(sessionObject);
    }

    @RequiresPermissions("competition:review:session:edit")
    @Log(title = "现场评审对象顺序", businessType = BusinessType.UPDATE)
    @PutMapping("/object/{id}")
    public AjaxResult updateSessionObject(@PathVariable("id") Long id, @RequestBody ReviewSessionObject sessionObject) {
        sessionObject.setId(id);
        return toAjax(reviewSessionService.updateSessionObject(sessionObject));
    }

    @RequiresPermissions("competition:review:session:remove")
    @Log(title = "现场评审对象顺序", businessType = BusinessType.DELETE)
    @DeleteMapping("/object/{id}")
    public AjaxResult removeSessionObject(@PathVariable("id") Long id) {
        return toAjax(reviewSessionService.deleteSessionObjectByIds(new Long[]{id}));
    }

    @RequiresPermissions("competition:review:session:list")
    @GetMapping("/object/list")
    public TableDataInfo sessionObjectList(ReviewSessionObject query) {
        startPage();
        List<ReviewSessionObject> list = reviewSessionService.selectSessionObjectList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:review:session:list")
    @GetMapping("/event-log/list")
    public TableDataInfo eventLogList(ReviewSessionEventLog query) {
        startPage();
        List<ReviewSessionEventLog> list = reviewSessionService.selectEventLogList(query);
        return getDataTable(list);
    }
}
