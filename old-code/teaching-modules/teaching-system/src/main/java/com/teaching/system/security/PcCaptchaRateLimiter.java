package com.teaching.system.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * Redis-backed limiter for public PC verification-code requests.
 *
 * <p>The account and trusted client-IP counters are incremented atomically.
 * Counters expire from their first request so repeated blocked calls cannot
 * create a permanent lock. Identifiers are hashed before becoming Redis keys.</p>
 */
@Service
@SuppressWarnings({"rawtypes", "unchecked"})
public class PcCaptchaRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(PcCaptchaRateLimiter.class);
    private static final String KEY_PREFIX = "security:{pc-captcha}:request:";

    private static final DefaultRedisScript<List> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local accountCount = redis.call('INCR', KEYS[1]); "
                    + "if accountCount == 1 then redis.call('EXPIRE', KEYS[1], ARGV[3]); end; "
                    + "local ipCount = redis.call('INCR', KEYS[2]); "
                    + "if ipCount == 1 then redis.call('EXPIRE', KEYS[2], ARGV[3]); end; "
                    + "if accountCount > tonumber(ARGV[1]) or ipCount > tonumber(ARGV[2]) then "
                    + "  local ttl = math.max(redis.call('TTL', KEYS[1]), "
                    + "redis.call('TTL', KEYS[2]), 1); "
                    + "  return {0, ttl}; "
                    + "end; "
                    + "return {1, 0};",
            List.class);

    private final RedisTemplate redisTemplate;

    @Value("${security.pc-captcha.rate-limit.account-threshold:3}")
    private int accountThreshold = 3;

    @Value("${security.pc-captcha.rate-limit.ip-threshold:20}")
    private int ipThreshold = 20;

    @Value("${security.pc-captcha.rate-limit.window-seconds:600}")
    private long windowSeconds = 600L;

    public PcCaptchaRateLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Consumes one send allowance. Redis failures are fail-closed so an outage
     * cannot turn the endpoint into an unbounded mail/SMS relay.
     */
    public SendStatus consume(String account, String clientIp) {
        try {
            List<Long> values = (List<Long>) redisTemplate.execute(
                    CONSUME_SCRIPT,
                    rateLimitKeys(account, clientIp),
                    safeAccountThreshold(),
                    safeIpThreshold(),
                    safeWindowSeconds());
            if (values == null || values.size() < 2) {
                log.error("Redis returned an invalid PC captcha rate-limit result");
                return SendStatus.unavailable();
            }
            return number(values.get(0)) == 1L
                    ? SendStatus.allowed()
                    : SendStatus.blocked(normalizeTtl(number(values.get(1))));
        } catch (RuntimeException e) {
            log.error("Unable to update PC captcha rate-limit state; dispatch is denied safely", e);
            return SendStatus.unavailable();
        }
    }

    private List<String> rateLimitKeys(String account, String clientIp) {
        return Arrays.asList(
                KEY_PREFIX + "account:" + sha256(normalizeAccount(account)),
                KEY_PREFIX + "ip:" + sha256(normalizeIp(clientIp)));
    }

    private int safeAccountThreshold() {
        return Math.max(1, accountThreshold);
    }

    private int safeIpThreshold() {
        return Math.max(1, ipThreshold);
    }

    private int safeWindowSeconds() {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, windowSeconds));
    }

    private long normalizeTtl(long ttl) {
        return ttl > 0 ? ttl : safeWindowSeconds();
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String normalizeAccount(String account) {
        return account == null ? "" : account.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeIp(String clientIp) {
        return clientIp == null ? "unknown" : clientIp.trim().toLowerCase(Locale.ROOT);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public static final class SendStatus {
        private final boolean available;
        private final boolean blocked;
        private final long retryAfterSeconds;

        private SendStatus(boolean available, boolean blocked, long retryAfterSeconds) {
            this.available = available;
            this.blocked = blocked;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public static SendStatus allowed() {
            return new SendStatus(true, false, 0L);
        }

        public static SendStatus blocked(long retryAfterSeconds) {
            return new SendStatus(true, true, Math.max(1L, retryAfterSeconds));
        }

        public static SendStatus unavailable() {
            return new SendStatus(false, true, 0L);
        }

        public boolean isAvailable() {
            return available;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}
