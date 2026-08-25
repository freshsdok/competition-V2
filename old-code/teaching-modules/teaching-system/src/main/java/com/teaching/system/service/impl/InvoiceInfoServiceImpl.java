package com.teaching.system.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.DictUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.*;
import com.teaching.system.config.InvoiceConfig;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.system.domain.InvoicePerInfo;
import com.teaching.system.domain.MerchantParamConfig;
import com.teaching.system.domain.vo.invoice.*;
import com.teaching.system.mapper.OrderInfoMapper;
import com.teaching.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.InvoiceInfoMapper;
import com.teaching.system.domain.InvoiceInfo;

import static com.teaching.common.core.constant.DictConstant.*;

/**
 * 发票信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-28
 */
@Slf4j
@Service
public class InvoiceInfoServiceImpl implements IInvoiceInfoService
{
    @Autowired
    private InvoiceInfoMapper invoiceInfoMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private InvoiceConfig invoiceConfig;

    @Autowired
    private IOrderInfoService orderInfoService;

    @Autowired
    private IMerchantParamConfigService merchantParamConfigService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ISysDictDataService sysDictDataService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IInvoicePerInfoService iInvoicePerInfoService;
    /**
     * 查询发票信息
     *
     * @param id 发票信息主键
     * @return 发票信息
     */
    @Override
    public InvoiceInfo selectInvoiceInfoById(Long id)
    {
        return invoiceInfoMapper.selectInvoiceInfoById(id);
    }

