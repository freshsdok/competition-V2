package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCompetitionDirectIssueReq;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialGenerateReq;
import com.teaching.competition.service.ICompetitionSceneCredentialService;
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
 * 赛事现场证件Controller。
 */
@RestController
@RequestMapping({"/sceneCredential", "/competition/sceneCredential"})
public class CompetitionSceneCredentialController extends BaseController {

    @Autowired
    private ICompetitionSceneCredentialService competitionSceneCredentialService;

    @RequiresPermissions("competition:sceneCredential:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneCredential credential) {
        startPage();
        List<CompetitionSceneCredential> list = competitionSceneCredentialService.selectCompetitionSceneCredentialList(credential);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneCredential:list")
    @GetMapping("/competitionList")
    public TableDataInfo competitionList(CompetitionSceneCredential credential) {
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        startPage();
        List<CompetitionSceneCredential> list = competitionSceneCredentialService.selectCompetitionSceneCredentialList(credential);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneCredential:query")
    @GetMapping("/{credentialId}")
    public AjaxResult getInfo(@PathVariable("credentialId") Long credentialId) {
        return success(competitionSceneCredentialService.selectCompetitionSceneCredentialById(credentialId));
    }

    @RequiresPermissions("competition:sceneCredential:add")
    @Log(title = "赛事现场证件生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody CompetitionSceneCredentialGenerateReq req) {
        return success(competitionSceneCredentialService.generateCompetitionSceneCredential(req));
    }

    @RequiresPermissions("competition:sceneCredential:add")
    @Log(title = "大赛级现场证件直接发证", businessType = BusinessType.INSERT)
    @PostMapping("/competitionDirectIssue")
    public AjaxResult competitionDirectIssue(@RequestBody CompetitionSceneCompetitionDirectIssueReq req) {
        return success(competitionSceneCredentialService.competitionDirectIssue(req));
    }

    @RequiresPermissions("competition:sceneCredential:edit")
    @Log(title = "赛事现场证件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneCredential credential) {
        return toAjax(competitionSceneCredentialService.updateCompetitionSceneCredential(credential));
    }

    @RequiresPermissions("competition:sceneCredential:remove")
    @Log(title = "赛事现场证件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{credentialIds}")
    public AjaxResult remove(@PathVariable Long[] credentialIds) {
        return toAjax(competitionSceneCredentialService.deleteCompetitionSceneCredentialByIds(credentialIds));
    }

    @GetMapping("/myList")
    public AjaxResult myList() {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(competitionSceneCredentialService.selectMyCompetitionSceneCredentialList(userId));
    }
}
