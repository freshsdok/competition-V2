package com.teaching.competition.service.impl;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.poi.InMemoryMultipartFile;
import com.teaching.competition.domain.CertificateFilterOptions;
import com.teaching.competition.domain.CertificateImageCache;
import com.teaching.competition.domain.CertificateImageCacheQuery;
import com.teaching.competition.domain.CertificateImageCacheStats;
import com.teaching.competition.domain.CertificateImageDownload;
import com.teaching.competition.domain.CertificateImageSyncRun;
import com.teaching.competition.domain.CertificatePictureItem;
import com.teaching.competition.domain.GuidedCertificateQuery;
import com.teaching.competition.mapper.CertificateImageCacheMapper;
import com.teaching.competition.service.CertificateExternalRequestGate;
import com.teaching.competition.service.CertificateImageCacheService;
import com.teaching.competition.service.MiitecCertificatePictureClient;
import com.teaching.system.api.RemoteOssUploadService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CertificateImageCacheServiceImpl implements CertificateImageCacheService {
    private static final Logger log = LoggerFactory.getLogger(CertificateImageCacheServiceImpl.class);
    private static final String CREATE_LOCK = "competition:certificate:image:sync:create";
    private static final String RUN_LOCK = "competition:certificate:image:sync:run";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_FALLBACK_CODES = 100;
    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger(1);

    private final CertificateImageCacheMapper cacheMapper;
    private final MiitecCertificatePictureClient pictureClient;
    private final CertificateExternalRequestGate requestGate;
    private final RemoteOssUploadService ossUploadService;
    private final RedissonClient redissonClient;
    private final long[] retryDelaysMillis;
    private final long failureCooldownMillis;
    private final ExecutorService batchExecutor;
    private final ExecutorService fallbackExecutor;
    private final Set<String> fallbackQueued = ConcurrentHashMap.newKeySet();

    public CertificateImageCacheServiceImpl(
            CertificateImageCacheMapper cacheMapper,
            MiitecCertificatePictureClient pictureClient,
            CertificateExternalRequestGate requestGate,
            RemoteOssUploadService ossUploadService,
            RedissonClient redissonClient,
            @Value("${competition.certificate.image.retry-delay-1-ms:5000}") long retryDelay1,
            @Value("${competition.certificate.image.retry-delay-2-ms:30000}") long retryDelay2,
            @Value("${competition.certificate.image.retry-delay-3-ms:120000}") long retryDelay3,
            @Value("${competition.certificate.image.failure-cooldown-hours:6}") long cooldownHours) {
        this.cacheMapper = cacheMapper;
        this.pictureClient = pictureClient;
        this.requestGate = requestGate;
        this.ossUploadService = ossUploadService;
        this.redissonClient = redissonClient;
        this.retryDelaysMillis = new long[]{
                Math.max(0, retryDelay1), Math.max(0, retryDelay2), Math.max(0, retryDelay3)};
        this.failureCooldownMillis = Duration.ofHours(Math.max(1, cooldownHours)).toMillis();
        this.batchExecutor = singleExecutor("certificate-cache-batch-", 2);
        this.fallbackExecutor = singleExecutor("certificate-cache-priority-", 500);
    }

    @PostConstruct
    public void recoverInterruptedWork() {
        cacheMapper.recoverStaleSyncing(Date.from(Instant.now().minus(Duration.ofMinutes(10))));
        CertificateImageSyncRun activeRun = cacheMapper.selectActiveRun();
        if (activeRun != null) {
            batchExecutor.execute(() -> executeBatch(activeRun.getRunId()));
        }
    }

    @PreDestroy
    public void shutdownExecutors() {
        batchExecutor.shutdownNow();
        fallbackExecutor.shutdownNow();
    }

    @Override
    public void seedGuidedCertificates(Long userId) {
        validateUserId(userId);
        cacheMapper.seedGuidedCertificates(userId);
    }

    @Override
    public List<CertificateImageCache> selectGuidedPage(Long userId, GuidedCertificateQuery query) {
        validateUserId(userId);
        return cacheMapper.selectGuidedPage(userId, query);
    }

    @Override
    public CertificateFilterOptions selectGuidedFilterOptions(Long userId) {
        validateUserId(userId);
        CertificateFilterOptions options = new CertificateFilterOptions();
        options.setContestNames(cacheMapper.selectGuidedContestNames(userId));
        options.setSessions(cacheMapper.selectGuidedSessions(userId));
        options.setContestAreas(cacheMapper.selectGuidedContestAreas(userId));
        options.setRuningNumYears(cacheMapper.selectGuidedYears(userId));
        return options;
    }

    @Override
    public Map<String, Object> enqueueFallback(Long userId, List<String> certCodes) {
        validateUserId(userId);
        cacheMapper.seedGuidedCertificates(userId);
        List<String> normalized = normalizeCodes(certCodes, MAX_FALLBACK_CODES);
        if (normalized.isEmpty()) {
            throw new GlobalException("请选择需要获取图片的证书");
        }
        List<String> authorized = cacheMapper.selectAuthorizedCodes(userId, normalized);
        if (authorized.size() != normalized.size()) {
            throw new GlobalException("请求中包含无权访问的证书编号");
        }

        int accepted = 0;
        for (String certCode : authorized) {
            CertificateImageCache cache = cacheMapper.selectByCertCode(certCode);
            if (cache != null && CertificateImageCache.SUCCESS.equals(cache.getCacheStatus())) {
                continue;
            }
            if (cache != null && (CertificateImageCache.SYNCING.equals(cache.getCacheStatus())
                    || (cache.getNextRetryTime() != null && cache.getNextRetryTime().after(new Date())))) {
                continue;
            }
            if (fallbackQueued.add(certCode)) {
                accepted++;
                fallbackExecutor.execute(() -> {
                    try {
                        syncOne(certCode, true);
                    } finally {
                        fallbackQueued.remove(certCode);
                    }
                });
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("acceptedCount", accepted);
        result.put("requestedCount", normalized.size());
        result.put("status", accepted > 0 ? "QUEUED" : "UNCHANGED");
        return result;
    }

    @Override
    public String getPreviewUrl(Long userId, String certCode) {
        validateUserId(userId);
        List<String> authorized = cacheMapper.selectAuthorizedCodes(userId, List.of(certCode));
        if (authorized.size() != 1) {
            throw new GlobalException("无权查看该证书图片");
        }
        CertificateImageCache cache = cacheMapper.selectByCertCode(certCode);
        if (cache == null || !CertificateImageCache.SUCCESS.equals(cache.getCacheStatus())
                || StringUtils.isBlank(cache.getObjectKey())) {
            enqueueFallback(userId, List.of(certCode));
            throw new GlobalException("证书图片正在获取，请稍后刷新");
        }
        R<String> response = ossUploadService.getPresignedUrl(cache.getObjectKey());
        if (response == null || R.isError(response) || StringUtils.isBlank(response.getData())) {
            throw new GlobalException("生成证书图片预览地址失败");
        }
        return response.getData();
    }

    @Override
    public CertificateImageCacheStats getStats() {
        CertificateImageCacheStats stats = cacheMapper.selectStats();
        if (stats == null) {
            stats = new CertificateImageCacheStats();
        }
        stats.setRequestsPerSecond(requestGate.getRequestsPerSecond());
        stats.setCoverageRate(stats.getTotalCount() == 0 ? 0D
                : Math.round(stats.getSuccessCount() * 10000D / stats.getTotalCount()) / 100D);
        long remaining = stats.getPendingCount() + stats.getSyncingCount()
                + stats.getNotFoundCount() + stats.getFailedCount();
        stats.setEstimatedRemainingSeconds((long) Math.ceil(
                remaining / (double) requestGate.getRequestsPerSecond()));
        CertificateImageSyncRun latest = cacheMapper.selectLatestRun();
        stats.setCurrentRun(cacheMapper.selectActiveRun());
        stats.setLastRunTime(latest == null ? null : latest.getStartTime());
        return stats;
    }

    @Override
    public List<CertificateImageCache> selectAdminPage(CertificateImageCacheQuery query) {
        return cacheMapper.selectAdminPage(query);
    }

    @Override
    public CertificateImageSyncRun startBatch(String source, Long operatorId, String operatorName) {
        RLock createLock = redissonClient.getLock(CREATE_LOCK);
        boolean locked = false;
        try {
            // 使用 Redisson watchdog 续期，避免首次 seed 超过10秒后锁提前失效。
            locked = createLock.tryLock();
            if (!locked) {
                throw new GlobalException("同步任务正在创建，请稍后刷新");
            }
            CertificateImageSyncRun active = cacheMapper.selectActiveRun();
            if (active != null) {
                return active;
            }
            cacheMapper.seedAllGuidedCertificates();
            long total = cacheMapper.countSyncCandidates();
            CertificateImageSyncRun run = new CertificateImageSyncRun();
            run.setSource(StringUtils.isBlank(source) ? "MANUAL" : source);
            run.setRunStatus(CertificateImageSyncRun.RUNNING);
            run.setTotalCount(Math.max(0, total));
            run.setProcessedCount(0L);
            run.setSuccessCount(0L);
            run.setFailureCount(0L);
            run.setRequestsPerSecond(requestGate.getRequestsPerSecond());
            run.setOperatorId(operatorId);
            run.setOperatorName(operatorName);
            run.setStartTime(new Date());
            cacheMapper.insertRun(run);
            batchExecutor.execute(() -> executeBatch(run.getRunId()));
            return run;
        } finally {
            if (locked && createLock.isHeldByCurrentThread()) {
                createLock.unlock();
            }
        }
    }

    @Override
    public CertificateImageSyncRun pauseBatch() {
        CertificateImageSyncRun active = requireActiveRun();
        cacheMapper.pauseRun(active.getRunId());
        return cacheMapper.selectRunById(active.getRunId());
    }

    @Override
    public CertificateImageSyncRun resumeBatch() {
        CertificateImageSyncRun active = requireActiveRun();
        cacheMapper.resumeRun(active.getRunId());
        return cacheMapper.selectRunById(active.getRunId());
    }

    @Override
    public int retryFailedRecords() {
        CertificateImageSyncRun active = cacheMapper.selectActiveRun();
        if (active != null && CertificateImageSyncRun.RUNNING.equals(active.getRunStatus())) {
            throw new GlobalException("请先暂停当前同步任务再重试失败记录");
        }
        return cacheMapper.retryFailedRecords();
    }

    @Override
    public int resetCertificate(String certCode) {
        if (StringUtils.isBlank(certCode)) {
            throw new GlobalException("请提供证书编号");
        }
        String normalized = certCode.trim();
        CertificateImageCache cache = cacheMapper.selectByCertCode(normalized);
        if (cache == null) {
            throw new GlobalException("未找到该证书缓存记录");
        }
        if (CertificateImageCache.SYNCING.equals(cache.getCacheStatus())) {
            throw new GlobalException("该证书正在同步，请等待当前请求完成后再重置");
        }
        return cacheMapper.resetCertificate(normalized);
    }

    @Override
    public CertificateImageSyncRun getCurrentRun() {
        return cacheMapper.selectActiveRun();
    }

    @Override
    public List<CertificateImageSyncRun> getRunHistory() {
        return cacheMapper.selectRunHistory();
    }

    @Override
    public boolean syncOne(String certCode, boolean highPriority) {
        return synchronizeCertificate(certCode, highPriority, null) == SyncOutcome.SUCCESS;
    }

    @Scheduled(cron = "${competition.certificate.image.sync-cron:0 0 0 * * ?}", zone = "Asia/Shanghai")
    public void scheduledSync() {
        try {
            startBatch("SCHEDULED", null, "system");
        } catch (RuntimeException exception) {
            log.warn("每日证书图片同步未启动：{}", exception.getMessage());
        }
    }

    private void executeBatch(Long runId) {
        RLock runLock = redissonClient.getLock(RUN_LOCK);
        boolean locked = false;
        try {
            locked = runLock.tryLock();
            if (!locked) {
                log.info("已有实例持有证书图片同步锁，runId={}等待现有实例处理", runId);
                return;
            }
            CertificateImageSyncRun run = cacheMapper.selectRunById(runId);
            if (run == null || (!CertificateImageSyncRun.RUNNING.equals(run.getRunStatus())
                    && !CertificateImageSyncRun.PAUSED.equals(run.getRunStatus()))) {
                return;
            }
            if (StringUtils.isNotBlank(run.getCurrentCertCode())) {
                // 当前实例已取得全局批次锁，说明上一实例已退出；释放其最后一条在途标记后续跑。
                cacheMapper.releaseSyncing(run.getCurrentCertCode());
            }

            while (!Thread.currentThread().isInterrupted()) {
                run = cacheMapper.selectRunById(runId);
                if (run == null) {
                    return;
                }
                if (CertificateImageSyncRun.PAUSED.equals(run.getRunStatus())) {
                    Thread.sleep(1000L);
                    continue;
                }
                if (!CertificateImageSyncRun.RUNNING.equals(run.getRunStatus())) {
                    return;
                }

                List<CertificateImageCache> candidates = cacheMapper.selectSyncCandidates(BATCH_SIZE);
                if (candidates.isEmpty()) {
                    // 用户高优先级任务可能正在处理 SYNCING；初次全量完成必须真正没有 PENDING/SYNCING。
                    cacheMapper.recoverStaleSyncing(
                            Date.from(Instant.now().minus(Duration.ofMinutes(10))));
                    if (cacheMapper.countPendingOrSyncing() > 0) {
                        Thread.sleep(500L);
                        continue;
                    }
                    run.setRunStatus(CertificateImageSyncRun.COMPLETED);
                    run.setCurrentCertCode(null);
                    run.setEndTime(new Date());
                    cacheMapper.updateRun(run);
                    return;
                }

                for (CertificateImageCache candidate : candidates) {
                    CertificateImageSyncRun fresh = cacheMapper.selectRunById(runId);
                    if (fresh == null || !CertificateImageSyncRun.RUNNING.equals(fresh.getRunStatus())) {
                        break;
                    }
                    run = fresh;
                    run.setCurrentCertCode(candidate.getCertCode());
                    cacheMapper.updateRun(run);
                    SyncOutcome outcome = synchronizeCertificate(candidate.getCertCode(), false, runId);
                    if (outcome == SyncOutcome.SKIPPED) {
                        continue;
                    }
                    run.setProcessedCount(value(run.getProcessedCount()) + 1);
                    if (outcome == SyncOutcome.SUCCESS) {
                        run.setSuccessCount(value(run.getSuccessCount()) + 1);
                    } else {
                        run.setFailureCount(value(run.getFailureCount()) + 1);
                    }
                    cacheMapper.updateRun(run);
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            CertificateImageSyncRun interruptedRun = cacheMapper.selectRunById(runId);
            if (interruptedRun != null && StringUtils.isNotBlank(interruptedRun.getCurrentCertCode())) {
                cacheMapper.releaseSyncing(interruptedRun.getCurrentCertCode());
            }
            log.info("证书图片同步线程因服务停止而退出，任务保留供重启续跑，runId={}", runId);
        } catch (RuntimeException exception) {
            if (Thread.currentThread().isInterrupted()) {
                CertificateImageSyncRun interruptedRun = cacheMapper.selectRunById(runId);
                if (interruptedRun != null && StringUtils.isNotBlank(interruptedRun.getCurrentCertCode())) {
                    cacheMapper.releaseSyncing(interruptedRun.getCurrentCertCode());
                }
                log.info("证书图片同步线程因服务停止而退出，任务保留供重启续跑，runId={}", runId);
                return;
            }
            log.error("证书图片批量同步异常，runId={}", runId, exception);
            markRunFailed(runId, shortMessage(exception));
        } finally {
            if (locked && runLock.isHeldByCurrentThread()) {
                runLock.unlock();
            }
        }
    }

    private SyncOutcome synchronizeCertificate(String certCode, boolean highPriority, Long runId) {
        CertificateImageCache existing = cacheMapper.selectByCertCode(certCode);
        if (existing == null) {
            return SyncOutcome.SKIPPED;
        }
        if (CertificateImageCache.SUCCESS.equals(existing.getCacheStatus())) {
            return SyncOutcome.SUCCESS;
        }
        if (cacheMapper.markSyncing(certCode) == 0) {
            CertificateImageCache current = cacheMapper.selectByCertCode(certCode);
            return current != null && CertificateImageCache.SUCCESS.equals(current.getCacheStatus())
                    ? SyncOutcome.SUCCESS : SyncOutcome.SKIPPED;
        }

        CertificatePictureItem lastItem = null;
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt <= retryDelaysMillis.length; attempt++) {
            if (!awaitRunAllowsRequest(runId)) {
                cacheMapper.releaseSyncing(certCode);
                return SyncOutcome.SKIPPED;
            }
            try {
                requestGate.acquire(highPriority);
                // 限流器等待期间可能被管理员暂停；获得许可后再检查一次，确保暂停期间不发出新请求。
                if (!awaitRunAllowsRequest(runId)) {
                    cacheMapper.releaseSyncing(certCode);
                    return SyncOutcome.SKIPPED;
                }
                lastItem = pictureClient.queryCertificateInfo(certCode);
                if (lastItem != null && StringUtils.isNotBlank(lastItem.getCertPicture())) {
                    CertificateImageDownload image = pictureClient.downloadCertificatePicture(
                            lastItem.getCertPicture());
                    CertificateImageCache success = buildSuccessCache(certCode, lastItem, image);
                    R<String> upload = ossUploadService.ossUpload(
                            new InMemoryMultipartFile(image.content(), success.getFileName(), image.contentType()),
                            "certificate-image-cache", String.valueOf(success.getRuningNumYear()));
                    if (upload == null || R.isError(upload) || StringUtils.isBlank(upload.getData())) {
                        throw new IllegalStateException("上传证书图片到私有对象存储失败"
                                + (upload == null ? "" : "：" + upload.getMsg()));
                    }
                    success.setObjectKey(upload.getData());
                    try {
                        if (cacheMapper.markSuccess(success) == 0) {
                            throw new IllegalStateException("证书图片缓存元数据更新失败");
                        }
                    } catch (RuntimeException exception) {
                        deleteObjectQuietly(success.getObjectKey(), "回滚未落库的新缓存对象");
                        throw exception;
                    }
                    deleteReplacedObject(existing.getObjectKey(), success.getObjectKey());
                    return SyncOutcome.SUCCESS;
                }
                lastFailure = null;
            } catch (RuntimeException exception) {
                lastFailure = exception;
            }

            if (attempt < retryDelaysMillis.length) {
                sleepRetry(retryDelaysMillis[attempt]);
            }
        }

        CertificateImageCache failure = new CertificateImageCache();
        failure.setCertCode(certCode);
        copyMetadata(lastItem, failure);
        failure.setRetryCount(retryDelaysMillis.length + 1);
        failure.setNextRetryTime(Date.from(Instant.now().plusMillis(failureCooldownMillis)));
        if (lastFailure == null) {
            failure.setLastError(lastItem == null ? "外部平台未返回该证书" : "外部平台返回证书信息但没有图片");
            cacheMapper.markNotFound(failure);
        } else {
            failure.setLastError(shortMessage(lastFailure));
            cacheMapper.markFailed(failure);
        }
        return SyncOutcome.FAILED;
    }

    private void deleteReplacedObject(String previousObjectKey, String currentObjectKey) {
        if (StringUtils.isBlank(previousObjectKey) || previousObjectKey.equals(currentObjectKey)) {
            return;
        }
        deleteObjectQuietly(previousObjectKey, "删除旧缓存对象");
    }

    private void deleteObjectQuietly(String objectKey, String action) {
        try {
            R<Boolean> deleted = ossUploadService.deleteFile(objectKey);
            if (deleted == null || R.isError(deleted) || !Boolean.TRUE.equals(deleted.getData())) {
                log.warn("{}失败，objectKey={}", action, objectKey);
            }
        } catch (RuntimeException exception) {
            log.warn("{}时异常，objectKey={}", action, objectKey, exception);
        }
    }

    /** 暂停后不再产生新的外部请求；已经在途的单次请求允许自然结束。 */
    private boolean awaitRunAllowsRequest(Long runId) {
        if (runId == null) {
            return true;
        }
        while (!Thread.currentThread().isInterrupted()) {
            CertificateImageSyncRun run = cacheMapper.selectRunById(runId);
            if (run == null) {
                return false;
            }
            if (CertificateImageSyncRun.RUNNING.equals(run.getRunStatus())) {
                return true;
            }
            if (!CertificateImageSyncRun.PAUSED.equals(run.getRunStatus())) {
                return false;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private CertificateImageCache buildSuccessCache(String certCode, CertificatePictureItem item,
                                                     CertificateImageDownload image) {
        CertificateImageCache cache = new CertificateImageCache();
        cache.setCertCode(certCode);
        copyMetadata(item, cache);
        cache.setFileName(buildFileName(certCode, item.getCertPicture(), image.contentType()));
        cache.setMimeType(image.contentType());
        cache.setFileSize((long) image.content().length);
        cache.setSha256(sha256(image.content()));
        return cache;
    }

    private void copyMetadata(CertificatePictureItem source, CertificateImageCache target) {
        if (source == null) {
            return;
        }
        target.setContestName(source.getContestName());
        target.setName(source.getName());
        target.setSession(source.getSession());
        target.setContestArea(source.getContestArea());
        target.setRuningNumYear(source.getRuningNumYear());
    }

    private String buildFileName(String certCode, String url, String contentType) {
        String extension = null;
        try {
            String path = URI.create(url).getPath();
            int dot = path.lastIndexOf('.');
            if (dot >= 0 && dot < path.length() - 1) {
                extension = path.substring(dot + 1).replaceAll("[^A-Za-z0-9]", "")
                        .toLowerCase(Locale.ROOT);
            }
        } catch (RuntimeException ignored) {
        }
        if (StringUtils.isBlank(extension) || extension.length() > 8) {
            extension = contentType != null && contentType.toLowerCase().contains("png") ? "png" : "jpg";
        }
        return certCode + "." + extension;
    }

    private String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算证书图片摘要失败", exception);
        }
    }

    private void sleepRetry(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("证书图片重试被中断", exception);
        }
    }

    private void markRunFailed(Long runId, String message) {
        CertificateImageSyncRun run = cacheMapper.selectRunById(runId);
        if (run == null || CertificateImageSyncRun.COMPLETED.equals(run.getRunStatus())) {
            return;
        }
        run.setRunStatus(CertificateImageSyncRun.FAILED);
        run.setLastError(message);
        run.setEndTime(new Date());
        cacheMapper.updateRun(run);
    }

    private CertificateImageSyncRun requireActiveRun() {
        CertificateImageSyncRun active = cacheMapper.selectActiveRun();
        if (active == null) {
            throw new GlobalException("当前没有可操作的同步任务");
        }
        return active;
    }

    private List<String> normalizeCodes(List<String> certCodes, int limit) {
        if (certCodes == null || certCodes.isEmpty()) {
            return List.of();
        }
        Set<String> unique = ConcurrentHashMap.newKeySet();
        List<String> result = new ArrayList<>();
        for (String code : certCodes) {
            if (StringUtils.isBlank(code)) {
                continue;
            }
            String normalized = code.trim();
            if (unique.add(normalized)) {
                result.add(normalized);
            }
            if (result.size() > limit) {
                throw new GlobalException("单次最多提交" + limit + "个证书编号");
            }
        }
        return result;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new GlobalException("登录状态已失效，请重新登录");
        }
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }

    private String shortMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (StringUtils.isBlank(message)) {
            message = current.getClass().getSimpleName();
        }
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 900) {
            return message;
        }
        return new String(bytes, 0, 900, StandardCharsets.UTF_8);
    }

    private static ExecutorService singleExecutor(String prefix, int queueCapacity) {
        return new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), task -> {
                    Thread thread = new Thread(task, prefix + THREAD_SEQUENCE.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    private enum SyncOutcome {
        SUCCESS, FAILED, SKIPPED
    }
}
