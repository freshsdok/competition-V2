package com.teaching.system.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskNotification;
import com.teaching.system.domain.FileTaskNotificationForm;
import com.teaching.system.domain.vo.FileTaskNotificationCountVo;
import com.teaching.system.mapper.FileTaskNotificationMapper;
import com.teaching.system.service.FileTaskAudienceService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileTaskNotificationServiceImplTest {

    private FileTaskAudienceService audienceService;
    private FileTaskNotificationMapper notificationMapper;
    private FileTaskNotificationServiceImpl service;

    @Before
    public void setUp() throws Exception {
        audienceService = mock(FileTaskAudienceService.class);
        notificationMapper = mock(FileTaskNotificationMapper.class);
        service = new FileTaskNotificationServiceImpl();
        setField(service, "audienceService", audienceService);
        setField(service, "notificationMapper", notificationMapper);

        SysUser user = new SysUser();
        user.setUserId(88L);
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId("88");
        SecurityContextHolder.setUserName("admin88");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.remove();
    }

    @Test
    public void fourTargetTypesEachInsertOneSortedSnapshotRow() throws Exception {
        FileTaskAudienceService.AudienceSnapshot audience =
                snapshot("2", List.of(3L, 1L, 2L, 3L), Map.of(2L, new Date()));
        when(audienceService.resolveUploadTaskAudience(9L)).thenReturn(audience);

        List<FileTaskNotification> inserted = new ArrayList<>();
        AtomicLong id = new AtomicLong(100L);
        when(notificationMapper.insert(any(FileTaskNotification.class))).thenAnswer(invocation -> {
            FileTaskNotification notification = invocation.getArgument(0);
            notification.setId(id.getAndIncrement());
            inserted.add(notification);
            return 1;
        });

        Map<String, Object> all = service.send(9L, form("ALL", null));
        Map<String, Object> single = service.send(9L, form("SINGLE", 3L));
        Map<String, Object> uploaded = service.send(9L, form("UPLOADED", null));
        Map<String, Object> notUploaded = service.send(9L, form("NOT_UPLOADED", null));

        verify(notificationMapper, times(4)).insert(any(FileTaskNotification.class));
        Assert.assertEquals(List.of("1,2,3", "3", "2", "1,3"), inserted.stream()
                .map(FileTaskNotification::getRecipientUserIds).toList());
        Assert.assertEquals(List.of(3, 1, 1, 2), inserted.stream()
                .map(FileTaskNotification::getRecipientCount).toList());
        Assert.assertNull(inserted.get(0).getTargetUserId());
        Assert.assertEquals(Long.valueOf(3L), inserted.get(1).getTargetUserId());
        Assert.assertEquals(3, all.get("recipientCount"));
        Assert.assertEquals(1, single.get("recipientCount"));
        Assert.assertEquals(1, uploaded.get("recipientCount"));
        Assert.assertEquals(2, notUploaded.get("recipientCount"));
    }

    @Test
    public void shouldRejectDraftEmptyScopeAndSingleOutsideAudienceWithoutInsert() throws Exception {
        when(audienceService.resolveUploadTaskAudience(1L))
                .thenReturn(snapshot("1", List.of(1L), Map.of()));
        assertServiceException(() -> service.send(1L, form("ALL", null)));

        when(audienceService.resolveUploadTaskAudience(2L))
                .thenReturn(snapshot("2", List.of(1L), Map.of()));
        assertServiceException(() -> service.send(2L, form("UPLOADED", null)));
        assertServiceException(() -> service.send(2L, form("SINGLE", 99L)));

        verify(notificationMapper, never()).insert(any(FileTaskNotification.class));
    }

    @Test
    public void userMustRemainInCurrentAudienceAndMapperReceivesTaskNoticeAndUserIds() throws Exception {
        FileTask task = new FileTask();
        task.setId(9L);
        task.setTaskStatus("2");
        when(audienceService.requireUploadTask(9L)).thenReturn(task);
        when(audienceService.isCurrentRecipient(task, 1L)).thenReturn(true);
        when(audienceService.isCurrentRecipient(task, 2L)).thenReturn(false);

        FileTaskNotification summary = new FileTaskNotification();
        summary.setId(7L);
        summary.setFileTaskId(9L);
        summary.setTitle("提醒");
        when(notificationMapper.selectUserList(9L, 1L)).thenReturn(List.of(summary));

        Assert.assertEquals(1, service.selectUserList(9L, 1L).size());
        assertServiceException(() -> service.selectUserList(9L, 2L));
        verify(notificationMapper, times(1)).selectUserList(9L, 1L);
        verify(notificationMapper, never()).selectUserList(9L, 2L);

        when(notificationMapper.selectUserDetail(9L, 7L, 1L)).thenReturn(summary);
        Assert.assertEquals(Long.valueOf(7L),
                service.selectUserDetail(9L, 7L, 1L).getNotificationId());
        verify(notificationMapper).selectUserDetail(9L, 7L, 1L);
    }

    @Test
    public void withdrawIsIdempotentAndCountsAreFilledInOneBatch() throws Exception {
        FileTask task = new FileTask();
        task.setId(9L);
        task.setTaskStatus("1");
        when(audienceService.requireUploadTask(9L)).thenReturn(task);

        FileTaskNotification active = new FileTaskNotification();
        active.setStatus("ACTIVE");
        FileTaskNotification withdrawn = new FileTaskNotification();
        withdrawn.setStatus("WITHDRAWN");
        when(notificationMapper.selectByTaskAndId(9L, 7L)).thenReturn(active, withdrawn);

        service.withdraw(9L, 7L);
        service.withdraw(9L, 7L);
        verify(notificationMapper, times(1)).withdraw(9L, 7L, 88L, "admin88");

        FileTask first = new FileTask();
        first.setId(9L);
        FileTask second = new FileTask();
        second.setId(10L);
        FileTaskNotificationCountVo count = new FileTaskNotificationCountVo();
        count.setFileTaskId(10L);
        count.setNotificationCount(100);
        when(notificationMapper.countActiveByTaskIdsAndUser(List.of(9L, 10L), 1L))
                .thenReturn(List.of(count));

        service.fillNotificationCounts(List.of(first, second), 1L);
        Assert.assertEquals(Integer.valueOf(0), first.getNotificationCount());
        Assert.assertEquals(Integer.valueOf(100), second.getNotificationCount());
        verify(notificationMapper, times(1))
                .countActiveByTaskIdsAndUser(List.of(9L, 10L), 1L);
    }

    private static FileTaskNotificationForm form(String targetType, Long targetUserId) {
        FileTaskNotificationForm form = new FileTaskNotificationForm();
        form.setTargetType(targetType);
        form.setTargetUserId(targetUserId);
        form.setTitle("上传提醒");
        form.setContentBase64(java.util.Base64.getEncoder().encodeToString(
                "<p>请按时提交</p>".getBytes(StandardCharsets.UTF_8)));
        return form;
    }

    @SuppressWarnings("unchecked")
    private static FileTaskAudienceService.AudienceSnapshot snapshot(
            String taskStatus, List<Long> userIds, Map<Long, Date> uploaded) throws Exception {
        FileTask task = new FileTask();
        task.setId(9L);
        task.setTaskStatus(taskStatus);
        Constructor<FileTaskAudienceService.AudienceSnapshot> constructor =
                FileTaskAudienceService.AudienceSnapshot.class.getDeclaredConstructor(FileTask.class);
        constructor.setAccessible(true);
        FileTaskAudienceService.AudienceSnapshot snapshot = constructor.newInstance(task);

        Field usersField = snapshot.getClass().getDeclaredField("userIds");
        usersField.setAccessible(true);
        ((SortedSet<Long>) usersField.get(snapshot)).addAll(new TreeSet<>(userIds));
        Field uploadsField = snapshot.getClass().getDeclaredField("uploadTimesByUser");
        uploadsField.setAccessible(true);
        ((Map<Long, Date>) uploadsField.get(snapshot)).putAll(uploaded);
        return snapshot;
    }

    private static void assertServiceException(ThrowingRunnable runnable) {
        try {
            runnable.run();
            Assert.fail("expected ServiceException");
        } catch (ServiceException expected) {
            // expected
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