    @Override
    public InvoiceInfo selectPersonalInvoiceById(Long id) {
        InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoById(id);
        if (invoiceInfo == null) {
            throw new ServiceException("发票记录不存在");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if (!Objects.equals(currentUserId, invoiceInfo.getUserId())) {
            throw new ServiceException("无权访问该发票", 403);
        }
        return invoiceInfo;
    }

    private List<OrderInfo> requireCurrentUserOrders(List<Long> orderIds, boolean requirePaid) {
        if (CollUtil.isEmpty(orderIds)) {
            throw new ServiceException("请选择订单");
        }
        List<Long> distinctIds = orderIds.stream().filter(Objects::nonNull).distinct().toList();
        List<OrderInfo> orders = orderInfoService.selectOrderListByOrderIds(distinctIds);
        if (orders.size() != distinctIds.size()) {
            throw new ServiceException("订单不存在或已失效");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        boolean unauthorized = orders.stream().anyMatch(order -> !Objects.equals(currentUserId, order.getUserId()));
        if (unauthorized) {
            throw new ServiceException("无权操作所选订单", 403);
        }
        if (requirePaid && orders.stream().anyMatch(order -> !PAID.equals(order.getPayStatus()))) {
            throw new ServiceException("仅已支付订单可以申请开票");
        }
        if (requirePaid && orders.stream().anyMatch(order -> "1".equals(order.getInvoiceStatus()) || "2".equals(order.getInvoiceStatus()))) {
            throw new ServiceException("所选订单包含已开票或开票中的项目");
        }
        return orders;
    }

    private String invoiceIdempotencyKey(InvoiceApplyReq applyReq) {
        if (StringUtils.isEmpty(applyReq.getRandomId()) || applyReq.getRandomId().length() > 64) {
            throw new ServiceException("开票请求标识无效");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        String resource;
        if (CollUtil.isNotEmpty(applyReq.getOrderIds())) {
            String orderPart = applyReq.getOrderIds().stream().filter(Objects::nonNull).map(String::valueOf).sorted().collect(Collectors.joining(","));
            String userPart = Optional.ofNullable(applyReq.getUserIds()).orElse(Collections.emptyList()).stream()
                    .filter(Objects::nonNull).map(String::valueOf).sorted().collect(Collectors.joining(","));
            resource = orderPart + "|" + userPart;
        } else {
            resource = "legacy|" + applyReq.getId();
        }
        String businessKey = UUID.nameUUIDFromBytes(resource.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        return "personal:invoice:apply:" + currentUserId + ":" + businessKey;
    }

    /**
     * 查询发票信息列表
     *
     * @param invoiceInfo 发票信息
     * @return 发票信息
     */
    @Override
    public List<InvoiceInfo> selectInvoiceInfoList(InvoiceInfo invoiceInfo)
    {
        //筛选条件结束时间设置为当天的最后时间
        if (invoiceInfo != null && invoiceInfo.getApplyEndTime() != null) {
            invoiceInfo.setApplyEndTime(DateUtil.endOfDay(invoiceInfo.getApplyEndTime()));
        }
        List<InvoiceInfo> invoiceInfos = invoiceInfoMapper.selectInvoiceInfoList(invoiceInfo);
        if (CollUtil.isNotEmpty(invoiceInfos)) {
            invoiceInfos.forEach(e->{
                String invoiceContent = e.getInvoiceContent();
                if (ObjectUtil.isNotEmpty(invoiceContent)) {
                    String[] split = invoiceContent.split("&");
                    if (split.length > 1) {
                        String goodsLabel = sysDictDataService.selectDictLabel(INVOICE_GOODS_CODE, split[0]);
                        String feeTypeLabel = sysDictDataService.selectDictLabel(FEE_TYPE, split[1]);
                        e.setInvoiceContentName(goodsLabel + feeTypeLabel);
                    }
                }
            });
        }
        return invoiceInfos;
    }

    /**
     * 新增发票信息
     *
     * @param invoiceInfo 发票信息
     * @return 结果
     */
    @Override
    public int insertInvoiceInfo(InvoiceInfo invoiceInfo)
    {
        invoiceInfo.setCreateTime(DateUtils.getNowDate());
        return invoiceInfoMapper.insertInvoiceInfo(invoiceInfo);
    }

    /**
     * 修改发票信息
     *
     * @param invoiceInfo 发票信息
     * @return 结果
     */
    @Override
    public int updateInvoiceInfo(InvoiceInfo invoiceInfo)
    {
        invoiceInfo.setUpdateTime(DateUtils.getNowDate());
        return invoiceInfoMapper.updateInvoiceInfo(invoiceInfo);
    }

    /**
     * 批量删除发票信息
     *
     * @param ids 需要删除的发票信息主键
     * @return 结果
     */
    @Override
    public int deleteInvoiceInfoByIds(Long[] ids)
    {
        return invoiceInfoMapper.deleteInvoiceInfoByIds(ids);
    }

    /**
     * 删除发票信息信息
     *
     * @param id 发票信息主键
     * @return 结果
     */
    @Override
    public int deleteInvoiceInfoById(Long id)
    {
        return invoiceInfoMapper.deleteInvoiceInfoById(id);
    }

    @Override
    public AjaxResult invoiceApply(InvoiceApplyReq applyReq) {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(applyReq.getId());
        if (orderInfo == null) {
            return AjaxResult.error("订单不存在");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if (!Objects.equals(currentUserId, orderInfo.getUserId())) {
            throw new ServiceException("无权为该订单申请开票", 403);
        }
        if (!DictConstant.PAID.equals(orderInfo.getPayStatus()) ) {
            return AjaxResult.error("订单非已支付状态，不能开票！");
        }
        if ("1".equals(orderInfo.getInvoiceStatus()) || "2".equals(orderInfo.getInvoiceStatus())) {
            return AjaxResult.error("订单已开票或正在开票中");
        }
        String idempotencyKey = invoiceIdempotencyKey(applyReq);
        if (!Boolean.TRUE.equals(redisService.setIfAbsent(idempotencyKey, "PROCESSING", 2, java.util.concurrent.TimeUnit.MINUTES))) {
            return AjaxResult.error("开票申请正在处理中，请勿重复提交");
        }
        MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByMerId(orderInfo.getMerId());
        //构建请求参数
        InvoiceApplyData invoiceApplyData = buildAppDataReqParam(applyReq,orderInfo,merchantParamConfig);
        Map<String,InvoiceApplyData> reqMap = new HashMap<>();
        reqMap.put("order", invoiceApplyData);
        //构建发票记录内容
        String content = JSONUtil.toJsonStr(reqMap);

        //构建申请记录
        InvoiceInfo insertInfo = buildInvoiceInfo(applyReq,orderInfo,merchantParamConfig);


        /**
         * 本接口使用自用型应用获取令牌，使用配置的固定access_token即可
         */
        String response = invoiceConfig.requestApi(invoiceConfig.getAPPLY_METHOD(), content,merchantParamConfig.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),merchantParamConfig);
        System.out.println(response);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        if("E0000".equals(jsonObject.get("code"))){
            Object result = jsonObject.get("result");
            JSONObject resultJson = JSONUtil.parseObj(result);
            String invoiceSerialNum = resultJson.getStr("invoiceSerialNum");
            //交易流水号
            insertInfo.setInvoiceSerialNum(invoiceSerialNum);
            //插入记录数据
            invoiceInfoMapper.insertInvoiceInfo(insertInfo);
            redisService.setCacheObject(idempotencyKey, "SUCCESS", 24L, java.util.concurrent.TimeUnit.HOURS);
            return AjaxResult.success("开票申请成功");
        }else{
            redisService.deleteObject(idempotencyKey);
            return AjaxResult.error(jsonObject.getStr("describe"));
        }
    }

    @Override
    public AjaxResult invoiceApplyNew(List<InvoiceApplyReq> applyReqList) {
        if (CollUtil.isEmpty(applyReqList)) {
            return AjaxResult.error("请选择需要开票的项目");
        }
        StringBuilder errorMsg = new StringBuilder();
        for (InvoiceApplyReq applyReq : applyReqList) {
            String idempotencyKey = null;
            boolean guardAcquired = false;
            try{
                idempotencyKey = invoiceIdempotencyKey(applyReq);
                String cachedStatus = redisService.getCacheObject(idempotencyKey);
                if ("SUCCESS".equals(cachedStatus)) {
                    continue;
                }
                List<OrderInfo> personalOrders = requireCurrentUserOrders(applyReq.getOrderIds(), true);
                List<String> merchantIds = personalOrders.stream().map(OrderInfo::getMerId).distinct().toList();
                if (merchantIds.size() != 1 || !Objects.equals(applyReq.getMerId(), merchantIds.get(0))) {
                    throw new ServiceException("收款单位与所选订单不一致");
                }
                String commodityType = personalOrders.get(0).getCommodityType();
                if (personalOrders.stream().anyMatch(order -> !Objects.equals(commodityType, order.getCommodityType()))) {
                    throw new ServiceException("不同业务类型不能合并开票");
                }
                applyReq.setCommodityType(commodityType);
                applyReq.setInvoiceAmount(validateAndCalculateInvoiceSubjects(personalOrders, applyReq.getUserIds(), commodityType));
                if (!Boolean.TRUE.equals(redisService.setIfAbsent(idempotencyKey, "PROCESSING", 2, java.util.concurrent.TimeUnit.MINUTES))) {
                    throw new ServiceException("开票申请正在处理中，请勿重复提交");
                }
                guardAcquired = true;
                MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByMerId(applyReq.getMerId());
                //生成一个虚拟orderId-由前端带过来
                String orderId = applyReq.getRandomId();
                //构建请求参数
                InvoiceApplyData invoiceApplyData = buildAppDataReqParamNew(applyReq,merchantParamConfig);
                Map<String,InvoiceApplyData> reqMap = new HashMap<>();
                reqMap.put("order", invoiceApplyData);
                //构建发票记录内容
                String content = JSONUtil.toJsonStr(reqMap);

                //构建申请记录
                InvoiceInfo insertInfo = buildInvoiceInfoNew(applyReq,merchantParamConfig);
                /**
                 * 本接口使用自用型应用获取令牌，使用配置的固定access_token即可
                 */
                String response = invoiceConfig.requestApi(invoiceConfig.getAPPLY_METHOD(), content,merchantParamConfig.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),merchantParamConfig);
                System.out.println(response);
                JSONObject jsonObject = JSONUtil.parseObj(response);
                if("E0000".equals(jsonObject.get("code"))){
                    Object result = jsonObject.get("result");
                    JSONObject resultJson = JSONUtil.parseObj(result);
                    String invoiceSerialNum = resultJson.getStr("invoiceSerialNum");
                    //交易流水号
                    insertInfo.setInvoiceSerialNum(invoiceSerialNum);
                    //插入记录数据
                    int i = invoiceInfoMapper.insertInvoiceInfo(insertInfo);
                    if (i <= 0) {
                        throw new ServiceException("保存开票记录失败");
                    }
                    // 外部开票已成功并已落库，先固化幂等结果，避免后续状态同步异常造成重复开票。
                    redisService.setCacheObject(idempotencyKey, "SUCCESS", 24L, java.util.concurrent.TimeUnit.HOURS);
                    guardAcquired = false;
                    try {
                        //将关联人员和订单id集合存放到缓存中
                        redisService.setCacheList("invoice_apply_user:"+orderId,applyReq.getUserIds());
                        redisService.setCacheList("invoice_apply_order:"+orderId,applyReq.getOrderIds());
                        //更新用户开票状态为开票中
                        if("competition".equals(applyReq.getCommodityType())){
                            pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(orderId,"2");
                        }
                        if("cert".equals(applyReq.getCommodityType())){
                            updateCertOrderInvoiceStatus(orderId,"2");
                        }
                    } catch (Exception syncException) {
                        log.error("开票已成功，但关联状态同步失败，orderId:{}，错误信息：{}", orderId, syncException.getMessage());
                    }
                }else{
                    log.error("开票异常-orderId:{}，错误信息：{}",orderId, jsonObject.getStr("describe"));
                    throw new ServiceException(jsonObject.getStr("describe"));
                }
            }catch (Exception e){
                if (guardAcquired && idempotencyKey != null && !"SUCCESS".equals(redisService.getCacheObject(idempotencyKey))) {
                    redisService.deleteObject(idempotencyKey);
                }
                log.error("收款公司{}开票异常，关联订单orderId:{}，错误信息：{}",applyReq.getMerId(),applyReq.getOrderIds(), e.getMessage());
                errorMsg.append("收款公司"+applyReq.getMerId()+"开票异常，错误信息："+e.getMessage()+"\n");
            }
        }
        if (errorMsg.toString().isEmpty()) {
            return AjaxResult.success("开票申请成功");
        }else{
            return AjaxResult.error(errorMsg.toString());
        }
    }

    /**
     * 所选开票人员必须属于当前账号拥有的订单；开票金额始终由服务端重算。
     */
    private BigDecimal validateAndCalculateInvoiceSubjects(List<OrderInfo> orders, List<Long> requestedUserIds, String commodityType) {
        if (CollUtil.isEmpty(requestedUserIds)) {
            throw new ServiceException("请选择需要开票的项目");
        }
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        if ("cert".equals(commodityType)) {
            if (requestedUserIds.stream().anyMatch(userId -> !Objects.equals(currentUserId, userId))) {
                throw new ServiceException("证书开票申请人与当前账号不一致", 403);
            }
            return orders.stream().map(OrderInfo::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (!"competition".equals(commodityType)) {
            throw new ServiceException("暂不支持该业务类型开票");
        }
        List<Long> memberIds = requestedUserIds.stream().filter(Objects::nonNull).distinct().toList();
        CompetitionApplyInfo request = new CompetitionApplyInfo();
        request.setMemberIds(CollUtil.join(memberIds, ","));
        R<List<CompetitionApplyInfo>> response = competitionService.queryTeamMemberInvoiceStatus(request, SecurityConstants.INNER);
        if (!R.isSuccess(response) || CollUtil.isEmpty(response.getData())) {
            throw new ServiceException("查询开票人员信息失败");
        }
        Map<Long, CompetitionApplyInfo> memberMap = response.getData().stream()
                .filter(member -> member.getMemberId() != null)
                .collect(Collectors.toMap(CompetitionApplyInfo::getMemberId, member -> member, (left, right) -> left));
        Set<String> allowedTeamCodes = orders.stream()
                .map(OrderInfo::getCommodityId)
                .filter(StringUtils::isNotEmpty)
                .flatMap(value -> Arrays.stream(value.split(",")))
                .collect(Collectors.toSet());
        BigDecimal total = BigDecimal.ZERO;
        for (Long memberId : memberIds) {
            CompetitionApplyInfo member = memberMap.get(memberId);
            if (member == null || !allowedTeamCodes.contains(member.getTeamCode())) {
                throw new ServiceException("所选开票人员不属于当前订单", 403);
            }
            if (!"0".equals(member.getInvoiceStatus())) {
                throw new ServiceException("所选人员已开票或正在开票中");
            }
            if (StringUtils.isNotEmpty(member.getFee())) {
                total = total.add(new BigDecimal(member.getFee()));
            }
        }
        return total;
    }


    private InvoiceInfo buildInvoiceInfo(InvoiceApplyReq applyReq, OrderInfo orderInfo,MerchantParamConfig config) {
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.setOrderId(orderInfo.getOrderId());
        invoiceInfo.setInvoiceType(applyReq.getInvoiceType());  //蓝票=支付类型，红票=退款类型
        invoiceInfo.setBuyerName(applyReq.getBuyerName()); //买方姓名/企业名称
        invoiceInfo.setBuyerTaxNum(applyReq.getBuyerTaxNumber()); //买方税号
        invoiceInfo.setBankAccount(config.getAccount());
        invoiceInfo.setUserId(orderInfo.getUserId());
        invoiceInfo.setOrgId(orderInfo.getOrgId());
        invoiceInfo.setSalerTaxNum(config.getTaxNum()); //卖方税号
//        invoiceInfo.setSalerAddress(config.getAddress());
        invoiceInfo.setInvoicDate(orderInfo.getCreateTime());
        invoiceInfo.setClerk(config.getClerk());
        invoiceInfo.setBuyerPhone(applyReq.getPhone());
        invoiceInfo.setBuyerEmail(applyReq.getEmail());
        invoiceInfo.setInvoiceClass(applyReq.getInvoiceClass());
        invoiceInfo.setCallBackUrl(invoiceConfig.getCALLBACK());
        invoiceInfo.setCreateTime(new Date());
        invoiceInfo.setRemark(applyReq.getRemark());
        invoiceInfo.setIssuedStatus(INVOICE_PENDING); //待开票
        invoiceInfo.setAmount(orderInfo.getAmount()); //开票金额
        return invoiceInfo;
    }

    private InvoiceInfo buildInvoiceInfoNew(InvoiceApplyReq applyReq,MerchantParamConfig config) {
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.setOrderId(applyReq.getRandomId());
        invoiceInfo.setInvoiceType(applyReq.getInvoiceType());  //蓝票=支付类型，红票=退款类型
        invoiceInfo.setBuyerName(applyReq.getBuyerName()); //买方姓名/企业名称
        invoiceInfo.setBuyerTaxNum(applyReq.getBuyerTaxNumber()); //买方税号
        invoiceInfo.setBankAccount(config.getAccount());
        invoiceInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        invoiceInfo.setOrgId(SecurityUtils.getLoginUser().getSysUser().getOrgId());
        invoiceInfo.setSalerTaxNum(config.getTaxNum()); //卖方税号
//        invoiceInfo.setSalerAddress(config.getAddress());
        invoiceInfo.setInvoicDate(new Date());
        invoiceInfo.setClerk(config.getClerk());
        invoiceInfo.setBuyerPhone(applyReq.getPhone());
        invoiceInfo.setBuyerEmail(applyReq.getEmail());
        invoiceInfo.setInvoiceClass(applyReq.getInvoiceClass());
        invoiceInfo.setCallBackUrl(invoiceConfig.getCALLBACK());
        invoiceInfo.setCreateTime(new Date());
        invoiceInfo.setRemark(applyReq.getRemark());
        invoiceInfo.setIssuedStatus(INVOICE_PENDING); //待开票
        invoiceInfo.setAmount(applyReq.getInvoiceAmount()); //开票金额
        invoiceInfo.setInvoiceContent(applyReq.getGoodsCode());
        return invoiceInfo;
    }

    /**
     * 构建发票申请数据
     * @param orderInfo
     * @return
     */
    InvoiceApplyData buildAppDataReqParam(InvoiceApplyReq applyReq,OrderInfo orderInfo,MerchantParamConfig config){
        InvoiceApplyData applyData = new InvoiceApplyData();
        applyData.setBuyerName(applyReq.getBuyerName());        //买方姓名/企业名称
        applyData.setBuyerTaxNum(applyReq.getBuyerTaxNumber()); //买方税号
        //判断是否有填写邮箱，有就设置推送，没有不推送
        if (StringUtil.isNotEmpty(applyReq.getEmail())) {
            applyData.setEmail(applyReq.getEmail());                //邮箱
            applyData.setPushMode("0");                              //推送方式：-1,不推送;0,邮箱;1,手机（默认）;2,邮箱、手机
        }else {
            applyData.setPushMode("-1");                              //推送方式：-1,不推送
        }
        applyData.setBuyerPhone(applyReq.getPhone());           //买方手机
        applyData.setSalerTaxNum(config.getTaxNum());    //卖方税号
        //applyData.setSalerTel(); //销方电话（在诺税通saas工作台配置过的可以不传，以传入的为准）
        //applyData.setSalerAddress(config.getAddress()); //销方地址（在诺税通saas工作台配置过的可以不传，以传入的为准）
        applyData.setSalerAccount(config.getBank() + " " + config.getAccount()); //销方银行开户行及账号
        applyData.setOrderNo(orderInfo.getOrderId());  //订单号
        applyData.setInvoiceDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, orderInfo.getCreateTime())); //订单时间
        //applyData.setInvoiceCode();   //冲红时填写的对应蓝票发票代码（红票必填 10位或12 位， 11位的时候请左补 0）
        //applyData.setInvoiceNum();    //冲红时填写的对应蓝票发票号码（红票必填，不满8位请左补0）
        //applyData.setRedReason();       //冲红原因
        applyData.setRemark(applyReq.getRemark());          //冲红时填写
        applyData.setClerk(config.getClerk()); //开票员
        applyData.setChecker(config.getChecker()); //复核员
        applyData.setInvoiceType(applyReq.getInvoiceType()); //付费开票对应蓝票，退费开票对应红票
        applyData.setInvoiceLine(StringUtil.isEmpty(applyReq.getInvoiceLine()) ? "pc" : applyReq.getInvoiceLine());     //发票种类，p-普通发票(电票)  pc:电子发票(普通发票)-即数电普票(电子)-使用pc
        applyData.setCallBackUrl(invoiceConfig.getCALLBACK()); //开票成功后回调地址
        applyData.setInvoiceDetail(buildInvoiceDetailList(orderInfo,applyReq,config));
        applyData.setExtensionNumber(config.getExtension());      //分机号
        return applyData;
    }

    /**
     * 构建发票申请数据
     * @param
     * @return
     */
    InvoiceApplyData buildAppDataReqParamNew(InvoiceApplyReq applyReq,MerchantParamConfig config){
        InvoiceApplyData applyData = new InvoiceApplyData();
        applyData.setBuyerName(applyReq.getBuyerName());        //买方姓名/企业名称
        applyData.setBuyerTaxNum(applyReq.getBuyerTaxNumber()); //买方税号
        //判断是否有填写邮箱，有就设置推送，没有不推送
        if (StringUtil.isNotEmpty(applyReq.getEmail())) {
            applyData.setEmail(applyReq.getEmail());                //邮箱
            applyData.setPushMode("0");                              //推送方式：-1,不推送;0,邮箱;1,手机（默认）;2,邮箱、手机
        }else {
            applyData.setPushMode("-1");                              //推送方式：-1,不推送
        }
        applyData.setBuyerPhone(applyReq.getPhone());           //买方手机
        applyData.setSalerTaxNum(config.getTaxNum());    //卖方税号
        //applyData.setSalerTel(); //销方电话（在诺税通saas工作台配置过的可以不传，以传入的为准）
        //applyData.setSalerAddress(config.getAddress()); //销方地址（在诺税通saas工作台配置过的可以不传，以传入的为准）
        applyData.setSalerAccount(config.getBank() + " " + config.getAccount()); //销方银行开户行及账号
        applyData.setOrderNo(applyReq.getRandomId());  //订单号
        applyData.setInvoiceDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, new Date())); //订单时间
        //applyData.setInvoiceCode();   //冲红时填写的对应蓝票发票代码（红票必填 10位或12 位， 11位的时候请左补 0）
        //applyData.setInvoiceNum();    //冲红时填写的对应蓝票发票号码（红票必填，不满8位请左补0）
        //applyData.setRedReason();       //冲红原因
        applyData.setRemark(applyReq.getRemark());          //冲红时填写
        applyData.setClerk(config.getClerk()); //开票员
        applyData.setChecker(config.getChecker()); //复核员
        applyData.setInvoiceType(applyReq.getInvoiceType()); //付费开票对应蓝票，退费开票对应红票
        applyData.setInvoiceLine(StringUtil.isEmpty(applyReq.getInvoiceLine()) ? "pc" : applyReq.getInvoiceLine());     //发票种类，p-普通发票(电票)  pc:电子发票(普通发票)-即数电普票(电子)-使用pc
        applyData.setCallBackUrl(invoiceConfig.getCALLBACK()); //开票成功后回调地址
        applyData.setInvoiceDetail(buildInvoiceDetailListNew(applyReq,config));
        applyData.setExtensionNumber(config.getExtension());      //分机号
        return applyData;
    }

    /**
     * 构建发票明细
     * @param orderInfo 订单信息
     * @return
     */
    private List<InvoiceDetail> buildInvoiceDetailList(OrderInfo orderInfo,InvoiceApplyReq applyReq,MerchantParamConfig config) {

        List<InvoiceDetail> details = new ArrayList<>();
        orderInfoService.getAndSetOderDetail(orderInfo,false);
        List<CompetitionApplyInfoVO> competitionList = orderInfo.getCompetitionList();
        List<NumDetail> numDetailList = orderInfo.getNumDetailList();
        //查询字典配置的商品单位值
        List<SysDictData> commodityUnit = DictUtils.getDictCache(COMMODITY_UNIT);
        if (CollUtil.isNotEmpty(commodityUnit)) {
            commodityUnit.forEach(sysDictData -> {
                if (COMPETITION.equals(sysDictData.getDictLabel())) {
                    applyReq.setCommodityUnit(sysDictData.getDictValue());
                }
            });
        }
        if (CollUtil.isNotEmpty(numDetailList)) {
            numDetailList.forEach((numDetail)->{
                InvoiceDetail detail = new InvoiceDetail();
                detail.setGoodsName("报名费");                                                                                  //商品名称
                detail.setGoodsCode(applyReq.getGoodsCode());                                                                  //商品编码，商品种类的编号 3049900000000000000-现代服务
                detail.setWithTaxFlag("1");                                                                                    //含税标志
                detail.setPrice(numDetail.getPrice());                                                                          //单价
                detail.setNum(numDetail.getNum().toString());                                                                   //数量
                detail.setUnit(StringUtil.isEmpty(applyReq.getCommodityUnit()) ? "个" : applyReq.getCommodityUnit());            //单位
                detail.setTaxRate(config.getTaxRate().toString());                                                                  //税率
                details.add(detail);
            });
        }
        return details;
    }
    /**
     * 构建发票明细
     * @param
     * @return
     */
    private List<InvoiceDetail> buildInvoiceDetailListNew(InvoiceApplyReq applyReq,MerchantParamConfig config) {

        List<InvoiceDetail> details = new ArrayList<>();
        List<Long> userIds = applyReq.getUserIds();
        List<CompetitionApplyInfo> applyInfoList = new ArrayList<>();
        if("competition".equals(applyReq.getCommodityType())){
            CompetitionApplyInfo memberRela = new CompetitionApplyInfo();
            memberRela.setMemberIds(CollUtil.join(userIds,","));
            R<List<CompetitionApplyInfo>> listR = competitionService.queryTeamMemberInvoiceStatus(memberRela, SecurityConstants.INNER);
            if (R.FAIL == listR.getCode()) {
                throw new ServiceException("获取用户信息失败！");
            }
            applyInfoList.addAll(listR.getData());
        }
        List<CompetitionCertExchangeApply> certExchangeApplyList = new ArrayList<>();
        if("cert".equals(applyReq.getCommodityType())){
            List<OrderInfo> orderInfoList = orderInfoMapper.selectOrderListByOrderIds(applyReq.getOrderIds());
            if(CollectionUtils.isNotEmpty(orderInfoList)){
                orderInfoList.stream().forEach(orderInfo -> {
                    CompetitionCertExchangeApply certExchangeApply = com.alibaba.fastjson2.JSONObject.parseObject(orderInfo.getTeamInfoList(), CompetitionCertExchangeApply.class);
                    certExchangeApplyList.add(certExchangeApply);
                });
            }

        }
        //查询字典配置的商品单位值
        List<SysDictData> commodityUnit = DictUtils.getDictCache(COMMODITY_UNIT);
        if (CollUtil.isNotEmpty(commodityUnit)) {
            commodityUnit.forEach(sysDictData -> {
                if (COMPETITION.equals(sysDictData.getDictLabel())) {
                    applyReq.setCommodityUnit(sysDictData.getDictValue());
                }
            });
        }
        if(CollectionUtils.isNotEmpty(applyInfoList)){
            List<String> feeList = applyInfoList.stream().map(CompetitionApplyInfo::getFee).distinct().toList();
            String goodsCode = applyReq.getGoodsCode();
            String[] split = goodsCode.split("&");
            String label = sysDictDataService.selectDictLabel(FEE_TYPE, split[1]);
            if (CollUtil.isNotEmpty(feeList)) {
                feeList.forEach(fee->{
                    List<CompetitionApplyInfo> list = applyInfoList.stream().filter(user -> fee.equals(user.getFee())).toList();
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setGoodsName(label);                                                                                  //商品名称
                    detail.setGoodsCode(split[0]);                                                                  //商品编码，商品种类的编号 3049900000000000000-现代服务
                    detail.setWithTaxFlag("1");  //含税标志
                    detail.setPrice(fee);                                                                          //单价
                    detail.setNum(list.size() + "");                                                                   //数量
                    detail.setUnit(StringUtil.isEmpty(applyReq.getCommodityUnit()) ? "个" : applyReq.getCommodityUnit());            //单位
                    detail.setTaxRate(config.getTaxRate().toString());                                                                  //税率
                    details.add(detail);
                });
            }
        }
        if(CollectionUtils.isNotEmpty(certExchangeApplyList)){
            List<String> feeList = certExchangeApplyList.stream().map(CompetitionCertExchangeApply::getRepayAmount).distinct().toList();
            String goodsCode = applyReq.getGoodsCode();
            String[] split = goodsCode.split("&");
            String label = sysDictDataService.selectDictLabel(FEE_TYPE, split[1]);
            if (CollUtil.isNotEmpty(feeList)) {
                feeList.forEach(fee->{
                    List<CompetitionCertExchangeApply> list = certExchangeApplyList.stream().filter(user -> fee.equals(user.getRepayAmount())).toList();
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setGoodsName(label);                                                                                  //商品名称
                    detail.setGoodsCode(split[0]);                                                                  //商品编码，商品种类的编号 3049900000000000000-现代服务
                    detail.setWithTaxFlag("1");  //含税标志
                    detail.setPrice(fee);                                                                          //单价
                    detail.setNum(list.size() + "");                                                                   //数量
                    detail.setUnit(StringUtil.isEmpty(applyReq.getCommodityUnit()) ? "个" : applyReq.getCommodityUnit());            //单位
                    detail.setTaxRate(config.getTaxRate().toString());                                                                  //税率
                    details.add(detail);
                });
            }
        }
        return details;
    }

    @Override
    public Map<String,String> applyCallback(Map<String, String> notifyMap) {
        Map<String,String> resultMap = new HashMap<>();

        String content = notifyMap.get("content");  //返回结果内容
        String orderId = notifyMap.get("orderno");  //订单号
        //根据订单号查询发票申请记录
        InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoByOrderId(orderId);
        if (invoiceInfo == null) {
            resultMap.put("status", "0001");
            resultMap.put("message", "订单号不存在");
            return resultMap;
        }
        //返回结果内容转换成java类
        ApplyCallBackContent resp = JSON.parseObject(content, ApplyCallBackContent.class);
        /**
         * 发票状态（1：开票完成、2：开票失败、3：开票成功签章失败（电票时））；  注：1、企业资质-开票失败是否回调 为“是” 时返回该字段；
         * 2、（开票失败、开票成功签章失败时 ） content中只返回：发票流水号、发票状态、销方税号、失败原因 四个字段
         */
        String cStatus = resp.getCStatus();
        if (!INVOICE_SUCCESS.equals(cStatus)) {
            //开票失败，更新发票状态
            invoiceInfo.setIssuedStatus(INVOICE_FAILED);
            invoiceInfo.setFailReason(resp.getCErrorMessage());   //失败原因
            invoiceInfoMapper.updateInvoiceInfo(invoiceInfo);
            // 区分报名还是证书
            //开票失败，开票用户开票状态回滚为0
            if(isInvoiceStatus(orderId)){
                updateCertOrderInvoiceStatus(orderId,"0");
            } else {
                pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(orderId,"0");
            }
        }else{
            //开票成功，更新订单发票状态
            invoiceInfo.setIssuedStatus(INVOICE_SUCCESS);
            invoiceInfo.setInvoiceNum(resp.getCFphm()); //发票号码
            invoiceInfo.setInvoiceCode(resp.getCFpdm()); //发票代码
            invoiceInfo.setIssuedTime(resp.getCKprq()); //开票日期
            invoiceInfo.setcUrl(resp.getCUrl()); //发票PDF地址
            invoiceInfo.setcJpgUrl(resp.getCJpgUrl()); //发票详情地址
            int i = invoiceInfoMapper.updateInvoiceInfo(invoiceInfo);
            if (i > 0) {
                // 区分报名还是证书
                if(isInvoiceStatus(orderId)){
                    updateCertOrderInvoiceStatus(orderId,"1");
                } else {
                    pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(orderId,"1");
                }
                //开票成功，将用户开票的抬头进行记录
                insertInvoicePerInfo(invoiceInfo);
            }
        }
        resultMap.put("status", "0000");
        resultMap.put("message", "同步成功");
        return resultMap;
    }

    // true是证书 false是报名
    public boolean isInvoiceStatus(String orderId) {
        boolean commodityTypeIsCert = true;
        List<Long> orderIds = redisService.getCacheList("invoice_apply_order:" + orderId);
        if(CollectionUtils.isNotEmpty(orderIds)){
            List<OrderInfo> orderInfos = orderInfoMapper.selectOrderListByOrderIds(orderIds);
            List<String> commodityTypes = orderInfos.stream().map(OrderInfo::getCommodityType).toList();
            if(commodityTypes.stream().allMatch(ct -> ct.equals("cert"))){
            } else {
                commodityTypeIsCert = false;
            }
        }
        return commodityTypeIsCert;
    }

    //开票成功，将用户开票的抬头进行记录
    private void insertInvoicePerInfo(InvoiceInfo invoiceInfo) {
        try {
            //发票类型为个人时不需要记录，只记录企业
            if (INVOICE_CLASS_PERSONAL.equals(invoiceInfo.getInvoiceClass())) {
                return;
            }
            //校验该抬头之前有没有创建过
            InvoicePerInfo checkInfo = new InvoicePerInfo();
            checkInfo.setEnterpriseName(invoiceInfo.getBuyerName());
            checkInfo.setTaxpayerIdentificationNumber(invoiceInfo.getBuyerTaxNum());
            checkInfo.setUserId(invoiceInfo.getUserId().toString());
            List<InvoicePerInfo> invoicePerInfos = iInvoicePerInfoService.selectInvoicePerInfoList(checkInfo);
            //如果查询数据不为空，说明该用户已经有了这个抬头信息，不用重复添加
            if (CollUtil.isNotEmpty(invoicePerInfos)) {
                return;
            }
            //插入抬头信息
            InvoicePerInfo invoicePerInfo = new InvoicePerInfo();
            invoicePerInfo.setUserId(invoiceInfo.getUserId() == null ? null : invoiceInfo.getUserId().toString());
            invoicePerInfo.setEnterpriseName(invoiceInfo.getBuyerName());
            invoicePerInfo.setTaxpayerIdentificationNumber(invoiceInfo.getBuyerTaxNum());
            invoiceInfo.setBuyerEmail(invoiceInfo.getBuyerEmail());
            iInvoicePerInfoService.insertInvoicePerInfo(invoicePerInfo);
        }catch (Exception e){
            log.info("添加抬头信息失败：" + e.getMessage());
        }
    }

    // 更新证书发票状态
    public void updateCertOrderInvoiceStatus(String orderId,String status) {
        // 证书发票状态更新
        //从redis中获取人员id
        List<Long> cacheList = redisService.getCacheList("invoice_apply_user:" + orderId);
        //从redis中取出关联orderId
        List<Long> orderIds = redisService.getCacheList("invoice_apply_order:" + orderId);
        if (CollUtil.isNotEmpty(cacheList) && CollUtil.isNotEmpty(orderIds)) {
            List<CompetitionCertExchangeApply> certExchangeApplyList = new ArrayList<>();
            cacheList.forEach(userId->{
                orderIds.stream().forEach(orderIdCache->{
                    CompetitionCertExchangeApply certExchangeApply = new CompetitionCertExchangeApply();
                    certExchangeApply.setUserId(userId);
                    certExchangeApply.setOrderId(orderIdCache);
                    certExchangeApply.setInvoiceStatus(status);
                    certExchangeApplyList.add(certExchangeApply);
                });
            });
            R<Integer> integerR = competitionService.updateUserCertExchangeApplyInvoiceStatus(certExchangeApplyList, SecurityConstants.INNER);
            if (integerR.getCode() == 200) {
                orderInfoMapper.selectOrderListByOrderIds(orderIds).forEach(orderInfo -> {
                    orderInfo.setInvoiceStatus("1");
                    orderInfoMapper.updateOrderInfo(orderInfo);
                });
            }
        }
    }

    public void pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(String orderId,String status) {
        //从redis中获取人员id
        List<Long> cacheList = redisService.getCacheList("invoice_apply_user:" + orderId);
        //更新用户发票状态,并判断关联订单是否已全部开票完成，更新订单里面的状态
        if (CollUtil.isNotEmpty(cacheList)) {
            List<CompetitionApplyInfo> memberRelas = new ArrayList<>();
            cacheList.forEach(userId->{
                CompetitionApplyInfo teamMemberRela = new CompetitionApplyInfo();
                teamMemberRela.setMemberId(userId);
                teamMemberRela.setInvoiceStatus(status);
                memberRelas.add(teamMemberRela);
            });
            log.info("开票完成，推送开票关联用户id入参：{}",cacheList);
            R<Integer> integerR = competitionService.updatePayStatus(memberRelas, SecurityConstants.INNER);
            //用户状态更新成功后，判断关联订单下的人员是否都已开票，开票完成，跟新订单的开票状态
            if (integerR.getCode() == 200) {
                //从redis中取出关联orderId
                List<Long> orderIds = redisService.getCacheList("invoice_apply_order:" + orderId);
                log.info("关联订单id：{}",orderIds);
                if (CollUtil.isNotEmpty(orderIds)) {
                    orderInfoMapper.selectOrderListByOrderIds(orderIds).forEach(orderInfo -> {
                        String teamCodes = orderInfo.getCommodityId();
                        CompetitionApplyInfo memberRela = new CompetitionApplyInfo();
                        memberRela.setTeamCodes(teamCodes);
                        R<List<CompetitionApplyInfo>> listR = competitionService.queryTeamMemberInvoiceStatus(memberRela, SecurityConstants.INNER);
                        if (listR.getCode() == 200 && CollUtil.isNotEmpty(listR.getData())) {
                            List<CompetitionApplyInfo> data = listR.getData();
                            //过滤指导老师（知道老师没有开票状态）
                            List<CompetitionApplyInfo> filterData = data.stream().filter(e -> !"指导教师".equals(e.getCompetitionRoleName())).toList();
                            //如果订单下所有人都已开票，(1-已开票，2-开票中)
                            if (filterData.stream().allMatch(teamMemberRela1 -> "1,2".contains(teamMemberRela1.getInvoiceStatus()))) {
                                //订单开票状态改为已开票
                                orderInfo.setInvoiceStatus("1");
                                orderInfoMapper.updateOrderInfo(orderInfo);
                                log.info("订单{}下的用户已全部开票完成，更新开票状态为1",orderInfo.getId());
                            }else {
                                //订单开票状态改为开票
                                orderInfo.setInvoiceStatus("0");
                                orderInfoMapper.updateOrderInfo(orderInfo);
                                log.info("订单{}下的用户未全部开票完成，更新开票状态为0",orderInfo.getId());
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public AjaxResult deliveryInvoice(Long id) {
        InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoById(id);
        MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByTaxNum(invoiceInfo.getSalerTaxNum());
        //构建请求参数
        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("taxnum", invoiceInfo.getSalerTaxNum());  //销方税号
        requestParam.put("invoiceNum", invoiceInfo.getInvoiceNum());  //发票号码
        requestParam.put("invoiceCode", invoiceInfo.getInvoiceCode());  //发票代码
//        requestParam.put("phone", invoiceInfo.getBuyerPhone());  //交付手机号
        requestParam.put("email", invoiceInfo.getBuyerEmail());  //交付邮箱
        //构建发票记录内容
        String content = JSONUtil.toJsonStr(requestParam);
        log.info("发票交付请求参数：{}",content);
        /**
         * 本接口使用自用型应用获取令牌，使用配置的固定access_token即可
         */
        String response = invoiceConfig.requestApi(invoiceConfig.getDeliveryMethod(), content,merchantParamConfig.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),merchantParamConfig);
        log.info("发票交付响应参数：{}",response);
        System.out.println(response);
        Map<String,String> respMap = JSON.parseObject(response, Map.class);
        if (!"E0000".equals(respMap.get("code"))) {
            return AjaxResult.error(respMap.get("describe"));
        }
        return AjaxResult.success("交付成功");
    }

    @Override
    public AjaxResult queryPersonalInvoiceResult(InvoiceQueryReq invoiceQueryReq) {
        if (invoiceQueryReq == null
                || (CollUtil.isEmpty(invoiceQueryReq.getOrderNos()) && CollUtil.isEmpty(invoiceQueryReq.getSerialNos()))) {
            return AjaxResult.error("请选择发票记录");
        }
        InvoiceInfo filter = new InvoiceInfo();
        filter.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        List<InvoiceInfo> personalInvoices = invoiceInfoMapper.selectInvoiceInfoList(filter);
        Set<String> ownedOrderNos = personalInvoices.stream()
                .map(InvoiceInfo::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> ownedSerialNos = personalInvoices.stream()
                .map(InvoiceInfo::getInvoiceSerialNum).filter(Objects::nonNull).collect(Collectors.toSet());
        boolean unauthorizedOrder = CollUtil.isNotEmpty(invoiceQueryReq.getOrderNos())
                && invoiceQueryReq.getOrderNos().stream().anyMatch(orderNo -> !ownedOrderNos.contains(orderNo));
        boolean unauthorizedSerial = CollUtil.isNotEmpty(invoiceQueryReq.getSerialNos())
                && invoiceQueryReq.getSerialNos().stream().anyMatch(serialNo -> !ownedSerialNos.contains(serialNo));
        if (unauthorizedOrder || unauthorizedSerial) {
            throw new ServiceException("无权更新该发票状态", 403);
        }
        return queryInvoiceResult(invoiceQueryReq);
    }

    @Override
    public AjaxResult queryInvoiceResult(InvoiceQueryReq invoiceQueryReq) {
        InvoiceInfo invoiceInfoResult = null;
        List<String> orderNos = invoiceQueryReq.getOrderNos();
        List<String> serialNos = invoiceQueryReq.getSerialNos();
        if (CollUtil.isNotEmpty(orderNos)) {
            invoiceInfoResult = invoiceInfoMapper.selectInvoiceInfoByOrderId(orderNos.get(0));
        }
        if (invoiceInfoResult == null && CollUtil.isNotEmpty(serialNos)) {
            invoiceInfoResult = invoiceInfoMapper.selectInvoiceInfoBySerialNo(serialNos.get(0));
        }
        if (invoiceInfoResult == null) {
            return AjaxResult.error("未查询到发票信息");
        }
        MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByTaxNum(invoiceInfoResult.getSalerTaxNum());
        String content = JSONUtil.toJsonStr(invoiceQueryReq);
        log.info("开票结果查询请求参数：{}",content);
        String response = invoiceConfig.requestApi(invoiceConfig.getQueryMethod(), content,merchantParamConfig.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),merchantParamConfig);
        log.info("开票结果查询响应参数：{}",response);
        if (StringUtil.isEmpty(response)) {
            return AjaxResult.error("请求失败");
        }
        System.out.println(response);
        Map<String,String> respMap = JSON.parseObject(response, Map.class);
        if (!"E0000".equals(respMap.get("code"))) {
            return AjaxResult.error(respMap.get("describe"));
        }
        Object result = respMap.get("result");
        //将result转换成List<InvoiceQueryResult>
        List<InvoiceQueryResult> list = JSON.parseArray(result.toString(), InvoiceQueryResult.class);
        if (CollUtil.isEmpty(list)) {
            return AjaxResult.error("未查询到发票信息");
        }
        for (InvoiceQueryResult resp : list) {
            /**
             * 发票状态： 2 :开票完成（ 最终状 态），其他状态
             * 分别为: 20:开票中; 21:开票成功签章中;22:开票失
             * 败;24: 开票成功签章失败;3:发票已作废 31: 发票作
             * 废中 备注：22、24状态时，无需再查询，请确认
             * 开票失败原因以及签章失败原因； 注：请以该状
             * 态码区分发票状态
             */
            String status = resp.getStatus();
            //订单编号
            String orderNo = resp.getOrderNo();
            InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoByOrderId(orderNo);
            if (invoiceInfo == null) {
                log.info("未查询到订单发票信息,订单编号：{}",orderNo);
                continue;
            }
            if (INVOICE_SUCCESS.equals(invoiceInfo.getIssuedStatus())) {
                log.info("已是开票状态，无需再处理,订单编号：{}",orderNo);
                continue;
            }
            if ("2".equals(status)) {
                //开票成功，更新订单发票状态
                invoiceInfo.setIssuedStatus(INVOICE_SUCCESS);
                invoiceInfo.setInvoiceNum(resp.getInvoiceNo()); //发票号码
                invoiceInfo.setInvoiceCode(resp.getInvoiceCode()); //发票代码
                invoiceInfo.setIssuedTime(new Date(resp.getInvoiceTime())); //开票日期
                invoiceInfo.setcUrl(resp.getPdfUrl()); //发票PDF地址
                int i = invoiceInfoMapper.updateInvoiceInfo(invoiceInfo);
                log.info("开票成功，更新订单发票状态,订单编号：{}",orderNo);
                if (i > 0) {
                    //开票成功，推送用户开票状态并判断订单中发票状态是否需要更新并处理
                    // 查订单商品类型区分报名还是证件互通订单
                    if (isInvoiceStatus(orderNo)) {
                        updateCertOrderInvoiceStatus(orderNo, "1");
                    } else {
                        pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(orderNo, "1");
                    }
                    //添加发票抬头
                    insertInvoicePerInfo(invoiceInfo);
                }
            }else if("20".equals(status) || "21".equals(status)){
                //开票中，不做处理
                log.info("开票中，不做处理,订单编号：{}",orderNo);
            }else if("22".equals(status) || "24".equals(status)){
                //开票失败，更新订单发票状态-----开票失败不显示开票失败，显示开票中，等管理员处理后正常显示
                invoiceInfo.setIssuedStatus(INVOICE_FAILED);
                invoiceInfo.setFailReason(resp.getFailCause());   //失败原因
                invoiceInfoMapper.updateInvoiceInfo(invoiceInfo);
                log.info("开票失败，更新订单发票状态,订单编号：{}",orderNo);
            }else if ("3".equals(status) || "31".equals(status)) {
                //发票已作废/作废中
                log.info("发票已作废/作废中,订单编号：{}",orderNo);
            }else {
                log.info("请求失败,未知的状态码【{}】,订单编号：{}",status,orderNo);
            }
        }
        return AjaxResult.success(list);
    }

    /**
     * 定时查询待支付已生成二维码，且在有效期范围内的订单数据，查询支付结果状态
     */
    @Override
    public void syncInvoiceResult() {
        log.info("========================开始同步开票结果==========================");
        List<InvoiceInfo> invoiceInfos = invoiceInfoMapper.syncInvoiceResult();
        if (CollUtil.isEmpty(invoiceInfos)) {
            log.info("没有需要同步结果的开票信息！");
            return;
        }
        for (InvoiceInfo invoiceInfo : invoiceInfos) {
            InvoiceQueryReq invoiceQueryReq = new InvoiceQueryReq();
            invoiceQueryReq.setOrderNos(Arrays.asList(invoiceInfo.getOrderId()));
            invoiceQueryReq.setSerialNos(Arrays.asList(invoiceInfo.getInvoiceSerialNum()));
            queryInvoiceResult(invoiceQueryReq);
            log.info("同步状态值,发票号:"+invoiceInfo.getOrderId());
        }
        log.info("===========================同步结束==================================");
    }

    @Override
    public AjaxResult reInvoice(Long id) {
        InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoById(id);
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByTaxNum(invoiceInfo.getSalerTaxNum());
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("fpqqlsh",invoiceInfo.getInvoiceSerialNum()); //发票流水号，和订单号二选一，都存在以流水号为准
        reqMap.put("orderno",invoiceInfo.getOrderId()); //订单号，和发票流水号二选一，都存在以流水号为准
        String content = JSONUtil.toJsonStr(reqMap);
        log.info("重开票请求参数：{}",content);
        String response = invoiceConfig.requestApi(invoiceConfig.getReInvoiceMethod(), content,config.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),config);
        log.info("重开票响应参数：{}",response);
        JSONObject respJson = JSONUtil.parseObj(response);
        if (!"E0000".equals(respJson.getStr("code"))) {
            return AjaxResult.error(respJson.getStr("describe"));
        }else {
            if(isInvoiceStatus(invoiceInfo.getOrderId())){
                updateCertOrderInvoiceStatus(invoiceInfo.getOrderId(),"2");
            } else {
                pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(invoiceInfo.getOrderId(),"2");
            }
            return AjaxResult.success(respJson.getStr("describe"));
        }
    }

    @Override
    public AjaxResult fastInvoiceRed(Long id) {
        InvoiceInfo invoiceInfo = invoiceInfoMapper.selectInvoiceInfoById(id);
        MerchantParamConfig config = merchantParamConfigService.selectMerchantParamConfigByTaxNum(invoiceInfo.getSalerTaxNum());
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("orderNo",invoiceInfo.getOrderId()); //订单号
        reqMap.put("taxNum",invoiceInfo.getSalerTaxNum()); //纳税人识别号
        reqMap.put("billNo",invoiceInfo.getInvoiceNum()); //发票号码
        reqMap.put("billUuid", IdUtil.fastSimpleUUID()); //红字确认单uuid
        reqMap.put("invoiceId",invoiceInfo.getInvoiceSerialNum()); //发票流水号
        String content = JSONUtil.toJsonStr(reqMap);
        log.info("发票冲红请求参数：{}",content);
        String response = invoiceConfig.requestApi(invoiceConfig.getReInvoiceMethod(), content,config.getInvoiceAccessToken(),invoiceConfig.getCALLBACK(),config);
        log.info("发票冲红相应参数：{}",response);
        JSONObject respJson = JSONUtil.parseObj(response);
        if (!"E0000".equals(respJson.getStr("code"))) {
            return AjaxResult.error(respJson.getStr("describe"));
        }else {
            Object result = respJson.get("result");
            JSONObject jsonObject = JSONUtil.parseObj(result);
            String invoiceSerialNum = jsonObject.getStr("invoiceSerialNum");
            return AjaxResult.success(invoiceSerialNum);
        }
    }

    @Override
    public AjaxResult queryTeamAndUserByOrderId(OrderInfo orderInfoReq) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Map<String,Object> resp = new HashMap<>();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);  //当前登录人
        orderInfo.setInvoiceStatus("0");   //未开票
        orderInfo.setPayStatus(PAID);
        orderInfo.setCommodityType("competition");
        List<OrderInfo> orderInfos = orderInfoService.selectOrderInfoList(orderInfo, false);
        if (CollUtil.isNotEmpty(orderInfos)) {
            List<Map<String,Object>> list = new ArrayList<>();
            //        List<OrderInfo> orderInfos = orderInfoService.selectOrderListByOrderIds(ids);
            //拿到所有团队id
            List<String> teamCodes = orderInfos.stream().map(OrderInfo::getCommodityId).toList();
            String teamCodeStr = CollUtil.join(teamCodes,",");
            //远程调用接口查询人员信息
            CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
            applyInfo.setTeamCodes(teamCodeStr);
            long l1 = System.currentTimeMillis();
            R<List<CompetitionApplyInfo>> listR = competitionService.queryTeamMemberInvoiceStatus(applyInfo, SecurityConstants.INNER);
            long l2 = System.currentTimeMillis();
            System.out.println("调用远程接口时间："+(l2-l1));
            if (listR.getCode() != 200 || CollUtil.isEmpty(listR.getData())) {
                return AjaxResult.success(new ArrayList<>());
            }
            List<CompetitionApplyInfo> data = listR.getData();
            //过滤掉已经开票或者开票中的用户，只保留未开票的用户
            List<CompetitionApplyInfo> unInoivceList = data.stream().filter(info -> "0".equals(info.getInvoiceStatus()) || "指导教师".equals(info.getCompetitionRoleName())).toList();
            //获取过滤后的团队codeList
            List<String> teamCodeList = unInoivceList.stream().map(CompetitionApplyInfo::getTeamCode).distinct().toList();
//        ArrayList<String> teamCodeList = CollUtil.toList(teamCodeStr.split(","));
            teamCodeList.forEach(teamCode -> {
                //按照团队进行分组
                Map<String, Object> map = new HashMap<>();
                Optional<OrderInfo> first = orderInfos.stream().filter(item -> item.getCommodityId().contains(teamCode)).findFirst();
                List<CompetitionApplyInfo> allList = unInoivceList.stream().filter(item -> item.getTeamCode().equals(teamCode)).toList();
                List<CompetitionApplyInfo> guideTeachers = allList.stream().filter(item -> item.getCompetitionRoleName().equals("指导教师")).toList();
                String guideTeacher = "";
                if (CollUtil.isNotEmpty(guideTeachers)) {
                    List<String> teachers = guideTeachers.stream().map(CompetitionApplyInfo::getGuideTeacher).toList();
                    if (CollUtil.isNotEmpty(teachers)) {
                        guideTeacher = CollUtil.join(teachers, ",");
                    }
                }
                List<CompetitionApplyInfo> filterList = allList.stream().filter(item -> !item.getCompetitionRoleName().equals("指导教师")).toList();
                if (CollUtil.isNotEmpty(filterList)) {
                    CompetitionApplyInfo competitionApplyInfo = filterList.get(0);
                    map.put("orderId",first.get().getId().toString());
                    map.put("commodityType",first.get().getCommodityType());
                    map.put("teamCode",teamCode);
                    map.put("teamName",competitionApplyInfo.getTeamName());
                    map.put("leaderTeacher",competitionApplyInfo.getLeaderTeacher());
                    map.put("guideTeacher",guideTeacher);
                    map.put("competitionName", competitionApplyInfo.getCompetitionName());
                    map.put("competitionTrackName", competitionApplyInfo.getCompetitionTrackName());
                    map.put("secondLevelName", competitionApplyInfo.getSecondLevelName());
                    map.put("userInfo",filterList);
                    list.add(map);
                }
            });
            resp.put("competition",list);
        }
        // 获取证书互换未开票订单
        List<Map<String, Object>> certExchangeRuleOrderList = queryCompetitionCertExchangeRuleByOrderId(userId, "cert");
        if(CollectionUtils.isNotEmpty(certExchangeRuleOrderList)){
            resp.put("cert",certExchangeRuleOrderList);
        }
        return AjaxResult.success(resp);
    }

    @Override
    public List<Map<String, Object>> queryCompetitionCertExchangeRuleByOrderId(Long userId,String commodityType) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);  //当前登录人
        orderInfo.setInvoiceStatus("0");   //未开票
        orderInfo.setPayStatus(PAID);
        orderInfo.setCommodityType(commodityType);
        List<OrderInfo> orderInfos = orderInfoService.selectOrderInfoList(orderInfo, false);
        if (CollUtil.isEmpty(orderInfos)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        orderInfos.stream().forEach(orderInfoRes -> {
            Map<String, Object> map = new HashMap<>();
            if(StringUtils.isNotEmpty(orderInfoRes.getTeamInfoList())){
                Map<String,Object> teamMap =  com.alibaba.fastjson2.JSONObject.parseObject(orderInfoRes.getTeamInfoList(), Map.class);
                map.put("orderId",orderInfoRes.getId().toString());
                map.put("commodityType",orderInfoRes.getCommodityType());
                map.put("ruleName",orderInfoRes.getCommodityName());
                map.put("userId",orderInfoRes.getUserId().toString());
                map.put("userName",orderInfoRes.getUserName());
                map.put("repayAmount",orderInfoRes.getAmount().toString());
                map.put("originCertList",teamMap.get("originCertList"));
                map.put("targetCertList",teamMap.get("targetCertList"));
                map.put("invoiceStatus",orderInfoRes.getInvoiceStatus());
                list.add(map);
            }
        });
        return list;
    }

    @Override
    public AjaxResult queryInvoiceAmount(List<InvoiceAmountReq> invoiceAmountReqs) {
        if (CollUtil.isEmpty(invoiceAmountReqs)) {
            return AjaxResult.error("请选择需要开票的项目");
        }
        List<InvoiceAmountResp> respList = new ArrayList<>();
        //订单id集合
        List<Long> ids = invoiceAmountReqs.stream().map(InvoiceAmountReq::getOrderId).distinct().toList();
        requireCurrentUserOrders(ids, true);
        //查询所有订单信息
        List<OrderInfo> orders = orderInfoService.selectOrderListByOrderIds(ids);
        // 区分赛事报名订单和证书互换订单
        List<OrderInfo> competitionOrders = orders.stream()
                .filter(item -> "competition".equals(item.getCommodityType())).toList();
        List<OrderInfo> certOrders = orders.stream()
                .filter(item -> "cert".equals(item.getCommodityType())).toList();
        Map<Long, OrderInfo> orderMap = orders.stream().collect(Collectors.toMap(OrderInfo::getId, order -> order));
        Set<String> requestPairs = new HashSet<>();
        for (InvoiceAmountReq req : invoiceAmountReqs) {
            OrderInfo order = orderMap.get(req.getOrderId());
            if (order == null || req.getUserId() == null || !requestPairs.add(req.getOrderId() + ":" + req.getUserId())) {
                throw new ServiceException("开票项目无效或重复");
            }
            if ("cert".equals(order.getCommodityType()) && !Objects.equals(order.getUserId(), req.getUserId())) {
                throw new ServiceException("证书开票申请人与订单不一致", 403);
            }
        }
        // 仅赛事订单的参赛人员需要远程查询，证书订单中的 userId 是订单所有人。
        List<Long> competitionOrderIds = competitionOrders.stream().map(OrderInfo::getId).toList();
        List<Long> competitionUserIds = invoiceAmountReqs.stream()
                .filter(req -> competitionOrderIds.contains(req.getOrderId()))
                .map(InvoiceAmountReq::getUserId).distinct().toList();
        String userIdsStr = CollUtil.join(competitionUserIds, ",");
        List<CompetitionApplyInfo> competitionApplyInfoDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(competitionOrders)){
            //远程调用接口查询人员信息
            CompetitionApplyInfo rela = new CompetitionApplyInfo();
            rela.setMemberIds(userIdsStr);
            long l1 = System.currentTimeMillis();
            R<List<CompetitionApplyInfo>> listR = competitionService.queryTeamMemberInvoiceStatus(rela, SecurityConstants.INNER);
            long l2 = System.currentTimeMillis();
            System.out.println("调用远程接口时间："+(l2-l1));
            if (listR.getCode() != 200) {
                throw new ServiceException("查询团队人员信息失败！");
            }
            competitionApplyInfoDataList.addAll(listR.getData());
            Map<Long, CompetitionApplyInfo> memberMap = competitionApplyInfoDataList.stream()
                    .filter(member -> member.getMemberId() != null)
                    .collect(Collectors.toMap(CompetitionApplyInfo::getMemberId, member -> member, (left, right) -> left));
            invoiceAmountReqs.stream().filter(req -> competitionOrderIds.contains(req.getOrderId())).forEach(req -> {
                CompetitionApplyInfo member = memberMap.get(req.getUserId());
                OrderInfo selectedOrder = orderMap.get(req.getOrderId());
                Set<String> teamCodes = Arrays.stream(selectedOrder.getCommodityId().split(",")).collect(Collectors.toSet());
                if (member == null || !teamCodes.contains(member.getTeamCode()) || !"0".equals(member.getInvoiceStatus())) {
                    throw new ServiceException("所选开票人员不属于当前订单或已开票", 403);
                }
            });
        }
        List<CompetitionCertExchangeApply> certExchangeApplyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(certOrders)){
            certOrders.stream().forEach(orderInfo -> {
                CompetitionCertExchangeApply certExchangeApply = com.alibaba.fastjson2.JSONObject.parseObject(orderInfo.getTeamInfoList(), CompetitionCertExchangeApply.class);;
                certExchangeApply.setRulerName(orderInfo.getCommodityName());
                certExchangeApply.setUserName(orderInfo.getUserName());
                certExchangeApplyList.add(certExchangeApply);
            });
        }
        //根据订单，获取涉及的商户号
        List<String> merchantCommodityGroups = orders.stream()
                .map(order -> order.getMerId() + "@@" + order.getCommodityType()).distinct().toList();
        // 每个商户、业务类型分别分组，避免同商户的赛事和证书金额互相覆盖。
        merchantCommodityGroups.forEach(groupKey -> {
            String[] groupParts = groupKey.split("@@", 2);
            String merId = groupParts[0];
            String groupCommodityType = groupParts[1];
            InvoiceAmountResp resp = new InvoiceAmountResp();
            //获取当前商户配置信息
            MerchantParamConfig merchantParamConfig = merchantParamConfigService.selectMerchantParamConfigByMerId(merId);
            resp.setMerId(merId);
            resp.setMerName(merchantParamConfig.getMerName());  //收款公司
            resp.setTaxNum(merchantParamConfig.getTaxNum());    //税号
            //筛选当前商户关联的订单号
            List<OrderInfo> filterOrders = orders.stream()
                    .filter(order -> Objects.equals(order.getMerId(), merId) && Objects.equals(order.getCommodityType(), groupCommodityType)).toList();
            List<Long> orderIds = filterOrders.stream().map(OrderInfo::getId).toList();
            resp.setOrderIds(orderIds);
            //筛选当前商户关联的用户id
            List<Long> userIdList = invoiceAmountReqs.stream().filter(item -> orderIds.contains(item.getOrderId())).map(InvoiceAmountReq::getUserId).toList();
            resp.setUserIds(userIdList);
            // 报名信息赋值
            if("competition".equals(groupCommodityType) && CollectionUtils.isNotEmpty(competitionApplyInfoDataList)){
                //获取关联用户的信息
                List<CompetitionApplyInfo> users = competitionApplyInfoDataList.stream().filter(item -> userIdList.contains(item.getMemberId())).toList();
                //计算当前商户用户的总金额
                BigDecimal invoiceAmount = users.stream().map(CompetitionApplyInfo::getFee).filter(Objects::nonNull).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
                resp.setInvoiceAmount(invoiceAmount);
                //团队编号赋值
                List<String> teamCodeList = filterOrders.stream().map(OrderInfo::getCommodityId).toList();
                if (CollUtil.isNotEmpty(teamCodeList)) {
                    resp.setTeamCodes(CollUtil.join(teamCodeList,","));
                }
                //团队人员信息赋值
                List<Map<String,Object>> members = buildInvoiceMembers(orderIds,userIdList);
                resp.setMembers(members);
                resp.setCommodityType("competition");
            }
            // 证书订单证书信息赋值
            if("cert".equals(groupCommodityType) && CollectionUtils.isNotEmpty(certExchangeApplyList)){
                List<CompetitionCertExchangeApply> filteredCertApplications = filterOrders.stream().map(order -> {
                    CompetitionCertExchangeApply apply = com.alibaba.fastjson2.JSONObject.parseObject(order.getTeamInfoList(), CompetitionCertExchangeApply.class);
                    apply.setRulerName(order.getCommodityName());
                    apply.setUserName(order.getUserName());
                    return apply;
                }).toList();
                builderCertExchangeApplyInfo(filteredCertApplications, resp, filterOrders);
            }
            //开票内容赋值
            setInvoiceContent(merchantParamConfig, resp);
            respList.add(resp);
        });
        return AjaxResult.success(respList);
    }

    private void builderCertExchangeApplyInfo(List<CompetitionCertExchangeApply> certExchangeApplyList, InvoiceAmountResp resp, List<OrderInfo> filterOrders) {
        BigDecimal invoiceAmount = certExchangeApplyList.stream().map(CompetitionCertExchangeApply::getRepayAmount).filter(Objects::nonNull).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        resp.setInvoiceAmount(invoiceAmount);
        List<String> rulerIds = filterOrders.stream().map(OrderInfo::getCommodityId).toList();
        if (CollUtil.isNotEmpty(rulerIds)) {
            resp.setCertRuleId(CollUtil.join(rulerIds,","));
        }
        List<Map<String,Object>> certExchangeApplys = new ArrayList<>();
        Set<String> rulerNameSet = new HashSet();
        Set<String> originCertNameSet = new HashSet<>();
        Set<String> targetCertNameSet = new HashSet<>();
        Set<String> userNameSet = new HashSet<>();
        certExchangeApplyList.stream().forEach(apply -> {
            Map<String,Object> certExchangeApplyMap = new HashMap<>();
            certExchangeApplyMap.put("rulerName",apply.getRulerName());
            certExchangeApplyMap.put("userId",apply.getUserId());
            certExchangeApplyMap.put("userName",apply.getUserName());
            certExchangeApplyMap.put("originCertList",apply.getOriginCertList());
            certExchangeApplyMap.put("targetCertList",apply.getTargetCertList());
            certExchangeApplys.add(certExchangeApplyMap);
            rulerNameSet.add(apply.getRulerName());
            List<String> originCertNameList = apply.getOriginCertList().stream().map(CertConfigInfo::getCertConfigName).toList();
            originCertNameSet.addAll(originCertNameList);
            List<String> targetCertNameList = apply.getTargetCertList().stream().map(CertConfigInfo::getCertConfigName).toList();
            targetCertNameSet.addAll(targetCertNameList);
            userNameSet.add(apply.getUserName());
        });
        resp.setCertApplyInfo(certExchangeApplys);
        resp.setCommodityType("cert");
        // 证书申请备注信息
        List<Map<String,Object>> certExchangeApplyRemarkList = new ArrayList<>();
        Map<String,Object> certExchangeApplyRemarkMap = new HashMap<>();
        certExchangeApplyRemarkMap.put("label","证书互通名称");
        certExchangeApplyRemarkMap.put("value",StringUtils.join(rulerNameSet, ","));
        Map<String,Object> originCertRemarkMap = new HashMap<>();
        originCertRemarkMap.put("label","源证书名称");
        originCertRemarkMap.put("value",StringUtils.join(originCertNameSet, ","));
        Map<String,Object> targetCertRemarkMap = new HashMap<>();
        targetCertRemarkMap.put("label","目标证书名称");
        targetCertRemarkMap.put("value",StringUtils.join(targetCertNameSet, ","));
        Map<String,Object> userNameRemarkMap = new HashMap<>();
        userNameRemarkMap.put("label","申请人姓名");
        userNameRemarkMap.put("value",StringUtils.join(userNameSet, ","));
        certExchangeApplyRemarkList.add(certExchangeApplyRemarkMap);
        certExchangeApplyRemarkList.add(originCertRemarkMap);
        certExchangeApplyRemarkList.add(targetCertRemarkMap);
        certExchangeApplyRemarkList.add(userNameRemarkMap);
        resp.setCertApplyRemarkInfo(certExchangeApplyRemarkList);
    }

    private List<Map<String, Object>> buildInvoiceMembers(List<Long> orderIds,List<Long> userIdList) {
        List<Map<String,Object>> resultList = new ArrayList<>();
        try{
            List<OrderInfo> orderInfos = orderInfoService.selectOrderListByOrderIds(orderIds);
            //拿到所有团队id
            List<String> teamCodes = orderInfos.stream().map(OrderInfo::getCommodityId).toList();
            String teamCodeStr = CollUtil.join(teamCodes,",");
            //远程调用接口查询人员信息
            CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
            applyInfo.setTeamCodes(teamCodeStr);
            R<List<CompetitionApplyInfo>> listR = competitionService.queryTeamMemberInvoiceStatus(applyInfo, SecurityConstants.INNER);
            if (listR.getCode() != 200 || CollUtil.isEmpty(listR.getData())) {
                return null;
            }
            List<CompetitionApplyInfo> data = listR.getData();
            //过滤掉非选中的用户，同时保留指导老师
            List<CompetitionApplyInfo> selectList = data.stream().filter(info -> userIdList.contains(info.getMemberId()) || "指导教师".equals(info.getCompetitionRoleName())).toList();
            //获取过滤后的团队codeList
            List<String> teamCodeList = selectList.stream().map(CompetitionApplyInfo::getTeamCode).distinct().toList();
            teamCodeList.forEach(teamCode -> {
                //按照团队进行分组
                Map<String, Object> map = new HashMap<>();
                List<CompetitionApplyInfo> allList = selectList.stream().filter(item -> item.getTeamCode().equals(teamCode)).toList();
                List<CompetitionApplyInfo> guideTeachers = allList.stream().filter(item -> item.getCompetitionRoleName().equals("指导教师")).toList();
                String guideTeacher = "";
                if (CollUtil.isNotEmpty(guideTeachers)) {
                    List<String> teachers = guideTeachers.stream().map(CompetitionApplyInfo::getGuideTeacher).toList();
                    if (CollUtil.isNotEmpty(teachers)) {
                        guideTeacher = CollUtil.join(teachers, ",");
                    }
                }
                List<CompetitionApplyInfo> filterList = allList.stream().filter(item -> !item.getCompetitionRoleName().equals("指导教师")).toList();
                if (CollUtil.isNotEmpty(filterList)) {
                    CompetitionApplyInfo competitionApplyInfo = filterList.get(0);
                    map.put("teamCode",teamCode);
                    map.put("teamName",competitionApplyInfo.getTeamName());
                    map.put("leaderTeacher",competitionApplyInfo.getLeaderTeacher());
                    map.put("guideTeacher",guideTeacher);
                    map.put("competitionName", competitionApplyInfo.getCompetitionName());
                    map.put("competitionTrackName", competitionApplyInfo.getCompetitionTrackName());
                    map.put("secondLevelName", competitionApplyInfo.getSecondLevelName());
                    map.put("userInfo",filterList);
                    resultList.add(map);
                }
            });
        }catch (Exception e){
            log.error("获取开票人员信息失败："+e.getMessage());
        }
        return resultList;
    }

    //开票内容赋值
    private void setInvoiceContent(MerchantParamConfig merchantParamConfig, InvoiceAmountResp resp) {
        String invoiceContent = merchantParamConfig.getInvoiceContent();
        if (ObjectUtil.isNotEmpty(invoiceContent)) {
            List<Map<String,String>> contentMapList = new ArrayList<>();
            String[] split = invoiceContent.split(",");
            for (String content : split) {
                if (content.contains("&")) {
                    String[] split1 = content.split("&");
                    String goodsLabel = sysDictDataService.selectDictLabel(INVOICE_GOODS_CODE, split1[0]);
                    String feeTypeLabel = sysDictDataService.selectDictLabel(FEE_TYPE, split1[1]);
                    Map<String, String> map = new HashMap<>();
                    map.put(content,goodsLabel+feeTypeLabel);
                    contentMapList.add(map);
                }
            }
            resp.setInvoiceContent(contentMapList);
        }
    }
}
