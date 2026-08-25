package com.teaching.competition.util;

public class StringNumberUtil {

    //    判断字符是否是纯数字
    public static boolean isNumber(String str) {
        return str.matches("\\d+");
    }
}
