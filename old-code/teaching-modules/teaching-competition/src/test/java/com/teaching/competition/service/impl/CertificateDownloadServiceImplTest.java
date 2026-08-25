package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.domain.CertificateDownloadSummary;
import com.teaching.competition.mapper.UserCertificateOriginMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateDownloadServiceImplTest {
    private UserCertificateOriginMapper mapper;
    private CertificateDownloadServiceImpl service;

    @Before
    public void setUp() {
        mapper = mock(UserCertificateOriginMapper.class);
        service = new CertificateDownloadServiceImpl(mapper);
    }

    @Test
    public void summaryUsesDistinctCertificateCodesAndTeamCountFromMapper() {
        when(mapper.selectGuidedCertificateCodes(10L))
                .thenReturn(Arrays.asList("CERT-1", "CERT-2"));
        when(mapper.countGuidedCertificateTeams(10L)).thenReturn(1L);

        CertificateDownloadSummary summary = service.getGuidedCertificateSummary(10L);

        assertEquals(1L, summary.getTeamCount());
        assertEquals(2L, summary.getCertificateCount());
        assertTrue(summary.isDownloadable());
    }

    @Test
    public void legacySynchronousPictureEndpointIsRejected() {
        try {
            service.getGuidedCertificatePictures(10L);
            fail("旧同步接口应被拒绝");
        } catch (GlobalException exception) {
            assertTrue(exception.getMessage().contains("已废弃"));
        }
    }

    @Test
    public void emptyAuthorizationCannotStartDownload() {
        when(mapper.selectGuidedCertificateCodes(10L)).thenReturn(Collections.emptyList());
        CertificateDownloadSummary summary = service.getGuidedCertificateSummary(10L);
        assertFalse(summary.isDownloadable());
    }
}
