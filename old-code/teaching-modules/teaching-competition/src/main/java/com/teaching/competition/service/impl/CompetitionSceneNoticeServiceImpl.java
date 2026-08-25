package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionSceneNotice;
import com.teaching.competition.domain.CompetitionSceneNoticeAccessVo;
import com.teaching.competition.domain.CompetitionSceneNoticeForm;
import com.teaching.competition.domain.CompetitionSceneNoticeQuery;
import com.teaching.competition.domain.CompetitionSceneNoticeSchedule;
import com.teaching.competition.domain.CompetitionSceneNoticeVo;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.MyCompetitionSceneNoticeVo;
import com.teaching.competition.mapper.CompetitionSceneNoticeMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.ICompetitionSceneNoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 赛事现场通知Service实现。
 */
@Service
public class CompetitionSceneNoticeServiceImpl implements ICompetitionSceneNoticeService {

    static final String TYPE_ANNOUNCEMENT = "ANNOUNCEMENT";
    static final String TYPE_PERSONAL = "PERSONAL";
    static final String SCOPE_COMPETITION = "COMPETITION";
    static final String SCOPE_SCHEDULE = "SCHEDULE";
    static final String SCOPE_PERSON = "PERSON";
    static final String PUBLISH_DRAFT = "DRAFT";
    static final String PUBLISH_PUBLISHED = "PUBLISHED";
    static final String PUBLISH_DISABLED = "DISABLED";

    private static final Set<String> NOTICE_LEVELS = new HashSet<>(Arrays.asList("NORMAL", "IMPORTANT", "URGENT"));
    private static final Set<String> ANNOUNCEMENT_SCOPES = new HashSet<>(Arrays.asList(SCOPE_COMPETITION, SCOPE_SCHEDULE));
    private static final Set<String> CHANGEABLE_PUBLISH_STATUSES = new HashSet<>(Arrays.asList(PUBLISH_DRAFT, PUBLISH_DISABLED));

    @Autowired
    private CompetitionSceneNoticeMapper noticeMapper;

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @Override
    public CompetitionSceneNoticeVo selectCompetitionSceneNoticeById(Long noticeId) {
        CompetitionSceneNoticeVo notice = requireNotice(noticeId);
        fillScheduleIds(notice);
        sanitizeContent(notice);
        return notice;
    }

    @Override
    public List<CompetitionSceneNoticeVo> selectCompetitionSceneNoticeList(CompetitionSceneNoticeQuery query) {
        CompetitionSceneNoticeQuery normalized = query == null ? new CompetitionSceneNoticeQuery() : query;
        normalized.setTitle(trimToNull(normalized.getTitle()));
        List<CompetitionSceneNoticeVo> list = noticeMapper.selectCompetitionSceneNoticeList(normalized);
        list.forEach(this::sanitizeContent);
        return list;
    }

