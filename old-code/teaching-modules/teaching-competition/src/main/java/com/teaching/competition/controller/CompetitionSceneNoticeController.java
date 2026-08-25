package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionSceneNoticeForm;
import com.teaching.competition.domain.CompetitionSceneNoticeQuery;
import com.teaching.competition.domain.CompetitionSceneNoticeVo;
import com.teaching.competition.service.ICompetitionSceneNoticeService;
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
 * 赛事现场公告与个人通知Controller。
 */
@RestController
@RequestMapping({"/sceneNotice", "/competition/sceneNotice"})
public class CompetitionSceneNoticeController extends BaseController {

    @Autowired
    private ICompetitionSceneNoticeService noticeService;

    @RequiresPermissions("competition:sceneNotice:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneNoticeQuery query) {
        startPage();
        List<CompetitionSceneNoticeVo> list = noticeService.selectCompetitionSceneNoticeList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneNotice:query")
    @GetMapping("/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return success(noticeService.selectCompetitionSceneNoticeById(noticeId));
    }

    @RequiresPermissions("competition:sceneNotice:add")
    @Log(title = "赛事现场通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionSceneNoticeForm form) {
        return toAjax(noticeService.insertCompetitionSceneNotice(form));
    }

    @RequiresPermissions("competition:sceneNotice:edit")
    @Log(title = "赛事现场通知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneNoticeForm form) {
        return toAjax(noticeService.updateCompetitionSceneNotice(form));
    }

    @RequiresPermissions("competition:sceneNotice:remove")
    @Log(title = "赛事现场通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteCompetitionSceneNoticeByIds(noticeIds));
    }

    @RequiresPermissions("competition:sceneNotice:publish")
    @Log(title = "赛事现场通知状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody CompetitionSceneNoticeForm form) {
        return toAjax(noticeService.changePublishStatus(form));
    }

    @RequiresPermissions("competition:sceneNotice:publish")
    @Log(title = "赛事现场通知发布", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{noticeId}")
    public AjaxResult publish(@PathVariable("noticeId") Long noticeId) {
        return toAjax(noticeService.publishCompetitionSceneNotice(noticeId));
    }

    @GetMapping("/myList")
    public AjaxResult myList() {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(noticeService.selectMyCompetitionSceneNoticeList(userId));
    }
}

