package com.teaching.competition.review;

import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.domain.ReviewResultPublishLog;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewResultPublishDTO;
import com.teaching.competition.review.dto.ReviewResultRevokeDTO;
import com.teaching.competition.review.enums.ReviewResultStatus;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewResultMapper;
import com.teaching.competition.review.mapper.ReviewResultPublishLogMapper;
import com.teaching.competition.review.service.impl.ReviewResultServiceImpl;
import com.teaching.competition.review.vo.ReviewResultGenerateItemVO;
import com.teaching.competition.review.vo.ReviewResultGenerateResponseVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用评审模块第七包结果汇总和发布流程测试。
 */
public class ReviewResultServiceImplTest {

    @Test
    public void generateSkipsObjectsWithoutSubmittedScore() throws Exception {
        Fixture fixture = createFixture(Collections.singletonList(item(10L, 2, 0, null)));

        ReviewResultGenerateResponseVO response = fixture.service.generate(generateDTO());

        Assert.assertEquals(0, response.getGeneratedCount().intValue());
        Assert.assertEquals(1, response.getSkippedCount().intValue());
        Assert.assertTrue(response.getWarnings().get(0).contains("暂无已提交评分"));
        verify(fixture.resultMapper, never()).insert(any(ReviewResult.class));
    }

    @Test
    public void generateAverageResultsWarnsIncompleteAndRefreshesRank() throws Exception {
        List<ReviewResultGenerateItemVO> items = Arrays.asList(
                item(10L, 2, 1, new BigDecimal("90.00")),
                item(11L, 1, 1, new BigDecimal("95.00"))
        );
        Fixture fixture = createFixture(items);

        ReviewResultGenerateResponseVO response = fixture.service.generate(generateDTO());

        Assert.assertEquals(2, response.getGeneratedCount().intValue());
        Assert.assertEquals(1, response.getWarningCount().intValue());
        Assert.assertTrue(response.getWarnings().get(0).contains("评分尚未完成"));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> rankCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(fixture.resultMapper, times(2)).updateRank(idCaptor.capture(), rankCaptor.capture(), anyString());
        Assert.assertEquals(Long.valueOf(1011L), idCaptor.getAllValues().get(0));
        Assert.assertEquals(Integer.valueOf(1), rankCaptor.getAllValues().get(0));
        Assert.assertEquals(Long.valueOf(1010L), idCaptor.getAllValues().get(1));
        Assert.assertEquals(Integer.valueOf(2), rankCaptor.getAllValues().get(1));
    }

    @Test
    public void publishAndRevokeWritePublishLogAndAudit() throws Exception {
        Fixture fixture = createFixture(Collections.emptyList());
        ReviewResult result = result(70L, 10L, new BigDecimal("92.00"));
        when(fixture.resultMapper.selectById(70L)).thenReturn(result);

        ReviewResultPublishDTO publishDTO = new ReviewResultPublishDTO();
        publishDTO.setPublishContent("发布给填报人");
        fixture.service.publish(70L, publishDTO);

        Assert.assertEquals(ReviewResultStatus.PUBLISHED.getCode(), result.getResultStatus());

        ReviewResultRevokeDTO revokeDTO = new ReviewResultRevokeDTO();
        revokeDTO.setRevokeReason("重新核验结论");
        fixture.service.revoke(70L, revokeDTO);

        Assert.assertEquals(ReviewResultStatus.REVOKED.getCode(), result.getResultStatus());
        verify(fixture.publishLogMapper, times(2)).insert(any(ReviewResultPublishLog.class));
        verify(fixture.auditLogMapper, times(2)).insert(any(ReviewAuditLog.class));
    }

    private static Fixture createFixture(List<ReviewResultGenerateItemVO> generateItems) throws Exception {
        Fixture fixture = new Fixture();
        fixture.service = new TestReviewResultServiceImpl();
        fixture.resultMapper = mock(ReviewResultMapper.class);
        fixture.publishLogMapper = mock(ReviewResultPublishLogMapper.class);
        fixture.auditLogMapper = mock(ReviewAuditLogMapper.class);
        fixture.results = new ArrayList<>();

        setField(fixture.service, "mapper", fixture.resultMapper);
        setField(fixture.service, "publishLogMapper", fixture.publishLogMapper);
        setField(fixture.service, "auditLogMapper", fixture.auditLogMapper);

        when(fixture.resultMapper.selectGenerateItems(any())).thenReturn(generateItems);
        when(fixture.resultMapper.selectList(any())).thenAnswer(invocation -> {
            ReviewResult query = invocation.getArgument(0);
            if (query.getObjectId() != null) {
                return Collections.emptyList();
            }
            return fixture.results;
        });
        when(fixture.resultMapper.insert(any(ReviewResult.class))).thenAnswer(invocation -> {
            ReviewResult result = invocation.getArgument(0);
            result.setId(1000L + result.getObjectId());
            fixture.results.add(result);
            return 1;
        });
        when(fixture.resultMapper.updateRank(anyLong(), any(), anyString())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Integer rank = invocation.getArgument(1);
            for (ReviewResult result : fixture.results) {
                if (id.equals(result.getId())) {
                    result.setCalculatedRank(rank);
                }
            }
            return 1;
        });
        return fixture;
    }

    private static ReviewResultGenerateDTO generateDTO() {
        ReviewResultGenerateDTO dto = new ReviewResultGenerateDTO();
        dto.setActivityId(1L);
        dto.setRoundId(2L);
        return dto;
    }

    private static ReviewResultGenerateItemVO item(Long objectId, int assignedCount, int submittedCount, BigDecimal score) {
        ReviewResultGenerateItemVO item = new ReviewResultGenerateItemVO();
        item.setActivityId(1L);
        item.setRoundId(2L);
        item.setObjectId(objectId);
        item.setObjectCode("OBJ-" + objectId);
        item.setObjectName("项目" + objectId);
        item.setAssignedCount(assignedCount);
        item.setSubmittedCount(submittedCount);
        item.setCalculatedScore(score);
        return item;
    }

    private static ReviewResult result(Long id, Long objectId, BigDecimal score) {
        ReviewResult result = new ReviewResult();
        result.setId(id);
        result.setActivityId(1L);
        result.setRoundId(2L);
        result.setObjectId(objectId);
        result.setCalculatedScore(score);
        result.setResultStatus(ReviewResultStatus.GENERATED.getCode());
        return result;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static class TestReviewResultServiceImpl extends ReviewResultServiceImpl {
        @Override
        protected Long currentUserId() {
            return 201L;
        }

        @Override
        protected String currentUsername() {
            return "result-admin";
        }
    }

    private static class Fixture {
        private TestReviewResultServiceImpl service;
        private ReviewResultMapper resultMapper;
        private ReviewResultPublishLogMapper publishLogMapper;
        private ReviewAuditLogMapper auditLogMapper;
        private List<ReviewResult> results;
    }
}
