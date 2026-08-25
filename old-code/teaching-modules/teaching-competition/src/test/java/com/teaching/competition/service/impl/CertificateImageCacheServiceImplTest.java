package com.teaching.competition.service.impl;

import com.teaching.common.core.domain.R;
import com.teaching.competition.domain.CertificateImageCache;
import com.teaching.competition.domain.CertificateImageDownload;
import com.teaching.competition.domain.CertificatePictureItem;
import com.teaching.competition.mapper.CertificateImageCacheMapper;
import com.teaching.competition.service.CertificateExternalRequestGate;
import com.teaching.competition.service.MiitecCertificatePictureClient;
import com.teaching.system.api.RemoteOssUploadService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RedissonClient;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class CertificateImageCacheServiceImplTest {
    private CertificateImageCacheMapper mapper;
    private MiitecCertificatePictureClient client;
    private CertificateExternalRequestGate gate;
    private RemoteOssUploadService oss;
    private CertificateImageCacheServiceImpl service;

    @Before
    public void setUp() {
        mapper = mock(CertificateImageCacheMapper.class);
        client = mock(MiitecCertificatePictureClient.class);
        gate = mock(CertificateExternalRequestGate.class);
        oss = mock(RemoteOssUploadService.class);
        service = new CertificateImageCacheServiceImpl(mapper, client, gate, oss,
                mock(RedissonClient.class), 0, 0, 0, 1);
    }

    @After
    public void tearDown() {
        service.shutdownExecutors();
    }

    @Test
    public void successfulImageIsUploadedAndHasSha256Metadata() {
        CertificateImageCache pending = pending("CERT-1");
        CertificatePictureItem item = picture("CERT-1", "https://cx.miitec.cn/files/test.jpg");
        when(mapper.selectByCertCode("CERT-1")).thenReturn(pending);
        when(mapper.markSyncing("CERT-1")).thenReturn(1);
        when(client.queryCertificateInfo("CERT-1")).thenReturn(item);
        when(client.downloadCertificatePicture(item.getCertPicture()))
                .thenReturn(new CertificateImageDownload(new byte[]{1, 2, 3}, "image/jpeg"));
        when(oss.ossUpload(any(), eq("certificate-image-cache"), eq("2026")))
                .thenReturn(R.ok("certificate-image-cache/2026/test.jpg"));
        when(mapper.markSuccess(any(CertificateImageCache.class))).thenReturn(1);

        assertTrue(service.syncOne("CERT-1", true));

        ArgumentCaptor<CertificateImageCache> captor = ArgumentCaptor.forClass(CertificateImageCache.class);
        verify(mapper).markSuccess(captor.capture());
        assertEquals("certificate-image-cache/2026/test.jpg", captor.getValue().getObjectKey());
        assertEquals(Long.valueOf(3), captor.getValue().getFileSize());
        assertNotNull(captor.getValue().getSha256());
        assertEquals(64, captor.getValue().getSha256().length());
    }

    @Test
    public void metadataWithoutPictureIsRetainedAsNotFound() {
        CertificateImageCache pending = pending("CERT-2");
        CertificatePictureItem item = picture("CERT-2", null);
        when(mapper.selectByCertCode("CERT-2")).thenReturn(pending);
        when(mapper.markSyncing("CERT-2")).thenReturn(1);
        when(client.queryCertificateInfo("CERT-2")).thenReturn(item);

        assertFalse(service.syncOne("CERT-2", false));

        ArgumentCaptor<CertificateImageCache> captor = ArgumentCaptor.forClass(CertificateImageCache.class);
        verify(mapper).markNotFound(captor.capture());
        assertEquals("张三", captor.getValue().getName());
        assertEquals("全国总决赛", captor.getValue().getContestArea());
        assertEquals(Integer.valueOf(4), captor.getValue().getRetryCount());
    }

    @Test
    public void fallbackDoesNotRequeueCertificateDuringFailureCooldown() {
        CertificateImageCache cooled = pending("CERT-3");
        cooled.setCacheStatus(CertificateImageCache.FAILED);
        cooled.setNextRetryTime(new Date(System.currentTimeMillis() + 60_000L));
        when(mapper.selectAuthorizedCodes(7L, List.of("CERT-3")))
                .thenReturn(List.of("CERT-3"));
        when(mapper.selectByCertCode("CERT-3")).thenReturn(cooled);

        Map<String, Object> result = service.enqueueFallback(7L, List.of("CERT-3"));

        assertEquals(0, result.get("acceptedCount"));
        assertEquals("UNCHANGED", result.get("status"));
        verifyNoInteractions(client);
    }

    private CertificateImageCache pending(String certCode) {
        CertificateImageCache cache = new CertificateImageCache();
        cache.setCertCode(certCode);
        cache.setCacheStatus(CertificateImageCache.PENDING);
        return cache;
    }

    private CertificatePictureItem picture(String certCode, String url) {
        CertificatePictureItem item = new CertificatePictureItem();
        item.setCertCode(certCode);
        item.setCertPicture(url);
        item.setContestName("大学生新一代信息通信科技大赛工程实践赛道");
        item.setName("张三");
        item.setSession("十三");
        item.setContestArea("全国总决赛");
        item.setRuningNumYear(2026);
        return item;
    }
}
