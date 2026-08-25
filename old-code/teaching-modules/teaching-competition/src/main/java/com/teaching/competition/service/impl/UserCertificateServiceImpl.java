package com.teaching.competition.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.core.web.page.TableSupport;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.CompetitionCertificateQueryRequest;
import com.teaching.competition.mapper.*;
import com.teaching.system.api.domain.*;
import com.teaching.competition.domain.CertPlayerInfo;
import com.teaching.competition.service.IUserCertificateService;
import com.teaching.system.api.RemoteUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;

/**
 * 用户证书Service业务层处理
 *
 * @author teaching
 */
@Service
public class UserCertificateServiceImpl implements IUserCertificateService {
    private static final Logger log = LoggerFactory.getLogger(UserCertificateServiceImpl.class);
    private static final String QUERY_TYPE_PERSON = "PERSON";
    private static final String QUERY_TYPE_ORGANIZATION = "ORGANIZATION";
    private static final String QUERY_TYPE_CERT_CODE = "CERT_CODE";
    @Autowired
    private UserCertificateMapper userCertificateMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private CertPlayerInfoMapper certPlayerInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    @Autowired
    private CandidateCertInfoMapper candidateCertInfoMapper;

    @Autowired
    private UserCertificateOriginMapper userCertificateOriginMapper;

