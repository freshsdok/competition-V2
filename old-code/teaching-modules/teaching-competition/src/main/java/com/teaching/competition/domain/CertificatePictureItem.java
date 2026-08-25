package com.teaching.competition.domain;

/**
 * 前端打包证书时使用的图片信息。
 */
public class CertificatePictureItem {
    private String certCode;
    private String certPicture;
    private String fileName;
    private String contestName;
    private String name;
    private String session;
    private String contestArea;
    private Integer runingNumYear;

    public CertificatePictureItem() {
    }

    public CertificatePictureItem(String certCode, String certPicture, String fileName) {
        this.certCode = certCode;
        this.certPicture = certPicture;
        this.fileName = fileName;
    }

    public String getCertCode() {
        return certCode;
    }

    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    public String getCertPicture() {
        return certPicture;
    }

    public void setCertPicture(String certPicture) {
        this.certPicture = certPicture;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getContestArea() {
        return contestArea;
    }

    public void setContestArea(String contestArea) {
        this.contestArea = contestArea;
    }

    public Integer getRuningNumYear() {
        return runingNumYear;
    }

    public void setRuningNumYear(Integer runingNumYear) {
        this.runingNumYear = runingNumYear;
    }
}
