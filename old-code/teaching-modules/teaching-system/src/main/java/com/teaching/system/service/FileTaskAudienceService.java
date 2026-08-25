package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.redis.service.RedisService;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskConfig;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.domain.vo.FileTaskRecipientPageVo;
import com.teaching.system.domain.vo.FileTaskRecipientVo;
import com.teaching.system.mapper.FileTaskConfigMapper;
import com.teaching.system.mapper.FileTaskMapper;
import com.teaching.system.mapper.FileUploadManagerMapper;
import com.teaching.system.mapper.SysUserGroupMapper;
import com.teaching.system.mapper.SysUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 文件任务动态受众及当前有效上传的唯一判定入口。
 */
@Service
public class FileTaskAudienceService {

    public static final String UPLOAD_STATUS_ALL = "ALL";
    public static final String UPLOAD_STATUS_UPLOADED = "UPLOADED";
    public static final String UPLOAD_STATUS_NOT_UPLOADED = "NOT_UPLOADED";

    private static final int USER_QUERY_BATCH_SIZE = 200;
    private static final int MAX_PAGE_SIZE = 500;

    @Autowired
    private FileTaskMapper fileTaskMapper;

    @Autowired
    private FileTaskConfigMapper fileTaskConfigMapper;

    @Autowired
    private FileUploadManagerMapper fileUploadManagerMapper;

    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 管理端应交人员分页。汇总数字始终基于完整动态受众，不受检索条件影响。
     */
    public FileTaskRecipientPageVo getRecipients(Long taskId, int pageNum, int pageSize,
                                                 String keyword, String uploadStatus) {
        AudienceSnapshot snapshot = resolveUploadTaskAudience(taskId);
        String normalizedStatus = normalizeUploadStatus(uploadStatus);
        List<FileTaskRecipientVo> allRows = hydrateRecipients(snapshot);

        FileTaskRecipientPageVo result = new FileTaskRecipientPageVo();
        result.setTotalCount(allRows.size());
        long uploadedCount = allRows.stream().filter(row -> Boolean.TRUE.equals(row.getUploaded())).count();
        result.setUploadedCount(uploadedCount);
        result.setNotUploadedCount(allRows.size() - uploadedCount);

        String normalizedKeyword = StringUtils.isBlank(keyword)
                ? null : keyword.trim().toLowerCase(Locale.ROOT);
        List<FileTaskRecipientVo> filtered = allRows.stream()
                .filter(row -> matchesStatus(row, normalizedStatus))
                .filter(row -> matchesKeyword(row, normalizedKeyword))
                .collect(Collectors.toList());
        result.setTotal(filtered.size());

        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        long fromLong = (long) (safePageNum - 1) * safePageSize;
        if (fromLong >= filtered.size()) {
            result.setRows(Collections.emptyList());
            return result;
        }
        int from = (int) fromLong;
        int to = Math.min(from + safePageSize, filtered.size());
        result.setRows(new ArrayList<>(filtered.subList(from, to)));
        return result;
    }

    /**
     * 校验任务存在且包含上传配置，并按正向用户组缓存计算实时受众。
     */
    public AudienceSnapshot resolveUploadTaskAudience(Long taskId) {
        FileTask task = requireUploadTask(taskId);
        return resolveAudience(task);
    }

    /**
     * 仅校验上传任务，不读取上传记录或用户资料。
     */
    public FileTask requireUploadTask(Long taskId) {
        FileTask task = requireTask(taskId);
        if (!hasUploadConfig(taskId)) {
            throw new ServiceException("当前任务不包含文件上传配置");
        }
        return task;
    }