    @Override
    public List<MyCompetitionSceneNoticeVo> selectMyCompetitionSceneNoticeList(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<CompetitionSceneNoticeAccessVo> accessList = noticeMapper.selectMySceneNoticeAccess(userId);
        Set<Long> memberIds = new HashSet<>();
        Set<Long> targetIds = new HashSet<>();
        Set<Long> seriesIds = new HashSet<>();
        Set<Long> scheduleIds = new HashSet<>();
        if (accessList != null) {
            for (CompetitionSceneNoticeAccessVo access : accessList) {
                if (access.getMemberId() != null) {
                    memberIds.add(access.getMemberId());
                }
                if ("TARGET".equals(access.getAccessType()) && access.getTargetId() != null) {
                    targetIds.add(access.getTargetId());
                }
                if (!"IDENTITY".equals(access.getAccessType())) {
                    if (access.getCompetitionSeriesId() != null) {
                        seriesIds.add(access.getCompetitionSeriesId());
                    }
                    if (access.getScheduleId() != null) {
                        scheduleIds.add(access.getScheduleId());
                    }
                }
            }
        }
        List<CompetitionSceneNoticeVo> notices = noticeMapper.selectMyVisibleNoticeList(
                userId,
                new ArrayList<>(memberIds),
                new ArrayList<>(targetIds),
                new ArrayList<>(seriesIds),
                new ArrayList<>(scheduleIds));
        Map<Long, MyCompetitionSceneNoticeVo> groups = new LinkedHashMap<>();
        for (CompetitionSceneNoticeVo notice : notices) {
            if (notice.getCompetitionSeriesId() == null) {
                continue;
            }
            sanitizeContent(notice);
            MyCompetitionSceneNoticeVo group = groups.computeIfAbsent(notice.getCompetitionSeriesId(), key -> {
                MyCompetitionSceneNoticeVo value = new MyCompetitionSceneNoticeVo();
                value.setCompetitionSeriesId(key);
                value.setCompetitionId(notice.getCompetitionId());
                value.setCompetitionName(notice.getCompetitionName());
                return value;
            });
            if (TYPE_PERSONAL.equals(notice.getNoticeType())) {
                group.getPersonalNotices().add(notice);
            } else if (TYPE_ANNOUNCEMENT.equals(notice.getNoticeType())) {
                group.getAnnouncements().add(notice);
            }
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCompetitionSceneNotice(CompetitionSceneNoticeForm form) {
        CompetitionSceneNotice notice = buildNoticeForSave(form, null);
        notice.setPublishStatus(PUBLISH_DRAFT);
        notice.setStatus("0");
        notice.setDelFlag("0");
        notice.setCreateBy(SecurityUtils.getUsername());
        notice.setCreateTime(DateUtils.getNowDate());
        int rows = noticeMapper.insertCompetitionSceneNotice(notice);
        replaceScheduleRelations(notice, form.getScheduleIds());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitionSceneNotice(CompetitionSceneNoticeForm form) {
        if (form == null || form.getNoticeId() == null) {
            throw new ServiceException("通知ID不能为空");
        }
        CompetitionSceneNoticeVo existing = requireNotice(form.getNoticeId());
        CompetitionSceneNotice notice = buildNoticeForSave(form, existing);
        notice.setNoticeId(existing.getNoticeId());
        notice.setPublishStatus(existing.getPublishStatus());
        notice.setStatus(existing.getStatus());
        notice.setUpdateBy(SecurityUtils.getUsername());
        notice.setUpdateTime(DateUtils.getNowDate());
        int rows = noticeMapper.updateCompetitionSceneNotice(notice);
        replaceScheduleRelations(notice, form.getScheduleIds());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCompetitionSceneNoticeByIds(Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            return 0;
        }
        String username = SecurityUtils.getUsername();
        for (Long noticeId : noticeIds) {
            if (noticeId != null) {
                noticeMapper.deleteNoticeScheduleRelations(noticeId);
            }
        }
        return noticeMapper.deleteCompetitionSceneNoticeByIds(noticeIds, username);
    }

    @Override
    public int changePublishStatus(CompetitionSceneNoticeForm form) {
        if (form == null || form.getNoticeId() == null) {
            throw new ServiceException("通知ID不能为空");
        }
        String publishStatus = normalizeUpper(form.getPublishStatus());
        if (!CHANGEABLE_PUBLISH_STATUSES.contains(publishStatus)) {
            throw new ServiceException("仅支持将通知改为草稿或停用状态");
        }
        requireNotice(form.getNoticeId());
        return noticeMapper.updateNoticePublishStatus(form.getNoticeId(), publishStatus, null, SecurityUtils.getUsername());
    }

    @Override
    public int publishCompetitionSceneNotice(Long noticeId) {
        CompetitionSceneNoticeVo notice = requireNotice(noticeId);
        validateEffectiveTime(notice.getPublishTime(), notice.getExpireTime(), true);
        return noticeMapper.updateNoticePublishStatus(noticeId, PUBLISH_PUBLISHED, DateUtils.getNowDate(),
                SecurityUtils.getUsername());
    }

    private CompetitionSceneNotice buildNoticeForSave(CompetitionSceneNoticeForm form, CompetitionSceneNoticeVo existing) {
        if (form == null) {
            throw new ServiceException("通知信息不能为空");
        }
        CompetitionSceneNotice notice = new CompetitionSceneNotice();
        BeanUtils.copyProperties(form, notice);
        notice.setTitle(trimToNull(form.getTitle()));
        String rawContent = CompetitionSceneNoticeContentCodec.resolveContent(form);
        notice.setContent(CompetitionSceneNoticeHtmlSanitizer.sanitize(rawContent));
        notice.setNoticeLevel(normalizeUpper(form.getNoticeLevel()));
        notice.setIsTop("1".equals(form.getIsTop()) ? "1" : "0");
        notice.setSortNo(form.getSortNo() == null ? 100 : form.getSortNo());

        if (StringUtils.isBlank(notice.getTitle())) {
            throw new ServiceException("通知标题不能为空");
        }
        if (notice.getTitle().length() > 255) {
            throw new ServiceException("通知标题不能超过255个字符");
        }
        if (StringUtils.isBlank(notice.getContent())) {
            throw new ServiceException("通知内容不能为空");
        }
        if (!NOTICE_LEVELS.contains(notice.getNoticeLevel())) {
            notice.setNoticeLevel("NORMAL");
        }
        validateEffectiveTime(notice.getPublishTime(), notice.getExpireTime(), false);

        String noticeType = existing == null ? normalizeUpper(form.getNoticeType()) : existing.getNoticeType();
        notice.setNoticeType(noticeType);
        if (TYPE_PERSONAL.equals(noticeType)) {
            Long targetId = existing == null ? form.getTargetId() : existing.getTargetId();
            applyPersonalRecipient(notice, targetId);
        } else if (TYPE_ANNOUNCEMENT.equals(noticeType)) {
            applyAnnouncementScope(notice, form);
        } else {
            throw new ServiceException("不支持的通知类型");
        }
        return notice;
    }

    private void applyPersonalRecipient(CompetitionSceneNotice notice, Long targetId) {
        if (targetId == null) {
            throw new ServiceException("个人通知必须选择接收人");
        }
        CompetitionSceneScheduleTarget target = targetMapper.selectCompetitionSceneScheduleTargetById(targetId);
        if (target == null || "1".equals(target.getDelFlag()) || "1".equals(target.getStatus())) {
            throw new ServiceException("接收对象不存在或已停用");
        }
        if (target.getUserId() == null && target.getMemberId() == null) {
            throw new ServiceException("当前绑定对象未关联平台用户或报名成员，无法发送个人通知");
        }
        if (target.getCompetitionSeriesId() == null) {
            throw new ServiceException("当前绑定对象未关联赛事，无法发送个人通知");
        }
        Long competitionId = noticeMapper.selectCompetitionIdBySeriesId(target.getCompetitionSeriesId());
        if (competitionId == null) {
            throw new ServiceException("绑定对象关联的赛事不存在或已删除");
        }
        notice.setScopeType(SCOPE_PERSON);
        notice.setTargetId(target.getTargetId());
        notice.setCompetitionSeriesId(target.getCompetitionSeriesId());
        notice.setCompetitionId(competitionId);
        notice.setUserId(target.getUserId());
        notice.setMemberId(target.getMemberId());
        notice.setRecipientName(firstNotBlank(target.getUserName(), target.getTargetName(), target.getTeamName()));
    }

    private void applyAnnouncementScope(CompetitionSceneNotice notice, CompetitionSceneNoticeForm form) {
        String scopeType = normalizeUpper(form.getScopeType());
        if (!ANNOUNCEMENT_SCOPES.contains(scopeType)) {
            throw new ServiceException("大赛公告可见范围只能是赛事级或赛场级");
        }
        if (form.getCompetitionSeriesId() == null) {
            throw new ServiceException("大赛公告必须选择赛事");
        }
        Long competitionId = noticeMapper.selectCompetitionIdBySeriesId(form.getCompetitionSeriesId());
        if (competitionId == null) {
            throw new ServiceException("所选赛事不存在或已删除");
        }
        notice.setScopeType(scopeType);
        notice.setCompetitionSeriesId(form.getCompetitionSeriesId());
        notice.setCompetitionId(competitionId);
        notice.setTargetId(null);
        notice.setUserId(null);
        notice.setMemberId(null);
        notice.setRecipientName(null);
        validateScheduleScope(notice, form.getScheduleIds());
    }

    private void validateScheduleScope(CompetitionSceneNotice notice, List<Long> scheduleIds) {
        if (!SCOPE_SCHEDULE.equals(notice.getScopeType())) {
            return;
        }
        List<Long> distinctIds = distinctIds(scheduleIds);
        if (distinctIds.isEmpty()) {
            throw new ServiceException("赛场级公告至少选择一个赛场");
        }
        int matched = noticeMapper.countSchedulesInSeries(notice.getCompetitionSeriesId(), distinctIds);
        if (matched != distinctIds.size()) {
            throw new ServiceException("所选赛场不存在或不属于当前赛事");
        }
    }

    private void replaceScheduleRelations(CompetitionSceneNotice notice, List<Long> scheduleIds) {
        noticeMapper.deleteNoticeScheduleRelations(notice.getNoticeId());
        if (!TYPE_ANNOUNCEMENT.equals(notice.getNoticeType()) || !SCOPE_SCHEDULE.equals(notice.getScopeType())) {
            return;
        }
        List<CompetitionSceneNoticeSchedule> relations = distinctIds(scheduleIds).stream().map(scheduleId -> {
            CompetitionSceneNoticeSchedule relation = new CompetitionSceneNoticeSchedule();
            relation.setNoticeId(notice.getNoticeId());
            relation.setScheduleId(scheduleId);
            relation.setCreateBy(SecurityUtils.getUsername());
            relation.setCreateTime(DateUtils.getNowDate());
            return relation;
        }).collect(Collectors.toList());
        if (!relations.isEmpty()) {
            noticeMapper.batchInsertNoticeScheduleRelations(relations);
        }
    }

    private CompetitionSceneNoticeVo requireNotice(Long noticeId) {
        if (noticeId == null) {
            throw new ServiceException("通知ID不能为空");
        }
        CompetitionSceneNoticeVo notice = noticeMapper.selectCompetitionSceneNoticeById(noticeId);
        if (notice == null) {
            throw new ServiceException("通知不存在或已删除");
        }
        return notice;
    }

    private void fillScheduleIds(CompetitionSceneNoticeVo notice) {
        if (notice != null && SCOPE_SCHEDULE.equals(notice.getScopeType())) {
            notice.setScheduleIds(noticeMapper.selectNoticeScheduleIds(notice.getNoticeId()));
        }
    }

    private void sanitizeContent(CompetitionSceneNoticeVo notice) {
        if (notice != null) {
            notice.setContent(CompetitionSceneNoticeHtmlSanitizer.sanitize(notice.getContent()));
        }
    }

    private void validateEffectiveTime(Date publishTime, Date expireTime, boolean publishing) {
        if (publishTime != null && expireTime != null && !expireTime.after(publishTime)) {
            throw new ServiceException("失效时间必须晚于发布时间");
        }
        if (publishing && expireTime != null && !expireTime.after(new Date())) {
            throw new ServiceException("通知已超过失效时间，不能发布");
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private String normalizeUpper(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String trimToNull(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
