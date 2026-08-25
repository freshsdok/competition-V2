package com.teaching.competition.service;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.Team;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
public class ExcelTeamReaderTeacherCompetitionTest {

    private static final String[] TEACHER_HEADERS = {
            "*参赛组别",
            "*选手1姓名",
            "*选手证件号",
            "*选手手机号",
            "*选手邮箱",
            "*选手性别\n（下拉选择）",
            "*学校",
            "单位",
            "机构",
            "工号",
            "院系",
            "职务",
            "指导教师1姓名",
            "指导教师1手机",
            "指导教师1邮箱",
            "指导教师2姓名",
            "指导教师2手机",
            "指导教师2邮箱"
    };

    private static final String[] COMPACT_TEACHER_HEADERS = {
            "*参赛组别", "*学校", "*选手1姓名", "*选手证件号", "*选手手机号", "*选手邮箱", "*选手性别",
            "指导教师1姓名", "指导教师1手机", "指导教师1邮箱", "指导教师2姓名", "指导教师2手机", "指导教师2邮箱"
    };

    @Test
    public void emptyTeacherSheetProducesNoTeams() throws Exception {
        byte[] workbook = createWorkbook(true, null);

        List<Team> teams = new ExcelTeamReader()
                .readTeamsFromExcel(new ByteArrayInputStream(workbook));

        assertTrue(teams.isEmpty());
    }

    @Test
    public void jsonRenamedAsExcelIsRejectedWithReadableMessage() throws Exception {
        assertGlobalException("不是有效的Excel文件", () ->
                new ExcelTeamReader().readTeamsFromExcel(
                        new ByteArrayInputStream("{\"code\":500,\"msg\":\"服务未找到\"}".getBytes())));
    }

    @Test
    public void invalidTeacherCompetitionGroupIsRejectedBeforeAuthentication() throws Exception {
        byte[] workbook = createWorkbook(true, row -> {
            populateValidPlayer(row);
            row.getCell(0).setCellValue("研究生组");
        });

        assertGlobalException("只能选择“职业组（含中职）”或“本科组”", () ->
                new ExcelTeamReader().readTeamsFromExcel(new ByteArrayInputStream(workbook)));
    }

    @Test
    public void invalidTeacherCompetitionSexIsRejectedBeforeAuthentication() throws Exception {
        byte[] workbook = createWorkbook(true, row -> {
            populateValidPlayer(row);
            row.getCell(5).setCellValue("未知");
        });

        assertGlobalException("只能选择“男”或“女”", () ->
                new ExcelTeamReader().readTeamsFromExcel(new ByteArrayInputStream(workbook)));
    }

    @Test
    public void allGuideTeacherFieldsMayBeBlank() throws Exception {
        byte[] workbook = createWorkbook(true, this::populateValidPlayer);

        List<Team> teams = new ExcelTeamReader()
                .readTeamsFromExcel(new ByteArrayInputStream(workbook));

        assertEquals(1, teams.size());
        assertEquals(1, teams.get(0).getPlayers().size());
        assertTrue(teams.get(0).getGuideTeachers().isEmpty());
        assertEquals(ApplyConstants.TEAM_LEADER_MEMBER,
                teams.get(0).getPlayers().get(0).getCompetitionRoleName());
        assertEquals(Integer.valueOf(1), teams.get(0).getPlayers().get(0).getTeamSort());
        assertEquals("测试学校", teams.get(0).getPlayers().get(0).getSchoolName());
        assertEquals("测试单位", teams.get(0).getPlayers().get(0).getCompanyName());
    }

    @Test
    public void compactTeacherTemplateIsAccepted() throws Exception {
        byte[] workbook = createWorkbook(true, COMPACT_TEACHER_HEADERS, row -> {
            row.createCell(0).setCellValue("本科组");
            row.createCell(1).setCellValue("测试学校");
            row.createCell(2).setCellValue("教师选手");
            row.createCell(3).setCellValue("11010519491231002X");
            row.createCell(4).setCellValue("13800000000");
            row.createCell(5).setCellValue("teacher@example.com");
            row.createCell(6).setCellValue("男");
        });

        List<Team> teams = new ExcelTeamReader()
                .readTeamsFromExcel(new ByteArrayInputStream(workbook));

        assertEquals(1, teams.size());
        assertEquals("测试学校", teams.get(0).getPlayers().get(0).getSchoolName());
        assertEquals("教师选手", teams.get(0).getPlayers().get(0).getUserName());
    }

