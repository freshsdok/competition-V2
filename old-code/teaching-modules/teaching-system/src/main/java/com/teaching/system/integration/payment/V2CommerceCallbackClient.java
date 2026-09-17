package com.teaching.system.integration.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Only transports the signed envelope; V2 remains responsible for verification and settlement. */
@Component
@ConditionalOnProperty(name = "v2.commerce-callback.enabled", havingValue = "true")
public class V2CommerceCallbackClient {
    private final URI endpoint;
    private final int connectTimeout;
    private final int readTimeout;
    private final ObjectMapper json = new ObjectMapper();

    public V2CommerceCallbackClient(
            @Value("${v2.commerce-callback.api-base-url}") String baseUrl,
            @Value("${v2.commerce-callback.provider-instance-id}") long providerInstanceId,
            @Value("${v2.commerce-callback.allow-insecure-http:false}") boolean allowHttp,
            @Value("${v2.commerce-callback.connect-timeout-ms:3000}") int connectTimeout,
            @Value("${v2.commerce-callback.read-timeout-ms:5000}") int readTimeout) {
        URI base = URI.create(baseUrl.trim().replaceAll("/+$", ""));
        if (base.getHost() == null || base.getUserInfo() != null || base.getQuery() != null
                || base.getFragment() != null || providerInstanceId <= 0
                || !("https".equalsIgnoreCase(base.getScheme())
                    || (allowHttp && "http".equalsIgnoreCase(base.getScheme())))
                || connectTimeout < 1 || connectTimeout > 30000 || readTimeout < 1 || readTimeout > 30000) {
            throw new IllegalArgumentException("Invalid V2 commerce callback configuration");
        }
        this.endpoint = URI.create(base + "/public/commerce/cmb-callbacks/" + providerInstanceId);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public Map<String, String> forwardPayment(Map<String, String> fields) {
        HttpURLConnection connection = null;
        try {
            byte[] form = fields.entrySet().stream().map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                    .collect(Collectors.joining("&")).getBytes(StandardCharsets.UTF_8);
            connection = (HttpURLConnection) endpoint.toURL().openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(form.length);
            try (var output = connection.getOutputStream()) { output.write(form); }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) throw new IllegalStateException("V2 callback HTTP failure");
            byte[] body;
            try (InputStream input = connection.getInputStream()) { body = input.readNBytes(65537); }
            if (body.length > 65536) throw new IllegalStateException("V2 callback response too large");
            Map<String, String> ack = json.readValue(body, new TypeReference<Map<String, String>>() {});
            if (!"0.0.1".equals(ack.get("version")) || !"UTF-8".equalsIgnoreCase(ack.get("encoding"))
                    || !"02".equals(ack.get("signMethod")) || ack.get("sign") == null || ack.get("sign").isBlank()
                    || !("FAIL".equals(ack.get("returnCode"))
                        || ("SUCCESS".equals(ack.get("returnCode")) && "SUCCESS".equals(ack.get("respCode"))))) {
                throw new IllegalStateException("V2 callback acknowledgement is invalid");
            }
            return ack;
        } catch (Exception failure) {
            // Do not expose original envelope, signature or upstream response in exceptions/logs.
            throw new IllegalStateException("V2 payment callback forwarding failed");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
