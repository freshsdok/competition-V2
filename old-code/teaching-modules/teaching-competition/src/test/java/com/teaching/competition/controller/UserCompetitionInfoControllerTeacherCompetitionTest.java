package com.teaching.competition.controller;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.UserCompetitionApplyInfoTeam;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserCompetitionInfoControllerTeacherCompetitionTest {

    @Test
    public void teacherCompetitionAllowsOnePlayerAndNoGuideTeacher() {
        UserCompetitionInfoController.validateTeacherCompetitionRoster(
                List.of(applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER)));
    }

    @Test
    public void teacherCompetitionAllowsTwoGuideTeachersWithoutContactDetails() {
        List<CompetitionApplyInfo> roster = new ArrayList<>();
        roster.add(applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER));
        roster.add(applyInfo(2L, ApplyConstants.TEAM_GUIDE_TEACHER));
        roster.add(applyInfo(3L, ApplyConstants.TEAM_GUIDE_TEACHER));

        UserCompetitionInfoController.validateTeacherCompetitionRoster(roster);
    }

    @Test
    public void teacherCompetitionRejectsMoreThanOnePlayer() {
        List<CompetitionApplyInfo> roster = List.of(
                applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER),
                applyInfo(2L, ApplyConstants.TEAM_LEADER_MEMBER));

        assertGlobalException("教师赛参赛选手人数必须为1名",
                () -> UserCompetitionInfoController.validateTeacherCompetitionRoster(roster));
    }

    @Test
    public void teacherCompetitionRejectsNonLeaderPlayerRole() {
        List<CompetitionApplyInfo> roster = List.of(
                applyInfo(1L, ApplyConstants.TEAM_MEMBER));

        assertGlobalException("教师赛参赛选手角色必须为队长",
                () -> UserCompetitionInfoController.validateTeacherCompetitionRoster(roster));
    }

    @Test
    public void teacherCompetitionRejectsMoreThanTwoGuideTeachers() {
        List<CompetitionApplyInfo> roster = List.of(
                applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER),
                applyInfo(2L, ApplyConstants.TEAM_GUIDE_TEACHER),
                applyInfo(3L, ApplyConstants.TEAM_GUIDE_TEACHER),
                applyInfo(4L, ApplyConstants.TEAM_GUIDE_TEACHER));

        assertGlobalException("教师赛指导教师人数不能多于2名",
                () -> UserCompetitionInfoController.validateTeacherCompetitionRoster(roster));
    }

    @Test
    public void teacherCompetitionRejectsPlayerReplacement() {
        CompetitionApplyInfo deletedPlayer = applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER);
        deletedPlayer.setDelFlag("1");
        CompetitionApplyInfo addedPlayer = applyInfo(null, ApplyConstants.TEAM_LEADER_MEMBER);
        addedPlayer.setDelFlag("0");

        UserCompetitionApplyInfoTeam change = teacherCompetitionChange(
                ApplyConstants.OPERATION_CHANGE, List.of(deletedPlayer, addedPlayer));

        assertGlobalException("教师赛参赛选手不支持新增、删除或替换",
                () -> UserCompetitionInfoController.validateTeacherCompetitionRosterChange(
                        change, List.of(applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER))));
    }

    @Test
    public void teacherCompetitionAllowsDeletingTheLastGuideTeacher() {
        CompetitionApplyInfo deletedGuideTeacher = applyInfo(2L, ApplyConstants.TEAM_GUIDE_TEACHER);
        deletedGuideTeacher.setDelFlag("1");
        UserCompetitionApplyInfoTeam change = teacherCompetitionChange(
                ApplyConstants.OPERATION_CHANGE_TEACHER, List.of(deletedGuideTeacher));

        UserCompetitionInfoController.validateTeacherCompetitionRosterChange(
                change,
                List.of(
                        applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER),
                        applyInfo(2L, ApplyConstants.TEAM_GUIDE_TEACHER)));
    }

    @Test
    public void teacherCompetitionAllowsOrdinaryPlayerInformationEdit() {
        CompetitionApplyInfo editedPlayer = applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER);
        editedPlayer.setPhone("13800000000");
        UserCompetitionApplyInfoTeam change = teacherCompetitionChange(
                ApplyConstants.OPERATION_CHANGE_INFO, List.of(editedPlayer));

        UserCompetitionInfoController.validateTeacherCompetitionRosterChange(
                change, List.of(applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER)));
    }

    @Test
    public void teacherCompetitionRejectsRoleChangeDisguisedAsInformationEdit() {
        CompetitionApplyInfo editedPlayer = applyInfo(1L, "教师");
        UserCompetitionApplyInfoTeam change = teacherCompetitionChange(
                ApplyConstants.OPERATION_CHANGE_INFO, List.of(editedPlayer));

        assertGlobalException("教师赛参赛人员角色不允许修改",
                () -> UserCompetitionInfoController.validateTeacherCompetitionRosterChange(
                        change, List.of(applyInfo(1L, ApplyConstants.TEAM_LEADER_MEMBER))));
    }

    @Test
    public void prepareGuideTeacherFieldsKeepsPhoneAndEmailInCorrectColumns() {
        CompetitionApplyInfo guideTeacher = applyInfo(null, ApplyConstants.TEAM_GUIDE_TEACHER);
        guideTeacher.setUserName("教师甲");
        guideTeacher.setPhone("13800000000");
        guideTeacher.setEmail("teacher@example.com");

        UserCompetitionInfoController.prepareGuideTeacherFields(guideTeacher);

        Assert.assertEquals("教师甲", guideTeacher.getGuideTeacher());
        Assert.assertEquals("13800000000", guideTeacher.getGuideTeacherPhone());
        Assert.assertEquals("teacher@example.com", guideTeacher.getGuideTeacherEmail());
    }

    private static CompetitionApplyInfo applyInfo(Long memberId, String roleName) {
        CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
        applyInfo.setMemberId(memberId);
        applyInfo.setCompetitionRoleName(roleName);
        applyInfo.setCompetitionTrackName(ApplyConstants.COMPETITION_TRACK_NAME_TEACHER);
        return applyInfo;
    }

    private static UserCompetitionApplyInfoTeam teacherCompetitionChange(
            String changeType, List<CompetitionApplyInfo> changes) {
        UserCompetitionApplyInfoTeam applyInfoTeam = new UserCompetitionApplyInfoTeam();
        applyInfoTeam.setCompetitionTrackName(ApplyConstants.COMPETITION_TRACK_NAME_TEACHER);
        applyInfoTeam.setChangeType(changeType);
        applyInfoTeam.setCompetitionApplyInfoList(changes);
        return applyInfoTeam;
    }

    private static void assertGlobalException(String expectedMessage, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (GlobalException exception) {
            Assert.assertEquals(expectedMessage, exception.getMessage());
            return;
        }
        Assert.fail("Expected GlobalException");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
