package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.competition.domain.CertificateDownloadSummary;
import com.teaching.competition.domain.CertificatePictureListResult;
import com.teaching.competition.mapper.UserCertificateOriginMapper;
import com.teaching.competition.service.ICertificateDownloadService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 负责人证书统计。旧的同步全量图片查询方法已停用，防止绕过全局限流和本地缓存。
 */
@Service
public class CertificateDownloadServiceImpl implements ICertificateDownloadService {
    private final UserCertificateOriginMapper userCertificateOriginMapper;

    public CertificateDownloadServiceImpl(UserCertificateOriginMapper userCertificateOriginMapper) {
        this.userCertificateOriginMapper = userCertificateOriginMapper;
    }

    @Override
    public CertificateDownloadSummary getGuidedCertificateSummary(Long userId) {
        validateUserId(userId);
        List<String> certCodes = userCertificateOriginMapper.selectGuidedCertificateCodes(userId);
        CertificateDownloadSummary summary = new CertificateDownloadSummary();
        summary.setTeamCount(userCertificateOriginMapper.countGuidedCertificateTeams(userId));
        summary.setCertificateCount(certCodes.size());
        summary.setDownloadable(!certCodes.isEmpty());
        return summary;
    }

    @Override
    @Deprecated
    public CertificatePictureListResult getGuidedCertificatePictures(Long userId) {
        validateUserId(userId);
        throw new GlobalException("同步全量图片接口已废弃，请使用分页缓存和异步导出接口");
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new GlobalException("登录状态已失效，请重新登录");
        }
    }
}
