package com.teaching.competition.util;

import org.springframework.stereotype.Component;

import java.util.Random;

public class UUIDUtils {

    public static String getUUID() {
        String uuid = java.util.UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    // 根据时间戳生成随机6位字符
    public static String getRandomCode() {
        long timestamp = System.currentTimeMillis();
        // 使用时间戳的最后6位数字 + 随机数生成6位数字
        Random random = new Random();
        int randomPart = random.nextInt(1000000);  // 生成一个[0, 999999]范围内的随机数
        // 如果随机数是小于6位数的，需要补零
        String randomString = String.format("%06d", randomPart);
        // 拼接时间戳的部分和随机数
        String result = String.valueOf(timestamp % 1000000) + randomString;
        // 截取最后6位
        String sixDigitNumber = result.substring(result.length() - 6);
        return sixDigitNumber;
    }
}
