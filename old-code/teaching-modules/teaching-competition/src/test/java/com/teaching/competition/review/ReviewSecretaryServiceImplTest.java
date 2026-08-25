package com.teaching.competition.review;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.dto.ReviewSecretarySessionObjectStatusDTO;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewCheckinStatus;
import com.teaching.competition.review.enums.ReviewEventType;
import com.teaching.competition.review.enums.ReviewMemberRole;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewRecordStatus;
import com.teaching.competition.review.enums.ReviewSessionObjectStatus;
import com.teaching.competition.review.enums.ReviewSessionStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewRecordMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewSessionEventLogMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.service.impl.ReviewSecretaryServiceImpl;
import com.teaching.competition.review.vo.ReviewSecretarySessionObjectVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用评审模块第六包秘书现场控制台测试。
 */
public class ReviewSecretaryServiceImplTest {

    @Test
    public void secretaryCanViewOwnSessionAndScoreProgress() throws Exception {
        Fixture fixture = createFixture();

        List<ReviewSecretarySessionObjectVO> list = fixture.service.listSessionObjects(50L);

        Assert.assertEquals(3, list.size());
        Assert.assertEquals("1/2", list.get(0).getScoreProgress().getDisplayText());
        Assert.assertEquals("负责人甲", list.get(0).getLeaderName());
    }

    @Test(expected = ServiceException.class)
    public void nonSecretaryCannotOperateSession() throws Exception {
        Fixture fixture = createFixture();
        fixture.service.userId = 9L;

        fixture.service.getSessionDetail(50L);
    }

    @Test
    public void adminCanOperateAnySession() throws Exception {
        Fixture fixture = createFixture();
        fixture.service.userId = 1L;

        Assert.assertEquals(Long.valueOf(50L), fixture.service.getSessionDetail(50L).getSessionId());
    }

    @Test
    public void scanSetCurrentUpdatesSessionObjectAndWritesEvent() throws Exception {
        Fixture fixture = createFixture();
        ReviewSessionCurrentObjectDTO dto = new ReviewSessionCurrentObjectDTO();
        dto.setObjectId(10L);
        dto.setSourceType("SCAN");
        dto.setCertificateCode("CERT-10");

        ReviewSessionCurrentObjectVO vo = fixture.service.setCurrentObject(50L, dto);

        Assert.assertEquals(Long.valueOf(10L), vo.getObjectId());
        Assert.assertEquals(Long.valueOf(10L), fixture.session.getCurrentObjectId());
        Assert.assertEquals(ReviewSessionObjectStatus.REVIEWING.getCode(), fixture.so1.getReviewStatus());
        verify(fixture.sessionMapper).update(fixture.session);
        verify(fixture.sessionObjectMapper).update(fixture.so1);
        ArgumentCaptor<ReviewSessionEventLog> captor = ArgumentCaptor.forClass(ReviewSessionEventLog.class);
        verify(fixture.eventLogMapper).insert(captor.capture());
        Assert.assertEquals(ReviewEventType.SCAN_CERT.getCode(), captor.getValue().getEventType());
        Assert.assertEquals(Long.valueOf(2L), captor.getValue().getOperatorUserId());
    }

    @Test
    public void nextObjectSkipsAbsentSkippedAndCompletedObjects() throws Exception {
        Fixture fixture = createFixture();
        fixture.session.setCurrentObjectId(10L);
        fixture.so2.setCheckinStatus(ReviewCheckinStatus.ABSENT.getCode());
        fixture.so2.setReviewStatus(ReviewSessionObjectStatus.SKIPPED.getCode());
        fixture.so3.setCheckinStatus(ReviewCheckinStatus.PRESENT.getCode());
        fixture.so3.setReviewStatus(ReviewSessionObjectStatus.WAITING.getCode());

        ReviewSessionCurrentObjectVO vo = fixture.service.nextObject(50L);

        Assert.assertEquals(Long.valueOf(12L), vo.getObjectId());
        Assert.assertEquals(ReviewSessionObjectStatus.REVIEWING.getCode(), fixture.so3.getReviewStatus());
    }

    @Test(expected = ServiceException.class)
    public void skippedObjectCannotBeSetCurrent() throws Exception {
        Fixture fixture = createFixture();
        fixture.so1.setReviewStatus(ReviewSessionObjectStatus.SKIPPED.getCode());
        ReviewSessionCurrentObjectDTO dto = new ReviewSessionCurrentObjectDTO();
        dto.setObjectId(10L);
        dto.setSourceType("MANUAL");

        fixture.service.setCurrentObject(50L, dto);
    }

