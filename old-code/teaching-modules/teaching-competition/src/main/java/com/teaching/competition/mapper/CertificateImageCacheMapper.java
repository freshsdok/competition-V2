package com.teaching.competition.mapper;

import com.teaching.competition.domain.CertificateExportTask;
import com.teaching.competition.domain.CertificateImageCache;
import com.teaching.competition.domain.CertificateImageCacheQuery;
import com.teaching.competition.domain.CertificateImageCacheStats;
import com.teaching.competition.domain.CertificateImageSyncRun;
import com.teaching.competition.domain.GuidedCertificateQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CertificateImageCacheMapper {
    int seedAllGuidedCertificates();

    int seedGuidedCertificates(@Param("userId") Long userId);

    List<CertificateImageCache> selectGuidedPage(@Param("userId") Long userId,
                                                  @Param("query") GuidedCertificateQuery query);

    List<String> selectGuidedContestNames(@Param("userId") Long userId);

    List<String> selectGuidedSessions(@Param("userId") Long userId);

    List<String> selectGuidedContestAreas(@Param("userId") Long userId);

    List<Integer> selectGuidedYears(@Param("userId") Long userId);

    List<String> selectAuthorizedCodes(@Param("userId") Long userId,
                                       @Param("certCodes") List<String> certCodes);

    List<String> selectAllAuthorizedCodes(@Param("userId") Long userId);

    CertificateImageCache selectByCertCode(@Param("certCode") String certCode);

    List<CertificateImageCache> selectByCertCodes(@Param("certCodes") List<String> certCodes);

    List<CertificateImageCache> selectSyncCandidates(@Param("limit") int limit);

    long countSyncCandidates();

    long countPendingOrSyncing();

    List<CertificateImageCache> selectAdminPage(@Param("query") CertificateImageCacheQuery query);

    CertificateImageCacheStats selectStats();

    int markSyncing(@Param("certCode") String certCode);

    int releaseSyncing(@Param("certCode") String certCode);

    int markSuccess(CertificateImageCache cache);

    int markNotFound(CertificateImageCache cache);

    int markFailed(CertificateImageCache cache);

    int recoverStaleSyncing(@Param("staleBefore") Date staleBefore);

    int retryFailedRecords();

    int resetCertificate(@Param("certCode") String certCode);

    int insertRun(CertificateImageSyncRun run);

    int updateRun(CertificateImageSyncRun run);

    CertificateImageSyncRun selectActiveRun();

    CertificateImageSyncRun selectLatestRun();

    CertificateImageSyncRun selectRunById(@Param("runId") Long runId);

    List<CertificateImageSyncRun> selectRunHistory();

    int pauseRun(@Param("runId") Long runId);

    int resumeRun(@Param("runId") Long runId);

    int failInterruptedRuns(@Param("message") String message);

    int insertExportTask(CertificateExportTask task);

    int updateExportTask(CertificateExportTask task);

    CertificateExportTask selectExportTask(@Param("taskId") String taskId);

    List<CertificateExportTask> selectRecoverableExportTasks();

    List<CertificateExportTask> selectExpiredExportTasks(@Param("now") Date now,
                                                          @Param("limit") int limit);

    int markExportExpired(@Param("taskId") String taskId);
}