    /**
     * 按任务各 groupUserIds 正向集合实时判断，避免依赖可能短暂不一致的用户反向缓存。
     */
    public boolean isCurrentRecipient(FileTask task, Long userId) {
        if (task == null || userId == null) {
            return false;
        }
        for (Long groupId : parseIds(task.getUserGroupIds())) {
            Set<Long> groupUserIds = redisService.getCacheObject("groupUserIds:info:" + groupId);
            if (groupUserIds != null && groupUserIds.contains(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用相同口径填充管理端任务列表人数及当前有效上传人数。
     */
    public void fillTaskUploadStatistics(List<FileTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<Long> taskIds = tasks.stream()
                .map(FileTask::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Map<Long, Date>> validUploads = validUploadsByTask(taskIds);
        for (FileTask task : tasks) {
            AudienceSnapshot audience = resolveAudience(task, validUploads.get(task.getId()));
            task.setPeopleCount(audience.getUserIds().size());
            task.setUploadedCount(audience.getUploadTimesByUser().size());
        }
    }

    public FileTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new ServiceException("任务ID不能为空");
        }
        FileTask task = fileTaskMapper.selectFileTaskByTaskId(taskId);
        if (task == null) {
            throw new ServiceException("文件任务不存在或已删除");
        }
        return task;
    }

    public boolean hasUploadConfig(Long taskId) {
        FileTaskConfig query = new FileTaskConfig();
        query.setTaskId(taskId);
        query.setTaskType("1");
        List<FileTaskConfig> configs = fileTaskConfigMapper.selectFileTaskConfigList(query);
        return configs != null && !configs.isEmpty();
    }

    private AudienceSnapshot resolveAudience(FileTask task) {
        Map<Long, Map<Long, Date>> uploads = validUploadsByTask(Collections.singletonList(task.getId()));
        return resolveAudience(task, uploads.get(task.getId()));
    }

    private AudienceSnapshot resolveAudience(FileTask task, Map<Long, Date> validUploadTimes) {
        AudienceSnapshot snapshot = new AudienceSnapshot(task);
        List<Long> groupIds = parseIds(task == null ? null : task.getUserGroupIds());
        if (groupIds.isEmpty()) {
            return snapshot;
        }

        List<SysUserGroup> groups = sysUserGroupMapper.selectSysUserGroupByIds(groupIds);
        Map<Long, String> groupNames = (groups == null ? Collections.<SysUserGroup>emptyList() : groups).stream()
                .filter(group -> group != null && group.getId() != null)
                .collect(Collectors.toMap(SysUserGroup::getId,
                        group -> StringUtils.defaultString(group.getName()),
                        (left, right) -> left, LinkedHashMap::new));
        for (Long groupId : groupIds) {
            Set<Long> groupUserIds = redisService.getCacheObject("groupUserIds:info:" + groupId);
            if (groupUserIds == null || groupUserIds.isEmpty()) {
                continue;
            }
            String groupName = groupNames.get(groupId);
            for (Long userId : groupUserIds) {
                if (userId == null) {
                    continue;
                }
                snapshot.userIds.add(userId);
                if (StringUtils.isNotBlank(groupName)) {
                    snapshot.groupNamesByUser
                            .computeIfAbsent(userId, key -> new LinkedHashSet<>())
                            .add(groupName);
                }
            }
        }
        if (validUploadTimes != null) {
            validUploadTimes.forEach((userId, uploadTime) -> {
                if (snapshot.userIds.contains(userId)) {
                    snapshot.uploadTimesByUser.put(userId, uploadTime);
                }
            });
        }
        return snapshot;
    }

    private List<FileTaskRecipientVo> hydrateRecipients(AudienceSnapshot snapshot) {
        Map<Long, Map<String, Object>> userById = new HashMap<>();
        List<Long> ids = new ArrayList<>(snapshot.getUserIds());
        for (int i = 0; i < ids.size(); i += USER_QUERY_BATCH_SIZE) {
            List<Long> batch = ids.subList(i, Math.min(i + USER_QUERY_BATCH_SIZE, ids.size()));
            List<Map<String, Object>> users = sysUserMapper.selectUserListByUserGroupIds(batch);
            if (users == null) {
                continue;
            }
            for (Map<String, Object> user : users) {
                if (user == null) {
                    continue;
                }
                Long userId = toLong(value(user, "userId"));
                if (userId != null) {
                    userById.put(userId, user);
                }
            }
        }

        List<FileTaskRecipientVo> rows = new ArrayList<>(ids.size());
        for (Long userId : ids) {
            Map<String, Object> user = userById.getOrDefault(userId, Collections.emptyMap());
            FileTaskRecipientVo row = new FileTaskRecipientVo();
            row.setUserId(userId);
            row.setUserName(toString(value(user, "userName")));
            row.setRealName(firstNotBlank(
                    toString(value(user, "realName")),
                    toString(value(user, "nickName")),
                    row.getUserName()));
            row.setPhoneNumber(toString(value(user, "phoneNumber")));
            row.setSchoolName(toString(value(user, "schoolName")));
            Set<String> names = snapshot.getGroupNamesByUser().get(userId);
            row.setUserGroupNames(names == null ? "" : String.join("、", names));
            Date uploadTime = snapshot.getUploadTimesByUser().get(userId);
            row.setUploaded(snapshot.getUploadTimesByUser().containsKey(userId));
            row.setUploadTime(uploadTime);
            rows.add(row);
        }
        return rows;
    }

    private Map<Long, Map<Long, Date>> validUploadsByTask(List<Long> taskIds) {
        Map<Long, Map<Long, Date>> result = new HashMap<>();
        if (taskIds == null || taskIds.isEmpty()) {
            return result;
        }
        List<FileUploadManager> latest = fileUploadManagerMapper.selectLatestByTaskIds(taskIds);
        if (latest == null) {
            return result;
        }
        for (FileUploadManager manager : latest) {
            if (!isValidUpload(manager) || manager.getFileTaskId() == null || manager.getUserId() == null) {
                continue;
            }
            Date effectiveTime = manager.getUploadTime();
            if (effectiveTime == null) {
                effectiveTime = manager.getUpdateTime() == null ? manager.getCreateTime() : manager.getUpdateTime();
            }
            result.computeIfAbsent(manager.getFileTaskId(), key -> new HashMap<>())
                    .put(manager.getUserId(), effectiveTime);
        }
        return result;
    }

    static boolean isValidUpload(FileUploadManager manager) {
        if (manager == null || !"0".equals(manager.getDelFlag())) {
            return false;
        }
        String fileInfo = manager.getFileInfo();
        return StringUtils.isNotBlank(fileInfo)
                && !"[]".equals(fileInfo.replaceAll("\\s+", ""));
    }

    private static boolean matchesStatus(FileTaskRecipientVo row, String status) {
        if (UPLOAD_STATUS_UPLOADED.equals(status)) {
            return Boolean.TRUE.equals(row.getUploaded());
        }
        if (UPLOAD_STATUS_NOT_UPLOADED.equals(status)) {
            return !Boolean.TRUE.equals(row.getUploaded());
        }
        return true;
    }

    private static boolean matchesKeyword(FileTaskRecipientVo row, String keyword) {
        if (keyword == null) {
            return true;
        }
        return contains(row.getRealName(), keyword)
                || contains(row.getUserName(), keyword)
                || contains(row.getPhoneNumber(), keyword)
                || contains(row.getSchoolName(), keyword)
                || contains(row.getUserGroupNames(), keyword);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static String normalizeUploadStatus(String status) {
        String normalized = StringUtils.isBlank(status)
                ? UPLOAD_STATUS_ALL : status.trim().toUpperCase(Locale.ROOT);
        if (!Set.of(UPLOAD_STATUS_ALL, UPLOAD_STATUS_UPLOADED, UPLOAD_STATUS_NOT_UPLOADED)
                .contains(normalized)) {
            throw new ServiceException("不支持的上传状态");
        }
        return normalized;
    }

    private static List<Long> parseIds(String csv) {
        if (StringUtils.isBlank(csv)) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String part : csv.split(",")) {
            try {
                ids.add(Long.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
                // 跳过历史脏值，避免单个无效用户组使整个任务不可查看。
            }
        }
        return new ArrayList<>(ids);
    }

    private static Object value(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public static final class AudienceSnapshot {
        private final FileTask task;
        private final SortedSet<Long> userIds = new TreeSet<>();
        private final Map<Long, Set<String>> groupNamesByUser = new HashMap<>();
        private final Map<Long, Date> uploadTimesByUser = new HashMap<>();

        private AudienceSnapshot(FileTask task) {
            this.task = task;
        }

        public FileTask getTask() {
            return task;
        }

        public SortedSet<Long> getUserIds() {
            return Collections.unmodifiableSortedSet(userIds);
        }

        public Map<Long, Set<String>> getGroupNamesByUser() {
            return Collections.unmodifiableMap(groupNamesByUser);
        }

        public Map<Long, Date> getUploadTimesByUser() {
            return Collections.unmodifiableMap(uploadTimesByUser);
        }
    }
}
