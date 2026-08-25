package com.teaching.flowable.utils;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;

import java.text.DecimalFormat;

/**
 * @author Administrator
 */
public class NumberCodeUtil {
    private static final String STR_FORMAT = "000";
    private static final String SHXB_STR = "SHXB";
    private static final String FYQR_STR = "FYQR";
    public static String YYYY_MM_DD = "yyyyMMdd";

    public static String haoAddOne(String number) {
        number = StringUtils.isBlank(number) ? "0" : number;
        Integer intHao = Integer.parseInt(number);
        intHao++;
        DecimalFormat df = new DecimalFormat(STR_FORMAT);
        return df.format(intHao);
    }

    /**
     * 返回上划下拨编号  number为null or "" 返回001 否则加1
     * SHXB+yyyyMMdd+(001-999)
     *
     * @param number 当前的尾号
     * @return 结果
     */
    public static String getShxbCode(String number) {
        String date = DateUtils.dateTime();
        String s = haoAddOne(number);
        return SHXB_STR + date + s;
    }

    /**
     * 返回费用确认编号  number为null or "" 返回001 否则加1
     * FYQR+yyyyMMdd+(001-999)
     *
     * @param number 当前的尾号
     * @return 结果
     */
    public static String getFyqrCode(String number) {
        String date = DateUtils.dateTime();
        String s = haoAddOne(number);
        return FYQR_STR + date + s;
    }

    public static void main(String[] args) {
        String dk = getShxbCode("1");
        System.out.println(dk);
    }
}
