package com.teaching.system.service;

import org.junit.Assert;
import org.junit.Test;

public class FileTaskNotificationHtmlSanitizerTest {

    @Test
    public void shouldKeepChineseListsLinksAndSafeImages() {
        String input = "<p style=\"text-align:center\">中文正文</p>"
                + "<ul><li>事项一</li></ul>"
                + "<a href=\"https://example.com/a\">说明</a>"
                + "<img src=\"https://static.example.com/a.png\" alt=\"图\">";

        String cleaned = FileTaskNotificationHtmlSanitizer.sanitize(input);

        Assert.assertTrue(cleaned.contains("中文正文"));
        Assert.assertTrue(cleaned.contains("<li>事项一</li>"));
        Assert.assertTrue(cleaned.contains("https://example.com/a"));
        Assert.assertTrue(cleaned.contains("https://static.example.com/a.png"));
        Assert.assertTrue(FileTaskNotificationHtmlSanitizer.hasVisualContent(cleaned));
    }

    @Test
    public void shouldRemoveScriptsEventsDangerousUrlsAndEmbeddedMedia() {
        String input = "<p onclick=\"alert(1)\">安全正文</p>"
                + "<script>alert(1)</script>"
                + "<iframe src=\"https://evil.example\"></iframe>"
                + "<video src=\"https://evil.example/a.mp4\"></video>"
                + "<a href=\"javascript:alert(1)\">链接</a>"
                + "<img src=\"javascript:alert(1)\" onerror=\"alert(1)\">";

        String cleaned = FileTaskNotificationHtmlSanitizer.sanitize(input);
        String lower = cleaned.toLowerCase();

        Assert.assertTrue(cleaned.contains("安全正文"));
        Assert.assertFalse(lower.contains("<script"));
        Assert.assertFalse(lower.contains("<iframe"));
        Assert.assertFalse(lower.contains("<video"));
        Assert.assertFalse(lower.contains("onclick"));
        Assert.assertFalse(lower.contains("onerror"));
        Assert.assertFalse(lower.contains("javascript:"));
    }

    @Test
    public void shouldRejectVisuallyEmptyContentAndUnsafeImageOnlyContent() {
        Assert.assertFalse(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<p><br></p>&nbsp;")));
        Assert.assertFalse(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<video src=\"https://example.com/a.mp4\"></video>")));
        Assert.assertFalse(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<script>alert(1)</script>")));
        Assert.assertFalse(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<img src=\"javascript:alert(1)\">")));
        Assert.assertTrue(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<img src=\"https://example.com/a.png\">")));
        Assert.assertTrue(FileTaskNotificationHtmlSanitizer.hasVisualContent(
                FileTaskNotificationHtmlSanitizer.sanitize("<img src=\"/profile/a.png\">")));
    }

    @Test
    public void shouldKeepOnlySafeLayoutStyles() {
        String input = "<p style=\"text-align:center;font-size:18px;"
                + "position:fixed;background-image:url(javascript:alert(1))\">正文</p>";

        String cleaned = FileTaskNotificationHtmlSanitizer.sanitize(input).toLowerCase();

        Assert.assertTrue(cleaned.contains("text-align:center"));
        Assert.assertTrue(cleaned.contains("font-size:18px"));
        Assert.assertFalse(cleaned.contains("position"));
        Assert.assertFalse(cleaned.contains("background-image"));
        Assert.assertFalse(cleaned.contains("javascript:"));
    }
}
