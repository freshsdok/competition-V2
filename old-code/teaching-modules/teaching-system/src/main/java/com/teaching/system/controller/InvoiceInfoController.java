package com.teaching.system.controller;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Map;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.domain.vo.invoice.InvoiceAmountReq;
import com.teaching.system.domain.vo.invoice.InvoiceApplyReq;
import com.teaching.system.domain.vo.invoice.InvoiceQueryReq;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.InvoiceInfo;
import com.teaching.system.service.IInvoiceInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 发票信息Controller
 * 
 * @author teaching
 * @date 2025-10-28
 */
@Slf4j
@RestController
@RequestMapping("/invoice")
public class InvoiceInfoController extends BaseController
{
    @Autowired
    private IInvoiceInfoService invoiceInfoService;

    @Autowired
    private CompetitionService competitionService;

    /**
     * 查询发票信息列表
     */
    @RequiresPermissions("system:info:list")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody InvoiceInfo invoiceInfo)
    {
        startPage();
        List<InvoiceInfo> list = invoiceInfoService.selectInvoiceInfoList(invoiceInfo);
        return getDataTable(list);
    }

    /**
     * 个人登录列表展示接口
     * @param invoiceInfo 发票信息
     * @return
     */
    @PostMapping("/personalList")
    public TableDataInfo personalList(@RequestBody InvoiceInfo invoiceInfo)
    {
        invoiceInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        startPage();
        List<InvoiceInfo> list = invoiceInfoService.selectInvoiceInfoList(invoiceInfo);
        return getDataTable(list);
    }

    /**
     * 导出发票信息列表
     */
    @RequiresPermissions("system:info:export")
    @Log(title = "发票信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvoiceInfo invoiceInfo)
    {
        List<InvoiceInfo> list = invoiceInfoService.selectInvoiceInfoList(invoiceInfo);
        ExcelUtil<InvoiceInfo> util = new ExcelUtil<InvoiceInfo>(InvoiceInfo.class);
        util.exportExcel(response, list, "发票信息数据");
    }

    /**
     * 获取发票信息详细信息
     */
    @RequiresPermissions("system:info:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(invoiceInfoService.selectInvoiceInfoById(id));
    }

    /**
     * 新增发票信息
     */
    @RequiresPermissions("system:info:add")
    @Log(title = "发票信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InvoiceInfo invoiceInfo)
    {
        return toAjax(invoiceInfoService.insertInvoiceInfo(invoiceInfo));
    }

    /**
     * 修改发票信息
     */
    @RequiresPermissions("system:info:edit")
    @Log(title = "发票信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InvoiceInfo invoiceInfo)
    {
        return toAjax(invoiceInfoService.updateInvoiceInfo(invoiceInfo));
    }

    /**
     * 删除发票信息
     */
    @RequiresPermissions("system:info:remove")
    @Log(title = "发票信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(invoiceInfoService.deleteInvoiceInfoByIds(ids));
    }

    /**
     * 发票申请--废弃
     */
    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody InvoiceApplyReq applyReq){
        return invoiceInfoService.invoiceApply(applyReq);
    }

    /**
     * 发票申请
     */
    @PostMapping("/applyNew")
    public AjaxResult applyNew(@RequestBody List<InvoiceApplyReq> applyReqList){
        return invoiceInfoService.invoiceApplyNew(applyReqList);
    }

    /**
     * 发票重新交付，给用户推送发票
     * @param id
     * @return
     */
    @GetMapping("/deliveryInvoice")
    public AjaxResult deliveryInvoice(@RequestParam Long id){
        invoiceInfoService.selectPersonalInvoiceById(id);
        return invoiceInfoService.deliveryInvoice(id);
    }

    /**
     * 发票申请回调接口
     * @param notifyMap
     */
    @PostMapping("/scan")
    public Map<String,String> callback(@RequestParam Map<String,String> notifyMap) {
        if (notifyMap == null) {
            return null;
        }
        // 处理回调参数
        String operater = notifyMap.get("operater");
        if ("callback".equals(operater)) {
            return invoiceInfoService.applyCallback(notifyMap);
        }
        return null;
    }

    /**
     * pdf下载
     */
    @RequiresPermissions("system:info:query")
    @GetMapping("/pdf/download")
    public void downloadPdfWithHutool(@RequestParam String pdfUrl,
                                      HttpServletResponse response) {
        try (HttpResponse httpResponse = HttpRequest.get(pdfUrl)
                .timeout(30000)
                .setFollowRedirects(true)
                .execute()) {

            if (!httpResponse.isOk()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("PDF文件不存在或下载失败");
                return;
            }

            // 设置响应头
            String fileName = extractFileName(pdfUrl);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"");

            // 获取PDF数据并写入response
            byte[] pdfData = httpResponse.bodyBytes();
            try (OutputStream out = response.getOutputStream()) {
                out.write(pdfData);
                out.flush();
            }

            log.info("PDF下载成功: {}, 大小: {} bytes", pdfUrl, pdfData.length);

        } catch (Exception e) {
            log.error("PDF下载失败: {}", pdfUrl, e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件下载失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误信息失败", ex);
            }
        }
    }

    /**
     * 下载当前登录人的发票 PDF，下载地址由服务端从发票记录中读取。
     */
    @GetMapping("/personal/pdf/{id}")
    public void downloadPersonalPdf(@PathVariable Long id, HttpServletResponse response) {
        InvoiceInfo invoiceInfo = invoiceInfoService.selectPersonalInvoiceById(id);
        if (invoiceInfo.getcUrl() == null || invoiceInfo.getcUrl().isBlank()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        downloadPdfWithHutool(invoiceInfo.getcUrl(), response);
    }
    /**
     * 提取文件名
     */
    private static String extractFileName(String pdfUrl) {
        try {
            String[] pathSegments = pdfUrl.split("/");
            String lastSegment = pathSegments[pathSegments.length - 1];

            if (lastSegment.contains("?")) {
                lastSegment = lastSegment.substring(0, lastSegment.indexOf("?"));
            }

            if (!lastSegment.isEmpty() && lastSegment.endsWith(".pdf")) {
                return lastSegment;
            } else {
                return "invoice_" + System.currentTimeMillis() + ".pdf";
            }
        } catch (Exception e) {
            return "invoice_" + System.currentTimeMillis() + ".pdf";
        }
    }

    /**
     * 查询开票结果
     * @param invoiceQueryReq 请求参数
     * @return
     */
    @PostMapping("/queryInvoiceResult")
    public AjaxResult queryInvoiceResult(@RequestBody InvoiceQueryReq invoiceQueryReq){
        return invoiceInfoService.queryPersonalInvoiceResult(invoiceQueryReq);
    }

    /**
     * 同步开票结果定时任务调用
     */
    @InnerAuth
    @GetMapping("/syncInvoiceResult")
    public void syncInvoiceResult(){
        invoiceInfoService.syncInvoiceResult();
    }

    /**
     * 开票重试-开票失败时调用
     */
    @RequiresPermissions("system:info:edit")
    @GetMapping("/reInvoice")
    public AjaxResult reInvoice(@RequestParam Long id){
        return invoiceInfoService.reInvoice(id);
    }

    /**
     * 发票快捷冲红接口
     */
    @RequiresPermissions("system:info:edit")
    @GetMapping("/fastInvoiceRed")
    public AjaxResult fastInvoiceRed(@RequestParam Long id){
        return invoiceInfoService.fastInvoiceRed(id);
    }


    /**
     * 根据选择的订单id查询订单下所有团队及人员信息
     */
    @PostMapping("/queryTeamAndUserByOrderId")
    public AjaxResult queryTeamAndUserByOrderId(@RequestBody OrderInfo orderInfo){
        return invoiceInfoService.queryTeamAndUserByOrderId(orderInfo);
    }

    /**
     * 根据订单id、人员id查询人员信息并进行不同商户的金额汇总
     */
    @PostMapping("/queryInvoiceAmount")
    public AjaxResult queryInvoiceAmountBy(@RequestBody List<InvoiceAmountReq> req){
        return invoiceInfoService.queryInvoiceAmount(req);
    }

    @GetMapping("/getSecondList")
    public R<?> getSecondList() {
        Map<String,List<Map<String,Object>>> res = Map.of(
                "competition",competitionService.queryCompetitionInfo(SecurityConstants.INNER).getData(),
                "course",new ArrayList<>(),
                "exam",new ArrayList<>(),
                "store",new ArrayList<>()
        );
        return R.ok(res);
    }

    @RequiresPermissions("system:info:edit")
    @GetMapping("/test")
    public void test(@RequestParam String orderId){
        invoiceInfoService.pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(orderId,"1");
    }
}
