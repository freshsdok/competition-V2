package com.teaching.competition.service;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 所有外部证书平台请求共享的 Redis 全局限流入口。
 */
@Component
public class CertificateExternalRequestGate {
    private static final String RATE_LIMIT_KEY = "competition:certificate:image:external-rate";
    private static final String HIGH_PRIORITY_KEY = "competition:certificate:image:high-priority-waiting";

    private final RRateLimiter rateLimiter;
    private final RAtomicLong highPriorityWaiting;
    private final int requestsPerSecond;

    public CertificateExternalRequestGate(
            RedissonClient redissonClient,
            @Value("${competition.certificate.image.requests-per-second:1}") int requestsPerSecond) {
        this.requestsPerSecond = Math.max(1, requestsPerSecond);
        this.rateLimiter = redissonClient.getRateLimiter(RATE_LIMIT_KEY);
        this.highPriorityWaiting = redissonClient.getAtomicLong(HIGH_PRIORITY_KEY);
        this.rateLimiter.setRate(RateType.OVERALL, this.requestsPerSecond,
                1, RateIntervalUnit.SECONDS);
    }

    public void acquire(boolean highPriority) {
        if (highPriority) {
            highPriorityWaiting.incrementAndGet();
            try {
                rateLimiter.acquire();
            } finally {
                highPriorityWaiting.decrementAndGet();
            }
            return;
        }

        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("证书图片同步已中断");
            }
            while (highPriorityWaiting.get() > 0) {
                sleepBriefly();
            }
            if (rateLimiter.tryAcquire(250L, TimeUnit.MILLISECONDS)) {
                return;
            }
        }
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public boolean hasHighPriorityWaiting() {
        return highPriorityWaiting.get() > 0;
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("证书图片同步已中断", exception);
        }
    }
}
