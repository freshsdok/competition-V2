package com.teaching.system.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskNotification;
import com.teaching.system.domain.FileTaskNotificationForm;
import com.teaching.system.domain.vo.FileTaskNotificationCountVo;
import com.teaching.system.domain.vo.FileTaskNotificationVo;
import com.teaching.system.mapper.FileTaskNotificationMapper;
import com.teaching.system.service.FileTaskAudienceService;
import com.teaching.system.service.FileTaskNotificationContentCodec;
import com.teaching.system.service.FileTaskNotificationHtmlSanitizer;
import com.teaching.system.service.IFileTaskNotificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 文件上传任务通知实现。
 */
@Service
public class FileTaskNotificationServiceImpl implements IFileTaskNotificationService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String TARGET_SINGLE = "SINGLE";
    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_UPLOADED = "UPLOADED";
    private static final String TARGET_NOT_UPLOADED = "NOT_UPLOADED";
    private static final Set<String> TARGET_TYPES =
            Set.of(TARGET_SINGLE, TARGET_ALL, TARGET_UPLOADED, TARGET_NOT_UPLOADED);

    @Autowired
    private FileTaskAudienceService audienceService;

    @Autowired
    private FileTaskNotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> send(Long taskId, FileTaskNotificationForm form) {
        if (form == null) {
            throw new ServiceException("通知信息不能为空");
        }
        FileTaskAudienceService.AudienceSnapshot audience =
                audienceService.resolveUploadTaskAudience(taskId);
        if (!Constants.TASK_STATUS_PUBLISH.equals(audience.getTask().getTaskStatus())) {
            throw new ServiceException("仅已发布任务可以发送通知");
        }

        String targetType = normalizeTargetType(form.getTargetType());
        TreeSet<Long> recipients = resolveRecipients(audience, targetType, form.getTargetUserId());
        if (recipients.isEmpty()) {
            throw new ServiceException("当前发送范围内没有接收人");
        }

        String title = StringUtils.trimToNull(form.getTitle());
        if (title == null) {
            throw new ServiceException("通知标题不能为空");
        }
        if (title.length() > 255) {
            throw new ServiceException("通知标题不能超过255个字符");
        }
        String rawContent = FileTaskNotificationContentCodec.decodeUtf8Base64(form.getContentBase64());
        String content = FileTaskNotificationHtmlSanitizer.sanitize(rawContent);
        if (!FileTaskNotificationHtmlSanitizer.hasVisualContent(content)) {
            throw new ServiceException("通知内容不能为空");
        }

        Long senderUserId = currentUserId();
        String username = SecurityUtils.getUsername();
        FileTaskNotification notification = new FileTaskNotification();
        notification.setFileTaskId(taskId);
        notification.setTargetType(targetType);
        notification.setTargetUserId(TARGET_SINGLE.equals(targetType) ? form.getTargetUserId() : null);
        notification.setRecipientUserIds(recipients.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        notification.setRecipientCount(recipients.size());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(STATUS_ACTIVE);
        notification.setSenderUserId(senderUserId);
        notification.setSendTime(DateUtils.getNowDate());
        notification.setCreateBy(username);
        notification.setCreateTime(DateUtils.getNowDate());
        notification.setUpdateBy(username);
        notification.setUpdateTime(DateUtils.getNowDate());
        if (notificationMapper.insert(notification) <= 0) {
            throw new ServiceException("通知发送失败");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notificationId", notification.getId());
        result.put("recipientCount", notification.getRecipientCount());
        return result;
    }

    @Override
    public List<FileTaskNotificationVo> selectAdminList(Long taskId) {
        List<FileTaskNotification> notifications = notificationMapper.selectAdminList(taskId);
        if (notifications == null) {
            return Collections.emptyList();
        }
        return notifications.stream().map(this::toAdminVo).collect(Collectors.toList());
    }

    @Override
    public FileTaskNotificationVo selectAdminDetail(Long taskId, Long notificationId) {
        audienceService.requireUploadTask(taskId);
        FileTaskNotification notification = notificationMapper.selectByTaskAndId(taskId, notificationId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        return toAdminVo(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long taskId, Long notificationId) {
        audienceService.requireUploadTask(taskId);
        FileTaskNotification notification = notificationMapper.selectByTaskAndId(taskId, notificationId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        if (STATUS_WITHDRAWN.equals(notification.getStatus())) {
            return;
        }
        notificationMapper.withdraw(taskId, notificationId, currentUserId(), SecurityUtils.getUsername());
    }

    @Override
    public List<FileTaskNotificationVo> selectUserList(Long taskId, Long userId) {
        requireUserTaskAccess(taskId, userId);
        List<FileTaskNotification> notifications = notificationMapper.selectUserList(taskId, userId);
        if (notifications == null) {
            return Collections.emptyList();
        }
        return notifications.stream().map(this::toUserSummaryVo).collect(Collectors.toList());
    }

    @Override
    public FileTaskNotificationVo selectUserDetail(Long taskId, Long notificationId, Long userId) {
        requireUserTaskAccess(taskId, userId);
        FileTaskNotification notification =
                notificationMapper.selectUserDetail(taskId, notificationId, userId);
        if (notification == null) {
            throw new ServiceException("通知不存在或无权查看", 403);
        }
        FileTaskNotificationVo vo = toUserSummaryVo(notification);
        vo.setContent(FileTaskNotificationHtmlSanitizer.sanitize(notification.getContent()));
        return vo;
    }

    @Override
    public void fillNotificationCounts(List<FileTask> tasks, Long userId) {
        if (tasks == null || tasks.isEmpty() || userId == null) {
            return;
        }
        List<Long> taskIds = tasks.stream().map(FileTask::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (taskIds.isEmpty()) {
            return;
        }
        List<FileTaskNotificationCountVo> counts =
                notificationMapper.countActiveByTaskIdsAndUser(taskIds, userId);
        Map<Long, Integer> countByTask = counts == null ? new HashMap<>()
                : counts.stream().collect(Collectors.toMap(
                        FileTaskNotificationCountVo::getFileTaskId,
                        item -> item.getNotificationCount() == null ? 0 : item.getNotificationCount(),
                        Integer::sum));
        tasks.forEach(task -> task.setNotificationCount(countByTask.getOrDefault(task.getId(), 0)));
    }

    private void requireUserTaskAccess(Long taskId, Long userId) {
        if (userId == null) {
            throw new ServiceException("登录状态无效", 401);
        }
        FileTask task = audienceService.requireUploadTask(taskId);
        if (!Constants.TASK_STATUS_PUBLISH.equals(task.getTaskStatus())
                || !audienceService.isCurrentRecipient(task, userId)) {
            throw new ServiceException("任务不存在或无权查看", 403);
        }
    }

    private static TreeSet<Long> resolveRecipients(FileTaskAudienceService.AudienceSnapshot audience,
                                                   String targetType, Long targetUserId) {
        TreeSet<Long> recipients = new TreeSet<>();
        if (TARGET_SINGLE.equals(targetType)) {
            if (targetUserId == null || !audience.getUserIds().contains(targetUserId)) {
                throw new ServiceException("所选用户不属于当前任务");
            }
            recipients.add(targetUserId);
            return recipients;
        }
        if (TARGET_ALL.equals(targetType)) {
            recipients.addAll(audience.getUserIds());
            return recipients;
        }
        if (TARGET_UPLOADED.equals(targetType)) {
            recipients.addAll(audience.getUploadTimesByUser().keySet());
            return recipients;
        }
        recipients.addAll(audience.getUserIds());
        recipients.removeAll(audience.getUploadTimesByUser().keySet());
        return recipients;
    }

    private static String normalizeTargetType(String targetType) {
        String normalized = StringUtils.trimToEmpty(targetType).toUpperCase(Locale.ROOT);
        if (!TARGET_TYPES.contains(normalized)) {
            throw new ServiceException("不支持的发送范围");
        }
        return normalized;
    }

    private static Long currentUserId() {
        var loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            return loginUser.getSysUser().getUserId();
        }
        return SecurityUtils.getUserId();
    }

    private FileTaskNotificationVo toAdminVo(FileTaskNotification notification) {
        FileTaskNotificationVo vo = baseVo(notification);
        vo.setFileTaskId(notification.getFileTaskId());
        vo.setTargetType(notification.getTargetType());
        vo.setRecipientCount(notification.getRecipientCount());
        vo.setContent(notification.getContent() == null
                ? null : FileTaskNotificationHtmlSanitizer.sanitize(notification.getContent()));
        vo.setStatus(notification.getStatus());
        vo.setSenderUserId(notification.getSenderUserId());
        vo.setSenderName(notification.getSenderName());
        vo.setWithdrawUserId(notification.getWithdrawUserId());
        vo.setWithdrawUserName(notification.getWithdrawUserName());
        vo.setWithdrawTime(notification.getWithdrawTime());
        return vo;
    }

    private FileTaskNotificationVo toUserSummaryVo(FileTaskNotification notification) {
        return baseVo(notification);
    }

    private static FileTaskNotificationVo baseVo(FileTaskNotification notification) {
        FileTaskNotificationVo vo = new FileTaskNotificationVo();
        vo.setNotificationId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setSendTime(notification.getSendTime());
        return vo;
    }
}
