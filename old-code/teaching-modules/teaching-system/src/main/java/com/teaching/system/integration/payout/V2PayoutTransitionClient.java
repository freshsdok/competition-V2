package com.teaching.system.integration.payout;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * V1仅用当前已认证用户的稳定来源键向V2换取一次性入口码。
 * 适配器不读取获奖金额、不复制V1 Session/角色/权限，也不在V1保存银行或发票资料，便于迁移完成后整包删除。
 */
@Component
@ConditionalOnProperty(name = "v2.payout-transition.enabled", havingValue = "true")
public class V2PayoutTransitionClient {
    private final RestTemplate restTemplate;
    private final URI issueEndpoint;
    private final String webBaseUrl;
    private final String serviceCredential;
    private final long campaignId;

    public V2PayoutTransitionClient(
            RestTemplateBuilder builder,
            @Value("${v2.payout-transition.api-base-url:}") String apiBaseUrl,
            @Value("${v2.payout-transition.web-base-url:}") String webBaseUrl,
            @Value("${v2.payout-transition.campaign-id:0}") long campaignId,
            @Value("${v2.payout-transition.allow-insecure-http:false}") boolean allowInsecureHttp) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.issueEndpoint = endpointUri(apiBaseUrl, "/api/v1/public/payout/transitions", allowInsecureHttp);
        this.webBaseUrl = endpointBase(webBaseUrl, allowInsecureHttp);
        this.campaignId = campaignId;
        this.serviceCredential = requireEnvironmentSecret("V2_PAYOUT_BRIDGE_CREDENTIAL");
        if (campaignId <= 0) {
            throw new IllegalStateException("V2_PAYOUT_CAMPAIGN_ID must identify the approved payout campaign");
        }
    }

    public EntryLink issue(String sourceUserKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Payout-Bridge-Credential", serviceCredential);
        IssueResponse response = restTemplate.postForObject(issueEndpoint,
                new HttpEntity<>(Map.of("sourceSystem", "DESHI_V1", "sourceUserKey", sourceUserKey,
                        "campaignId", campaignId), headers), IssueResponse.class);
        if (response == null || response.code() == null || response.code().isBlank()
                || response.expiresAt() == null || !"SETTLEMENT_PAYOUT".equals(response.purpose())) {
            throw new IllegalStateException("V2 payout transition response is invalid");
        }
        String entryUrl = UriComponentsBuilder.fromHttpUrl(webBaseUrl)
                .path("/payout/transition")
                .queryParam("code", response.code())
                .build(true)
                .toUriString();
        return new EntryLink(entryUrl, response.expiresAt(), "V2_PAYOUT_ENTRY_READY");
    }

    private static URI endpointUri(String baseUrl, String path, boolean allowInsecureHttp) {
        return URI.create(endpointBase(baseUrl, allowInsecureHttp) + path);
    }

    static String endpointBase(String value, boolean allowInsecureHttp) {
        String normalized = value == null ? "" : value.trim().replaceAll("/+$", "");
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("V2 payout transition endpoint is invalid", exception);
        }
        if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null) {
            return normalized;
        }
        if (allowInsecureHttp && "http".equalsIgnoreCase(uri.getScheme()) && isLoopbackHost(uri.getHost())) {
            return normalized;
        }
        throw new IllegalStateException(
                "V2 payout transition endpoints must use HTTPS; HTTP is allowed only for loopback development endpoints");
    }

    private static boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                || "::1".equals(host) || "[::1]".equals(host);
    }

    private static String requireEnvironmentSecret(String name) {
        String value = System.getenv(name);
        if (value == null || value.length() < 32) {
            throw new IllegalStateException(name + " is missing or too weak");
        }
        return value;
    }

    public record EntryLink(String entryUrl, Instant expiresAt, String status) {}
    private record IssueResponse(String code, Instant expiresAt, String purpose) {}
}
