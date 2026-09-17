package com.teaching.system.integration.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.system.mapper.OrderInfoMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "v2.commerce-callback.enabled", havingValue = "true")
public class V2CommerceCallbackDispatcher {
    private final OrderInfoMapper orders;
    private final V2CommerceCallbackClient client;
    private final ObjectMapper json = new ObjectMapper();

    public V2CommerceCallbackDispatcher(OrderInfoMapper orders, V2CommerceCallbackClient client) {
        this.orders = orders;
        this.client = client;
    }

    public Optional<Map<String, String>> forwardPaymentIfUnknown(Map<String, String> fields) {
        String orderId = candidateOrder(fields);
        if (orderId == null || orders.selectOrderInfoByOrderId(orderId) != null) return Optional.empty();
        // V1 also uses UUIDs. Existence in V1 takes precedence; V2 must independently authenticate unknown orders.
        log.info("V1_CMB_V2_FORWARD_START orderId={}", orderId);
        try {
            Map<String, String> ack = client.forwardPayment(new LinkedHashMap<>(fields));
            log.info("V1_CMB_V2_FORWARD_RESULT orderId={} returnCode={} respCode={}",
                    orderId, ack.get("returnCode"), ack.get("respCode"));
            return Optional.of(ack);
        } catch (RuntimeException failure) {
            log.warn("V1_CMB_V2_FORWARD_FAILED orderId={}", orderId);
            // Never fall back to legacy settlement or invent a SUCCESS acknowledgement.
            throw new IllegalStateException("V2 payment callback forwarding failed");
        }
    }

    private String candidateOrder(Map<String, String> fields) {
        if (fields == null || fields.get("sign") == null || fields.get("sign").isBlank()) return null;
        try {
            JsonNode business = fields.containsKey("biz_content")
                    ? json.readTree(fields.get("biz_content")) : json.valueToTree(fields);
            if (business == null || !business.isObject() || business.has("refundAmt")) return null;
            JsonNode order = business.get("orderId");
            if (order == null || !order.isTextual()) return null;
            String id = order.textValue();
            return id.matches("(?:[0-9A-Fa-f]{32}|V2[0-9A-Fa-f]{30})") ? id : null;
        } catch (Exception malformed) {
            return null;
        }
    }
}
