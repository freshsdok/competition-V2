package com.teaching.common.core.utils.sign;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA加密解密
 *
 * @author cesoft
 **/
public class RsaUtils {
    public static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCL/ITLMWZZ1kXmWyxjxPVD30ica9+Mcm7eSvBI+MBgzbPdx8EEGGgUqbJoIx7ecx17sqKt2L0nSxn1ob3CdIqc1/DmWXEVZsr4WDdo7R71hmiL3q1Tb6Eyv87Wb7T4+Jk6j43+3OC8nh2MtRs0bDPclFCDKYHXAIWuDlvWxSdQzwIDAQAB";
    // Rsa 私钥
    public static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIv8hMsxZlnWReZbLGPE9UPfSJxr34xybt5K8Ej4wGDNs93HwQQYaBSpsmgjHt5zHXuyoq3YvSdLGfWhvcJ0ipzX8OZZcRVmyvhYN2jtHvWGaIverVNvoTK/ztZvtPj4mTqPjf7c4LyeHYy1GzRsM9yUUIMpgdcAha4OW9bFJ1DPAgMBAAECgYAQjPwUXBRaVcuw5yGx8BUBf9JBcD2fiN4T2S9cqVBxgZCdDaOD/PC9VKz7w/8/1MNtHxs9y6zdivMYSBW7+nRy1tCtVfEBhoKBabs7XjMDG1dZlB1DxjTqiXUtStwPEftMjYDNctnmsQTmVYUc5DEdbIN3dMNArMCEDHdOZwUEcQJBAKp82wfhir/x3au8WshAxOcIoin7A5QLbEE7tP+IfEzQu2tjDoOo/Er5PRa5uVZx568M0JBdpd5aLmuU3TVdJN8CQQDSMzNscWDftOE+p8lLMlzAIkguWZtaHecwuoVcxLs+saH8sPpvzf496VeEfytA5+oJwl5stslXpzfo1ysK9eIRAkEAk1EsGtVDhbTDrUAm2d9Nxa1qIwhqASUVuBCVyDDx55Z+PL7trcr5pvdTWC3H/vCSGGrkVbr2NvqKHiAWPaRs1QJBAKEOmB1ENizSZC/k0chrO0QAQHw7Llx7QxREJkExgCMGag667/jQxjhb3THpWpPt3pZBtqXn3BfsSVt/2jwYsHECQAPzEKvdV6/r8FO1RKhQWWMgSFGlRV+3T8zS6z3oiNQcjRFguRZ6q3bv31ohY0or/mBzGHIyKo1DEMl7iLJ/zyM=";

    /**
     * 私钥解密
     *
     * @param text             待解密的文本
     * @return 解密后的文本
     */
    public static String decryptByPrivateKey(String text) throws Exception {
        return decryptByPrivateKey(privateKey, text);
    }

    /**
     * 公钥解密
     *
     * @param publicKeyString 公钥
     * @param text            待解密的信息
     * @return 解密后的文本
     */
    public static String decryptByPublicKey(String publicKeyString, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));
        return new String(result);
    }

    /**
     * 私钥加密
     * @param text
     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String text) throws Exception{
        return encryptByPrivateKey(privateKey, text);
    }

    /**
     * 私钥加密
     *
     * @param privateKeyString 私钥
     * @param text             待加密的信息
     * @return 加密后的文本
     */
    public static String encryptByPrivateKey(String privateKeyString, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * 私钥解密
     *
     * @param privateKeyString 私钥
     * @param text             待解密的文本
     * @return 解密后的文本
     */
    public static String decryptByPrivateKey(String privateKeyString, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));
        return new String(result);
    }

    public static void main(String[] args) throws Exception {
        // 私钥加密公钥解
        String s = encryptByPrivateKey(privateKey, "123456");
        System.out.println(s);
        String s2 = decryptByPublicKey(publicKey, s);
        System.out.println(s2);
        //公钥加密私钥解
        String s3 = encryptByPublicKey(publicKey, "4114221111111");
        System.out.println(s3);
        String s4 = decryptByPrivateKey(privateKey, s3);
        System.out.println(s4);
    }

    /**
     * 公钥加密
     * @param text
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String text) throws Exception{
        return encryptByPublicKey(publicKey, text);
    }

    /**
     * 公钥加密
     *
     * @param publicKeyString 公钥
     * @param text            待加密的文本
     * @return 加密后的文本
     */
    public static String encryptByPublicKey(String publicKeyString, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * 构建RSA密钥对
     *
     * @return 生成后的公私钥信息
     */
    public static RsaKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String publicKeyString = Base64.encodeBase64String(rsaPublicKey.getEncoded());
        String privateKeyString = Base64.encodeBase64String(rsaPrivateKey.getEncoded());
        return new RsaKeyPair(publicKeyString, privateKeyString);
    }

    /**
     * RSA密钥对对象
     */
    public static class RsaKeyPair {
        private final String publicKey;
        private final String privateKey;

        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }
}
