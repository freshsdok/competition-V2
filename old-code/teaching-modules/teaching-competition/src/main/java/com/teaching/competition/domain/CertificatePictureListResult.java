package com.teaching.competition.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 团队报名负责人证书图片查询结果。
 */
public class CertificatePictureListResult {
    private long teamCount;
    private long certificateCount;
    private List<CertificatePictureItem> certPictureList = new ArrayList<>();
    private List<String> missingCertCodeList = new ArrayList<>();

    public long getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(long teamCount) {
        this.teamCount = teamCount;
    }

    public long getCertificateCount() {
        return certificateCount;
    }

    public void setCertificateCount(long certificateCount) {
        this.certificateCount = certificateCount;
    }

    public List<CertificatePictureItem> getCertPictureList() {
        return certPictureList;
    }

    public void setCertPictureList(List<CertificatePictureItem> certPictureList) {
        this.certPictureList = certPictureList;
    }

    public List<String> getMissingCertCodeList() {
        return missingCertCodeList;
    }

    public void setMissingCertCodeList(List<String> missingCertCodeList) {
        this.missingCertCodeList = missingCertCodeList;
    }
}
