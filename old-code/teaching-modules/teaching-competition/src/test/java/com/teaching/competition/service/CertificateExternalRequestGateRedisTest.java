package com.teaching.competition.service;

import org.junit.Assume;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/** 使用独立 Redis DB 15 验证跨实例共享限流；无本地 Redis 时自动跳过。 */
public class CertificateExternalRequestGateRedisTest {
    private static final String RATE_KEY = "competition:certificate:image:external-rate";
    private static final String PRIORITY_KEY = "competition:certificate:image:high-priority-waiting";

    @Test
    public void twoInstancesShareOnePermitPerSecondAcrossPriorities() throws Exception {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(15)
                .setConnectTimeout(1000)
                .setTimeout(1000)
                .setRetryAttempts(0);
        RedissonClient redisson = Redisson.create(config);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            try {
                redisson.getKeys().delete(RATE_KEY, PRIORITY_KEY);
            } catch (RuntimeException unavailable) {
                Assume.assumeNoException("本地Redis不可用，跳过跨实例限流集成测试", unavailable);
            }

            CertificateExternalRequestGate first = new CertificateExternalRequestGate(redisson, 1);
            CertificateExternalRequestGate second = new CertificateExternalRequestGate(redisson, 1);
            CountDownLatch start = new CountDownLatch(1);
            List<Long> acquiredAt = Collections.synchronizedList(new ArrayList<>());

            Future<?> low = executor.submit(() -> acquire(first, false, start, acquiredAt));
            Future<?> highOne = executor.submit(() -> acquire(second, true, start, acquiredAt));
            Future<?> highTwo = executor.submit(() -> acquire(first, true, start, acquiredAt));
            start.countDown();
            low.get(8, TimeUnit.SECONDS);
            highOne.get(8, TimeUnit.SECONDS);
            highTwo.get(8, TimeUnit.SECONDS);

            List<Long> ordered = new ArrayList<>(acquiredAt);
            Collections.sort(ordered);
            assertTrue("应当获取三个许可", ordered.size() == 3);
            for (int index = 1; index < ordered.size(); index++) {
                long intervalMillis = TimeUnit.NANOSECONDS.toMillis(
                        ordered.get(index) - ordered.get(index - 1));
                assertTrue("跨实例请求间隔不应小于850ms，实际=" + intervalMillis,
                        intervalMillis >= 850L);
            }
        } finally {
            executor.shutdownNow();
            try {
                redisson.getKeys().delete(RATE_KEY, PRIORITY_KEY);
            } catch (RuntimeException ignored) {
            }
            redisson.shutdown();
        }
    }

    private void acquire(CertificateExternalRequestGate gate, boolean highPriority,
                         CountDownLatch start, List<Long> acquiredAt) {
        try {
            start.await();
            gate.acquire(highPriority);
            acquiredAt.add(System.nanoTime());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