    @Test
    public void markAbsentSetsSkippedAndWritesAbsentEvent() throws Exception {
        Fixture fixture = createFixture();
        ReviewSecretarySessionObjectStatusDTO dto = new ReviewSecretarySessionObjectStatusDTO();
        dto.setCheckinStatus("ABSENT");

        fixture.service.updateSessionObjectStatus(100L, dto);

        Assert.assertEquals(ReviewCheckinStatus.ABSENT.getCode(), fixture.so1.getCheckinStatus());
        Assert.assertEquals(ReviewSessionObjectStatus.SKIPPED.getCode(), fixture.so1.getReviewStatus());
        ArgumentCaptor<ReviewSessionEventLog> captor = ArgumentCaptor.forClass(ReviewSessionEventLog.class);
        verify(fixture.eventLogMapper).insert(captor.capture());
        Assert.assertEquals(ReviewEventType.ABSENT.getCode(), captor.getValue().getEventType());
    }

    @Test
    public void markDelayedWritesDelayEventWithoutChangingAssignment() throws Exception {
        Fixture fixture = createFixture();
        ReviewSecretarySessionObjectStatusDTO dto = new ReviewSecretarySessionObjectStatusDTO();
        dto.setReviewStatus("DELAYED");

        fixture.service.updateSessionObjectStatus(100L, dto);

        Assert.assertEquals(ReviewSessionObjectStatus.DELAYED.getCode(), fixture.so1.getReviewStatus());
        ArgumentCaptor<ReviewSessionEventLog> captor = ArgumentCaptor.forClass(ReviewSessionEventLog.class);
        verify(fixture.eventLogMapper).insert(captor.capture());
        Assert.assertEquals(ReviewEventType.DELAY.getCode(), captor.getValue().getEventType());
    }

    private static Fixture createFixture() throws Exception {
        Fixture fixture = new Fixture();
        fixture.service = new TestReviewSecretaryServiceImpl();
        fixture.sessionMapper = mock(ReviewSessionMapper.class);
        fixture.sessionObjectMapper = mock(ReviewSessionObjectMapper.class);
        fixture.eventLogMapper = mock(ReviewSessionEventLogMapper.class);
        fixture.objectMapper = mock(ReviewObjectMapper.class);
        fixture.objectMemberMapper = mock(ReviewObjectMemberMapper.class);
        fixture.assignmentMapper = mock(ReviewAssignmentMapper.class);
        fixture.recordMapper = mock(ReviewRecordMapper.class);
        fixture.activityMapper = mock(ReviewActivityMapper.class);
        fixture.roundMapper = mock(ReviewRoundMapper.class);

        setField(fixture.service, "sessionMapper", fixture.sessionMapper);
        setField(fixture.service, "sessionObjectMapper", fixture.sessionObjectMapper);
        setField(fixture.service, "eventLogMapper", fixture.eventLogMapper);
        setField(fixture.service, "objectMapper", fixture.objectMapper);
        setField(fixture.service, "objectMemberMapper", fixture.objectMemberMapper);
        setField(fixture.service, "assignmentMapper", fixture.assignmentMapper);
        setField(fixture.service, "recordMapper", fixture.recordMapper);
        setField(fixture.service, "activityMapper", fixture.activityMapper);
        setField(fixture.service, "roundMapper", fixture.roundMapper);

        fixture.session = session();
        fixture.object1 = object(10L, "OBJ-10");
        fixture.object2 = object(11L, "OBJ-11");
        fixture.object3 = object(12L, "OBJ-12");
        fixture.so1 = sessionObject(100L, 10L, 1);
        fixture.so2 = sessionObject(101L, 11L, 2);
        fixture.so3 = sessionObject(102L, 12L, 3);
        fixture.sessionObjects = new ArrayList<>();
        fixture.sessionObjects.add(fixture.so1);
        fixture.sessionObjects.add(fixture.so2);
        fixture.sessionObjects.add(fixture.so3);

        when(fixture.sessionMapper.selectById(50L)).thenReturn(fixture.session);
        when(fixture.objectMapper.selectById(10L)).thenReturn(fixture.object1);
        when(fixture.objectMapper.selectById(11L)).thenReturn(fixture.object2);
        when(fixture.objectMapper.selectById(12L)).thenReturn(fixture.object3);
        when(fixture.activityMapper.selectById(1L)).thenReturn(activity());
        when(fixture.roundMapper.selectById(5L)).thenReturn(round());
        when(fixture.sessionObjectMapper.selectById(100L)).thenReturn(fixture.so1);
        when(fixture.sessionObjectMapper.selectList(any())).thenAnswer(invocation -> {
            ReviewSessionObject query = invocation.getArgument(0);
            if (query.getObjectId() != null) {
                for (ReviewSessionObject item : fixture.sessionObjects) {
                    if (query.getObjectId().equals(item.getObjectId())) {
                        return Collections.singletonList(item);
                    }
                }
                return Collections.emptyList();
            }
            return fixture.sessionObjects;
        });
        when(fixture.objectMemberMapper.selectList(any())).thenReturn(Collections.singletonList(member()));
        when(fixture.assignmentMapper.selectList(any())).thenReturn(assignments());
        when(fixture.recordMapper.selectList(any())).thenAnswer(invocation -> {
            ReviewRecord query = invocation.getArgument(0);
            if (Long.valueOf(1000L).equals(query.getAssignmentId())) {
                return Collections.singletonList(record(ReviewRecordStatus.SUBMITTED.getCode()));
            }
            return Collections.emptyList();
        });
        return fixture;
    }

