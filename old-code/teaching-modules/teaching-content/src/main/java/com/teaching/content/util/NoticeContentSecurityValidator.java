package com.teaching.content.util;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.content.domain.NoticeInfo;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知公告正文和封面地址的服务端安全校验。
 *
 * <p>校验采用拒绝策略，不改写合法富文本，避免静默改变编辑人员提交的正文。</p>
 */
public final class NoticeContentSecurityValidator {

    private static final Pattern NUMERIC_HTML_ENTITY = Pattern.compile(
            "&#(?:[xX]([0-9a-fA-F]{1,6})|([0-9]{1,7}));?");
    private static final Pattern PERCENT_ENCODED_BYTE = Pattern.compile("%([0-9a-fA-F]{2})");
    private static final Pattern INVISIBLE_CHARACTERS = Pattern.compile(
            "[\\u0000-\\u001f\\u007f\\u200b-\\u200f\\u2028\\u2029\\u2060\\ufeff]");
    private static final Pattern DANGEROUS_SCHEME = Pattern.compile(
            "(?:^|[^a-z0-9+.-])(?:file|javascript|vbscript):");
    private static final Pattern DANGEROUS_DATA_URI = Pattern.compile(
            "(?:^|[^a-z0-9+.-])data\\s*:\\s*(?:text/html|image/svg\\+xml|application/xhtml\\+xml)");
    private static final Pattern WINDOWS_ABSOLUTE_PATH = Pattern.compile(
            "(?i)(?:^|[\\s\"'=(:;>])(?:[a-z]:[\\\\/])");
    private static final Pattern UNC_PATH = Pattern.compile(
            "(?:^|[\\s\"'=(:;>])\\\\\\\\[^\\\\\\s/]+[\\\\/]");
    private static final Pattern UNIX_LOCAL_PATH = Pattern.compile(
            "(?i)(?:^|[\\s\"'=(:;>])/(?:users|home|root|etc|var|tmp|private|volumes|opt|usr|proc|sys|dev|library)(?:/|\\b)");
    private static final Pattern ACTIVE_HTML_TAG = Pattern.compile(
            "(?i)<\\s*(?:script|iframe|object|embed|base|meta|link|form|input|button|textarea|select|option|svg|math|style)\\b");
    private static final Pattern EVENT_HANDLER_ATTRIBUTE = Pattern.compile(
            "(?i)\\son[a-z0-9_-]+\\s*=");
    private static final Pattern DANGEROUS_CSS = Pattern.compile(
            "(?i)(?:expression\\s*\\(|url\\s*\\(\\s*[\"']?\\s*(?:javascript|file)\\s*:|behavior\\s*:|-moz-binding\\s*:)");
    private static final Pattern EXPLICIT_SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");
    private static final Pattern SAFE_ROOTED_RESOURCE_PATH = Pattern.compile(
            "^/(?:profile|upload|uploads|file|files|resource|resources|static|assets|prod-api)(?:/|$)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_RELATIVE_RESOURCE_PATH = Pattern.compile(
            "^(?:\\./)?(?:profile|upload|uploads|file|files|resource|resources|static|assets)(?:/|$)",
            Pattern.CASE_INSENSITIVE);

    private NoticeContentSecurityValidator() {
    }

    /**
     * 校验通知公告所有可能包含资源引用的字段。
     *
     * @param noticeInfo 待新增或更新的通知公告
     */
    public static void validate(NoticeInfo noticeInfo) {
        if (noticeInfo == null) {
            return;
        }
        validateRichText(noticeInfo.getNoticeContent());
        validateResourceUrl(noticeInfo.getNoticeImage());
    }

    /**
     * 校验富文本正文，保留合法 HTML 原文。
     *
     * @param content 富文本正文
     */
    public static void validateRichText(String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        String normalized = normalize(content);
        rejectDangerousReference(normalized, "公告内容");
        if (ACTIVE_HTML_TAG.matcher(normalized).find()
                || EVENT_HANDLER_ATTRIBUTE.matcher(normalized).find()
                || DANGEROUS_CSS.matcher(normalized).find()) {
            throw new ServiceException("公告内容包含不安全的 HTML");
        }
    }

    /**
     * 校验封面资源地址。允许 HTTP(S) 以及系统受控的相对资源目录。
     *
     * @param resourceUrl 封面资源地址
     */
    public static void validateResourceUrl(String resourceUrl) {
        if (resourceUrl == null || resourceUrl.isBlank()) {
            return;
        }
        String value = resourceUrl.trim();
        String normalized = normalize(value);
        rejectDangerousReference(normalized, "公告图片地址");

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return;
        }
        if (normalized.startsWith("//")
                || normalized.startsWith("\\\\")
                || normalized.startsWith("../")
                || normalized.contains("/../")
                || EXPLICIT_SCHEME.matcher(normalized).find()) {
            throw new ServiceException("公告图片地址仅支持 HTTP(S) 或受控相对资源路径");
        }
        if (SAFE_ROOTED_RESOURCE_PATH.matcher(normalized).find()
                || SAFE_RELATIVE_RESOURCE_PATH.matcher(normalized).find()) {
            return;
        }
        throw new ServiceException("公告图片地址仅支持 HTTP(S) 或受控相对资源路径");
    }

    private static void rejectDangerousReference(String normalized, String fieldName) {
        if (DANGEROUS_SCHEME.matcher(normalized).find()
                || DANGEROUS_DATA_URI.matcher(normalized).find()
                || WINDOWS_ABSOLUTE_PATH.matcher(normalized).find()
                || UNC_PATH.matcher(normalized).find()
                || UNIX_LOCAL_PATH.matcher(normalized).find()) {
            throw new ServiceException(fieldName + "包含不安全的本地路径或脚本链接");
        }
    }

    private static String normalize(String value) {
        String normalized = value;
        for (int i = 0; i < 3; i++) {
            String decoded = decodeNamedHtmlEntities(decodeNumericHtmlEntities(normalized));
            decoded = decodePercentEncodedAscii(decoded);
            if (decoded.equals(normalized)) {
                break;
            }
            normalized = decoded;
        }
        normalized = INVISIBLE_CHARACTERS.matcher(normalized).replaceAll("");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String decodeNamedHtmlEntities(String value) {
        return value
                .replaceAll("(?i)&amp;", "&")
                .replaceAll("(?i)&colon;", ":")
                .replaceAll("(?i)&sol;", "/")
                .replaceAll("(?i)&bsol;", Matcher.quoteReplacement("\\"))
                .replaceAll("(?i)&tab;", "\t")
                .replaceAll("(?i)&newline;", "\n");
    }

    private static String decodeNumericHtmlEntities(String value) {
        Matcher matcher = NUMERIC_HTML_ENTITY.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String digits = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            int radix = matcher.group(1) != null ? 16 : 10;
            String replacement = matcher.group();
            try {
                int codePoint = Integer.parseInt(digits, radix);
                if (Character.isValidCodePoint(codePoint)) {
                    replacement = new String(Character.toChars(codePoint));
                }
            } catch (IllegalArgumentException ignored) {
                // 无效实体保留原样，后续 HTML 渲染也无法将其变成有效字符。
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String decodePercentEncodedAscii(String value) {
        Matcher matcher = PERCENT_ENCODED_BYTE.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            int decoded = Integer.parseInt(matcher.group(1), 16);
            String replacement = decoded <= 0x7f
                    ? String.valueOf((char) decoded)
                    : matcher.group();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
