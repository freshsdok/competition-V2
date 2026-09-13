package com.teaching.system.controller;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.integration.embedded.V2InternalEmbeddedBridgeClient;
import com.teaching.system.service.TeamCollectionService;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TeamCollectionControllerTest {
    @After public void clear() { SecurityContextHolder.remove(); }

    @Test public void forgedUserHeaderWithoutTokenResolvedLoginCannotReadOrAcquire() {
        var service = mock(TeamCollectionService.class);
        var controller = new TeamCollectionController(service);
        SecurityContextHolder.setUserId("101");
        assertEquals(Integer.valueOf(401), assertThrows(ServiceException.class, controller::mine).getCode());
        assertThrows(ServiceException.class, () -> controller.start(1,new TeamCollectionController.Version(0)));
        verifyNoInteractions(service);
    }

    @Test public void actorComesFromLoginAndLegacySettlementEndpointRequiresTeamGate() {
        var service = mock(TeamCollectionService.class);
        var user = new LoginUser(); user.setUserid(101L);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER,user);
        SecurityContextHolder.setUserId("999");
        var controller = new TeamCollectionController(service);
        controller.mine(); controller.start(1,new TeamCollectionController.Version(0));
        controller.confirm(1,new TeamCollectionController.Version(1));
        controller.release(1,new TeamCollectionController.Version(1));
        verify(service).mine(101); verify(service).start(1,101,0);
        verify(service).confirm(1,101,1); verify(service).release(1,101,1);
        var bridgeController = new V2InternalEmbeddedBridgeController(new StaticListableBeanFactory().getBeanProvider(V2InternalEmbeddedBridgeClient.class), service);
        bridgeController.settlement(new V2InternalEmbeddedBridgeController.SettlementEntry(1,1));
        verify(service).entry(1,101,1);
    }
}