    @Test
    public void schoolIsRequired() throws Exception {
        byte[] workbook = createWorkbook(true, row -> {
            populateValidPlayer(row);
            row.getCell(6).setCellValue("");
        });

        assertGlobalException("【*学校】未填写", () ->
                new ExcelTeamReader().readTeamsFromExcel(new ByteArrayInputStream(workbook)));
    }

    @Test
    public void guideTeacherPhoneWithoutNameIsRejected() throws Exception {
        byte[] workbook = createWorkbook(true, row -> {
            populateValidPlayer(row);
            row.createCell(13).setCellValue("13800000001");
        });

        assertGlobalException("【指导教师1姓名】未填写", () ->
                new ExcelTeamReader().readTeamsFromExcel(new ByteArrayInputStream(workbook)));
    }

    @Test
    public void legacyFourSheetTemplateIsStillAccepted() throws Exception {
        byte[] workbook = createWorkbook(false, null);

        List<Team> teams = new ExcelTeamReader()
                .readTeamsFromExcel(new ByteArrayInputStream(workbook));

        assertTrue(teams.isEmpty());
    }

    private byte[] createWorkbook(boolean includeTeacherSheet, RowCustomizer rowCustomizer)
            throws IOException {
        return createWorkbook(includeTeacherSheet, TEACHER_HEADERS, rowCustomizer);
    }

    private byte[] createWorkbook(boolean includeTeacherSheet, String[] teacherHeaders,
                                  RowCustomizer rowCustomizer) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            createLegacySheet(workbook, "工程实践赛", 21);
            createLegacySheet(workbook, "产教融合5G+创新应用赛", 50);
            createLegacySheet(workbook, "ICT基础通识赛", 21);
            createLegacySheet(workbook, "“一带一路”留学生组", 62);
            if (includeTeacherSheet) {
                Sheet teacherSheet = workbook.createSheet(ApplyConstants.COMPETITION_TRACK_NAME_TEACHER);
                createTitleAndHeaders(teacherSheet, teacherHeaders);
                if (rowCustomizer != null) {
                    rowCustomizer.customize(teacherSheet.createRow(2));
                }
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createLegacySheet(Workbook workbook, String sheetName, int headerCount) {
        Sheet sheet = workbook.createSheet(sheetName);
        String[] headers = new String[headerCount];
        for (int i = 0; i < headerCount; i++) {
            headers[i] = "列" + (i + 1);
        }
        createTitleAndHeaders(sheet, headers);
    }

    private void createTitleAndHeaders(Sheet sheet, String[] headers) {
        sheet.createRow(0).createCell(0).setCellValue("报名注意事项");
        Row header = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    private void populateValidPlayer(Row row) {
        row.createCell(0).setCellValue("本科组");
        row.createCell(1).setCellValue("教师选手");
        row.createCell(2).setCellValue("11010519491231002X");
        row.createCell(3).setCellValue("13800000000");
        row.createCell(4).setCellValue("teacher@example.com");
        row.createCell(5).setCellValue("男");
        row.createCell(6).setCellValue("测试学校");
        row.createCell(7).setCellValue("测试单位");
        row.createCell(8).setCellValue("测试机构");
        row.createCell(9).setCellValue("T001");
        row.createCell(10).setCellValue("计算机学院");
        row.createCell(11).setCellValue("讲师");
    }

    private void assertGlobalException(String expectedMessagePart, ThrowingRunnable runnable)
            throws Exception {
        try {
            runnable.run();
        } catch (GlobalException exception) {
            assertTrue("实际异常信息：" + exception.getMessage(),
                    exception.getMessage().contains(expectedMessagePart));
            return;
        }
        fail("Expected GlobalException containing: " + expectedMessagePart);
    }

    @FunctionalInterface
    private interface RowCustomizer {
        void customize(Row row);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
