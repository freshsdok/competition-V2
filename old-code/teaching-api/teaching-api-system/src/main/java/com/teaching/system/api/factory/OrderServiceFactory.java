package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import com.teaching.system.api.domain.TeamChangeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 赛事服务降级处理
 *
 * @author teaching
 */
@Component
public class OrderServiceFactory implements FallbackFactory<OrderService> {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceFactory.class);

    @Override
    public OrderService create(Throwable cause) {
        return new OrderService(){
            @Override
            public R<Integer> createOrder(OrderInfo orderInfo, String source) {
                return R.fail("订单创建失败:" + cause.getMessage());
            }

            @Override
            public R<Void> statementTask(String billDate,String source) {
                return R.fail("对账任务失败:" + cause.getMessage());
            }

            @Override
            public R<Void> payTask(String source) {
                return R.fail("支付结果同步失败:" + cause.getMessage());
            }

            @Override
            public R<Void> refundTask(String source) {
                return R.fail("退款结果同步失败:" + cause.getMessage());
            }

            @Override
            public R<OrderInfo> getOrderByUserIdAndCommodityId(Long userId, String commodityId, String source) {
                return R.fail("查询订单信息失败：" + cause.getMessage());
            }

            @Override
            public R<String> innerGetCheckOpinion(String auditType, Long businessId, String source) {
                return R.fail("查询拒绝意见失败：" + cause.getMessage());
            }

            @Override
            public R<Integer> innerAddAuditTask(String auditType, Long businessId, String source) {
                return R.fail("新增审核任务失败：" + cause.getMessage());
            }

            @Override
            public R<List<OrderInfo>> selectOrderStatus(SelectOrderStatusReq req, String source) {
                return R.fail("查询订单状态失败：" + cause.getMessage());
            }

            @Override
            public R<OrderInfo> getOrdersByCommodityId(String commodityId, String source) {
                return R.fail("查询订单列表失败：" + cause.getMessage());
            }
            @Override
            public R<Integer> getCountByTeamCodes(String[] teamCodes, String source) {
                return R.fail("查询订单数量失败：" + cause.getMessage());
            }

            @Override
            public R<OrderInfo> createPayOrderByTeamChange(TeamChangeDto teamChangeDto, String source) {
                return R.fail("生成支付单失败：" + cause.getMessage());
            }

            @Override
            public R<OrderInfo> createRefundOrderByTeamChange(TeamChangeDto teamChangeDto, String source) {
                return R.fail("生成退费单失败：" + cause.getMessage());
            }

            @Override
            public R<Integer> updateRefundStatus(Long refundId, String refundReason,String changeType, String source) {
                return R.fail("更新退费订单状态失败：" + cause.getMessage());
            }

            @Override
            public R<Integer> updateRefundCancelStatus(Long refundId, String source) {
                return R.fail("取消退款订单，修改状态失败：" + cause.getMessage());
            }

            @Override
            public R<Map<String, Object>> getPayAndRefundOrderInfo(Long refundId, String source) {
                return R.fail("查询退款和原支付订单信息失败：" + cause.getMessage());
            }

            @Override
            public R<Integer> checkTeamChangePayOrder(String teamCode, String source) {
                return R.fail("查询调整订单是否有进行中的订单失败：" + cause.getMessage());
            }

            @Override
            public R<String> getOrderStatusByUserIdAndCommodityId(Long userId, String commodityId, String source) {
                return R.fail("查询订单状态失败：" + cause.getMessage());
            }

            @Override
            public R<Void> updateTeamInfo(String updateSize, String source) {
                return R.fail("更新原订单团队信息失败：" + cause.getMessage());
            }

            @Override
            public R<AjaxResult> getCertPaymentUrl(OrderInfo orderInfo, String source) {
                return R.fail("获取证书支付链接失败：" + cause.getMessage());
            }
        };
    }
}
