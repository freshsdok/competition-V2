package com.teaching.competition.service;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.GuideTeacher;
import com.teaching.competition.domain.Player;
import com.teaching.competition.domain.Team;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelTeamReader {

    private static final String CompetitionTrackNameOne = "工程实践赛";
    private static final String CompetitionTrackNameTwo = "产教融合5G+创新应用赛";
    private static final String CompetitionTrackNameThree = "ICT基础通识赛";
    private static final String CompetitionTrackNameFour = "“一带一路”留学生组";
    private static final String CompetitionTrackNameTeacher = ApplyConstants.COMPETITION_TRACK_NAME_TEACHER;
    // 判断用户名是邮箱还是手机号
    public static final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // 手机号正则
    public static final String phoneRegex = "^1\\d{10}$";
    // 大陆居民身份证号码
    public static final String mainlandIdCardRegex = "^\\d{17}[\\dXx]$";

    public static final String nationalityName = "中国";

    private static final Set<String> legacyCompetitionTrackNames = Set.of(
            CompetitionTrackNameOne,
            CompetitionTrackNameTwo,
            CompetitionTrackNameThree,
            CompetitionTrackNameFour
    );

    private static final Set<String> teacherCompetitionGroupNames = Set.of(
            "职业组（含中职）",
            "本科组"
    );

    private static final List<String> teacherCompetitionHeaders = List.of(
            "*参赛组别",
            "*选手1姓名",
            "*选手证件号",
            "*选手手机号",
            "*选手邮箱",
            "*选手性别",
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
    );

    private static final List<String> compactTeacherCompetitionHeaders = List.of(
            "*参赛组别", "*学校", "*选手1姓名", "*选手证件号", "*选手手机号", "*选手邮箱", "*选手性别",
            "指导教师1姓名", "指导教师1手机", "指导教师1邮箱", "指导教师2姓名", "指导教师2手机", "指导教师2邮箱"
    );

    public static final List<String> competitionTrackNameList = Arrays.asList(
            CompetitionTrackNameOne,
            CompetitionTrackNameTwo,
            CompetitionTrackNameThree,
            CompetitionTrackNameFour,
            CompetitionTrackNameTeacher
    );
    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    private Excel.Type type;

    public List<CompetitionApplyInfo> readCompetitionApplyInfoFromExcel(InputStream is) throws Exception {
        List<CompetitionApplyInfo> competitionApplyInfos = new ArrayList<>();
        List<Team> teams = this.readTeamsFromExcel(is);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (CollectionUtils.isNotEmpty(teams)) {
            teams.stream().forEach(team -> {
                String teamCode = currentYear+"_"+ IdUtil.getSnowflakeNextId();
                // 参赛选手
                if (CollectionUtils.isNotEmpty(team.getPlayers())) {
                    team.getPlayers().stream().forEach(player -> {
                        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                        BeanUtils.copyProperties(player, competitionApplyInfo);
                        competitionApplyInfo.setTeamName(team.getTeamName());
                        competitionApplyInfo.setTeamCode(teamCode);
                        competitionApplyInfo.setCompetitionTrackName(team.getCompetitionTrackName());
                        competitionApplyInfo.setSecondLevelName(team.getSecondLevelName());
                        competitionApplyInfo.setInvoiceStatus("0");
                        competitionApplyInfo.setIdCardType(player.getIdCardType());
                        if (CompetitionTrackNameTeacher.equals(team.getCompetitionTrackName())) {
                            competitionApplyInfo.setJoinType(Constants.JOIN_TYPE_TEAM);
                        }
                        competitionApplyInfos.add(competitionApplyInfo);
                    });
                }
                // 指导教师
                if(CollectionUtils.isNotEmpty(team.getGuideTeachers())){
                    team.getGuideTeachers().forEach(guideTeacher -> {
                        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
                        BeanUtils.copyProperties(guideTeacher, competitionApplyInfo);
                        competitionApplyInfo.setTeamName(team.getTeamName());
                        competitionApplyInfo.setTeamCode(teamCode);
                        competitionApplyInfo.setCompetitionTrackName(team.getCompetitionTrackName());
                        competitionApplyInfo.setSecondLevelName(team.getSecondLevelName());
                        if (CompetitionTrackNameTeacher.equals(team.getCompetitionTrackName())) {
                            competitionApplyInfo.setJoinType(Constants.JOIN_TYPE_TEAM);
                        }
                        competitionApplyInfos.add(competitionApplyInfo);
                    });
                }
            });
        }
        return competitionApplyInfos;
    }

//    public String createTeamCode(){
//        // 报名编号最大值
//        AtomicInteger memberId = new AtomicInteger();
//        RedisService redisService = SpringUtils.getBean(RedisService.class);
//        if(redisService.hasKey("memberId")){
//            int memberIdRedis= redisService.getCacheObject("memberId");
//            memberId.set(memberIdRedis);
//            redisService.setCacheObject("memberId", memberId.getAndIncrement());
//        } else {
//            CompetitionApplyInfoMapper competitionApplyInfoMapper = SpringUtils.getBean(CompetitionApplyInfoMapper.class);
//            AtomicInteger memberIdLocal = new AtomicInteger(competitionApplyInfoMapper.selectMaxMemberId());
//            redisService.setCacheObject("memberId", memberIdLocal.getAndIncrement());
//            memberId = redisService.getCacheObject("memberId");
//        }
//        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//        // 生成唯一团队编号
//        return currentYear+"_"+memberId;
//    }

    // 解析excel数据
    public List<Team> readTeamsFromExcel(InputStream is) throws Exception {
        List<Team> teams = new ArrayList<>();
        this.type = Excel.Type.IMPORT;
        try {
            this.wb = WorkbookFactory.create(is);
        } catch (IOException e) {
            throw new GlobalException("上传的文件不是有效的Excel文件，请重新下载报名模板后填写并上传（请勿上传.~开头的临时文件）");
        }
        Iterator<Sheet> sheetIterator = wb.sheetIterator();
        Set<String> sheetNames = new HashSet<>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheetNames.add(wb.getSheetName(i));
        }
        Set<String> competitionTrackNamesWithTeacher = new HashSet<>(legacyCompetitionTrackNames);
        competitionTrackNamesWithTeacher.add(CompetitionTrackNameTeacher);
        boolean legacyTemplate = sheetNames.equals(legacyCompetitionTrackNames);
        boolean teacherTemplate = sheetNames.equals(competitionTrackNamesWithTeacher);
        if (!legacyTemplate && !teacherTemplate) {
            throw new GlobalException("请勿删除其他sheet页或本表首行、表头等信息");
        }
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            //校验sheet页准确性
            if(!competitionTrackNameList.contains(sheet.getSheetName())){
                throw new GlobalException("请勿删除其他sheet页或本表首行、表头等信息");
            }
            switch (sheet.getSheetName()) {
                case CompetitionTrackNameOne, CompetitionTrackNameThree:
                    teams.addAll(getTeamFromRow(sheet));
                    break;
                case CompetitionTrackNameTwo:
                    teams.addAll(getTeamFromRow(sheet,7));
                    break;
                case CompetitionTrackNameFour:
                    teams.addAll(getNationTeamFromRow(sheet,9));
                    break;
                case CompetitionTrackNameTeacher:
                    teams.addAll(getTeacherTeamFromRow(sheet));
                    break;
                default:
                    break;
            }
        }
        wb.close();
        is.close();
        return teams;
    }

    // 教师赛：每行固定读取一名参赛选手和零至两名指导教师
    private List<Team> getTeacherTeamFromRow(Sheet sheet) throws Exception {
        List<Team> teams = new ArrayList<>();
        checkExcel(sheet);
        List<String> headers = checkTeacherCompetitionHeader(sheet);
        boolean compactTemplate = headers == compactTeacherCompetitionHeaders;
        int playerStart = compactTemplate ? 2 : 1;
        int schoolIndex = compactTemplate ? 1 : 6;
        int guideTeacherStart = compactTemplate ? 7 : 12;
        for (Row row : sheet) {
            if (row.getRowNum() <= 1 || isRowEmpty(row)) {
                continue;
            }
            int excelLine = row.getRowNum() + 1;
            String secondLevelName = getCellValueAsString(row.getCell(0));
            if (StringUtils.isBlank(secondLevelName)) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheet.getSheetName()
                        + "】中【" + teacherCompetitionHeaders.get(0) + "】未填写，请补充");
            }
            if (!teacherCompetitionGroupNames.contains(secondLevelName)) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheet.getSheetName()
                        + "】中【" + teacherCompetitionHeaders.get(0)
                        + "】填写有误，只能选择“职业组（含中职）”或“本科组”");
            }
            Player player = new Player();
            player.setUserName(getCellValueAsString(row.getCell(playerStart)));
            player.setIdCard(getCellValueAsString(row.getCell(playerStart + 1)));
            player.setPhone(getCellValueAsString(row.getCell(playerStart + 2)));
            player.setEmail(getCellValueAsString(row.getCell(playerStart + 3)));
            player.setSex(getCellValueAsString(row.getCell(playerStart + 4)));
            player.setSchoolName(getCellValueAsString(row.getCell(schoolIndex)));
            if (!compactTemplate) {
                player.setCompanyName(getCellValueAsString(row.getCell(7)));
                player.setOrgNameSnapshot(getCellValueAsString(row.getCell(8)));
                player.setEmployeeCode(getCellValueAsString(row.getCell(9)));
                player.setDepartmentName(getCellValueAsString(row.getCell(10)));
                player.setProfession(getCellValueAsString(row.getCell(11)));
            }
            player.setCompetitionRoleName(ApplyConstants.TEAM_LEADER_MEMBER);
            player.setTeamSort(1);
            player.setMessage(checkTeacherCompetitionPlayer(player, sheet.getSheetName(), excelLine));

            Team team = new Team();
            team.setSecondLevelName(secondLevelName);
            team.setCompetitionTrackName(sheet.getSheetName());
            team.setTeamName("DS_" + secondLevelName + "_" + RandomUtil.randomNumbers(6));
            team.addPlayer(player);

            int teacherSort = 1;
            for (int teacherIndex = 0; teacherIndex < 2; teacherIndex++) {
                int columnIndex = guideTeacherStart + teacherIndex * 3;
                String teacherName = getCellValueAsString(row.getCell(columnIndex));
                String teacherPhone = getCellValueAsString(row.getCell(columnIndex + 1));
                String teacherEmail = getCellValueAsString(row.getCell(columnIndex + 2));
                if (StringUtils.isAllBlank(teacherName, teacherPhone, teacherEmail)) {
                    continue;
                }
                if (StringUtils.isBlank(teacherName)) {
                    throw new GlobalException("【第" + excelLine + "行】【" + sheet.getSheetName()
                            + "】中【" + headers.get(columnIndex)
                            + "】未填写，请补充");
                }
                if (StringUtils.isNotBlank(teacherPhone) && !teacherPhone.matches(phoneRegex)) {
                    throw new GlobalException("【第" + excelLine + "行】【" + sheet.getSheetName()
                            + "】中【" + teacherName + "】【"
                            + headers.get(columnIndex + 1) + "】格式有误，请正确填写");
                }
                if (StringUtils.isNotBlank(teacherEmail) && !teacherEmail.matches(emailRegex)) {
                    throw new GlobalException("【第" + excelLine + "行】【" + sheet.getSheetName()
                            + "】中【" + teacherName + "】【"
                            + headers.get(columnIndex + 2) + "】格式有误，请正确填写");
                }

                GuideTeacher guideTeacher = new GuideTeacher();
                guideTeacher.setGuideTeacherName(teacherName);
                guideTeacher.setGuideTeacherPhone(teacherPhone);
                guideTeacher.setGuideTeacherEmail(teacherEmail);
                guideTeacher.setUserName(teacherName);
                guideTeacher.setPhone(teacherPhone);
                guideTeacher.setEmail(teacherEmail);
                guideTeacher.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
                guideTeacher.setTeamSort(teacherSort++);
                team.addGuideTeacher(guideTeacher);
            }
            teams.add(team);
        }
        return teams;
    }

    private List<String> checkTeacherCompetitionHeader(Sheet sheet) {
        Row header = sheet.getRow(1);
        if (header != null) {
            for (List<String> expectedHeaders : List.of(teacherCompetitionHeaders, compactTeacherCompetitionHeaders)) {
                if (matchesTeacherCompetitionHeader(header, expectedHeaders)) {
                    return expectedHeaders;
                }
            }
        }
        throw new GlobalException("请勿删除、增加或修改【" + sheet.getSheetName() + "】sheet页表头");
    }

    private boolean matchesTeacherCompetitionHeader(Row header, List<String> expectedHeaders) {
        if (getRowNotEmptyCellNum(header) != expectedHeaders.size()) {
            return false;
        }
        for (int i = 0; i < expectedHeaders.size(); i++) {
            String actualHeader = normalizeHeader(getCellValueAsString(header.getCell(i)));
            String expectedHeader = normalizeHeader(expectedHeaders.get(i));
            boolean sexHeaderWithHint = "*选手性别".equals(expectedHeader)
                    && normalizeHeader("*选手性别（下拉选择）").equals(actualHeader);
            boolean legacyRequiredFirstTeacherHeader = "指导教师1姓名".equals(expectedHeader)
                    && normalizeHeader("*指导教师1姓名").equals(actualHeader);
            if (!Objects.equals(expectedHeader, actualHeader)
                    && !sexHeaderWithHint
                    && !legacyRequiredFirstTeacherHeader) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String header) {
        return header == null ? null : header.replaceAll("[\\s\\u3000]+", "");
    }

    private String checkTeacherCompetitionPlayer(Player player, String sheetName, int excelLine) {
        if (StringUtils.isBlank(player.getUserName())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + teacherCompetitionHeaders.get(1) + "】未填写，请补充");
        }
        if (StringUtils.isBlank(player.getIdCard())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(2) + "】未填写，请补充");
        }
        if (!player.getIdCard().matches(mainlandIdCardRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(2)
                    + "】格式有误，请填写18位大陆居民身份证号码");
        }
        player.setIdCard(player.getIdCard().toUpperCase(Locale.ROOT));
        player.setIdCardType("1");
        if (StringUtils.isBlank(player.getPhone())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(3) + "】未填写，请补充");
        }
        if (!player.getPhone().matches(phoneRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(3) + "】格式有误，请正确填写");
        }
        if (StringUtils.isBlank(player.getEmail())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(4) + "】未填写，请补充");
        }
        if (!player.getEmail().matches(emailRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(4) + "】格式有误，请正确填写");
        }
        if (StringUtils.isBlank(player.getSex())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(5) + "】未填写，请补充");
        }
        if (!Set.of("男", "女").contains(player.getSex())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(5)
                    + "】填写有误，只能选择“男”或“女”");
        }
        if (StringUtils.isBlank(player.getSchoolName())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【"
                    + player.getUserName() + "】【" + teacherCompetitionHeaders.get(6) + "】未填写，请补充");
        }
        return "";
    }

    // 工程实践赛,ICT基础通识赛 读取数据
    private List<Team> getTeamFromRow(Sheet sheet) throws Exception{
        List<Team> teams = new ArrayList<>();
        int lineNum = 0;
        checkExcel(sheet);
        // 存放列的序号和field
        // 定义一个map用于存放excel列的序号和field.
        Map<Integer, String> cellMap = new HashMap<>();
        // 获取表头
        Row heard = sheet.getRow(1);
        int rowNotEmptyCellNum = getRowNotEmptyCellNum(heard);
//        int lastCellNum = heard.getLastCellNum();
        if(rowNotEmptyCellNum != 21){
            throw new GlobalException("请勿删除其他sheet页或本表首行、表头等信息");
        }
        for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
            Cell cell = heard.getCell(i);
            if (StringUtils.isNotNull(cell)) {
                String value = this.getCellValueAsString(cell);
                cellMap.put(i,value);
            }
        }
        for (Row row : sheet) {
            if (isRowEmpty(row)) {
                continue;
            }
            if (row.getRowNum() == 0) continue;
            // 跳过标题行
            if (row.getRowNum() == 1) continue; // 跳过标题行

            // 获取列名称
            Team team = new Team();
            // 参赛组别
            String secondLevelName = getCellValueAsString(row.getCell(0));
            if(StringUtils.isBlank(secondLevelName)){
                throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(0) + "】未填写，请补充");
            }
            team.setSecondLevelName(secondLevelName);
            team.setCompetitionTrackName(sheet.getSheetName());
            team.setTeamName("DS_"+secondLevelName+"_"+RandomUtil.randomNumbers(6));
