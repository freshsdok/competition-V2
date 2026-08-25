package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CertificateImportCompetitionInfo;
import com.teaching.competition.domain.CertificateImportRequest;
import com.teaching.competition.domain.CertificateImportSqlResult;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateImportServiceImplTest {

    private CompetitionApplyInfoMapper competitionApplyInfoMapper;
    private CertificateImportServiceImpl service;

    @Before
    public void setUp() {
        competitionApplyInfoMapper = mock(CompetitionApplyInfoMapper.class);
        service = new CertificateImportServiceImpl(competitionApplyInfoMapper);
    }

    @Test
    public void generateImportSqlBuildsExpectedCertificateNameAndBothTableInserts() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant();
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));

        CertificateImportSqlResult result = service.generateImportSql(buildSampleExcel());

        assertEquals(1, result.getRowCount());
        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains(
                "第十三届大学生新一代信息通信科技大赛工程实践赛道中，荣获安徽省赛区一等奖，特此表彰！"));
        assertTrue(result.getSqlContent().contains("INSERT INTO `user_certificate_history`"));
        assertTrue(result.getSqlContent().contains("INSERT INTO `user_certificate_origin`"));
        assertTrue(result.getSqlContent().contains("'2026-06-09 00:00:00'"));
        assertTrue(result.getSqlContent().contains("'IITCHJDICT26000001'"));
        assertTrue(result.getSqlContent().contains("'1', '一等奖', 10001, '李世祥'"));
        assertTrue(result.getSqlContent().contains("competition_series_id=77，competition_id=79"));

        String historyInsert = result.getSqlContent().substring(
                result.getSqlContent().indexOf("INSERT INTO `user_certificate_history`"),
                result.getSqlContent().indexOf("-- 2. 用户源证书"));
        assertFalse(historyInsert.contains("WHERE t.`user_id` IS NOT NULL"));
    }

    @Test
    public void generateImportSqlKeepsUnmatchedRowInHistoryAndSkipsOrigin() throws Exception {
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.emptyList());

        CertificateImportSqlResult result = service.generateImportSql(buildSampleExcel());

        assertEquals(1, result.getRowCount());
        assertEquals(0, result.getOriginRowCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("仅写入历史证书表"));
        assertTrue(result.getSqlContent().contains("WHERE t.`user_id` IS NOT NULL"));
        assertTrue(result.getSqlContent().contains("NULL, '李世祥', NULL"));
    }

    @Test
    public void generateImportSqlMatchesSchoolSuffixAlias() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                27475L, "余进凯", "云南大学滇池学院", "2026_2019758314762260487", "ICT基础通识赛");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));

        MultipartFile excel = buildExcel(
                "余进凯", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26024828",
                "滇池学院", "余进凯、李鹏", "孔新玉、付英");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("'2026_2019758314762260487'"));
    }

    @Test
    public void generateImportSqlUsesPlayerRosterToResolveMultipleTeams() throws Exception {
        CompetitionApplyInfo firstTeamApplicant = buildApplicant(
                30001L, "张鑫", "太原科技大学", "2026_2009813314003939329", "ICT基础通识赛");
        CompetitionApplyInfo secondTeamApplicant = buildApplicant(
                30001L, "张鑫", "太原科技大学", "2026_2009813314003939347", "ICT基础通识赛");
        CompetitionApplyInfo firstTeammate = buildApplicant(
                30002L, "李佳祺", "太原科技大学", "2026_2009813314003939329", "ICT基础通识赛");
        CompetitionApplyInfo secondTeammate = buildApplicant(
                30003L, "成昊", "太原科技大学", "2026_2009813314003939347", "ICT基础通识赛");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(firstTeamApplicant, secondTeamApplicant, firstTeammate, secondTeammate));

        MultipartFile excel = buildExcel(
                "张鑫", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26000343",
                "太原科技大学", "张鑫、成昊", "曹俊琴、闫晓梅");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("'2026_2009813314003939347'"));
        assertFalse(result.getSqlContent().contains("'2026_2009813314003939329'"));
    }

    @Test
    public void generateImportSqlPairsDuplicateNamesInSameTeamByTeamOrder() throws Exception {
        CompetitionApplyInfo firstApplicant = buildApplicant(
                8252L, "刘博", "黑龙江科技大学", "2025_2003109013592162304", "工程实践赛");
        firstApplicant.setMemberId(2980L);
        firstApplicant.setTeamSort(1);
        CompetitionApplyInfo secondApplicant = buildApplicant(
                8231L, "刘博", "黑龙江科技大学", "2025_2003109013592162304", "工程实践赛");
        secondApplicant.setMemberId(2981L);
        secondApplicant.setTeamSort(2);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(firstApplicant, secondApplicant));

        CertificateImportSqlResult result = service.generateImportSql(buildDuplicateNameExcel());

        assertEquals(2, result.getRowCount());
        assertEquals(2, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        String firstCertRow = sqlValueRow(result.getSqlContent(), "IITCHJDICT26005039");
        String secondCertRow = sqlValueRow(result.getSqlContent(), "IITCHJDICT26005040");
        assertTrue(firstCertRow.contains("8252, '刘博', 'ID8252'"));
        assertTrue(secondCertRow.contains("8231, '刘博', 'ID8231'"));
    }

    @Test
    public void generateImportSqlDoesNotGuessWhenDuplicateNameCertificateCountDiffers() throws Exception {
        CompetitionApplyInfo firstApplicant = buildApplicant(
                8252L, "刘博", "黑龙江科技大学", "2025_2003109013592162304", "工程实践赛");
        firstApplicant.setMemberId(2980L);
        firstApplicant.setTeamSort(1);
        CompetitionApplyInfo secondApplicant = buildApplicant(
                8231L, "刘博", "黑龙江科技大学", "2025_2003109013592162304", "工程实践赛");
        secondApplicant.setMemberId(2981L);
        secondApplicant.setTeamSort(2);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(firstApplicant, secondApplicant));

        MultipartFile excel = buildExcel(
                "刘博", "大学生新一代信息通信科技大赛工程实践赛道", "IITCHJDICT26005039",
                "黑龙江科技大学", "刘博、刘博", "宁姗、赵秋多");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(0, result.getOriginRowCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("同一报名团队存在多个同名参赛人"));
    }

    @Test
    public void generateImportSqlMatchesRenamedSchoolByUniquePlayerRoster() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                31001L, "测试学生", "浙江树人大学", "TEAM_ZJSRU_001", "ICT基础通识赛");
        CompetitionApplyInfo teammate = buildApplicant(
                31002L, "测试队友", "浙江树人大学", "TEAM_ZJSRU_001", "ICT基础通识赛");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(applicant, teammate));

        MultipartFile excel = buildExcel(
                "测试学生", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26000848",
                "浙江树人学院", "测试学生、测试队友", "指导教师甲");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("'TEAM_ZJSRU_001'"));
    }

    @Test
    public void generateImportSqlUsesRosterWhenSameNameExistsAtDifferentSchools() throws Exception {
        CompetitionApplyInfo targetApplicant = buildApplicant(
                32001L, "张思宇", "重庆电子工程职业学院", "TEAM_CQ_001", "ICT基础通识赛");
        CompetitionApplyInfo targetTeammate = buildApplicant(
                32002L, "杨熔", "重庆电子工程职业学院", "TEAM_CQ_001", "ICT基础通识赛");
        CompetitionApplyInfo otherApplicant = buildApplicant(
                32003L, "张思宇", "西安航空学院", "TEAM_XA_001", "ICT基础通识赛");
        CompetitionApplyInfo otherTeammate = buildApplicant(
                32004L, "其他队友", "西安航空学院", "TEAM_XA_001", "ICT基础通识赛");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(targetApplicant, targetTeammate, otherApplicant, otherTeammate));

        MultipartFile excel = buildExcel(
                "张思宇", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26000870",
                "重庆电子科技职业大学", "杨熔、张思宇", "指导教师乙");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("'TEAM_CQ_001'"));
        assertFalse(result.getSqlContent().contains("'TEAM_XA_001'"));
    }

    @Test
    public void generateImportSqlFallsBackToUniqueVerifiedUserByIdCard() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                6014L, "李硕磊", "黄淮学院", "2026_2031165418102210563", "ICT基础通识赛");
        applicant.setUserId(null);
        CompetitionApplyInfo verifiedUser = new CompetitionApplyInfo();
        verifiedUser.setIdCard("ID6014");
        verifiedUser.setUserId(6014L);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));
        when(competitionApplyInfoMapper.selectUniqueVerifiedUsersByIdCards(Collections.singletonList("ID6014")))
                .thenReturn(Collections.singletonList(verifiedUser));

        MultipartFile excel = buildExcel(
                "李硕磊", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26022790",
                "黄淮学院", "李硕磊", "徐毛旦、周原");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("6014, '李硕磊', 'ID6014'"));
    }

    @Test
    public void generateImportSqlFallsBackToUniqueActivePhoneAccount() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                78371L, "罗慧", "新疆天山职业技术大学", "2026_2031237488542629904", "工程实践");
        applicant.setUserId(null);
        applicant.setPhone("13800009904");
        CompetitionApplyInfo phoneUser = new CompetitionApplyInfo();
        phoneUser.setPhone("13800009904");
        phoneUser.setUserId(5069L);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));
        when(competitionApplyInfoMapper.selectUniqueVerifiedUsersByIdCards(Collections.singletonList("ID78371")))
                .thenReturn(Collections.emptyList());
        when(competitionApplyInfoMapper.selectUniqueActiveUsersByPhones(Collections.singletonList("13800009904")))
                .thenReturn(Collections.singletonList(phoneUser));

        MultipartFile excel = buildExcel(
                "罗慧", "大学生新一代信息通信科技大赛工程实践赛道", "IITCHJDICT26018644",
                "新疆天山职业技术大学", "罗慧、张新燕", "张荣斌、李慧敏");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("5069, '罗慧', 'ID78371'"));
    }

    @Test
    public void generateImportSqlReportsMissingUserIdPrecisely() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                33001L, "待关联学生", "测试大学", "TEAM_MISSING_USER", "ICT基础通识赛");
        applicant.setUserId(null);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));
        when(competitionApplyInfoMapper.selectUniqueVerifiedUsersByIdCards(Collections.singletonList("ID33001")))
                .thenReturn(Collections.emptyList());

        MultipartFile excel = buildExcel(
                "待关联学生", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26000901",
                "测试大学", "待关联学生", "指导教师甲");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(0, result.getOriginRowCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("缺少用户ID（未找到唯一的实名认证账号或报名手机号账号）"));
        assertFalse(result.getSqlContent().contains("缺少身份证号"));
    }

    @Test
    public void generateImportSqlAllowsResolvedUserWithoutIdCard() throws Exception {
        CompetitionApplyInfo applicant = buildApplicant(
                34001L, "缺证件学生", "测试大学", "TEAM_MISSING_ID_CARD", "ICT基础通识赛");
        applicant.setIdCard(null);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Collections.singletonList(applicant));

        MultipartFile excel = buildExcel(
                "缺证件学生", "大学生新一代信息通信科技大赛ICT基础通识赛道", "IITCHJDICT26000902",
                "测试大学", "缺证件学生", "指导教师乙");
        CertificateImportSqlResult result = service.generateImportSql(excel);

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("34001, '缺证件学生', NULL"));
    }

    @Test
    public void generateImportSqlGivesSameTeamCertificateToEveryStudent() throws Exception {
        CompetitionApplyInfo first = buildTeamApplicant(
                41001L, 51001L, 1, "王慧雯", "征拓队", "安徽信息工程学院", "TEAM_5G_001");
        CompetitionApplyInfo second = buildTeamApplicant(
                41002L, 51002L, 2, "邢圆圆", "征拓队", "安徽信息工程学院", "TEAM_5G_001");
        CompetitionApplyInfo third = buildTeamApplicant(
                41003L, 51003L, 3, "周逸航", "征拓队", "安徽信息工程学院", "TEAM_5G_001");
        CompetitionApplyInfo teacher = buildTeamApplicant(
                49999L, 59999L, 4, "彭佩云", "征拓队", "安徽信息工程学院", "TEAM_5G_001");
        teacher.setCompetitionRoleName("指导教师");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(first, second, third, teacher));

        CertificateImportSqlResult result = service.generateImportSql(buildTeamExcel(
                "征拓队", "IITCHJDICT26024949", "安徽信息工程学院", "王慧雯、邢圆圆、周逸航", "彭佩云、申鹏"));

        assertEquals(1, result.getRowCount());
        assertEquals(3, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains(
                "第十三届大学生新一代信息通信科技大赛产教融合5G+创新应用赛道中，荣获省赛一等奖，特此表彰！"));
        assertEquals(3, countOccurrences(result.getSqlContent(), "'IITCHJDICT26024949'"));
        assertTrue(result.getSqlContent().contains("41001, '王慧雯', 'ID41001'"));
        assertTrue(result.getSqlContent().contains("41002, '邢圆圆', 'ID41002'"));
        assertTrue(result.getSqlContent().contains("41003, '周逸航', 'ID41003'"));
        assertFalse(result.getSqlContent().contains("49999, '彭佩云'"));
        assertTrue(result.getSqlContent().contains("WHERE t.`history_row` = 1"));
    }

    @Test
    public void generateImportSqlDoesNotGuessTeamWhenCompleteRosterDiffers() throws Exception {
        CompetitionApplyInfo first = buildTeamApplicant(
                42001L, 52001L, 1, "李飒", "Maybach", "北华大学", "TEAM_5G_002");
        CompetitionApplyInfo second = buildTeamApplicant(
                42002L, 52002L, 2, "刘相宏", "Maybach", "北华大学", "TEAM_5G_002");
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(77L))
                .thenReturn(Arrays.asList(first, second));

        CertificateImportSqlResult result = service.generateImportSql(buildTeamExcel(
                "Maybach", "IITCHJDICT26024950", "北华大学", "李飒、其他队员", "马惜平、高玉峰"));

        assertEquals(1, result.getRowCount());
        assertEquals(0, result.getOriginRowCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("未找到完整参赛队员名单完全一致的团队"));
        assertTrue(result.getSqlContent().contains("仅写入历史证书表"));
    }

    @Test
    public void generateNationalTeamSqlSupportsActualTeamHeaderAndMeritAward() throws Exception {
        CompetitionApplyInfo first = buildTeamApplicant(
                51001L, 61001L, 1, "张伯豪", "小唐人", "北京化工大学", "TEAM_NATIONAL_001");
        CompetitionApplyInfo second = buildTeamApplicant(
                51002L, 61002L, 2, "张凯麟", "小唐人", "北京化工大学", "TEAM_NATIONAL_001");
        stubNationalCompetition(Arrays.asList(first, second));

        MultipartFile excel = buildWorkbook(
                "国赛团队.xlsx",
                new String[]{"序号", "参赛姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号",
                        "参赛单位", "参赛队员", "指导教师", "流水号"},
                new String[]{"1", "小唐人", "十三", "大学生新一代信息通信科技大赛产教融合5G+创新应用赛道",
                        "全国总决赛", "优胜奖", "IITCHJDICT26027678", "北京化工大学",
                        "张伯豪、张凯麟", "邢藏菊、何宾", ""});

        CertificateImportSqlResult result = service.generateImportSql(
                excel, nationalRequest("STUDENT_TEAM"));

        assertEquals(1, result.getRowCount());
        assertEquals(2, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("competition_series_id=81，competition_id=83"));
        assertTrue(result.getSqlContent().contains("'2026-08-03 00:00:00'"));
        assertTrue(result.getSqlContent().contains("'4', '优胜奖'"));
    }

    @Test
    public void generateNationalTeamSqlMatchesEnglishNamesAndAllowsMissingIdCard() throws Exception {
        CompetitionApplyInfo first = buildTeamApplicant(
                51101L, 61101L, 1, "Li Ming", "International Team", "Test University", "TEAM_NATIONAL_ENGLISH");
        first.setIdCard(null);
        CompetitionApplyInfo second = buildTeamApplicant(
                51102L, 61102L, 2, "王小明", "International Team", "Test University", "TEAM_NATIONAL_ENGLISH");
        stubNationalCompetition(Arrays.asList(first, second));

        MultipartFile excel = buildWorkbook(
                "国赛英文姓名团队.xlsx",
                new String[]{"序号", "参赛姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号",
                        "参赛单位", "参赛队员", "指导教师", "流水号"},
                new String[]{"1", "International Team", "十三", "大学生新一代信息通信科技大赛产教融合5G+创新应用赛道",
                        "全国总决赛", "一等奖", "IITCHJDICT26027677", "Test University",
                        "LI  MING、王小明", "Teacher A", ""});

        CertificateImportSqlResult result = service.generateImportSql(
                excel, nationalRequest("STUDENT_TEAM"));

        assertEquals(2, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("51101, 'Li Ming', NULL"));
        assertTrue(result.getSqlContent().contains("51102, '王小明', 'ID51102'"));
    }

    @Test
    public void generateTeacherHonorSqlUsesConfirmedNameAndAllowsMissingIdCard() throws Exception {
        CompetitionApplyInfo teacher = buildApplicant(
                52001L, "张钰", "安徽大学", "TEAM_TEACHER_001", "工程实践赛");
        teacher.setCompetitionRoleName("指导教师");
        teacher.setIdCard(null);
        stubNationalCompetition(Collections.singletonList(teacher));

        MultipartFile excel = buildWorkbook(
                "国赛教师.xlsx",
                new String[]{"序号", "老师姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位", "流水号"},
                new String[]{"1", "张钰", "十三", "大学生新一代信息通信科技大赛", "全国总决赛",
                        "工程实践赛道一等奖1组，二等奖1组", "IITCRYDICT26027679", "安徽大学", ""});

        CertificateImportSqlResult result = service.generateImportSql(
                excel, nationalRequest("TEACHER_HONOR"));

        assertEquals(1, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains(
                "第十三届大学生新一代信息通信科技大赛全国总决赛中，指导学生获得工程实践赛道一等奖1组，二等奖1组，"
                        + "成绩斐然，荣获“优秀指导教师”，特发此证，以资鼓励！"));
        String certificateRow = sqlValueRow(result.getSqlContent(), "IITCRYDICT26027679");
        assertTrue(certificateRow.contains("'工程实践赛道一等奖1组，二等奖1组'"));
        assertFalse(certificateRow.contains("TEAM_TEACHER_001"));
    }

    @Test
    public void generateTeacherHonorSqlKeepsUnmatchedTeacherInHistoryOnly() throws Exception {
        stubNationalCompetition(Collections.emptyList());
        MultipartFile excel = buildWorkbook(
                "国赛教师未匹配.xlsx",
                new String[]{"序号", "老师姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位"},
                new String[]{"1", "待匹配老师", "十三", "大学生新一代信息通信科技大赛", "全国总决赛",
                        "ICT基础通识赛道三等奖1组", "IITCRYDICT26029337", "测试大学"});

        CertificateImportSqlResult result = service.generateImportSql(
                excel, nationalRequest("TEACHER_HONOR"));

        assertEquals(0, result.getOriginRowCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getSqlContent().contains("未匹配到老师姓名，仅写入历史证书表"));
    }

    @Test
    public void generateOrganizationHonorSqlWritesHistoryOnlyWithConfirmedName() throws Exception {
        stubNationalCompetition(Collections.emptyList());
        MultipartFile excel = buildWorkbook(
                "优秀组织单位.xlsx",
                new String[]{"序号", "届数", "大赛名称", "称号", "证书编号", "参赛单位"},
                new String[]{"1", "十三", "大学生新一代信息通信科技大赛", "优秀组织协同奖",
                        "IITCRYDICT26029338", "北方工业大学"});

        CertificateImportSqlResult result = service.generateImportSql(
                excel, nationalRequest("ORGANIZATION_HONOR"));

        assertEquals(1, result.getRowCount());
        assertEquals(0, result.getOriginRowCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getSqlContent().contains(
                "第十三届大学生新一代信息通信科技大赛中荣获优秀组织协同奖，特发此证，以资鼓励！"));
        assertTrue(result.getSqlContent().contains("'北方工业大学', '北方工业大学'"));
    }

    private MultipartFile buildSampleExcel() throws IOException {
        return buildExcel(
                "李世祥", "大学生新一代信息通信科技大赛工程实践赛道", "IITCHJDICT26000001",
                "安徽财经大学", "李世祥、马万里", "穆宽林");
    }

    private MultipartFile buildTeamExcel(String teamName, String certCode, String schoolName,
                                         String players, String guideTeachers) throws IOException {
        String[] headers = {
                "序号", "团队名称", "届数", "大赛名称", "赛道", "赛区", "奖项", "证书编号",
                "参赛单位", "参赛队员", "指导教师", "流水号"
        };
        String[] values = {
                "1", teamName, "十三", "大学生新一代信息通信科技大赛产教融合5G+创新应用赛道",
                "产教融合5G+创新应用赛道", "省赛", "一等奖", certCode,
                schoolName, players, guideTeachers, ""
        };
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("团队证书");
            Row headerRow = sheet.createRow(0);
            Row dataRow = sheet.createRow(1);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
                dataRow.createCell(index).setCellValue(values[index]);
            }
            workbook.write(outputStream);
            return new ByteArrayMultipartFile("产教融合5G团队证书.xlsx", outputStream.toByteArray());
        }
    }

    private MultipartFile buildDuplicateNameExcel() throws IOException {
        String[] headers = {
                "序号", "参赛姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号",
                "参赛单位", "参赛队员", "指导教师", "流水号"
        };
        String[][] values = {
                {"5039", "刘博", "十三", "大学生新一代信息通信科技大赛工程实践赛道", "黑龙江省赛区", "二等奖",
                        "IITCHJDICT26005039", "黑龙江科技大学", "刘博、刘博", "宁姗、赵秋多", ""},
                {"5040", "刘博", "十三", "大学生新一代信息通信科技大赛工程实践赛道", "黑龙江省赛区", "二等奖",
                        "IITCHJDICT26005040", "黑龙江科技大学", "刘博、刘博", "宁姗、赵秋多", ""}
        };
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }
            for (int rowIndex = 0; rowIndex < values.length; rowIndex++) {
                Row dataRow = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < values[rowIndex].length; columnIndex++) {
                    dataRow.createCell(columnIndex).setCellValue(values[rowIndex][columnIndex]);
                }
            }
            workbook.write(outputStream);
            return new ByteArrayMultipartFile("同队同名学生证书.xlsx", outputStream.toByteArray());
        }
    }

    private MultipartFile buildWorkbook(String fileName, String[] headers, String[] values) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            Row dataRow = sheet.createRow(1);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
                if (index < values.length) {
                    dataRow.createCell(index).setCellValue(values[index]);
                }
            }
            workbook.write(outputStream);
            return new ByteArrayMultipartFile(fileName, outputStream.toByteArray());
        }
    }

    private void stubNationalCompetition(java.util.List<CompetitionApplyInfo> applicants) {
        CertificateImportCompetitionInfo info = new CertificateImportCompetitionInfo();
        info.setCompetitionSeriesId(81L);
        info.setCompetitionSeriesName("第十三届");
        info.setCompetitionId(83L);
        info.setCompetitionName("大学生新一代信息通信科技大赛全国总决赛");
        when(competitionApplyInfoMapper.selectCertificateImportCompetitionInfo(81L)).thenReturn(info);
        when(competitionApplyInfoMapper.selectCertificateImportApplicants(81L)).thenReturn(applicants);
    }

    private CertificateImportRequest nationalRequest(String certificateType) {
        CertificateImportRequest request = new CertificateImportRequest();
        request.setCompetitionSeriesId(81L);
        request.setCertificateType(certificateType);
        request.setIssuanceDate("2026-08-03");
        return request;
    }

    private String sqlValueRow(String sql, String certCode) {
        int certIndex = sql.indexOf("'" + certCode + "'");
        int rowStart = sql.lastIndexOf("  (", certIndex);
        int rowEnd = sql.indexOf('\n', certIndex);
        return sql.substring(rowStart, rowEnd);
    }

    private int countOccurrences(String text, String fragment) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(fragment, index)) >= 0) {
            count++;
            index += fragment.length();
        }
        return count;
    }

    private MultipartFile buildExcel(String userName, String competitionName, String certCode,
                                     String schoolName, String players, String guideTeachers) throws IOException {
        String[] headers = {
                "序号", "参赛姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号",
                "参赛单位", "参赛队员", "指导教师", "流水号"
        };
        String[] values = {
                "1", userName, "十三", competitionName, "安徽省赛区", "一等奖",
                certCode, schoolName, players, guideTeachers, ""
        };
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("获奖证书");
            Row headerRow = sheet.createRow(0);
            Row dataRow = sheet.createRow(1);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
                dataRow.createCell(index).setCellValue(values[index]);
            }
            workbook.write(outputStream);
            return new ByteArrayMultipartFile("学生获奖证书.xlsx", outputStream.toByteArray());
        }
    }

    private CompetitionApplyInfo buildApplicant() {
        return buildApplicant(10001L, "李世祥", "安徽财经大学", "TEAM_100001", "工程实践");
    }

    private CompetitionApplyInfo buildApplicant(Long userId, String userName, String schoolName,
                                                 String teamCode, String trackName) {
        CompetitionApplyInfo applicant = new CompetitionApplyInfo();
        applicant.setUserId(userId);
        applicant.setUserName(userName);
        applicant.setIdCard("ID" + userId);
        applicant.setSchool("1001");
        applicant.setSchoolName(schoolName);
        applicant.setCompetitionTrackId("SD_100001");
        applicant.setCompetitionTrackName(trackName);
        applicant.setSecondLevelCode("CT_100001");
        applicant.setSecondLevelName("工程实践组");
        applicant.setTeamCode(teamCode);
        applicant.setCompetitionRoleName("参赛选手");
        return applicant;
    }

    private CompetitionApplyInfo buildTeamApplicant(Long userId, Long memberId, int teamSort,
                                                     String userName, String teamName, String schoolName,
                                                     String teamCode) {
        CompetitionApplyInfo applicant = buildApplicant(
                userId, userName, schoolName, teamCode, "产教融合5G+创新应用赛");
        applicant.setMemberId(memberId);
        applicant.setTeamSort(teamSort);
        applicant.setTeamName(teamName);
        applicant.setCompetitionTrackId("SD_513057");
        applicant.setSecondLevelCode("CT_5G_001");
        applicant.setSecondLevelName("产教融合5G+创新应用组");
        return applicant;
    }

    private static final class ByteArrayMultipartFile implements MultipartFile {
        private final String originalFilename;
        private final byte[] content;

        private ByteArrayMultipartFile(String originalFilename, byte[] content) {
            this.originalFilename = originalFilename;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) {
            throw new UnsupportedOperationException("not needed by this test");
        }
    }
}
