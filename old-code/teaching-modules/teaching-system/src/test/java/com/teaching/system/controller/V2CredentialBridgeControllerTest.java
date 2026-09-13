package com.teaching.system.controller;

import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.integration.credential.V2CredentialBridgeClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class V2CredentialBridgeControllerTest {
    @After public void clearContext() { SecurityContextHolder.remove(); }

    @Test public void disabledBridgeRetainsRoutesAndRequiresLoginBeforeAvailability() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(V2CredentialBridgeController.class, V2CredentialBridgeClient.class);
            context.refresh();
            var mappings = new RequestMappingHandlerMapping();
            mappings.setApplicationContext(context);
            mappings.afterPropertiesSet();
            assertEquals(2, mappings.getHandlerMethods().size());
            var controller = context.getBean(V2CredentialBridgeController.class);
            assertEquals(List.of(), controller.discovery().get(AjaxResult.DATA_TAG));
            assertEquals(Integer.valueOf(401), assertThrows(ServiceException.class, controller::entry).getCode());
            SecurityContextHolder.setUserId("700001");
            assertEquals(Integer.valueOf(503), assertThrows(ServiceException.class, controller::entry).getCode());
        }
    }

    @Test public void authenticatedEntryPassesOnlyTrustedSourceId() {
        var client = mock(V2CredentialBridgeClient.class);
        var controller = new V2CredentialBridgeController(new StaticListableBeanFactory(Map.of("client", client))
                .getBeanProvider(V2CredentialBridgeClient.class));
        SecurityContextHolder.setUserId("700001");
        var entry = new V2CredentialBridgeClient.Entry("https://synthetic.invalid/credential/transition#code=" + "a".repeat(43), Instant.parse("2099-01-01T00:00:00Z"));
        when(client.issue(700001L)).thenReturn(entry);
        assertSame(entry, controller.entry().get(AjaxResult.DATA_TAG));
        verify(client).issue(700001L);
    }

    @Test public void upstreamFailureReturnsControlledUnavailableWithoutDetails() {
        var client = mock(V2CredentialBridgeClient.class);
        var controller = new V2CredentialBridgeController(new StaticListableBeanFactory(Map.of("client", client))
                .getBeanProvider(V2CredentialBridgeClient.class));
        SecurityContextHolder.setUserId("700001");
        when(client.issue(700001L)).thenThrow(new org.springframework.web.client.RestClientException("private upstream detail"));
        var error = assertThrows(ServiceException.class, controller::entry);
        assertEquals(Integer.valueOf(503), error.getCode());
        assertFalse(error.getMessage().contains("private upstream detail"));
    }
}
