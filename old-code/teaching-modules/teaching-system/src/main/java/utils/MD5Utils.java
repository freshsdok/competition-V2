package utils;

import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    /**
     * MD5加密
     *
     * @param input
     * @return
     */
    public static String getMD5Content(String input) {
        try {
            // 拿到MD转换器
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] inputByteArray = input.getBytes();
            messageDigest.update(inputByteArray);

            byte[] resultByteArray = messageDigest.digest();

            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字节数组转换成16进组的字符串
     *
     * @param byteArray
     * @return
     */
    public static String byteArrayToHex(byte[] byteArray) {
        // 初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        char[] resultCharArray = new char[byteArray.length * 2];

        int index = 0;
        // 遍历字节数组，通过位运算，转换成字符放到字符数据中
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        return new String(resultCharArray);
    }

    /**
     * 姓名第一个字密文+ 姓名其他部分明文。
     *
     * @param userName
     * @return
     */
    public static String userNameMd5(String userName) {
        return DigestUtils.md5DigestAsHex(userName.substring(0, 1).getBytes()) +
                userName.substring(1);
    }

    /**
     * 身份证号前6位（明文）+出生年月日（密文）+身份证号后4位（明文）。
     *
     * @param identifyNum
     * @return
     */
    public static String identifyNumMd5(String identifyNum) {
        return identifyNum.substring(0, 6) +
                DigestUtils.md5DigestAsHex(identifyNum.substring(6, 14).getBytes()) +
                identifyNum.substring(14);
    }
}
