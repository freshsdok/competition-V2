package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.mapper.UserCertificateMapper;
import com.teaching.system.api.domain.CertConfigInfo;
import com.teaching.competition.domain.CertOrgInfo;
import com.teaching.competition.mapper.CertConfigInfoMapper;
import com.teaching.competition.mapper.CertOrgInfoMapper;
import com.teaching.competition.service.ICertConfigInfoService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.UserCertificate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 证书配置信息Service业务层处理
 *
 * @author teaching
 */
@Service
public class CertConfigInfoServiceImpl implements ICertConfigInfoService {
    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private CertOrgInfoMapper certOrgInfoMapper;

    @Autowired
    private UserCertificateMapper userCertificateMapper;

    /**
     * 查询证书配置信息
     *
     * @param certConfigId 证书配置信息主键
     * @return 证书配置信息
     */
    @Override
    public CertConfigInfo selectCertConfigInfoById(Long certConfigId) {
        return certConfigInfoMapper.selectCertConfigInfoById(certConfigId);
    }

    /**
     * 查询证书配置信息列表
     *
     * @param certConfigInfo 证书配置信息
     * @return 证书配置信息集合
     */
    @Override
    public List<CertConfigInfo> selectCertConfigInfoList(CertConfigInfo certConfigInfo) {
        // 获取当前用户
        // 如果是超管看所有数据
        boolean admin = SecurityUtils.getLoginUser().getSysUser().isAdmin();
        if(!admin){
            Long userMangerId = SecurityUtils.getUserId();
            certConfigInfo.setCertManagerRole(userMangerId.toString());
        }
        List<CertConfigInfo> certConfigInfos = certConfigInfoMapper.selectCertConfigInfoList(certConfigInfo);
        if(CollectionUtils.isNotEmpty(certConfigInfos)){
            certConfigInfos.stream().forEach(certConfigInfoRes -> {
                if(StringUtils.isNotBlank(certConfigInfoRes.getCertManagerRole())){
                    //翻译管理员角色对应名称
                    List<Long> userIdList = Arrays.stream(StringUtils.split(certConfigInfoRes.getCertManagerRole(), ",")).toList()
                            .stream()
                            .map(Long::parseLong)
                            .toList();
                    StringBuffer sb = new StringBuffer();
                    userIdList.stream().forEach(userId -> {
                        R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
                        if(R.isSuccess(userInfoById) && Objects.nonNull(userInfoById.getData())){
                            sb.append(userInfoById.getData().getNickName()).append(",");
                        }
                    });
                    if(StringUtils.isNotBlank(sb)){
                        certConfigInfoRes.setCertManagerRoleName(sb.substring(0, sb.length() - 1));
                    }
                }
                // 翻译机构
                CertOrgInfo certOrgInfo = certOrgInfoMapper.selectCertOrgInfoByOrgCode(certConfigInfoRes.getOrgCode());
                if(Objects.nonNull(certOrgInfo)){
                    certConfigInfoRes.setOrgCodeName(certOrgInfo.getOrgName());
                }
            });
        }
        return certConfigInfos;
    }

    /**
     * 新增证书配置信息
     *
     * @param certConfigInfo 证书配置信息
     * @return 结果
     */
    @Override
    public int insertCertConfigInfo(CertConfigInfo certConfigInfo) {
        List<CertConfigInfo> certConfigInfos = certConfigInfoMapper.selectCertConfigInfoByName(certConfigInfo.getCertConfigName());
        if(CollectionUtils.isNotEmpty(certConfigInfos)){
            throw new GlobalException("证书配置名称已存在");
        }
        if("1".equals(certConfigInfo.getCertPeriodType())){
            certConfigInfo.setCertPeriodTime(null);
        }
        certConfigInfo.setYear(DateUtils.dateTimeNow("yyyy"));
        certConfigInfo.setCreateTime(DateUtils.getNowDate());
        certConfigInfo.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        return certConfigInfoMapper.insertCertConfigInfo(certConfigInfo);
    }

    /**
     * 修改证书配置信息
     *
     * @param certConfigInfo 证书配置信息
     * @return 结果
     */
    @Override
    public int updateCertConfigInfo(CertConfigInfo certConfigInfo) {
//        List<CertConfigInfo> certConfigInfos = certConfigInfoMapper.selectCertConfigInfoByName(certConfigInfo.getCertConfigName());
//        if(CollectionUtils.isNotEmpty(certConfigInfos)){
//            throw new GlobalException("证书配置名称已存在");
//        }
        certConfigInfo.setUpdateTime(DateUtils.getNowDate());
        if("1".equals(certConfigInfo.getCertPeriodType())){
            certConfigInfo.setCertPeriodTime(null);
        }
        UserCertificate userCertificate = new UserCertificate();
        userCertificate.setCertConfigId(certConfigInfo.getCertConfigId());
        userCertificate.setCertUrl(certConfigInfo.getCertLinkUrl());
        userCertificateMapper.updateUserCertificateByCertConfigId(userCertificate);
        return certConfigInfoMapper.updateCertConfigInfo(certConfigInfo);
    }

    /**
     * 删除证书配置信息
     *
     * @param certConfigId 证书配置信息主键
     * @return 结果
     */
    @Override
    public int deleteCertConfigInfoById(Long certConfigId) {
        return certConfigInfoMapper.deleteCertConfigInfoById(certConfigId);
    }

    /**
     * 批量删除证书配置信息
     *
     * @param certConfigIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCertConfigInfoByIds(Long[] certConfigIds) {
        return certConfigInfoMapper.deleteCertConfigInfoByIds(certConfigIds);
    }
}