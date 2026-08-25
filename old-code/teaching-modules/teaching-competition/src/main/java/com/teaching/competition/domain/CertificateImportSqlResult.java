package com.teaching.competition.domain;

/**
 * 证书导入 SQL 生成结果。
 */
public class CertificateImportSqlResult {
    private final String fileName;
    private final String sqlContent;
    private final int rowCount;
    private final int originRowCount;
    private final int warningCount;

    public CertificateImportSqlResult(String fileName, String sqlContent, int rowCount,
                                      int originRowCount, int warningCount) {
        this.fileName = fileName;
        this.sqlContent = sqlContent;
        this.rowCount = rowCount;
        this.originRowCount = originRowCount;
        this.warningCount = warningCount;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getOriginRowCount() {
        return originRowCount;
    }

    public int getWarningCount() {
        return warningCount;
    }
}
