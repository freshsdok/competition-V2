package com.teaching.system.service;

import com.teaching.common.redis.service.RedisService;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskConfig;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.domain.vo.FileTaskRecipientPageVo;
import com.teaching.system.mapper.FileTaskConfigMapper;
import com.teaching.system.mapper.FileTaskMapper;
import com.teaching.system.mapper.FileUploadManagerMapper;
import com.teaching.system.mapper.SysUserGroupMapper;
import com.teaching.system.mapper.SysUserMapper;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileTaskAudienceServiceTest {

    @Test
    public void shouldMergeOverlappingGroupsSortUsersAndKeepUnfilteredSummary() throws Exception {
        FileTaskMapper taskMapper = mock(FileTaskMapper.class);
        FileTaskConfigMapper configMapper = mock(FileTaskConfigMapper.class);
        FileUploadManagerMapper uploadMapper = mock(FileUploadManagerMapper.class);
        SysUserGroupMapper groupMapper = mock(SysUserGroupMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        RedisService redisService = mock(RedisService.class);

        FileTask task = new FileTask();
        task.setId(9L);
        task.setUserGroupIds("20,10");
        when(taskMapper.selectFileTaskByTaskId(9L)).thenReturn(task);

        FileTaskConfig uploadConfig = new FileTaskConfig();
        uploadConfig.setTaskType("1");
        when(configMapper.selectFileTaskConfigList(any())).thenReturn(List.of(uploadConfig));

        SysUserGroup first = new SysUserGroup();
        first.setId(20L);
        first.setName("校赛组");
        SysUserGroup second = new SysUserGroup();
        second.setId(10L);
        second.setName("省赛组");
        when(groupMapper.selectSysUserGroupByIds(List.of(20L, 10L))).thenReturn(List.of(first, second));
        when(redisService.getCacheObject("groupUserIds:info:20")).thenReturn(Set.of(3L, 1L));
        when(redisService.getCacheObject("groupUserIds:info:10")).thenReturn(Set.of(2L, 3L));

        Date uploadTime = new Date(1000L);
        FileUploadManager uploaded = upload(9L, 2L, "0", "[{\"fileName\":\"a.pdf\"}]", uploadTime);
        FileUploadManager deleted = upload(9L, 3L, "1", "[{\"fileName\":\"old.pdf\"}]", new Date(2000L));
        when(uploadMapper.selectLatestByTaskIds(List.of(9L))).thenReturn(List.of(uploaded, deleted));

        when(userMapper.selectUserListByUserGroupIds(List.of(1L, 2L, 3L))).thenReturn(List.of(
                Map.of("userId", 3L, "userName", "u3", "realName", "王三",
                        "phoneNumber", "133", "schoolName", "第三学校"),
                Map.of("userId", 1L, "userName", "u1", "realName", "李一",
                        "phoneNumber", "131", "schoolName", "第一学校"),
                Map.of("userId", 2L, "userName", "u2", "realName", "赵二",
                        "phoneNumber", "132", "schoolName", "第二学校")
        ));

        FileTaskAudienceService service = new FileTaskAudienceService();
        setField(service, "fileTaskMapper", taskMapper);
        setField(service, "fileTaskConfigMapper", configMapper);
        setField(service, "fileUploadManagerMapper", uploadMapper);
        setField(service, "sysUserGroupMapper", groupMapper);
        setField(service, "sysUserMapper", userMapper);
        setField(service, "redisService", redisService);

        FileTaskRecipientPageVo page =
                service.getRecipients(9L, 1, 10, "第二学校", "UPLOADED");

        Assert.assertEquals(3L, page.getTotalCount());
        Assert.assertEquals(1L, page.getUploadedCount());
        Assert.assertEquals(2L, page.getNotUploadedCount());
        Assert.assertEquals(1L, page.getTotal());
        Assert.assertEquals(Long.valueOf(2L), page.getRows().get(0).getUserId());
        Assert.assertEquals(uploadTime, page.getRows().get(0).getUploadTime());

        FileTaskRecipientPageVo all = service.getRecipients(9L, 1, 10, null, "ALL");
        Assert.assertEquals(List.of(1L, 2L, 3L), all.getRows().stream()
                .map(row -> row.getUserId()).toList());
        Assert.assertEquals("校赛组、省赛组", all.getRows().get(2).getUserGroupNames());
    }

    @Test
    public void validUploadRequiresCurrentRowWithNonEmptyFilesButNotATimestamp() {
        Assert.assertTrue(FileTaskAudienceService.isValidUpload(
                upload(1L, 1L, "0", "[{\"fileName\":\"a.pdf\"}]", null)));
        Assert.assertFalse(FileTaskAudienceService.isValidUpload(
                upload(1L, 1L, "1", "[{\"fileName\":\"a.pdf\"}]", new Date())));
        Assert.assertFalse(FileTaskAudienceService.isValidUpload(
                upload(1L, 1L, "0", "[]", new Date())));
        Assert.assertFalse(FileTaskAudienceService.isValidUpload(
                upload(1L, 1L, "0", "[  ]", new Date())));
        Assert.assertFalse(FileTaskAudienceService.isValidUpload(
                upload(1L, 1L, "0", "  ", new Date())));
    }

    private static FileUploadManager upload(Long taskId, Long userId, String delFlag,
                                            String fileInfo, Date uploadTime) {
        FileUploadManager manager = new FileUploadManager();
        manager.setFileTaskId(taskId);
        manager.setUserId(userId);
        manager.setDelFlag(delFlag);
        manager.setFileInfo(fileInfo);
        manager.setUploadTime(uploadTime);
        return manager;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
