package com.teaching.system.domain.vo.invoice;

import lombok.Data;

/**
 * @Description: 发票明细
 */
@Data
public class InvoiceDetail {
    /** 商品名称（即invoiceLineProperty=1，则此商品行为折扣行）数电票长度限制为100个字符 最大长度100 */
    private String goodsName;
    /** 商品编码（商品税收分类编码开发者自行填写）最大长度19 */
    private String goodsCode;
    /** 自行编码（可不填）最大长度16 */
    private String selfCode;
    /** 单价含税标志：0不含税;1含税 最大长度1 */
    private String withTaxFlag;
    /** 单价（精确到小数点后8位）最大长度16 */
    private String price;
    /** 数量（精确到小数点后8位，开具红票时数量为负数）最大长度16 */
    private String num;
    /** 单位 最大长度20 */
    private String unit;
    /** 规格型号、车辆识别代号/车架号码（当发票性质为机动车、二手车时）最大长度40 */
    private String specType;
    /** 税额（精确到小数点后2位），[不含税金额]*[税率]=[税额];税额允许误差为0.06 最大长度16 */
    private String tax;
    /** 税率 最大长度10 */
    private String taxRate;
    /** 不含税金额（精确到小数点后2位）最大长度16 */
    private String taxExcludedAmount;
    /** 含税金额（精确到小数点后2位）最大长度16 */
    private String taxIncludedAmount;
    /** 发票行性质：0正常行,1折扣行,2被折扣行，红票只有正常行 最大长度1 */
    private String invoiceLineProperty;
    /** 优惠政策标识 最大长度2 */
    private String favouredPolicyFlag;
    /** 即征即退类型标识 最大长度2 */
    private String immediateTaxReturnType;
    /** 增值税特殊管理（优惠政策名称）当favouredPolicyFlag为1时，此项必填 最大长度50 */
    private String favouredPolicyName;
    /** 扣除额，差额征收时填写，目前只支持填写一项 最大长度16 */
    private String deduction;
    /** 零税率标识：空非零税率；1免税；2不征税；3普通零税率 最大长度1 */
    private String zeroRateFlag;
    /** 业务明细自定义字段1，本应用仅作保存 最大长度255 */
    private String dField1;
    /** 业务明细自定义字段2，本应用仅作保存 最大长度255 */
    private String dField2;
    /** 业务明细自定义字段3，本应用仅作保存 最大长度255 */
    private String dField3;
    /** 业务明细自定义字段4，本应用仅作保存 最大长度255 */
    private String dField4;
    /** 业务明细自定义字段5，本应用仅作保存 最大长度255 */
    private String dField5;
}
