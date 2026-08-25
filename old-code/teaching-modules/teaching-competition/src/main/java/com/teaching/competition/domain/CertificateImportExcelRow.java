package com.teaching.competition.domain;

/**
 * 获奖证书导入 Excel 行数据。
 */
public class CertificateImportExcelRow {
    private String sheetName;
    private int rowNumber;
    private boolean teamCertificate;
    private CertificateImportType certificateType;
    private String userName;
    private String teamName;
    private String competitionEdition;
    private String competitionName;
    private String competitionTrackName;
    private String competitionRegion;
    private String awardsName;
    private String certCode;
    private String schoolName;
    private String player;
    private String guideTeacher;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public boolean isTeamCertificate() {
        return teamCertificate;
    }

    public void setTeamCertificate(boolean teamCertificate) {
        this.teamCertificate = teamCertificate;
    }

    public CertificateImportType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateImportType certificateType) {
        this.certificateType = certificateType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCompetitionEdition() {
        return competitionEdition;
    }

    public void setCompetitionEdition(String competitionEdition) {
        this.competitionEdition = competitionEdition;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionRegion() {
        return competitionRegion;
    }

    public void setCompetitionRegion(String competitionRegion) {
        this.competitionRegion = competitionRegion;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public String getCertCode() {
        return certCode;
    }

    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }
}
