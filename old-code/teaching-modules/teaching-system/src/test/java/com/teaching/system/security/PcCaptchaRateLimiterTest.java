package com.teaching.system.security;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

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
public class PcCaptchaRateLimiterTest {

    private RedisTemplate redisTemplate;
    private PcCaptchaRateLimiter limiter;

    @Before
    public void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        limiter = new PcCaptchaRateLimiter(redisTemplate);
    }

    @Test
    public void consumesHashedAccountAndIpCountersAtomically() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(Arrays.asList(1L, 0L));

        PcCaptchaRateLimiter.SendStatus status =
                limiter.consume("User@Example.com", "203.0.113.10");

        assertTrue(status.isAvailable());
        assertFalse(status.isBlocked());
        ArgumentCaptor<List> keys = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(any(RedisScript.class), keys.capture(),
                eq(3), eq(20), eq(600));
        assertEquals(2, keys.getValue().size());
        assertTrue(keys.getValue().get(0).toString().contains("account:"));
        assertTrue(keys.getValue().get(1).toString().contains("ip:"));
        assertFalse(keys.getValue().toString().contains("User@Example.com"));
        assertFalse(keys.getValue().toString().contains("203.0.113.10"));
    }

    @Test
    public void thresholdResultReturnsRetryAfter() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(Arrays.asList(0L, 75L));

        PcCaptchaRateLimiter.SendStatus status =
                limiter.consume("user@example.com", "203.0.113.10");

        assertTrue(status.isAvailable());
        assertTrue(status.isBlocked());
        assertEquals(75L, status.getRetryAfterSeconds());
    }

    @Test
    public void redisFailureIsFailClosed() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenThrow(new IllegalStateException("redis unavailable"));

        PcCaptchaRateLimiter.SendStatus status =
                limiter.consume("user@example.com", "203.0.113.10");

        assertFalse(status.isAvailable());
        assertTrue(status.isBlocked());
    }
}
