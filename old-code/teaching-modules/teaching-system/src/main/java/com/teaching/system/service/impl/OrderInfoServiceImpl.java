package com.teaching.system.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.uuid.IdUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.DictUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteFlowService;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.model.LoginUser;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.system.domain.*;
import com.teaching.system.domain.vo.TransactionDetail;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IMerchantParamConfigService;
import com.teaching.system.service.IPayService;
import com.teaching.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.teaching.system.service.IOrderInfoService;
import org.springframework.transaction.annotation.Transactional;
import utils.PaySM2Util;
import utils.SignatureUtil;
import utils.TransactionExcelReader;

import static com.teaching.common.core.constant.DictConstant.*;
import static com.teaching.common.core.constant.PayConstant.*;

/**
 * 订单信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-17
 */
@Slf4j
@Service
public class OrderInfoServiceImpl implements IOrderInfoService
{
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private IPayService payService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OrderStatementRecordMapper recordMapper;

    @Autowired
    private RemoteFileService fileService;

    @Autowired
    private InvoiceInfoMapper invoiceInfoMapper;

    //超时时长-秒
    @Value("${pay.payValidTime}")
    private int payValidTime;

    @Value("${pay.orderQrcodeUrl}")
    private String orderQrcodeUrl;

    //私钥
//    @Value("${pay.privateKey}")
//    private String privateKey;
//
//    //公钥
//    @Value("${pay.publicKey}")
//    private String publicKey;
//
//    @Value("${pay.memId}")
//    private String memId;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private OrderGoodsRelationMapper orderGoodsRelationMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private IMerchantParamConfigService merchantParamConfigService;


    @Autowired
    private MerchantParamConfigMapper merchantParamConfigMapper;

    @Autowired
    private RemoteFlowService remoteFlowService;


    /**
     * 上传文件存储在本地的根路径
     */
//    @Value("${file.path}")
//    private String localFilePath;


    @Autowired
    private OrderStatementRecordMapper orderStatementRecordMapper;

