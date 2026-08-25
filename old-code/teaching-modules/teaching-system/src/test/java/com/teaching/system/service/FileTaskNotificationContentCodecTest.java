package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class FileTaskNotificationContentCodecTest {

    @Test
    public void shouldDecodeUtf8ChineseRichText() {
        String html = "<p>上传提醒：请按时提交</p>";
        String encoded = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));

        Assert.assertEquals(html, FileTaskNotificationContentCodec.decodeUtf8Base64(encoded));
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectInvalidBase64() {
        FileTaskNotificationContentCodec.decodeUtf8Base64("%%%not-base64%%%");
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectMalformedUtf8() {
        String encoded = Base64.getEncoder().encodeToString(new byte[]{(byte) 0xC3, (byte) 0x28});
        FileTaskNotificationContentCodec.decodeUtf8Base64(encoded);
    }
}