    /**
     * 查询用户证书
     *
     * @param certId 用户证书主键
     * @return 用户证书
     */
    @Override
    public UserCertificate selectUserCertificateById(UserCertificate userCertificateReq) {
       UserCertificate userCertificate = userCertificateMapper.selectUserCertificateById(userCertificateReq);
        if(Objects.nonNull(userCertificate)){
            // 获取学校名称
            if (StringUtils.isNotEmpty(userCertificate.getSchoolId()) && StringUtils.isNotEmpty(userCertificate.getSchoolName())) {
                R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(userCertificate.getSchoolId(), SecurityConstants.INNER);
                if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                    NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                    userCertificate.setSchoolName(collegeInfo.getSchoolName());
                }
            }
        }
        return userCertificate;
    }

    /**
     * 查询用户证书列表
     *
     * @param userCertificate 用户证书
     * @return 用户证书集合
     */
    @Override
    public List<UserCertificate> selectUserCertificateList(UserCertificate userCertificate) {
        List<UserCertificate> userCertificates = userCertificateMapper.selectUserCertificateList(userCertificate);
        fillSchoolNames(userCertificates);
        return userCertificates;
    }

    @Override
    public List<UserCertificate> selectCompetitionCertificateList(
            CompetitionCertificateQueryRequest queryRequest) {
        validateCompetitionCertificateQuery(queryRequest);
        return userCertificateMapper.selectCompetitionCertificateList(queryRequest);
    }

    private void validateCompetitionCertificateQuery(CompetitionCertificateQueryRequest queryRequest) {
        if (queryRequest == null || StringUtils.isBlank(queryRequest.getQueryType())) {
            throw new GlobalException("请选择证书查询方式");
        }
        queryRequest.setQueryType(queryRequest.getQueryType().trim().toUpperCase(Locale.ROOT));
        queryRequest.setSchoolName(StringUtils.trim(queryRequest.getSchoolName()));
        queryRequest.setUserName(StringUtils.trim(queryRequest.getUserName()));
        queryRequest.setCertCode(StringUtils.trim(queryRequest.getCertCode()).toUpperCase(Locale.ROOT));

        switch (queryRequest.getQueryType()) {
            case QUERY_TYPE_PERSON -> {
                validatePublicQueryText(queryRequest.getSchoolName(), "学校", 2, 100);
                validatePublicQueryText(queryRequest.getUserName(), "获证人姓名", 2, 100);
            }
            case QUERY_TYPE_ORGANIZATION ->
                    validatePublicQueryText(queryRequest.getSchoolName(), "单位名称", 2, 100);
            case QUERY_TYPE_CERT_CODE ->
                    validatePublicQueryText(queryRequest.getCertCode(), "证书编号", 6, 255);
            default -> throw new GlobalException("证书查询方式不正确");
        }
    }

    private void validatePublicQueryText(String value, String fieldName, int minLength, int maxLength) {
        if (StringUtils.isBlank(value)) {
            throw new GlobalException("请输入" + fieldName);
        }
        if (value.length() < minLength || value.length() > maxLength) {
            throw new GlobalException(fieldName + "长度应为" + minLength + "-" + maxLength + "个字符");
        }
        if (value.indexOf('%') >= 0 || value.indexOf('_') >= 0) {
            throw new GlobalException(fieldName + "不能包含%或_");
        }
    }

    private void fillSchoolNames(List<UserCertificate> userCertificates) {
        if(CollectionUtils.isNotEmpty(userCertificates)){
            userCertificates.stream().forEach(userCertificateRes -> {
                // 获取学校名称
                if (StringUtils.isEmpty(userCertificateRes.getSchoolId()) && Objects.nonNull(userCertificateRes.getUserId())) {
                    R<SysUser> userInfoById = userService.getUserInfoById(userCertificateRes.getUserId(), SecurityConstants.INNER);
                    if(R.isSuccess(userInfoById) && Objects.nonNull(userInfoById.getData())){
                        userCertificateRes.setSchoolId(userInfoById.getData().getSchool());
                    }
                }
                if (StringUtils.isNotEmpty(userCertificateRes.getSchoolId())) {
                    R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(userCertificateRes.getSchoolId(), SecurityConstants.INNER);
                    if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                        NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                        userCertificateRes.setSchoolName(collegeInfo.getSchoolName());
                    }
                }
            });
//            if(StringUtils.isNotBlank(userCertificate.getPlayer())){
//                //过滤userCertificates中player包含传入值的记录
//                userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                        StringUtils.containsIgnoreCase(userCertificateRes.getPlayer(), userCertificate.getPlayer().trim())).collect(Collectors.toList());
//            }
//            if(StringUtils.isNotBlank(userCertificate.getGuideTeacher())){
//                userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                        StringUtils.containsIgnoreCase(userCertificateRes.getGuideTeacher(), userCertificate.getGuideTeacher().trim())).collect(Collectors.toList());
//            }
//            if(StringUtils.isNotBlank(userCertificate.getSchoolName())){
//                userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                        StringUtils.containsIgnoreCase(userCertificateRes.getSchoolName(), userCertificate.getSchoolName().trim())).collect(Collectors.toList());
//            }
        }
    }

    @Override
    public TableDataInfo getUserCertificateList(UserCertificate userCertificate) {
        TableDataInfo rspData = new TableDataInfo();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        List<UserCertificate> userCertificates = userCertificateMapper.selectUserCertificateList(userCertificate);
        if(CollectionUtils.isEmpty(userCertificates)){
            rspData.setCode(HttpStatus.SUCCESS);
            rspData.setRows(null);
            rspData.setMsg("查询成功");
            rspData.setTotal(0);
            return rspData;
        }
        userCertificates.stream().forEach(userCertificateRes -> {
            // 翻译赛事信息
            if(StringUtils.isNotEmpty(userCertificateRes.getTeamCode())){
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("teamCode",userCertificateRes.getTeamCode());
                params.put("competitionSeriesId",userCertificateRes.getCompetitionSeriesId());
                params.put("competitionTrackId",userCertificateRes.getCompetitionTrackId());
                params.put("secondLevelCode",userCertificateRes.getSecondLevelCode());
                List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCode(params);
                if (CollectionUtils.isNotEmpty(applyInfoList)) {
                    userCertificateRes.setTeamName(applyInfoList.get(0).getTeamName());
                    // 整合选手及指导教师信息
                    userCertificateRes.setPlayer(StringUtils.join(applyInfoList.stream().
                            filter(competitionApplyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName()))
                            .map(CompetitionApplyInfo::getUserName).toList(), ","));
                    userCertificateRes.setGuideTeacher(StringUtils.join(applyInfoList.stream().
                            filter(competitionApplyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName())).map(CompetitionApplyInfo::getUserName).toList(), ","));
                } else {
                    userCertificateRes.setPlayer(userCertificateRes.getUserName());
                }
            } else {
                userCertificateRes.setPlayer(userCertificateRes.getUserName());
            }
            // 获取学校名称
            if (StringUtils.isEmpty(userCertificateRes.getSchoolId()) && Objects.nonNull(userCertificateRes.getUserId())) {
                R<SysUser> userInfoById = userService.getUserInfoById(userCertificateRes.getUserId(), SecurityConstants.INNER);
                if(R.isSuccess(userInfoById) && Objects.nonNull(userInfoById.getData())){
                    userCertificateRes.setSchoolId(userInfoById.getData().getSchool());
                }
            }
            if (StringUtils.isNotEmpty(userCertificateRes.getSchoolId())) {
                R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(userCertificateRes.getSchoolId(), SecurityConstants.INNER);
                if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                    NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                    userCertificateRes.setSchoolName(collegeInfo.getSchoolName());
                }
            }
        });
