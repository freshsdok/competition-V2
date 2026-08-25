package com.teaching.competition.service.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * 现场通知富文本白名单测试。
 */
public class CompetitionSceneNoticeHtmlSanitizerTest {

    @Test
    public void shouldRemoveScriptsEventsFramesAndDangerousProtocols() {
        String input = "<p onclick=\"alert(1)\">安全正文</p>"
                + "<script>alert('xss')</script>"
                + "<iframe src=\"https://evil.example\"></iframe>"
                + "<a href=\"javascript:alert(1)\">危险链接</a>"
                + "<img src=\"https://static.example/a.png\" onerror=\"alert(1)\">";

        String cleaned = CompetitionSceneNoticeHtmlSanitizer.sanitize(input);
        String lower = cleaned.toLowerCase();

        Assert.assertTrue(cleaned.contains("安全正文"));
        Assert.assertTrue(cleaned.contains("https://static.example/a.png"));
        Assert.assertFalse(lower.contains("<script"));
        Assert.assertFalse(lower.contains("<iframe"));
        Assert.assertFalse(lower.contains("onclick"));
        Assert.assertFalse(lower.contains("onerror"));
        Assert.assertFalse(lower.contains("javascript:"));
    }

    @Test
    public void shouldKeepCommonRichTextTags() {
        String input = "<h3>标题</h3><p><strong>重点</strong><br>正文</p><ul><li>事项一</li></ul>";

        String cleaned = CompetitionSceneNoticeHtmlSanitizer.sanitize(input);

        Assert.assertTrue(cleaned.contains("<h3>标题</h3>"));
        Assert.assertTrue(cleaned.contains("<strong>重点</strong>"));
        Assert.assertTrue(cleaned.contains("<li>事项一</li>"));
    }

    @Test
    public void shouldKeepSafeLayoutStylesAndRemoveDangerousCss() {
        String input = "<p style=\"text-align: center; font-size: 19px; "
                + "background-image: url(javascript:alert(1)); position: fixed;\">正文</p>"
                + "<img src=\"https://static.example/a.png\" style=\"width: 30%; expression(alert(1))\">";

        String cleaned = CompetitionSceneNoticeHtmlSanitizer.sanitize(input);
        String lower = cleaned.toLowerCase();

        Assert.assertTrue(lower.contains("text-align:center"));
        Assert.assertTrue(lower.contains("font-size:19px"));
        Assert.assertTrue(lower.contains("width:30%"));
        Assert.assertFalse(lower.contains("background-image"));
        Assert.assertFalse(lower.contains("position"));
        Assert.assertFalse(lower.contains("javascript:"));
        Assert.assertFalse(lower.contains("expression"));
    }
}
