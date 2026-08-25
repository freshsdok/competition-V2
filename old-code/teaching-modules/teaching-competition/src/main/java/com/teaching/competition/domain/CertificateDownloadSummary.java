package com.teaching.competition.domain;

/**
 * 团队报名负责人可下载证书统计。
 */
public class CertificateDownloadSummary {
    private long teamCount;
    private long certificateCount;
    private boolean downloadable;

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

    public boolean isDownloadable() {
        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }
}
