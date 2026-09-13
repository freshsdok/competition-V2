package com.teaching.system.integration.embedded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

/** 本地Wire测试只使用合成来源键和密钥，验证V1发出的HMAC覆盖固定目标且返回同源相对路径。 */
public class V2InternalEmbeddedBridgeClientTest {
    private static final String SECRET = "synthetic-v1-v2-embedded-secret-32-plus";

    @Test
    public void signsExactSettlementWireAndReturnsOnlySameOriginPath() throws Exception {
        AtomicReference<String> signature = new AtomicReference<>(), canonical = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/v1-bridge/sessions", exchange -> {
            String target = exchange.getRequestHeaders().getFirst("X-V2-Bridge-Target");
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("SETTLEMENT_PROFILE", target);
            assertTrue(body.contains("\"sourceUserId\":77"));
            signature.set(exchange.getRequestHeaders().getFirst("X-V2-Bridge-Signature"));
            canonical.set(String.join("\n", "POST", "/internal/v1-bridge/sessions",
                    exchange.getRequestHeaders().getFirst("X-V2-Bridge-Caller"),
                    exchange.getRequestHeaders().getFirst("X-V2-Bridge-Timestamp"),
                    exchange.getRequestHeaders().getFirst("X-V2-Bridge-Nonce"),
                    exchange.getRequestHeaders().getFirst("X-V2-Bridge-Request-Id"), target,
                    "V1_SYS_USER", "77", ""));
            byte[] response = ("{\"bootstrapReference\":\"" + "a".repeat(43)
                    + "\",\"targetCapability\":\"SETTLEMENT_PROFILE\",\"embeddedPath\":"
                    + "\"/embedded/settlement-profile#bootstrap=" + "a".repeat(43)
                    + "\",\"expiresAt\":\"2026-09-12T12:03:00Z\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length); exchange.getResponseBody().write(response); exchange.close();
        });
        server.start();
        try {
            var client = new V2InternalEmbeddedBridgeClient(new RestTemplateBuilder(),
                    "http://127.0.0.1:" + server.getAddress().getPort(), "/v2-embedded", true,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), SECRET);
            var entry = client.settlement(77);
            assertTrue(entry.entryPath().startsWith("/v2-embedded/embedded/settlement-profile#bootstrap="));
            assertEquals(sign(canonical.get()), signature.get());
        } finally { server.stop(0); }
    }

    @Test
    public void rejectsPublicHttpAndArbitraryProxyPrefix() {
        assertThrows(IllegalStateException.class, () -> V2InternalEmbeddedBridgeClient.fixedBase("http://example.com", true));
        assertThrows(IllegalStateException.class, () -> new V2InternalEmbeddedBridgeClient(new RestTemplateBuilder(),
                "https://v2.example.test", "https://attacker.test", false,
                Duration.ofSeconds(1), Duration.ofSeconds(1), SECRET));
    }

    private static String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