//        if(StringUtils.isNotBlank(userCertificate.getPlayer())){
//            //过滤userCertificates中player包含传入值的记录
//            userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                    StringUtils.containsIgnoreCase(userCertificateRes.getPlayer(), userCertificate.getPlayer().trim())).collect(Collectors.toList());
//        }
//        if(StringUtils.isNotBlank(userCertificate.getGuideTeacher())){
//            userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                    StringUtils.containsIgnoreCase(userCertificateRes.getGuideTeacher(), userCertificate.getGuideTeacher().trim())).collect(Collectors.toList());
//        }
//        if(StringUtils.isNotBlank(userCertificate.getSchoolName())){
//            userCertificates = userCertificates.stream().filter(userCertificateRes ->
//                    StringUtils.containsIgnoreCase(userCertificateRes.getSchoolName(), userCertificate.getSchoolName().trim())).collect(Collectors.toList());
//        }
        List<UserCertificate> paginate = PageUtils.paginate(userCertificates, pageNum, pageSize);
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(paginate);
        rspData.setMsg("查询成功");
        rspData.setTotal(new com.github.pagehelper.PageInfo(userCertificates).getTotal());
        return rspData;
    }

    /**
     * 新增用户证书
     *
     * @param userCertificate 用户证书
     * @return 结果
     */
    @Override
    public int insertUserCertificate(UserCertificate userCertificate) {
        userCertificate.setCreateTime(DateUtils.getNowDate());
        userCertificate.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        // 生成证书名称
        if(StringUtils.isEmpty(userCertificate.getCertName())){
            userCertificate.setCertName(userCertificate.getCompetitionName() +"大赛中，荣获"+
                    userCertificate.getCompetitionTrackName() + userCertificate.getSecondLevelName()  + userCertificate.getAwardsName()+"，特此表彰！");
        }
        return userCertificateMapper.insertUserCertificate(userCertificate);
    }

    /**
     * 批量新增用户证书
     *
     * @param userCertificateList 用户证书列表
     * @return 结果
     */
    @Override
    public int batchInsertUserCertificate(List<UserCertificate> userCertificateList,Date issuanceDate) {
        List<UserCertificate> userCertificateListAdd = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(userCertificateList)){
            userCertificateList.stream().forEach(userCertificate -> {
                userCertificate.setIssuanceDate(issuanceDate);
                String year = null;
                if (issuanceDate != null) {
                    year = String.valueOf(issuanceDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .getYear());
                }
                userCertificate.setYear(year);
                // 过滤掉已经发过证书得候选人
//                List<UserCertificate> userCertificateListRes = userCertificateMapper.selectUserCertificateByIdCard(userCertificate.getCertConfigId(),userCertificate.getIdCard());
//                if(CollectionUtils.isEmpty(userCertificateListRes)){
                    // 证书有效期,
                    CertConfigInfo certConfigInfo = certConfigInfoMapper.selectCertConfigInfoById(userCertificate.getCertConfigId());
                    if(Objects.nonNull(certConfigInfo)){
                        userCertificate.setCertPeriod(certConfigInfo.getCertPeriodTime());
                        userCertificate.setAcquireWay(ApplyConstants.CERT_ACQUIRE_WAY);
                        userCertificate.setAwardsName(certConfigInfo.getAwardsName());
                        userCertificate.setCertUrl(certConfigInfo.getCertLinkUrl());
                        // 奖项名称翻译
                        R<List<SysDictData>> awardsNameList = userService.dictType("awards_name", SecurityConstants.INNER);
                        if(R.isSuccess(awardsNameList) && CollectionUtils.isNotEmpty(awardsNameList.getData())){
                            String awardsName = awardsNameList.getData().stream().filter(sysDictData -> sysDictData.getDictValue().equals(certConfigInfo.getAwardsName()))
                                    .findFirst().get().getDictLabel();
                            // 生成证书名称
                            if(StringUtils.isEmpty(userCertificate.getCertName())){
                                StringBuffer sb = new StringBuffer();
                                if(StringUtils.isNotEmpty(certConfigInfo.getCompetitionName())){
                                    sb.append(certConfigInfo.getCompetitionName()).append("大赛中,");
                                }
                                if(StringUtils.isNotEmpty(certConfigInfo.getCompetitionTrackName())){
                                    sb.append("荣获"+certConfigInfo.getCompetitionTrackName());
                                }
                                if(StringUtils.isNotEmpty(certConfigInfo.getSecondLevelName())){
                                    sb.append(certConfigInfo.getSecondLevelName());
                                }
                                if(StringUtils.isNotEmpty(sb)){
                                    userCertificate.setCertName(sb.toString()+"荣获"+awardsName+",特此表彰!");
                                } else {
                                    userCertificate.setCertName("荣获"+awardsName+",特此表彰!");
                                }
                            }
                        }
                    } else {
                        throw new GlobalException("证书配置信息不存在");
                    }
                    userCertificate.setCreateTime(DateUtils.getNowDate());
                    userCertificate.setCreateBy(SecurityUtils.getLoginUser().getUsername());
//                    userCertificate.setIssuanceDate(DateUtils.getNowDate());
                    userCertificate.setCertStatus(ApplyConstants.CERT_STATUS_EFFECTIVE);
                    userCertificate.setCompetitionStageId(certConfigInfo.getCompetitionStageId());
                    userCertificate.setCompetitionSeriesId(certConfigInfo.getCompetitionSeriesId());
                    userCertificate.setCompetitionTrackId(certConfigInfo.getCompetitionTrackId());
                    userCertificate.setSecondLevelCode(certConfigInfo.getSecondLevelCode());
                    userCertificate.setOrgCode(certConfigInfo.getOrgCode());
                // 根据赛事信息获取报名信息进行整合入库
                if(StringUtils.isNotEmpty(userCertificate.getTeamCode())){
                    Map<String,Object> params = new HashMap<String,Object>();
                    params.put("teamCode",userCertificate.getTeamCode());
                    params.put("competitionSeriesId",userCertificate.getCompetitionSeriesId());
                    params.put("competitionTrackId",userCertificate.getCompetitionTrackId());
                    params.put("secondLevelCode",userCertificate.getSecondLevelCode());
                    List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCode(params);
                    if (CollectionUtils.isNotEmpty(applyInfoList)) {
                        userCertificate.setTeamName(applyInfoList.get(0).getTeamName());
                        // 整合选手及指导教师信息
                        userCertificate.setPlayer(StringUtils.join(applyInfoList.stream().
                                filter(competitionApplyInfo -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName()))
                                .map(CompetitionApplyInfo::getUserName).toList(), ","));
                        userCertificate.setGuideTeacher(StringUtils.join(applyInfoList.stream().
                                filter(competitionApplyInfo -> ApplyConstants.TEAM_GUIDE_TEACHER.equals(competitionApplyInfo.getCompetitionRoleName())).map(CompetitionApplyInfo::getUserName).toList(), ","));
                    }else {
                        userCertificate.setPlayer(userCertificate.getUserName());
                    }
                } else {
                    userCertificate.setPlayer(userCertificate.getUserName());
                }
                    userCertificateListAdd.add(userCertificate);
//                } else {
//                    log.info(userCertificate.getUserName()+"用户证书已存在,证书名称为:"+userCertificateListRes.get(0).getCertName());
//                }
            });
            if(CollectionUtils.isNotEmpty(userCertificateListAdd)){
                userCertificateMapper.batchInsertUserCertificate(userCertificateListAdd);
            }
            // 获取报名人员信息
            List<CertPlayerInfo> certPlayerInfoList = new ArrayList<>();
            if(StringUtils.isNotEmpty(userCertificateListAdd)){
                userCertificateListAdd.stream().forEach(userCertificateRes -> {
//                    Map<String,Object> params = new HashMap<String,Object>();
//                    params.put("teamCode",userCertificateRes.getTeamCode());
//                    List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByUserTeamCode(params);
//                    if(CollectionUtils.isNotEmpty(applyInfoList)){
//                        applyInfoList.stream().forEach(applyInfo -> {
//                            CertPlayerInfo certPlayerInfo = new CertPlayerInfo();
//                            certPlayerInfo.setCertId(userCertificateRes.getCertId());
//                            certPlayerInfo.setUserId(applyInfo.getUserId());
//                            certPlayerInfo.setMemberId(applyInfo.getMemberId());
//                            certPlayerInfo.setTeamCode(applyInfo.getTeamCode());
//                            certPlayerInfoList.add(certPlayerInfo);
//                        });
//                    }
                    CertPlayerInfo certPlayerInfo = new CertPlayerInfo();
                    certPlayerInfo.setCertId(userCertificateRes.getCertConfigId());
                    certPlayerInfo.setUserId(userCertificateRes.getUserId());
                    // 根据证书配置的赛事赛道组别匹配候选人的赛事赛道组别是否完全一样，不一样则不记录teamcode,证书颁发只显示自己名称
                    certPlayerInfo.setMemberId(userCertificateRes.getMemberId());
                    certPlayerInfo.setTeamCode(userCertificateRes.getTeamCode());
                    certPlayerInfoList.add(certPlayerInfo);
                });
            }
            if(CollectionUtils.isNotEmpty(certPlayerInfoList)){
                certPlayerInfoMapper.batchInsertCertPlayerInfo(certPlayerInfoList);
            }
            // 发过证书后删除候选人
            List<Long> memberIdList = userCertificateList.stream()
                    .filter(userCertificate -> Objects.nonNull(userCertificate.getMemberId()))
                    .map(UserCertificate::getMemberId).toList();
            if(CollectionUtils.isNotEmpty(memberIdList)){
                // 超过200分批删除
                List<List<Long>> partition = Lists.partition(memberIdList, 200);
                for (List<Long> memberIds : partition){
                    Long[] memberId = memberIds.toArray(Long[]::new);
                    candidateCertInfoMapper.deleteCandidateCertInfoByMemberId(memberId);
                }
                // deleteCandidateCertInfoByCartId
            }
            List<String> idCardList = userCertificateList.stream()
                    .filter(userCertificate -> Objects.isNull(userCertificate.getMemberId())  && StringUtils.isNotEmpty(userCertificate.getIdCard()))
                    .map(UserCertificate::getIdCard).toList();
            if(CollectionUtils.isNotEmpty(idCardList)){
                // 超过200分批删除
                List<List<String>> partition = Lists.partition(idCardList, 200);
                for (List<String> idCardIds : partition){
                    candidateCertInfoMapper.deleteCandidateCertInfoByCartId(idCardIds);
                }
            }
        }
        return 1;
    }

    /**
     * 修改用户证书
     *
     * @param userCertificate 用户证书
     * @return 结果
     */
    @Override
    public int updateUserCertificate(UserCertificate userCertificate) {
        if(Objects.nonNull(userCertificate.getIssuanceDate())){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(userCertificate.getIssuanceDate());
            int year = calendar.get(Calendar.YEAR);
            userCertificate.setYear(String.valueOf(year));
        }
        UserCertificateOrigin userCertificateOrigin = new UserCertificateOrigin();
        BeanUtils.copyProperties(userCertificate,userCertificateOrigin);
        userCertificateOriginMapper.updateUserCertificateOrigin(userCertificateOrigin);
        userCertificateMapper.updateUserCertificate(userCertificate);
        return 1;
    }

    /**
     * 删除用户证书
     *
     * @param certId 用户证书主键
     * @return 结果
     */
    @Override
    public int deleteUserCertificateById(UserCertificate userCertificate) {
        UserCertificateOrigin userCertificateOrigin = new UserCertificateOrigin();
        BeanUtils.copyProperties(userCertificate,userCertificateOrigin);
        userCertificateOriginMapper.deleteUserCertificateOriginById(userCertificateOrigin);
        userCertificateMapper.deleteUserCertificateById(userCertificate);
        return 1;
    }

    /**
     * 批量删除用户证书
     *
     * @param certIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteUserCertificateByIds(Long[] certIds) {
        return userCertificateMapper.deleteUserCertificateByIds(certIds);
    }
}