    private static ReviewSession session() {
        ReviewSession session = new ReviewSession();
        session.setId(50L);
        session.setActivityId(1L);
        session.setRoundId(5L);
        session.setSessionName("第六包现场联调");
        session.setSecretaryUserId(2L);
        session.setStatus(ReviewSessionStatus.NOT_STARTED.getCode());
        return session;
    }

    private static ReviewObject object(Long id, String code) {
        ReviewObject object = new ReviewObject();
        object.setId(id);
        object.setActivityId(1L);
        object.setObjectCode(code);
        object.setObjectName("评审对象" + id);
        object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
        return object;
    }

    private static ReviewSessionObject sessionObject(Long id, Long objectId, Integer sequenceNo) {
        ReviewSessionObject item = new ReviewSessionObject();
        item.setId(id);
        item.setActivityId(1L);
        item.setRoundId(5L);
        item.setSessionId(50L);
        item.setObjectId(objectId);
        item.setSequenceNo(sequenceNo);
        item.setCheckinStatus(ReviewCheckinStatus.WAITING.getCode());
        item.setReviewStatus(ReviewSessionObjectStatus.WAITING.getCode());
        return item;
    }

    private static ReviewActivity activity() {
        ReviewActivity activity = new ReviewActivity();
        activity.setId(1L);
        activity.setActivityName("评审测试活动");
        return activity;
    }

    private static ReviewRound round() {
        ReviewRound round = new ReviewRound();
        round.setId(5L);
        round.setRoundName("现场答辩");
        return round;
    }

    private static ReviewObjectMember member() {
        ReviewObjectMember member = new ReviewObjectMember();
        member.setMemberName("负责人甲");
        member.setMemberRole(ReviewMemberRole.LEADER.getCode());
        member.setIsPrimary("Y");
        return member;
    }

    private static List<ReviewAssignment> assignments() {
        ReviewAssignment a1 = new ReviewAssignment();
        a1.setId(1000L);
        a1.setStatus(ReviewAssignmentStatus.ASSIGNED.getCode());
        ReviewAssignment a2 = new ReviewAssignment();
        a2.setId(1001L);
        a2.setStatus(ReviewAssignmentStatus.ASSIGNED.getCode());
        List<ReviewAssignment> assignments = new ArrayList<>();
        assignments.add(a1);
        assignments.add(a2);
        return assignments;
    }

    private static ReviewRecord record(String status) {
        ReviewRecord record = new ReviewRecord();
        record.setRecordStatus(status);
        return record;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static class TestReviewSecretaryServiceImpl extends ReviewSecretaryServiceImpl {
        private Long userId = 2L;

        @Override
        protected Long currentUserId() {
            return userId;
        }

        @Override
        protected String currentUsername() {
            return "review-secretary-test";
        }
    }

    private static class Fixture {
        TestReviewSecretaryServiceImpl service;
        ReviewSessionMapper sessionMapper;
        ReviewSessionObjectMapper sessionObjectMapper;
        ReviewSessionEventLogMapper eventLogMapper;
        ReviewObjectMapper objectMapper;
        ReviewObjectMemberMapper objectMemberMapper;
        ReviewAssignmentMapper assignmentMapper;
        ReviewRecordMapper recordMapper;
        ReviewActivityMapper activityMapper;
        ReviewRoundMapper roundMapper;
        ReviewSession session;
        ReviewObject object1;
        ReviewObject object2;
        ReviewObject object3;
        ReviewSessionObject so1;
        ReviewSessionObject so2;
        ReviewSessionObject so3;
        List<ReviewSessionObject> sessionObjects;
    }
}
