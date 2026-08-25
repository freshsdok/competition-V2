package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import com.teaching.system.api.domain.TeamChangeDto;

/**
 * 订单信息Service接口
 *
 * @author teaching
 * @date 2025-10-17
 */
public interface IOrderInfoService
{
    /**
     * 查询订单信息
     *
     * @param orderId 订单信息主键
     * @return 订单信息
     */
    public OrderInfo selectOrderInfoByOrderId(Long orderId);

    /**
     * 查询当前登录人的订单详情。
     */
    public OrderInfo selectPersonalOrderInfoById(Long orderId);

    /**
     * 查询订单信息列表
     *
     * @param orderInfo 订单信息
     * @return 订单信息集合
     */
    public List<OrderInfo> selectOrderInfoList(OrderInfo orderInfo,boolean isPersonal);

    /**
     * 新增订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    public int insertOrderInfo(OrderInfo orderInfo);

    OrderInfo insertRefundOrderInfo(OrderInfo orderInfo,TeamChangeDto teamChangeDto);
    /**
     * 修改订单信息
     *
     * @param orderInfo 订单信息
     * @return 结果
     */
    public int updateOrderInfo(OrderInfo orderInfo);

    public int updateOrderInfoTeamJson(String updateSize);

    /**
     * 批量删除订单信息
     *
     * @param ids 需要删除的订单信息主键集合
     * @return 结果
     */
    public int deleteOrderInfoByOrderIds(Long[] ids);

    /**
     * 删除订单信息信息
     *
     * @param id 订单信息主键
     * @return 结果
     */
    public int deleteOrderInfoByOrderId(Long id);

    /**
     * 支付结果通知
     */
    public void payResultNotify(String orderId, String status);

    /**
     * 获取支付二维码链接
     * @param isFirst
     * @return
     */
    AjaxResult getPaymentUrl(OrderInfo orderInfo, boolean isFirst);

    AjaxResult getCertPaymentUrl(OrderInfo orderInfo);

    /**
     * 重新生成支付链接
     * */
    AjaxResult regeneratePaymentUrl(Long id);

    /**
     * 支付回调
     * @param notifyMap
     * @return
     */
    Map<String,String> paymentCallback(Map<String, String> notifyMap);

    /**
     * 取消订单
     * @param id
     * @return
     */
    AjaxResult cancelOrder(Long id);

    /**
     * 退费重缴取消订单
     * @param id
     * @return
     */
    AjaxResult cancelRepaymentOrder(Long id);

    /**
     * 申请退款
     * @param id
     * @param refundReason
     * @return
     */
    AjaxResult refund(Long id,String refundReason);

    /**
     * 退款回调
     * @param notifyMap
     * @return
     */
    Map<String, String> refundCallback(Map<String, String> notifyMap);

    /**
     * 查询支付结果
     * @param id
     * @return
     */
    AjaxResult queryPaymentResult(Long id);

    /**
     * 查询退款结果
     * @param id
     * @return
     */
    AjaxResult queryRefundResult(Long id);

    AjaxResult wxMiniCreateOrder(Long id,String ip);

//    AjaxResult statementUrl(String billDate);

    /**
     * 执行对账任务
     */
    void statementTask(String billDate);

    /**
     * 支付结果状态同步
     */
    void syncPaymentResult();

    /**
     * 退款结果状态同步
     */
    void syncRefundResult();

    OrderInfo getOrderInfoByUserIdAndCommodityId(Long userId, String commodityId);

    /**
     * 根据用户id和商品id查询订单状态
     * @param userId
     * @param commodityId
     * @return
     */
    String selectOrderStatusByUserIdAndCommodityId(Long userId, String commodityId);

    List<OrderInfo> selectOrderStatus(SelectOrderStatusReq req);

    AjaxResult updateAttachmentInfo(Long id, String attachmentInfo);

    int checkAttachmentInfo(Long id, Integer checkStatus, String checkReason);

    AjaxResult updatePaymentMethod(Long id, String paymentMethod);

    void getAndSetOderDetail(OrderInfo orderInfo,boolean isPersonal);

    int getPendingCount( Long userId);

    Map<String,Long> perStatusCount(OrderInfo orderInfo);

    /**
     * 根据团队编码查询订单数量
     * @param teamCodes
     * @return
     */
    int getCountByTeamCodes(String[] teamCodes);

    OrderInfo getOrderByCommodityId(String commodityId);

    Map<String,String> getOfflineBankInfo(Long id);

    List<String> selectTeamCodesByOrderIds(List<Long> ids);

    List<OrderInfo> selectOrderListByOrderIds(List<Long> ids);

    List<String> getCommodityNameList();

    AjaxResult createPayOrderByTeamChange(TeamChangeDto teamChangeDto);

    AjaxResult createRefundOrderByTeamChange(TeamChangeDto teamChangeDto);

    void updateRefundStatus(Long refundId, String refundReason,String changeType);

    Map<String,Object> getPayAndRefundOrderInfo(Long refundId);

    void updateRefundCancelStatus(Long refundId);

    int checkTeamChangePayOrder(String teamCode);
}
