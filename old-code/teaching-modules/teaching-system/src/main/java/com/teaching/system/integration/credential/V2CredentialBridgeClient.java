package com.teaching.system.integration.credential;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** V1只使用自己的登录主体行键请求入口，不读取V2数据库，也不传递旧JWT、角色或人员资料。 */
@Component
@ConditionalOnProperty(name="v2.credential-transition.enabled",havingValue="true")
public class V2CredentialBridgeClient {
    private final RestTemplate http;
    private final String apiBase,webBase,credential;
    public V2CredentialBridgeClient(RestTemplateBuilder builder, String apiBase, String webBase, String credential) {
        this(builder, apiBase, webBase, credential, false);
    }
    @Autowired
    public V2CredentialBridgeClient(RestTemplateBuilder builder,
            @Value("${v2.credential-transition.api-base-url:}") String apiBase,
            @Value("${v2.credential-transition.web-base-url:}") String webBase,
            @Value("${V2_CREDENTIAL_BRIDGE_CREDENTIAL:}") String credential,
            @Value("${v2.credential-transition.allow-loopback-http:false}") boolean allowLoopbackHttp) {
        this.http=builder.setConnectTimeout(Duration.ofSeconds(3)).setReadTimeout(Duration.ofSeconds(5)).build();
        this.apiBase=base(apiBase,allowLoopbackHttp);this.webBase=base(webBase,allowLoopbackHttp);this.credential=credential;
        if(credential.length()<32) throw new IllegalStateException("Credential bridge secret is unavailable");
    }
    public List<Discovery> discover() {
        try {
            Discovery[] rows=http.getForObject(apiBase+"/api/v1/public/credential-exchange/offerings",Discovery[].class);
            return rows==null?List.of():List.of(rows);
        } catch(RestClientException unavailable) {
            // V2故障只影响此卡片，不拖垮V1首页；不记录可能含请求信息的异常正文。
            return List.of();
        }
    }
    public Entry issue(long authenticatedV1UserId) {
        if(authenticatedV1UserId<=0) throw new IllegalArgumentException("Authenticated V1 account required");
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Credential-Bridge-Credential",credential);
        Issued result=http.postForObject(apiBase+"/api/v1/public/credential-transitions",
                new HttpEntity<>(new Issue("V1_SYS_USER",authenticatedV1UserId),headers),Issued.class);
        if(result==null || !"CREDENTIAL_EXCHANGE".equals(result.purpose()) || result.code()==null
                || !result.code().matches("[A-Za-z0-9_-]{43}") || result.expiresAt()==null)
            throw new IllegalStateException("Credential bridge response invalid");
        // Fragment不进入HTTP访问日志及Referer；V2消费前会立即从地址栏删除。
        return new Entry(webBase+"/credential/transition#code="+result.code(),result.expiresAt());
    }
    private static String base(String raw, boolean allowLoopbackHttp) {
        URI uri=URI.create(raw.trim());
        boolean localHttp = allowLoopbackHttp && "http".equals(uri.getScheme())
                && List.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
        if((!"https".equals(uri.getScheme()) && !localHttp) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null)
            throw new IllegalStateException("Credential bridge requires a fixed HTTPS base URL");
        return uri.toString().replaceAll("/+$","");
    }
    public record Discovery(long offeringId,String displayName,String targetCredentialName,Instant startsAt,Instant endsAt) {}
    public record Entry(String entryUrl,Instant expiresAt) { @Override public String toString(){return "Entry[redacted]";} }
    private record Issue(String sourceSystem,long sourceUserId) { @Override public String toString(){return "Issue[redacted]";} }
    private record Issued(String code,String purpose,Instant expiresAt) { @Override public String toString(){return "Issued[redacted]";} }
}
