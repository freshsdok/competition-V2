package com.teaching.competition.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// 生成提取码
public class ExtractionCodeUtil {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final int RANDOM_STRING_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();
    private static final char[] LOWER_ARRAY = CHAR_LOWER.toCharArray();
    private static final char[] UPPER_ARRAY = CHAR_UPPER.toCharArray();

    public static String getExtractionCode() {
        StringBuilder sb = new StringBuilder(RANDOM_STRING_LENGTH);
        // 用于确保字符唯一性（尽管在这个场景下不是必须的，但可以作为扩展使用）
        Set<Character> chars = new HashSet<>();
        boolean hasLower = false, hasUpper = false;
        while (sb.length() < RANDOM_STRING_LENGTH) {
            char c;
            if (!hasLower) { // 确保至少有一个小写字母
                c = LOWER_ARRAY[random.nextInt(LOWER_ARRAY.length)];
                hasLower = true; // 标记已添加小写字母
            } else if (!hasUpper) { // 确保至少有一个大写字母
                c = UPPER_ARRAY[random.nextInt(UPPER_ARRAY.length)];
                hasUpper = true; // 标记已添加大写字母
            } else { // 随机选择小写或大写字母以外的字符（可选）
                c = (random.nextBoolean() ? LOWER_ARRAY[random.nextInt(LOWER_ARRAY.length)] : UPPER_ARRAY[random.nextInt(UPPER_ARRAY.length)]);
            }
            // 确保字符唯一（可选）
            if (chars.add(c)) {
                sb.append(c);
            }
            // 如果不关心唯一性，可以直接sb.append(c);即可。
        }
        return sb.toString();
    }
}
