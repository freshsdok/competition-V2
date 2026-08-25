package com.teaching.content.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.content.domain.NoticeInfo;
import com.teaching.content.mapper.NoticeInfoMapper;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NoticeInfoServiceSecurityTest {

    private NoticeInfoServiceImpl service;
    private NoticeInfoMapper mapper;

    @Before
    public void setUp() throws Exception {
        service = new NoticeInfoServiceImpl();
        mapper = mock(NoticeInfoMapper.class);
        Field field = NoticeInfoServiceImpl.class.getDeclaredField("noticeInfoMapper");
        field.setAccessible(true);
        field.set(service, mapper);

        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(new SysUser());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.remove();
    }

    @Test
    public void insertShouldRejectUnsafeContentBeforePersistence() {
        NoticeInfo noticeInfo = normalNotice();
        noticeInfo.setNoticeContent("<img src=\"file:///C:/Users/alice/secret.png\">");

        assertThrows(ServiceException.class, () -> service.insertNoticeInfo(noticeInfo));

        verify(mapper, never()).insertNoticeInfo(any(NoticeInfo.class));
    }

    @Test
    public void updateShouldRejectUnsafeImageBeforePersistence() {
        NoticeInfo noticeInfo = normalNotice();
        noticeInfo.setNoticeId(7L);
        noticeInfo.setNoticeImage("javascript:alert(1)");

        assertThrows(ServiceException.class, () -> service.updateNoticeInfo(noticeInfo));

        verify(mapper, never()).updateNoticeInfo(any(NoticeInfo.class));
    }

    @Test
    public void insertAndUpdateShouldAcceptNormalRichTextAndResources() {
        NoticeInfo insert = normalNotice();
        when(mapper.insertNoticeInfo(insert)).thenReturn(1);

        assertEquals(1, service.insertNoticeInfo(insert));
        verify(mapper).insertNoticeInfo(insert);

        NoticeInfo update = normalNotice();
        update.setNoticeId(9L);
        update.setNoticeImage("/profile/upload/notice-cover.png");
        when(mapper.updateNoticeInfo(update)).thenReturn(1);

        assertEquals(1, service.updateNoticeInfo(update));
        verify(mapper).updateNoticeInfo(update);
    }

    private NoticeInfo normalNotice() {
        NoticeInfo noticeInfo = new NoticeInfo();
        noticeInfo.setNoticeTitle("安全公告");
        noticeInfo.setNoticeAbstract("摘要");
        noticeInfo.setNoticeContent("<h2>标题</h2><p><strong>正常正文</strong></p>"
                + "<a href=\"https://www.example.com/guide\">指南</a>");
        noticeInfo.setNoticeImage("https://static.example/notice/cover.png");
        return noticeInfo;
    }
}