//            String teamCode = "TD_" + RandomUtil.randomNumbers(6);
//            team.setTeamCode(teamCode);
            // 从第二列开始读取选手信息
            // 工程实践赛,ICT基础通识赛 只能读取两个选手
            int maxPlayerCount = 1;
            int excelRowNum = 0;
            for (int i = 1; i < row.getLastCellNum(); i += 7) {
                if (maxPlayerCount > 2) break;
                Player player = new Player();
                String userName = getCellValueAsString(row.getCell(i));
                String idCard = getCellValueAsString(row.getCell(i + 1));
                String phone = getCellValueAsString(row.getCell(i + 2));
                String mail = getCellValueAsString(row.getCell(i + 3));
                String sex = getCellValueAsString(row.getCell(i + 4));
                String classInfo = getCellValueAsString(row.getCell(i + 5));
                String profession = getCellValueAsString(row.getCell(i + 6));
                player.setUserName(userName);
                player.setIdCard(idCard);
                player.setPhone(phone);
                player.setEmail(mail);
                player.setSex(sex);
                player.setClassInfo(classInfo);
                player.setProfession(profession);
                player.setCompetitionRoleName("队员");
                player.setTeamSort(maxPlayerCount);
                // 数据校验
                String message = getCheckMessage(player,sheet.getSheetName(),lineNum,i,cellMap);
                player.setMessage(message);
                team.addPlayer(player);
                excelRowNum = i+7;
                maxPlayerCount++;
            }
            // 读取指导教师信息
            int teacherLineNum = 0;
            for (int i = excelRowNum; i < row.getLastCellNum(); i += 3) {
                String teacherName = getCellValueAsString(row.getCell(i));
                String teacherPhone = getCellValueAsString(row.getCell(i + 1));
                String teacherEmail = getCellValueAsString(row.getCell(i + 2));
                if(teacherLineNum == 0 && StringUtils.isBlank(teacherName)){
                    throw new GlobalException("【第" + (lineNum+3) + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(i) + "】未填写，请补充");
                }
                if(StringUtils.isAllBlank(teacherName,teacherPhone,teacherEmail)){
                    continue;
                }
                GuideTeacher guideTeacher = new GuideTeacher();
                guideTeacher.setGuideTeacherName(teacherName);
                guideTeacher.setGuideTeacherPhone(teacherPhone);
                guideTeacher.setGuideTeacherEmail(teacherEmail);
                guideTeacher.setUserName(teacherName);
                guideTeacher.setPhone(teacherPhone);
                guideTeacher.setEmail(teacherEmail);
                guideTeacher.setCompetitionRoleName("指导教师");
                guideTeacher.setTeamSort(teacherLineNum+1);
                getTeacherCheckMessage(guideTeacher, sheet.getSheetName(), lineNum, i, cellMap);
                team.addGuideTeacher(guideTeacher);
                teacherLineNum++;
            }
            teams.add(team);
            lineNum++;
        }
        return teams;
    }

    // 产教融合5G+创新应用赛获取单元格值
    private List<Team> getTeamFromRow(Sheet sheet,int startRowNum) throws Exception{
        List<Team> teams = new ArrayList<>();
        int lineNum = 0;
        checkExcel(sheet);
        Map<Integer, String> cellMap = new HashMap<>();
        // 获取表头
        Row heard = sheet.getRow(1);
        int rowNotEmptyCellNum = getRowNotEmptyCellNum(heard);
        if(rowNotEmptyCellNum != 50){
            throw new GlobalException("请勿删除其他sheet页或本表首行、表头等信息");
        }
        for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
            Cell cell = heard.getCell(i);
            if (StringUtils.isNotNull(cell)) {
                String value = this.getCellValueAsString(cell);
                cellMap.put(i,value);
            }
        }
        for (Row row : sheet) {
            if (isRowEmpty(row)) {
                continue;
            }
            if (row.getRowNum() == 0) continue; // 跳过标题行
            if (row.getRowNum() == 1) continue; // 跳过标题行
            Team team = new Team();
            // 参赛组别
            String secondLevelName = getCellValueAsString(row.getCell(0));
            if(StringUtils.isBlank(secondLevelName)){
                throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(0) + "】未填写，请补充");
            }
            team.setSecondLevelName(secondLevelName);
            String teamName = getCellValueAsString(row.getCell(1));
            team.setTeamName(teamName);
            if(StringUtils.isBlank(teamName)){
                throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(1) + "】未填写，请补充");
            }
            team.setCompetitionTrackName(sheet.getSheetName());