    /**
     * 查询订单信息
     *
     * @param id 订单信息主键
     * @return 订单信息
     */
    @Override
    public OrderInfo selectOrderInfoByOrderId(Long id)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        //手机号赋值
        if (orderInfo != null) {
            Long userId = orderInfo.getUserId();
            if (userId != null) {
                SysUser sysUser = sysUserMapper.selectUserById(userId);
                if (sysUser != null) {
                    orderInfo.setPhoneNumber(sysUser.getPhonenumber());
                    orderInfo.setSchoolName(sysUser.getSchoolName());
                }
            }
            //用户名赋值，新增保存的是用户表的nickName,增加认证查询，如果已认证，取认证信息，没有认证，取表中的字段名
            /*AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(orderInfo.getUserId());
            if (authInfo != null) {
                orderInfo.setUserName(authInfo.getRealName());
            }*/
            String merId = orderInfo.getMerId();
            if (StringUtils.isNotEmpty(merId)) {
                MerchantParamConfig paramConfig = merchantParamConfigMapper.selectMerchantParamConfigByMerId(merId);
                if (ObjectUtil.isNotEmpty(paramConfig)) {
                    orderInfo.setMerName(paramConfig.getMerName());
                }
            }
            getAndSetOderDetail(orderInfo,false);
        }
        return orderInfo;
    }

    @Override
    public OrderInfo selectPersonalOrderInfoById(Long id) {
        requireCurrentUserOrder(id);
        return selectOrderInfoByOrderId(id);
    }

    private OrderInfo requireCurrentUserOrder(Long id) {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo == null) {
            throw new ServiceException("订单不存在");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if (!Objects.equals(currentUserId, orderInfo.getUserId())) {
            throw new ServiceException("无权访问该订单", 403);
        }
        return orderInfo;
    }

    private boolean acquireSubmitGuard(String operation, Long orderId, int seconds) {
        return acquireSubmitGuard(operation, String.valueOf(orderId), seconds);
    }

    private boolean acquireSubmitGuard(String operation, String resource, int seconds) {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String key = "personal:submit:" + operation + ":" + currentUserId + ":" + Integer.toHexString(resource.hashCode());
        return Boolean.TRUE.equals(redisService.setIfAbsent(key, "PROCESSING", seconds, java.util.concurrent.TimeUnit.SECONDS));
    }

    /**
     * 查询订单商品详情
     * @param orderInfo
     */
    public void getAndSetOderDetail(OrderInfo orderInfo,boolean isPersonal) {
        if ("cert".equals(orderInfo.getCommodityType())) {
            return;
        }
        //获取商品详情信息
        log.info("=======================================调用远端详情接口===========================================");
        OrderGoodsRelation relation = new OrderGoodsRelation();
        relation.setOrderId(orderInfo.getId());
        List<OrderGoodsRelation> orderGoodsRelations = orderGoodsRelationMapper.selectOrderGoodsRelationList(relation);
        if (CollUtil.isNotEmpty(orderGoodsRelations)) {
            List<String> commodityIds = orderGoodsRelations.stream().map(OrderGoodsRelation::getCommodityId).toList();
            //调用远端接口.获取商品详情
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("teamCodeList",commodityIds);
            //如果是用户端查询需要增加当前登录人
            if (isPersonal) {
                reqMap.put("userId", SecurityUtils.getLoginUser().getSysUser().getUserId());
            }
            log.info("===============================调用远端详情接口,teamCodeList参数：{}====================================", CollUtil.join(commodityIds,","));
            R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
            log.info("===============================调用远端详情接口,返回结果：{}====================================", detailForOrder);
            if (R.isSuccess(detailForOrder)) {
                orderInfo.setCompetitionList(detailForOrder.getData());
                List<CompetitionApplyInfoVO> competitionList = detailForOrder.getData();
                if (CollUtil.isNotEmpty(competitionList)) {
                    //过滤掉fee值为空的对象，然后按fee分组
                    Map<String, List<CompetitionApplyInfoVO>> competitionMap = competitionList.stream().filter(competitionApplyInfoVO -> competitionApplyInfoVO.getFee() != null)
                            .collect(Collectors.groupingBy(CompetitionApplyInfoVO::getFee));
                    List<NumDetail> numList = new ArrayList<>();
                    competitionMap.forEach((fee,competitionApplyInfoVOS)->{
                        List<CompetitionApplyInfoVO> competitionApplyInfoVOS1 = competitionMap.get(fee);
                        Optional<Integer> sum = competitionApplyInfoVOS1.stream().map(CompetitionApplyInfoVO::getTeamSize).reduce(Integer::sum);
                        CompetitionApplyInfoVO vo = competitionApplyInfoVOS1.get(0);
                        //赛事名称+赛项名称+组名
                        //NumDetail numDetail = new NumDetail(String.format("%s%s%s%s%s",vo.getCompetitionName(),"-",vo.getCompetitionTrackName(),"-",vo.getSecondLevelName()) , sum.orElse(0), fee);
                        //name只要赛事名称
                        NumDetail numDetail = new NumDetail(vo.getCompetitionName() , sum.orElse(0), fee);
                        numList.add(numDetail);
                    });
                    orderInfo.setNumDetailList(numList);
                }
            }
        }
    }

    /**
     * 查询订单信息列表
     *
     * @param orderInfo 订单信息
     * @return 订单信息
     */
    @Override
    public List<OrderInfo> selectOrderInfoList(OrderInfo orderInfo,boolean isPersonal)
    {
        //筛选条件结束时间设置为当天的最后时间
        if (orderInfo != null && orderInfo.getPayEndTime() != null) {
            orderInfo.setPayEndTime(DateUtil.endOfDay(orderInfo.getPayEndTime()));
        }
        List<OrderInfo> orderInfos = orderInfoMapper.selectOrderInfoList(orderInfo);
//        List<InvoiceInfo> invoiceInfos = invoiceInfoMapper.selectInvoiceInfoList(new InvoiceInfo());
        List<MerchantParamConfig> merchantParamConfigs = merchantParamConfigService.selectMerchantParamConfigList(new MerchantParamConfig());
        if (CollUtil.isNotEmpty(orderInfos)) {
            log.info("获取订单信息:"+JSONObject.toJSONString(orderInfos));
            for (OrderInfo info : orderInfos) {
                //是退费重缴的缴费订单且是待支付时 隐藏取消订单按钮
                if(Objects.nonNull(info.getPayOrderId())){
                    List<OrderGoodsRelation> orderGoodsRelationList = orderGoodsRelationMapper.selectByOrderIdAndCommodityId(info.getPayOrderId(),info.getCommodityId());
                    log.info("获取原订单信息:"+JSONObject.toJSONString(orderGoodsRelationList));
                    if(CollUtil.isNotEmpty(orderGoodsRelationList)){
                        // 订单id唯一，则隐藏取消订单按钮
                        orderGoodsRelationList.stream().forEach(orderGoodsRelation -> {
                            info.setChangeType(orderGoodsRelation.getChangeType());
                        });
                    }
                }
//                info.setHide(DictConstant.PENDING.equals(info.getPayStatus()) && info.getPayOrderId() != null);
                //用户名赋值，新增保存的是用户表的nickName,增加认证查询，如果已认证，取认证信息，没有认证，取表中的字段名
                AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(info.getUserId());
                if (authInfo != null) {
                    info.setUserName(authInfo.getRealName());
                }
                //个人列表需查询详情
                if (isPersonal) {
                    //获取商品详情信息
                    getAndSetOderDetail(info,true);
                }
                merchantParamConfigs.stream().filter(e -> e.getMerId().equals(info.getMerId())).findFirst().ifPresent(merchantParamConfig -> info.setMerName(merchantParamConfig.getMerName()));
            }
        }
        return orderInfos;
    }

    /**
     * 构建订单信息
     * @param orderInfo
     */
    public void buildOrderInfo(OrderInfo orderInfo){
        MerchantParamConfig config = merchantParamConfigService.getConfig(orderInfo.getCommodityType(), orderInfo.getCompetitionSeriesId());
        if (StringUtils.isEmpty(orderInfo.getCommodityId())) {
            throw new RuntimeException("商品id不能为空");
        }
        //查询用户有该商品最新的订单
        //OrderInfo commodityOrder = orderInfoMapper.getOrderByUserIdAndCommodityId(orderInfo.getUserId(), orderInfo.getCommodityId());
        //原订单信息
        OrderInfo selectOrderInfo = null;
        if (StringUtils.isNotEmpty(orderInfo.getOrderId())) {
            selectOrderInfo = orderInfoMapper.selectOrderInfoByOrderId(orderInfo.getOrderId());
        }
        //商品已有订单
        if (selectOrderInfo != null) {
            //如果订单状态不是失败状态，返回异常信息
            if (!selectOrderInfo.getPayStatus().equals(DictConstant.FAILED)) {
                throw new RuntimeException("用户已有该商品订单，且非支付失败状态，不能重复下单");
            }else {
                //有老订单且是失败状态，认为是重新下单，修改老订单状态
                selectOrderInfo.setLastOrder("0");
                updateOrderInfo(selectOrderInfo);
                log.info("修改历史订单lastOrder状态值,Id:"+orderInfo.getId());
            }
        }
        //构建新订单信息--雪花id-作为订单id
        orderInfo.setId(IdUtil.getSnowflakeNextId());
        //订单orderId-和支付交互时使用的订单id
        orderInfo.setOrderId(IdUtils.simpleUUID());
        orderInfo.setCreateTime(DateUtils.getNowDate());
        orderInfo.setLastOrder("1");
        orderInfo.setMerId(config.getMerId());
        setUserName(orderInfo);
        //权限机构
        LoginUser loginUser = SecurityUtils.getLoginUser();
        orderInfo.setOrgId(loginUser != null ? loginUser.getSysUser().getOrgId() : null);
        //新插入数据，默认待支付
        orderInfo.setPayStatus(DictConstant.PENDING);
        // 团队信息
        //调用远端接口.获取商品详情
        Map<String, Object> reqMap = new HashMap<>();
        List<String> commodityIds = Arrays.asList(orderInfo.getCommodityId().split(","));
        reqMap.put("teamCodeList",commodityIds);
        reqMap.put("userId", SecurityUtils.getLoginUser().getSysUser().getUserId());
        R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
        if (R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())) {
            orderInfo.setTeamInfoList(JSONObject.toJSONString(detailForOrder.getData()));
        }
    }

    /**
     * 新增订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertOrderInfo(OrderInfo orderInfo)
    {
        orderInfo.setOrderType(ORDER_TYPE_PAY);
        int i = orderInfoMapper.insertOrderInfo(orderInfo);
        //订单插入成功，插入订单关联商品信息
        if (i > 0) {
            //插入关联团队信息
            insertOrderGoodsList(orderInfo);
        }
        return i;
    }

    @Override
    public OrderInfo insertRefundOrderInfo(OrderInfo payOrder,TeamChangeDto teamChangeDto) {

        OrderInfo refundOrderInfo = new OrderInfo();
        //构建新订单信息--雪花id-作为订单id
        refundOrderInfo.setId(IdUtil.getSnowflakeNextId());
        refundOrderInfo.setOrderId(IdUtils.simpleUUID());
        //订单关联id
        refundOrderInfo.setPayOrderId(payOrder.getId());
        refundOrderInfo.setCommodityId(teamChangeDto.getTeamCode());
        refundOrderInfo.setCommodityName(payOrder.getCommodityName());
        refundOrderInfo.setCommodityType(payOrder.getCommodityType());
        refundOrderInfo.setCreateTime(DateUtils.getNowDate());
        refundOrderInfo.setLastOrder("1");
        refundOrderInfo.setMerId(payOrder.getMerId());
        refundOrderInfo.setUserId(payOrder.getUserId());
        refundOrderInfo.setUserName(payOrder.getUserName());
        setUserName(refundOrderInfo);
        refundOrderInfo.setOrgId(payOrder.getOrgId());
        refundOrderInfo.setPayMethod(OFFLINE); //线下退款
        refundOrderInfo.setAmount(teamChangeDto.getAmount()); //退款金额
        refundOrderInfo.setOrderType(DictConstant.ORDER_TYPE_REFUND);  //退款订单
        refundOrderInfo.setPayStatus(REFUNDING);                       //退款中
        refundOrderInfo.setTeamInfoList(teamChangeDto.getTeamNewInfo());
        //团队人员
        Map<String,String> teamUsersMap = new HashMap<>();
        if (teamChangeDto.getUserIds() != null) {
            teamUsersMap.put(teamChangeDto.getTeamCode(), teamChangeDto.getUserIds());
        }
        refundOrderInfo.setTeamUsers(teamUsersMap);
        int i = orderInfoMapper.insertOrderInfo(refundOrderInfo);
        //订单插入成功，插入订单关联商品信息
        if (i > 0) {
            //插入关联团队信息
            insertOrderGoodsList(refundOrderInfo);
        }
        //orderGoodsRelationMapper.updateChangeType(payOrder.getOrderId(),teamChangeDto.getTeamCode(), teamChangeDto.getChangeType());
        return refundOrderInfo;
    }

    private void setUserName(OrderInfo orderInfo) {
        if (orderInfo.getUserId() != null) {
            //查询实名认证姓名
            AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(orderInfo.getUserId());
            if (authInfo != null) {
                orderInfo.setUserName(authInfo.getRealName());
            }else{  //没有认证，取系统用户名
                SysUser sysUser = userService.selectUserById(orderInfo.getUserId());
                if (sysUser != null) {
                    orderInfo.setUserName(sysUser.getNickName());
                }
            }
        }
    }

    private void insertOrderGoodsList(OrderInfo orderInfo) {
        int i = orderGoodsRelationMapper.deleteByOrderId(orderInfo.getId().toString());
        String commodityIds = orderInfo.getCommodityId();
        String[] split = commodityIds.split(",");
        List<OrderGoodsRelation> orderGoodsRelations = new ArrayList<>();
        for (String commodityId : split) {
            OrderGoodsRelation relation = new OrderGoodsRelation();
            relation.setOrderId(orderInfo.getId());
            relation.setCommodityId(commodityId);
            relation.setOrgId(orderInfo.getOrgId());
            relation.setCreateTime(DateUtils.getNowDate());
            relation.setDelFlag("0");
            //查找团队对应的人员信息
            if (CollUtil.isNotEmpty(orderInfo.getTeamUsers())) {
                Map<String, String> teamUsers = orderInfo.getTeamUsers();
                if (teamUsers.containsKey(commodityId)) {
                    relation.setUsers(teamUsers.get(commodityId));
                }
            }
            orderGoodsRelations.add(relation);
        }
        //批量插入订单关联商品信息
        orderGoodsRelationMapper.insertOrderGoodsRelationBatch(orderGoodsRelations);
    }

    @Override
    @Transactional
    public AjaxResult getCertPaymentUrl(OrderInfo orderInfo) {
        if (Objects.isNull(orderInfo.getCommodityName())) {
            throw new ServiceException("订单信息不能为空，请检查订单是否有商品信息！");
        }
        OrderInfo selectOrderInfo = null;
        if (orderInfo.getOrderId() != null) {
            selectOrderInfo = orderInfoMapper.selectOrderInfoByOrderId(orderInfo.getOrderId());
        }
        if (selectOrderInfo == null) {
            buildOrderInfo(orderInfo);
            AjaxResult ajaxResult = applyOrderQrCode(orderInfo);
            if (ajaxResult.isSuccess()) {
                insertOrderInfo(orderInfo);
                updateBusinessStatus(orderInfo);
            }
            return ajaxResult;
        }
        if (DictConstant.PAID.equals(selectOrderInfo.getPayStatus()) || DictConstant.REFUNDED.equals(selectOrderInfo.getPayStatus())) {
            return AjaxResult.error("订单已支付，不能生成二维码！");
        }
        if (DictConstant.FAILED.equals(selectOrderInfo.getPayStatus())) {
            buildOrderInfo(orderInfo);
            AjaxResult ajaxResult = applyOrderQrCode(orderInfo);
            if (ajaxResult.isSuccess()) {
                insertOrderInfo(orderInfo);
                updateBusinessStatus(orderInfo);
            }
            return ajaxResult;
        }
        return applyOrderQrCode(selectOrderInfo);
    }

    private void updateBusinessStatus(OrderInfo orderInfo) {
        if ("cert".equals(orderInfo.getCommodityType())) {
            updateCertApplyStatus(orderInfo);
            return;
        }
        updateCompetitionOrderStatus(orderInfo);
    }

    private void updateCertApplyStatus(OrderInfo orderInfo) {
        CompetitionCertExchangeApply apply = new CompetitionCertExchangeApply();
        apply.setRuleId(Long.valueOf(String.valueOf(orderInfo.getCommodityId())));
        apply.setUserId(orderInfo.getUserId());
        apply.setPayStatus(orderInfo.getPayStatus());
        apply.setOrderId(orderInfo.getId());
        apply.setPayTime(orderInfo.getPayTime());
        if ("paid".equals(orderInfo.getPayStatus())) {
            apply.setApplyStatus("2");
        } else if ("cancelled".equals(orderInfo.getPayStatus())) {
            apply.setApplyStatus("0");
        }
        competitionService.updateUserCertExchangeApply(apply, SecurityConstants.INNER);
    }

    /**
     * 修改赛事状态
     * @param orderInfo
     */
    private void updateCompetitionOrderStatus(OrderInfo orderInfo) {
        List<OrderGoodsRelation> relations = orderGoodsRelationMapper.selectByOrderId(orderInfo.getId());
        List<String> codeList = relations.stream().map(OrderGoodsRelation::getCommodityId).toList();
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("teamCodeList",codeList);
        reqMap.put("payStatus", orderInfo.getPayStatus());
        reqMap.put("payTime", orderInfo.getPayTime());
        log.info("订单Id:"+orderInfo.getOrderId()+"赛事团队codeList:"+CollUtil.join(codeList,",") + "修改状态值:" + orderInfo.getPayStatus());
        competitionService.updateCompetitionTrackStatus(reqMap, SecurityConstants.INNER);
    }

    /**
     * 修改订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    @Override
    public int updateOrderInfo(OrderInfo orderInfo)
    {
        orderInfo.setUpdateTime(DateUtils.getNowDate());
        return orderInfoMapper.updateOrderInfo(orderInfo);
    }

    @Override
    public int updateOrderInfoTeamJson(String updateSize) {
        List<OrderInfo> orderInfos = orderInfoMapper.selectOrderInfoTeamJson();
        if (CollectionUtils.isNotEmpty(orderInfos)) {
            log.info("定时任务开始更新订单信息中团队信息:"+orderInfos.size());
            List<OrderInfo> updateOrderInfos = new ArrayList<>();
            if(StringUtils.isNotEmpty(updateSize) && Integer.parseInt(updateSize)>orderInfos.size()){
                updateOrderInfos.addAll(orderInfos);
            } else if(StringUtils.isNotEmpty(updateSize)){
                updateOrderInfos.addAll(orderInfos.subList(0,Integer.parseInt(updateSize)));
            }
            if(CollectionUtils.isNotEmpty(updateOrderInfos)){
                updateOrderInfos.forEach(orderInfo -> {
                    //调用远端接口.获取商品详情
                    Map<String, Object> reqMap = new HashMap<>();
                    List<String> commodityIds = Arrays.asList(orderInfo.getCommodityId().split(","));
                    reqMap.put("teamCodeList",commodityIds);
                    R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
                    if (R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())) {
                        // 获取teamJson为null的订单信息
                        OrderInfo orderInfoUpdate = new OrderInfo();
                        orderInfoUpdate.setId(orderInfo.getId());
                        orderInfoUpdate.setTeamInfoList(JSONObject.toJSONString(detailForOrder.getData()));
                        orderInfoMapper.updateOrderInfo(orderInfoUpdate);
                    }
                });
            }
            log.info("定时任务开始更新订单信息中团队信息任务结束");
        }
        return 0;
    }

    /**
     * 批量删除订单信息
     *
     * @param ids 需要删除的订单信息主键
     * @return 结果
     */
    @Override
    public int deleteOrderInfoByOrderIds(Long[] ids)
    {
        return orderInfoMapper.deleteOrderInfoByOrderIds(ids);
    }

    /**
     * 删除订单信息信息
     *
     * @param id 订单信息主键
     * @return 结果
     */
    @Override
    public int deleteOrderInfoByOrderId(Long id)
    {
        return orderInfoMapper.deleteOrderInfoByOrderId(id);
    }

    @Override
    public void payResultNotify(String orderId, String status) {

    }

    @Override
    @Transactional
    public AjaxResult getPaymentUrl(OrderInfo orderInfo,boolean isFirst) {
        log.info("===============================获取二维码接口开始============================");
        OrderInfo selectOrderInfo =null;
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        orderInfo.setUserId(currentUserId);
        //查询人员该商品最新的订单信息
        //OrderInfo commodityOrder = getOrderInfoByUserIdAndCommodityId(orderInfo.getUserId(), orderInfo.getCommodityId());
        //如果有orderId，根据orderId获取订单信息
        if (orderInfo.getOrderId() != null) {
            selectOrderInfo = orderInfoMapper.selectOrderInfoByOrderId(orderInfo.getOrderId());
            if (selectOrderInfo == null || !Objects.equals(currentUserId, selectOrderInfo.getUserId())) {
                throw new ServiceException("无权访问或支付该订单", 403);
            }
            // 已存在订单的商品、金额、用户等关键字段一律以服务端记录为准。
            orderInfo = selectOrderInfo;
        }
        if (Objects.isNull(orderInfo.getCommodityName())) {
            throw new ServiceException("订单信息不能为空，请检查订单是否有商品信息！");
        }
        //如果没有订单，直接下单,并生成二维码
        if (selectOrderInfo == null) {
            String resource = String.valueOf(orderInfo.getCompetitionSeriesId()) + ":" + String.valueOf(orderInfo.getCommodityId());
            if (!acquireSubmitGuard("create-payment", resource, 30)) {
                return AjaxResult.error("支付请求正在处理中，请勿重复提交");
            }
            //查询当前用户缓存中的token,做token校验，并远程调用接口获取总金额和团队code并赋值
            setAmountAndCommodityIds(orderInfo);
            //创建订单
            buildOrderInfo(orderInfo);
            log.info("已下单，即将获取二维码");
            AjaxResult ajaxResult = applyOrderQrCode(orderInfo);
            if (ajaxResult.isSuccess()) {
                //插入数据
                insertOrderInfo(orderInfo);
                //删除缓存中的token
                if (redisService.hasKey("settlement:" + orderInfo.getCompetitionSeriesId() +":"+ orderInfo.getUserId())) {
                    redisService.deleteObject("settlement:" + orderInfo.getCompetitionSeriesId()+":" + orderInfo.getUserId());
                }
                //需改赛事团队订单状态
                updateBusinessStatus(orderInfo);
            }
            return ajaxResult;
        }else {
            //如果订单已支付，直接返回。暂时没有退款
            if (DictConstant.PAID.equals(selectOrderInfo.getPayStatus()) || DictConstant.REFUNDED.equals(selectOrderInfo.getPayStatus())) {
                log.info("订单已支付,不能生成二维码");
                return AjaxResult.error("订单已支付，不能生成二维码！");
            }
            //判断是否是重新生成订单,如果之前订单是失败状态，重新下单并把之前的订单last_order值改为非最新订单0(需改状态insertOrderInfo里面实现)
            if(DictConstant.FAILED.equals(selectOrderInfo.getPayStatus())){
                if (!acquireSubmitGuard("retry-payment", selectOrderInfo.getId(), 30)) {
                    return AjaxResult.error("支付请求正在处理中，请勿重复提交");
                }
                //添加新的订单
                buildOrderInfo(orderInfo);
                log.info("新单已下，即将生成二维码");
                AjaxResult ajaxResult = applyOrderQrCode(orderInfo);
                if (ajaxResult.isSuccess()) {
                    insertOrderInfo((orderInfo));
                }
                return ajaxResult;
            }else{
                //如果有二维码，且不是重新生成，直接返回原二维码
                if (StringUtils.isNotEmpty(selectOrderInfo.getQrCode()) && isFirst) {
                    log.info("===============================订单已有二维码链接，直接返回============================");
                    return AjaxResult.success(selectOrderInfo);
                }else{
                    if (!acquireSubmitGuard("refresh-payment", selectOrderInfo.getId(), 30)) {
                        return AjaxResult.error("支付请求正在处理中，请勿重复提交");
                    }
                    //生成/重新生成二维码
                    return applyOrderQrCode(selectOrderInfo);
                }
            }
        }
    }

    void setAmountAndCommodityIds(OrderInfo orderInfo){
        log.info("===============================下单远程获取团队code和金额============================");
        log.info("==================身份认证================");
        //身份验证
        Long userId = orderInfo.getUserId();
        //验证令牌
        String key = "settlement:" + orderInfo.getCompetitionSeriesId()+ ":" + userId;
        Object cacheObject = redisService.getCacheObject(key);
        if (ObjectUtil.isNull(cacheObject)) {
            log.info("结算信息已失效，请重新选择4");
            throw new ServiceException("结算信息已失效，请重新选择");
        }
        //验证 token teamCode
        Map<String, Object> map = (Map<String, Object>) cacheObject;
        String string = MapUtils.getString(map, "token");
        if (!orderInfo.getToken().equals(string)) {
            log.info("结算信息有误，请重新选择5");
            throw new ServiceException("结算信息有误，请重新选择");
        }
        log.info("==================身份认证通过================");
        log.info("=======调用远程接口，入参CompetitionSeriesId：{}==========" , orderInfo.getCompetitionSeriesId());
        //远程接口查询商品总金额和商品codes
        R<Map<String, Object>> teamInfo = competitionService.getTeamInfo(orderInfo.getCompetitionSeriesId(),SecurityConstants.INNER);
        log.info("=======调用远程接口，出参：{}==========" , teamInfo);
        if (teamInfo.getCode() != 200 || teamInfo.getData() == null) {
            log.info("结算信息有误，请重新选择6");
            throw new ServiceException("结算信息有误，请重新选择");
        }
        //取值，订单赋值
        Map<String, Object> data = teamInfo.getData();
        List<String> teamCodeList = (List<String>)data.get("teamCodeList");
        if (CollUtil.isEmpty(teamCodeList)) {
            log.info("结算信息有误，请重新选择7");
            throw new ServiceException("结算信息有误，请重新选择");
        }
        orderInfo.setCommodityId(CollUtil.join(teamCodeList, ","));
        orderInfo.setAmount(new BigDecimal(data.get("totalFee").toString()));
    }

    @Transactional
    AjaxResult applyOrderQrCode(OrderInfo orderInfo) {
        //否则生成新的二维码
        //调用接口获取支付二维码信息
        String qrCode;
        log.info("===============================开始创建二维码链接============================");
        Map<String, String> returnMap = payService.applyOrderQrCode(orderInfo);
        if (returnMap.get("code").equals(SUCCESS)) {
            String bizContent = returnMap.get("data");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseBodyMap = null;
            try {
                responseBodyMap = objectMapper.readValue(bizContent, Map.class);
            } catch (JsonProcessingException e) {
                throw new ServiceException("获取支付二维码信息解析失败！返回二维码数据：" + bizContent);
            }
            //二维码地址
            qrCode = responseBodyMap.get(QR_CODE);
            //兼容测试环境商户号，返回的支付二维码链接的处理  30899917273028O-测试商户号
            if(orderQrcodeUrl.contains("https://api.cmburl.cn:8065")){
                String substring = qrCode.substring(qrCode.indexOf(".com") + 4);
                qrCode ="http://payment-uat.cs.cmburl.cn"+substring;
            }
            //银行订单号--订单二维码没有银行订单号
            //String cmbOrderId = responseBodyMap.get(PayConstant.CMB_ORDER_ID);
            //orderInfo.setTargetOrderId(cmbOrderId);
            //更新订单信息
            orderInfo.setQrCode(qrCode);
            orderInfo.setQrCodeExpireTime(addTimeToDate(new Date()));
            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("===============================创建二维码链接结束============================");
            return AjaxResult.success(orderInfo);
        }else {
            throw new ServiceException(returnMap.get("msg"));
        }
    }

    //将yyyyMMddHHmmss格式时间数据增加时长
    public String addTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, payValidTime);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public String addTimeToDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, payValidTime);
        return sdf.format(calendar.getTime());

    }


    @Override
    @Transactional
    public AjaxResult regeneratePaymentUrl(Long id) {
        log.info("============================重新生成二维码=========================");
        //查询订单信息
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("regenerate-payment", id, 30)) {
            return AjaxResult.error("请求正在处理中，请勿重复提交");
        }
        //如果订单已支付，直接返回原对象 ,订单取消状态也不能生成支付二维码 前端根据状态区分
        if (PAID.equals(orderInfo.getPayStatus()) || CANCELLED.equals(orderInfo.getPayStatus())) {
            return AjaxResult.success(orderInfo);
        }
        //查询当前订单是否已是失效状态，如果是，直接生成新的二维码，否则发起前二维码失效
        Map<String, String> uqeryResultMap = payService.queryOrder(orderInfo);
        //如果订单未支付且没有失效，此处返回的是FIAL
        if (!FAIL.equals(uqeryResultMap.get("code"))) {
            String queryBizContent = uqeryResultMap.get("data");
            ObjectMapper queryObjectMapper = new ObjectMapper();
            Map<String,String> queryResponseMap = null;
            try {
                queryResponseMap = queryObjectMapper.readValue(queryBizContent, Map.class);
            }catch (JsonProcessingException e) {
                return AjaxResult.error("退款返回数据解析失败："+queryBizContent);
            }
            /**
             * 交易状态
             * S- 支付成功
             * P- 支付中
             * C - 订单已关闭
             * D - 交易已撤销
             */
            String tradeState = queryResponseMap.get("tradeState"); //订单状态
            if ("S".equals(tradeState) || "P".equals(tradeState)) {
                return AjaxResult.error(300, "订单已支付或支付中，请稍后刷新...");
            }
            if("C".equals(tradeState) || "D".equals(tradeState)){
                //重新生成二维码，生成新的orderId
                log.info("订单{}原二维码已失效，重新生成二维码",orderInfo.getId());
                return generateNewUrl(orderInfo);
            }
        }

        //发起老订单失效
        //订单接口调用
        Map<String, String> resultMap = payService.closeOrder(orderInfo);
        if (FAIL.equals(resultMap.get("code"))) {
            //特殊情况下会出现订单不存在的情况，直接重新生成新的二维码
//            log.error("关闭订单失败，订单id："+orderInfo.getId());
//            throw new ServiceException(resultMap.get("msg"));
            log.info("关闭订单失败，返回结果："+resultMap);
            return generateNewUrl(orderInfo);
        }
        String bizContent = resultMap.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(bizContent, Map.class);
        } catch (JsonProcessingException e) {
            log.error("关闭订单:"+orderInfo.getId()+",返回数据解析失败："+bizContent);
            throw new ServiceException("重新刷新二维码失败，请重试！");
        }
        //关单处理状态
        //C 订单关闭成功
        //F 被关闭交易为失败状态，关单失败
        String closeState = responseMap.get("closeState");
        if("C".equals(closeState)){
            //重新生成二维码，生成新的orderId
            return generateNewUrl(orderInfo);
        }else {
            log.error("关闭订单失败，订单id："+orderInfo.getId()+",返回状态码："+closeState);
            throw new ServiceException("原订单二维码取消失败，请重新刷新二维码");
        }
    }

    private AjaxResult generateNewUrl(OrderInfo orderInfo) {
        String newOrderId = IdUtils.simpleUUID();
        //更新订单信息，并将二维码链接置空防止直接查询到原来的二维码
        orderInfo.setOrderId(newOrderId);
        orderInfo.setQrCode("");
        //重新生成二维码将原状态重置为待支付
        orderInfo.setPayStatus(PENDING);
        orderInfoMapper.updateOrderInfo(orderInfo);
        //插入关联团队信息
        insertOrderGoodsList(orderInfo);
        //使用新的订单id生成订单
        return getPaymentUrl(orderInfo, false);
    }

    @Override
    public Map<String, String> paymentCallback(Map<String, String> notifyMap){
        log.info("支付回调请求参数："+notifyMap);

        Map<String,String> resultMap = new HashMap<>();
        resultMap.put(VERSION,VERSION_VALUE);  //版本号
        resultMap.put(ENCODING,ENCODING_VALUE); //编码方式
        resultMap.put(SIGN_METHOD,SIGN_METHOD_VALUE);  //加密方式-SM2

        //SUCCESS表示商户接收通知成功并校验成功
        resultMap.put("returnCode", SUCCESS);

        // 返回结果验签
        //Map<String, String> receivableMap = notifyMap.entrySet().stream().collect(Collectors.toMap(e -> SignatureUtil.decode(e.getKey()), e -> SignatureUtil.decode(e.getValue())));
        String sign = notifyMap.remove("sign");
        //对待加签内容进行排序拼接
        String contentStr = SignatureUtil.getSignContent(notifyMap);
        //验证签名-使用招行公钥进行验签
        /*boolean flag = PaySM2Util.sm2Check(contentStr,sign, publicKey);
        if (!flag) {
            //验签失败
            System.out.println("验签失败");
            resultMap.put("returnCode", "FAIL");
            return resultMap;
        }
        System.out.println("验签成功");*/
        //通知内容解析
        String bizContent = notifyMap.get(BIZ_CONTENT);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseBodyMap = null;
        try {
            responseBodyMap = objectMapper.readValue(bizContent, Map.class);
        }catch (JsonProcessingException e) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode",FAIL);  //响应码
            resultMap.put("respMsg","支付回调数据解析失败，返回数据："+resultMap);  //响应信息
            return resultMap;
        }
        //商户订单号
        String orderId = responseBodyMap.get("orderId");
        //交易金额
        String txnAmt = responseBodyMap.get("txnAmt");
        //支付方式 支付方式，目前支持：支付宝/微信/银联/数字人民币
        //ZF：支付宝
        //WX：微信
        //YL：银联
        //EC：数字人民币
        String payType = responseBodyMap.get("payType");
        //第三方订单号（微信、支付宝侧的订单号）
        String targetOrderId = responseBodyMap.get("thirdOrderId");
        //订单完成日期
        String endDate = responseBodyMap.get("endDate");
        //订单完成时间
        String endTime = responseBodyMap.get("endTime");
        String bizOrderId = responseBodyMap.get("orderId"); //商户订单号
        String outOrderId = responseBodyMap.get("outOrderId"); //外部商户订单号
        String cmbOrderId = responseBodyMap.get("cmbOrderId"); //招行生成的订单号

        //根据订单号查询本地订单信息
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderId(orderId);
        if (orderInfo == null) {
            resultMap.put("respCode",FAIL);  //响应码
            resultMap.put("respMsg","订单不存在");  //响应信息
            log.info("支付回调处理失败，返回数据："+resultMap);
            return resultMap;
        }
        if (!DictConstant.PENDING.equals(orderInfo.getPayStatus())) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode",SUCCESS);  //响应码
            addSignToMap(resultMap,orderInfo);
            log.info("本地订单非待支付状态，无需再次处理数据，直接返回数据："+resultMap);
            return resultMap;
        }
        //校验金额是否一致,本地存储数据单位为元，支付返回为分
        if (orderInfo.getAmount().multiply(new BigDecimal(100)).compareTo(new BigDecimal(txnAmt)) != 0) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode", FAIL);  //响应码
            resultMap.put("respMsg", "订单金额不一致");  //响应信息
            log.info("支付回调处理失败，返回数据：" + resultMap);
            return resultMap;
        }
        //更新订单信息
        orderInfo.setPayStatus(DictConstant.PAID);
        orderInfo.setPayMode(payType);
        orderInfo.setPayTime(convertToDate(endDate, endTime) == null ? new Date() : convertToDate(endDate, endTime));
        orderInfo.setUpdateTime(new Date());
        orderInfo.setTargetOrderId(targetOrderId);
        orderInfo.setCmbOrderId(cmbOrderId);
        orderInfo.setBizOrderId(bizOrderId);
        orderInfo.setOutOrderId(outOrderId);
        orderInfo.setPayMethod(ONLINE);
        orderInfoMapper.updateOrderInfo(orderInfo);
        //构建成功结果
        resultMap.put("returnCode", SUCCESS);
        resultMap.put("respCode",SUCCESS);  //响应码
        addSignToMap(resultMap,orderInfo);
        log.info("支付回调处理成功，返回数据："+resultMap);
        updateBusinessStatus(orderInfo);
        //判断是否是退费重缴,是的话发起退费流程
        startRefundFlow(orderInfo);
        return resultMap;
    }

    void startRefundFlow(OrderInfo orderInfo){
        if (orderInfo.getPayOrderId() != null) {
            //查询支付是否已经有对应的退款订单，如果已经有了，就不用再发起了
//            OrderInfo refundOrder = orderInfoMapper.selectRefundOrderByPayOrderId(orderInfo.getPayOrderId());
//            if (refundOrder != null) {
//                log.info("该支付订单已有对应的退费单，无需再次发起！orderId:" + orderInfo.getId());
//                return;
//            }
            //查询调整订单的原订单，查看对应团队，获取调整类型
            List<OrderGoodsRelation> orderGoodsRelations = orderGoodsRelationMapper.selectByOrderId(orderInfo.getPayOrderId());
            OrderGoodsRelation orderGoodsRelation = orderGoodsRelations.stream().filter(e -> e.getCommodityId().equals(orderInfo.getCommodityId())).findFirst().orElse(null);
            if (orderGoodsRelation == null) {
                log.info("未找到原订单对应的团队信息，无法发起退费流程，订单ID："+orderInfo.getId());
                return;
            }
            //更新调整单的支付状态
            orderGoodsRelation.setPayStatus("paid");
            orderGoodsRelationMapper.updateOrderGoodsRelation(orderGoodsRelation);
            //发起退费
            if (REPAYMENT.equals(orderGoodsRelation.getChangeType())) {
                //异步发起流程，不影响主服务
                new Thread(() -> {
                    try {
                        startRefundFlow(orderInfo, orderGoodsRelation);
                    } catch (Exception e) {
                        log.info("发起退费流程失败：orderId:"+orderInfo.getId());
                    }
                }).start();
            }
        }
    }

    private void startRefundFlow(OrderInfo orderInfo, OrderGoodsRelation orderGoodsRelation) {
        String teamCode = orderGoodsRelation.getCommodityId();
        Map<String, Object> variables = new HashMap<>();
        variables.put("teamCode", teamCode);
        variables.put("operationType", REPAYMENT);
        variables.put("commodityType", orderInfo.getCommodityType());
        variables.put("amount", orderInfo.getAmount());
        variables.put("reason"," 退费重缴自动发起");
        variables.put("teamInfo", orderInfo.getCommodityName());
        variables.put("ApplicantId", orderInfo.getUserId());
        variables.put("ApplicantName", orderInfo.getUserName());
        //关联原订单id，备用
        variables.put("payOrderId", orderInfo.getPayOrderId());
//                variables.put("userIds", orderInfo.getTeamUsers().get(teamCode));
        //发起退费流程
        remoteFlowService.startByCategory(variables,RETIRED,teamCode, SecurityConstants.INNER);
    }

    private void addSignToMap(Map<String,String> resultMap,OrderInfo orderInfo) {
        MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        //对待加签内容进行排序拼接
        String signContent = SignatureUtil.getSignContent(resultMap);
        //加签-使用商户私钥加签
        resultMap.put("sign", PaySM2Util.sm2Sign(signContent, merchantParamConfig.getPayPrivateKey()));
        System.out.println("加签成功");
    }

    @Override
    public AjaxResult cancelOrder(Long id) {
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("cancel-order", id, 15)) {
            return AjaxResult.error("订单正在处理中，请勿重复提交");
        }
        if (DictConstant.PAID.equals(orderInfo.getPayStatus())) {
            return AjaxResult.error("订单已支付，无法取消");
        }
        //如果是线下支付，没有线上订单，直接修改状态即可
        if (OFFLINE.equals(orderInfo.getPayMethod())) {
            orderInfo.setPayStatus(DictConstant.CANCELLED);
            orderInfo.setUpdateTime(new Date());
            orderInfoMapper.updateOrderInfo(orderInfo);
            updateBusinessStatus(orderInfo);
            //如果是调整单，更新状态
            updateOriginPayStatus(orderInfo);
            return AjaxResult.success("订单已取消！");
        }
        //先查询判断老二维码是否已失效，如果已经失效，直接修改订单状态即可
        Map<String, String> uqeryResultMap = payService.queryOrder(orderInfo);
        //如果订单未支付且没有失效，此处返回的是FIAL
        if (!FAIL.equals(uqeryResultMap.get("code"))) {
            String queryBizContent = uqeryResultMap.get("data");
            ObjectMapper queryObjectMapper = new ObjectMapper();
            Map<String,String> queryResponseMap = null;
            try {
                queryResponseMap = queryObjectMapper.readValue(queryBizContent, Map.class);
            }catch (JsonProcessingException e) {
                return AjaxResult.error("退款返回数据解析失败："+queryBizContent);
            }

            /**
             * 交易状态
             * C - 订单已关闭
             * D - 交易已撤销
             */
            String tradeState = queryResponseMap.get("tradeState"); //订单状态
            if("C".equals(tradeState) || "D".equals(tradeState)){
                //原订单已失效，不需要再次关闭二维码订单，直接修改本地订单状态即可
                log.info("订单{}原二维码已失效，直接取消本地订单,订单状态值：{}",orderInfo.getId(),tradeState);
                orderInfo.setPayStatus(DictConstant.CANCELLED);
                orderInfo.setUpdateTime(new Date());
                orderInfoMapper.updateOrderInfo(orderInfo);
                updateBusinessStatus(orderInfo);
                //如果是调整单，更新状态
                updateOriginPayStatus(orderInfo);
                return AjaxResult.success("订单已取消！");
            }
        }
        //订单接口调用
        Map<String, String> resultMap = payService.closeOrder(orderInfo);
        if (FAIL.equals(resultMap.get("code"))) {
//            return AjaxResult.error(resultMap.get("msg"));
            //return AjaxResult.error("订单取消异常，请稍后再试");
            log.error("订单取消异常,orderId:"+orderInfo.getId()+",msg:"+resultMap.get("msg"));
            orderInfo.setPayStatus(DictConstant.CANCELLED);
            orderInfo.setUpdateTime(new Date());
            orderInfoMapper.updateOrderInfo(orderInfo);
            //订单取消，修改商品订单状态
            updateBusinessStatus(orderInfo);
            //如果是调整单，更新状态
            updateOriginPayStatus(orderInfo);
            return AjaxResult.success("订单已取消！");
        }
        String bizContent = resultMap.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(bizContent, Map.class);
        } catch (JsonProcessingException e) {
            return AjaxResult.error("关闭订单返回数据解析失败："+bizContent);
        }
        //关单处理状态
        //C 订单关闭成功
        //F 被关闭交易为失败状态，关单失败
        String closeState = responseMap.get("closeState");
        if("C".equals(closeState)){
            orderInfo.setPayStatus(DictConstant.CANCELLED);
            orderInfo.setUpdateTime(new Date());
            orderInfoMapper.updateOrderInfo(orderInfo);
            updateBusinessStatus(orderInfo);
            //如果是调整单，更新状态
            updateOriginPayStatus(orderInfo);
            return AjaxResult.success("订单已取消！");
        }else {
            return AjaxResult.error("被关闭交易为失败状态，关单失败");
        }
    }

    @Transactional
    @Override
    public AjaxResult cancelRepaymentOrder(Long id) {
        // 退费重缴已经生成待支付订单，进行取消
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("cancel-repayment", id, 15)) {
            return AjaxResult.error("订单正在处理中，请勿重复提交");
        }
        if (DictConstant.PAID.equals(orderInfo.getPayStatus())) {
            return AjaxResult.error("订单已支付，无法取消");
        }
        orderInfo.setPayStatus(DictConstant.CANCELLED);
        orderInfo.setUpdateTime(new Date());
        orderInfoMapper.updateOrderInfo(orderInfo);
        //订单取消，修改商品订单状态为取消
        orderGoodsRelationMapper.updatePayStatus(orderInfo.getId().toString(), orderInfo.getCommodityId(), CANCELLED);
        updateOriginPayStatus(orderInfo);
        R<Integer> cancelOrderOperationTimes = competitionService.cancelRepaymentOperationTimes(orderInfo.getCommodityId(), SecurityConstants.INNER);
        if(R.isError(cancelOrderOperationTimes)){
            throw new GlobalException(cancelOrderOperationTimes.getMsg());
        }
        return AjaxResult.success("订单已取消！");
    }

    /**
     * 更新原始订单状态为cancelled
     */
    private void updateOriginPayStatus(OrderInfo orderInfo){
        try{
            //payorderID不为空，说明为调整单
            if (orderInfo.getPayOrderId() != null) {
                //更新原订单对应的当前团队关联表的支付状态payStatus为取消状态
                orderGoodsRelationMapper.updatePayStatus(orderInfo.getPayOrderId().toString(), orderInfo.getCommodityId(), CANCELLED);
            }
        }catch (Exception e){
            log.error("更新原始订单"+orderInfo.getId()+"失败："+e.getMessage());
        }
    }

    @Override
    public AjaxResult refund(Long id,String refundReason) {
        log.info("退款接口调用，订单ID："+id);
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo == null) {
            log.info("退款接口调用，订单不存在，订单ID："+id);
            return AjaxResult.error("订单不存在");
        }
        if (!DictConstant.PAID.equals(orderInfo.getPayStatus())) {
            log.info("退款接口调用，订单非支付状态，无法退款，订单ID："+id);
            return AjaxResult.error("订单非支付状态，无法退款");
        }
        //生成退款订单号--第一次退款自动生成订单号，如果退款失败，重新发起，还要使用原来的退款单号，不能再重新生成
        if (StringUtils.isEmpty(orderInfo.getRefundOrderId())) {
            String refundOrderId = IdUtils.simpleUUID();
            orderInfo.setRefundOrderId(refundOrderId);
            orderInfoMapper.updateOrderInfo(orderInfo);
        }
        //调用退款接口
        Map<String, String> resultMap = payService.refundOrder(orderInfo);
        if (FAIL.equals(resultMap.get("code"))) {
            return AjaxResult.error(resultMap.get("msg"));
        }
        String bizContent = resultMap.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(bizContent, Map.class);
        }catch (JsonProcessingException e) {
            return AjaxResult.error("退款返回数据解析失败："+bizContent);
        }
        //退款处理状态
        //S 退款成功
        //F 退款失败
        //P 退款处理中
        String refundState = responseMap.get("refundState");
        //退款发起成功
        if("S".equals(refundState)){
            //状态改为退款中
            orderInfo.setPayStatus(DictConstant.REFUNDED);
            orderInfo.setUpdateTime(new Date());
            orderInfo.setRefundTime(new Date());
            orderInfo.setRefundAmount(orderInfo.getAmount());
            orderInfo.setRefundReason(refundReason);
            //orderInfoMapper.updateOrderInfo(orderInfo);
            return AjaxResult.success("退款成功！");
        }else if("F".equals(refundState)){      //失败处理
            return AjaxResult.error(responseMap.get("failureReason"));
        }else if("P".equals(refundState)){     //退款中
            //状态改为退款中
            orderInfo.setPayStatus(DictConstant.REFUNDING);
            orderInfo.setUpdateTime(new Date());
            orderInfo.setRefundAmount(orderInfo.getAmount());
            orderInfo.setRefundReason(refundReason);
            return AjaxResult.success("退款处理中，请稍后查询退款状态");
        }else {
            return AjaxResult.error("退款发起失败，请重新发起退款申请！");
        }
    }

    @Override
    public Map<String, String> refundCallback(Map<String, String> notifyMap) {

        log.info("退款回调请求参数："+notifyMap);

        Map<String,String> resultMap = new HashMap<>();
        resultMap.put(VERSION,VERSION_VALUE);  //版本号
        resultMap.put(ENCODING,ENCODING_VALUE); //编码方式
        resultMap.put(SIGN_METHOD,SIGN_METHOD_VALUE);  //加密方式-SM2

        //SUCCESS表示商户接收通知成功并校验成功
        resultMap.put("returnCode", SUCCESS);

        // 返回结果验签
        String sign = notifyMap.remove("sign");
        //对待加签内容进行排序拼接
        String contentStr = SignatureUtil.getSignContent(notifyMap);
        //验证签名-使用招行公钥进行验签
        /*boolean flag = PaySM2Util.sm2Check(contentStr,sign, publicKey);
        if (!flag) {
            //验签失败
            System.out.println("验签失败");
            resultMap.put("returnCode", "FAIL");
            return resultMap;
        }*/

        String bizContent = notifyMap.get(BIZ_CONTENT);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseBodyMap = null;
        try {
            responseBodyMap = objectMapper.readValue(bizContent, Map.class);
        }catch (JsonProcessingException e) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode",FAIL);  //响应码
            resultMap.put("respMsg","退款回调数据解析失败，返回数据："+resultMap);  //响应信息
            return resultMap;
        }
        //商户退款订单号
        String refundOrderId = responseBodyMap.get("orderId");
        //退款金额
        String refundAmt = responseBodyMap.get("refundAmt");

        //第三方订单号（微信、支付宝侧的订单号）
        String targetOrderId = responseBodyMap.get("thirdOrderId");
        //订单完成日期
        String txnTime = responseBodyMap.get("endDate");
        //订单完成时间
        String txnDate = responseBodyMap.get("endTime");


        //根据订单号查询本地订单信息
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByRefundOrderId(refundOrderId);
        if (orderInfo == null) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode",FAIL);  //响应码
            resultMap.put("respMsg","订单不存在");  //响应信息
            log.info("退款回调处理失败，返回数据："+resultMap);
            return resultMap;
        }
        if (DictConstant.REFUNDED.equals(orderInfo.getPayStatus())) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode",SUCCESS);  //响应码
            addSignToMap(resultMap,orderInfo);
            log.info("本地订单已是退款状态，无需再次处理数据，直接返回数据："+resultMap);
            return resultMap;
        }
        //校验金额是否一致,单位不一致，本地数据金额*100
        if (orderInfo.getAmount().multiply(new BigDecimal(100)).compareTo(new BigDecimal(refundAmt)) != 0) {
            resultMap.put("returnCode", SUCCESS);
            resultMap.put("respCode", FAIL);  //响应码
            resultMap.put("respMsg", "退款金额和缴费金额不一致");  //响应信息
            log.info("退款回调处理失败，返回数据：" + resultMap);
            return resultMap;
        }
        //更新订单信息
        orderInfo.setPayStatus(DictConstant.REFUNDED);
        orderInfo.setRefundTime(new Date());
        orderInfo.setUpdateTime(new Date());
        orderInfo.setRefundAmount(orderInfo.getAmount());
        orderInfoMapper.updateOrderInfo(orderInfo);
        //构建成功结果
        resultMap.put("returnCode", SUCCESS);
        resultMap.put("respCode",SUCCESS);  //响应码
        addSignToMap(resultMap,orderInfo);
        log.info("退款回调处理成功，返回数据："+resultMap);
        return resultMap;
    }

    @Override
    public AjaxResult queryPaymentResult(Long id) {

        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo == null) {
            return AjaxResult.error("订单不存在");
        }
        Map<String, String> resultMap = payService.queryOrder(orderInfo);
        if (FAIL.equals(resultMap.get("code"))) {
            return AjaxResult.error(resultMap.get("msg"));
        }
        String bizContent = resultMap.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(bizContent, Map.class);
        }catch (JsonProcessingException e) {
            return AjaxResult.error("退款返回数据解析失败："+bizContent);
        }
        String txnAmt = responseMap.get("txnAmt");   //交易金额
        String payType = responseMap.get("payType"); //交易方式
        /**
         * 交易状态
         * C - 订单已关闭
         * D - 交易已撤销
         * P - 交易在进行
         * F - 交易失败
         * S - 交易成功
         * R - 转入退款
         */
        String tradeState = responseMap.get("tradeState"); //交易时间
        String endDate = responseMap.get("endDate"); //订单完成日期
        String endTime = responseMap.get("endTime"); //订单完成时间
        String thirdOrderId = responseMap.get("thirdOrderId"); //支付宝、微信侧的订单号
        String merId = responseMap.get("merId");//商户号
        String bizOrderId = responseMap.get("orderId"); //商户订单号
        String outOrderId = responseMap.get("outOrderId"); //外部商户订单号
        String cmbOrderId = responseMap.get("cmbOrderId"); //招行生成的订单号

        if ("S".equals(tradeState)) {
            orderInfo.setPayStatus(DictConstant.PAID);
            orderInfo.setPayTime(convertToDate(endDate, endTime) == null ? new Date() : convertToDate(endDate, endTime));
            orderInfo.setUpdateTime(new Date());
            orderInfo.setPayMode(payType);
            orderInfo.setTargetOrderId(thirdOrderId);
            orderInfo.setMerId(merId);
            orderInfo.setOutOrderId(outOrderId);
            orderInfo.setCmbOrderId(cmbOrderId);
            orderInfo.setBizOrderId(bizOrderId);
            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("订单查询结果：支付成功，订单号："+orderInfo.getId());
            updateBusinessStatus(orderInfo);
            //退费重缴的发起退费流程
            startRefundFlow(orderInfo);
            return AjaxResult.success("支付成功！");
        }else if ("C".equals(tradeState) || "D".equals(tradeState)){
            //取消订单不做同步
//            orderInfo.setPayStatus(DictConstant.CANCELLED);
//            orderInfo.setUpdateTime(new Date());
//            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("订单查询结果：订单已关闭/撤销，订单号："+orderInfo.getId());
            return AjaxResult.success("订单已关闭/撤销");
        }else if ("F".equals(tradeState)){
            log.info("订单查询结果：支付失败，订单号："+orderInfo.getId());
            orderInfo.setPayStatus(DictConstant.FAILED);
            orderInfo.setUpdateTime(new Date());
            orderInfoMapper.updateOrderInfo(orderInfo);
            return AjaxResult.success("支付失败");
        }else if ("P".equals(tradeState)){
            orderInfo.setPayStatus(DictConstant.PAYING);
            log.info("订单查询结果：支付进行中，订单号："+orderInfo.getId());
            return AjaxResult.success("支付进行中");
        }else if ("R".equals(tradeState)){
//            orderInfo.setPayStatus(DictConstant.REFUNDING);
//            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("订单查询结果：转入退款，订单号："+orderInfo.getId());
            return AjaxResult.success("订单转入退款");
        }else{
            log.info("订单查询结果：未知状态，订单号："+orderInfo.getId());
            return AjaxResult.success("订单状态未知");
        }
    }

    //将日期格式为yyyyMMdd和时间格式为HHmmss的两个字符串转换成Data类型的值
    private Date convertToDate(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
            return sdf.parse(dateStr + " " + timeStr);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public AjaxResult queryRefundResult(Long id) {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo == null) {
            return AjaxResult.error("订单不存在");
        }
        Map<String, String> resultMap = payService.queryRefund(orderInfo);
        if (FAIL.equals(resultMap.get("code"))) {
            log.error("查询退款结果失败："+resultMap.get("msg"));
            return AjaxResult.error(resultMap.get("msg"));
        }
        String bizContent = resultMap.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(bizContent, Map.class);
        }catch (JsonProcessingException e) {
            log.error("退款返回数据解析失败："+bizContent);
            return AjaxResult.error("退款返回数据解析失败："+bizContent);
        }
        String refundAmt = responseMap.get("refundAmt");   //退款金额
        /**
         * 交易状态
         * P 预退款完成
         * F 失败
         * S 退款成功
         */
        String tradeState = responseMap.get("tradeState"); //交易时间
        String endDate = responseMap.get("endDate"); //订单完成日期
        String endTime = responseMap.get("endTime"); //订单完成时间
        if ("S".equals(tradeState)) {
            orderInfo.setPayStatus(DictConstant.REFUNDED);
            orderInfo.setRefundTime(convertToDate(endDate, endTime) == null ? new Date() : convertToDate(endDate, endTime));
            orderInfo.setUpdateTime(new Date());
            orderInfo.setRefundAmount(new BigDecimal(refundAmt).divide(new BigDecimal(100)));
            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("退款查询结果：退款成功，订单号："+orderInfo.getId());
            return AjaxResult.success("退款成功！");
        }else if ("F".equals(tradeState)){
            orderInfo.setPayStatus(DictConstant.FAILED);
            orderInfo.setUpdateTime(new Date());
            orderInfoMapper.updateOrderInfo(orderInfo);
            log.info("退款结果：退款失败，订单号："+orderInfo.getId());
            return AjaxResult.success("退款失败");
        }else if ("P".equals(tradeState)){
            log.info("退款查询结果：预退款完成，订单号："+orderInfo.getId());
            return AjaxResult.success("预退款完成");
        }{
            log.info("退款查询结果：未知状态，订单号："+orderInfo.getId());
            return AjaxResult.success("退款状态未知");
        }
    }

    @Override
    public AjaxResult wxMiniCreateOrder(Long id,String ip) {
        log.info("===============================微信小程序发起订单开始============================");
        log.info("订单id："+id);
        //查询订单信息
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("mini-payment", id, 30)) {
            return AjaxResult.error("支付请求正在处理中，请勿重复提交");
        }
        if (DictConstant.PAID.equals( orderInfo.getPayStatus())) {
            log.error("订单已支付");
            return AjaxResult.error("订单已支付");
        }
        log.info("订单信息："+orderInfo);
        //调用接口获取支付二维码信息
        Map<String, String> returnMap = payService.wxMiniCreateOrder(orderInfo,ip);
        if (returnMap.get("code").equals(SUCCESS)) {
            log.info("===============================微信小程序下单结束============================");
            return AjaxResult.success("已下单");
        }else {
            log.error("微信小程序下单失败："+returnMap.get("msg"));
            return AjaxResult.error(returnMap.get("msg"));
        }
    }

    /*@Override
    public AjaxResult statementUrl(String billDate) {

        //获取对账文件地址
        Map<String, String> resultMap = payService.statementDownloadUrl(billDate);
        if (FAIL.equals(resultMap.get("code"))) {
            log.error("获取账单下载地址失败："+resultMap.get("msg"));
            return AjaxResult.error(resultMap.get("msg"));
        }
        String fileDownloadUrl = resultMap.get("data");
        log.info("获取账单下载地址成功："+fileDownloadUrl);
        return AjaxResult.success("获取下载地址成功！",fileDownloadUrl);
    }*/

    @Override
    public void statementTask(String billDate) {

        List<MerchantParamConfig> merchantParamConfigs = merchantParamConfigService.selectMerchantParamConfigList(new MerchantParamConfig());
        if (CollUtil.isEmpty(merchantParamConfigs)) {
            log.error("对账定时任务：无可用的商户配置信息！");
        }
        for (MerchantParamConfig merchantParamConfig : merchantParamConfigs) {
            //校验是否已经对账
            Integer count = recordMapper.selectOrderStatementRecordByBillDate(billDate);
            if (count > 0) {
                log.info(billDate+"已完成对账，无需再次对账");
                continue;
            }
            R<String> filePathJSon = fileService.getFilePath();
            String filePath = filePathJSon.getData()+ "/" + billDate+".xlsx";
            Map<String, String> resultMap = new HashMap<>();
            try{
                //获取对账文件地址
                resultMap = payService.statementDownloadUrl(billDate,merchantParamConfig);
            }catch (Exception e){
                log.info("获取对账单下载地址错误：" + e.getMessage());
                continue;
            }
            if (FAIL.equals(resultMap.get("code"))) {
                log.error("获取账单下载地址失败："+resultMap.get("msg"));
                continue;
            }
            String fileDownloadUrl = resultMap.get("data");
            log.info("获取账单下载地址成功："+fileDownloadUrl);
            //下载对账文件到本地
            HttpUtil.downloadFile(fileDownloadUrl, FileUtil.file(filePath));
            log.info("对账文件下载成功："+filePath);
            // 读取交易明细
            List<TransactionDetail> transactions =  TransactionExcelReader.readTransactionDetails(filePath);
            log.info("读取对账文件内容："+ transactions);
            //对比本地数据，增加对账记录
            if (CollUtil.isNotEmpty(transactions)) {
                for (TransactionDetail transaction : transactions) {
                    //对账记录
                    OrderStatementRecord record = initOrderStatementRecord(transaction,billDate);
                    //交易类型 消费/退货
                    String transactionType = transaction.getTransactionType();
                    //获取交易订单号,如果是退货，取原交易订单号
                    String merchantOrderNo = "消费".equals(transactionType)? transaction.getMerchantOrderNo():transaction.getOriginalMerchantOrderNo();
                    OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderId(merchantOrderNo);
                    if (orderInfo == null) {
                        //插入一条对账记录，记录错误信息
                        record.setStatus(FAIL);
                        record.setRemark("订单不存在");
                        recordMapper.insertOrderStatementRecord(record);
                        continue;
                    }
                    BigDecimal amount = orderInfo.getAmount();

                    //对比交易记录--对账订单的金额单位为元
                    if ("消费".equals(transactionType)) {
                        if (amount.compareTo(transaction.getTransactionAmount())==0) {
                            //支付记录对账成功
                            record.setStatus(SUCCESS);
                            recordMapper.insertOrderStatementRecord(record);
                        }else{
                            //插入一条对账记录，记录错误信息
                            record.setStatus(FAIL);
                            record.setRemark("收费支付金额不一致");
                            recordMapper.insertOrderStatementRecord(record);
                        }
                    }else if ("退货".equals(transactionType)){
                        if(amount.compareTo(transaction.getTransactionAmount().abs())==0) {
                            //退费记录对账成功
                            record.setStatus(SUCCESS);
                            recordMapper.insertOrderStatementRecord(record);
                        }else{
                            //插入一条对账记录，记录错误信息
                            record.setStatus(FAIL);
                            record.setRemark("退款金额不一致");
                            recordMapper.insertOrderStatementRecord(record);
                        }
                    }
                }
            }
        }
        log.info("对账结束，对账日期:"+billDate);
    }

    //初始化对账记录
    private OrderStatementRecord initOrderStatementRecord(TransactionDetail transaction,String billDate) {

        OrderStatementRecord record = new OrderStatementRecord();
        //交易金额
        record.setAmount(transaction.getTransactionAmount());
        //交易类型 消费/退货
        String transactionType = transaction.getTransactionType();
        record.setOrderType(transactionType);
        //关联订单号
        record.setOrderId("消费".equals(transactionType)? transaction.getMerchantOrderNo():transaction.getOriginalMerchantOrderNo());
        //订单日期
        record.setBillDate(transaction.getBillDate());
        //对账文件名称
        record.setStatementFileName(billDate+".xlsx");
        //创建时间
        record.setCreateTime(new Date());
        return record;
    }

    /**
     * 定时查询待支付已生成二维码，且在有效期范围内的订单数据，查询支付结果状态
     */
    @Override
    public void syncPaymentResult() {
        log.info("========================开始同步支付结果==========================");
        List<OrderInfo> orderInfos = orderInfoMapper.syncPaymentResult();
        if (CollUtil.isEmpty(orderInfos)) {
            log.info("没有需要同步结果的支付订单！");
            return;
        }
        for (OrderInfo orderInfo : orderInfos) {
            queryPaymentResult(orderInfo.getId());
            log.info("同步状态值,订单号:"+orderInfo.getOrderId());
        }
        log.info("===========================同步结束==================================");
    }

    /**
     * 同步退款结果状态
     */
    @Override
    public void syncRefundResult() {
        log.info("========================开始同步退款结果==========================");
        List<OrderInfo> orderInfos = orderInfoMapper.syncRefundResult();
        if (CollUtil.isEmpty(orderInfos)) {
            log.info("没有需要同步结果的退款订单！");
            return;
        }
        for (OrderInfo orderInfo : orderInfos) {
            queryRefundResult(orderInfo.getId());
        }
        log.info("===========================同步结束==================================");
    }

    @Override
    public OrderInfo getOrderInfoByUserIdAndCommodityId(Long userId, String commodityId) {
        List<OrderInfo> list = orderInfoMapper.getOrderByUserIdAndCommodityId(userId, commodityId);
        return CollUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    @Override
    public String selectOrderStatusByUserIdAndCommodityId(Long userId, String commodityId) {
        List<OrderInfo> list = orderInfoMapper.getOrderByUserIdAndCommodityId(userId, commodityId);
        if (CollUtil.isNotEmpty(list)) {
            return list.stream()
                    .map(OrderInfo::getPayStatus)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
        }
        return null;
    }

    @Override
    public List<OrderInfo> selectOrderStatus(SelectOrderStatusReq req) {
        return orderInfoMapper.selectOrderStatus(req);
    }

    @Override
    @Transactional
    public AjaxResult updateAttachmentInfo(Long id, String attachmentInfo) {
        //支付成功，修改商品订单状态
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("payment-proof", id, 15)) {
            return AjaxResult.error("凭证正在提交，请勿重复操作");
        }
        AjaxResult error = checkPayStatus(orderInfo);
        if (error != null) return error;
        orderInfoMapper.updateAttachmentInfo(id, attachmentInfo);
        updateBusinessStatus(orderInfo);
        return AjaxResult.success("凭证已上传");
    }

    @Override
    @Transactional
    public int checkAttachmentInfo(Long id, Integer checkStatus, String checkReason) {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo != null) {
            orderInfo.setPayStatus(checkStatus == 1 ? PAID : APPROVE_REJECTED);
            orderInfo.setAuditOpinion(checkReason);
            //审核通过，更新支付时间
            if (checkStatus == 1) {
                orderInfo.setPayTime(new Date());
            }
            orderInfo.setUpdateTime(new Date());
            int i = orderInfoMapper.updateOrderInfo(orderInfo);
            updateBusinessStatus(orderInfo);
            if (checkStatus == 1) {
                //审批成功，退费重缴的发起退费流程
                startRefundFlow(orderInfo);
            }
            return i;
        }
        return 0;
    }

    @Override
    public AjaxResult updatePaymentMethod(Long id, String paymentMethod) {
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        if (!acquireSubmitGuard("payment-method", id, 15)) {
            return AjaxResult.error("支付方式正在更新，请勿重复操作");
        }
        AjaxResult error = checkPayStatus(orderInfo);
        if (error != null) return error;
        orderInfo.setPayMethod(paymentMethod);
        return AjaxResult.success(orderInfoMapper.updateOrderInfo(orderInfo));
    }

    @Nullable
    private AjaxResult checkPayStatus(OrderInfo orderInfo) {
        try {
            //查询当前订单是否已是失效状态，如果是，直接生成新的二维码，否则发起前二维码失效
            Map<String, String> uqeryResultMap = payService.queryOrder(orderInfo);
            //如果订单未支付且没有失效，此处返回的是FIAL
            if (!FAIL.equals(uqeryResultMap.get("code"))) {
                String queryBizContent = uqeryResultMap.get("data");
                ObjectMapper queryObjectMapper = new ObjectMapper();
                Map<String, String> queryResponseMap = null;
                try {
                    queryResponseMap = queryObjectMapper.readValue(queryBizContent, Map.class);
                } catch (JsonProcessingException e) {
                    log.info("修改订单支付状态，查询订单实时状态返回数据报错：" + e.getMessage());
                }
                /**
                 * 交易状态
                 * S- 支付成功
                 * P- 支付中
                 * C - 订单已关闭
                 * D - 交易已撤销
                 */
                String tradeState = queryResponseMap.get("tradeState"); //订单状态
                if ("S".equals(tradeState) || "P".equals(tradeState)) {
                    return AjaxResult.error(300, "订单已扫码支付或支付中，请稍后刷新...");
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public int getPendingCount(Long userId) {
        return orderInfoMapper.getPendingCount(userId);
    }

    @Override
    public Map<String, Long> perStatusCount(OrderInfo orderInfo) {
        //筛选条件结束时间设置为当天的最后时间
        if (orderInfo != null && orderInfo.getPayEndTime() != null) {
            orderInfo.setPayEndTime(DateUtil.endOfDay(orderInfo.getPayEndTime()));
        }
        List<OrderInfo> orderInfos = orderInfoMapper.selectOrderInfoList(orderInfo);
        //统计各状态的订单数
        List<SysDictData> dictCache = DictUtils.getDictCache(PAY_STATUS);
        if (CollUtil.isEmpty(dictCache)) {
            return new HashMap<>();
        }
        //字典所有值结合
        List<String> list = dictCache.stream().map(SysDictData::getDictValue).toList();
        Map<String, Long> statusCount = orderInfos.stream().collect(Collectors.groupingBy(OrderInfo::getPayStatus, Collectors.counting()));
        //订单已有的字典值
        Set<String> strings = statusCount.keySet();
        for (String key : list) {
            if (!strings.contains(key)) {
                statusCount.put(key, 0L);
            }
        }
        return statusCount;
    }

    @Override
    public int getCountByTeamCodes(String[] teamCodes) {
        return orderInfoMapper.selectCountByTeamCodes(teamCodes);
    }
    @Override
    public OrderInfo getOrderByCommodityId(String commodityId) {
        return orderInfoMapper.getOrderByCommodityId(commodityId);
    }

    @Override
    public Map<String, String> getOfflineBankInfo(Long id) {
        OrderInfo orderInfo = requireCurrentUserOrder(id);
        MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("merName", merchantParamConfig.getMerName());
        returnMap.put("account", merchantParamConfig.getAccount());
        returnMap.put("bank", merchantParamConfig.getBank());
        return returnMap;
    }

    @Override
    public List<String> selectTeamCodesByOrderIds(List<Long> ids) {

        return orderInfoMapper.selectTeamCodesByOrderIds(ids);
    }

    @Override
    public List<OrderInfo> selectOrderListByOrderIds(List<Long> ids) {
        return orderInfoMapper.selectOrderListByOrderIds(ids);
    }

    @Override
    public List<String> getCommodityNameList() {
        return orderInfoMapper.getCommodityNameList();
    }

    @Override
    public AjaxResult createPayOrderByTeamChange(TeamChangeDto teamChangeDto) {
        if (teamChangeDto == null || StringUtils.isEmpty(teamChangeDto.getTeamCode())) {
            throw new ServiceException("团队信息不能为空");
        }
        String teamCode = teamChangeDto.getTeamCode();
        if (!acquireSubmitGuard("team-payment", teamCode, 30)) {
            return AjaxResult.error("订单正在生成中，请勿重复提交");
        }
        if(REPAYMENT.equals(teamChangeDto.getChangeType())){
            //校验退费重缴可操作次数
            R<Boolean> checked = competitionService.checkRepaymentTimes(SecurityConstants.INNER, teamChangeDto.getCompetitionSeriesId(), teamChangeDto.getTeamCode());
            if(!R.isSuccess(checked)){
                throw new ServiceException("退费重缴次数已用完，无法生成变更订单");
            }
        }
        //根据团队code获取原始订单
        //变更后可能会有一个团队code对应多个订单的情况，所以返回list，但是我需要的是CommodityName，值都一样，取第一个就好
        OrderInfo selectOrder = orderInfoMapper.selectOrderByTeamCode(teamCode);
        if (selectOrder == null) {
            throw new ServiceException("未找到原订单信息，无法生成变更订单");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if (!Objects.equals(currentUserId, selectOrder.getUserId())) {
            throw new ServiceException("无权为该团队生成变更订单", 403);
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCommodityType(teamChangeDto.getCommodityType());
        orderInfo.setEventId(teamChangeDto.getEventId());
        orderInfo.setCompetitionSeriesId(teamChangeDto.getEventId());
        orderInfo.setCommodityId(teamChangeDto.getTeamCode());
        orderInfo.setUserId(currentUserId);
        orderInfo.setCommodityName(selectOrder.getCommodityName());
        orderInfo.setPayMethod(ONLINE);
        //变更人员赋值
        Map<String, String> teamUsers = new HashMap<>();
        teamUsers.put(teamChangeDto.getTeamCode(),teamChangeDto.getUserIds());
        orderInfo.setTeamUsers(teamUsers);
        //如果金额有值，直接取值，没有的话在进行金额计算
        if (teamChangeDto.getAmount()!=null && teamChangeDto.getAmount().compareTo(BigDecimal.ZERO)>0) {
            orderInfo.setAmount(teamChangeDto.getAmount());
        }else{
            //根据组别code查询单价，计算金额
            String secondLevelCode = teamChangeDto.getSecondLevelCode();
            R<String> competitionFee = competitionService.getCompetitionFee(secondLevelCode, SecurityConstants.INNER);
            if (competitionFee.getCode() != 200 || competitionFee.getData() == null) {
                throw new ServiceException("获取赛事收费标准失败，无法生成变更订单");
            }
            BigDecimal fee = new BigDecimal(competitionFee.getData());
            if (teamChangeDto.getUserNum() != null) {
                orderInfo.setAmount(fee.multiply(new BigDecimal(teamChangeDto.getUserNum())));
            }else{
                List<String> users = Arrays.stream(teamChangeDto.getUserIds().split(",")).toList();
                //计算金额
                orderInfo.setAmount(fee.multiply(new BigDecimal(users.size())));
            }
        }
        orderInfo.setRelAmount(orderInfo.getAmount());
        //订单状态
        orderInfo.setPayStatus(DictConstant.PENDING);
        //关联原始订单--退费重交支付完成后，发起退费使用
        orderInfo.setPayOrderId(selectOrder.getId());
        //变更类型
        orderInfo.setChangeType(teamChangeDto.getChangeType());
        //创建订单
        buildOrderInfo(orderInfo);
        if(StringUtils.isNotEmpty(teamChangeDto.getTeamNewInfo())){
            orderInfo.setTeamInfoList(teamChangeDto.getTeamNewInfo());
        }
        log.info("orderInfo:"+orderInfo);
        log.info("已构建订单数据，即将获取二维码");
        AjaxResult ajaxResult = applyOrderQrCode(orderInfo);
        log.info("生成支付订单二维码结果："+ajaxResult);
        if (ajaxResult.isSuccess()) {
            //插入数据
            insertOrderInfo(orderInfo);
            //记录操作次数
            competitionService.recordUsedTimes(SecurityConstants.INNER,teamCode,teamChangeDto.getCompetitionSeriesId().toString());
        }
        orderGoodsRelationMapper.updateChangeType(selectOrder.getId().toString(),teamChangeDto.getTeamCode(),teamChangeDto.getChangeType());
        return ajaxResult;
    }

    @Override
    public AjaxResult createRefundOrderByTeamChange(TeamChangeDto teamChangeDto) {

        //根据团队code获取原始订单
        String teamCode = teamChangeDto.getTeamCode();
        String changeType = teamChangeDto.getChangeType();
        OrderInfo payOrder;
        if (REPAYMENT.equals(changeType)) {
            //退费重缴发起时，参数中有payOrderId(原订单id)，可以直接查询原订单信息
            if (StringUtils.isNotEmpty(teamChangeDto.getPayOrderId())) {
                payOrder = orderInfoMapper.selectOrderInfoById(Long.parseLong(teamChangeDto.getPayOrderId()));
            }else {
                //退费重缴发起退费流程时，查询团队调整类型为repayment，且已支付的最后一个订单
                payOrder = orderInfoMapper.selectRefundRepaymentOrderByTeamCode(teamCode);
            }
        }else {
            //其他两个类型，查询类型不是repayment且已支付的第一个订单
            payOrder = orderInfoMapper.selectOrderByTeamCode(teamCode);
        }
        if (payOrder == null) {
            throw new ServiceException("未找到对应的订单信息，无法生成变更订单");
        }
        //生成退款单
        return AjaxResult.success(insertRefundOrderInfo(payOrder,teamChangeDto));
    }

    @Override
    @Transactional
    public void updateRefundStatus(Long refundId,String refundReason,String changeType) {
        //修改退费订单状态
        OrderInfo refundOrder = orderInfoMapper.selectOrderInfoById(refundId);
        //根据退费中状态，修改设置已退费状态  1、退费中-> 已退费   2、重缴退费中->重缴已退费
        refundOrder.setPayStatus(REFUNDING.equals(refundOrder.getPayStatus()) ? REFUNDED : REPAY_REFUNDED);
        refundOrder.setRefundReason(refundReason);
        int i = orderInfoMapper.updateOrderInfo(refundOrder);
        //修改原订单退费金额和实缴金额
        OrderInfo payOrderInfo = orderInfoMapper.selectOrderInfoById(refundOrder.getPayOrderId());
        //更新退费金额-有可能会有多次退费，所以是累加
        BigDecimal getRefundAmount = payOrderInfo.getRefundAmount() == null ? BigDecimal.ZERO : payOrderInfo.getRefundAmount();
        payOrderInfo.setRefundAmount(getRefundAmount.add(refundOrder.getAmount()));
        payOrderInfo.setRelAmount(payOrderInfo.getAmount().subtract(getRefundAmount));
        orderInfoMapper.updateOrderInfo(payOrderInfo);
        //退费成功，修改关联团队和人员的退费状态
        if (i > 0) {
            List<OrderGoodsRelation> orderGoodsRelations = orderGoodsRelationMapper.selectByOrderId(refundOrder.getId());
            if (CollUtil.isEmpty(orderGoodsRelations)) {
                log.info("未找到退费订单关联团队");
            }else {
                //查询关联团队和具体人员
                List<String> users = orderGoodsRelations.stream().map(OrderGoodsRelation::getUsers).toList();
                if (CollUtil.isNotEmpty(users)) {
                    String join = StringUtils.join(users, ",");
                    List<String> userIds = Arrays.stream(join.split(",")).toList();
                }
            }
            //原订单关联团队调整类型修改--调整类型字典change_type
            List<OrderGoodsRelation> payOrderGoodsRelations = orderGoodsRelationMapper.selectByOrderId(payOrderInfo.getId());
            payOrderGoodsRelations.stream().filter(relation->relation.getCommodityId().equals(refundOrder.getCommodityId())).findFirst().ifPresent(relation->{
                relation.setChangeType(changeType);
                orderGoodsRelationMapper.updateOrderGoodsRelation(relation);
            });
        }
    }

    @Override
    public Map<String, Object> getPayAndRefundOrderInfo(Long refundId) {
        Map<String,Object> resultMap = new HashMap<>();
        //查询退款订单信息
        OrderInfo refundOrder = selectOrderInfoByOrderId(refundId);
        resultMap.put("refundOrder",refundOrder);
        if (refundOrder == null || refundOrder.getPayOrderId() == null) {
            resultMap.put("payOrder",null);
            return resultMap;
        }
        Long payOrderId = refundOrder.getPayOrderId();
        //查询支付订单信息
        OrderInfo payOrder = selectOrderInfoByOrderId(payOrderId);
        resultMap.put("payOrder",payOrder);
        return resultMap;
    }

    @Override
    public void updateRefundCancelStatus(Long refundId) {
        //退费订单取消
        orderInfoMapper.updateRefundCancelStatus(refundId);
    }

//    @Override
//    public OrderInfo checkTeamChangePayOrder(String teamCode) {
//        //查询团队关联的已支付的订单，按时间排序
//        List<OrderInfo> orderInfoList = orderInfoMapper.selectOrderListByTeamCode(teamCode);
//        List<OrderGoodsRelation> orderGoodsRelations = orderGoodsRelationMapper.selectByCommodityId(teamCode);
//        //按顺序查找原始订单
//        for(OrderInfo orderinfo: orderInfoList){
//            OrderGoodsRelation orderGoodsRelation = orderGoodsRelations.stream().filter(relation -> relation.getOrderId().longValue() == orderinfo.getId().longValue()).findFirst().orElse(null);
//            String changeType = orderGoodsRelation.getChangeType();
//            //如果变更类型为空，说明没有做过变更，直接返回该订单
//            if (StringUtils.isEmpty(changeType)) {
//                return orderinfo;
//            }
//            //查询关联的订单，可能是支付，也可能是退费单
//            List<OrderInfo> relatedOrders = orderInfoMapper.selectOrderInfoByPayOrderId(orderinfo.getId());
//            //查询是否有进行中的订单(待支付/支付中/退费中/重缴退费中/审批中)
//            List<OrderInfo> workingOrder = relatedOrders.stream().filter(e -> e.getPayStatus().equals(PENDING) || e.getPayStatus().equals(PAYING) || e.getPayStatus().equals(REFUNDING) ||
//                    e.getPayStatus().equals(REPAY_REFUNDING) || e.getPayStatus().equals(APPROVING)).toList();
//            if (CollUtil.isEmpty(workingOrder)) {
//                return orderinfo;
//            }
//            //changeType有值，且不为空，说明流程已经结束。判断如果是退费重交，且是
//            if (REPAYMENT.equals(changeType)) {
//            }
//        };
//        return null;
//    }

    @Override
    public int checkTeamChangePayOrder(String teamCode) {
        //查询团队是否存在待支付订单
        OrderInfo orderInfo = orderInfoMapper.selectPendingOrderByTeamCode(teamCode);
//        List<OrderGoodsRelation> orderGoodsRelations = orderGoodsRelationMapper.selectByOrderId(orderInfo.getId());
//        Optional<OrderGoodsRelation> first = orderGoodsRelations.stream().filter(e -> e.getCommodityId().equals(teamCode)).findFirst();
//        if (first.isPresent()) {
//            OrderGoodsRelation orderGoodsRelation = first.get();
//            String changeType = orderGoodsRelation.getChangeType();
//            String payStatus = orderGoodsRelation.getPayStatus();
//            if (REPAYMENT.equals(changeType) && PENDING.equals(payStatus)) {
//                return 1;
//            }
//        }
        if(Objects.nonNull(orderInfo)){
            return 1;
        }
        return 0;
    }


}
