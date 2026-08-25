package com.teaching.competition.service;

import com.teaching.competition.domain.CertificatePictureItem;
import com.teaching.competition.domain.CertificateImageDownload;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MiitecCertificatePictureClientTest {

    @Test
    public void queryCertificatePictureReturnsMatchingPicture() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MiitecCertificatePictureClient client = new MiitecCertificatePictureClient(
                restTemplate, MiitecCertificatePictureClient.DEFAULT_QUERY_URL);
        when(restTemplate.postForObject(
                eq(MiitecCertificatePictureClient.DEFAULT_QUERY_URL),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn("{\"code\":\"00000 00000\",\"data\":[{"
                        + "\"serialNum\":\"IITCHJDICT26027548\","
                        + "\"contestName\":\"大学生新一代信息通信科技大赛工程实践赛道\","
                        + "\"name\":\"刘春林\","
                        + "\"session\":\"十三\","
                        + "\"contestArea\":\"全国总决赛\","
                        + "\"runingNumYear\":2026,"
                        + "\"certPicture\":\"https://cx.miitec.cn/files/test.jpg?token=1\"}]}");

        CertificatePictureItem certificate =
                client.queryCertificateInfo("IITCHJDICT26027548");

        assertEquals("IITCHJDICT26027548", certificate.getCertCode());
        assertEquals("大学生新一代信息通信科技大赛工程实践赛道",
                certificate.getContestName());
        assertEquals("刘春林", certificate.getName());
        assertEquals("十三", certificate.getSession());
        assertEquals("全国总决赛", certificate.getContestArea());
        assertEquals(Integer.valueOf(2026), certificate.getRuningNumYear());
        assertEquals("https://cx.miitec.cn/files/test.jpg?token=1",
                certificate.getCertPicture());
    }

    @Test
    public void queryCertificatePictureDoesNotUseAnotherCertificateRecord() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MiitecCertificatePictureClient client = new MiitecCertificatePictureClient(
                restTemplate, MiitecCertificatePictureClient.DEFAULT_QUERY_URL);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{\"code\":\"00000 00000\",\"data\":[{"
                        + "\"serialNum\":\"OTHER\","
                        + "\"certPicture\":\"https://cx.miitec.cn/files/other.jpg\"}]}");

        assertNull(client.queryCertificateInfo("IITCHJDICT26027548"));
    }

    @Test
    public void queryCertificateInfoKeepsMetadataWhenPictureIsMissing() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MiitecCertificatePictureClient client = new MiitecCertificatePictureClient(
                restTemplate, MiitecCertificatePictureClient.DEFAULT_QUERY_URL);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{\"code\":\"00000 00000\",\"data\":[{"
                        + "\"serialNum\":\"CERT-NO-IMAGE\","
                        + "\"contestName\":\"赛事名称\",\"name\":\"张三\","
                        + "\"session\":\"十三\",\"contestArea\":\"全国总决赛\","
                        + "\"runingNumYear\":2026,\"certPicture\":null}]}");

        CertificatePictureItem certificate = client.queryCertificateInfo("CERT-NO-IMAGE");

        assertEquals("赛事名称", certificate.getContestName());
        assertEquals("张三", certificate.getName());
        assertNull(certificate.getCertPicture());
    }

    @Test
    public void downloadCertificatePictureReadsAllowedImageThroughBoundedStream() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String pictureUrl = "https://cx.miitec.cn/files/test.jpg";
        mockDownload(restTemplate, pictureUrl, new byte[]{1, 2, 3}, 3L);
        MiitecCertificatePictureClient client = new MiitecCertificatePictureClient(
                restTemplate, MiitecCertificatePictureClient.DEFAULT_QUERY_URL);

        CertificateImageDownload download = client.downloadCertificatePicture(pictureUrl);

        assertEquals(3, download.content().length);
        assertEquals("image/jpeg", download.contentType());
    }

    @Test(expected = IllegalStateException.class)
    public void downloadCertificatePictureRejectsOversizedContentBeforeReadingBody() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String pictureUrl = "https://cx.miitec.cn/files/oversized.jpg";
        mockDownload(restTemplate, pictureUrl, new byte[]{1}, 10 * 1024 * 1024 + 1L);
        MiitecCertificatePictureClient client = new MiitecCertificatePictureClient(
                restTemplate, MiitecCertificatePictureClient.DEFAULT_QUERY_URL);

        client.downloadCertificatePicture(pictureUrl);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockDownload(RestTemplate restTemplate, String pictureUrl,
                              byte[] body, long contentLength) {
        when(restTemplate.execute(eq(URI.create(pictureUrl)), eq(HttpMethod.GET),
                isNull(), any(ResponseExtractor.class))).thenAnswer(invocation -> {
            ResponseExtractor extractor = invocation.getArgument(3);
            ClientHttpResponse response = mock(ClientHttpResponse.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(contentLength);
            when(response.getStatusCode()).thenReturn(HttpStatus.OK);
            when(response.getHeaders()).thenReturn(headers);
            when(response.getBody()).thenReturn(new ByteArrayInputStream(body));
            return extractor.extractData(response);
        });
    }
}
