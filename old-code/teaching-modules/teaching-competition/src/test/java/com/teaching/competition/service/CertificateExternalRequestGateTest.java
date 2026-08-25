package com.teaching.competition.service;

import org.junit.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CertificateExternalRequestGateTest {
    @Test
    public void allPrioritiesUseTheSameOverallRedisRateLimiter() {
        RedissonClient redisson = mock(RedissonClient.class);
        RRateLimiter limiter = mock(RRateLimiter.class);
        RAtomicLong priorityCounter = mock(RAtomicLong.class);
        when(redisson.getRateLimiter("competition:certificate:image:external-rate"))
                .thenReturn(limiter);
        when(redisson.getAtomicLong("competition:certificate:image:high-priority-waiting"))
                .thenReturn(priorityCounter);
        when(limiter.tryAcquire(250L, java.util.concurrent.TimeUnit.MILLISECONDS)).thenReturn(true);

        CertificateExternalRequestGate gate = new CertificateExternalRequestGate(redisson, 1);
        gate.acquire(false);
        gate.acquire(true);

        verify(limiter).setRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);
        verify(limiter).tryAcquire(250L, java.util.concurrent.TimeUnit.MILLISECONDS);
        verify(limiter).acquire();
    }
}
