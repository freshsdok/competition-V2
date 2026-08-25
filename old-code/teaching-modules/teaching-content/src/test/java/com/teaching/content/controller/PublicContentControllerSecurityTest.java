package com.teaching.content.controller;

import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.content.domain.query.PublicNoticeQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;
import com.teaching.content.domain.vo.PublicNoticeInfo;
import com.teaching.content.service.INewsInfoService;
import com.teaching.content.service.INoticeInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublicContentControllerSecurityTest {

    private NewsInfoController newsController;
    private NoticeInfoController noticeController;
    private INewsInfoService newsService;
    private INoticeInfoService noticeService;

    @Before
    public void setUp() throws Exception {
        newsController = new NewsInfoController();
        noticeController = new NoticeInfoController();
        newsService = mock(INewsInfoService.class);
        noticeService = mock(INoticeInfoService.class);
        inject(newsController, "newsInfoService", newsService);
        inject(noticeController, "noticeInfoService", noticeService);
    }

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void hiddenNewsShouldReturnUniformNotFoundAndNotIncreaseReadingQuantity() {
        when(newsService.selectPublicNewsInfoByNewsId(12L)).thenReturn(null);

        AjaxResult response = newsController.getPublicInfo(12L);

        assertEquals(HttpStatus.NOT_FOUND, response.get(AjaxResult.CODE_TAG));
        assertEquals("内容不存在或不可访问", response.get(AjaxResult.MSG_TAG));
        verify(newsService, never()).increaseReadingQuantity(12L);
    }

    @Test
    public void publishedNewsShouldIncrementAndReturnOnlyPublicModel() {
        PublicNewsInfo newsInfo = new PublicNewsInfo();
        newsInfo.setNewsId(11L);
        newsInfo.setReadingQuantity(8);
        when(newsService.selectPublicNewsInfoByNewsId(11L)).thenReturn(newsInfo);
        when(newsService.increaseReadingQuantity(11L)).thenReturn(1);

        AjaxResult response = newsController.getPublicInfo(11L);

        assertEquals(HttpStatus.SUCCESS, response.get(AjaxResult.CODE_TAG));
        assertSame(newsInfo, response.get(AjaxResult.DATA_TAG));
        assertEquals(Integer.valueOf(9), newsInfo.getReadingQuantity());
        verify(newsService).increaseReadingQuantity(11L);
    }

    @Test
    public void hiddenNoticeShouldReturnUniformNotFound() {
        when(noticeService.selectPublicNoticeInfoByNoticeId(2L)).thenReturn(null);

        AjaxResult response = noticeController.getPublicInfo(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.get(AjaxResult.CODE_TAG));
        assertEquals("内容不存在或不可访问", response.get(AjaxResult.MSG_TAG));
    }

    @Test
    public void publishedNoticeShouldReturnPublicModel() {
        PublicNoticeInfo noticeInfo = new PublicNoticeInfo();
        noticeInfo.setNoticeId(1L);
        when(noticeService.selectPublicNoticeInfoByNoticeId(1L)).thenReturn(noticeInfo);

        AjaxResult response = noticeController.getPublicInfo(1L);

        assertEquals(HttpStatus.SUCCESS, response.get(AjaxResult.CODE_TAG));
        assertSame(noticeInfo, response.get(AjaxResult.DATA_TAG));
    }

    @Test
    public void publicNoticePaginationShouldUseFilteredTotalAndAllowLastPartialPage() {
        PublicNoticeInfo first = notice(1L);
        PublicNoticeInfo second = notice(2L);
        PublicNoticeInfo third = notice(3L);
        when(noticeService.selectPublicNoticeInfoList(
                org.mockito.ArgumentMatchers.any(PublicNoticeQuery.class)))
                .thenReturn(List.of(first, second, third));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("pageNum")).thenReturn("2");
        when(request.getParameter("pageSize")).thenReturn("2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        TableDataInfo response = noticeController.getList(new PublicNoticeQuery());

        assertEquals(3L, response.getTotal());
        assertEquals(List.of(third), response.getRows());
    }

    private PublicNoticeInfo notice(Long id) {
        PublicNoticeInfo notice = new PublicNoticeInfo();
        notice.setNoticeId(id);
        return notice;
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
