package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.domain.CertificateImportCompetitionInfo;
import com.teaching.competition.domain.CertificateImportExcelRow;
import com.teaching.competition.domain.CertificateImportPreviewResult;
import com.teaching.competition.domain.CertificateImportRequest;
import com.teaching.competition.domain.CertificateImportSqlResult;
import com.teaching.competition.domain.CertificateImportType;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.service.ICertificateImportService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 获奖证书 Excel 校验和双表导入 SQL 生成。
 */
@Service
public class CertificateImportServiceImpl implements ICertificateImportService {
    private static final long LEGACY_COMPETITION_SERIES_ID = 77L;
    private static final long LEGACY_COMPETITION_ID = 79L;
    private static final String LEGACY_COMPETITION_NAME = "大学生新一代信息通信科技大赛";
    private static final String LEGACY_ISSUANCE_DATE = "2026-06-09";
    static final String ORG_CODE = "258452";
    static final String CERT_URL = "https://cx.miitec.cn/certificateSearch?type=4";

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final int MAX_IMPORT_ROWS = 50_000;
    private static final int MAX_ERROR_MESSAGES = 30;
    private static final int SQL_VALUE_CHUNK_SIZE = 300;
    private static final String GUIDE_TEACHER_ROLE = "指导教师";

    private static final List<String> STUDENT_COMMON_REQUIRED_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位", "参赛队员", "指导教师"
    ));
    private static final List<String> TEACHER_REQUIRED_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "老师姓名", "届数", "大赛名称", "赛区", "奖项", "证书编号", "参赛单位"
    ));
    private static final List<String> ORGANIZATION_REQUIRED_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "届数", "大赛名称", "称号", "证书编号", "参赛单位"
    ));
    private static final Pattern TEACHER_TRACK_PATTERN = Pattern.compile("^(.+?赛道)(?=一等奖|二等奖|三等奖|优胜奖)");

    private static final Map<String, String> AWARD_CODE_MAP;

    static {
        Map<String, String> awards = new LinkedHashMap<>();
        awards.put("一等奖", "1");
        awards.put("二等奖", "2");
        awards.put("三等奖", "3");
        awards.put("优胜奖", "4");
        AWARD_CODE_MAP = Collections.unmodifiableMap(awards);
    }

    private final CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    public CertificateImportServiceImpl(CompetitionApplyInfoMapper competitionApplyInfoMapper) {
        this.competitionApplyInfoMapper = competitionApplyInfoMapper;
    }

    @Override
    public CertificateImportSqlResult generateImportSql(MultipartFile file) {
        ImportContext legacyContext = new ImportContext(
                LEGACY_COMPETITION_SERIES_ID, LEGACY_COMPETITION_ID, LEGACY_COMPETITION_NAME,
                CertificateImportType.STUDENT_PERSONAL, LocalDate.parse(LEGACY_ISSUANCE_DATE));
        PreparedImport prepared = prepareImport(file, legacyContext, true);
        String sqlContent = buildSql(file.getOriginalFilename(), prepared.matchedRows,
                prepared.matchWarnings, legacyContext);
        return new CertificateImportSqlResult(
                buildSqlFileName(file.getOriginalFilename()), sqlContent, prepared.matchedRows.size(),
                prepared.originRowCount(), prepared.matchWarnings.size());
    }

    @Override
    public CertificateImportPreviewResult previewImport(MultipartFile file, CertificateImportRequest request) {
        ImportContext context = resolveImportContext(request);
        PreparedImport prepared = prepareImport(file, context, false);
        return new CertificateImportPreviewResult(prepared.matchedRows.size(), prepared.originRowCount(),
                prepared.matchWarnings.size(), prepared.matchWarnings);
    }

    @Override
    public CertificateImportSqlResult generateImportSql(MultipartFile file, CertificateImportRequest request) {
        ImportContext context = resolveImportContext(request);
        PreparedImport prepared = prepareImport(file, context, false);
        String sqlContent = buildSql(file.getOriginalFilename(), prepared.matchedRows,
                prepared.matchWarnings, context);
        return new CertificateImportSqlResult(
                buildSqlFileName(file.getOriginalFilename()), sqlContent, prepared.matchedRows.size(),
                prepared.originRowCount(), prepared.matchWarnings.size());
    }

    private PreparedImport prepareImport(MultipartFile file, ImportContext context, boolean legacyAutoDetect) {
        validateFile(file);
        List<CertificateImportExcelRow> excelRows = legacyAutoDetect
                ? parseExcel(file) : parseExcel(file, context.certificateType);
        List<String> errors = new ArrayList<>();
        validateExcelRows(excelRows, errors);
        throwIfInvalid(errors);

        List<CompetitionApplyInfo> applicants = legacyAutoDetect
                ? competitionApplyInfoMapper.selectCompetitionApplyInfoListByCompetitionSeriesId(context.competitionSeriesId)
                : competitionApplyInfoMapper.selectCertificateImportApplicants(context.competitionSeriesId);
        applyUserIdFallback(applicants);
        List<String> matchWarnings = new ArrayList<>();
        List<MatchedCertificateRow> matchedRows = matchApplicants(excelRows, applicants, matchWarnings, context);
        return new PreparedImport(matchedRows, matchWarnings);
    }

    private ImportContext resolveImportContext(CertificateImportRequest request) {
        if (request == null || request.getCompetitionSeriesId() == null) {
            throw new GlobalException("请选择赛事系列");
        }
        CertificateImportType certificateType = CertificateImportType.fromCode(request.getCertificateType());
        if (certificateType == null) {
            throw new GlobalException("请选择正确的证书类型");
        }
        LocalDate issuanceDate;
        try {
            issuanceDate = LocalDate.parse(Objects.toString(request.getIssuanceDate(), ""),
                    DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            throw new GlobalException("发证日期格式不正确，请使用yyyy-MM-dd");
        }
        CertificateImportCompetitionInfo competitionInfo = competitionApplyInfoMapper
                .selectCertificateImportCompetitionInfo(request.getCompetitionSeriesId());
        if (competitionInfo == null || competitionInfo.getCompetitionSeriesId() == null
                || competitionInfo.getCompetitionId() == null
                || isBlank(competitionInfo.getCompetitionName())) {
            throw new GlobalException("所选赛事系列不存在或所属大赛已删除");
        }
        return new ImportContext(competitionInfo.getCompetitionSeriesId(), competitionInfo.getCompetitionId(),
                competitionInfo.getCompetitionName(), certificateType, issuanceDate);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException("请上传获奖证书 Excel 文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GlobalException("Excel 文件不能超过20MB");
        }
        String fileName = Objects.toString(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            throw new GlobalException("仅支持xlsx或xls格式文件");
        }
    }

    List<CertificateImportExcelRow> parseExcel(MultipartFile file) {
        return parseExcelInternal(file, null);
    }

    List<CertificateImportExcelRow> parseExcel(MultipartFile file, CertificateImportType certificateType) {
        return parseExcelInternal(file, certificateType);
    }

    private List<CertificateImportExcelRow> parseExcelInternal(
            MultipartFile file, CertificateImportType requestedType) {
        List<CertificateImportExcelRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (!hasContent(sheet, formatter, evaluator)) {
                    continue;
                }
                HeaderInfo headerInfo = findHeader(sheet, requestedType, formatter, evaluator);
                if (headerInfo == null) {
                    addError(errors, "工作表【" + sheet.getSheetName() + "】前10行内未找到"
                            + (requestedType == null ? "证书导入" : requestedType.getDescription()) + "表头");
                    continue;
                }
                List<String> requiredHeaders = requiredHeaders(headerInfo.certificateType);
                List<String> missingHeaders = requiredHeaders.stream()
                        .filter(header -> !headerInfo.columns.containsKey(header))
                        .toList();
                if (headerInfo.certificateType == CertificateImportType.STUDENT_TEAM
                        && !headerInfo.columns.containsKey("团队名称")
                        && !headerInfo.columns.containsKey("参赛姓名")) {
                    missingHeaders = new ArrayList<>(missingHeaders);
                    missingHeaders.add("团队名称（也支持参赛姓名）");
                }
                if (!missingHeaders.isEmpty()) {
                    addError(errors, "工作表【" + sheet.getSheetName() + "】缺少表头：" + String.join("、", missingHeaders));
                    continue;
                }
                for (int rowIndex = headerInfo.rowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    CertificateImportExcelRow excelRow = readExcelRow(sheet, row, rowIndex,
                            headerInfo, formatter, evaluator);
                    if (excelRow == null) {
                        continue;
                    }
                    rows.add(excelRow);
                    if (rows.size() > MAX_IMPORT_ROWS) {
                        throw new GlobalException("单次最多处理" + MAX_IMPORT_ROWS + "条证书数据");
                    }
                }
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("Excel 文件读取失败，请确认文件未损坏或加密：" + safeMessage(e));
        }
        throwIfInvalid(errors);
        if (rows.isEmpty()) {
            throw new GlobalException("Excel 中未读取到证书数据");
        }
        return rows;
    }

    private List<String> requiredHeaders(CertificateImportType certificateType) {
        if (certificateType == CertificateImportType.TEACHER_HONOR) {
            return new ArrayList<>(TEACHER_REQUIRED_HEADERS);
        }
        if (certificateType == CertificateImportType.ORGANIZATION_HONOR) {
            return new ArrayList<>(ORGANIZATION_REQUIRED_HEADERS);
        }
        List<String> headers = new ArrayList<>(STUDENT_COMMON_REQUIRED_HEADERS);
        if (certificateType == CertificateImportType.STUDENT_PERSONAL) {
            headers.add("参赛姓名");
        }
        return headers;
    }

    private HeaderInfo findHeader(Sheet sheet, CertificateImportType requestedType,
                                  DataFormatter formatter, FormulaEvaluator evaluator) {
        int maxHeaderRow = Math.min(sheet.getLastRowNum(), 9);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= maxHeaderRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> columns = new LinkedHashMap<>();
            for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                String value = cleanCellValue(getCellValue(row.getCell(columnIndex), formatter, evaluator));
                if (!value.isEmpty()) {
                    columns.putIfAbsent(value, columnIndex);
                }
            }
            if (!columns.containsKey("证书编号")) {
                continue;
            }
            CertificateImportType actualType = requestedType;
            if (actualType == null) {
                actualType = columns.containsKey("团队名称")
                        ? CertificateImportType.STUDENT_TEAM : CertificateImportType.STUDENT_PERSONAL;
            }
            boolean markerPresent = switch (actualType) {
                case STUDENT_PERSONAL -> columns.containsKey("参赛姓名");
                case STUDENT_TEAM -> columns.containsKey("团队名称") || columns.containsKey("参赛姓名");
                case TEACHER_HONOR -> columns.containsKey("老师姓名");
                case ORGANIZATION_HONOR -> columns.containsKey("称号");
            };
            if (markerPresent) {
                return new HeaderInfo(rowIndex, columns, actualType);
            }
        }
        return null;
    }

    private CertificateImportExcelRow readExcelRow(Sheet sheet, Row row, int rowIndex, HeaderInfo headerInfo,
                                                    DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return null;
        }
        Map<String, Integer> columns = headerInfo.columns;
        CertificateImportType certificateType = headerInfo.certificateType;
        CertificateImportExcelRow result = new CertificateImportExcelRow();
        result.setSheetName(sheet.getSheetName());
        result.setRowNumber(rowIndex + 1);
        result.setCertificateType(certificateType);
        result.setTeamCertificate(certificateType == CertificateImportType.STUDENT_TEAM);
        if (certificateType == CertificateImportType.STUDENT_PERSONAL) {
            result.setUserName(readColumn(row, columns, "参赛姓名", formatter, evaluator));
        } else if (certificateType == CertificateImportType.STUDENT_TEAM) {
            result.setTeamName(firstNonBlank(
                    readColumn(row, columns, "团队名称", formatter, evaluator),
                    readColumn(row, columns, "参赛姓名", formatter, evaluator)));
        } else if (certificateType == CertificateImportType.TEACHER_HONOR) {
            result.setUserName(readColumn(row, columns, "老师姓名", formatter, evaluator));
        }
        result.setCompetitionEdition(readColumn(row, columns, "届数", formatter, evaluator));
        result.setCompetitionName(readColumn(row, columns, "大赛名称", formatter, evaluator));
        result.setCompetitionRegion(readColumn(row, columns, "赛区", formatter, evaluator));
        result.setAwardsName(firstNonBlank(
                readColumn(row, columns, "奖项", formatter, evaluator),
                readColumn(row, columns, "称号", formatter, evaluator)));
        result.setCertCode(readColumn(row, columns, "证书编号", formatter, evaluator));
        result.setSchoolName(readColumn(row, columns, "参赛单位", formatter, evaluator));
        if (certificateType == CertificateImportType.TEACHER_HONOR) {
            result.setCompetitionTrackName(extractTeacherTrack(result.getAwardsName()));
            result.setPlayer(result.getUserName());
            result.setGuideTeacher("");
        } else if (certificateType == CertificateImportType.ORGANIZATION_HONOR) {
            result.setUserName(result.getSchoolName());
            result.setPlayer(result.getSchoolName());
            result.setGuideTeacher("");
        } else {
            result.setCompetitionTrackName(firstNonBlank(
                    readColumn(row, columns, "赛道", formatter, evaluator),
                    extractTrackFromCompetitionName(result.getCompetitionName())));
            result.setPlayer(readColumn(row, columns, "参赛队员", formatter, evaluator));
            result.setGuideTeacher(readColumn(row, columns, "指导教师", formatter, evaluator));
        }
        if (isImportRowEmpty(result)) {
            return null;
        }
        return result;
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? cleanCellValue(second) : cleanCellValue(first);
    }

    private String readColumn(Row row, Map<String, Integer> columns, String header,
                              DataFormatter formatter, FormulaEvaluator evaluator) {
        Integer columnIndex = columns.get(header);
        if (columnIndex == null) {
            return "";
        }
        return cleanCellValue(getCellValue(row.getCell(columnIndex), formatter, evaluator));
    }

    private String getCellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell, evaluator);
    }

    private boolean hasContent(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int lastRow = Math.min(sheet.getLastRowNum(), 20);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                if (!cleanCellValue(getCellValue(row.getCell(columnIndex), formatter, evaluator)).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateExcelRows(List<CertificateImportExcelRow> rows, List<String> errors) {
        Map<String, List<CertificateImportExcelRow>> certCodeRows = rows.stream()
                .filter(row -> !isBlank(row.getCertCode()))
                .collect(Collectors.groupingBy(row -> normalizeText(row.getCertCode()), LinkedHashMap::new, Collectors.toList()));

        for (CertificateImportExcelRow row : rows) {
            CertificateImportType certificateType = certificateTypeOf(row);
            switch (certificateType) {
                case STUDENT_PERSONAL -> {
                    validateRequired(row, "参赛姓名", row.getUserName(), errors);
                    validateRequired(row, "参赛队员", row.getPlayer(), errors);
                }
                case STUDENT_TEAM -> {
                    validateRequired(row, "团队名称", row.getTeamName(), errors);
                    validateRequired(row, "参赛队员", row.getPlayer(), errors);
                }
                case TEACHER_HONOR -> {
                    validateRequired(row, "老师姓名", row.getUserName(), errors);
                    validateRequired(row, "奖项中的赛道", row.getCompetitionTrackName(), errors);
                }
                case ORGANIZATION_HONOR -> {
                    // 优秀组织单位没有赛区、参赛队员和指导教师列。
                }
            }
            validateRequired(row, "届数", row.getCompetitionEdition(), errors);
            validateRequired(row, "大赛名称", row.getCompetitionName(), errors);
            if (certificateType != CertificateImportType.ORGANIZATION_HONOR) {
                validateRequired(row, "赛区", row.getCompetitionRegion(), errors);
            }
            validateRequired(row, certificateType == CertificateImportType.ORGANIZATION_HONOR ? "称号" : "奖项",
                    row.getAwardsName(), errors);
            validateRequired(row, "证书编号", row.getCertCode(), errors);
            validateRequired(row, "参赛单位", row.getSchoolName(), errors);

            validateLength(row, "参赛姓名", row.getUserName(), 100, errors);
            validateLength(row, "团队名称", row.getTeamName(), 100, errors);
            validateLength(row, "届数", row.getCompetitionEdition(), 100, errors);
            validateLength(row, "大赛名称", row.getCompetitionName(), 160, errors);
            validateLength(row, "赛道", row.getCompetitionTrackName(), 100, errors);
            validateLength(row, "赛区", row.getCompetitionRegion(), 100, errors);
            validateLength(row, "奖项", row.getAwardsName(), 100, errors);
            validateLength(row, "证书编号", row.getCertCode(), 255, errors);
            validateLength(row, "参赛单位", row.getSchoolName(), 100, errors);
            validateLength(row, "参赛队员", row.getPlayer(), 100, errors);
            validateLength(row, "指导教师", row.getGuideTeacher(), 100, errors);

            if ((certificateType == CertificateImportType.STUDENT_PERSONAL
                    || certificateType == CertificateImportType.STUDENT_TEAM)
                    && !isBlank(row.getAwardsName()) && awardCode(row.getAwardsName()) == null) {
                addRowError(errors, row, "奖项【" + row.getAwardsName()
                        + "】不在一等奖、二等奖、三等奖、优胜奖范围内");
            }
            if (certificateType == CertificateImportType.STUDENT_PERSONAL
                    && !isBlank(row.getUserName()) && !isBlank(row.getPlayer())
                    && !containsPlayer(row.getPlayer(), row.getUserName())) {
                addRowError(errors, row, "参赛队员中未包含参赛姓名【" + row.getUserName() + "】");
            }
            String certName = buildCertificateName(row);
            if (certName.length() > 255) {
                addRowError(errors, row, "生成后的证书名称超过255个字符");
            }
        }

        certCodeRows.values().stream()
                .filter(duplicates -> duplicates.size() > 1)
                .forEach(duplicates -> addError(errors,
                        "证书编号【" + duplicates.get(0).getCertCode() + "】在Excel中重复，位置：" + duplicates.stream()
                                .map(this::rowLocation).collect(Collectors.joining("、"))));
    }

    private List<MatchedCertificateRow> matchApplicants(List<CertificateImportExcelRow> rows,
                                                         List<CompetitionApplyInfo> applicants,
                                                         List<String> warnings,
                                                         ImportContext context) {
        Map<String, List<CompetitionApplyInfo>> applicantMap = new HashMap<>();
        Map<String, List<CompetitionApplyInfo>> applicantNameMap = new HashMap<>();
        Map<String, List<CompetitionApplyInfo>> teacherNameMap = new HashMap<>();
        Map<String, Set<String>> teamPlayerMap = new HashMap<>();
        Map<String, List<CompetitionApplyInfo>> teamApplicantsMap = new LinkedHashMap<>();
        Map<String, Long> duplicateExcelRecipientCounts = rows.stream()
                .collect(Collectors.groupingBy(this::duplicateExcelRecipientKey, LinkedHashMap::new, Collectors.counting()));
        Map<String, Integer> duplicateApplicantOffsets = new HashMap<>();
        if (applicants != null) {
            for (CompetitionApplyInfo applicant : applicants) {
                if (GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName())) {
                    teacherNameMap.computeIfAbsent(normalizePersonName(applicant.getUserName()), ignored -> new ArrayList<>())
                            .add(applicant);
                    continue;
                }
                String key = applicantKey(applicant.getUserName(), applicant.getSchoolName());
                applicantMap.computeIfAbsent(key, ignored -> new ArrayList<>()).add(applicant);
                applicantNameMap.computeIfAbsent(normalizePersonName(applicant.getUserName()), ignored -> new ArrayList<>())
                        .add(applicant);
                if (!isBlank(applicant.getTeamCode()) && !isBlank(applicant.getUserName())) {
                    teamPlayerMap.computeIfAbsent(applicant.getTeamCode(), ignored -> new LinkedHashSet<>())
                            .add(normalizePersonName(applicant.getUserName()));
                    teamApplicantsMap.computeIfAbsent(applicant.getTeamCode(), ignored -> new ArrayList<>())
                            .add(applicant);
                }
            }
        }

        List<MatchedCertificateRow> matchedRows = new ArrayList<>();
        for (CertificateImportExcelRow row : rows) {
            CertificateImportType certificateType = certificateTypeOf(row);
            if (certificateType == CertificateImportType.ORGANIZATION_HONOR) {
                matchedRows.add(MatchedCertificateRow.forApplicants(row, Collections.emptyList(),
                        storedAwardValue(row), buildCertificateName(row)));
                continue;
            }
            if (certificateType == CertificateImportType.TEACHER_HONOR) {
                matchedRows.add(matchTeacherCertificate(row, teacherNameMap, warnings, context));
                continue;
            }
            if (certificateType == CertificateImportType.STUDENT_TEAM) {
                matchedRows.add(matchTeamCertificate(row, teamApplicantsMap, warnings, context));
                continue;
            }
            List<CompetitionApplyInfo> exactSchoolApplicants = applicantMap.getOrDefault(
                    applicantKey(row.getUserName(), row.getSchoolName()), Collections.emptyList());
            List<CompetitionApplyInfo> sameNameApplicants = applicantNameMap.getOrDefault(
                    normalizePersonName(row.getUserName()), Collections.emptyList());
            List<CompetitionApplyInfo> samePerson = exactSchoolApplicants.isEmpty()
                    ? sameNameApplicants.stream()
                    .filter(applicant -> schoolMatches(row.getSchoolName(), applicant.getSchoolName()))
                    .collect(Collectors.toList())
                    : exactSchoolApplicants;
            List<CompetitionApplyInfo> trackMatched = samePerson.stream()
                    .filter(applicant -> trackMatches(row.getCompetitionName(), applicant.getCompetitionTrackName()))
                    .filter(applicant -> !GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(this::applicantFingerprint, applicant -> applicant, (left, right) -> left, LinkedHashMap::new),
                            map -> new ArrayList<>(map.values())));
            List<CompetitionApplyInfo> sameNameTrackApplicants = sameNameApplicants.stream()
                    .filter(applicant -> trackMatches(row.getCompetitionName(), applicant.getCompetitionTrackName()))
                    .filter(applicant -> !GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(this::applicantFingerprint, applicant -> applicant, (left, right) -> left, LinkedHashMap::new),
                            map -> new ArrayList<>(map.values())));
            List<CompetitionApplyInfo> rosterMatches = findTeamPlayerMatches(
                    sameNameTrackApplicants, row.getPlayer(), teamPlayerMap);
            if (rosterMatches.size() == 1) {
                // 学校简称、曾用名或更名场景下，以完整团队成员唯一命中作为安全兜底。
                trackMatched = rosterMatches;
            } else {
                trackMatched = narrowByTeamPlayers(trackMatched, row.getPlayer(), teamPlayerMap);
            }
            trackMatched = resolveDuplicateNameApplicants(
                    row, trackMatched, duplicateExcelRecipientCounts, duplicateApplicantOffsets);

            if (trackMatched.isEmpty()) {
                if (sameNameApplicants.isEmpty()) {
                    addRowWarning(warnings, row, "在赛事" + context.competitionSeriesId
                            + "的已缴费报名数据中未匹配到参赛姓名，仅写入历史证书表");
                } else if (samePerson.isEmpty()) {
                    String schools = sameNameApplicants.stream().map(CompetitionApplyInfo::getSchoolName)
                            .filter(Objects::nonNull).distinct().collect(Collectors.joining("、"));
                    addRowWarning(warnings, row, "参赛单位与报名学校不一致，仅写入历史证书表；报名学校：" + schools);
                } else {
                    String tracks = samePerson.stream().map(CompetitionApplyInfo::getCompetitionTrackName)
                            .filter(Objects::nonNull).distinct().collect(Collectors.joining("、"));
                    addRowWarning(warnings, row, "Excel大赛名称中的赛道与报名赛道不一致，仅写入历史证书表；报名赛道：" + tracks);
                }
                matchedRows.add(MatchedCertificateRow.forApplicant(
                        row, null, storedAwardValue(row), buildCertificateName(row)));
                continue;
            }
            if (trackMatched.size() > 1) {
                List<String> teams = trackMatched.stream().map(CompetitionApplyInfo::getTeamCode)
                        .filter(Objects::nonNull).distinct().collect(Collectors.toList());
                if (teams.size() == 1) {
                    addRowWarning(warnings, row, "同一报名团队存在多个同名参赛人，Excel证书行数或队员名单重名次数无法一一对应，"
                            + "仅写入历史证书表；团队编号：" + teams.get(0));
                } else {
                    addRowWarning(warnings, row, "匹配到多个报名团队，无法确定用户证书归属，仅写入历史证书表；团队编号："
                            + String.join("、", teams));
                }
                matchedRows.add(MatchedCertificateRow.forApplicant(
                        row, null, storedAwardValue(row), buildCertificateName(row)));
                continue;
            }

            CompetitionApplyInfo applicant = trackMatched.get(0);
            if (applicant.getUserId() == null) {
                addRowWarning(warnings, row, "匹配到的报名数据缺少用户ID"
                        + "（未找到唯一的实名认证账号或报名手机号账号），仅写入历史证书表");
                matchedRows.add(MatchedCertificateRow.forApplicant(
                        row, null, storedAwardValue(row), buildCertificateName(row)));
                continue;
            }
            List<String> applicantErrors = new ArrayList<>();
            validateApplicantLengths(row, applicant, applicantErrors);
            if (!applicantErrors.isEmpty()) {
                addRowWarning(warnings, row, "报名关联字段超过证书表长度限制，仅写入历史证书表");
                matchedRows.add(MatchedCertificateRow.forApplicant(
                        row, null, storedAwardValue(row), buildCertificateName(row)));
            } else {
                matchedRows.add(MatchedCertificateRow.forApplicant(
                        row, applicant, storedAwardValue(row), buildCertificateName(row)));
            }
        }
        return matchedRows;
    }

    private MatchedCertificateRow matchTeacherCertificate(
            CertificateImportExcelRow row,
            Map<String, List<CompetitionApplyInfo>> teacherNameMap,
            List<String> warnings,
            ImportContext context) {
        List<CompetitionApplyInfo> sameNameApplicants = teacherNameMap.getOrDefault(
                normalizePersonName(row.getUserName()), Collections.emptyList());
        if (sameNameApplicants.isEmpty()) {
            addRowWarning(warnings, row, "在赛事" + context.competitionSeriesId
                    + "的已缴费指导教师报名数据中未匹配到老师姓名，仅写入历史证书表");
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }

        List<CompetitionApplyInfo> sameSchoolApplicants = sameNameApplicants.stream()
                .filter(applicant -> schoolMatches(row.getSchoolName(), applicant.getSchoolName()))
                .collect(Collectors.toList());
        if (sameSchoolApplicants.isEmpty()) {
            String schools = sameNameApplicants.stream().map(CompetitionApplyInfo::getSchoolName)
                    .filter(Objects::nonNull).distinct().collect(Collectors.joining("、"));
            addRowWarning(warnings, row, "老师姓名已匹配，但参赛单位与指导教师报名学校不一致，"
                    + "仅写入历史证书表；报名学校：" + schools);
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }

        List<CompetitionApplyInfo> trackMatchedApplicants = sameSchoolApplicants.stream()
                .filter(applicant -> trackMatches(row.getCompetitionTrackName(), applicant.getCompetitionTrackName()))
                .collect(Collectors.toList());
        if (trackMatchedApplicants.isEmpty()) {
            String tracks = sameSchoolApplicants.stream().map(CompetitionApplyInfo::getCompetitionTrackName)
                    .filter(Objects::nonNull).distinct().collect(Collectors.joining("、"));
            addRowWarning(warnings, row, "奖项中的赛道与指导教师报名赛道不一致，"
                    + "仅写入历史证书表；报名赛道：" + tracks);
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }

        Map<Long, CompetitionApplyInfo> accounts = trackMatchedApplicants.stream()
                .filter(applicant -> applicant.getUserId() != null)
                .collect(Collectors.toMap(CompetitionApplyInfo::getUserId, applicant -> applicant,
                        (left, right) -> preferredTeacherApplicant(left, right), LinkedHashMap::new));
        if (accounts.isEmpty()) {
            addRowWarning(warnings, row, "已唯一定位指导教师报名信息，但未找到可唯一确认的系统账号，"
                    + "仅写入历史证书表");
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }
        if (accounts.size() > 1) {
            addRowWarning(warnings, row, "指导教师报名信息关联到多个系统账号，无法安全确定证书归属，"
                    + "仅写入历史证书表；用户ID：" + accounts.keySet().stream()
                    .map(String::valueOf).collect(Collectors.joining("、")));
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }

        CompetitionApplyInfo applicant = accounts.values().iterator().next();
        List<String> applicantErrors = new ArrayList<>();
        validateApplicantLengths(row, applicant, applicantErrors);
        if (!applicantErrors.isEmpty()) {
            addRowWarning(warnings, row, "指导教师账号关联字段超过证书表长度限制，仅写入历史证书表");
            return MatchedCertificateRow.forApplicant(row, null,
                    storedAwardValue(row), buildCertificateName(row));
        }
        return MatchedCertificateRow.forApplicant(row, applicant,
                storedAwardValue(row), buildCertificateName(row));
    }

    private CompetitionApplyInfo preferredTeacherApplicant(
            CompetitionApplyInfo left, CompetitionApplyInfo right) {
        if (isBlank(left.getIdCard()) && !isBlank(right.getIdCard())) {
            return right;
        }
        if (isBlank(left.getCompetitionTrackId()) && !isBlank(right.getCompetitionTrackId())) {
            return right;
        }
        return left;
    }

    private MatchedCertificateRow matchTeamCertificate(
            CertificateImportExcelRow row,
            Map<String, List<CompetitionApplyInfo>> teamApplicantsMap,
            List<String> warnings,
            ImportContext context) {
        Map<String, Long> excelPlayerCounts = splitNameCounts(row.getPlayer());
        List<List<CompetitionApplyInfo>> rosterMatchedTeams = teamApplicantsMap.values().stream()
                .filter(team -> excelPlayerCounts.equals(teamPlayerCounts(team)))
                .collect(Collectors.toList());

        if (rosterMatchedTeams.isEmpty()) {
            addRowWarning(warnings, row, "在赛事" + context.competitionSeriesId
                    + "的已缴费报名数据中未找到完整参赛队员名单完全一致的团队，仅写入历史证书表");
            return MatchedCertificateRow.forApplicants(row, Collections.emptyList(),
                    storedAwardValue(row), buildCertificateName(row));
        }
        if (rosterMatchedTeams.size() > 1) {
            String teamCodes = rosterMatchedTeams.stream()
                    .map(team -> team.get(0).getTeamCode()).distinct().collect(Collectors.joining("、"));
            addRowWarning(warnings, row, "完整参赛队员名单匹配到多个报名团队，"
                    + "无法确定用户证书归属，仅写入历史证书表；团队编号：" + teamCodes);
            return MatchedCertificateRow.forApplicants(row, Collections.emptyList(),
                    storedAwardValue(row), buildCertificateName(row));
        }

        List<CompetitionApplyInfo> orderedApplicants = new ArrayList<>(rosterMatchedTeams.get(0));
        orderedApplicants.sort(Comparator
                .comparing(CompetitionApplyInfo::getTeamSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CompetitionApplyInfo::getMemberId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(CompetitionApplyInfo::getUserId, Comparator.nullsLast(Long::compareTo)));
        List<CompetitionApplyInfo> validApplicants = new ArrayList<>();
        List<String> skippedMembers = new ArrayList<>();
        for (CompetitionApplyInfo applicant : orderedApplicants) {
            if (applicant.getUserId() == null) {
                skippedMembers.add(applicant.getUserName() + "（缺少用户ID）");
                continue;
            }
            List<String> applicantErrors = new ArrayList<>();
            validateApplicantLengths(row, applicant, applicantErrors);
            if (!applicantErrors.isEmpty()) {
                skippedMembers.add(applicant.getUserName() + "（超长报名字段）");
                continue;
            }
            validApplicants.add(applicant);
        }
        if (!skippedMembers.isEmpty()) {
            addRowWarning(warnings, row, "团队已唯一匹配，但以下队员未写入user_certificate_origin："
                    + String.join("、", skippedMembers));
        }
        return MatchedCertificateRow.forApplicants(row, validApplicants,
                storedAwardValue(row), buildCertificateName(row));
    }

    private Map<String, Long> teamPlayerCounts(List<CompetitionApplyInfo> applicants) {
        return applicants.stream()
                .map(CompetitionApplyInfo::getUserName)
                .map(this::normalizePersonName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.groupingBy(name -> name, LinkedHashMap::new, Collectors.counting()));
    }

    private Map<String, Long> splitNameCounts(String names) {
        return Arrays.stream(Objects.toString(names, "").split("[、,，;；/]+"))
                .map(this::normalizePersonName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.groupingBy(name -> name, LinkedHashMap::new, Collectors.counting()));
    }

    private List<CompetitionApplyInfo> resolveDuplicateNameApplicants(
            CertificateImportExcelRow row,
            List<CompetitionApplyInfo> candidates,
            Map<String, Long> duplicateExcelRecipientCounts,
            Map<String, Integer> duplicateApplicantOffsets) {
        if (candidates.size() <= 1) {
            return candidates;
        }
        List<String> teamCodes = candidates.stream()
                .map(CompetitionApplyInfo::getTeamCode)
                .filter(teamCode -> !isBlank(teamCode))
                .distinct()
                .collect(Collectors.toList());
        if (teamCodes.size() != 1) {
            return candidates;
        }
        long excelRowCount = duplicateExcelRecipientCounts.getOrDefault(duplicateExcelRecipientKey(row), 0L);
        long repeatedPlayerCount = countNameOccurrences(row.getPlayer(), row.getUserName());
        if (excelRowCount != candidates.size() || repeatedPlayerCount != candidates.size()) {
            return candidates;
        }

        List<CompetitionApplyInfo> orderedCandidates = new ArrayList<>(candidates);
        orderedCandidates.sort(Comparator
                .comparing(CompetitionApplyInfo::getTeamSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CompetitionApplyInfo::getMemberId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(CompetitionApplyInfo::getUserId, Comparator.nullsLast(Long::compareTo)));
        String offsetKey = teamCodes.get(0) + "|" + duplicateExcelRecipientKey(row);
        int offset = duplicateApplicantOffsets.getOrDefault(offsetKey, 0);
        if (offset >= orderedCandidates.size()) {
            return candidates;
        }
        duplicateApplicantOffsets.put(offsetKey, offset + 1);
        return Collections.singletonList(orderedCandidates.get(offset));
    }

    private String duplicateExcelRecipientKey(CertificateImportExcelRow row) {
        return normalizePersonName(row.getUserName()) + "|"
                + normalizeSchoolName(row.getSchoolName()) + "|"
                + normalizeTrackText(row.getCompetitionName()) + "|"
                + normalizeText(row.getCompetitionRegion()) + "|"
                + normalizeText(row.getAwardsName()) + "|"
                + normalizeText(row.getPlayer()) + "|"
                + normalizeText(row.getGuideTeacher());
    }

    private long countNameOccurrences(String names, String userName) {
        String normalizedName = normalizePersonName(userName);
        return Arrays.stream(Objects.toString(names, "").split("[、,，;；/]+"))
                .map(this::normalizePersonName)
                .filter(normalizedName::equals)
                .count();
    }

    private void applyUserIdFallback(List<CompetitionApplyInfo> applicants) {
        if (applicants == null || applicants.isEmpty()) {
            return;
        }
        applyVerifiedUserIdFallback(applicants);
        applyPhoneUserIdFallback(applicants);
        applyLeaderTeacherUserIdFallback(applicants);
        applyVerifiedTeacherNameSchoolFallback(applicants);
    }

    private void applyVerifiedUserIdFallback(List<CompetitionApplyInfo> applicants) {
        List<String> idCards = applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .map(CompetitionApplyInfo::getIdCard)
                .filter(idCard -> !isBlank(idCard))
                .map(this::cleanCellValue)
                .distinct()
                .collect(Collectors.toList());
        if (idCards.isEmpty()) {
            return;
        }
        List<CompetitionApplyInfo> verifiedUsers = competitionApplyInfoMapper
                .selectUniqueVerifiedUsersByIdCards(idCards);
        if (verifiedUsers == null || verifiedUsers.isEmpty()) {
            return;
        }
        Map<String, Long> verifiedUserIdMap = new HashMap<>();
        Set<String> ambiguousIdCards = new LinkedHashSet<>();
        verifiedUsers.stream()
                .filter(user -> user.getUserId() != null && !isBlank(user.getIdCard()))
                .forEach(user -> {
                    String idCard = normalizeIdCard(user.getIdCard());
                    Long existingUserId = verifiedUserIdMap.putIfAbsent(idCard, user.getUserId());
                    if (existingUserId != null && !Objects.equals(existingUserId, user.getUserId())) {
                        ambiguousIdCards.add(idCard);
                    }
                });
        ambiguousIdCards.forEach(verifiedUserIdMap::remove);
        applicants.stream()
                .filter(applicant -> applicant.getUserId() == null && !isBlank(applicant.getIdCard()))
                .forEach(applicant -> applicant.setUserId(
                        verifiedUserIdMap.get(normalizeIdCard(applicant.getIdCard()))));
    }

    private void applyPhoneUserIdFallback(List<CompetitionApplyInfo> applicants) {
        List<String> phones = applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .map(CompetitionApplyInfo::getPhone)
                .filter(phone -> !isBlank(phone))
                .map(this::cleanCellValue)
                .distinct()
                .collect(Collectors.toList());
        if (phones.isEmpty()) {
            return;
        }
        List<CompetitionApplyInfo> phoneUsers = competitionApplyInfoMapper
                .selectUniqueActiveUsersByPhones(phones);
        if (phoneUsers == null || phoneUsers.isEmpty()) {
            return;
        }
        Map<String, CompetitionApplyInfo> phoneUserMap = new HashMap<>();
        Set<String> ambiguousPhones = new LinkedHashSet<>();
        phoneUsers.stream()
                .filter(user -> user.getUserId() != null && !isBlank(user.getPhone()))
                .forEach(user -> {
                    String phone = normalizeText(user.getPhone());
                    CompetitionApplyInfo existingUser = phoneUserMap.putIfAbsent(phone, user);
                    if (existingUser != null && !Objects.equals(existingUser.getUserId(), user.getUserId())) {
                        ambiguousPhones.add(phone);
                    }
                });
        ambiguousPhones.forEach(phoneUserMap::remove);
        applicants.stream()
                .filter(applicant -> applicant.getUserId() == null && !isBlank(applicant.getPhone()))
                .forEach(applicant -> copyResolvedUser(
                        applicant, phoneUserMap.get(normalizeText(applicant.getPhone()))));
    }

    private void applyLeaderTeacherUserIdFallback(List<CompetitionApplyInfo> applicants) {
        List<Long> userIds = applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .filter(applicant -> GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                .map(CompetitionApplyInfo::getLeaderTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        List<CompetitionApplyInfo> activeUsers = competitionApplyInfoMapper.selectActiveUsersByIds(userIds);
        if (activeUsers == null || activeUsers.isEmpty()) {
            return;
        }
        Map<Long, CompetitionApplyInfo> activeUserMap = activeUsers.stream()
                .filter(user -> user.getUserId() != null)
                .collect(Collectors.toMap(CompetitionApplyInfo::getUserId, user -> user,
                        (left, right) -> left, LinkedHashMap::new));
        applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .filter(applicant -> GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                .forEach(applicant -> {
                    CompetitionApplyInfo user = activeUserMap.get(applicant.getLeaderTeacherId());
                    if (user == null) {
                        return;
                    }
                    boolean samePhone = !isBlank(applicant.getPhone())
                            && normalizeText(applicant.getPhone()).equals(normalizeText(user.getPhone()));
                    boolean sameName = normalizePersonName(applicant.getUserName())
                            .equals(normalizePersonName(user.getUserName()));
                    boolean schoolCompatible = isBlank(user.getSchoolName())
                            || schoolMatches(applicant.getSchoolName(), user.getSchoolName());
                    if (samePhone || (sameName && schoolCompatible)) {
                        copyResolvedUser(applicant, user);
                    }
                });
    }

    private void applyVerifiedTeacherNameSchoolFallback(List<CompetitionApplyInfo> applicants) {
        List<String> realNames = applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .filter(applicant -> GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                .map(CompetitionApplyInfo::getUserName)
                .filter(name -> !isBlank(name))
                .map(this::cleanCellValue)
                .distinct()
                .collect(Collectors.toList());
        if (realNames.isEmpty()) {
            return;
        }
        List<CompetitionApplyInfo> verifiedUsers = competitionApplyInfoMapper
                .selectVerifiedUsersByRealNames(realNames);
        if (verifiedUsers == null || verifiedUsers.isEmpty()) {
            return;
        }
        Map<String, List<CompetitionApplyInfo>> verifiedNameMap = verifiedUsers.stream()
                .filter(user -> user.getUserId() != null && !isBlank(user.getUserName()))
                .collect(Collectors.groupingBy(user -> normalizePersonName(user.getUserName()),
                        LinkedHashMap::new, Collectors.toList()));
        applicants.stream()
                .filter(applicant -> applicant.getUserId() == null)
                .filter(applicant -> GUIDE_TEACHER_ROLE.equals(applicant.getCompetitionRoleName()))
                .forEach(applicant -> {
                    Map<Long, CompetitionApplyInfo> schoolMatchedUsers = verifiedNameMap
                            .getOrDefault(normalizePersonName(applicant.getUserName()), Collections.emptyList()).stream()
                            .filter(user -> schoolMatches(applicant.getSchoolName(), user.getSchoolName()))
                            .collect(Collectors.toMap(CompetitionApplyInfo::getUserId, user -> user,
                                    (left, right) -> left, LinkedHashMap::new));
                    if (schoolMatchedUsers.size() == 1) {
                        copyResolvedUser(applicant, schoolMatchedUsers.values().iterator().next());
                    }
                });
    }

    private void copyResolvedUser(CompetitionApplyInfo applicant, CompetitionApplyInfo user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        applicant.setUserId(user.getUserId());
        if (isBlank(applicant.getIdCard()) && !isBlank(user.getIdCard())) {
            applicant.setIdCard(user.getIdCard());
        }
        if (isBlank(applicant.getSchool()) && !isBlank(user.getSchool())) {
            applicant.setSchool(user.getSchool());
        }
    }

    private List<CompetitionApplyInfo> narrowByTeamPlayers(List<CompetitionApplyInfo> candidates, String players,
                                                            Map<String, Set<String>> teamPlayerMap) {
        if (candidates.size() <= 1) {
            return candidates;
        }
        List<CompetitionApplyInfo> rosterMatches = findTeamPlayerMatches(candidates, players, teamPlayerMap);
        return rosterMatches.isEmpty() ? candidates : rosterMatches;
    }

    private List<CompetitionApplyInfo> findTeamPlayerMatches(List<CompetitionApplyInfo> candidates, String players,
                                                              Map<String, Set<String>> teamPlayerMap) {
        Set<String> excelPlayers = splitNames(players);
        if (excelPlayers.isEmpty()) {
            return Collections.emptyList();
        }
        List<CompetitionApplyInfo> exactMatches = candidates.stream()
                .filter(candidate -> excelPlayers.equals(teamPlayerMap.get(candidate.getTeamCode())))
                .collect(Collectors.toList());
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        List<CompetitionApplyInfo> containsMatches = candidates.stream()
                .filter(candidate -> {
                    Set<String> registeredPlayers = teamPlayerMap.get(candidate.getTeamCode());
                    return registeredPlayers != null && registeredPlayers.containsAll(excelPlayers);
                })
                .collect(Collectors.toList());
        return containsMatches;
    }

    private boolean schoolMatches(String excelSchoolName, String applicantSchoolName) {
        String excelSchool = normalizeSchoolName(excelSchoolName);
        String applicantSchool = normalizeSchoolName(applicantSchoolName);
        if (excelSchool.equals(applicantSchool)) {
            return true;
        }
        int shorterLength = Math.min(excelSchool.length(), applicantSchool.length());
        return shorterLength >= 4
                && (excelSchool.endsWith(applicantSchool) || applicantSchool.endsWith(excelSchool));
    }

    private String normalizeSchoolName(String schoolName) {
        return normalizeText(schoolName)
                .replace("（", "(")
                .replace("）", ")")
                .replace("－", "-");
    }

    private String normalizeIdCard(String idCard) {
        return normalizeText(idCard).toUpperCase(Locale.ROOT);
    }

    private void validateApplicantLengths(CertificateImportExcelRow row, CompetitionApplyInfo applicant, List<String> errors) {
        validateLength(row, "报名团队编号", applicant.getTeamCode(), 64, errors);
        validateLength(row, "报名赛道编码", applicant.getCompetitionTrackId(), 64, errors);
        validateLength(row, "报名组别编码", applicant.getSecondLevelCode(), 64, errors);
        validateLength(row, "报名赛道名称", applicant.getCompetitionTrackName(), 100, errors);
        validateLength(row, "报名组别名称", applicant.getSecondLevelName(), 100, errors);
        validateLength(row, "报名学校编码", applicant.getSchool(), 100, errors);
        validateLength(row, "报名身份证号", applicant.getIdCard(), 100, errors);
    }

    String buildCertificateName(CertificateImportExcelRow row) {
        CertificateImportType certificateType = certificateTypeOf(row);
        if (certificateType == CertificateImportType.TEACHER_HONOR) {
            return normalizeEdition(row.getCompetitionEdition())
                    + cleanCellValue(row.getCompetitionName())
                    + cleanCellValue(row.getCompetitionRegion())
                    + "中，指导学生获得"
                    + cleanCellValue(row.getAwardsName())
                    + "，成绩斐然，荣获“优秀指导教师”，特发此证，以资鼓励！";
        }
        if (certificateType == CertificateImportType.ORGANIZATION_HONOR) {
            return normalizeEdition(row.getCompetitionEdition())
                    + cleanCellValue(row.getCompetitionName())
                    + "中荣获"
                    + cleanCellValue(row.getAwardsName())
                    + "，特发此证，以资鼓励！";
        }
        return normalizeEdition(row.getCompetitionEdition())
                + cleanCellValue(row.getCompetitionName())
                + "中，荣获"
                + cleanCellValue(row.getCompetitionRegion())
                + cleanCellValue(row.getAwardsName())
                + "，特此表彰！";
    }

    private String extractTeacherTrack(String awardName) {
        Matcher matcher = TEACHER_TRACK_PATTERN.matcher(cleanCellValue(awardName));
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractTrackFromCompetitionName(String competitionName) {
        String value = cleanCellValue(competitionName);
        int trackIndex = value.lastIndexOf("赛道");
        if (trackIndex < 0) {
            return "";
        }
        String baseCompetitionName = LEGACY_COMPETITION_NAME;
        int baseNameIndex = value.indexOf(baseCompetitionName);
        int start = baseNameIndex >= 0 ? baseNameIndex + baseCompetitionName.length() : 0;
        return start <= trackIndex ? value.substring(start, trackIndex + 2) : "";
    }

    private String storedAwardValue(CertificateImportExcelRow row) {
        CertificateImportType certificateType = certificateTypeOf(row);
        if (certificateType == CertificateImportType.TEACHER_HONOR
                || certificateType == CertificateImportType.ORGANIZATION_HONOR) {
            return cleanCellValue(row.getAwardsName());
        }
        return awardCode(row.getAwardsName());
    }

    private String normalizeEdition(String edition) {
        String value = cleanCellValue(edition);
        String core = value.startsWith("第") ? value.substring(1) : value;
        core = core.endsWith("届") ? core.substring(0, core.length() - 1) : core;
        return "第" + core + "届";
    }

    private String buildSql(String originalFilename, List<MatchedCertificateRow> rows,
                            List<String> matchWarnings, ImportContext context) {
        long originRowCount = rows.stream().mapToInt(MatchedCertificateRow::originRowCount).sum();
        List<SqlCertificateRow> sqlRows = flattenSqlRows(rows);
        String issuanceDateTime = context.issuanceDate + " 00:00:00";
        String certificateYear = String.valueOf(context.issuanceDate.getYear());
        StringBuilder sql = new StringBuilder(Math.max(16_384, rows.size() * 1_200));
        sql.append("-- 获奖证书双表导入SQL\n")
                .append("-- 源文件：").append(safeSqlComment(originalFilename)).append("\n")
                .append("-- 证书类型：").append(context.certificateType.getDescription()).append("\n")
                .append("-- 目标赛事：competition_series_id=").append(context.competitionSeriesId)
                .append("，competition_id=").append(context.competitionId)
                .append("，").append(safeSqlComment(context.competitionName)).append("\n")
                .append("-- 发证日期：").append(context.issuanceDate).append("；历史证书行数：").append(rows.size())
                .append("；可关联用户证书行数：").append(originRowCount).append("\n")
                .append("-- 双表规则：全部有效Excel行进入user_certificate_history；仅成功关联用户的行进入user_certificate_origin。\n")
                .append("-- 说明：脚本按证书编号、用户ID幂等导入；执行前请先完成数据库备份。\n\n")
                .append(buildWarningComments(matchWarnings))
                .append("SET NAMES utf8mb4;\n")
                .append("START TRANSACTION;\n\n")
                .append("DROP TEMPORARY TABLE IF EXISTS `tmp_certificate_import`;\n")
                .append("CREATE TEMPORARY TABLE `tmp_certificate_import` (\n")
                .append("  `row_no` int NOT NULL,\n")
                .append("  `history_row` tinyint NOT NULL,\n")
                .append("  `cert_code` varchar(255) NOT NULL,\n")
                .append("  `cert_name` varchar(255) NOT NULL,\n")
                .append("  `competition_edition` varchar(100) NOT NULL,\n")
                .append("  `competition_region` varchar(100) NOT NULL,\n")
                .append("  `competition_track_id` varchar(64) DEFAULT NULL,\n")
                .append("  `competition_track_name` varchar(100) DEFAULT NULL,\n")
                .append("  `second_level_code` varchar(64) DEFAULT NULL,\n")
                .append("  `second_level_name` varchar(100) DEFAULT NULL,\n")
                .append("  `team_code` varchar(64) DEFAULT NULL,\n")
                .append("  `awards_name` varchar(100) NOT NULL,\n")
                .append("  `awards_name_desc` varchar(100) NOT NULL,\n")
                .append("  `user_id` bigint DEFAULT NULL,\n")
                .append("  `user_name` varchar(100) NOT NULL,\n")
                .append("  `history_user_name` varchar(100) NOT NULL,\n")
                .append("  `id_card` varchar(100) DEFAULT NULL,\n")
                .append("  `player` varchar(100) NOT NULL,\n")
                .append("  `guide_teacher` varchar(100) DEFAULT NULL,\n")
                .append("  `school` varchar(100) DEFAULT NULL,\n")
                .append("  `school_name` varchar(100) NOT NULL,\n")
                .append("  PRIMARY KEY (`row_no`),\n")
                .append("  KEY `idx_tmp_cert_code` (`cert_code`),\n")
                .append("  KEY `idx_tmp_user_cert` (`user_id`, `cert_code`)\n")
                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n");

        appendTempTableValues(sql, sqlRows);

        sql.append("-- 执行前统计：应与Excel有效数据行数一致。\n")
                .append("SELECT COUNT(*) AS `excel_valid_rows` FROM `tmp_certificate_import` WHERE `history_row` = 1;\n")
                .append("SELECT COUNT(*) AS `origin_candidate_rows` FROM `tmp_certificate_import` WHERE `user_id` IS NOT NULL;\n")
                .append("SELECT COUNT(*) AS `history_already_exists`\n")
                .append("FROM `tmp_certificate_import` t\n")
                .append("WHERE t.`history_row` = 1\n")
                .append("  AND EXISTS (SELECT 1 FROM `user_certificate_history` h WHERE h.`cert_code` = t.`cert_code` AND h.`del_flag` = '0');\n")
                .append("SELECT COUNT(*) AS `origin_already_exists`\n")
                .append("FROM `tmp_certificate_import` t\n")
                .append("WHERE t.`user_id` IS NOT NULL\n")
                .append("  AND EXISTS (SELECT 1 FROM `user_certificate_origin` o WHERE o.`cert_code` = t.`cert_code` AND o.`user_id` = t.`user_id` AND o.`del_flag` = '0');\n\n")
                .append("-- 1. 原始历史证书：支持公开证书查询，不保存身份证号。\n")
                .append("INSERT INTO `user_certificate_history` (\n")
                .append("  `cert_code`, `cert_name`, `cert_url`, `issuance_date`, `acquire_way`,\n")
                .append("  `competition_series_id`, `competition_track_id`, `second_level_code`,\n")
                .append("  `competition_name`, `competition_track_name`, `cert_status`, `team_code`,\n")
                .append("  `awards_name`, `awards_name_desc`, `year`, `org_code`, `user_name`,\n")
                .append("  `player`, `guide_teacher`, `school`, `school_name`, `create_by`, `create_time`, `del_flag`, `version`\n")
                .append(")\n")
                .append("SELECT\n")
                .append("  t.`cert_code`, t.`cert_name`, ").append(sqlString(CERT_URL)).append(", ").append(sqlString(issuanceDateTime)).append(", '1',\n")
                .append("  ").append(context.competitionSeriesId).append(", t.`competition_track_id`, t.`second_level_code`,\n")
                .append("  t.`competition_edition`, t.`competition_region`, '0', t.`team_code`,\n")
                .append("  t.`awards_name_desc`, t.`awards_name_desc`, ").append(sqlString(certificateYear)).append(", ").append(sqlString(ORG_CODE)).append(", t.`history_user_name`,\n")
                .append("  t.`player`, NULLIF(t.`guide_teacher`, ''), t.`school`, t.`school_name`, 'certificate_import_sql', NOW(), '0', 0\n")
                .append("FROM `tmp_certificate_import` t\n")
                .append("WHERE t.`history_row` = 1\n")
                .append("  AND NOT EXISTS (\n")
                .append("  SELECT 1 FROM `user_certificate_history` h\n")
                .append("  WHERE h.`cert_code` = t.`cert_code` AND h.`del_flag` = '0'\n")
                .append(");\n")
                .append("SELECT ROW_COUNT() AS `history_inserted_rows`;\n\n")
                .append("-- 2. 用户源证书：支持登录后的“我的证书”和赛证兑换识别。\n")
                .append("INSERT INTO `user_certificate_origin` (\n")
                .append("  `cert_code`, `cert_name`, `cert_url`, `issuance_date`, `acquire_way`,\n")
                .append("  `competition_series_id`, `competition_track_id`, `second_level_code`,\n")
                .append("  `competition_name`, `competition_track_name`, `second_level_name`,\n")
                .append("  `cert_status`, `team_code`, `awards_name`, `awards_name_desc`, `year`, `org_code`,\n")
                .append("  `user_id`, `user_name`, `id_card`, `player`, `guide_teacher`, `school`, `school_name`,\n")
                .append("  `create_by`, `create_time`, `del_flag`, `version`\n")
                .append(")\n")
                .append("SELECT\n")
                .append("  t.`cert_code`, t.`cert_name`, ").append(sqlString(CERT_URL)).append(", ").append(sqlString(issuanceDateTime)).append(", '1',\n")
                .append("  ").append(context.competitionSeriesId).append(", t.`competition_track_id`, t.`second_level_code`,\n")
                .append("  t.`competition_edition`, t.`competition_track_name`, t.`second_level_name`,\n")
                .append("  '0', t.`team_code`, t.`awards_name`, t.`awards_name_desc`, ").append(sqlString(certificateYear)).append(", ").append(sqlString(ORG_CODE)).append(",\n")
                .append("  t.`user_id`, t.`user_name`, t.`id_card`, t.`player`, NULLIF(t.`guide_teacher`, ''), t.`school`, t.`school_name`,\n")
                .append("  'certificate_import_sql', NOW(), '0', 0\n")
                .append("FROM `tmp_certificate_import` t\n")
                .append("WHERE t.`user_id` IS NOT NULL\n")
                .append("  AND NOT EXISTS (\n")
                .append("  SELECT 1 FROM `user_certificate_origin` o\n")
                .append("  WHERE o.`cert_code` = t.`cert_code` AND o.`user_id` = t.`user_id` AND o.`del_flag` = '0'\n")
                .append(");\n")
                .append("SELECT ROW_COUNT() AS `origin_inserted_rows`;\n\n")
                .append("-- 3. 重新导入的负责人学生证书重新进入图片同步队列；图片正文仍只保存在私有对象存储。\n")
                .append("INSERT INTO `certificate_image_cache` (\n")
                .append("  `cert_code`, `contest_name`, `recipient_name`, `session`, `contest_area`, `runing_num_year`,\n")
                .append("  `cache_status`, `retry_count`, `create_time`, `update_time`\n")
                .append(")\n")
                .append("SELECT t.`cert_code`, t.`competition_track_name`, MAX(t.`user_name`),\n")
                .append("       t.`competition_edition`, t.`competition_region`, ").append(sqlString(certificateYear)).append(",\n")
                .append("       'PENDING', 0, NOW(), NOW()\n")
                .append("FROM `tmp_certificate_import` t\n")
                .append("WHERE t.`user_id` IS NOT NULL AND t.`team_code` IS NOT NULL AND t.`team_code` != ''\n")
                .append("GROUP BY t.`cert_code`, t.`competition_track_name`, t.`competition_edition`, t.`competition_region`\n")
                .append("ON DUPLICATE KEY UPDATE\n")
                .append("  `contest_name` = VALUES(`contest_name`), `recipient_name` = VALUES(`recipient_name`),\n")
                .append("  `session` = VALUES(`session`), `contest_area` = VALUES(`contest_area`),\n")
                .append("  `runing_num_year` = VALUES(`runing_num_year`), `cache_status` = 'PENDING',\n")
                .append("  `retry_count` = 0, `last_error` = NULL, `next_retry_time` = NULL, `update_time` = NOW();\n")
                .append("SELECT ROW_COUNT() AS `image_cache_activated_rows`;\n\n")
                .append("DROP TEMPORARY TABLE IF EXISTS `tmp_certificate_import`;\n")
                .append("COMMIT;\n")
                .append("-- 如执行过程出现异常，请在当前连接中执行 ROLLBACK;\n");
        return sql.toString();
    }

    private List<SqlCertificateRow> flattenSqlRows(List<MatchedCertificateRow> rows) {
        List<SqlCertificateRow> sqlRows = new ArrayList<>();
        for (MatchedCertificateRow matched : rows) {
            if (matched.applicants.isEmpty()) {
                sqlRows.add(new SqlCertificateRow(matched, null, true));
                continue;
            }
            for (int index = 0; index < matched.applicants.size(); index++) {
                sqlRows.add(new SqlCertificateRow(matched, matched.applicants.get(index), index == 0));
            }
        }
        return sqlRows;
    }

    private void appendTempTableValues(StringBuilder sql, List<SqlCertificateRow> rows) {
        for (int start = 0; start < rows.size(); start += SQL_VALUE_CHUNK_SIZE) {
            int end = Math.min(start + SQL_VALUE_CHUNK_SIZE, rows.size());
            sql.append("INSERT INTO `tmp_certificate_import` (\n")
                    .append("  `row_no`, `history_row`, `cert_code`, `cert_name`, `competition_edition`, `competition_region`,\n")
                    .append("  `competition_track_id`, `competition_track_name`, `second_level_code`, `second_level_name`, `team_code`,\n")
                    .append("  `awards_name`, `awards_name_desc`, `user_id`, `user_name`, `id_card`, `history_user_name`, `player`, `guide_teacher`, `school`, `school_name`\n")
                    .append(") VALUES\n");
            for (int index = start; index < end; index++) {
                SqlCertificateRow sqlRow = rows.get(index);
                MatchedCertificateRow matched = sqlRow.matched;
                CertificateImportExcelRow row = matched.excelRow;
                CompetitionApplyInfo applicant = sqlRow.applicant;
                String historyUserName = historyUserName(row);
                boolean teacherCertificate = certificateTypeOf(row) == CertificateImportType.TEACHER_HONOR;
                sql.append("  (")
                        .append(index + 1).append(", ")
                        .append(sqlRow.historyRow ? "1" : "0").append(", ")
                        .append(sqlString(row.getCertCode())).append(", ")
                        .append(sqlString(matched.certName)).append(", ")
                        .append(sqlString(row.getCompetitionEdition())).append(", ")
                        .append(sqlString(row.getCompetitionRegion())).append(", ")
                        .append(sqlString(applicant == null ? null : applicant.getCompetitionTrackId())).append(", ")
                        .append(sqlString(applicant == null || isBlank(applicant.getCompetitionTrackName())
                                ? row.getCompetitionTrackName() : applicant.getCompetitionTrackName())).append(", ")
                        .append(sqlString(applicant == null ? null : applicant.getSecondLevelCode())).append(", ")
                        .append(sqlString(applicant == null ? null : applicant.getSecondLevelName())).append(", ")
                        .append(sqlString(applicant == null || teacherCertificate ? null : applicant.getTeamCode())).append(", ")
                        .append(sqlString(matched.awardCode)).append(", ")
                        .append(sqlString(row.getAwardsName())).append(", ")
                        .append(applicant == null ? "NULL" : applicant.getUserId()).append(", ")
                        .append(sqlString(applicant == null ? historyUserName : applicant.getUserName())).append(", ")
                        .append(sqlString(applicant == null ? null : applicant.getIdCard())).append(", ")
                        .append(sqlString(historyUserName)).append(", ")
                        .append(sqlString(row.getPlayer())).append(", ")
                        .append(sqlString(row.getGuideTeacher())).append(", ")
                        .append(sqlString(applicant == null ? null : applicant.getSchool())).append(", ")
                        .append(sqlString(row.getSchoolName())).append(")")
                        .append(index == end - 1 ? ";\n\n" : ",\n");
            }
        }
    }

    private String sqlString(String value) {
        if (value == null) {
            return "NULL";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("'", "''")
                .replace("\u0000", "")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return "'" + escaped + "'";
    }

    private String awardCode(String awardName) {
        String normalized = normalizeText(awardName);
        String code = AWARD_CODE_MAP.get(normalized);
        if (code != null) {
            return code;
        }
        return null;
    }

    private boolean trackMatches(String excelCompetitionName, String applicantTrackName) {
        String excelTrack = normalizeTrackText(excelCompetitionName);
        String applicantTrack = normalizeTrackText(applicantTrackName);
        return !excelTrack.isEmpty() && !applicantTrack.isEmpty() && excelTrack.contains(applicantTrack);
    }

    private String normalizeTrackText(String value) {
        return normalizeText(value)
                .replace("“", "")
                .replace("”", "")
                .replace("\"", "")
                .replace("赛道", "赛");
    }

    private boolean containsPlayer(String players, String userName) {
        String normalizedName = normalizePersonName(userName);
        return splitNames(players).contains(normalizedName);
    }

    private Set<String> splitNames(String names) {
        return Arrays.stream(Objects.toString(names, "").split("[、,，;；/]+"))
                .map(this::normalizePersonName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String applicantKey(String userName, String schoolName) {
        return normalizePersonName(userName) + "|" + normalizeText(schoolName);
    }

    private String applicantFingerprint(CompetitionApplyInfo applicant) {
        return applicant.getUserId() + "|" + Objects.toString(applicant.getTeamCode(), "") + "|"
                + Objects.toString(applicant.getCompetitionTrackId(), "") + "|"
                + Objects.toString(applicant.getSecondLevelCode(), "");
    }

    private boolean isImportRowEmpty(CertificateImportExcelRow row) {
        return isBlank(row.getUserName())
                && isBlank(row.getTeamName())
                && isBlank(row.getCompetitionEdition())
                && isBlank(row.getCompetitionName())
                && isBlank(row.getCompetitionTrackName())
                && isBlank(row.getCompetitionRegion())
                && isBlank(row.getAwardsName())
                && isBlank(row.getCertCode())
                && isBlank(row.getSchoolName())
                && isBlank(row.getPlayer())
                && isBlank(row.getGuideTeacher());
    }

    private boolean isTeamCertificate(CertificateImportExcelRow row) {
        return certificateTypeOf(row) == CertificateImportType.STUDENT_TEAM;
    }

    private CertificateImportType certificateTypeOf(CertificateImportExcelRow row) {
        if (row.getCertificateType() != null) {
            return row.getCertificateType();
        }
        return row.isTeamCertificate()
                ? CertificateImportType.STUDENT_TEAM : CertificateImportType.STUDENT_PERSONAL;
    }

    private String historyUserName(CertificateImportExcelRow row) {
        return switch (certificateTypeOf(row)) {
            case STUDENT_TEAM -> row.getTeamName();
            case ORGANIZATION_HONOR -> row.getSchoolName();
            case STUDENT_PERSONAL, TEACHER_HONOR -> row.getUserName();
        };
    }

    private void validateRequired(CertificateImportExcelRow row, String fieldName, String value, List<String> errors) {
        if (isBlank(value)) {
            addRowError(errors, row, "【" + fieldName + "】不能为空");
        }
    }

    private void validateLength(CertificateImportExcelRow row, String fieldName, String value, int maxLength,
                                List<String> errors) {
        if (value != null && value.length() > maxLength) {
            addRowError(errors, row, "【" + fieldName + "】不能超过" + maxLength + "个字符");
        }
    }

    private void addRowError(List<String> errors, CertificateImportExcelRow row, String message) {
        addError(errors, rowLocation(row) + "：" + message);
    }

    private void addRowWarning(List<String> warnings, CertificateImportExcelRow row, String message) {
        warnings.add(rowLocation(row) + "：" + message);
    }

    private void addError(List<String> errors, String message) {
        if (errors.size() < MAX_ERROR_MESSAGES) {
            errors.add(message);
        }
    }

    private void throwIfInvalid(List<String> errors) {
        if (errors.isEmpty()) {
            return;
        }
        String message = "证书Excel校验未通过：" + String.join("；", errors);
        if (errors.size() >= MAX_ERROR_MESSAGES) {
            message += "；错误较多，仅展示前" + MAX_ERROR_MESSAGES + "条";
        }
        throw new GlobalException(message);
    }

    private String rowLocation(CertificateImportExcelRow row) {
        return "工作表【" + row.getSheetName() + "】第" + row.getRowNumber() + "行";
    }

    private String buildSqlFileName(String originalFilename) {
        String original = Objects.toString(originalFilename, "证书导入");
        int extensionIndex = original.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? original.substring(0, extensionIndex) : original;
        baseName = baseName.replaceAll("[^\\p{L}\\p{N}_-]", "_");
        if (baseName.isEmpty()) {
            baseName = "证书导入";
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return baseName + "_证书导入_" + time + ".sql";
    }

    private String safeSqlComment(String value) {
        return Objects.toString(value, "").replace("\r", " ").replace("\n", " ");
    }

    private String buildWarningComments(List<String> warnings) {
        if (warnings.isEmpty()) {
            return "-- 用户关联校验：全部通过。\n\n";
        }
        StringBuilder comments = new StringBuilder()
                .append("-- 用户关联提醒：以下").append(warnings.size())
                .append("行存在未关联用户，详情如下。\n");
        for (String warning : warnings) {
            comments.append("-- [关联提醒] ").append(safeSqlComment(warning)).append("\n");
        }
        return comments.append("\n").toString();
    }

    private String cleanCellValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "")
                .replace("\u200B", "")
                .replace('\u00A0', ' ')
                .trim();
    }

    private String normalizeText(String value) {
        return cleanCellValue(value).replaceAll("\\s+", "");
    }

    private String normalizePersonName(String value) {
        return normalizeText(value).toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return cleanCellValue(value).isEmpty();
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private static final class HeaderInfo {
        private final int rowIndex;
        private final Map<String, Integer> columns;
        private final CertificateImportType certificateType;

        private HeaderInfo(int rowIndex, Map<String, Integer> columns,
                           CertificateImportType certificateType) {
            this.rowIndex = rowIndex;
            this.columns = columns;
            this.certificateType = certificateType;
        }
    }

    private static final class ImportContext {
        private final long competitionSeriesId;
        private final long competitionId;
        private final String competitionName;
        private final CertificateImportType certificateType;
        private final LocalDate issuanceDate;

        private ImportContext(long competitionSeriesId, long competitionId, String competitionName,
                              CertificateImportType certificateType, LocalDate issuanceDate) {
            this.competitionSeriesId = competitionSeriesId;
            this.competitionId = competitionId;
            this.competitionName = competitionName;
            this.certificateType = certificateType;
            this.issuanceDate = issuanceDate;
        }
    }

    private static final class PreparedImport {
        private final List<MatchedCertificateRow> matchedRows;
        private final List<String> matchWarnings;

        private PreparedImport(List<MatchedCertificateRow> matchedRows, List<String> matchWarnings) {
            this.matchedRows = matchedRows;
            this.matchWarnings = matchWarnings;
        }

        private int originRowCount() {
            return matchedRows.stream().mapToInt(MatchedCertificateRow::originRowCount).sum();
        }
    }

    private static final class MatchedCertificateRow {
        private final CertificateImportExcelRow excelRow;
        private final List<CompetitionApplyInfo> applicants;
        private final String awardCode;
        private final String certName;

        private static MatchedCertificateRow forApplicant(
                CertificateImportExcelRow excelRow, CompetitionApplyInfo applicant,
                String awardCode, String certName) {
            return new MatchedCertificateRow(excelRow,
                    applicant == null ? Collections.emptyList() : Collections.singletonList(applicant),
                    awardCode, certName);
        }

        private static MatchedCertificateRow forApplicants(
                CertificateImportExcelRow excelRow, List<CompetitionApplyInfo> applicants,
                String awardCode, String certName) {
            return new MatchedCertificateRow(excelRow, applicants, awardCode, certName);
        }

        private MatchedCertificateRow(CertificateImportExcelRow excelRow,
                                      List<CompetitionApplyInfo> applicants,
                                      String awardCode, String certName) {
            this.excelRow = excelRow;
            this.applicants = applicants;
            this.awardCode = awardCode;
            this.certName = certName;
        }

        private int originRowCount() {
            return applicants.size();
        }
    }

    private static final class SqlCertificateRow {
        private final MatchedCertificateRow matched;
        private final CompetitionApplyInfo applicant;
        private final boolean historyRow;

        private SqlCertificateRow(MatchedCertificateRow matched, CompetitionApplyInfo applicant,
                                  boolean historyRow) {
            this.matched = matched;
            this.applicant = applicant;
            this.historyRow = historyRow;
        }
    }
}
