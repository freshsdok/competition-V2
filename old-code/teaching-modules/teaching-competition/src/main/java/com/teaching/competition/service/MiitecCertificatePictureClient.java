package com.teaching.competition.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.domain.CertificatePictureItem;
import com.teaching.competition.domain.CertificateImageDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * 工信部人才交流中心证书图片查询客户端。
 */
@Component
public class MiitecCertificatePictureClient {
    static final String DEFAULT_QUERY_URL =
            "https://cx.miitec.cn/server-front/credentialsinfo/queryGradeImage";
    private static final String SUCCESS_CODE = "00000 00000";
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    private final RestTemplate restTemplate;
    private final String queryUrl;

    @Autowired
    public MiitecCertificatePictureClient(
            @Value("${competition.certificate.query-grade-image-url:" + DEFAULT_QUERY_URL + "}") String queryUrl,
            @Value("${competition.certificate.query-connect-timeout-ms:3000}") int connectTimeout,
            @Value("${competition.certificate.query-read-timeout-ms:5000}") int readTimeout) {
        this.restTemplate = createRestTemplate(connectTimeout, readTimeout);
        this.queryUrl = queryUrl;
    }

    MiitecCertificatePictureClient(RestTemplate restTemplate, String queryUrl) {
        this.restTemplate = restTemplate;
        this.queryUrl = queryUrl;
    }

    /**
     * 按证书编号查询图片地址；未查询到图片时返回 {@code null}。
     */
    public String queryCertificatePicture(String certCode) {
        CertificatePictureItem certificate = queryCertificateInfo(certCode);
        return certificate == null ? null : certificate.getCertPicture();
    }

    /**
     * 按证书编号查询证书图片及筛选展示所需的信息；未查询到时返回 {@code null}。
     */
    public CertificatePictureItem queryCertificateInfo(String certCode) {
        if (StringUtils.isBlank(certCode)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                JSON.toJSONString(Collections.singletonMap("serialNum", certCode)), headers);

        final String responseBody;
        try {
            responseBody = restTemplate.postForObject(queryUrl, request, String.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException("查询证书图片失败", exception);
        }
        if (StringUtils.isBlank(responseBody)) {
            return null;
        }

        JSONObject response = JSON.parseObject(responseBody);
        if (!SUCCESS_CODE.equals(response.getString("code"))) {
            throw new IllegalStateException("证书平台返回失败：" + response.getString("message"));
        }
        JSONArray data = response.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return null;
        }
        for (int index = 0; index < data.size(); index++) {
            JSONObject certificate = data.getJSONObject(index);
            if (certCode.equalsIgnoreCase(certificate.getString("serialNum"))) {
                CertificatePictureItem item = new CertificatePictureItem();
                item.setCertCode(certificate.getString("serialNum").trim());
                String picture = certificate.getString("certPicture");
                item.setCertPicture(StringUtils.isBlank(picture) ? null : picture.trim());
                item.setContestName(certificate.getString("contestName"));
                item.setName(certificate.getString("name"));
                item.setSession(certificate.getString("session"));
                item.setContestArea(certificate.getString("contestArea"));
                item.setRuningNumYear(certificate.getInteger("runingNumYear"));
                return item;
            }
        }
        return null;
    }

