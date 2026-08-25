package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.domain.CompetitionSceneNoticeForm;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 富文本Base64传输兼容测试。
 */
public class CompetitionSceneNoticeContentCodecTest {

    @Test
    public void shouldDecodeUtf8RichTextWithoutDamagingJsonSensitiveCharacters() {
        String html = "<p style=\"text-align: center;\"><span style=\"font-size: 19px;\">"
                + "<strong>大家好</strong></span></p><p style=\"text-align: center;\">"
                + "<img src=\"http://localhost:9300/statics/2026/07/10/IMG_4229.PNG\" "
                + "alt=\"\" data-href=\"\" style=\"width: 30%;\"></p>";
        CompetitionSceneNoticeForm form = new CompetitionSceneNoticeForm();
        form.setContentBase64(Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8)));

        Assert.assertEquals(html, CompetitionSceneNoticeContentCodec.resolveContent(form));
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectMalformedBase64() {
        CompetitionSceneNoticeForm form = new CompetitionSceneNoticeForm();
        form.setContentBase64("not-a-valid-base64***");

        CompetitionSceneNoticeContentCodec.resolveContent(form);
    }

    @Test
    public void shouldRemainCompatibleWithPlainContentForDirectRequests() {
        CompetitionSceneNoticeForm form = new CompetitionSceneNoticeForm();
        form.setContent("<p>普通请求</p>");

        Assert.assertEquals("<p>普通请求</p>", CompetitionSceneNoticeContentCodec.resolveContent(form));
    }
}

