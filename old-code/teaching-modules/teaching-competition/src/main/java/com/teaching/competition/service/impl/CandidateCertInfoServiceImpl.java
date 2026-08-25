package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.CandidateCertInfo;
import com.teaching.competition.domain.CandidateCertInfoImport;
import com.teaching.competition.mapper.CandidateCertInfoMapper;
import com.teaching.competition.mapper.CertConfigInfoMapper;
import com.teaching.competition.mapper.CertPlayerInfoMapper;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.service.ICandidateCertInfoService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.CertConfigInfo;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.SysUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 候选人证书Service业务层处理
 *
 * @author teaching
 */
@Service
public class CandidateCertInfoServiceImpl implements ICandidateCertInfoService {
    @Autowired
    private CandidateCertInfoMapper candidateCertInfoMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    /**
     * 查询候选人证书
     *
     * @param candidateId 候选人证书主键
     * @return 候选人证书
     */
    @Override
    public CandidateCertInfo selectCandidateCertInfoById(Long candidateId) {
        return candidateCertInfoMapper.selectCandidateCertInfoById(candidateId, null).get(0);
    }

    /**
     * 查询候选人证书列表
     *
     * @param candidateCertInfo 候选人证书
     * @return 候选人证书集合
     */
    @Override
    public List<CandidateCertInfo> selectCandidateCertInfoList(CandidateCertInfo candidateCertInfo) {
        List<CandidateCertInfo> candidateCertInfos = candidateCertInfoMapper.selectCandidateCertInfoList(candidateCertInfo);
        if(CollectionUtils.isNotEmpty(candidateCertInfos)){
            candidateCertInfos.stream().forEach(candidateCertInfoRes -> {
                StringBuffer sourceData = new StringBuffer();
                if(StringUtils.isNotEmpty(candidateCertInfoRes.getCompetitionName())){
                    sourceData.append(candidateCertInfoRes.getCompetitionName());
                }
                if(StringUtils.isNotEmpty(candidateCertInfoRes.getCompetitionTrackName())){
                    sourceData.append(candidateCertInfoRes.getCompetitionTrackName());
                }
                if(StringUtils.isNotEmpty(candidateCertInfoRes.getSecondLevelName())){
                    sourceData.append(candidateCertInfoRes.getSecondLevelName());
                }
                candidateCertInfoRes.setSourceData(sourceData.toString());
                if(Objects.nonNull(candidateCertInfoRes.getLeaderTeacherId())){
                    R<SysUser> sysUserR = userService.getUserCenterInfo(candidateCertInfoRes.getLeaderTeacherId(), SecurityConstants.INNER);
                    if(R.isSuccess(sysUserR) && Objects.nonNull(sysUserR.getData())){
                        AuthInfo authInfo = sysUserR.getData().getAuthInfo();
                        if(authInfo != null){
                            candidateCertInfoRes.setLeaderTeacherName(authInfo.getRealName());
                        }
                    }
                }
            });
        }
        return candidateCertInfos;
    }

    /**
     * 新增候选人证书
     *
     * @param candidateCertInfo 候选人证书
     * @return 结果
     */
    @Override
    public int insertCandidateCertInfo(CandidateCertInfo candidateCertInfo) {
        // 获取删除候选人
        if("1".equals(candidateCertInfo.getDelFlag())){
            if(Objects.nonNull(candidateCertInfo.getCandidateId())){
                return candidateCertInfoMapper.deleteCandidateCertInfoById(candidateCertInfo.getCandidateId());
            }
            if(Objects.nonNull(candidateCertInfo.getMemberId())) {
                Long[] memberIds = Arrays.asList(candidateCertInfo.getMemberId()).toArray(Long[]::new);
                return candidateCertInfoMapper.deleteCandidateCertInfoByMemberId(memberIds);
            }
        } else {
            // 新增候选人信息
            // 防重复
            Map<String,Object> params = new HashMap<>();
            params.put("certConfigId", candidateCertInfo.getCertConfigId());
            params.put("userName", candidateCertInfo.getUserName());
            params.put("idCard", candidateCertInfo.getIdCard());
            List<CandidateCertInfo> certInfo = candidateCertInfoMapper.selectCandidateCertInfoListByUser(params);
            if(CollectionUtils.isEmpty(certInfo)){
                candidateCertInfo.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                candidateCertInfo.setSourceType(ApplyConstants.SOURCE_TYPE_COMPETITION);
                candidateCertInfo.setPlayerSources(ApplyConstants.PLAYER_SOURCE_ADD);
                return candidateCertInfoMapper.insertCandidateCertInfo(candidateCertInfo);
            }
        }
        return 1;
    }

