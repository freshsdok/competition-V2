package com.teaching.competition.domain;

/**
 * 证书导入配置。赛事信息以服务端根据赛事系列查询的结果为准。
 */
public class CertificateImportRequest {
    private Long competitionSeriesId;
    private String certificateType;
    private String issuanceDate;

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
    }
}
