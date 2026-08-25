package com.teaching.system.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PcLoginRateLimiterTest {

    private RedisTemplate redisTemplate;
    private PcLoginRateLimiter limiter;

    @Before
    public void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        limiter = new PcLoginRateLimiter(redisTemplate);
    }

    @Test
    public void reservesAccountAndIpAtomicallyBeforeAuthentication() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(Arrays.asList(1L, 0L));

        PcLoginRateLimiter.LimitStatus status =
                limiter.reserveAttempt("User@Example.com", "203.0.113.10");

        assertTrue(status.isAvailable());
        assertFalse(status.isBlocked());
        ArgumentCaptor<List> keys = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(any(RedisScript.class), keys.capture(),
                eq(5), eq(30), eq(30));
        assertEquals(4, keys.getValue().size());
        assertTrue(keys.getValue().get(0).toString().contains("account:"));
        assertTrue(keys.getValue().get(1).toString().contains("ip:"));
        assertTrue(keys.getValue().get(2).toString().contains("pending-account:"));
        assertTrue(keys.getValue().get(3).toString().contains("pending-ip:"));
    }

    @Test
    public void thresholdResultIsBlocked() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(Arrays.asList(0L, 45L));

        PcLoginRateLimiter.LimitStatus status =
                limiter.reserveAttempt("user@example.com", "203.0.113.10");

        assertTrue(status.isBlocked());
        assertEquals(45L, status.getRetryAfterSeconds());
    }

    @Test
    public void successfulLoginDoesNotClearIpFailureHistory() {
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(1L);

        assertTrue(limiter.recordSuccess("user@example.com", "203.0.113.10"));

        ArgumentCaptor<List> keys = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(any(RedisScript.class), keys.capture());
        assertEquals(3, keys.getValue().size());
        assertTrue(keys.getValue().get(0).toString().contains("account:"));
        assertFalse(keys.getValue().get(0).toString().contains("pending-"));
        assertTrue(keys.getValue().stream()
                .noneMatch(key -> key.toString().contains(":fail:ip:")));
    }
}
