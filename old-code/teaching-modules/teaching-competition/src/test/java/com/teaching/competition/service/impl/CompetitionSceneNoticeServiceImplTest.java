package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.domain.CompetitionSceneNoticeAccessVo;
import com.teaching.competition.domain.CompetitionSceneNoticeForm;
import com.teaching.competition.domain.CompetitionSceneNoticeVo;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.MyCompetitionSceneNoticeVo;
import com.teaching.competition.mapper.CompetitionSceneNoticeMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 当前用户通知聚合测试。
 */
@RunWith(MockitoJUnitRunner.class)
public class CompetitionSceneNoticeServiceImplTest {

    @Mock
    private CompetitionSceneNoticeMapper noticeMapper;

    @Mock
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @InjectMocks
    private CompetitionSceneNoticeServiceImpl service;

    private CompetitionSceneNoticeVo personal;
    private CompetitionSceneNoticeVo announcement;
    private CompetitionSceneNoticeVo anotherCompetition;

    @Before
    public void setUp() {
        personal = notice(1L, 81L, "PERSONAL", "个人提醒", "<p onclick=\"x()\">个人内容</p>");
        announcement = notice(2L, 81L, "ANNOUNCEMENT", "大赛公告", "<p>公告内容</p>");
        anotherCompetition = notice(3L, 82L, "ANNOUNCEMENT", "另一赛事", "<p>另一赛事内容</p>");
    }

    @Test
    public void shouldGroupByCompetitionAndSplitPersonalBeforeAnnouncements() {
        when(noticeMapper.selectMySceneNoticeAccess(100L)).thenReturn(Collections.emptyList());
        when(noticeMapper.selectMyVisibleNoticeList(eq(100L), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(Arrays.asList(personal, announcement, anotherCompetition));

        List<MyCompetitionSceneNoticeVo> groups = service.selectMyCompetitionSceneNoticeList(100L);

        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(Long.valueOf(81L), groups.get(0).getCompetitionSeriesId());
        Assert.assertEquals(1, groups.get(0).getPersonalNotices().size());
        Assert.assertEquals("个人提醒", groups.get(0).getPersonalNotices().get(0).getTitle());
        Assert.assertEquals(1, groups.get(0).getAnnouncements().size());
        Assert.assertEquals("大赛公告", groups.get(0).getAnnouncements().get(0).getTitle());
        Assert.assertEquals(Long.valueOf(82L), groups.get(1).getCompetitionSeriesId());
        Assert.assertFalse(groups.get(0).getPersonalNotices().get(0).getContent().contains("onclick"));
    }

    @Test
    public void shouldReturnEmptyListWithoutLoggedInUser() {
        Assert.assertTrue(service.selectMyCompetitionSceneNoticeList(null).isEmpty());
    }

    @Test
    public void shouldBuildNoticeFilterFromPrecomputedAccessSets() {
        CompetitionSceneNoticeAccessVo identity = access("IDENTITY", 81L, null, null, 501L);
        CompetitionSceneNoticeAccessVo credential = access("CREDENTIAL", 81L, 701L, null, 502L);
        CompetitionSceneNoticeAccessVo target = access("TARGET", 82L, 702L, 801L, 503L);
        when(noticeMapper.selectMySceneNoticeAccess(100L)).thenReturn(Arrays.asList(identity, credential, target));
        when(noticeMapper.selectMyVisibleNoticeList(eq(100L), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(Collections.emptyList());

        service.selectMyCompetitionSceneNoticeList(100L);

        verify(noticeMapper).selectMyVisibleNoticeList(
                eq(100L),
                argThat(items -> items.containsAll(Arrays.asList(501L, 502L, 503L))),
                argThat(items -> items.size() == 1 && items.contains(801L)),
                argThat(items -> items.containsAll(Arrays.asList(81L, 82L))),
                argThat(items -> items.containsAll(Arrays.asList(701L, 702L))));
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectScheduleOutsideSelectedCompetition() {
        CompetitionSceneNoticeForm form = announcementForm();
        form.setScopeType("SCHEDULE");
        form.setScheduleIds(Collections.singletonList(900L));
        when(noticeMapper.selectCompetitionIdBySeriesId(81L)).thenReturn(8L);
        when(noticeMapper.countSchedulesInSeries(81L, Collections.singletonList(900L))).thenReturn(0);

        service.insertCompetitionSceneNotice(form);
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectPersonalTargetWithoutStableIdentity() {
        CompetitionSceneNoticeForm form = new CompetitionSceneNoticeForm();
        form.setNoticeType("PERSONAL");
        form.setTargetId(55L);
        form.setTitle("个人通知");
        form.setContent("<p>正文</p>");
        form.setNoticeLevel("NORMAL");
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setTargetId(55L);
        target.setCompetitionSeriesId(81L);
        target.setStatus("0");
        target.setDelFlag("0");
        when(targetMapper.selectCompetitionSceneScheduleTargetById(55L)).thenReturn(target);

        service.insertCompetitionSceneNotice(form);
    }

    @Test(expected = ServiceException.class)
    public void shouldRejectExpireTimeNotAfterPublishTime() {
        CompetitionSceneNoticeForm form = announcementForm();
        Date publishTime = new Date();
        form.setPublishTime(publishTime);
        form.setExpireTime(publishTime);

        service.insertCompetitionSceneNotice(form);
    }

    private CompetitionSceneNoticeForm announcementForm() {
        CompetitionSceneNoticeForm form = new CompetitionSceneNoticeForm();
        form.setNoticeType("ANNOUNCEMENT");
        form.setScopeType("COMPETITION");
        form.setCompetitionSeriesId(81L);
        form.setTitle("公告标题");
        form.setContent("<p>公告正文</p>");
        form.setNoticeLevel("NORMAL");
        return form;
    }

    private CompetitionSceneNoticeAccessVo access(String type,
                                                   Long seriesId,
                                                   Long scheduleId,
                                                   Long targetId,
                                                   Long memberId) {
        CompetitionSceneNoticeAccessVo access = new CompetitionSceneNoticeAccessVo();
        access.setAccessType(type);
        access.setCompetitionSeriesId(seriesId);
        access.setScheduleId(scheduleId);
        access.setTargetId(targetId);
        access.setMemberId(memberId);
        return access;
    }

    private CompetitionSceneNoticeVo notice(Long id, Long seriesId, String type, String title, String content) {
        CompetitionSceneNoticeVo notice = new CompetitionSceneNoticeVo();
        notice.setNoticeId(id);
        notice.setCompetitionSeriesId(seriesId);
        notice.setCompetitionId(seriesId + 1000);
        notice.setCompetitionName("赛事" + seriesId);
        notice.setNoticeType(type);
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
