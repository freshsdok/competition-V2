package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionTrackConfigMapper;
import com.teaching.competition.mapper.TeamManagerInfoMapper;
import com.teaching.competition.mapper.TeamMemberRelaMapper;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.CompetitionConfig;
import com.teaching.system.api.domain.CompetitionTrackConfig;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class UserCompetitionServiceImplTeacherCompetitionTest {

    private static final long UPLOADER_USER_ID = 900L;
    private static final long SERIES_ID = 77L;
    private static final String TRACK_ID = "teacher-track";
    private static final String USER_NAME = "张老师";
    private static final String ID_CARD = "11010119900101123X";

    private UserCompetitionServiceImpl service;
    private RemoteUserService userService;
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;
    private List<CompetitionTrackConfig> competitionTrackConfigResult;
    private CompetitionApplyInfo duplicateRegistrationResult;
    private Object[] duplicateRegistrationArguments;
    private List<CompetitionApplyInfo> unboundContestants;
    private int applicationBindCount;
    private int memberBindCount;
    private int managerBindCount;

    @Before
    public void setUp() throws Exception {
        service = new UserCompetitionServiceImpl();
        competitionTrackConfigResult = Collections.emptyList();
        duplicateRegistrationResult = null;
        duplicateRegistrationArguments = null;
        unboundContestants = Collections.emptyList();
        applicationBindCount = 0;
        memberBindCount = 0;
        managerBindCount = 0;

        userService = proxy(RemoteUserService.class, (proxy, method, args) -> {
            if ("getNationwideCollegeInfoInfoByName".equals(method.getName())) {
                return R.fail("school dictionary not configured");
            }
            return handleObjectMethod(proxy, method, args, "RemoteUserServiceTestStub");
        });
        competitionTrackConfigMapper = proxy(CompetitionTrackConfigMapper.class,
                (proxy, method, args) -> {
                    if ("selectCompetitionTrackConfigByName".equals(method.getName())) {
                        return competitionTrackConfigResult;
                    }
                    return handleObjectMethod(proxy, method, args,
                            "CompetitionTrackConfigMapperTestStub");
                });
        competitionApplyInfoMapper = proxy(CompetitionApplyInfoMapper.class,
                (proxy, method, args) -> {
                    if ("selectTeacherContestantActiveRegistration".equals(method.getName())) {
                        duplicateRegistrationArguments = args;
                        return duplicateRegistrationResult;
                    }
                    if ("selectUnboundTeacherContestantsByPhone".equals(method.getName())) {
                        return unboundContestants;
                    }
                    if ("bindTeacherContestantUser".equals(method.getName())) {
                        applicationBindCount++;
                        return 1;
                    }
                    return handleObjectMethod(proxy, method, args,
                            "CompetitionApplyInfoMapperTestStub");
                });
        setField("userService", userService);
        setField("competitionTrackConfigMapper", competitionTrackConfigMapper);
        setField("competitionApplyInfoMapper", competitionApplyInfoMapper);
        setField("teamMemberRelaMapper", proxy(TeamMemberRelaMapper.class, (proxy, method, args) -> {
            if ("bindTeacherContestantUser".equals(method.getName())) {
                memberBindCount++;
                return 1;
            }
            return handleObjectMethod(proxy, method, args, "TeamMemberRelaMapperTestStub");
        }));
        setField("teamManagerInfoMapper", proxy(TeamManagerInfoMapper.class, (proxy, method, args) -> {
            if ("bindTeacherContestantUser".equals(method.getName())) {
                managerBindCount++;
                return 1;
            }
            return handleObjectMethod(proxy, method, args, "TeamManagerInfoMapperTestStub");
        }));
    }

    @Test
    public void contestantNeedsNoAccountOrIdentityAndAllowsZeroGuideTeachers() throws Throwable {
        stubValidConfiguration(validConfiguration());

        CompetitionApplyInfo contestant = contestant("TEAM-1");
        invokeTeacherValidation(List.of(contestant));

        assertEquals(null, contestant.getUserId());
        assertEquals(null, contestant.getOrgId());
        assertEquals("Excel学校", contestant.getSchoolName());
        assertEquals("Excel单位", contestant.getCompanyName());
        assertEquals(Constants.JOIN_TYPE_TEAM, contestant.getJoinType());
        assertEquals(Constants.CHECK_PASS, contestant.getCheckStatus());
        assertEquals(null, contestant.getRealNameAuthStatus());
        assertEquals(Long.valueOf(UPLOADER_USER_ID), contestant.getLeaderTeacherId());
        assertNotNull(contestant.getRegistrationTime());
    }

    @Test
    public void duplicateContestantIdCardInWorkbookIsRejected() throws Throwable {
        stubValidConfiguration(validConfiguration());

        assertGlobalExceptionContains("证件号“" + ID_CARD + "”重复",
                () -> invokeTeacherValidation(List.of(
                        contestant("TEAM-1"),
                        contestant("TEAM-2")
                )));
    }

    @Test
    public void existingActiveRegistrationIsRejected() throws Throwable {
        stubValidConfiguration(validConfiguration());
        duplicateRegistrationResult = new CompetitionApplyInfo();

        assertGlobalExceptionContains("已报名本赛道",
                () -> invokeTeacherValidation(List.of(contestant("TEAM-1"))));
    }

    @Test
    public void duplicateRegistrationLookupExcludesCancelledApplications() throws Throwable {
        stubValidConfiguration(validConfiguration());

        invokeTeacherValidation(List.of(contestant("TEAM-1")));

        assertNotNull(duplicateRegistrationArguments);
        assertEquals(ID_CARD, duplicateRegistrationArguments[0]);
        assertEquals(Long.valueOf(SERIES_ID), duplicateRegistrationArguments[1]);
        assertEquals(TRACK_ID, duplicateRegistrationArguments[2]);
        assertEquals(DictConstant.CANCELLED, duplicateRegistrationArguments[3]);
    }

    @Test
    public void registeredPhoneBindsAllRowsForOneContestantIdentity() {
        CompetitionApplyInfo first = contestant("TEAM-1");
        first.setMemberId(1L);
        CompetitionApplyInfo second = contestant("TEAM-2");
        second.setMemberId(2L);
        unboundContestants = List.of(first, second);

        assertEquals(2, service.bindTeacherCompetitionUser(100L, "13800000000"));
        assertEquals(2, applicationBindCount);
        assertEquals(2, memberBindCount);
        assertEquals(2, managerBindCount);
    }

    @Test
    public void ambiguousPhoneDoesNotBindDifferentIdentities() {
        CompetitionApplyInfo first = contestant("TEAM-1");
        first.setMemberId(1L);
        CompetitionApplyInfo second = contestant("TEAM-2");
        second.setMemberId(2L);
        second.setIdCard("110101198001011234");
        unboundContestants = List.of(first, second);

        assertEquals(0, service.bindTeacherCompetitionUser(100L, "13800000000"));
        assertEquals(0, applicationBindCount);
    }

    @Test
    public void nonTeamConfigurationIsRejected() throws Throwable {
        CompetitionConfig config = validConfiguration();
        config.setJoinType(Constants.JOIN_TYPE_PERSON);
        stubValidConfiguration(config);

        assertGlobalExceptionContains("参赛方式必须配置为团队参赛",
                () -> invokeTeacherValidation(List.of(contestant("TEAM-1"))));
    }

    @Test
    public void contradictoryRosterConfigurationIsRejected() throws Throwable {
        CompetitionConfig config = validConfiguration();
        config.setIsTeacherNess(Constants.IS_YES);
        stubValidConfiguration(config);

        assertGlobalExceptionContains("指导教师必须配置为非必填、0至2人",
                () -> invokeTeacherValidation(List.of(contestant("TEAM-1"))));
    }

    @Test
    public void closedRegistrationConfigurationIsRejected() throws Throwable {
        CompetitionConfig config = validConfiguration();
        config.setApplyStartTime(new Date(System.currentTimeMillis() - 20_000L));
        config.setApplyEndTime(new Date(System.currentTimeMillis() - 10_000L));
        stubValidConfiguration(config);

        assertGlobalExceptionContains("报名已结束",
                () -> invokeTeacherValidation(List.of(contestant("TEAM-1"))));
    }

    @Test
    public void teacherContestantIdentityFieldsCannotBeChangedAndAreRestoredWhenOmitted() throws Throwable {
        CompetitionApplyInfo existing = contestant("TEAM-1");
        existing.setUserId(100L);
        existing.setOrgId(301L);
        existing.setSchool("school-1");
        existing.setSchoolName("认证学校");
        existing.setRealNameAuthStatus(Constants.AUTH_STATUS_PASS);

        CompetitionApplyInfo renamed = new CompetitionApplyInfo();
        renamed.setUserName("其他姓名");
        assertGlobalExceptionContains("姓名不能变更", () -> invokeFieldProtection(renamed, existing));

        CompetitionApplyInfo changedSchool = new CompetitionApplyInfo();
        changedSchool.setSchool("school-2");
        assertGlobalExceptionContains("学校或单位不能变更",
                () -> invokeFieldProtection(changedSchool, existing));

        CompetitionApplyInfo contactOnlyEdit = new CompetitionApplyInfo();
        contactOnlyEdit.setPhone("13900000000");
        contactOnlyEdit.setEmail("new@example.com");
        invokeFieldProtection(contactOnlyEdit, existing);
        assertEquals(existing.getUserId(), contactOnlyEdit.getUserId());
        assertEquals(existing.getUserName(), contactOnlyEdit.getUserName());
        assertEquals(existing.getIdCard(), contactOnlyEdit.getIdCard());
        assertEquals(existing.getOrgId(), contactOnlyEdit.getOrgId());
        assertEquals(existing.getSchool(), contactOnlyEdit.getSchool());
        assertEquals(existing.getSchoolName(), contactOnlyEdit.getSchoolName());
    }

    @Test
    public void firstGuideTeacherUsesContestantTemplateAndKeepsGuideRole() {
        CompetitionApplyInfo existingContestant = contestant("TEAM-1");
        CompetitionApplyInfo template = UserCompetitionServiceImpl.selectChangeTemplateApplyInfo(
                List.of(existingContestant),
                ApplyConstants.TEAM_GUIDE_TEACHER
        );
        assertSame(existingContestant, template);

        CompetitionApplyInfo newGuideTeacher = new CompetitionApplyInfo();
        newGuideTeacher.setUserName("指导教师甲");
        newGuideTeacher.setPhone("13800000000");
        newGuideTeacher.setEmail("guide@example.com");
        UserCompetitionServiceImpl.restoreChangeRoleFields(
                newGuideTeacher,
                ApplyConstants.TEAM_GUIDE_TEACHER
        );
        assertEquals(ApplyConstants.TEAM_GUIDE_TEACHER, newGuideTeacher.getCompetitionRoleName());
        assertEquals("指导教师甲", newGuideTeacher.getGuideTeacher());
        assertEquals("13800000000", newGuideTeacher.getGuideTeacherPhone());
        assertEquals("guide@example.com", newGuideTeacher.getGuideTeacherEmail());
    }

    private CompetitionConfig validConfiguration() {
        CompetitionConfig config = new CompetitionConfig();
        config.setJoinType(Constants.JOIN_TYPE_TEAM);
        config.setMinPernNum("1");
        config.setMaxPernNum("1");
        config.setIsTeacherNess(Constants.IS_NO);
        config.setMinTeacherNum("0");
        config.setMaxTeacherNum("2");
        config.setIsStudent(Constants.IS_NO);
        config.setIsRealNameAuth(Constants.IS_NO);
        config.setApplyStartTime(new Date(System.currentTimeMillis() - 60_000L));
        config.setApplyEndTime(new Date(System.currentTimeMillis() + 60_000L));
        return config;
    }

    private void stubValidConfiguration(CompetitionConfig config) {
        CompetitionTrackConfig trackConfig = new CompetitionTrackConfig();
        trackConfig.setCompetitionSeriesId(SERIES_ID);
        trackConfig.setCompetitionTrackId(TRACK_ID);
        trackConfig.setSecondLevelCode("UNDERGRADUATE");
        trackConfig.setSecondLevelName("本科组");
        trackConfig.setCompetitionConfig(config);
        competitionTrackConfigResult = List.of(trackConfig);
    }

    private CompetitionApplyInfo contestant(String teamCode) {
        CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
        applyInfo.setCompetitionSeriesId(SERIES_ID);
        applyInfo.setCompetitionTrackId(TRACK_ID);
        applyInfo.setCompetitionTrackName(ApplyConstants.COMPETITION_TRACK_NAME_TEACHER);
        applyInfo.setCompetitionRoleName(ApplyConstants.TEAM_LEADER_MEMBER);
        applyInfo.setSecondLevelCode("UNDERGRADUATE");
        applyInfo.setSecondLevelName("本科组");
        applyInfo.setTeamCode(teamCode);
        applyInfo.setTeamName(teamCode);
        applyInfo.setUserName(USER_NAME);
        applyInfo.setIdCard(ID_CARD);
        applyInfo.setPhone("13800000000");
        applyInfo.setEmail("teacher@example.com");
        applyInfo.setSchoolName("Excel学校");
        applyInfo.setCompanyName("Excel单位");
        applyInfo.setOrgNameSnapshot("Excel机构");
        return applyInfo;
    }

    private void invokeTeacherValidation(List<CompetitionApplyInfo> applyInfoList) throws Throwable {
        invokePrivate(
                "validateAndBindTeacherCompetitionApplyInfo",
                new Class<?>[]{List.class, Long.class},
                applyInfoList,
                UPLOADER_USER_ID
        );
    }

    private void invokeFieldProtection(CompetitionApplyInfo requested, CompetitionApplyInfo existing) throws Throwable {
        invokePrivate(
                "protectTeacherCompetitionContestantFields",
                new Class<?>[]{CompetitionApplyInfo.class, CompetitionApplyInfo.class},
                requested,
                existing
        );
    }

    private Object invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args) throws Throwable {
        Method method = UserCompetitionServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(service, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = UserCompetitionServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> interfaceType, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                invocationHandler
        );
    }

    private Object handleObjectMethod(Object proxy, Method method, Object[] args, String name) {
        switch (method.getName()) {
            case "toString":
                return name;
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return args != null && args.length == 1 && proxy == args[0];
            default:
                return null;
        }
    }

    private void assertGlobalExceptionContains(String expectedText, ThrowingRunnable runnable) throws Throwable {
        try {
            runnable.run();
        } catch (GlobalException exception) {
            if (!exception.getMessage().contains(expectedText)) {
                fail("Expected message containing <" + expectedText + "> but was <"
                        + exception.getMessage() + ">");
            }
            return;
        }
        fail("Expected GlobalException containing: " + expectedText);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
