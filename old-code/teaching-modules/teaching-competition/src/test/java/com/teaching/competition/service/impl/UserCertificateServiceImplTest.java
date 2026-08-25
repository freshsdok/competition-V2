package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.domain.CompetitionCertificateQueryRequest;
import com.teaching.competition.mapper.UserCertificateMapper;
import com.teaching.system.api.domain.UserCertificate;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UserCertificateServiceImplTest {
    private UserCertificateMapper userCertificateMapper;
    private UserCertificateServiceImpl service;

    @Before
    public void setUp() throws Exception {
        userCertificateMapper = mock(UserCertificateMapper.class);
        service = new UserCertificateServiceImpl();
        Field mapperField = UserCertificateServiceImpl.class.getDeclaredField("userCertificateMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, userCertificateMapper);
    }

    @Test
    public void selectCompetitionCertificateListSupportsPersonQuery() {
        CompetitionCertificateQueryRequest request = request(" person ");
        request.setSchoolName(" 安徽大学 ");
        request.setUserName(" 张钰 ");
        List<UserCertificate> expected = Collections.singletonList(new UserCertificate());
        when(userCertificateMapper.selectCompetitionCertificateList(request)).thenReturn(expected);

        List<UserCertificate> result = service.selectCompetitionCertificateList(request);

        assertEquals(expected, result);
        assertEquals("PERSON", request.getQueryType());
        assertEquals("安徽大学", request.getSchoolName());
        assertEquals("张钰", request.getUserName());
        verify(userCertificateMapper).selectCompetitionCertificateList(request);
    }

    @Test
    public void selectCompetitionCertificateListSupportsOrganizationAndCertCodeQueries() {
        CompetitionCertificateQueryRequest organization = request("ORGANIZATION");
        organization.setSchoolName("北方工业大学");
        when(userCertificateMapper.selectCompetitionCertificateList(organization))
                .thenReturn(Collections.emptyList());
        service.selectCompetitionCertificateList(organization);

        CompetitionCertificateQueryRequest certCode = request("CERT_CODE");
        certCode.setCertCode("iitcrydict26029338");
        when(userCertificateMapper.selectCompetitionCertificateList(certCode))
                .thenReturn(Collections.emptyList());
        service.selectCompetitionCertificateList(certCode);

        assertEquals("IITCRYDICT26029338", certCode.getCertCode());
        verify(userCertificateMapper).selectCompetitionCertificateList(organization);
        verify(userCertificateMapper).selectCompetitionCertificateList(certCode);
    }

    @Test
    public void selectCompetitionCertificateListRejectsUnsafeOrIncompleteQueries() {
        CompetitionCertificateQueryRequest incomplete = request("PERSON");
        incomplete.setSchoolName("安徽大学");
        assertGlobalException(incomplete, "请输入获证人姓名");

        CompetitionCertificateQueryRequest wildcard = request("ORGANIZATION");
        wildcard.setSchoolName("%%");
        assertGlobalException(wildcard, "不能包含%或_");

        CompetitionCertificateQueryRequest unknown = request("UNKNOWN");
        assertGlobalException(unknown, "证书查询方式不正确");
        verifyNoInteractions(userCertificateMapper);
    }

    private CompetitionCertificateQueryRequest request(String queryType) {
        CompetitionCertificateQueryRequest request = new CompetitionCertificateQueryRequest();
        request.setQueryType(queryType);
        return request;
    }

    private void assertGlobalException(CompetitionCertificateQueryRequest request, String expectedMessage) {
        try {
            service.selectCompetitionCertificateList(request);
            fail("应抛出GlobalException");
        } catch (GlobalException exception) {
            assertTrue(exception.getMessage().contains(expectedMessage));
        }
    }
}
