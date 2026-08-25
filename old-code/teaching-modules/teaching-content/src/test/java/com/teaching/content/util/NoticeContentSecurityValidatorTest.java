package com.teaching.content.util;

import com.teaching.common.core.exception.ServiceException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NoticeContentSecurityValidatorTest {

    @Test
    public void shouldRejectDangerousSchemesAndLocalPathsInRichText() {
        List<String> unsafeContents = List.of(
                "<img src=\"file:///C:/Users/alice/Desktop/secret.png\">",
                "<a href=\"javascript:alert(1)\">link</a>",
                "<a href=\"java&#x73;cript:alert(1)\">encoded link</a>",
                "<a href=\"java%73cript:alert(1)\">percent encoded link</a>",
                "<p>C:\\Users\\alice\\Desktop\\secret.docx</p>",
                "<p>\\\\fileserver\\share\\secret.docx</p>",
                "<p>/Users/alice/Desktop/secret.docx</p>",
                "<p>/etc/passwd</p>"
        );

        for (String content : unsafeContents) {
            Assert.assertThrows(ServiceException.class,
                    () -> NoticeContentSecurityValidator.validateRichText(content));
        }
    }

    @Test
    public void shouldRejectActiveHtmlAndEventHandlers() {
        List<String> unsafeContents = List.of(
                "<script>alert(1)</script>",
                "<iframe src=\"https://evil.example\"></iframe>",
                "<img src=\"https://static.example/a.png\" onerror=\"alert(1)\">",
                "<p style=\"background-image:url(javascript:alert(1))\">text</p>"
        );

        for (String content : unsafeContents) {
            Assert.assertThrows(ServiceException.class,
                    () -> NoticeContentSecurityValidator.validateRichText(content));
        }
    }

    @Test
    public void shouldRejectDangerousOrUncontrolledCoverUrls() {
        List<String> unsafeUrls = List.of(
                "file:///Users/alice/Desktop/cover.png",
                "javascript:alert(1)",
                "C:\\Users\\alice\\cover.png",
                "\\\\fileserver\\share\\cover.png",
                "/etc/cover.png",
                "../private/cover.png",
                "ftp://files.example/cover.png"
        );

        for (String url : unsafeUrls) {
            Assert.assertThrows(ServiceException.class,
                    () -> NoticeContentSecurityValidator.validateResourceUrl(url));
        }
    }

    @Test
    public void shouldKeepNormalRichTextAndWebResourcesUnchanged() {
        String richText = "<h2 style=\"text-align:center\">公告标题</h2>"
                + "<p><strong>正文</strong><br>说明</p>"
                + "<img src=\"https://static.example/notice/a.png\">"
                + "<a href=\"/profile/upload/notice.pdf\">附件</a>";

        NoticeContentSecurityValidator.validateRichText(richText);
        NoticeContentSecurityValidator.validateResourceUrl("https://static.example/notice/cover.png");
        NoticeContentSecurityValidator.validateResourceUrl("http://static.example/notice/cover.png");
        NoticeContentSecurityValidator.validateResourceUrl("/profile/upload/cover.png");
        NoticeContentSecurityValidator.validateResourceUrl("uploads/cover.png");
    }
}
