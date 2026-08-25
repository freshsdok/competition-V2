package com.teaching.system.service;

import cn.hutool.http.HTMLFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件任务通知富文本白名单清洗。HTMLFilter 非线程安全，每次清洗创建新实例。
 */
public final class FileTaskNotificationHtmlSanitizer {

    private static final Pattern UNQUOTED_STYLE_ATTRIBUTE = Pattern.compile(
            "\\s+style\\s*=\\s*(?![\\\"'])[^\\s>]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTED_STYLE_ATTRIBUTE = Pattern.compile(
            "\\s+style\\s*=\\s*([\\\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SAFE_LENGTH = Pattern.compile(
            "^(?:auto|0|[0-9]{1,4}(?:\\.[0-9]+)?(?:px|em|rem|%|vh|vw))$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_LINE_HEIGHT = Pattern.compile(
            "^(?:normal|[0-9]{1,2}(?:\\.[0-9]+)?|[0-9]{1,4}(?:\\.[0-9]+)?(?:px|em|rem|%))$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_COLOR = Pattern.compile(
            "^(?:#[0-9a-f]{3,8}|[a-z]{1,20}|rgba?\\([0-9.,%\\s]+\\))$", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGE_WITH_SRC = Pattern.compile(
            "<img\\b[^>]*\\bsrc\\s*=\\s*([\\\"'])([^\\\"']+)\\1[^>]*>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>", Pattern.DOTALL);
    private static final Pattern DISALLOWED_ELEMENT_WITH_CONTENT = Pattern.compile(
            "<\\s*(script|style|iframe|object|embed|form|video|audio|svg|math)\\b[^>]*>"
                    + ".*?<\\s*/\\s*\\1\\s*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private FileTaskNotificationHtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return new HTMLFilter(configuration()).filter(
                sanitizeStyleAttributes(stripDisallowedElementContent(html)));
    }

    /**
     * 可见文字或一张安全图片均视为有效；仅空标签、换行和空格实体不算内容。
     */
    public static boolean hasVisualContent(String sanitizedHtml) {
        if (sanitizedHtml == null) {
            return false;
        }
        Matcher imageMatcher = IMAGE_WITH_SRC.matcher(sanitizedHtml);
        while (imageMatcher.find()) {
            String src = imageMatcher.group(2).trim();
            // HTMLFilter 会把危险协议改写为 #...；片段本身也不是可加载图片。
            if (!src.isEmpty() && !src.startsWith("#")) {
                return true;
            }
        }
        String text = HTML_TAG.matcher(sanitizedHtml).replaceAll("")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&#xA0;", " ")
                .replace("&#xa0;", " ")
                .replace('\u00a0', ' ')
                .trim();
        return !text.isEmpty();
    }

    private static Map<String, Object> configuration() {
        Map<String, List<String>> allowed = new HashMap<>();
        List<String> styleAttribute = Collections.singletonList("style");
        for (String tag : Arrays.asList("p", "div", "span", "br", "ul", "ol", "li", "blockquote",
                "pre", "code", "strong", "b", "em", "i", "u", "s", "h1", "h2", "h3", "h4",
                "h5", "h6", "table", "thead", "tbody", "tr", "td", "th", "hr")) {
            allowed.put(tag, styleAttribute);
        }
        allowed.put("a", Arrays.asList("href", "target", "title"));
        allowed.put("img", Arrays.asList("src", "width", "height", "alt", "title", "style"));

        Map<String, Object> config = new HashMap<>();
        config.put("vAllowed", allowed);
        config.put("vSelfClosingTags", new String[]{"img", "br", "hr"});
        config.put("vNeedClosingTags", new String[]{"a", "b", "strong", "i", "em", "u", "s", "p", "div",
                "span", "ul", "ol", "li", "blockquote", "pre", "code", "h1", "h2", "h3", "h4", "h5",
                "h6", "table", "thead", "tbody", "tr", "td", "th"});
        config.put("vDisallowed", new String[]{"script", "iframe", "object", "embed", "form", "input", "button",
                "textarea", "select", "option", "svg", "math", "style", "video", "audio", "source", "track"});
        config.put("vAllowedProtocols", new String[]{"http", "https", "mailto"});
        config.put("vProtocolAtts", new String[]{"src", "href"});
        config.put("vRemoveBlanks", new String[]{"a", "b", "strong", "i", "em", "u", "s"});
        config.put("vAllowedEntities", new String[]{"amp", "gt", "lt", "quot", "nbsp"});
        config.put("stripComment", true);
        config.put("encodeQuotes", true);
        config.put("alwaysMakeTags", false);
        return config;
    }

    private static String sanitizeStyleAttributes(String html) {
        String withoutUnquotedStyle = UNQUOTED_STYLE_ATTRIBUTE.matcher(html).replaceAll("");
        Matcher matcher = QUOTED_STYLE_ATTRIBUTE.matcher(withoutUnquotedStyle);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String safeStyle = sanitizeStyleDeclarations(matcher.group(2));
            String replacement = safeStyle.isEmpty() ? "" : " style=\"" + safeStyle + "\"";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String stripDisallowedElementContent(String html) {
        String current = html;
        String previous;
        do {
            previous = current;
            current = DISALLOWED_ELEMENT_WITH_CONTENT.matcher(current).replaceAll("");
        } while (!current.equals(previous));
        return current;
    }

    private static String sanitizeStyleDeclarations(String style) {
        StringBuilder safe = new StringBuilder();
        for (String declaration : style.split(";")) {
            int colon = declaration.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String property = declaration.substring(0, colon).trim().toLowerCase();
            String value = declaration.substring(colon + 1).trim().toLowerCase();
            if (!isSafeStyle(property, value)) {
                continue;
            }
            if (safe.length() > 0) {
                safe.append(';');
            }
            safe.append(property).append(':').append(value);
        }
        return safe.toString();
    }

    private static boolean isSafeStyle(String property, String value) {
        return switch (property) {
            case "text-align" -> value.matches("left|right|center|justify|start|end");
            case "font-size", "width", "min-width", "max-width", "height", "min-height", "max-height",
                    "margin", "margin-top", "margin-right", "margin-bottom", "margin-left",
                    "padding", "padding-top", "padding-right", "padding-bottom", "padding-left" ->
                    SAFE_LENGTH.matcher(value).matches();
            case "line-height" -> SAFE_LINE_HEIGHT.matcher(value).matches();
            case "color", "background-color" -> SAFE_COLOR.matcher(value).matches();
            case "font-weight" -> value.matches("normal|bold|bolder|lighter|[1-9]00");
            case "font-style" -> value.matches("normal|italic|oblique");
            case "text-decoration" -> value.matches("none|underline|line-through|overline");
            default -> false;
        };
    }
}
