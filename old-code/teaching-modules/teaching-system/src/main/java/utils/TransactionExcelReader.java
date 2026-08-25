package utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.teaching.system.domain.vo.TransactionDetail;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class TransactionExcelReader {

    /**
     * 读取交易明细数据
     * @param filePath Excel文件路径
     * @return 交易明细列表
     */
    public static List<TransactionDetail> readTransactionDetails(String filePath) {
        List<TransactionDetail> transactionDetails = new ArrayList<>();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在: {}", filePath);
                return transactionDetails;
            }

            // 创建Excel读取器
            ExcelReader reader = ExcelUtil.getReader(file);

            // 查找交易明细开始行（包含表头的行）
            int startRow = findTransactionDetailStartRow(reader);
            if (startRow == -1) {
                log.error("未找到交易明细开始行");
                reader.close();
                return transactionDetails;
            }

            log.info("交易明细开始行: {}", startRow);

            // 设置表头别名映射
            setHeaderAlias(reader);

            // 从开始行读取数据（跳过表头）
            List<Map<String, Object>> rows = reader.read(startRow,startRow + 1, reader.getRowCount());

            // 转换为实体对象
            for (Map<String, Object> row : rows) {
                TransactionDetail detail = mapToTransactionDetail(row);
                if (isValidTransactionDetail(detail)) {
                    transactionDetails.add(detail);
                }
            }

            reader.close();
            log.info("成功读取 {} 条交易明细记录", transactionDetails.size());

        } catch (Exception e) {
            log.error("读取交易明细失败: {}", e.getMessage(), e);
        }

        return transactionDetails;
    }

    /**
     * 查找交易明细开始行（包含表头的行）
     */
    private static int findTransactionDetailStartRow(ExcelReader reader) {
        // 从第1行开始查找（0-based索引）
        for (int i = 0; i < reader.getRowCount(); i++) {
            List<Object> row = reader.readRow(i);
            if (CollUtil.isNotEmpty(row)) {
                // 查找包含"门店编号"的表头行
                for (Object cell : row) {
                    if (cell != null && "门店编号".equals(cell.toString().trim())) {
                        return i; // 返回表头所在行
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 设置表头别名映射
     */
    private static void setHeaderAlias(ExcelReader reader) {
        reader.addHeaderAlias("门店编号", "storeNo");
        reader.addHeaderAlias("门店名称", "storeName");
        reader.addHeaderAlias("商户名称", "merchantName");
        reader.addHeaderAlias("第三方商户号", "thirdPartyMerchantNo");
        reader.addHeaderAlias("商户号", "merchantNo");
        reader.addHeaderAlias("商户订单号", "merchantOrderNo");
        reader.addHeaderAlias("银行流水", "bankFlowNo");
        reader.addHeaderAlias("交易日期", "transactionDate");
        reader.addHeaderAlias("交易时间", "transactionTime");
        reader.addHeaderAlias("商品名称", "productName");
        reader.addHeaderAlias("交易金额", "transactionAmount");
        reader.addHeaderAlias("优惠金额(免充值优惠券金额)", "discountAmount");
        reader.addHeaderAlias("交易币种", "currency");
        reader.addHeaderAlias("费率", "rate");
        reader.addHeaderAlias("付款银行", "paymentBank");
        reader.addHeaderAlias("支付方式", "paymentMethod");
        reader.addHeaderAlias("交易类型", "transactionType");
        reader.addHeaderAlias("交易状态", "transactionStatus");
        reader.addHeaderAlias("付款钱包id", "payerWalletId");
        reader.addHeaderAlias("付款运营机构", "payerOperator");
        reader.addHeaderAlias("商户钱包id", "merchantWalletId");
        reader.addHeaderAlias("收款运营机构", "payeeOperator");
        reader.addHeaderAlias("商户出资优惠金额", "merchantDiscount");
        reader.addHeaderAlias("发卡方出资优惠金额", "issuerDiscount");
        reader.addHeaderAlias("银联出资优惠金额", "unionPayDiscount");
        reader.addHeaderAlias("结算金额", "settlementAmount");
        reader.addHeaderAlias("手续费", "serviceFee");
        reader.addHeaderAlias("第三方订单号", "thirdPartyOrderNo");
        reader.addHeaderAlias("企业红包金额", "enterpriseRedPacket");
        reader.addHeaderAlias("企业红包退款金额", "enterpriseRefund");
        reader.addHeaderAlias("AppID", "appId");
        reader.addHeaderAlias("清分结果", "clearingResult");
        reader.addHeaderAlias("清分日期", "clearingDate");
        reader.addHeaderAlias("清分账号", "clearingAccount");
        reader.addHeaderAlias("账单日期", "billDate");
        reader.addHeaderAlias("原交易银行流水(银行退款单号)", "originalBankFlowNo");
        reader.addHeaderAlias("退款金额", "refundAmount");
        reader.addHeaderAlias("退款类型", "refundType");
        reader.addHeaderAlias("原交易商户订单号(商户退款单号)", "originalMerchantOrderNo");
        reader.addHeaderAlias("退款备注", "refundRemark");
        reader.addHeaderAlias("收银员", "cashier");
        reader.addHeaderAlias("终端类型", "terminalType");
        reader.addHeaderAlias("终端号", "terminalNo");
        reader.addHeaderAlias("商户数据包", "merchantDataPacket");
        reader.addHeaderAlias("商户保留域", "merchantReserved");
        reader.addHeaderAlias("收款方备注", "payeeRemark");
        reader.addHeaderAlias("付款方备注", "payerRemark");
        reader.addHeaderAlias("付款人信息", "payerInfo");
        reader.addHeaderAlias("完成日期", "completeDate");
        reader.addHeaderAlias("付款人用户名", "payerUserName");
        reader.addHeaderAlias("入账流水", "accountFlowNo");
    }

    /**
     * 将Map转换为TransactionDetail对象
     */
    private static TransactionDetail mapToTransactionDetail(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }

        TransactionDetail detail = new TransactionDetail();

        try {
            // 基础信息
            detail.setStoreNo(getStringValue(row, "storeNo"));
            detail.setStoreName(getStringValue(row, "storeName"));
            detail.setMerchantName(getStringValue(row, "merchantName"));
            detail.setThirdPartyMerchantNo(getStringValue(row, "thirdPartyMerchantNo"));
            detail.setMerchantNo(getStringValue(row, "merchantNo"));
            detail.setMerchantOrderNo(getStringValue(row, "merchantOrderNo"));
            detail.setBankFlowNo(getStringValue(row, "bankFlowNo"));
            detail.setTransactionDate(getStringValue(row, "transactionDate"));
            detail.setTransactionTime(getStringValue(row, "transactionTime"));
            detail.setProductName(getStringValue(row, "productName"));

            // 金额信息
            detail.setTransactionAmount(getBigDecimalValue(row, "transactionAmount"));
            detail.setDiscountAmount(getBigDecimalValue(row, "discountAmount"));
            detail.setCurrency(getStringValue(row, "currency"));
            detail.setRate(getStringValue(row, "rate"));
            detail.setPaymentBank(getStringValue(row, "paymentBank"));
            detail.setPaymentMethod(getStringValue(row, "paymentMethod"));
            detail.setTransactionType(getStringValue(row, "transactionType"));
            detail.setTransactionStatus(getStringValue(row, "transactionStatus"));

            // 钱包信息
            detail.setPayerWalletId(getStringValue(row, "payerWalletId"));
            detail.setPayerOperator(getStringValue(row, "payerOperator"));
            detail.setMerchantWalletId(getStringValue(row, "merchantWalletId"));
            detail.setPayeeOperator(getStringValue(row, "payeeOperator"));

            // 优惠信息
            detail.setMerchantDiscount(getBigDecimalValue(row, "merchantDiscount"));
            detail.setIssuerDiscount(getBigDecimalValue(row, "issuerDiscount"));
            detail.setUnionPayDiscount(getBigDecimalValue(row, "unionPayDiscount"));

            // 结算信息
            detail.setSettlementAmount(getBigDecimalValue(row, "settlementAmount"));
            detail.setServiceFee(getBigDecimalValue(row, "serviceFee"));
            detail.setThirdPartyOrderNo(getStringValue(row, "thirdPartyOrderNo"));
            detail.setEnterpriseRedPacket(getBigDecimalValue(row, "enterpriseRedPacket"));
            detail.setEnterpriseRefund(getBigDecimalValue(row, "enterpriseRefund"));
            detail.setAppId(getStringValue(row, "appId"));

            // 清分信息
            detail.setClearingResult(getStringValue(row, "clearingResult"));
            detail.setClearingDate(getStringValue(row, "clearingDate"));
            detail.setClearingAccount(getStringValue(row, "clearingAccount"));
            detail.setBillDate(getStringValue(row, "billDate"));

            // 退款信息
            detail.setOriginalBankFlowNo(getStringValue(row, "originalBankFlowNo"));
            detail.setRefundAmount(getBigDecimalValue(row, "refundAmount"));
            detail.setRefundType(getStringValue(row, "refundType"));
            detail.setOriginalMerchantOrderNo(getStringValue(row, "originalMerchantOrderNo"));
            detail.setRefundRemark(getStringValue(row, "refundRemark"));

            // 其他信息
            detail.setCashier(getStringValue(row, "cashier"));
            detail.setTerminalType(getStringValue(row, "terminalType"));
            detail.setTerminalNo(getStringValue(row, "terminalNo"));
            detail.setMerchantDataPacket(getStringValue(row, "merchantDataPacket"));
            detail.setMerchantReserved(getStringValue(row, "merchantReserved"));
            detail.setPayeeRemark(getStringValue(row, "payeeRemark"));
            detail.setPayerRemark(getStringValue(row, "payerRemark"));
            detail.setPayerInfo(getStringValue(row, "payerInfo"));
            detail.setCompleteDate(getStringValue(row, "completeDate"));
            detail.setPayerUserName(getStringValue(row, "payerUserName"));
            detail.setAccountFlowNo(getStringValue(row, "accountFlowNo"));

        } catch (Exception e) {
            log.error("转换交易明细数据失败: {}", e.getMessage(), e);
            return null;
        }

        return detail;
    }

    /**
     * 验证交易明细数据是否有效
     */
    private static boolean isValidTransactionDetail(TransactionDetail detail) {
        if (detail == null) {
            return false;
        }

        // 必须有商户订单号或银行流水号
        return StrUtil.isNotBlank(detail.getMerchantOrderNo()) ||
                StrUtil.isNotBlank(detail.getBankFlowNo());
    }

    private static String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString().trim() : "";
    }

    private static BigDecimal getBigDecimalValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }

        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                String strValue = value.toString().trim();
                if (StrUtil.isBlank(strValue)) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(strValue);
            }
        } catch (Exception e) {
            log.warn("转换BigDecimal失败: key={}, value={}", key, value);
            return BigDecimal.ZERO;
        }
    }
}