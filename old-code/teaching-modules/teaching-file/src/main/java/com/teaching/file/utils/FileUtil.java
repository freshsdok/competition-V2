package com.teaching.file.utils;

import com.teaching.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class FileUtil {
    private static final String[] FONT_PATHS = {
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/simsun.ttc",
            "C:/Windows/Fonts/msyh.ttc",
            "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
            "/System/Library/Fonts/PingFang.ttc",
    };

    /**
     * 处理pdf文件
     *
     * @param inputFile
     * @param frontRemove
     * @param rearRemove
     * @param tempDir
     * @param watermark
     * @return
     * @throws IOException
     */
    public static File processPdfFile(File inputFile, int frontRemove, int rearRemove, File tempDir, String watermark) throws IOException {
        if (StringUtils.isNotBlank(watermark)) {
            return processPdfWithWatermark(inputFile, frontRemove, rearRemove, watermark, tempDir);
        }
        return processPdfFileNoWatermark(inputFile, frontRemove, rearRemove, tempDir);
    }

    /**
     * pdf文件页数处理（保留视频、附件等所有内容）
     *
     * @param inputFile   输入文件
     * @param frontRemove 删除前几页
     * @param rearRemove  删除后几页
     * @param tempDir     输出临时文件目录
     * @return 处理后的文件
     * @throws IOException
     */
    public static File processPdfFileNoWatermark(File inputFile, int frontRemove, int rearRemove, File tempDir)
            throws IOException {
        log.info("处理pdf开始");
        if (inputFile == null || !inputFile.exists()) {
            throw new IllegalArgumentException("输入文件不能为空或不存在");
        }
        if (tempDir == null || !tempDir.isDirectory()) {
            throw new IllegalArgumentException("临时目录无效");
        }
        if (frontRemove < 0 || rearRemove < 0) {
            throw new IllegalArgumentException("删除页数不能为负数");
        }
        try (PDDocument document = PDDocument.load(
                inputFile,
                MemoryUsageSetting.setupTempFileOnly().setTempDir(tempDir))) {

            if (document.isEncrypted()) {
                log.info("检测到加密PDF，正在移除安全限制...");
                document.setAllSecurityToBeRemoved(true);
            }

            int totalPages = document.getNumberOfPages();
            int totalRemove = frontRemove + rearRemove;
            log.info("开始处理PDF: {}, 总页数: {}, 删除前{}页, 删除后{}页", inputFile.getName(), totalPages, frontRemove, rearRemove);

            // 如果总页数小于等于要删除的页数，直接复制原文件返回
            if (totalPages <= totalRemove) {
                log.info("总页数({})小于等于要删除的页数({}), 不删除页面，直接复制原文件", totalPages, totalRemove);
                return copyFileToTempDir(inputFile, tempDir);
            }

            int startPage = frontRemove;
            int endPage = totalPages - rearRemove - 1;

            if (startPage > endPage || startPage < 0 || endPage >= totalPages) {
                log.info("无效的页码范围: startPage={}, endPage={}, totalPages={}", startPage, endPage, totalPages);
                throw new IllegalArgumentException("页码范围无效");
            }

            // 计算需要删除的页面索引
            java.util.Set<Integer> pagesToRemove = new java.util.HashSet<>();
            for (int i = 0; i < frontRemove; i++) {
                pagesToRemove.add(i);
            }
            for (int i = totalPages - rearRemove; i < totalPages; i++) {
                pagesToRemove.add(i);
            }

            // 从后往前删除页面，避免索引变化问题
            for (int i = totalPages - 1; i >= 0; i--) {
                if (pagesToRemove.contains(i)) {
                    document.removePage(i);
                }
            }

            File outputFile = File.createTempFile("processed_", ".pdf", tempDir);
            document.save(outputFile);
            log.info("PDF处理完成，保留页数: {}", (endPage - startPage + 1));
            return outputFile;
        }
    }


    /**
     * PDF文件页数处理并添加水印（保留视频、附件等所有内容）
     *
     * @param inputFile     输入文件
     * @param frontRemove   删除前几页
     * @param rearRemove    删除后几页
     * @param watermarkText 水印文字（支持中文）
     * @param tempDir       输出临时文件目录
     * @return 处理后的文件
     * @throws IOException
     */
    public static File processPdfWithWatermark(File inputFile, int frontRemove, int rearRemove, String watermarkText, File tempDir)
            throws IOException {
        if (inputFile == null || !inputFile.exists()) {
            throw new IllegalArgumentException("输入文件不能为空或不存在");
        }
        if (tempDir == null || !tempDir.isDirectory()) {
            throw new IllegalArgumentException("临时目录无效");
        }
        if (frontRemove < 0 || rearRemove < 0) {
            throw new IllegalArgumentException("删除页数不能为负数");
        }
        if (StringUtils.isEmpty(watermarkText)) {
            throw new IllegalArgumentException("水印文字不能为空");
        }

        try (PDDocument document = PDDocument.load(
                inputFile,
                MemoryUsageSetting.setupTempFileOnly().setTempDir(tempDir))) {

            if (document.isEncrypted()) {
                log.info("检测到加密PDF，正在移除安全限制...");
                document.setAllSecurityToBeRemoved(true);
            }

            int totalPages = document.getNumberOfPages();
            int totalRemove = frontRemove + rearRemove;
            log.info("开始处理PDF并添加水印: {}, 总页数: {}, 删除前{}页, 删除后{}页", inputFile.getName(), totalPages, frontRemove, rearRemove);

            // 如果总页数小于等于要删除的页数，不删除页面，但仍需添加水印
            if (totalPages <= totalRemove) {
                log.info("总页数({})小于等于要删除的页数({}), 不删除页面，仅添加水印", totalPages, totalRemove);
                return addWatermarkOnly(document, watermarkText, tempDir);
            }

            int startPage = frontRemove;
            int endPage = totalPages - rearRemove - 1;

            if (startPage > endPage || startPage < 0 || endPage >= totalPages) {
                log.info("无效的页码范围: startPage={}, endPage={}, totalPages={}", startPage, endPage, totalPages);
                throw new IllegalArgumentException("页码范围无效");
            }

            int pageCount = endPage - startPage + 1;
            PDFont font = loadChineseFont(document);

            // 先删除不需要的页面（从后往前删除）
            for (int i = totalPages - 1; i >= 0; i--) {
                if (i < startPage || i > endPage) {
                    document.removePage(i);
                }
            }

            // 为保留的页面添加水印
            addWatermarkToAllPages(document, watermarkText, font);

            File outputFile = File.createTempFile("processed_", ".pdf", tempDir);
            document.save(outputFile);
            log.info("PDF处理完成(带水印), 共 {} 页", pageCount);
            return outputFile;
        }
    }

    /**
     * 复制文件到临时目录
     */
    private static File copyFileToTempDir(File inputFile, File tempDir) throws IOException {
        File outputFile = File.createTempFile("processed_", ".pdf", tempDir);
        Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("文件已复制到临时目录: {}", outputFile.getAbsolutePath());
        return outputFile;
    }

    /**
     * 仅添加水印（不删除页面）
     */
    private static File addWatermarkOnly(PDDocument document, String watermarkText, File tempDir) throws IOException {
        PDFont font = loadChineseFont(document);
        addWatermarkToAllPages(document, watermarkText, font);

        File outputFile = File.createTempFile("processed_", ".pdf", tempDir);
        document.save(outputFile);
        log.info("PDF水印添加完成, 共 {} 页", document.getNumberOfPages());
        return outputFile;
    }

    /**
     * 为所有页面添加水印
     */
    private static void addWatermarkToAllPages(PDDocument document, String watermarkText, PDFont font) throws IOException {
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            PDPage page = document.getPage(i);
            PDRectangle pageSize = page.getMediaBox();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            if (StringUtils.isNotBlank(watermarkText)) {
                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    drawGridWatermarks(contentStream, watermarkText, pageWidth, pageHeight, font);
                }
            }
        }
    }

    private static PDFont loadChineseFont(PDDocument document) throws IOException {
        for (String fontPath : FONT_PATHS) {
            File fontFile = new File(fontPath);
            if (fontFile.exists()) {
                log.info("加载字体文件: {}", fontPath);
                return PDType0Font.load(document, fontFile);
            }
        }
        log.warn("未找到中文字体，使用默认字体(不支持中文)");
        return PDType1Font.HELVETICA_BOLD;
    }

    /**
     * 绘制网格状旋转的水印文字（极淡水印）
     *
     * @param contentStream
     * @param text
     * @param pageWidth
     * @param pageHeight
     * @param font
     * @throws IOException
     */
    private static void drawGridWatermarks(PDPageContentStream contentStream, String text, float pageWidth, float pageHeight, PDFont font)
            throws IOException {
        Color watermarkColor = new Color(220, 220, 220, 8);

        for (int row = -2; row <= 2; row++) {
            for (int col = -2; col <= 2; col++) {
                if (row == 0 && col == 0) continue;

                contentStream.setNonStrokingColor(watermarkColor);
                contentStream.beginText();
                contentStream.setFont(font, 12);

                float offsetX = col * (pageWidth / 4);
                float offsetY = row * (pageHeight / 3);

                contentStream.setTextMatrix(
                        java.awt.geom.AffineTransform.getRotateInstance(Math.toRadians(-30),
                                pageWidth / 2 + offsetX, pageHeight / 2 + offsetY));

                float smallTextWidth = font.getStringWidth(text) / 1000f * 12;
                contentStream.newLineAtOffset((pageWidth - smallTextWidth) / 2 + offsetX, pageHeight / 2 + offsetY);
                contentStream.showText(text);
                contentStream.endText();
            }
        }
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return ".pdf";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return ".pdf";
    }

    /**
     * 提取URL中的文件名
     *
     * @param url
     * @return
     */
    public static String extractFileNameFromUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }
        String path = url;
        if (url.contains("?")) {
            path = url.substring(0, url.indexOf("?"));
        }
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * 删除目录下所有文件
     *
     * @param directory
     */
    public static void cleanupDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanupDirectory(file);
                    } else {
                        if (!file.delete()) {
                            log.warn("删除临时文件失败: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!directory.delete()) {
                log.warn("删除临时目录失败: {}", directory.getAbsolutePath());
            }
            log.info("清理临时目录完成: {}", directory.getAbsolutePath());
        } catch (Exception e) {
            log.warn("清理临时目录异常: {}", directory.getAbsolutePath(), e);
        }
    }

    public static void main(String[] args) throws IOException {
        File tempDir = new File("D:/test/processed/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        processPdfFile(new File("D:/test/333.pdf"), 1, 1, tempDir, "我是水印");
    }

}
