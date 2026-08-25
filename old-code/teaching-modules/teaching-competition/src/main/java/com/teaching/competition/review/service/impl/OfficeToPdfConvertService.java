package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.review.config.ReviewPreviewProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Office 文档转 PDF 服务。
 */
@Service
public class OfficeToPdfConvertService {
    private static final Logger log = LoggerFactory.getLogger(OfficeToPdfConvertService.class);

    @Autowired
    private ReviewPreviewProperties properties;

    public Path convertToPdf(Long materialId, Path sourceFile, String sourceExt, Path cachePdf) {
        try {
            if (Files.isRegularFile(cachePdf) && Files.size(cachePdf) > 0) {
                return cachePdf;
            }
        } catch (IOException ex) {
            log.warn("读取评审材料预览缓存失败，materialId={}", materialId, ex);
        }

        synchronized (this) {
            try {
                if (Files.isRegularFile(cachePdf) && Files.size(cachePdf) > 0) {
                    return cachePdf;
                }
                Files.createDirectories(cachePdf.getParent());
                Path pdf = doConvert(materialId, sourceFile, sourceExt);
                try {
                    Files.move(pdf, cachePdf, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    deleteRecursively(pdf.getParent());
                }
                log.info("Office 转 PDF 成功，materialId={}, cache={}", materialId, cachePdf);
                return cachePdf;
            } catch (ServiceException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Office 转 PDF 失败，materialId={}", materialId, ex);
                throw new ServiceException("文件转换失败，请下载查看");
            }
        }
    }

    private Path doConvert(Long materialId, Path sourceFile, String sourceExt) throws IOException, InterruptedException {
        Path tempRoot = properties.resolveTempDir();
        Path workDir = tempRoot.resolve("convert-" + materialId + "-" + UUID.randomUUID()).normalize();
        Files.createDirectories(workDir);
        try {
            String ext = sourceExt == null || sourceExt.isBlank() ? "docx" : sourceExt.toLowerCase();
            Path input = workDir.resolve("source." + ext);
            Path output = workDir.resolve("source.pdf");
            Path convertLog = workDir.resolve("convert.log");
            Files.copy(sourceFile, input, StandardCopyOption.REPLACE_EXISTING);

            log.info("Office 转 PDF 开始，materialId={}, source={}", materialId, sourceFile);
            ProcessBuilder builder = new ProcessBuilder(
                    properties.getLibreofficePath(),
                    "--headless",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    workDir.toString(),
                    input.toString()
            );
            builder.redirectErrorStream(true);
            builder.redirectOutput(convertLog.toFile());
            Process process = builder.start();
            boolean finished = process.waitFor(properties.getConvertTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("Office 转 PDF 超时，materialId={}, timeoutSeconds={}",
                        materialId, properties.getConvertTimeoutSeconds());
                throw new ServiceException("文件转换超时，请下载查看");
            }
            if (process.exitValue() != 0 || !Files.isRegularFile(output)) {
                log.error("Office 转 PDF 命令失败，materialId={}, exitCode={}, output={}",
                        materialId, process.exitValue(), readConvertLog(convertLog));
                throw new ServiceException("文件转换失败，请下载查看");
            }
            return output;
        } catch (ServiceException ex) {
            deleteRecursively(workDir);
            throw ex;
        } catch (Exception ex) {
            deleteRecursively(workDir);
            throw ex;
        }
    }

    private String readConvertLog(Path convertLog) {
        try {
            if (!Files.isRegularFile(convertLog)) {
                return "";
            }
            String text = Files.readString(convertLog, StandardCharsets.UTF_8);
            return text.length() > 1000 ? text.substring(0, 1000) : text;
        } catch (Exception ex) {
            return "";
        }
    }

    private void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(item -> {
                try {
                    Files.deleteIfExists(item);
                } catch (IOException ex) {
                    log.warn("清理评审预览临时文件失败：{}", item, ex);
                }
            });
        } catch (IOException ex) {
            log.warn("清理评审预览临时目录失败：{}", path, ex);
        }
    }
}
