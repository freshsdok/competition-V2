package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.domain.CompetitionSceneNoticeForm;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 现场通知富文本传输解码器。
 */
final class CompetitionSceneNoticeContentCodec {

    private CompetitionSceneNoticeContentCodec() {
    }

    static String resolveContent(CompetitionSceneNoticeForm form) {
        if (form == null || StringUtils.isBlank(form.getContentBase64())) {
            return form == null ? null : form.getContent();
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(form.getContentBase64().trim());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("通知内容编码无效，请刷新页面后重试");
        }
    }
}