//            team.setTeamName("DS_"+secondLevelName+"_"+lineNum);
//            String teamCode = "TD_" + RandomUtil.randomNumbers(6);
//            team.setTeamCode(teamCode);
            // 从第二列开始读取选手信息
            // 产教融合5G+创新应用赛
            int maxPlayerCount = 1;
            int excelRowNum = 0;
            for (int i = 2; i < row.getLastCellNum(); i += startRowNum) {
                if (maxPlayerCount > 6) break;
                Player player = new Player();
                String userName = getCellValueAsString(row.getCell(i));
                String idCard = getCellValueAsString(row.getCell(i + 1));
                String phone = getCellValueAsString(row.getCell(i + 2));
                String mail = getCellValueAsString(row.getCell(i + 3));
                String sex = getCellValueAsString(row.getCell(i + 4));
                String classInfo = getCellValueAsString(row.getCell(i + 5));
                String profession = getCellValueAsString(row.getCell(i + 6));
                if (maxPlayerCount > 4 && StringUtils.isAllBlank(userName, idCard, phone, mail, sex, classInfo, profession)) {
                    excelRowNum = i + startRowNum;
                    maxPlayerCount++;
                    continue;
                }
                player.setUserName(userName);
                player.setIdCard(idCard);
                player.setPhone(phone);
                player.setEmail(mail);
                player.setSex(sex);
                player.setClassInfo(classInfo);
                player.setProfession(profession);
                player.setTeamSort(maxPlayerCount);
                // 数据校验
                String message = getCheckMessage(player,sheet.getSheetName(),lineNum,i,cellMap);
                player.setMessage(message);
                if(maxPlayerCount == 1){
                    player.setCompetitionRoleName("队长");
                } else {
                    player.setCompetitionRoleName("队员");
                }
                team.addPlayer(player);
                excelRowNum = i + startRowNum;
                maxPlayerCount++;
            }
            // 读取指导教师信息
            int teacherLineNum = 0;
            for (int i = excelRowNum; i < row.getLastCellNum(); i += 3) {
                GuideTeacher guideTeacher = new GuideTeacher();
                String teacherName = getCellValueAsString(row.getCell(i));
                String teacherPhone = getCellValueAsString(row.getCell(i + 1));
                String teacherEmail = getCellValueAsString(row.getCell(i + 2));
                if(teacherLineNum == 0 && StringUtils.isBlank(teacherName)){
                    throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(i) + "】未填写，请补充");
                }
                if(StringUtils.isAllBlank(teacherName,teacherPhone,teacherEmail)){
                    continue;
                }
                guideTeacher.setGuideTeacherName(teacherName);
                guideTeacher.setGuideTeacherPhone(teacherPhone);
                guideTeacher.setGuideTeacherEmail(teacherEmail);
                guideTeacher.setCompetitionRoleName("指导教师");
                guideTeacher.setUserName(teacherName);
                guideTeacher.setPhone(teacherPhone);
                guideTeacher.setEmail(teacherEmail);
                guideTeacher.setTeamSort(teacherLineNum +1);
                getTeacherCheckMessage(guideTeacher, sheet.getSheetName(), lineNum, i, cellMap);
                team.addGuideTeacher(guideTeacher);
                teacherLineNum++;
            }
            teams.add(team);
            lineNum++;
        }
        return teams;
    }

    // 留学生组
    private List<Team> getNationTeamFromRow(Sheet sheet,int startRowNum) throws Exception{
        List<Team> teams = new ArrayList<>();
        int lineNum = 0;
        checkExcel(sheet);
        Map<Integer, String> cellMap = new HashMap<>();
        // 获取表头
        Row heard = sheet.getRow(1);
        int rowNotEmptyCellNum = getRowNotEmptyCellNum(heard);
        if(rowNotEmptyCellNum != 62){
            throw new GlobalException("请勿删除其他sheet页或本表首行、表头等信息");
        }
        for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
            Cell cell = heard.getCell(i);
            if (StringUtils.isNotNull(cell)) {
                String value = this.getCellValueAsString(cell);
                cellMap.put(i,value);
            }
        }
        for (Row row : sheet) {
            if (isRowEmpty(row)) {
                continue;
            }
            if (row.getRowNum() == 0) continue; // 跳过标题行
            if (row.getRowNum() == 1) continue; // 跳过标题行
            Team team = new Team();
            // 参赛组别
            String secondLevelName = getCellValueAsString(row.getCell(0));
            if(StringUtils.isBlank(secondLevelName)){
                throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(0) + "】未填写，请补充");
            }
            team.setSecondLevelName(secondLevelName);
            String teamName = getCellValueAsString(row.getCell(1));
            team.setTeamName(teamName);
            if(StringUtils.isBlank(teamName)){
                throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(1) + "】未填写，请补充");
            }
            team.setCompetitionTrackName(sheet.getSheetName());
