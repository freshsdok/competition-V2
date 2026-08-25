package com.teaching.competition.review.config;

import com.teaching.common.core.exception.ServiceException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 评审材料在线预览配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "review.preview")
public class ReviewPreviewProperties {
    private static final Logger log = LoggerFactory.getLogger(ReviewPreviewProperties.class);

    private boolean enabled = true;

    private String cacheDir;

    private String tempDir;

    private String libreofficePath = "libreoffice";

    private long maxTextPreviewSize = 1048576L;

    private long convertTimeoutSeconds = 60L;

    @Value("${file.path:}")
    private String filePath;

    public Path resolveCacheDir() {
        return resolveWritableDir(cacheDir, "review-preview");
    }

    public Path resolveTempDir() {
        return resolveWritableDir(tempDir, "review-preview-temp");
    }

    private Path resolveWritableDir(String configuredDir, String fallbackName) {
        if (configuredDir != null && !configuredDir.isBlank()) {
            Path configured = Path.of(configuredDir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(configured);
                if (Files.isWritable(configured)) {
                    return configured;
                }
                log.warn("评审材料预览目录不可写，dir={}，将使用上传目录或系统临时目录兜底", configured);
            } catch (IOException | RuntimeException ex) {
                log.warn("评审材料预览目录创建失败，dir={}，将使用上传目录或系统临时目录兜底", configured, ex);
            }
        }

        Path uploadFallback = resolveUploadFallbackDir(fallbackName);
        if (uploadFallback != null) {
            try {
                Files.createDirectories(uploadFallback);
                if (Files.isWritable(uploadFallback)) {
                    return uploadFallback;
                }
                log.warn("评审材料预览上传目录兜底不可写，dir={}，将使用系统临时目录", uploadFallback);
            } catch (IOException | RuntimeException ex) {
                log.warn("评审材料预览上传目录兜底创建失败，dir={}，将使用系统临时目录", uploadFallback, ex);
            }
        }

        Path fallback = Path.of(System.getProperty("java.io.tmpdir"), fallbackName).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fallback);
            if (Files.isWritable(fallback)) {
                return fallback;
            }
        } catch (IOException | RuntimeException ex) {
            log.error("评审材料预览兜底目录创建失败，dir={}", fallback, ex);
        }
        throw new ServiceException("评审材料预览临时目录不可写，请联系管理员");
    }

    private Path resolveUploadFallbackDir(String fallbackName) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        return Path.of(filePath, fallbackName).toAbsolutePath().normalize();
    }
}
