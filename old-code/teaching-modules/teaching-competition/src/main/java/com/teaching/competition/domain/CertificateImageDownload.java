package com.teaching.competition.domain;

/**
 * 从外部证书平台下载到的图片正文及响应类型。
 */
public record CertificateImageDownload(byte[] content, String contentType) {
}