    /**
     * 下载外部平台返回的短时图片地址。只允许 HTTPS 且限制单张大小，避免将该方法变成任意 URL 代理。
     */
    public CertificateImageDownload downloadCertificatePicture(String pictureUrl) {
        if (StringUtils.isBlank(pictureUrl)) {
            throw new IllegalArgumentException("证书图片地址为空");
        }
        URI uri = URI.create(pictureUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || !uri.getHost().equalsIgnoreCase("cx.miitec.cn")) {
            throw new IllegalArgumentException("证书图片地址不在允许的域名范围内");
        }
        try {
            CertificateImageDownload download = restTemplate.execute(uri, HttpMethod.GET, null, response -> {
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException("证书图片下载响应失败：" + response.getStatusCode());
                }
                long contentLength = response.getHeaders().getContentLength();
                if (contentLength > MAX_IMAGE_BYTES) {
                    throw new IllegalStateException("证书图片超过10MB安全上限");
                }
                MediaType mediaType = response.getHeaders().getContentType();
                String contentType = mediaType == null ? "image/jpeg" : mediaType.toString();
                if (!contentType.toLowerCase().startsWith("image/")) {
                    throw new IllegalStateException("证书图片响应类型非法：" + contentType);
                }

                ByteArrayOutputStream output = new ByteArrayOutputStream(
                        contentLength > 0 ? (int) contentLength : 64 * 1024);
                byte[] buffer = new byte[16 * 1024];
                int total = 0;
                int read;
                while ((read = response.getBody().read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_IMAGE_BYTES) {
                        throw new IllegalStateException("证书图片超过10MB安全上限");
                    }
                    output.write(buffer, 0, read);
                }
                if (total == 0) {
                    throw new IllegalStateException("证书图片下载响应为空");
                }
                return new CertificateImageDownload(output.toByteArray(), contentType);
            });
            if (download == null) {
                throw new IllegalStateException("证书图片下载响应为空");
            }
            return download;
        } catch (RestClientException exception) {
            throw new IllegalStateException("下载证书图片失败", exception);
        }
    }

    private static RestTemplate createRestTemplate(int connectTimeout, int readTimeout) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeout))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .sslContext(buildSslContext())
                    .build();
            JdkClientHttpRequestFactory requestFactory =
                    new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
            return new RestTemplate(requestFactory);
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("初始化证书图片查询客户端失败", exception);
        }
    }

    /**
     * JDK默认信任库不一定包含CFCA根证书。这里只为本客户端补充指定的CFCA根证书，
     * 同时保留JDK默认信任链和标准主机名校验，不关闭HTTPS证书验证。
     */
    private static SSLContext buildSslContext() throws GeneralSecurityException, IOException {
        X509TrustManager systemTrustManager = trustManager(null);

        KeyStore cfcaTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        cfcaTrustStore.load(null, null);
        try (InputStream input = MiitecCertificatePictureClient.class
                .getResourceAsStream("/certificates/cfca-ev-root.pem")) {
            if (input == null) {
                throw new IOException("缺少CFCA根证书资源");
            }
            X509Certificate certificate = (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(input);
            cfcaTrustStore.setCertificateEntry("cfca-ev-root", certificate);
        }
        X509TrustManager cfcaTrustManager = trustManager(cfcaTrustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null,
                new TrustManager[]{new CompositeTrustManager(systemTrustManager, cfcaTrustManager)},
                new SecureRandom());
        return sslContext;
    }

    private static X509TrustManager trustManager(KeyStore keyStore)
            throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        return Arrays.stream(factory.getTrustManagers())
                .filter(X509TrustManager.class::isInstance)
                .map(X509TrustManager.class::cast)
                .findFirst()
                .orElseThrow(() -> new GeneralSecurityException("未找到X509信任管理器"));
    }

    private static final class CompositeTrustManager implements X509TrustManager {
        private final X509TrustManager systemTrustManager;
        private final X509TrustManager cfcaTrustManager;

        private CompositeTrustManager(X509TrustManager systemTrustManager,
                                      X509TrustManager cfcaTrustManager) {
            this.systemTrustManager = systemTrustManager;
            this.cfcaTrustManager = cfcaTrustManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            systemTrustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            try {
                systemTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException systemFailure) {
                try {
                    cfcaTrustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException cfcaFailure) {
                    cfcaFailure.addSuppressed(systemFailure);
                    throw cfcaFailure;
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] systemIssuers = systemTrustManager.getAcceptedIssuers();
            X509Certificate[] cfcaIssuers = cfcaTrustManager.getAcceptedIssuers();
            X509Certificate[] issuers = Arrays.copyOf(
                    systemIssuers, systemIssuers.length + cfcaIssuers.length);
            System.arraycopy(cfcaIssuers, 0, issuers, systemIssuers.length, cfcaIssuers.length);
            return issuers;
        }
    }
}
