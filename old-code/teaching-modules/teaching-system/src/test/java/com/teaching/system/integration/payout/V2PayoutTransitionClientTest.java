package com.teaching.system.integration.payout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class V2PayoutTransitionClientTest {

    @Test
    public void acceptsHttpsByDefault() {
        assertEquals("https://v2.example.test",
                V2PayoutTransitionClient.endpointBase("https://v2.example.test/", false));
    }

    @Test
    public void acceptsLoopbackHttpOnlyWhenExplicitlyAllowed() {
        assertEquals("http://localhost:8080",
                V2PayoutTransitionClient.endpointBase("http://localhost:8080/", true));
        assertEquals("http://127.0.0.1:5174",
                V2PayoutTransitionClient.endpointBase("http://127.0.0.1:5174", true));
        assertEquals("http://[::1]:8080",
                V2PayoutTransitionClient.endpointBase("http://[::1]:8080", true));
    }

    @Test
    public void rejectsLoopbackHttpWithoutExplicitOptIn() {
        assertThrows(IllegalStateException.class,
                () -> V2PayoutTransitionClient.endpointBase("http://localhost:8080", false));
    }

    @Test
    public void rejectsNonLoopbackHttpEvenWhenAllowed() {
        assertThrows(IllegalStateException.class,
                () -> V2PayoutTransitionClient.endpointBase("http://192.168.1.20:8080", true));
    }
}
