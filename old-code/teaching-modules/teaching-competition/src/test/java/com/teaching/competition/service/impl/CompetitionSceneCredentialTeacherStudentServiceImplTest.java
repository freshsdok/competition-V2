package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialQuery;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialVO;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneCredentialTeacherStudentServiceImplTest {

    private CompetitionSceneCredentialServiceImpl service;
    private CompetitionSceneCredentialMapper credentialMapper;
    private ICompetitionSceneSubjectOperationStateService operationStateService;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneCredentialServiceImpl();
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        operationStateService = mock(ICompetitionSceneSubjectOperationStateService.class);
        setField("credentialMapper", credentialMapper);
        setField("operationStateService", operationStateService);
    }

    @Test
    public void selectTeacherStudentCredentialListMarksMissingCredential() {
        CompetitionTeacherStudentCredentialVO missing = new CompetitionTeacherStudentCredentialVO();
        missing.setMemberId(11L);
        missing.setStudentName("学生甲");

        CompetitionTeacherStudentCredentialVO generated = new CompetitionTeacherStudentCredentialVO();
        generated.setCredentialId(1001L);
        generated.setCredentialStatus("EFFECTIVE");
        generated.setCompetitionSeriesId(200L);
        generated.setUserId(12L);
        generated.setStudentName("学生乙");

        when(credentialMapper.selectTeacherStudentCredentialList(eq(9L), any(CompetitionTeacherStudentCredentialQuery.class)))
                .thenReturn(Arrays.asList(missing, generated));

        List<CompetitionTeacherStudentCredentialVO> list =
                service.selectTeacherStudentCredentialList(9L, new CompetitionTeacherStudentCredentialQuery());

        assertEquals(2, list.size());
        assertEquals(CompetitionTeacherStudentCredentialVO.STATUS_NOT_GENERATED, list.get(0).getCredentialStatus());
        assertEquals("EFFECTIVE", list.get(1).getCredentialStatus());
        verify(credentialMapper, never()).updateCompetitionSceneCredential(any(CompetitionSceneCredential.class));
        verify(credentialMapper, never()).updateCompetitionSceneCredentialVerifyInfo(any(CompetitionSceneCredential.class));
    }

    @Test(expected = ServiceException.class)
    public void selectTeacherStudentCredentialDetailRejectsUnauthorizedCredential() {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(1001L);
        when(credentialMapper.selectCompetitionSceneCredentialById(1001L)).thenReturn(credential);
        when(credentialMapper.selectTeacherStudentCredentialDetail(9L, 1001L)).thenReturn(null);

        service.selectTeacherStudentCredentialDetail(9L, 1001L);
    }

    @Test
    public void selectTeacherStudentCredentialDetailFillsReadonlyState() {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(1001L);
        when(credentialMapper.selectCompetitionSceneCredentialById(1001L)).thenReturn(credential);

        CompetitionTeacherStudentCredentialVO detail = new CompetitionTeacherStudentCredentialVO();
        detail.setCredentialId(1001L);
        detail.setCompetitionSeriesId(200L);
        detail.setScheduleId(300L);
        detail.setUserId(12L);
        detail.setStudentName("学生乙");
        detail.setContestLocation("一号楼");
        detail.setContestRoom("101");
        when(credentialMapper.selectTeacherStudentCredentialDetail(9L, 1001L)).thenReturn(detail);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<CompetitionSceneCredential> credentials = invocation.getArgument(0);
            credentials.get(0).setReportStatus("1");
            credentials.get(0).setMaterialStatus("1");
            credentials.get(0).setWaitingStatus("0");
            return null;
        }).when(operationStateService).fillCredentialOperationStates(any(List.class));

        CompetitionTeacherStudentCredentialVO result =
                service.selectTeacherStudentCredentialDetail(9L, 1001L);

        assertEquals("1", result.getReportStatus());
        assertEquals("1", result.getMaterialStatus());
        assertEquals("0", result.getWaitingStatus());
        assertEquals("一号楼 / 101", result.getScheduleLocation());
        verify(credentialMapper, never()).updateCompetitionSceneCredential(any(CompetitionSceneCredential.class));
        verify(credentialMapper, never()).updateCompetitionSceneCredentialVerifyInfo(any(CompetitionSceneCredential.class));
    }

    @Test
    public void checkTeacherCanViewCredentialUsesAuthorizedDetailQuery() {
        when(credentialMapper.selectTeacherStudentCredentialDetail(9L, 1001L))
                .thenReturn(new CompetitionTeacherStudentCredentialVO());
        when(credentialMapper.selectTeacherStudentCredentialDetail(9L, 1002L))
                .thenReturn(null);

        assertTrue(service.checkTeacherCanViewCredential(9L, 1001L));
        assertFalse(service.checkTeacherCanViewCredential(9L, 1002L));
        assertFalse(service.checkTeacherCanViewCredential(null, 1001L));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneCredentialServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
