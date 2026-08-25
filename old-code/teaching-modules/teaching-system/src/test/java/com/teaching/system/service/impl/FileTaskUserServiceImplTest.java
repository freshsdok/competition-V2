package com.teaching.system.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.redis.service.RedisService;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.FileTask;
import com.teaching.system.mapper.FileTaskMapper;
import com.teaching.system.mapper.FileUploadRecordMapper;
import com.teaching.system.mapper.SysUserGroupMapper;
import com.teaching.system.service.FileTaskAudienceService;
import com.teaching.system.service.IFileTaskNotificationService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileTaskUserServiceImplTest {

    private static final Long USER_ID = 1244L;

    private FileTaskMapper fileTaskMapper;
    private RedisService redisService;
    private FileTaskAudienceService audienceService;
    private IFileTaskNotificationService notificationService;
    private FileTaskUserServiceImpl service;

    @Before
    public void setUp() throws Exception {
        fileTaskMapper = mock(FileTaskMapper.class);
        redisService = mock(RedisService.class);
        audienceService = mock(FileTaskAudienceService.class);
        notificationService = mock(IFileTaskNotificationService.class);

        service = new FileTaskUserServiceImpl();
        setField(service, "fileTaskMapper", fileTaskMapper);
        setField(service, "fileUploadRecordMapper", mock(FileUploadRecordMapper.class));
        setField(service, "redisService", redisService);
        setField(service, "sysUserGroupMapper", mock(SysUserGroupMapper.class));
        setField(service, "fileTaskAudienceService", audienceService);
        setField(service, "fileTaskNotificationService", notificationService);

        SysUser user = new SysUser();
        user.setUserId(USER_ID);
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId(String.valueOf(USER_ID));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.remove();
    }

    @Test
    public void missingReverseGroupCacheFallsBackToForwardMembership() {
        FileTask task = task(20L, "7");
        when(fileTaskMapper.selectFileTaskList(any(FileTask.class))).thenAnswer(invocation -> {
            FileTask query = invocation.getArgument(0);
            Assert.assertNull(query.getUserGroupIds());
            Assert.assertEquals("2", query.getTaskStatus());
            return List.of(task);
        });
        when(audienceService.isCurrentRecipient(task, USER_ID)).thenReturn(true);

        List<FileTask> result = service.selectFileTaskUserList(new FileTask());

        Assert.assertEquals(List.of(task), result);
        verify(notificationService).fillNotificationCounts(result, USER_ID);
    }

    @Test
    public void forwardMembershipStillRejectsTaskWhenReverseCacheIsMissing() {
        FileTask task = task(20L, "7");
        when(fileTaskMapper.selectFileTaskList(any(FileTask.class))).thenReturn(List.of(task));
        when(audienceService.isCurrentRecipient(task, USER_ID)).thenReturn(false);

        List<FileTask> result = service.selectFileTaskUserList(new FileTask());

        Assert.assertTrue(result.isEmpty());
        verify(notificationService, never()).fillNotificationCounts(any(), eq(USER_ID));
    }

    @Test
    public void staleReverseCacheCannotLimitPublishedTaskCandidates() {
        when(fileTaskMapper.selectFileTaskList(any(FileTask.class))).thenAnswer(invocation -> {
            FileTask query = invocation.getArgument(0);
            Assert.assertNull(query.getUserGroupIds());
            Assert.assertEquals("2", query.getTaskStatus());
            return Collections.emptyList();
        });

        Assert.assertTrue(service.selectFileTaskUserList(new FileTask()).isEmpty());
        verify(redisService, never()).getCacheObject("userGroup:info:" + USER_ID);
    }

    private static FileTask task(Long id, String userGroupIds) {
        FileTask task = new FileTask();
        task.setId(id);
        task.setUserGroupIds(userGroupIds);
        task.setTaskStatus("2");
        task.setFileTaskConfigList(Collections.emptyList());
        return task;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
