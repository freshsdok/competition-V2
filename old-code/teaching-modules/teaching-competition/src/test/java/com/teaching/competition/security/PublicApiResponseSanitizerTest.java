package com.teaching.competition.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionStageConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class PublicApiResponseSanitizerTest {

    private static final Set<String> FORBIDDEN_FIELDS = Set.of(
            "createBy", "createTime", "updateBy", "updateTime", "remark", "params",
            "version", "delFlag", "userId", "orgId", "publishPerson",
            "publishPersonName", "applyReason"
    );

    private final PublicApiResponseSanitizer sanitizer =
            new PublicApiResponseSanitizer(new ObjectMapper());

    @Test
    public void removesSensitiveFieldsFromTopLevelAndNestedEntities() {
        CompetitionDetailInfo detail = new CompetitionDetailInfo();
        detail.setCompetitionId(42L);
        detail.setCompetitionName("公开赛事");
        detail.setCreateBy("internal-admin");
        detail.setUserId(99L);
        detail.setOrgId(7L);
        detail.setPublishPerson(1L);
        detail.setPublishPersonName("admin");

        CompetitionStageConfig stage = new CompetitionStageConfig();
        stage.setStageId("stage-1");
        stage.setStageName("决赛");
        stage.setUserId(99L);
        stage.setOrgId(7L);
        stage.setCreateBy("internal-admin");
        detail.setCompetitionStageList(List.of(stage));

        JsonNode result = sanitizer.sanitize(detail);

        Assert.assertEquals(42L, result.path("competitionId").asLong());
        Assert.assertEquals("公开赛事", result.path("competitionName").asText());
        Assert.assertEquals("决赛",
                result.path("competitionStageList").path(0).path("stageName").asText());
        assertNoForbiddenFields(result);
    }

    @Test
    public void preservesNullAsJsonNull() {
        Assert.assertTrue(sanitizer.sanitize(null).isNull());
    }

    private static void assertNoForbiddenFields(JsonNode node) {
        if (node == null || node.isValueNode()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(PublicApiResponseSanitizerTest::assertNoForbiddenFields);
            return;
        }
        node.fieldNames().forEachRemaining(name ->
                Assert.assertFalse("Unexpected internal field: " + name, FORBIDDEN_FIELDS.contains(name)));
        node.elements().forEachRemaining(PublicApiResponseSanitizerTest::assertNoForbiddenFields);
    }
}