//            team.setTeamName("DS_"+secondLevelName+"_"+lineNum);
//            String teamCode = "TD_" + RandomUtil.randomNumbers(6);
//            team.setTeamCode(teamCode);
            // 从第二列开始读取选手信息
            // 工程实践赛,ICT基础通识赛 只能读取两个选手
            int maxPlayerCount = 1;
            int excelRowNum = 0;
            for (int i = 2; i < row.getLastCellNum(); i += startRowNum) {
                if (maxPlayerCount > 6) break;
                Player player = new Player();
                String userName = getCellValueAsString(row.getCell(i));
                String idCard = getCellValueAsString(row.getCell(i + 1));
                String phone = getCellValueAsString(row.getCell(i + 2));
                String mail = getCellValueAsString(row.getCell(i + 3));
                String sex = getCellValueAsString(row.getCell(i + 4));
                String nationalityName = getCellValueAsString(row.getCell(i + 5));
                String departmentName = getCellValueAsString(row.getCell(i + 6));
                String classInfo = getCellValueAsString(row.getCell(i + 7));
                String profession = getCellValueAsString(row.getCell(i + 8));
                if (maxPlayerCount > 4 && StringUtils.isAllBlank(userName, idCard, phone, mail, sex, classInfo, profession)) {
                    excelRowNum = i + startRowNum;
                    maxPlayerCount++;
                    continue;
                }
                player.setUserName(userName);
                player.setIdCard(idCard);
                player.setPhone(phone);
                player.setEmail(mail);
                player.setSex(sex);
                player.setNationalityName(nationalityName);
                player.setDepartmentName(departmentName);
                player.setClassInfo(classInfo);
                player.setProfession(profession);
                player.setTeamSort(maxPlayerCount);
                // 数据校验
                String message = getCheckMessage(player,sheet.getSheetName(),lineNum,i,cellMap);
                player.setMessage(message);
                if(maxPlayerCount == 0){
                    player.setCompetitionRoleName("队长");
                } else {
                    player.setCompetitionRoleName("队员");
                }
                team.addPlayer(player);
                excelRowNum = i + startRowNum;
                maxPlayerCount++;
            }
            // 读取指导教师信息
            int teacherLineNum = 0;
            for (int i = excelRowNum; i < row.getLastCellNum(); i += 3) {
                GuideTeacher guideTeacher = new GuideTeacher();
                String teacherName = getCellValueAsString(row.getCell(i));
                String teacherPhone = getCellValueAsString(row.getCell(i + 1));
                String teacherEmail = getCellValueAsString(row.getCell(i + 2));
                if(teacherLineNum == 0 && StringUtils.isBlank(teacherName)){
                    throw new GlobalException("【第" + (lineNum+3)  + "行】【" + sheet.getSheetName() + "】中【" + cellMap.get(i) + "】未填写，请补充");
                }
                if(StringUtils.isAllBlank(teacherName,teacherPhone,teacherEmail)){
                    continue;
                }
                guideTeacher.setGuideTeacherName(teacherName);
                guideTeacher.setGuideTeacherPhone(teacherPhone);
                guideTeacher.setGuideTeacherEmail(teacherEmail);
                guideTeacher.setCompetitionRoleName("指导教师");
                guideTeacher.setUserName(teacherName);
                guideTeacher.setPhone(teacherPhone);
                guideTeacher.setEmail(teacherEmail);
                guideTeacher.setTeamSort(teacherLineNum+1);
                getTeacherCheckMessage(guideTeacher, sheet.getSheetName(), lineNum, i, cellMap);
                team.addGuideTeacher(guideTeacher);
                teacherLineNum++;
            }
            teams.add(team);
            lineNum++;
        }
        return teams;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString().trim();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue()).trim();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                return cell.getCellFormula().trim();
            default:
                return null;
        }
    }

    // 获取列不为空值
    public int getRowNotEmptyCellNum(Row heard) {
        Set<Integer> nonEmptyColumns = new HashSet<>(); // 用于存储非空列的索引
        for (Cell cell : heard) { // 遍历行中的所有单元格
            if (cell != null && cell.getCellType() != CellType.BLANK && cell.getCellType() != CellType.ERROR) { // 如果单元格非空且不是错误类型
                nonEmptyColumns.add(cell.getColumnIndex()); // 添加列索引到集合中
            }
        }
        return nonEmptyColumns.size();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            switch (cell.getCellType()) {
                case STRING:
                    if (!cell.getStringCellValue().trim().isEmpty()) {
                        return false;
                    }
                    break;
                case NUMERIC:
                case BOOLEAN:
                case FORMULA:
                case ERROR:
                    return false;
                case BLANK:
                case _NONE:
                default:
                    break;
            }
        }
        return true;
    }

    public void checkExcel(Sheet sheet) throws Exception{
        Row heard0 = sheet.getRow(0);
        Cell headerCell = heard0.getCell(0);
        if (StringUtils.isNotNull(headerCell)) {
            String value = this.getCellValueAsString(headerCell);
            if (StringUtils.isEmpty(value) || !value.contains("报名注意事项")) {
                throw new GlobalException("请勿修改或删除excel报名模板首行内容,如已修改或删除，请还原或重新下载模板");
            }
        }
    }

    // 团队校验
    public String getCheckMessage(Player player, String sheetName, int excelLineZero,
                                int rowNum, Map<Integer, String> cellMap) throws GlobalException {
        String message = "";
        int excelLine = excelLineZero + 3;
        if (StringUtils.isBlank(player.getUserName())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + cellMap.get(rowNum) + "】未填写，请补充");
        }
        if (StringUtils.isBlank(player.getIdCard())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 1) + "】未填写，请补充");
        }
        // 调用实名认证校验 身份证号位数小于15位不进行实名认证
        if(player.getIdCard().trim().length()>=15  && (StringUtils.isEmpty(player.getNationalityName()) || nationalityName.equals(player.getNationalityName()))){
            player.setIdCardType("1");
            RemoteUserService remoteUserService = SpringUtils.getBean(RemoteUserService.class);
            AuthInfo authInfo = new AuthInfo();
            authInfo.setRealName(player.getUserName());
            authInfo.setIdCard(player.getIdCard());
            R<Map<String, Object>> authenticationMapR = remoteUserService.saveInnerAuthInfo(authInfo, SecurityConstants.INNER);
            if (R.isSuccess(authenticationMapR)) {
                if (!Boolean.parseBoolean(String.valueOf(authenticationMapR.getData().get("isok")))) {
                    throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】身份认证未通过，请检查姓名和身份证号是否匹配");
                } else {
                    // 如果excel输入性别与二元数据校验不一致,系统自动修复
                    Map<String, Object> authenticationMap = authenticationMapR.getData();
                    if (Objects.nonNull(authenticationMap.get("IdCardInfor"))) {
                        Map idCardInfor = (Map) authenticationMap.get("IdCardInfor");
                        player.setSex(idCardInfor.get("sex").toString());
                    }
                }
            } else {
                throw new GlobalException("身份认证服务调用失败");
            }
        } else {
            // 身份证位数小于18位不进行实名认证，记录身份认证类型港澳台胞港/澳/台胞证号，外籍学生为护照号
            message = "姓名"+player.getUserName()+"的证件号码"+player.getIdCard()+"可能不是身份证，请确认是否有误。点击“报名缴费”按钮则意味着您已确认上述信息";
            player.setIdCardType("2");
        }
        if (StringUtils.isBlank(player.getPhone())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 2) + "】未填写，请补充");
        }
        if (!player.getPhone().matches(phoneRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 2) + "】格式有误，请正确填写");
        }
        if (StringUtils.isBlank(player.getEmail())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 3) + "】未填写，请补充");
        }
        if (!player.getEmail().matches(emailRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 3) + "】格式有误，请正确填写");
        }
        if (StringUtils.isBlank(player.getSex())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 4) + "】未填写，请补充");
        }
        if (!sheetName.equals(CompetitionTrackNameFour)) {
            if (StringUtils.isBlank(player.getClassInfo())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 5) + "】未填写，请补充");
            }
            if (StringUtils.isBlank(player.getProfession())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 6) + "】未填写，请补充");
            }
        }
        if (sheetName.equals(CompetitionTrackNameFour)) {
            if (StringUtils.isBlank(player.getNationalityName())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 5) + "】未填写，请补充");
            }
            if (StringUtils.isBlank(player.getDepartmentName())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 6) + "】未填写，请补充");
            }
            if (StringUtils.isBlank(player.getClassInfo())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 7) + "】未填写，请补充");
            }
            if (StringUtils.isBlank(player.getProfession())) {
                throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + player.getUserName() + "】【" + cellMap.get(rowNum + 8) + "】未填写，请补充");
            }
        }
        return message;
    }

    //指导教师校验
    public void getTeacherCheckMessage(GuideTeacher guideTeacher, String sheetName,int excelLineZero,
                                int rowNum,Map<Integer, String> cellMap) throws GlobalException{
        int excelLine = excelLineZero + 3;
        if (StringUtils.isBlank(guideTeacher.getUserName())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + cellMap.get(rowNum) + "】未填写，请补充");
        }
        //所有指导教师 不验证手机号和邮箱
        /*if (StringUtils.isBlank(guideTeacher.getPhone())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + guideTeacher.getUserName() + "】【" + cellMap.get(rowNum + 1) + "】未填写，请补充");
        }
        if (!guideTeacher.getPhone().matches(phoneRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + guideTeacher.getUserName() + "】【" + cellMap.get(rowNum + 1) + "】格式有误，请正确填写");
        }
        if (StringUtils.isBlank(guideTeacher.getEmail())) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + guideTeacher.getUserName() + "】【" + cellMap.get(rowNum + 2) + "】未填写，请补充");
        }
        if (!guideTeacher.getEmail().matches(emailRegex)) {
            throw new GlobalException("【第" + excelLine + "行】【" + sheetName + "】中【" + guideTeacher.getUserName() + "】【" + cellMap.get(rowNum + 2) + "】格式有误，请正确填写");
        }*/
    }
}
