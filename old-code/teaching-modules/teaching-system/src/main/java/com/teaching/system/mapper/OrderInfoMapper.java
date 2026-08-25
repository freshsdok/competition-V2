package com.teaching.system.mapper;

import java.util.List;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-17
 */
@Mapper
public interface OrderInfoMapper
{
    /**
     * 查询订单信息
     *
     * @param id 订单信息主键
     * @return 订单信息
     */
    public OrderInfo selectOrderInfoById(Long id);

    /**
     * 获取无团队信息的原订单列表
     *
     * @return
     */
    public List<OrderInfo> selectOrderInfoTeamJson();

    /**
     * 查询订单信息列表
     *
     * @param orderInfo 订单信息
     * @return 订单信息集合
     */
    public List<OrderInfo> selectOrderInfoList(OrderInfo orderInfo);

    /**
     * 新增订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    public int insertOrderInfo(OrderInfo orderInfo);

    /**
     * 修改订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    public int updateOrderInfo(OrderInfo orderInfo);

    /**
     * 删除订单信息
     *
     * @param id 订单信息主键
     * @return 结果
     */
    public int deleteOrderInfoByOrderId(Long id);

    /**
     * 批量删除订单信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderInfoByOrderIds(Long[] ids);

    /**
     * 根据订单号查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo selectOrderInfoByOrderId(String orderId);

    /**
     * 退款订单号查询订单
     * @param refudnOrderId
     * @return
     */
    OrderInfo selectOrderInfoByRefundOrderId(String refudnOrderId);


    List<OrderInfo> syncPaymentResult();

    List<OrderInfo> syncRefundResult();

    List<OrderInfo> getOrderByUserIdAndCommodityId(Long userId, String commodityId);

    List<OrderInfo> selectOrderStatus(@Param("req") SelectOrderStatusReq req);

    void updateAttachmentInfo(Long id, String attachmentInfo);

    int getPendingCount(Long userId);

    int selectCountByTeamCodes(String[] teamCodes);

    OrderInfo getOrderByCommodityId(String commodityId);

    List<String> selectTeamCodesByOrderIds(List<Long> ids);

    List<OrderInfo> selectOrderListByOrderIds(List<Long> ids);

    List<String> getCommodityNameList();

    int updateRefundStatus(Long id, String refundReason);

    OrderInfo selectOrderByTeamCode(String teamCode);

    OrderInfo selectPendingOrderByTeamCode(String teamCode);

    OrderInfo selectRefundRepaymentOrderByTeamCode(String teamCode);

    List<OrderInfo> selectOrderListByTeamCode(String teamCode);

    void updateRefundCancelStatus(Long refundId);

    List<OrderInfo> selectOrderInfoByPayOrderId(Long payOrderId);

    OrderInfo selectRefundOrderByPayOrderId(Long payOrderId);
}
