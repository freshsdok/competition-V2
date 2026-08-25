package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialQuery;
import com.teaching.competition.service.ICompetitionSceneCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PC用户端现场证件Controller。
 */
@RestController
@RequestMapping("/userCompetition")
public class UserCompetitionSceneCredentialController extends BaseController {

    @Autowired
    private ICompetitionSceneCredentialService competitionSceneCredentialService;

    @GetMapping("/sceneCredential/myList")
    public AjaxResult myList() {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(competitionSceneCredentialService.selectMyCompetitionSceneCredentialList(userId));
    }

    @GetMapping("/sceneCredential/{credentialId}")
    public AjaxResult getInfo(@PathVariable("credentialId") Long credentialId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return competitionSceneCredentialService.selectMyCompetitionSceneCredentialList(userId)
                .stream()
                .filter(item -> credentialId.equals(item.getCredentialId()))
                .findFirst()
                .<AjaxResult>map(this::success)
                .orElseGet(() -> error("无权限查看该证件"));
    }

    @GetMapping("/teacher/studentCredentials")
    public AjaxResult teacherStudentCredentials(CompetitionTeacherStudentCredentialQuery query) {
        Long teacherUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(competitionSceneCredentialService.selectTeacherStudentCredentialList(teacherUserId, query));
    }

    @GetMapping("/teacher/studentCredential/{credentialId}")
    public AjaxResult teacherStudentCredentialDetail(@PathVariable("credentialId") Long credentialId) {
        Long teacherUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(competitionSceneCredentialService.selectTeacherStudentCredentialDetail(teacherUserId, credentialId));
    }
}
