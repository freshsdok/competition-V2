package com.teaching.competition.review.service.impl;

import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.review.config.ReviewPreviewProperties;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.service.IReviewFilePreviewService;
import com.teaching.competition.review.vo.ReviewMaterialPreviewVO;
import com.teaching.competition.review.vo.ReviewPreviewResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 评审材料在线预览服务实现。
 */
@Service
public class ReviewFilePreviewServiceImpl implements IReviewFilePreviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewFilePreviewServiceImpl.class);
    private static final String MATERIAL_STATUS_NORMAL = "NORMAL";
    private static final String PREVIEW_PDF = "pdf";
    private static final String PREVIEW_IMAGE = "image";
    private static final String PREVIEW_TEXT = "text";
    private static final String PREVIEW_UNSUPPORTED = "unsupported";
    private static final Set<String> PDF_TYPES = Set.of("pdf");
    private static final Set<String> IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final Set<String> TEXT_TYPES = Set.of("txt", "md", "json", "csv", "log");
    private static final Set<String> OFFICE_TYPES = Set.of("doc", "docx", "ppt", "pptx", "xls", "xlsx");
    private static final Map<String, String> IMAGE_MIME = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "bmp", "image/bmp",
            "webp", "image/webp"
    );

    @Autowired
    private ReviewPreviewProperties properties;

    @Autowired
    private ReviewObjectMaterialMapper materialMapper;

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewAssignmentMapper assignmentMapper;

    @Autowired
    private OfficeToPdfConvertService officeToPdfConvertService;

    @Value("${file.path:}")
    private String localFilePath;

    @Value("${file.prefix:/profile}")
    private String localFilePrefix;

    @Override
    public ReviewMaterialPreviewVO preview(Long fileId) {
        ReviewObjectMaterial material = requireAccessibleMaterial(fileId, "preview");
        String ext = resolveFileExt(material);
        String previewType = resolvePreviewType(ext);
        boolean officePreview = OFFICE_TYPES.contains(ext);

        ReviewMaterialPreviewVO vo = new ReviewMaterialPreviewVO();
        vo.setFileId(material.getId());
        vo.setFileName(resolveFileName(material));
        vo.setFileType(ext);
        vo.setPreviewType(properties.isEnabled() ? previewType : PREVIEW_UNSUPPORTED);
        vo.setPreviewUrl("/review/material/preview-stream/" + material.getId());
        vo.setDownloadUrl("/review/material/download/" + material.getId());
        vo.setConverted(officePreview && Files.isRegularFile(cachePdfPath(material)));
        if (!properties.isEnabled()) {
            vo.setMessage("在线预览功能未启用，请下载查看");
        } else if (PREVIEW_UNSUPPORTED.equals(previewType)) {
            log.info("评审材料文件类型不支持预览，materialId={}, ext={}", material.getId(), ext);
            vo.setMessage("当前文件暂不支持在线预览，请下载查看");
        }
        log.info("文件预览请求，materialId={}, fileName={}, previewType={}",
                material.getId(), vo.getFileName(), vo.getPreviewType());
        return vo;
    }

    @Override
    public ReviewPreviewResource previewStream(Long fileId) {
        ReviewObjectMaterial material = requireAccessibleMaterial(fileId, "preview-stream");
        if (!properties.isEnabled()) {
            throw new ServiceException("在线预览功能未启用，请下载查看", HttpStatus.UNSUPPORTED_TYPE);
        }
        String ext = resolveFileExt(material);
        String previewType = resolvePreviewType(ext);
        if (PREVIEW_UNSUPPORTED.equals(previewType)) {
            log.info("评审材料文件类型不支持预览，materialId={}, ext={}", material.getId(), ext);
            throw new ServiceException("当前文件暂不支持在线预览，请下载查看", HttpStatus.UNSUPPORTED_TYPE);
        }

        if (PREVIEW_TEXT.equals(previewType)) {
            return buildTextResource(material);
        }
        if (OFFICE_TYPES.contains(ext)) {
            return buildOfficePdfResource(material, ext);
        }

        SourceFile source = resolveSourceFile(material);
        try {
            if (PREVIEW_PDF.equals(previewType)) {
                return ReviewPreviewResource.fromPath(source.getPath(), withPdfExt(resolveFileName(material)),
                        "application/pdf", source.isTemporary());
            }
            String contentType = firstNotEmpty(material.getMimeType(), IMAGE_MIME.get(ext), "application/octet-stream");
            return ReviewPreviewResource.fromPath(source.getPath(), resolveFileName(material), contentType, source.isTemporary());
        } catch (IOException ex) {
            source.closeQuietly();
            throw new ServiceException("读取材料文件失败，请下载查看");
        }
    }

    @Override
    public ReviewPreviewResource download(Long fileId) {
        ReviewObjectMaterial material = requireAccessibleMaterial(fileId, "download");
        SourceFile source = resolveSourceFile(material);
        try {
            return ReviewPreviewResource.fromPath(source.getPath(), resolveFileName(material),
                    "application/octet-stream", source.isTemporary());
        } catch (IOException ex) {
            source.closeQuietly();
            throw new ServiceException("读取材料文件失败");
        }
    }

    private ReviewPreviewResource buildTextResource(ReviewObjectMaterial material) {
        SourceFile source = resolveSourceFile(material);
        try {
            long maxSize = Math.max(1L, properties.getMaxTextPreviewSize());
            long fileSize = Files.size(source.getPath());
            int readSize = (int) Math.min(maxSize, Integer.MAX_VALUE);
            byte[] bytes;
            try (InputStream input = Files.newInputStream(source.getPath())) {
                bytes = input.readNBytes(readSize);
            }
            if (fileSize > maxSize) {
                ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length + 128);
                output.write(bytes);
                output.write(("\n\n--- 文本文件较大，仅展示前 " + maxSize + " 字节，请下载查看完整内容。")
                        .getBytes(StandardCharsets.UTF_8));
                bytes = output.toByteArray();
            }
            return ReviewPreviewResource.fromBytes(bytes, resolveFileName(material), "text/plain;charset=UTF-8");
        } catch (IOException ex) {
            throw new ServiceException("读取文本材料失败，请下载查看");
        } finally {
            source.closeQuietly();
        }
    }

    private ReviewPreviewResource buildOfficePdfResource(ReviewObjectMaterial material, String ext) {
        SourceFile source = resolveSourceFile(material);
        try {
            Path pdf = officeToPdfConvertService.convertToPdf(material.getId(), source.getPath(), ext, cachePdfPath(material));
            return ReviewPreviewResource.fromPath(pdf, withPdfExt(resolveFileName(material)), "application/pdf", false);
        } catch (IOException ex) {
            throw new ServiceException("读取转换后的 PDF 失败，请下载查看");
        } finally {
            source.closeQuietly();
        }
    }

    private ReviewObjectMaterial requireAccessibleMaterial(Long fileId, String action) {
        if (fileId == null) {
            throw new ServiceException("材料文件ID不能为空");
        }
        Long userId = currentUserId();
        if (userId == null) {
            log.warn("权限校验失败：用户未登录，action={}, materialId={}", action, fileId);
            throw new ServiceException("用户未登录或登录已失效", HttpStatus.UNAUTHORIZED);
        }

        ReviewObjectMaterial material = materialMapper.selectById(fileId);
        if (material == null) {
            throw new ServiceException("评审材料不存在或已删除", HttpStatus.NOT_FOUND);
        }
        if (!ReviewConstants.YES.equals(material.getVisibleToReviewer())
                || (!StringUtils.isEmpty(material.getStatus()) && !MATERIAL_STATUS_NORMAL.equals(material.getStatus()))) {
            log.warn("权限校验失败：材料不可见，action={}, userId={}, materialId={}", action, userId, fileId);
            throw new ServiceException("无权限查看该评审材料", HttpStatus.FORBIDDEN);
        }

        ReviewObject object = objectMapper.selectById(material.getObjectId());
        if (object == null || !Objects.equals(object.getActivityId(), material.getActivityId())) {
            throw new ServiceException("评审材料所属对象不存在", HttpStatus.NOT_FOUND);
        }

        ReviewAssignment query = new ReviewAssignment();
        query.setReviewerUserId(userId);
        query.setActivityId(material.getActivityId());
        query.setObjectId(material.getObjectId());
        List<ReviewAssignment> assignments = assignmentMapper.selectList(query);
        boolean assigned = assignments != null && assignments.stream()
                .anyMatch(item -> !ReviewAssignmentStatus.CANCELLED.getCode().equals(item.getStatus()));
        if (!assigned) {
            log.warn("权限校验失败：用户未分配到评审对象，action={}, userId={}, materialId={}, objectId={}",
                    action, userId, fileId, material.getObjectId());
            throw new ServiceException("无权限查看该评审材料", HttpStatus.FORBIDDEN);
        }
        return material;
    }

    private SourceFile resolveSourceFile(ReviewObjectMaterial material) {
        String fileUrl = material.getFileUrl();
        if (StringUtils.isEmpty(fileUrl)) {
            throw new ServiceException("材料文件地址为空", HttpStatus.NOT_FOUND);
        }
        String trimmed = fileUrl.trim();
        if (isHttpUrl(trimmed)) {
            SourceFile local = tryResolveLocalUrl(trimmed);
            if (local != null && Files.isRegularFile(local.getPath())) {
                return local;
            }
            return downloadRemoteFile(material, trimmed);
        }
        return resolveLocalFile(material, trimmed);
    }

    private SourceFile resolveLocalFile(ReviewObjectMaterial material, String fileUrl) {
        String pathPart = removeQueryAndFragment(fileUrl);
        if (pathPart.contains("..")) {
            throw new ServiceException("材料文件路径不合法", HttpStatus.FORBIDDEN);
        }

        Path base = localBasePath();
        Path resolved;
        String matchedPrefix = findLocalFilePrefix(pathPart);
        if (matchedPrefix != null) {
            String relative = pathPart.substring(pathPart.indexOf(matchedPrefix) + matchedPrefix.length());
            resolved = resolveUnderBase(base, relative);
        } else {
            Path candidate = Path.of(pathPart).toAbsolutePath().normalize();
            if (StringUtils.isEmpty(localFilePath) || !candidate.startsWith(base)) {
                throw new ServiceException("材料文件路径不合法", HttpStatus.FORBIDDEN);
            }
            resolved = candidate;
        }

        if (!Files.isRegularFile(resolved)) {
            log.warn("评审材料文件不存在，materialId={}, path={}", material.getId(), resolved);
            throw new ServiceException("材料文件不存在，请联系管理员", HttpStatus.NOT_FOUND);
        }
        return new SourceFile(resolved, false);
    }

    private SourceFile tryResolveLocalUrl(String fileUrl) {
        if (StringUtils.isEmpty(localFilePath)) {
            return null;
        }
        try {
            String path = pathFromHttpUrl(fileUrl);
            String matchedPrefix = findLocalFilePrefix(path);
            if (StringUtils.isEmpty(path) || matchedPrefix == null) {
                return null;
            }
            String relative = path.substring(path.indexOf(matchedPrefix) + matchedPrefix.length());
            return new SourceFile(resolveUnderBase(localBasePath(), relative), false);
        } catch (Exception ex) {
            return null;
        }
    }

    private SourceFile downloadRemoteFile(ReviewObjectMaterial material, String fileUrl) {
        try {
            Path tempRoot = properties.resolveTempDir();
            Path workDir = tempRoot.resolve("source-" + material.getId() + "-" + UUID.randomUUID()).normalize();
            Files.createDirectories(workDir);
            String ext = resolveFileExt(material);
            String safeExt = StringUtils.isEmpty(ext) ? "bin" : ext;
            Path target = workDir.resolve("source." + safeExt);

            URLConnection connection = new URL(fileUrl).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            try (InputStream input = connection.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new SourceFile(target, true);
        } catch (IOException ex) {
            log.error("下载评审材料源文件失败，materialId={}, url={}", material.getId(), fileUrl, ex);
            throw new ServiceException("材料文件读取失败，请下载查看");
        }
    }

    private Path resolveUnderBase(Path base, String relative) {
        String decoded = URLDecoder.decode(firstNotEmpty(relative, ""), StandardCharsets.UTF_8);
        while (decoded.startsWith("/") || decoded.startsWith("\\")) {
            decoded = decoded.substring(1);
        }
        if (decoded.contains("..")) {
            throw new ServiceException("材料文件路径不合法", HttpStatus.FORBIDDEN);
        }
        Path resolved = base.resolve(decoded).normalize();
        if (!resolved.startsWith(base)) {
            throw new ServiceException("材料文件路径不合法", HttpStatus.FORBIDDEN);
        }
        return resolved;
    }

    private Path localBasePath() {
        if (StringUtils.isEmpty(localFilePath)) {
            throw new ServiceException("文件存储路径未配置");
        }
        return Path.of(localFilePath).toAbsolutePath().normalize();
    }

    private Path cachePdfPath(ReviewObjectMaterial material) {
        return properties.resolveCacheDir().resolve("review-material-" + material.getId() + ".pdf");
    }

    private String findLocalFilePrefix(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }
        for (String prefix : localFilePrefixes()) {
            if (filePath.contains(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private Set<String> localFilePrefixes() {
        LinkedHashSet<String> prefixes = new LinkedHashSet<>();
        addLocalFilePrefix(prefixes, localFilePrefix);
        addLocalFilePrefix(prefixes, "/profile");
        addLocalFilePrefix(prefixes, "/statics");
        return prefixes;
    }

    private void addLocalFilePrefix(Set<String> prefixes, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return;
        }
        String value = prefix.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.endsWith("/") && value.length() > 1) {
            value = value.substring(0, value.length() - 1);
        }
        prefixes.add(value);
    }

    private String resolvePreviewType(String ext) {
        if (PDF_TYPES.contains(ext) || OFFICE_TYPES.contains(ext)) {
            return PREVIEW_PDF;
        }
        if (IMAGE_TYPES.contains(ext)) {
            return PREVIEW_IMAGE;
        }
        if (TEXT_TYPES.contains(ext)) {
            return PREVIEW_TEXT;
        }
        return PREVIEW_UNSUPPORTED;
    }

    private String resolveFileExt(ReviewObjectMaterial material) {
        String ext = normalizeExt(material.getFileExt());
        if (StringUtils.isNotEmpty(ext)) {
            return ext;
        }
        ext = extFromName(material.getFileName());
        if (StringUtils.isNotEmpty(ext)) {
            return ext;
        }
        return extFromName(removeQueryAndFragment(material.getFileUrl()));
    }

    private String normalizeExt(String ext) {
        if (StringUtils.isEmpty(ext)) {
            return "";
        }
        String value = ext.trim().toLowerCase(Locale.ROOT);
        return value.startsWith(".") ? value.substring(1) : value;
    }

    private String extFromName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return normalizeExt(fileName.substring(index + 1));
    }

    private String resolveFileName(ReviewObjectMaterial material) {
        return firstNotEmpty(material.getFileName(), material.getMaterialName(), "评审材料-" + material.getId());
    }

    private String withPdfExt(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "preview.pdf";
        }
        int index = fileName.lastIndexOf('.');
        return (index > 0 ? fileName.substring(0, index) : fileName) + ".pdf";
    }

    private String removeQueryAndFragment(String value) {
        if (value == null) {
            return "";
        }
        int queryIndex = value.indexOf('?');
        int fragmentIndex = value.indexOf('#');
        int end = value.length();
        if (queryIndex >= 0) {
            end = Math.min(end, queryIndex);
        }
        if (fragmentIndex >= 0) {
            end = Math.min(end, fragmentIndex);
        }
        return value.substring(0, end);
    }

    private boolean isHttpUrl(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private String pathFromHttpUrl(String value) {
        String clean = removeQueryAndFragment(value);
        int schemeIndex = clean.indexOf("://");
        if (schemeIndex < 0) {
            return clean;
        }
        int pathIndex = clean.indexOf('/', schemeIndex + 3);
        return pathIndex < 0 ? "" : clean.substring(pathIndex);
    }

    private Long currentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    private String firstNotEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    private static class SourceFile {
        private final Path path;
        private final boolean temporary;

        private SourceFile(Path path, boolean temporary) {
            this.path = path;
            this.temporary = temporary;
        }

        private Path getPath() {
            return path;
        }

        private boolean isTemporary() {
            return temporary;
        }

        private void closeQuietly() {
            if (!temporary || path == null) {
                return;
            }
            Path parent = path.getParent();
            try {
                if (parent != null && Files.exists(parent)) {
                    try (java.util.stream.Stream<Path> stream = Files.walk(parent)) {
                        stream.sorted(Comparator.reverseOrder()).forEach(item -> {
                            try {
                                Files.deleteIfExists(item);
                            } catch (IOException ex) {
                                log.warn("清理评审材料预览临时文件失败：{}", item, ex);
                            }
                        });
                    }
                } else {
                    Files.deleteIfExists(path);
                }
            } catch (IOException ex) {
                log.warn("清理评审材料预览临时目录失败：{}", parent, ex);
            }
        }
    }
}
