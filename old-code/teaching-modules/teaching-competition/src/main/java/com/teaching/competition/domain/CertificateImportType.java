package com.teaching.competition.domain;

/**
 * 证书 Excel 导入类型。
 */
public enum CertificateImportType {
    STUDENT_PERSONAL("学生个人证书"),
    STUDENT_TEAM("学生团队证书"),
    TEACHER_HONOR("优秀指导教师证书"),
    ORGANIZATION_HONOR("优秀组织单位证书");

    private final String description;

    CertificateImportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static CertificateImportType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (CertificateImportType value : values()) {
            if (value.name().equalsIgnoreCase(code.trim())) {
                return value;
            }
        }
        return null;
    }
}
