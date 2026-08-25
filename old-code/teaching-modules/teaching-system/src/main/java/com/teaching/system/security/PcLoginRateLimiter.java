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
 * Redis-backed, expiring PC login failure limiter.
 *
 * <p>Both the normalized account and trusted client-IP dimensions are
 * incremented in one Lua script. INCR and first-write expiry are therefore
 * atomic and a process crash cannot leave a permanent lock key.</p>
 */
@Service
@SuppressWarnings({"rawtypes", "unchecked"})
public class PcLoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(PcLoginRateLimiter.class);
    // Hash tag keeps all keys used by a Lua invocation in one Redis Cluster slot.
    private static final String KEY_PREFIX = "security:{pc-login}:fail:";

    private static final DefaultRedisScript<List> RESERVE_SCRIPT = new DefaultRedisScript<>(
            "local accountFailures = tonumber(redis.call('GET', KEYS[1]) or '0'); "
                    + "local ipFailures = tonumber(redis.call('GET', KEYS[2]) or '0'); "
                    + "local accountPending = redis.call('INCR', KEYS[3]); "
                    + "if accountPending == 1 then redis.call('EXPIRE', KEYS[3], ARGV[3]); end; "
                    + "local ipPending = redis.call('INCR', KEYS[4]); "
                    + "if ipPending == 1 then redis.call('EXPIRE', KEYS[4], ARGV[3]); end; "
                    + "if accountFailures + accountPending > tonumber(ARGV[1]) "
                    + "or ipFailures + ipPending > tonumber(ARGV[2]) then "
                    + "  local accountLeft = redis.call('DECR', KEYS[3]); "
                    + "  if accountLeft <= 0 then redis.call('DEL', KEYS[3]); end; "
                    + "  local ipLeft = redis.call('DECR', KEYS[4]); "
                    + "  if ipLeft <= 0 then redis.call('DEL', KEYS[4]); end; "
                    + "  local ttl = math.max(redis.call('TTL', KEYS[1]), redis.call('TTL', KEYS[2]), "
                    + "redis.call('TTL', KEYS[3]), redis.call('TTL', KEYS[4]), 1); "
                    + "  return {0, ttl}; "
                    + "end; "
                    + "return {1, 0};",
            List.class);

    private static final DefaultRedisScript<List> FAILURE_SCRIPT = new DefaultRedisScript<>(
            "local accountPending = redis.call('DECR', KEYS[3]); "
                    + "if accountPending <= 0 then redis.call('DEL', KEYS[3]); end; "
                    + "local ipPending = redis.call('DECR', KEYS[4]); "
                    + "if ipPending <= 0 then redis.call('DEL', KEYS[4]); end; "
                    + "local accountCount = redis.call('INCR', KEYS[1]); "
                    + "if accountCount == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; "
                    + "local ipCount = redis.call('INCR', KEYS[2]); "
                    + "if ipCount == 1 then redis.call('EXPIRE', KEYS[2], ARGV[1]); end; "
                    + "local accountTtl = redis.call('TTL', KEYS[1]); "
                    + "local ipTtl = redis.call('TTL', KEYS[2]); "
                    + "return {accountCount, ipCount, accountTtl, ipTtl};",
            List.class);

    private static final DefaultRedisScript<Long> SUCCESS_SCRIPT = new DefaultRedisScript<>(
            "local accountPending = redis.call('DECR', KEYS[2]); "
                    + "if accountPending <= 0 then redis.call('DEL', KEYS[2]); end; "
                    + "local ipPending = redis.call('DECR', KEYS[3]); "
                    + "if ipPending <= 0 then redis.call('DEL', KEYS[3]); end; "
                    + "redis.call('DEL', KEYS[1]); "
                    + "return 1;",
            Long.class);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "local accountPending = redis.call('DECR', KEYS[1]); "
                    + "if accountPending <= 0 then redis.call('DEL', KEYS[1]); end; "
                    + "local ipPending = redis.call('DECR', KEYS[2]); "
                    + "if ipPending <= 0 then redis.call('DEL', KEYS[2]); end; "
                    + "return 1;",
            Long.class);

    private final RedisTemplate redisTemplate;

    @Value("${security.pc-login.rate-limit.account-threshold:5}")
    private int accountThreshold = 5;

    @Value("${security.pc-login.rate-limit.ip-threshold:30}")
    private int ipThreshold = 30;

    @Value("${security.pc-login.rate-limit.window-seconds:900}")
    private long windowSeconds = 900L;

    @Value("${security.pc-login.rate-limit.reservation-seconds:30}")
    private long reservationSeconds = 30L;

    public PcLoginRateLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically reserves capacity before password verification. Existing
     * failures and currently-running requests are evaluated together, closing
     * the concurrent pre-check race.
     */
    public LimitStatus reserveAttempt(String account, String clientIp) {
        try {
            List<Long> values = (List<Long>) redisTemplate.execute(
                    RESERVE_SCRIPT,
                    rateLimitKeys(account, clientIp),
                    safeAccountThreshold(),
                    safeIpThreshold(),
                    safeReservationSeconds());
            if (values == null || values.size() < 2) {
                log.error("Redis returned an invalid PC login reservation result");
                return LimitStatus.unavailable();
            }
            return number(values.get(0)) == 1L
                    ? LimitStatus.allowed()
                    : LimitStatus.blocked(normalizeTtl(number(values.get(1))));
        } catch (RuntimeException e) {
            log.error("Unable to reserve PC login rate-limit capacity; login is denied safely", e);
            return LimitStatus.unavailable();
        }
    }

    public LimitStatus recordFailure(String account, String clientIp) {
        try {
            List<Long> values = (List<Long>) redisTemplate.execute(
                    FAILURE_SCRIPT,
                    rateLimitKeys(account, clientIp),
                    safeWindowSeconds());
            if (values == null || values.size() < 4) {
                log.error("Redis returned an invalid PC login rate-limit result");
                return LimitStatus.unavailable();
            }

            long accountCount = number(values.get(0));
            long ipCount = number(values.get(1));
            long retryAfter = Math.max(normalizeTtl(number(values.get(2))),
                    normalizeTtl(number(values.get(3))));
            if (accountCount >= safeAccountThreshold() || ipCount >= safeIpThreshold()) {
                return LimitStatus.blocked(retryAfter);
            }
            return LimitStatus.allowed();
        } catch (RuntimeException e) {
            log.error("Unable to update PC login rate-limit state; login is denied safely", e);
            return LimitStatus.unavailable();
        }
    }

    /**
     * Completes a successful attempt and clears only the account failure key.
     * IP failure history is deliberately retained so one valid account cannot
     * be used to reset protection for every account behind the same source.
     */
    public boolean recordSuccess(String account, String clientIp) {
        try {
            List<String> keys = rateLimitKeys(account, clientIp);
            Long result = (Long) redisTemplate.execute(SUCCESS_SCRIPT,
                    Arrays.asList(keys.get(0), keys.get(2), keys.get(3)));
            return result != null && result == 1L;
        } catch (RuntimeException e) {
            log.error("Unable to complete PC login rate-limit reservation; login is denied safely", e);
            return false;
        }
    }

    public void releaseReservation(String account, String clientIp) {
        try {
            List<String> keys = rateLimitKeys(account, clientIp);
            redisTemplate.execute(RELEASE_SCRIPT, Arrays.asList(keys.get(2), keys.get(3)));
        } catch (RuntimeException e) {
            log.error("Unable to release PC login rate-limit reservation", e);
        }
    }

    private List<String> rateLimitKeys(String account, String clientIp) {
        String accountHash = sha256(normalizeAccount(account));
        String ipHash = sha256(normalizeIp(clientIp));
        return Arrays.asList(
                KEY_PREFIX + "account:" + accountHash,
                KEY_PREFIX + "ip:" + ipHash,
                KEY_PREFIX + "pending-account:" + accountHash,
                KEY_PREFIX + "pending-ip:" + ipHash);
    }

    private long normalizeTtl(long ttl) {
        return ttl > 0 ? ttl : safeWindowSeconds();
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

    private int safeReservationSeconds() {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, reservationSeconds));
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

    public static final class LimitStatus {
        private final boolean available;
        private final boolean blocked;
        private final long retryAfterSeconds;

        private LimitStatus(boolean available, boolean blocked, long retryAfterSeconds) {
            this.available = available;
            this.blocked = blocked;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public static LimitStatus allowed() {
            return new LimitStatus(true, false, 0L);
        }

        public static LimitStatus blocked(long retryAfterSeconds) {
            return new LimitStatus(true, true, Math.max(1L, retryAfterSeconds));
        }

        public static LimitStatus unavailable() {
            return new LimitStatus(false, true, 0L);
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
