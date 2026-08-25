package com.teaching.file.utils;

import java.text.DecimalFormat;

public class FileSizeUtil {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.#");

    /**
     * 将字节大小转换为带单位的字符串（自动选择合适的单位）
     * @param size 文件大小（字节）
     * @return 格式化后的字符串，如：1.5 MB
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, UNITS.length - 1);

        return DECIMAL_FORMAT.format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
    }

    /**
     * 将字节大小转换为带单位的字符串（精确到指定小数位）
     * @param size 文件大小（字节）
     * @param decimalPlaces 保留小数位数
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size, int decimalPlaces) {
        if (size <= 0) {
            return "0 B";
        }

        String pattern = "#,##0";
        if (decimalPlaces > 0) {
            pattern += "." + "#".repeat(decimalPlaces);
        }
        DecimalFormat df = new DecimalFormat(pattern);

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, UNITS.length - 1);

        return df.format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
    }

    /**
     * 使用1024进制（二进制）转换
     */
    public static String formatBinaryFileSize(long size) {
        return formatFileSizeWithBase(size, 1024);
    }

    /**
     * 使用1000进制（十进制）转换
     */
    public static String formatDecimalFileSize(long size) {
        return formatFileSizeWithBase(size, 1000);
    }

    private static String formatFileSizeWithBase(long size, int base) {
        if (size <= 0) {
            return "0 B";
        }

        String[] units;
        if (base == 1024) {
            units = new String[]{"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
        } else {
            units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        }

        int digitGroups = (int) (Math.log10(size) / Math.log10(base));
        digitGroups = Math.min(digitGroups, units.length - 1);

        return DECIMAL_FORMAT.format(size / Math.pow(base, digitGroups)) + " " + units[digitGroups];
    }
}