package com.teaching.system.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.integration.payout.V2PayoutTransitionClient;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.junit.After;
import org.junit.Test;

/**
 * 过渡入口只接受V1已认证主体的稳定来源键。
 * 测试不复制V1角色、权限或Session，因为它们不是V2过渡凭据的输入。
 */
public class V2PayoutTransitionControllerTest {
    private final V2PayoutTransitionClient client = mock(V2PayoutTransitionClient.class);
    private final V2PayoutTransitionController controller = new V2PayoutTransitionController(
            new StaticListableBeanFactory(Map.of("client", client))
                    .getBeanProvider(V2PayoutTransitionClient.class));

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.remove();
    }

    @Test
    public void authenticatedUserPassesOnlyStableSourceUserKey() {
        Instant expiresAt = Instant.parse("2026-09-05T00:05:00Z");
        var link = new V2PayoutTransitionClient.EntryLink(
                "https://v2.example.test/payout/transition?code=opaque", expiresAt,
                "V2_PAYOUT_ENTRY_READY");
        SecurityContextHolder.setUserId("42");
        // 即使V1上下文存在管理权限字符串，Controller也只能把人员来源键交给V2。
        SecurityContextHolder.setPermission("admin:*:*;finance:all");
        when(client.issue("42")).thenReturn(link);

        AjaxResult result = controller.entry();

        assertEquals(link, result.get(AjaxResult.DATA_TAG));
        verify(client).issue("42");
    }

    @Test
    public void missingGatewayIdentityFailsClosedBeforeIssuingTransition() {
        ServiceException failure = assertThrows(ServiceException.class, controller::entry);

        assertEquals(Integer.valueOf(401), failure.getCode());
        verifyNoInteractions(client);
    }

    @Test
    public void missingConfigurationKeepsRouteAndReturnsUnavailable() {
        assertUnavailableRoute(Map.of());
    }

    @Test
    public void disabledBridgeKeepsRouteAndReturnsUnavailable() {
        assertUnavailableRoute(Map.of("v2.payout-transition.enabled", "false"));
    }

    private void assertUnavailableRoute(Map<String, Object> properties) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("test", properties));
            context.register(V2PayoutTransitionController.class, V2PayoutTransitionClient.class);
            context.refresh();
            var mappings = new RequestMappingHandlerMapping();
            mappings.setApplicationContext(context);
            mappings.afterPropertiesSet();
            assertEquals(1, mappings.getHandlerMethods().entrySet().stream()
                    .filter(entry -> entry.getKey().getPatternValues().contains("/v2PayoutTransition/entry")
                            && entry.getKey().getMethodsCondition().getMethods()
                                    .contains(org.springframework.web.bind.annotation.RequestMethod.POST))
                    .count());
            var disabledController = context.getBean(V2PayoutTransitionController.class);
            assertEquals(Integer.valueOf(401),
                    assertThrows(ServiceException.class, disabledController::entry).getCode());
            SecurityContextHolder.setUserId("42");
            ServiceException failure = assertThrows(ServiceException.class, disabledController::entry);
            assertEquals(Integer.valueOf(503), failure.getCode());
            assertEquals("付款资料办理暂未开放，请稍后重试", failure.getMessage());
        }
    }
}
