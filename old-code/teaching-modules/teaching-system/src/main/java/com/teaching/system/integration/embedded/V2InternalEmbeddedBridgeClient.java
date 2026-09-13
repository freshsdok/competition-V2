package com.teaching.system.integration.embedded;

import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * V1服务端只证明“哪个已登录旧账号请求哪个固定能力”，不会把V1角色、组织或权限转交给V2。
 * HMAC覆盖目标与来源键，内网地址本身不被当作认证凭据。
 */
@Component
@ConditionalOnProperty(name = "v2.internal-embedded.enabled", havingValue = "true")
public class V2InternalEmbeddedBridgeClient {
    private static final String PATH = "/internal/v1-bridge/sessions";
    private static final String CALLER = "V1_PRODUCTION_BRIDGE";
    private static final String SOURCE = "V1_SYS_USER";
    private final RestTemplate http;
    private final String internalBase;
    private final String sameOriginPrefix;
    private final byte[] secret;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public V2InternalEmbeddedBridgeClient(
            RestTemplateBuilder builder,
            @Value("${v2.internal-embedded.internal-base-url:}") String internalBase,
            @Value("${v2.internal-embedded.same-origin-prefix:/v2-embedded}") String sameOriginPrefix,
            @Value("${v2.internal-embedded.allow-private-http:false}") boolean allowPrivateHttp,
            @Value("${v2.internal-embedded.connect-timeout:3s}") Duration connectTimeout,
            @Value("${v2.internal-embedded.read-timeout:5s}") Duration readTimeout) {
        this(builder, internalBase, sameOriginPrefix, allowPrivateHttp, connectTimeout, readTimeout,
                System.getenv("V1_V2_INTERNAL_BRIDGE_HMAC_SECRET"));
    }

    V2InternalEmbeddedBridgeClient(RestTemplateBuilder builder, String internalBase, String sameOriginPrefix,
            boolean allowPrivateHttp, Duration connectTimeout, Duration readTimeout, String rawSecret) {
        this.http = builder.setConnectTimeout(connectTimeout).setReadTimeout(readTimeout).build();
        this.internalBase = fixedBase(internalBase, allowPrivateHttp);
        if (!sameOriginPrefix.matches("/[A-Za-z0-9/_-]+") || sameOriginPrefix.contains("..")) {
            throw new IllegalStateException("V2 embedded proxy prefix must be a fixed same-origin path");
        }
        this.sameOriginPrefix = sameOriginPrefix.replaceAll("/+$", "");
        if (rawSecret == null || rawSecret.length() < 32) {
            throw new IllegalStateException("V1/V2 internal bridge HMAC secret is unavailable");
        }
        this.secret = rawSecret.getBytes(StandardCharsets.UTF_8);
    }

    public List<Discovery> discover() {
        try {
            Discovery[] rows = http.getForObject(internalBase + "/api/v1/public/credential-exchange/offerings", Discovery[].class);
            return rows == null ? List.of() : List.of(rows);
        } catch (RestClientException unavailable) {
            // 公开卡片故障只隐藏卡片，不拖垮V1首页；不能把异常正文或内部地址写入日志。
            return List.of();
        }
    }

    public Entry settlement(long authenticatedV1UserId) {
        return issue(authenticatedV1UserId, Target.SETTLEMENT_PROFILE, null);
    }

    public Entry credential(long authenticatedV1UserId, long offeringId) {
        if (offeringId <= 0) throw new IllegalArgumentException("Credential offering is required");
        return issue(authenticatedV1UserId, Target.CREDENTIAL_EXCHANGE, offeringId);
    }

    private Entry issue(long userId, Target target, Long resourceId) {
        if (userId <= 0) throw new IllegalArgumentException("Authenticated V1 account required");
        long timestamp = Instant.now().getEpochSecond();
        String nonce = randomToken(24), requestId = UUID.randomUUID().toString();
        Issue body = new Issue(SOURCE, userId, target, resourceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-V2-Bridge-Caller", CALLER);
        headers.set("X-V2-Bridge-Timestamp", Long.toString(timestamp));
        headers.set("X-V2-Bridge-Nonce", nonce);
        headers.set("X-V2-Bridge-Request-Id", requestId);
        headers.set("X-V2-Bridge-Target", target.name());
        headers.set("X-V2-Bridge-Signature", sign(canonical(timestamp, nonce, requestId, target, userId, resourceId)));
        Issued issued = http.postForObject(internalBase + PATH, new HttpEntity<>(body, headers), Issued.class);
        if (issued == null || issued.targetCapability() != target || issued.expiresAt() == null
                || issued.embeddedPath() == null || !issued.embeddedPath().matches("/embedded/[A-Za-z0-9/_-]+#bootstrap=[A-Za-z0-9_-]{43}")) {
            throw new IllegalStateException("V2 embedded bridge response invalid");
        }
        return new Entry(sameOriginPrefix + issued.embeddedPath(), issued.expiresAt());
    }

    private String canonical(long timestamp, String nonce, String requestId, Target target, long userId, Long resourceId) {
        return String.join("\n", "POST", PATH, CALLER, Long.toString(timestamp), nonce, requestId,
                target.name(), SOURCE, Long.toString(userId), resourceId == null ? "" : resourceId.toString());
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException failure) {
            throw new IllegalStateException("Bridge cryptography unavailable", failure);
        }
    }

    private String randomToken(int size) {
        byte[] value = new byte[size];
        random.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    static String fixedBase(String raw, boolean allowPrivateHttp) {
        URI uri = URI.create(raw == null ? "" : raw.trim());
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        boolean privateHttp = allowPrivateHttp && "http".equalsIgnoreCase(uri.getScheme()) && privateAddress(uri.getHost());
        if ((!https && !privateHttp) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalStateException("V2 internal bridge requires HTTPS or an explicitly enabled private HTTP endpoint");
        }
        return uri.toString().replaceAll("/+$", "");
    }

    private static boolean privateAddress(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress();
        } catch (Exception invalid) { return false; }
    }

    public enum Target { SETTLEMENT_PROFILE, CREDENTIAL_EXCHANGE }
    public record Discovery(long offeringId, String displayName, String targetCredentialName,
                            Instant startsAt, Instant endsAt, boolean free) {}
    public record Entry(String entryPath, Instant expiresAt) { @Override public String toString(){return "Entry[redacted]";} }
    private record Issue(String sourceSystem, long sourceUserId, Target targetCapability, Long targetResourceId) {}
    private record Issued(String bootstrapReference, Target targetCapability, Long targetResourceId,
                          String embeddedPath, Instant expiresAt) {}
}
