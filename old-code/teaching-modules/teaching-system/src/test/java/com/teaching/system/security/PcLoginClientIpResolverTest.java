package com.teaching.system.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PcLoginClientIpResolverTest {

    @Test
    public void ignoresForwardingHeadersFromUntrustedPeer() {
        PcLoginClientIpResolver resolver = new PcLoginClientIpResolver();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.99");

        assertEquals("203.0.113.5", resolver.resolve(request));
    }

    @Test
    public void usesRightmostUntrustedAddressBehindTrustedProxy() throws Exception {
        PcLoginClientIpResolver resolver = new PcLoginClientIpResolver();
        setField(resolver, "trustedProxies", "127.0.0.1;10.*.*.*");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("192.0.2.123, 198.51.100.20, 10.0.0.5");

        assertEquals("198.51.100.20", resolver.resolve(request));
    }

    @Test
    public void rejectsMalformedForwardedIpv6() throws Exception {
        PcLoginClientIpResolver resolver = new PcLoginClientIpResolver();
        setField(resolver, "trustedProxies", "127.0.0.1;::1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("::::");
        when(request.getHeader("X-Real-IP")).thenReturn(null);

        assertEquals("127.0.0.1", resolver.resolve(request));
    }

    @Test
    public void supportsConfiguredProxyHostnames() throws Exception {
        PcLoginClientIpResolver resolver = new PcLoginClientIpResolver();
        setField(resolver, "trustedProxies", "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.42");

        assertEquals("198.51.100.42", resolver.resolve(request));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
