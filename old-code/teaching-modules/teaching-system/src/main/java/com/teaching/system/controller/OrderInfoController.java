package com.teaching.system.controller;

import java.math.BigDecimal;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.CompetitionApplyInfoVO;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import com.teaching.system.api.domain.TeamChangeDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.service.IOrderInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 订单信息Controller
 *
 * @author teaching
 * @date 2025-10-17
 */
@RestController
@RequestMapping("/order")
public class OrderInfoController extends BaseController
{
    @Autowired
    private IOrderInfoService orderInfoService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CompetitionService competitionService;

    /**
     * 查询订单信息列表
     */
    @RequiresPermissions("system:info:list")
    @GetMapping("/list")
    public TableDataInfo list(OrderInfo orderInfo)
    {
        startPage();
        List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo,false);
        return getDataTable(list);
    }

    /**
     * 查询个人订单信息列表
     * @param orderInfo 个人订单列表
     * @return
     */
    @GetMapping("/personalList")
    public TableDataInfo personalList(OrderInfo orderInfo)
    {
        orderInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        startPage();
        List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo,true);
        return getDataTable(list);
    }

    @GetMapping("/perStatusCount")
    public AjaxResult perStatusCount(OrderInfo orderInfo){
        orderInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return success(orderInfoService.perStatusCount(orderInfo));
    }

    /**
     * 导出订单信息列表
     */
    @RequiresPermissions("system:info:export")
    @Log(title = "订单信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderInfo orderInfo)
    {
        List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo,false);
        ExcelUtil<OrderInfo> util = new ExcelUtil<OrderInfo>(OrderInfo.class);
        util.exportExcel(response, list, "订单信息数据");
    }

    /**
     * 获取订单信息详细信息
     */
    @RequiresPermissions("system:info:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderInfoService.selectOrderInfoByOrderId(id));
    }

    @GetMapping(value = "/personal/{id}")
    public AjaxResult getOrderInfo(@PathVariable("id") Long id)
    {
        return success(orderInfoService.selectPersonalOrderInfoById(id));
    }

    /**
     * 获取订单信息详细信息
     */
    @GetMapping(value = "/personal/getOrder")
    public AjaxResult getPersonalInfo(@RequestParam(required = false) Long userId,@RequestParam String commodityId)
    {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(orderInfoService.getOrderInfoByUserIdAndCommodityId(currentUserId,commodityId));
    }

    /**
     * 新增订单信息
     */
    @RequiresPermissions("system:info:add")
    @Log(title = "订单信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OrderInfo orderInfo)
    {
        return toAjax(orderInfoService.insertOrderInfo(orderInfo));
    }

    /**
     * 修改订单信息
     */
    @RequiresPermissions("system:info:edit")
    @Log(title = "订单信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderInfo orderInfo)
    {
        return toAjax(orderInfoService.updateOrderInfo(orderInfo));
    }

    @InnerAuth
    @GetMapping("/updateTeamInfo")
    public AjaxResult updateTeamInfo(@RequestParam String updateSize) {
        return toAjax(orderInfoService.updateOrderInfoTeamJson(updateSize));
    }

    /**
     * 删除订单信息
     */
    @RequiresPermissions("system:info:remove")
    @Log(title = "订单信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderInfoService.deleteOrderInfoByOrderIds(ids));
    }

    /**
     * 查询订单二维码链接
     * @return
     */
    @PostMapping("/getPaymentUrl")
    public AjaxResult getPaymentUrl(@RequestBody OrderInfo orderInfo) {
        return orderInfoService.getPaymentUrl(orderInfo,true);
    }

    /**
     * 生成证书订单并获取二维码（跨服务调用）
     */
    @InnerAuth
    @PostMapping("/getCertPaymentUrl")
    public AjaxResult getCertPaymentUrl(@RequestBody OrderInfo orderInfo) {
        return orderInfoService.getCertPaymentUrl(orderInfo);
    }

    /**
     * 重新生成订单二维码
     */
    @GetMapping("/regeneratePaymentUrl/{id}")
    public AjaxResult regeneratePaymentUrl(@PathVariable Long id) {
        return orderInfoService.regeneratePaymentUrl(id);
    }

    /**
     * 订单支付成功回调
     */
    @PostMapping("/paymentCallback")
    public Map<String, String> paymentCallback(@RequestParam Map<String,String> notifyMap) {
        return orderInfoService.paymentCallback(notifyMap);
    }

    /**
     * 取消订单
     */
    @GetMapping("/cancelOrder/{id}")
    public AjaxResult cancelOrder(@PathVariable Long id) {
        return orderInfoService.cancelOrder(id);
    }

    // 退费重缴取消待支付订单
    @GetMapping("/cancelRepaymentOrder/{id}")
    public AjaxResult cancelRepaymentOrder(@PathVariable Long id) {
        return orderInfoService.cancelRepaymentOrder(id);
    }

    /**
     * 退款
     */
    @GetMapping("/refund/{id}")
    public AjaxResult refund(@PathVariable Long id,@RequestParam(required = false) String refundReason) {
        orderInfoService.selectPersonalOrderInfoById(id);
        return orderInfoService.refund(id,refundReason);
    }

    /**
     * 退款成功回调
     */
    @PostMapping("/refundCallback")
    public Map<String, String> refundCallback(@RequestParam Map<String,String> notifyMap) {
        return orderInfoService.refundCallback(notifyMap);
    }

    /**
     * 支付结果查询
     */
    @GetMapping("/queryPaymentResult/{id}")
    public AjaxResult queryPaymentResult(@PathVariable Long id) {
        orderInfoService.selectPersonalOrderInfoById(id);
        return orderInfoService.queryPaymentResult(id);
    }

    /**
     * 退款结果查询
     */
    @GetMapping("/queryRefundResult/{id}")
    public AjaxResult queryRefundResult(@PathVariable Long id) {
        orderInfoService.selectPersonalOrderInfoById(id);
        return orderInfoService.queryRefundResult(id);
    }

    /**
     * 微信小程序下单
     */
    @GetMapping("/wxMiniCreateOrder/{id}")
    public AjaxResult wxMiniCreateOrder(@PathVariable Long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return orderInfoService.wxMiniCreateOrder(id,ip);
    }

    /**
     * 对账单下载地址获取
     * billDate 日期，格式为yyyy-MM-dd，如2025-10-24
     */
//    @GetMapping("/statementUrl/{billDate}")
//    public AjaxResult statementUrl(@PathVariable String billDate) {
//        return orderInfoService.statementUrl(billDate);
//    }

    @GetMapping("/statementTask")
    public void statementTask(@RequestParam String billDate) {
        orderInfoService.statementTask(billDate);
    }

    @GetMapping("/syncPaymentResult")
    public void syncPaymentResult() {
        orderInfoService.syncPaymentResult();
    }

    @GetMapping("/syncRefundResult")
    public void syncRefundResult() {
        orderInfoService.syncRefundResult();
    }


    /**
     * 根据用户id和商品id查询订单
     * @param userId
     * @param commodityId
     * @return
     */
    @GetMapping("/getOrderByUserIdAndCommodityId")
    public AjaxResult getOrderInfoByUserIdAndCommodityId(@RequestParam(required = false) Long userId,@RequestParam String commodityId) {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(orderInfoService.getOrderInfoByUserIdAndCommodityId(currentUserId,commodityId));
    }

    @InnerAuth
    @GetMapping("/inner/getOrderByUserIdAndCommodityId")
    public AjaxResult getOrderInfoByUserIdAndCommodityIdInner(@RequestParam Long userId,@RequestParam String commodityId) {
        return success(orderInfoService.getOrderInfoByUserIdAndCommodityId(userId, commodityId));
    }

    /**
     * 根据用户id和商品id查询订单状态
     * @param userId
     * @param commodityId
     * @return
     */
    @GetMapping("/getOrderStatusByUserIdAndCommodityId")
    public AjaxResult getOrderStatusByUserIdAndCommodityId(@RequestParam(required = false) Long userId, @RequestParam String commodityId) {
        Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(orderInfoService.selectOrderStatusByUserIdAndCommodityId(currentUserId, commodityId));
    }

    @InnerAuth
    @GetMapping("/inner/getOrderStatusByUserIdAndCommodityId")
    public AjaxResult getOrderStatusByUserIdAndCommodityIdInner(@RequestParam Long userId, @RequestParam String commodityId) {
        return success(orderInfoService.selectOrderStatusByUserIdAndCommodityId(userId, commodityId));
    }

    /**
     * 查询订单状态
     * @param req
     * @return
     */
    @InnerAuth
    @PostMapping("/selectOrderStatus")
    public AjaxResult selectOrderStatus(@RequestBody SelectOrderStatusReq req) {
        if (CollUtil.isEmpty(req.getCommodityIds())) {
            return error("商品id不能为空！");
        }
        return success(orderInfoService.selectOrderStatus(req));
    }

    /**
     * 更新附件信息
     * @param id
     * @param paymentProofFiles
     * @return
     */
    @GetMapping("/updatePaymentProof")
    public AjaxResult updateAttachmentInfo(@RequestParam Long id,@RequestParam String paymentProofFiles) {
        return orderInfoService.updateAttachmentInfo(id,paymentProofFiles);
    }

    /**
     * 转账证明审核
     * id 订单id
     * checkStatus 审核状态
     * auditOpinion 审核意见
     */
    @RequiresPermissions("system:info:edit")
    @GetMapping("/proofAudit")
    public AjaxResult checkAttachmentInfo(@RequestParam Long id,@RequestParam Integer auditStatus,@RequestParam(required = false) String auditOpinion) {
        return success(orderInfoService.checkAttachmentInfo(id,auditStatus,auditOpinion));
    }

    /**
     * 修改支付方式
     */
    @GetMapping("/updatePayMethod")
    public AjaxResult updatePaymentMethod(@RequestParam Long id,@RequestParam String payMethod) {
        return orderInfoService.updatePaymentMethod(id,payMethod);
    }

    /**
     * 查询未支付订单数量
     */
    @GetMapping("/getPendingCount")
    public AjaxResult getPendingCount() {
        //获取当前用户id
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(orderInfoService.getPendingCount(userId));
    }

    @InnerAuth
    @PostMapping("/count/{teamCodes}")
    public AjaxResult getPendingCount(@PathVariable String[] teamCodes) {
        return success(orderInfoService.getCountByTeamCodes(teamCodes));
    }

    //
    @InnerAuth
    @PostMapping("/getOrdersByCommodityId")
    public AjaxResult getOrderByCommodityId(@RequestBody String commodityId) {
        return success(orderInfoService.getOrderByCommodityId(commodityId));
    }

    /**
     * 查询订单线下转账信息
     */
    @GetMapping("/getOfflineBankInfo/{id}")
    public AjaxResult getOfflineBankInfo(@PathVariable Long id) {
        return success(orderInfoService.getOfflineBankInfo(id));
    };

    @GetMapping("/commodityNameList")
    public AjaxResult getCommodityNameList(){
        return success(orderInfoService.getCommodityNameList());
    }

    /**
     * 生成支付订单
     * 涉及流程流程：
     * 1、调整团队人员（增加人员）
     * 2、退费重缴+
     * @param teamChangeDto
     * @return
     */
    @PostMapping("/createPayOrderByTeamChange")
    public AjaxResult createPayOrderByTeamChange(@RequestBody TeamChangeDto teamChangeDto) {
        // 获取团队成员数量计算退费重缴金额
        //调用远端接口.获取商品详情
        Map<String, Object> reqMap = new HashMap<>();
        List<String> commodityIds = Arrays.asList(teamChangeDto.getTeamCode().split(","));
        reqMap.put("teamCodeList",commodityIds);
        R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
        if(R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())){
            // 单团队直接取值
            CompetitionApplyInfoVO competitionApplyInfoVO = detailForOrder.getData().get(0);
            if(Objects.nonNull(competitionApplyInfoVO)){
                teamChangeDto.setUserNum(competitionApplyInfoVO.getTeamSize());
            }
        }
        return orderInfoService.createPayOrderByTeamChange(teamChangeDto);
    }

    /**
     * 生成退款订单
     * 涉及流程流程：
     * 1、调整团队人员（减少人员）
     * 2、发起退费
     * 3、退费重缴
     * @param teamChangeDto
     * @return
     */
    @InnerAuth
    @PostMapping("/createRefundOrderByTeamChange")
    public AjaxResult createRefundOrderByTeamChange(@RequestBody TeamChangeDto teamChangeDto) {
        return orderInfoService.createRefundOrderByTeamChange(teamChangeDto);
    }

    @InnerAuth
    @PostMapping("/inner/createPayOrderByTeamChange")
    public AjaxResult createPayOrderByTeamChangeInner(@RequestBody TeamChangeDto teamChangeDto) {
        return orderInfoService.createPayOrderByTeamChange(teamChangeDto);
    }

    /**
     * 更新退款订单状态
     * @param refundId 退款订单id
     * @param refundReason 退款原因
     * @return
     */
    @InnerAuth
    @GetMapping("/updateRefundStatus")
    public void updateRefundStatus(@RequestParam Long refundId, @RequestParam(required = false) String refundReason, @RequestParam String changeType) {
         orderInfoService.updateRefundStatus(refundId, refundReason,changeType);
    }

    /**
     * 取消退款订单，修改状态（审核驳回）
     * @param refundId 退款订单id
     */
    @InnerAuth
    @GetMapping("/updateRefundCancelStatus")
    public void updateRefundCancelStatus(@RequestParam Long refundId) {
        orderInfoService.updateRefundCancelStatus(refundId);
    }

    /**
     * 根据退款订单id获取支付和退款订单信息
     * @param refundId 退款订单id
     * @return
     */
    @InnerAuth
    @GetMapping("/getPayAndRefundOrderInfo")
    public AjaxResult getPayAndRefundOrderInfo(@RequestParam Long refundId) {
        return success(orderInfoService.getPayAndRefundOrderInfo(refundId));
    }

    @InnerAuth
    @GetMapping("/checkTeamChangePayOrder")
    public AjaxResult checkTeamChangePayOrder(@RequestParam String teamCode) {
        return success(orderInfoService.checkTeamChangePayOrder(teamCode));
    }
}
