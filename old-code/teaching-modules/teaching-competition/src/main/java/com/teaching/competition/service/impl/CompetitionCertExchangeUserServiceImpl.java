package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisLock;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.*;
import com.teaching.competition.mapper.*;
import com.teaching.competition.service.ICompetitionCertExchangeRuleService;
import com.teaching.competition.service.ICompetitionCertExchangeUserService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CompetitionCertExchangeUserServiceImpl implements ICompetitionCertExchangeUserService {

    private static final Logger log = LoggerFactory.getLogger(CompetitionCertExchangeUserServiceImpl.class);
    @Autowired
    private CompetitionCertExchangeRuleMapper competitionCertExchangeRuleMapper;

    @Autowired
    private UserCertificateOriginMapper userCertificateOriginMapper;

    @Autowired
    private UserCertificateMapper userCertificateMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CertExchangeRuleDetailMapper certExchangeRuleDetailMapper;

    @Autowired
    private CompetitionCertExchangeApplyMapper competitionCertExchangeApplyMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CertConfigInfoMapper certConfigInfoMapper;

    @Autowired
    private CertPlayerInfoMapper certPlayerInfoMapper;

    @Autowired
    private ICompetitionCertExchangeRuleService competitionCertExchangeRuleService;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private RedisLock redisLock;

    @Override
    public CompetitionCertExchangeRuleUser queryUserCertExchangeApplyDetail(Long rulerId) throws Exception{
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        CompetitionCertExchangeRuleUser competitionCertExchangeRuleUser = new CompetitionCertExchangeRuleUser();
        CompetitionCertExchangeRule competitionCertExchangeRule = competitionCertExchangeRuleService.selectCompetitionCertExchangeRuleById(rulerId);
        if(Objects.nonNull(competitionCertExchangeRule)){
            BeanUtils.copyProperties(competitionCertExchangeRule, competitionCertExchangeRuleUser);
        }
        if(Objects.nonNull(competitionCertExchangeRule)){
            // 如果是或，则判断源证书是否拥有
//            if(ApplyConstants.OR_CONDITIONS.equals(competitionCertExchangeRuleUser.getCertConditions())){
//                competitionCertExchangeRuleUser.getOriginCertList().stream().forEach(originCertConfigInfo -> {
//                    //规则配置源证书
//                    // 判断证书是否用户拥有，不拥有则此证书不可选择
//                    // 获取当前用户信息
//                    R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(sysUser.getUserId(), SecurityConstants.INNER);
//                    if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData())) {
//                        AuthInfo authInfo = userCenterInfoLogin.getData().getAuthInfo();
//                        if(Objects.nonNull(authInfo)){
//                            // 先证书配置id查询用户证书是否存在
//                            CertPlayerInfo certPlayerInfo = new CertPlayerInfo();
//                            certPlayerInfo.setUserId(sysUser.getUserId());
//                            certPlayerInfo.setCertId(originCertConfigInfo.getCertConfigId());
//                            List<CertPlayerInfo> certPlayerInfos = certPlayerInfoMapper.selectCertPlayerInfoList(certPlayerInfo);
//                            if (CollectionUtils.isNotEmpty(certPlayerInfos)) {
//                                originCertConfigInfo.setUserSelect(true);
//                            }
//                            // 源证书历史只能通过奖项获取源证书
//                            if(!originCertConfigInfo.getUserSelect()){
//                                UserCertificateOrigin userCertificateOrigin = new UserCertificateOrigin();
//                                userCertificateOrigin.setUserName(authInfo.getRealName());
//                                userCertificateOrigin.setCompetitionSeriesId(competitionCertExchangeRule.getCompetitionSeriesId());
//                                userCertificateOrigin.setCompetitionStageId(originCertConfigInfo.getCompetitionStageId());
//                                userCertificateOrigin.setAwardsName(originCertConfigInfo.getAwardsName());
//                                List<UserCertificateOrigin> userCertificateOrigins =
//                                        userCertificateOriginMapper.selectUserCertificateOriginByStageId(userCertificateOrigin);
//                                if(CollectionUtils.isNotEmpty(userCertificateOrigins)){
//                                    originCertConfigInfo.setUserSelect(true);
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//            if(ApplyConstants.AND_CONDITIONS.equals(competitionCertExchangeRuleUser.getCertConditions())){
                // 判断当前用户是否拥有规则配置所有源证书
                AtomicBoolean isHaveAll = new AtomicBoolean(true);
                competitionCertExchangeRuleUser.getOriginCertList().stream().forEach(originCert -> {
                    AtomicBoolean isHaveOne = new AtomicBoolean(true);
                    //规则配置源证书
                    // 判断证书是否用户拥有，不拥有则此证书不可选择
//                    CertPlayerInfo certPlayerInfo = new CertPlayerInfo();
//                    certPlayerInfo.setUserId(sysUser.getUserId());
//                    certPlayerInfo.setCertId(originCert.getCertConfigId());
//                    List<CertPlayerInfo> certPlayerInfos = certPlayerInfoMapper.selectCertPlayerInfoList(certPlayerInfo);
//                    if(CollectionUtils.isEmpty(certPlayerInfos)){
//                        isHaveAll.set(false);
//                        isHaveOne.set(false);
//                        originCert.setApplyStatus(ApplyConstants.CERT_STATUS_NONE);
//                        originCert.setApplyStatusDes("无此证书");
//                    }
                    // 获取当前用户信息
                    R<SysUser> userCenterInfoLogin = userService.getUserCenterInfo(sysUser.getUserId(), SecurityConstants.INNER);
                    if (R.isSuccess(userCenterInfoLogin) && Objects.nonNull(userCenterInfoLogin.getData())) {
                        AuthInfo authInfo = userCenterInfoLogin.getData().getAuthInfo();
                        if(Objects.nonNull(authInfo)){
                            UserCertificate userCertificate = new UserCertificate();
                            userCertificate.setUserName(authInfo.getRealName());
                            try {
                                userCertificate.setIdCard(RsaUtils.decryptByPrivateKey(authInfo.getIdCard()));
                            } catch (Exception e) {
                                throw new GlobalException(e.getMessage());
                            }
                            userCertificate.setCompetitionSeriesId(originCert.getCompetitionSeriesId());
                            userCertificate.setCompetitionStageId(originCert.getCompetitionStageId());
                            userCertificate.setAwardsName(originCert.getAwardsName());
                            userCertificate.setCompetitionTrackId(originCert.getCompetitionTrackId());
                            userCertificate.setSecondLevelCode(originCert.getSecondLevelCode());
                            log.info("用户证书配置参数："+JSONObject.toJSONString(userCertificate));
                            List<UserCertificate> userCertificateOrigins =
                                    userCertificateMapper.selectUserCertificateList(userCertificate);
                            log.info("用户证书配置结果："+JSONObject.toJSONString(userCertificateOrigins));
                            if(CollectionUtils.isEmpty(userCertificateOrigins)){
                                isHaveOne.set(false);
                                isHaveAll.set(false);
                                originCert.setApplyStatus(ApplyConstants.CERT_STATUS_NONE);
                                originCert.setApplyStatusDes("无此证书");
                            } else {
                                // 判断源证书是否存在不满一年，不满一年则不可兑换
                                // 获取源证书年份,用户颁发多个取最早时间证书
                                UserCertificate earliestCert = Collections.min(userCertificateOrigins,
                                        Comparator.comparing(UserCertificate::getIssuanceDate));
                                if(StringUtils.isEmpty(originCert.getApplyStatus())
                                        && (StringUtils.isNotEmpty(originCert.getOwnYear()))
                                        && !DateUtils.isAfterOneYear(earliestCert.getYear(),originCert.getOwnYear())){
                                    isHaveAll.set(false);
                                    isHaveOne.set(false);
                                    originCert.setYear(earliestCert.getYear());
                                    originCert.setApplyStatus(ApplyConstants.CERT_STATUS_APPLY_YEAR_NO_ONE);
                                    originCert.setApplyStatusDes("拥有年限不足"+originCert.getOwnYear()+"年");
                                } else {
                                    originCert.setYear(earliestCert.getYear());
                                }
                            }
                        }
                    }
                    if(isHaveOne.get()){
                        originCert.setApplyStatus(ApplyConstants.CERT_STATUS_APPLY_YES);
                        originCert.setApplyStatusDes("满足条件");
                    }
                });
                // 源证书都满足则目前证书可申请
                if(isHaveAll.get()){
                    competitionCertExchangeRuleUser.getTargetCertList().stream().forEach(targetCert -> {
                        targetCert.setApplyStatus(ApplyConstants.CERT_STATUS_APPLY);
                        targetCert.setApplyStatusDes("可兑换");
                    });
                } else {
                    competitionCertExchangeRuleUser.getTargetCertList().stream().forEach(targetCert -> {
                        targetCert.setApplyStatus(ApplyConstants.CERT_STATUS_APPLY_NO);
                        targetCert.setApplyStatusDes("不可兑换");
                    });
                }
//            }
        }
        return competitionCertExchangeRuleUser;
    }

    @Override
    public CompetitionCertExchangeRule queryUserCertExchangeApplyDetailNoAuth(Long rulerId) {
        return competitionCertExchangeRuleMapper.selectCompetitionCertExchangeRuleById(rulerId);
    }

    @Override
    public CompetitionCertExchangeRuleUserApply queryUserCertExchangeApply(CompetitionCertExchangeRuleUserApply apply) {
        // 获取规则详情
        if(CollectionUtils.isNotEmpty(apply.getOriginCertList()) && CollectionUtils.isNotEmpty(apply.getTargetCertList())){
            // 计算源证书分值
            // 获取源证书分值
            List<String> originCertCodeList = apply.getOriginCertList().stream()
                    .filter(originCert -> StringUtils.isNotEmpty(originCert.getOriginCertScore()))
                    .map(CertConfigInfo::getOriginCertScore).toList();
            List<String> targetCertCodeList = apply.getTargetCertList().stream()
                    .filter(targetCert -> StringUtils.isNotEmpty(targetCert.getTargetCertScore()))
                    .map(CertConfigInfo::getTargetCertScore).toList();
            BigDecimal originCertScoreSum = originCertCodeList.stream()
                    .filter(StringUtils::isNotEmpty)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal targetCertScoreSum = targetCertCodeList.stream()
                    .filter(StringUtils::isNotEmpty)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if(targetCertScoreSum.compareTo(originCertScoreSum)>0){
                BigDecimal certAmountSum = targetCertScoreSum.subtract(originCertScoreSum);
                apply.setCertAmountSum(certAmountSum);
                apply.getTargetCertList().stream().forEach(targetCert -> {
                    BigDecimal targetScore = new BigDecimal(targetCert.getTargetCertScore());
                    BigDecimal certAmountOne = targetScore.subtract(originCertScoreSum);
                    targetCert.setCertAmount(certAmountOne);
                });
            }else{
                apply.getTargetCertList().stream().forEach(targetCert -> {
                    targetCert.setCertAmount(BigDecimal.ZERO);
                });
                apply.setCertAmountSum(BigDecimal.ZERO);
            }
        }
        return apply;
    }

    @Override
    public AjaxResult saveUserCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply) {
        // 校验证书是否可真互换
        if(Objects.isNull(competitionCertExchangeApply.getRuleId())){
            throw new RuntimeException("赛证互通申请信息获取失败");
        }
        // 增加分布式锁
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String lockKey = "rulerIdLock:"+competitionCertExchangeApply.getRuleId()+":"+userId;
        RLock rLock = redisLock.getRLock(lockKey);
        try{
            if(rLock.tryLock(3, 30, TimeUnit.SECONDS)){
                // 检验相同证书互换不能重复下单
                R<String> orderStatusByUserIdAndCommodityId = orderService.getOrderStatusByUserIdAndCommodityId(SecurityUtils.getLoginUser().getUserid(), competitionCertExchangeApply.getRuleId().toString(), SecurityConstants.INNER);
                if(R.isSuccess(orderStatusByUserIdAndCommodityId) && StringUtils.isNotEmpty(orderStatusByUserIdAndCommodityId.getMsg())){
                    String payStatus = orderStatusByUserIdAndCommodityId.getMsg();
                    List<String> payStatusList = Arrays.asList(payStatus.split(","));
                    if(payStatusList.contains(DictConstant.PENDING)){
                        throw new GlobalException("证书兑换存在未支付订单，请先支付");
                    }
                    // 存在兑换相同得证书则校验
                    // 源证书id集合
                    if(payStatusList.contains(DictConstant.PAID)){
                        List<Long> originCertIdList = competitionCertExchangeApply.getOriginCertList().stream()
                                .filter(originCert -> Objects.nonNull(originCert.getCertConfigId()))
                                .map(CertConfigInfo::getCertConfigId).toList();
                        List<Long> targetCertIdList = competitionCertExchangeApply.getTargetCertList().stream()
                                .filter(originCert -> Objects.nonNull(originCert.getCertConfigId()))
                                .map(CertConfigInfo::getCertConfigId).toList();
                        if(CollectionUtils.isNotEmpty(originCertIdList) && CollectionUtils.isNotEmpty(targetCertIdList)){
                            CompetitionCertExchangeApply certExchangeApply = new CompetitionCertExchangeApply();
                            certExchangeApply.setRuleId(competitionCertExchangeApply.getRuleId());
                            List<CompetitionCertExchangeApply> competitionCertExchangeApplies =
                                    competitionCertExchangeApplyMapper.selectCompetitionCertExchangeApplyList(certExchangeApply);
                            if (CollectionUtils.isNotEmpty(competitionCertExchangeApplies)) {
                                String originCertIds = originCertIdList.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
                                String targetCertIds = targetCertIdList.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
                                competitionCertExchangeApplies.stream().forEach(apply -> {
                                    String existOriginCertIds = Arrays.stream(apply.getOriginCertId().split(","))
                                            .sorted()
                                            .collect(Collectors.joining(","));
                                    String existTargetCertIds = Arrays.stream(apply.getTargetCertId().split(","))
                                            .sorted()
                                            .collect(Collectors.joining(","));
                                    if (originCertIds.equals(existOriginCertIds) && targetCertIds.equals(existTargetCertIds)) {
                                        throw new GlobalException("源证书和目标证书已存在兑换的订单");
                                    }
                                });
                            }
                        }
                    }
                }
                competitionCertExchangeApply.setApplyTime(DateUtils.getNowDate());
                competitionCertExchangeApply.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                competitionCertExchangeApply.setCreateTime(DateUtils.getNowDate());
                competitionCertExchangeApply.setUserId(SecurityUtils.getLoginUser().getUserid());
                competitionCertExchangeApply.setApplyStatus("1");
                // 调用下单接口进行付款返回订单id
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setCommodityType("cert");
                orderInfo.setUserName(SecurityUtils.getLoginUser().getUsername());
                orderInfo.setUserId(SecurityUtils.getLoginUser().getUserid());
                orderInfo.setAmount(new BigDecimal(competitionCertExchangeApply.getRepayAmount()));
                orderInfo.setCommodityId(competitionCertExchangeApply.getRuleId().toString());
                CompetitionCertExchangeRule competitionCertExchangeRule = competitionCertExchangeRuleMapper.selectCompetitionCertExchangeRuleById(competitionCertExchangeApply.getRuleId());
                orderInfo.setCommodityName(competitionCertExchangeRule.getRulerName());
                // 组装订单对应商品详细信息
                orderInfo.setTeamInfoList(JSONObject.toJSONString(competitionCertExchangeApply));
                orderInfo.setPayMethod("online");
                // 处理申请目标证书及原证书信息
                String originCertId = competitionCertExchangeApply.getOriginCertList().stream()
                        .filter(c -> Objects.nonNull(c.getCertConfigId())).
                        map(c -> c.getCertConfigId().toString()).collect(Collectors.joining(","));
                String targetCertId = competitionCertExchangeApply.getTargetCertList().stream()
                        .filter(c -> c.getCertConfigId()!=null).
                        map(c -> c.getCertConfigId().toString()).collect(Collectors.joining(","));
                competitionCertExchangeApply.setOriginCertId(originCertId);
                competitionCertExchangeApply.setTargetCertId(targetCertId);
                R<AjaxResult> certPaymentUrl = orderService.getCertPaymentUrl(orderInfo, SecurityConstants.INNER);
                if(R.isSuccess(certPaymentUrl)){
                    competitionCertExchangeApply.setPayStatus(DictConstant.PENDING);
                    competitionCertExchangeApplyMapper.insertCompetitionCertExchangeApply(competitionCertExchangeApply);
                    return certPaymentUrl.getData();
                }
            }
        } catch (Exception e){
            log.info("申请证书互换访问失败:" + e);
            throw new GlobalException(e.getMessage());
        }finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return AjaxResult.error("申请证书互换访问失败,请稍后重试");
    }

    @Override
    public Map<String,Object> saveUserCertExchangeApplyBeforeCheck(CompetitionCertExchangeApply competitionCertExchangeApply) {
        Map<String,Object> result = new HashMap<>();
        boolean flag = false;
        AtomicReference<String> msg = new AtomicReference<>("");
        if(Objects.isNull(competitionCertExchangeApply.getRuleId())){
            msg.set("赛证互通规则id不能为空");
            result.put("success",flag);
            result.put("msg",msg);
            return result;
        }
        R<String> orderStatusByUserIdAndCommodityId = orderService.getOrderStatusByUserIdAndCommodityId(SecurityUtils.getLoginUser().getUserid(), competitionCertExchangeApply.getRuleId().toString(), SecurityConstants.INNER);
        if(R.isSuccess(orderStatusByUserIdAndCommodityId) && StringUtils.isNotEmpty(orderStatusByUserIdAndCommodityId.getMsg())){
            String payStatus = orderStatusByUserIdAndCommodityId.getMsg();
            List<String> payStatusList = Arrays.asList(payStatus.split(","));
            if(payStatusList.contains(DictConstant.PENDING)){
                msg.set("证书兑换存在未支付订单，请先支付");
                result.put("success",flag);
                result.put("msg",msg);
                return result;
            }
            // 存在兑换相同得证书则校验
            // 源证书id集合
            if(payStatusList.contains(DictConstant.PAID)){
                List<Long> originCertIdList = competitionCertExchangeApply.getOriginCertList().stream()
                        .filter(originCert -> Objects.nonNull(originCert.getCertConfigId()))
                        .map(CertConfigInfo::getCertConfigId).toList();
                List<Long> targetCertIdList = competitionCertExchangeApply.getTargetCertList().stream()
                        .filter(originCert -> Objects.nonNull(originCert.getCertConfigId()))
                        .map(CertConfigInfo::getCertConfigId).toList();
                if(CollectionUtils.isNotEmpty(originCertIdList) && CollectionUtils.isNotEmpty(targetCertIdList)){
                    CompetitionCertExchangeApply certExchangeApply = new CompetitionCertExchangeApply();
                    certExchangeApply.setRuleId(competitionCertExchangeApply.getRuleId());
                    List<CompetitionCertExchangeApply> competitionCertExchangeApplies =
                            competitionCertExchangeApplyMapper.selectCompetitionCertExchangeApplyList(certExchangeApply);
                    if (CollectionUtils.isNotEmpty(competitionCertExchangeApplies)) {
                        String originCertIds = originCertIdList.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
                        String targetCertIds = targetCertIdList.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
                        for (CompetitionCertExchangeApply apply : competitionCertExchangeApplies) {
                            String existOriginCertIds = Arrays.stream(apply.getOriginCertId().split(","))
                                    .sorted()
                                    .collect(Collectors.joining(","));
                            String existTargetCertIds = Arrays.stream(apply.getTargetCertId().split(","))
                                    .sorted()
                                    .collect(Collectors.joining(","));
                            if (originCertIds.equals(existOriginCertIds) && targetCertIds.equals(existTargetCertIds)) {
                                msg.set("源证书和目标证书已存在兑换的订单");
                                result.put("success",flag);
                                result.put("msg",msg);
                                return result;
                            }
                        }
                    }
                }
            }
        }
        result.put("success",true);
        return result;
    }

    @Override
    @Transactional
    public int updateUserCertExchangeApply(CompetitionCertExchangeApply competitionCertExchangeApply) {
        // 支付成功后不生成目标证书
//        if(DictConstant.PAID.equals(competitionCertExchangeApply.getPayStatus())){
//            CompetitionCertExchangeApply competitionCertExchangeApplyReq = new CompetitionCertExchangeApply();
//            competitionCertExchangeApplyReq.setRuleId(competitionCertExchangeApply.getRuleId());
//            competitionCertExchangeApplyReq.setUserId(competitionCertExchangeApply.getUserId());
//            competitionCertExchangeApplyReq.setPayStatus(DictConstant.PENDING);
//            log.info("查询到目标证书互换申请列表参数:{}", JSONObject.toJSONString(competitionCertExchangeApplyReq));
//            List<CompetitionCertExchangeApply> competitionCertExchangeApplyList =
//                    competitionCertExchangeApplyMapper.selectCompetitionCertExchangeApplyList(competitionCertExchangeApplyReq);
//            log.info("查询到目标证书互换申请列表结果:{}", JSONObject.toJSONString(competitionCertExchangeApplyList));
//            if(CollectionUtils.isNotEmpty(competitionCertExchangeApplyList)){
//                CompetitionCertExchangeApply competitionCertExchangeApplyRes = competitionCertExchangeApplyList.get(0);
//                List<Long> targetCertIdList = Arrays.stream(competitionCertExchangeApplyRes.getTargetCertId().split(","))
//                        .filter(s -> !s.trim().isEmpty())
//                        .map(Long::parseLong)
//                        .toList();
//                log.info("查询到目标证书id:{}", JSONObject.toJSONString(targetCertIdList));
//                if(CollectionUtils.isNotEmpty(targetCertIdList)){
//                    targetCertIdList.stream().forEach(targetCertId -> {
//                        CertConfigInfo certConfigInfo = certConfigInfoMapper.selectCertConfigInfoById(targetCertId);
//                        // 互换id入证书表
//                        UserCertificate userCertificate = new UserCertificate();
//                        userCertificate.setCreateTime(DateUtils.getNowDate());
//                        userCertificate.setCreateBy(String.valueOf(competitionCertExchangeApply.getUserId()));
//                        // 生成证书名称
//                        // 奖项名称翻译
//                        R<List<SysDictData>> awardsNameList = userService.dictType("awards_name", SecurityConstants.INNER);
//                        if(R.isSuccess(awardsNameList) && CollectionUtils.isNotEmpty(awardsNameList.getData())) {
//                            String awardsName = awardsNameList.getData().stream().filter(sysDictData -> sysDictData.getDictValue().equals(certConfigInfo.getAwardsName()))
//                                    .findFirst().get().getDictLabel();
//                            if(StringUtils.isEmpty(userCertificate.getCertName())){
//                                StringBuffer sb = new StringBuffer();
//                                if(StringUtils.isNotEmpty(certConfigInfo.getCompetitionName())){
//                                    sb.append(certConfigInfo.getCompetitionName()).append("大赛中,");
//                                }
//                                if(StringUtils.isNotEmpty(certConfigInfo.getCompetitionTrackName())){
//                                    sb.append("荣获"+certConfigInfo.getCompetitionTrackName());
//                                }
//                                if(StringUtils.isNotEmpty(certConfigInfo.getSecondLevelName())){
//                                    sb.append(certConfigInfo.getSecondLevelName());
//                                }
//                                if(StringUtils.isNotEmpty(sb)){
//                                    userCertificate.setCertName(sb.toString()+"荣获"+awardsName+"，特此表彰！");
//                                } else {
//                                    userCertificate.setCertName("荣获"+awardsName+"，特此表彰！");
//                                }
//                            }
//                        }
//                        // 获取目表证书配置信息
//                        userCertificate.setCompetitionSeriesId(certConfigInfo.getCompetitionSeriesId());
//                        userCertificate.setCompetitionTrackId(certConfigInfo.getCompetitionTrackId());
//                        userCertificate.setSecondLevelCode(certConfigInfo.getSecondLevelCode());
//                        userCertificate.setCompetitionStageId(certConfigInfo.getCompetitionStageId());
//                        userCertificate.setAcquireWay(ApplyConstants.CERT_ACQUIRE_WAY);
//                        userCertificate.setAwardsName(certConfigInfo.getAwardsName());
//                        userCertificate.setCertStatus(ApplyConstants.CERT_STATUS_EFFECTIVE);
//                        userCertificate.setCertConfigId(targetCertId);
//                        userCertificate.setUserId(competitionCertExchangeApply.getUserId());
//                        userCertificate.setCertExchangeId(competitionCertExchangeApplyRes.getApplyId());
//                        userCertificate.setCertPeriod(certConfigInfo.getCertPeriodTime());
//                        userCertificate.setIssuanceDate(DateUtils.getNowDate());
//                        userCertificate.setYear(DateUtils.dateTimeNow("yyyy"));
//                        CompetitionApplyInfo competitionApplyInfo = new CompetitionApplyInfo();
//                        competitionApplyInfo.setUserId(userCertificate.getUserId());
//                        competitionApplyInfo.setCompetitionSeriesId(certConfigInfo.getCompetitionSeriesId());
//                        competitionApplyInfo.setCompetitionTrackId(certConfigInfo.getCompetitionTrackId());
//                        competitionApplyInfo.setSecondLevelCode(certConfigInfo.getSecondLevelCode());
//                        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoMapper.selectCompetitionApplyInfoListByImportIdCard(competitionApplyInfo);
//                        if(CollectionUtils.isNotEmpty(applyInfoList)){
//                            userCertificate.setUserName(applyInfoList.get(0).getUserName());
//                            userCertificate.setIdCard(applyInfoList.get(0).getIdCard());
//                        }
//                        log.info("新增赛证互通成功后证书信息:{}", JSONObject.toJSONString(userCertificate));
//                        userCertificateMapper.insertUserCertificate(userCertificate);
//                    });
//                }
//            }
//        }
        return competitionCertExchangeApplyMapper.updateCompetitionCertExchangeApplyPC(competitionCertExchangeApply);
    }

    @Override
    public int updateCompetitionCertExchangeApplyInvoiceStatus(List<CompetitionCertExchangeApply> competitionCertExchangeApplyList) {
        return competitionCertExchangeApplyMapper.updateCompetitionCertExchangeApplyInvoiceStatus(competitionCertExchangeApplyList);
    }

    @Override
    public List<UserCertificate> selectUserCertificateByUserId(UserCertificate userCertificate) throws Exception {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        userCertificate.setUserId(sysUser.getUserId());
        if(Objects.nonNull(sysUser.getAuthInfo())){
            userCertificate.setIdCard(RsaUtils.decryptByPrivateKey(sysUser.getAuthInfo().getIdCard()));
        }
        return userCertificateMapper.selectUserCertificateByUserId(userCertificate);
    }
}
