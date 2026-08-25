package com.teaching.competition.review.vo;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 可输出到 HTTP 响应的预览资源。
 */
public class ReviewPreviewResource implements Closeable {
    private final Path path;
    private final byte[] content;
    private final String fileName;
    private final String contentType;
    private final long contentLength;
    private final boolean deleteOnClose;

    private ReviewPreviewResource(Path path, byte[] content, String fileName, String contentType,
                                  long contentLength, boolean deleteOnClose) {
        this.path = path;
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.deleteOnClose = deleteOnClose;
    }

    public static ReviewPreviewResource fromPath(Path path, String fileName, String contentType,
                                                 boolean deleteOnClose) throws IOException {
        return new ReviewPreviewResource(path, null, fileName, contentType, Files.size(path), deleteOnClose);
    }

    public static ReviewPreviewResource fromBytes(byte[] content, String fileName, String contentType) {
        byte[] safeContent = content == null ? new byte[0] : content;
        return new ReviewPreviewResource(null, safeContent, fileName, contentType, safeContent.length, false);
    }

    public InputStream openStream() throws IOException {
        if (path != null) {
            return Files.newInputStream(path);
        }
        return new ByteArrayInputStream(content == null ? new byte[0] : content);
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    @Override
    public void close() throws IOException {
        if (deleteOnClose && path != null) {
            Files.deleteIfExists(path);
            Path parent = path.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (java.util.stream.Stream<Path> children = Files.list(parent)) {
                    if (!children.findAny().isPresent()) {
                        Files.deleteIfExists(parent);
                    }
                }
            }
        }
    }
}
