package com.teaching.competition.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Removes internal persistence, audit, ownership and publisher fields from
 * anonymous competition API responses.
 *
 * <p>The competition detail graph contains several nested domain entities.
 * Sanitizing the complete JSON tree at the anonymous boundary preserves the
 * existing public business payload while preventing nested entities from
 * reintroducing internal fields.</p>
 */
@Component
public class PublicApiResponseSanitizer {

    private static final Set<String> INTERNAL_FIELDS = Set.of(
            "searchValue",
            "createBy",
            "createTime",
            "updateBy",
            "updateTime",
            "remark",
            "params",
            "version",
            "delFlag",
            "userId",
            "orgId",
            "publishPerson",
            "publishPersonName",
            "applyReason"
    );

    private final ObjectMapper objectMapper;

    public PublicApiResponseSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode sanitize(Object source) {
        JsonNode root = objectMapper.valueToTree(source);
        removeInternalFields(root);
        return root;
    }

    private void removeInternalFields(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : (ArrayNode) node) {
                removeInternalFields(child);
            }
            return;
        }

        ObjectNode objectNode = (ObjectNode) node;
        INTERNAL_FIELDS.forEach(objectNode::remove);
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            removeInternalFields(fields.next().getValue());
        }
    }
}
