package com.teaching.system.api.domain;

import java.io.Serializable;

/**
 * 文件任务导入评审模块的材料信息。
 */
public class FileReviewImportMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;

    private String downloadLink;

    private Long fileSize;

    private String mimeType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
