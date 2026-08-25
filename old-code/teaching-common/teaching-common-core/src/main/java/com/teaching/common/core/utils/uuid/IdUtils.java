package com.teaching.common.core.utils.uuid;

import java.util.Random;

/**
 * ID生成器工具类
 *
 * @author teaching
 */
public class IdUtils
{
    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 随机UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
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
