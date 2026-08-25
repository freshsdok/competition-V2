package com.teaching.competition.controller;

import com.teaching.common.core.utils.file.FileUtils;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.competition.domain.CertificateImportPreviewResult;
import com.teaching.competition.domain.CertificateImportRequest;
import com.teaching.competition.domain.CertificateImportSqlResult;
import com.teaching.competition.service.ICertificateImportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 获奖证书导入 Controller。
 */
@RestController
@RequestMapping("/competition/certificateImport")
public class CertificateImportController {
    private final ICertificateImportService certificateImportService;

    @Autowired
    public CertificateImportController(ICertificateImportService certificateImportService) {
        this.certificateImportService = certificateImportService;
    }

    /**
     * 校验证书 Excel 并返回导入预览。此接口不会写入业务数据库。
     */
    @RequiresPermissions("competition:certificateImport:generateSql")
    @PostMapping("/preview")
    public AjaxResult preview(@RequestParam("file") MultipartFile file,
                              @RequestParam("competitionSeriesId") Long competitionSeriesId,
                              @RequestParam("certificateType") String certificateType,
                              @RequestParam("issuanceDate") String issuanceDate) {
        CertificateImportPreviewResult result = certificateImportService.previewImport(
                file, buildRequest(competitionSeriesId, certificateType, issuanceDate));
        return AjaxResult.success(result);
    }

    /**
     * 校验获奖证书 Excel，并生成双表导入 SQL。此接口不会写入业务数据库。
     */
    @RequiresPermissions("competition:certificateImport:generateSql")
    @Log(title = "证书导入SQL生成", businessType = BusinessType.EXPORT)
    @PostMapping("/generateSql")
    public void generateSql(@RequestParam("file") MultipartFile file,
                            @RequestParam("competitionSeriesId") Long competitionSeriesId,
                            @RequestParam("certificateType") String certificateType,
                            @RequestParam("issuanceDate") String issuanceDate,
                            HttpServletResponse response) throws IOException {
        CertificateImportSqlResult result = certificateImportService.generateImportSql(
                file, buildRequest(competitionSeriesId, certificateType, issuanceDate));
        response.reset();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("X-Certificate-Import-Rows", String.valueOf(result.getRowCount()));
        response.setHeader("X-Certificate-Origin-Rows", String.valueOf(result.getOriginRowCount()));
        response.setHeader("X-Certificate-Import-Warnings", String.valueOf(result.getWarningCount()));
        FileUtils.setAttachmentResponseHeader(response, result.getFileName());
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(result.getSqlContent().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    private CertificateImportRequest buildRequest(Long competitionSeriesId, String certificateType,
                                                  String issuanceDate) {
        CertificateImportRequest request = new CertificateImportRequest();
        request.setCompetitionSeriesId(competitionSeriesId);
        request.setCertificateType(certificateType);
        request.setIssuanceDate(issuanceDate);
        return request;
    }
}