    /**
     * 批量新增候选人证书
     *
     * @param candidateCertInfoList 候选人证书列表
     * @return 结果
     */
    @Override
    public int batchInsertCandidateCertInfo(List<CandidateCertInfo> candidateCertInfoList,Long certConfigId) {
        if(CollectionUtils.isNotEmpty(candidateCertInfoList)){
            List<CandidateCertInfo> candidateCertInfoListOld = candidateCertInfoMapper.selectCandidateCertInfoById(certConfigId, null);
            // 提取新旧列表的memberId集合
            Set<Long> oldMemberIds = CollectionUtils.isNotEmpty(candidateCertInfoListOld)
                    ? candidateCertInfoListOld.stream()
                    .map(CandidateCertInfo::getMemberId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    : Collections.emptySet();
            Set<Long> newMemberIds = candidateCertInfoList.stream()
                    .map(CandidateCertInfo::getMemberId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // 需要删除的：在旧列表中存在，但在新列表中不存在的memberId
            Set<Long> deleteMemberIds = CollectionUtils.isNotEmpty(oldMemberIds)
                    ? oldMemberIds.stream()
                    .filter(memberId -> !newMemberIds.contains(memberId))
                    .collect(Collectors.toSet())
                    : Collections.emptySet();
            // 执行删除操作
            if(CollectionUtils.isNotEmpty(deleteMemberIds)){
                Long[] deleteIds = deleteMemberIds.toArray(Long[]::new);
                candidateCertInfoMapper.deleteCandidateCertInfoByMemberId(deleteIds);
            }
            // 再新增候选人信息
            // 需要新增的：在新列表中存在，但在旧列表中不存在的memberId
            Set<Long> addMemberIds = newMemberIds.stream()
                    .filter(memberId -> !oldMemberIds.contains(memberId))
                    .collect(Collectors.toSet());
            // 防重复，准备新增数据
            List<CandidateCertInfo> createCandidateCertInfoList = new ArrayList<>();
            candidateCertInfoList.forEach(candidateCertInfo -> {
                if(addMemberIds.contains(candidateCertInfo.getMemberId())){
                    Map<String, Object> params = new HashMap<>();
                    params.put("certConfigId", certConfigId);
                    params.put("userName", candidateCertInfo.getUserName());
                    params.put("idCard", candidateCertInfo.getIdCard());
                    params.put("memberId", candidateCertInfo.getMemberId());
                    List<CandidateCertInfo> certInfo = candidateCertInfoMapper.selectCandidateCertInfoListByUser(params);
                    if (CollectionUtils.isEmpty(certInfo)) {
                        candidateCertInfo.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                        candidateCertInfo.setSourceType(ApplyConstants.SOURCE_TYPE_COMPETITION);
                        candidateCertInfo.setPlayerSources(ApplyConstants.PLAYER_SOURCE_ADD);
                        candidateCertInfo.setCertConfigId(certConfigId);
                        createCandidateCertInfoList.add(candidateCertInfo);
                    }
                }
            });
            if(CollectionUtils.isNotEmpty(createCandidateCertInfoList)){
                candidateCertInfoMapper.batchInsertCandidateCertInfo(createCandidateCertInfoList);
            }
//            // 获取删除候选人
//            List<CandidateCertInfo> deleteCandidateCertInfoList = candidateCertInfoList.stream()
//                    .filter(candidateCertInfo -> "1".equals(candidateCertInfo.getDelFlag())).toList();
//            if(CollectionUtils.isNotEmpty(deleteCandidateCertInfoList)){
//                Long[] candidateIdIds = deleteCandidateCertInfoList.stream()
//                        .filter(candidateCertInfo -> Objects.nonNull(candidateCertInfo.getCandidateId()))
//                        .map(CandidateCertInfo::getCandidateId).toArray(Long[]::new);
//                candidateCertInfoMapper.deleteCandidateCertInfoByIds(candidateIdIds);
//            }
//            // 新增候选人信息
//            List<CandidateCertInfo> addCandidateCertInfoList = candidateCertInfoList.stream()
//                    .filter(candidateCertInfo -> !"1".equals(candidateCertInfo.getDelFlag())).toList();
//
//            }
        } else {
            candidateCertInfoMapper.deleteCandidateCertInfoByCertConfigId(certConfigId);
        }
        return 1;
    }

    /**
     * 修改候选人证书
     *
     * @param candidateCertInfo 候选人证书
     * @return 结果
     */
    @Override
    public int updateCandidateCertInfo(CandidateCertInfo candidateCertInfo) {
        return candidateCertInfoMapper.updateCandidateCertInfo(candidateCertInfo);
    }

    /**
     * 删除候选人证书
     *
     * @param candidateId 候选人证书主键
     * @return 结果
     */
    @Override
    public int deleteCandidateCertInfoById(Long candidateId) {
        return candidateCertInfoMapper.deleteCandidateCertInfoById(candidateId);
    }

    /**
     * 批量删除候选人证书
     *
     * @param candidateIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCandidateCertInfoByIds(Long[] candidateIds) {
        return candidateCertInfoMapper.deleteCandidateCertInfoByIds(candidateIds);
    }

    /**
     * 导入候选人信息
     *
     * @param candidateList 候选人信息列表
     * @param updateSupport 是否更新支持
     * @param operName 操作人
     * @return 结果
     */
    @Override
    public String importCandidateCertInfo(List<CandidateCertInfoImport> candidateList, boolean updateSupport,
                                          String operName,Long certConfigId) {
        if (CollectionUtils.isEmpty(candidateList)) {
            return "导入数据不能为空";
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (CandidateCertInfoImport candidateImport : candidateList) {
            try {
                if (candidateImport == null) {
                    failureNum++;
                    failureMsg.append(failureNum + "、导入失败：文件内容无法解析，请使用系统提供的导入模板");
                    continue;
                }
                if (StringUtils.isEmpty(candidateImport.getUserName()) || StringUtils.isEmpty(candidateImport.getIdCard())) {
                    failureNum++;
                    failureMsg.append(failureNum + "、导入失败：姓名或身份证号为空，请使用系统提供的导入模板");
                    continue;
                }
                // 获取证书配置
                CertConfigInfo certConfigInfo = certConfigInfoMapper.selectCertConfigInfoById(certConfigId);
                Map<String, Object> params = new HashMap<>();
                params.put("competitionSeriesId", certConfigInfo.getCompetitionSeriesId());
                params.put("competitionTrackId", certConfigInfo.getCompetitionTrackId());
                params.put("secondLevelCode", certConfigInfo.getSecondLevelCode());
                params.put("userName", candidateImport.getUserName().trim());
                params.put("idCard", candidateImport.getIdCard().trim());
                List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByIdCard(params);
                List<CandidateCertInfo> candidateCertInfoList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(applyInfoList)){
                    applyInfoList.forEach(applyInfo -> {
                        List<CandidateCertInfo> certInfo = candidateCertInfoMapper.selectCandidateCertInfoById(certConfigId, applyInfo.getMemberId());
                        if(CollectionUtils.isEmpty(certInfo)){
                            CandidateCertInfo candidateCertInfoEntity = new CandidateCertInfo();
                            BeanUtils.copyProperties(applyInfo, candidateCertInfoEntity);
                            candidateCertInfoEntity.setScore(candidateImport.getScore());
                            candidateCertInfoEntity.setPlayerSources(ApplyConstants.SOURCE_TYPE_COMPETITION);
                            candidateCertInfoEntity.setSourceType(ApplyConstants.SOURCE_TYPE_IMPORT);
                            candidateCertInfoEntity.setCreateBy(operName);
                            candidateCertInfoEntity.setCreateTime(DateUtils.getNowDate());
                            candidateCertInfoEntity.setCertConfigId(certConfigId);
                            candidateCertInfoList.add(candidateCertInfoEntity);
                        }
                    });
                    if(CollectionUtils.isNotEmpty(candidateCertInfoList)){
                        candidateCertInfoMapper.batchInsertCandidateCertInfo(candidateCertInfoList);
                    }
                } else {
                    List<CandidateCertInfo> certInfo = candidateCertInfoMapper.selectCandidateCertInfoListByUser(Map.of("certConfigId", certConfigId,
                            "userName", candidateImport.getUserName(), "idCard", candidateImport.getIdCard()));
                    if(CollectionUtils.isEmpty(certInfo)){
                        // 查实名认证信息，查到了放user_id
                        AuthInfo authInfo = new AuthInfo();
                        authInfo.setRealName(candidateImport.getUserName().trim());
                        authInfo.setIdCard(candidateImport.getIdCard().trim());
                        R<List<AuthInfo>> authInfoByIdCardR = userService.selectAuthInfoByIdCard(authInfo, SecurityConstants.INNER);
                        CandidateCertInfo candidateCertInfoEntity = new CandidateCertInfo();
                        BeanUtils.copyProperties(candidateImport, candidateCertInfoEntity);
                        candidateCertInfoEntity.setPlayerSources(ApplyConstants.SOURCE_TYPE_COMPETITION);
                        candidateCertInfoEntity.setSourceType(ApplyConstants.PLAYER_SOURCE_IMPORT);
                        if(R.isSuccess(authInfoByIdCardR) && CollectionUtils.isNotEmpty(authInfoByIdCardR.getData())){
                            candidateCertInfoEntity.setUserId(authInfoByIdCardR.getData().get(0).getUserId());
                        }
                        candidateCertInfoEntity.setCreateBy(operName);
                        candidateCertInfoEntity.setCreateTime(DateUtils.getNowDate());
                        candidateCertInfoEntity.setCertConfigId(certConfigId);
                        candidateCertInfoEntity.setCompetitionSeriesId(certConfigInfo.getCompetitionSeriesId());
                        candidateCertInfoEntity.setCompetitionTrackId(certConfigInfo.getCompetitionTrackId());
                        candidateCertInfoEntity.setSecondLevelCode(certConfigInfo.getSecondLevelCode());
                        candidateCertInfoMapper.insertCandidateCertInfo(candidateCertInfoEntity);
                        successNum++;
                    } else {
                        failureNum++;
                        String msg = failureNum + "、参赛者 " + candidateImport.getUserName() + " 导入失败,信息已存在";
                        failureMsg.append(msg);
                    }
                }
            } catch (Exception e) {
                failureNum++;
                String msg = failureNum + "、参赛者 " + candidateImport.getUserName() + " 导入失败：" + e.getMessage();
                failureMsg.append(msg);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据导入成功");
        }
        return successMsg.toString();
    }

    @Override
    public int insertCandidateCertInfoFromAwards(CandidateCertInfo candidateCertInfo) {
        if(Objects.isNull(candidateCertInfo.getCertConfigId())){
            return 1;
        }
        Map<String, Object> param = new HashMap<>();
        // 获取证书配置
        CertConfigInfo certConfigInfo = certConfigInfoMapper.selectCertConfigInfoById(candidateCertInfo.getCertConfigId());
        param.put("competitionSeriesId", certConfigInfo.getCompetitionSeriesId());
        param.put("competitionTrackId", certConfigInfo.getCompetitionTrackId());
        param.put("secondLevelCode", certConfigInfo.getSecondLevelCode());
        param.put("awardsName", certConfigInfo.getAwardsName());
        List<CompetitionApplyInfo> applyInfoList =
                competitionApplyInfoMapper.selectCandidateCertInfoListFromAwards(param);
        List<CandidateCertInfo> candidateCertInfoList = new ArrayList<>();
        CandidateCertInfo candidateCertInfoReq = new CandidateCertInfo();
        candidateCertInfoReq.setCompetitionSeriesId(certConfigInfo.getCompetitionSeriesId());
        candidateCertInfoReq.setCompetitionTrackId(certConfigInfo.getCompetitionTrackId());
        candidateCertInfoReq.setSecondLevelCode(certConfigInfo.getSecondLevelCode());
        candidateCertInfoReq.setSourceType(ApplyConstants.SOURCE_TYPE_AWARDS);
        List<CandidateCertInfo> candidateCertInfos =
                candidateCertInfoMapper.selectCandidateCertInfoList(candidateCertInfoReq);
        List<String> exitsTeamCode = candidateCertInfos.stream().map(CandidateCertInfo::getTeamCode).distinct().toList();
        if(CollectionUtils.isNotEmpty(applyInfoList)){
            applyInfoList.stream().forEach(applyInfo -> {
                if (!exitsTeamCode.contains(applyInfo.getTeamCode())) {
                    CandidateCertInfo candidateCertInfoEntity = new CandidateCertInfo();
                    BeanUtils.copyProperties(applyInfo, candidateCertInfoEntity);
                    candidateCertInfoEntity.setCertConfigId(certConfigInfo.getCertConfigId());
                    candidateCertInfoEntity.setPlayerSources(ApplyConstants.SOURCE_TYPE_COMPETITION);
                    candidateCertInfoEntity.setSourceType(ApplyConstants.SOURCE_TYPE_AWARDS);
                    candidateCertInfoList.add(candidateCertInfoEntity);
                }
            });
        }
        if(CollectionUtils.isNotEmpty(candidateCertInfoList)){
            candidateCertInfoMapper.batchInsertCandidateCertInfo(candidateCertInfoList);
        }
        return 1;
    }
}
