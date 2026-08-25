package com.teaching.system.api;


import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.TeamChangeDto;
import com.teaching.system.api.factory.OrderServiceFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "orderInfoService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = OrderServiceFactory.class)
public interface OrderService {

    /**
     * 新增订单信息
     */
    @PostMapping("/order")
    R<Integer> createOrder(@RequestBody OrderInfo orderInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 对账任务
     */
    @GetMapping("/order/statementTask")
    R<Void> statementTask(@RequestParam String billDate, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 定时同步支付账单
     */
    @GetMapping("/order/syncPaymentResult")
    R<Void> payTask(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 定时同步退款账单
     */
    @GetMapping("/order/syncRefundResult")
    R<Void> refundTask(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据用户id和商品id查询订单信息
     */
    @GetMapping("/order/inner/getOrderByUserIdAndCommodityId")
    R<OrderInfo> getOrderByUserIdAndCommodityId(@RequestParam Long userId, @RequestParam String commodityId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据审核类型获取审核拒绝意见
     *
     * @param auditType  审核类型 TdConstants.AUDIT_FLOW_TYPE_XX
     * @param businessId 记录主键
     * @param source     来源
     * @return R<String>
     */
    @GetMapping("/task/getCheckOpinion/{auditType}/{businessId}")
    public R<String> innerGetCheckOpinion(@PathVariable String auditType, @PathVariable Long businessId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 新增审核任务
     *
     * @param auditType  审核类型 TdConstants.AUDIT_FLOW_TYPE_XX
     * @param businessId 记录主键
     * @param source     来源
     * @return R<Integer>
     */
    @GetMapping("/task/addAuditTask/{auditType}/{businessId}")
    public R<Integer> innerAddAuditTask(@PathVariable String auditType, @PathVariable Long businessId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询订单状态
     *
     * @param req
     * @return
     */
    @PostMapping("/order/selectOrderStatus")
    R<List<OrderInfo>> selectOrderStatus(@RequestBody SelectOrderStatusReq req, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据商品ID列表查询订单信息
     *
     * @param commodityId 商品ID列表
     * @param source       来源
     * @return R<List<OrderInfo>>
     */
    @PostMapping("/order/getOrdersByCommodityId")
    R<OrderInfo> getOrdersByCommodityId(@RequestBody String commodityId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据团队编码数组查询订单数量 判断团队是否已经生成订单
     * @param teamCodes
     * @param source
     * @return
     */
    @PostMapping("/order/count/{teamCodes}")
    public R<Integer> getCountByTeamCodes(@PathVariable String[] teamCodes, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据用户id和商品id查询订单状态
     * @param userId 用户id
     * @param commodityId 商品id
     * @param source 来源
     * @return 订单状态
     */
    @GetMapping("/order/inner/getOrderStatusByUserIdAndCommodityId")
    R<String> getOrderStatusByUserIdAndCommodityId(@RequestParam("userId") Long userId, @RequestParam("commodityId") String commodityId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/order/inner/createPayOrderByTeamChange")
    R<OrderInfo> createPayOrderByTeamChange(@RequestBody TeamChangeDto teamChangeDto, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/order/createRefundOrderByTeamChange")
    R<OrderInfo> createRefundOrderByTeamChange(@RequestBody TeamChangeDto teamChangeDto, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/order/updateRefundStatus")
    R<Integer> updateRefundStatus(@RequestParam Long refundId, @RequestParam(required = false) String refundReason,@RequestParam String changeType ,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/order/updateRefundCancelStatus")
    R<Integer> updateRefundCancelStatus(@RequestParam Long refundId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/order/getPayAndRefundOrderInfo")
    R<Map<String,Object>> getPayAndRefundOrderInfo(@RequestParam Long refundId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/order/checkTeamChangePayOrder")
    R<Integer> checkTeamChangePayOrder(@RequestParam String teamCode,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/order/updateTeamInfo")
    R<Void> updateTeamInfo(@RequestParam String updateSize,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/order/getCertPaymentUrl")
    R<AjaxResult> getCertPaymentUrl(@RequestBody OrderInfo orderInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

}
