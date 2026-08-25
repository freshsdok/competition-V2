package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 文件任务通知富文本传输解码。
 */
public final class FileTaskNotificationContentCodec {

    private FileTaskNotificationContentCodec() {
    }

    public static String decodeUtf8Base64(String contentBase64) {
        if (StringUtils.isBlank(contentBase64)) {
            throw new ServiceException("通知内容不能为空");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(contentBase64.trim());
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (IllegalArgumentException | CharacterCodingException exception) {
            throw new ServiceException("通知内容编码无效");
        }
    }
}
