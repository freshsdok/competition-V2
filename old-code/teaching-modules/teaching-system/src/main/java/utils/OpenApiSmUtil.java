package utils;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 国密工具
 *
 * @author DEMO
 */
public class OpenApiSmUtil {

    private final static byte[] SM4_CBC_IV = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static final X9ECParameters x9ce = GMNamedCurves.getByName("sm2p256v1");
    private static final ECDomainParameters ecDomainParameters = new ECDomainParameters(x9ce.getCurve(), x9ce.getG(), x9ce.getN());
    private static final ECParameterSpec ecParameterSpec = new ECParameterSpec(x9ce.getCurve(), x9ce.getG(), x9ce.getN());

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /*************************************************************************************
     *************************************** 加验签 ***************************************
     *************************************************************************************/

    /**
     * SM3摘要计算
     *
     * @param data 输入
     * @return 输出摘要结果
     */
    public static String digestHexBySm3(String data) {
        try {
            SM3Digest sm3Digest = new SM3Digest();
            sm3Digest.update(data.getBytes("UTF-8"), 0, data.getBytes("UTF-8").length);
            byte[] ret = new byte[sm3Digest.getDigestSize()];
            sm3Digest.doFinal(ret, 0);
            return Hex.toHexString(ret);
        } catch (Exception e) {
            throw new RuntimeException("SM3摘要计算出现异常");
        }
    }

    /**
     * SM2私钥签名
     *
     * @param privateKeyHex SM2私钥串（Hex）
     * @param dataStr       待签名字符串
     * @return
     * @throws Exception
     */
    public static String signHexBySm3WithSm2(String privateKeyHex, String dataStr) throws Exception {

        byte[] key = Hex.decode(privateKeyHex);
        byte[] data = dataStr.getBytes();

        // 获得一条签名曲线
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        // 构造domain函数
        ECDomainParameters domainParameters = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());

        // 国密要求，ID默认值为1234567812345678
        ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(new BigInteger(1, key), domainParameters);
        ParametersWithID parameters = new ParametersWithID(privateKey, "1234567812345678".getBytes());

        // 初始化签名实例
        SM2Signer signer = new SM2Signer();
        signer.init(true, parameters);
        signer.update(data, 0, data.length);

        // 计算签名值
        byte[] signature = decodeDERSignature(signer.generateSignature());

        return Hex.toHexString(signature);
    }

    /**
     * SM2公钥验签
     *
     * @param publicKeyHex SM2公钥（Hex）
     * @param dataStr      待验证数据
     * @param signStr      签名字符串（Base64）
     * @return
     * @throws Exception
     */
    public static boolean checkSignBySm2(String publicKeyHex, String dataStr, String signStr) throws Exception {
        BCECPublicKey bcecPublicKey = getBCECPublicKeyByPublicKeyHex(publicKeyHex);
        ECPublicKeyParameters publicKey = new ECPublicKeyParameters(bcecPublicKey.getQ(), ecDomainParameters);
        byte[] msg = dataStr.getBytes();
        byte[] signature = SmUtil.rsAsn1ToPlain(Base64Decoder.decode(signStr));
        SM2Signer signer = new SM2Signer();
        ParametersWithID parameters = new ParametersWithID(publicKey, "1234567812345678".getBytes());
        signer.init(false, parameters);
        signer.update(msg, 0, msg.length);
        return signer.verifySignature(encodeDERSignature(signature));
    }


    private static byte[] encodeDERSignature(byte[] signature) throws Exception {
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(signature, 0, r, 0, 32);
        System.arraycopy(signature, 32, s, 0, 32);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(new BigInteger(1, r)));
        vector.add(new ASN1Integer(new BigInteger(1, s)));

        try {
            return (new DERSequence(vector)).getEncoded();
        } catch (IOException var6) {
            throw new Exception();
        }
    }

    private static byte[] decodeDERSignature(byte[] signature) throws Exception {
        ASN1InputStream stream = new ASN1InputStream(new ByteArrayInputStream(signature));

        try {
            ASN1Sequence primitive = (ASN1Sequence)stream.readObject();
            Enumeration enumeration = primitive.getObjects();
            BigInteger R = ((ASN1Integer)enumeration.nextElement()).getValue();
            BigInteger S = ((ASN1Integer)enumeration.nextElement()).getValue();
            byte[] bytes = new byte[64];
            byte[] r = format(R.toByteArray());
            byte[] s = format(S.toByteArray());
            System.arraycopy(r, 0, bytes, 0, 32);
            System.arraycopy(s, 0, bytes, 32, 32);
            return bytes;
        } catch (Exception var10) {
            throw new Exception();
        }
    }

    /*************************************************************************************
     *************************************** 加解密 ***************************************
     *************************************************************************************/


    /**
     * 数字信封加密-全报文加密
     *
     * @param data         待加密数据-JSON字符串
     * @param publicKeyHex SM2公钥-HEX格式
     * @return 全报文数字信封-JSON字符串
     */
    public static String encryptDigEvp(String data, String publicKeyHex) {
        // 1 随机生成一个SM4密钥(16位)
        String sm4Key = RandomUtil.randomString(16);
        // 2 SM4加密数据明文，做一层base64编码防止乱码
        String cipData = encryptBase64BySm4(Base64.encode(data.getBytes()), sm4Key);
        // 3 SM2加密对称密钥
        String evpData = encryptBase64C1C3C2BySm2(sm4Key.getBytes(), publicKeyHex);
        // 4 封装数字信封
        Map<String, Object> digEvpMap = new HashMap<String, Object>(8);
        digEvpMap.put("xCipTxt", cipData);             // 传输数据密文
        digEvpMap.put("zCipLen", cipData.length());    // 传输数据密文长度
        digEvpMap.put("zDigEvp", evpData);             // 密钥信封密文
        digEvpMap.put("zEvpLen", evpData.length());    // 密钥信封密文长度
        return JSONUtil.toJsonStr(digEvpMap);
    }


    /**
     * 国密数字信封解密demo
     *
     * @throws Exception
     */
    public static String decryptDigEvp(String data, String privateKeyHex) {
        try {
            JSONObject dataMap = JSONUtil.parseObj(data);
            String zCipTxt = dataMap.getStr("zCipTxt", "");
            String zDigEvp = dataMap.getStr("zDigEvp", "");
            if (CharSequenceUtil.isNotBlank(zCipTxt)) {
                byte[] sm4KeyBytes = decryptC1C3C2BySm2(zDigEvp, privateKeyHex);
                return decryptBase64BySm4(Base64.decode(zCipTxt.getBytes()), sm4KeyBytes);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException("数字信封解密失败", e);
        }

    }


    /**
     * 国密SM4加密
     *
     * @param sm4Key SM4密钥串
     * @param data   待加密数据
     * @return 加密结果（Base64编码）
     */
    public static String encryptBase64BySm4(byte[] data, String sm4Key) {
        byte[] sm4KeyBytes = sm4Key.getBytes();
        if (sm4KeyBytes.length != 16) {
            throw new RuntimeException("err sm4key length");
        }
        try {
            SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, sm4KeyBytes, SM4_CBC_IV);
            return sm4.encryptBase64(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 国密SM4解密
     *
     * @param sm4KeyBytes SM4密钥串
     * @param data        待解密数据
     * @return 解密结果
     */
    public static String decryptBase64BySm4(byte[] data, byte[] sm4KeyBytes) {
        if (sm4KeyBytes.length != 16) {
            throw new RuntimeException("err sm4key length");
        }
        try {
            SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, sm4KeyBytes, SM4_CBC_IV);
            String base64Str = sm4.decryptStr(data);
            return new String(Base64.decode(base64Str));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * c1||c3||c2密文（Base64编码）
     *
     * @param data         待加密数据
     * @param publicKeyHex SM2公钥
     * @return c1||c3||c2密文（Base64编码）
     */
    public static String encryptBase64C1C3C2BySm2(byte[] data, String publicKeyHex) {
        byte[] result = encryptC1C3C2BySm2(data, publicKeyHex);
        return new String(Base64.encode(result));
    }

    /**
     * 明文
     *
     * @param data          待解密数据数据c1||c3||c2密文
     * @param privateKeyHex SM2私钥(Hex格式）
     * @return 明文数据
     */
    public static byte[] decryptC1C3C2BySm2(String data, String privateKeyHex) throws Exception {
        byte[] data1 = convertC1C3C2ToC1C2C3(Base64.decode(data.getBytes()));
        return decryptC1C2C3BySm2(data1, privateKeyHex);
    }


    /**
     * c1||c3||c2密文
     *
     * @param data         待加密数据
     * @param publicKeyHex SM2公钥
     * @return c1||c3||c2密文
     */
    private static byte[] encryptC1C3C2BySm2(byte[] data, String publicKeyHex) {
        byte[] data1 = encryptC1C2C3BySm2(data, publicKeyHex);
        return SmUtil.changeC1C2C3ToC1C3C2(data1, ecDomainParameters); //也可使用 convertC1C2C3ToC1C3C2(data1);
    }


    /**
     * c1||c2||c3密文
     *
     * @param data         待加密数据
     * @param publicKeyHex SM2公钥
     * @return c1||c2||c3密文
     */
    private static byte[] encryptC1C2C3BySm2(byte[] data, String publicKeyHex) {
        BCECPublicKey bcecPublicKey = getBCECPublicKeyByPublicKeyHex(publicKeyHex);
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(bcecPublicKey.getQ(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 明文
     *
     * @param data          待解密数据数据c1||c2||c3密文
     * @param privateKeyHex SM2私钥
     * @return 明文
     */
    private static byte[] decryptC1C2C3BySm2(byte[] data, String privateKeyHex) {
        BCECPrivateKey key = getBCECPrivateKeyByPrivateKeyHex(privateKeyHex);
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(key.getD(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, ecPrivateKeyParameters);
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }


    /*************************************************************************************
     ************************************** 公共方法 **************************************
     *************************************************************************************/

    /**
     * 格式转化：将BC C1C2C3 SM2密文转化为ASN1格式C1C3C2密文
     *
     * @return byte[] ASN1格式密文
     */
    private static byte[] convertC1C2C3ToC1C3C2(byte[] bcCipTxt) {

        //去除RAW密文开头的 04 标志位
        int bcCipLen = bcCipTxt.length - 1;
        if (96 >= bcCipLen) {
            throw new RuntimeException("bc cipTxt length error:" + bcCipLen);
        }

        //计算密文部分实际长度
        int C3Len = bcCipLen - 96;

        byte[] keyX = new byte[32];
        byte[] keyY = new byte[32];
        byte[] C3 = new byte[C3Len];
        byte[] C2 = new byte[32];

        System.arraycopy(bcCipTxt, 1, keyX, 0, 32);
        System.arraycopy(bcCipTxt, 33, keyY, 0, 32);
        System.arraycopy(bcCipTxt, 65, C3, 0, C3Len);
        System.arraycopy(bcCipTxt, 65 + C3Len, C2, 0, 32);

        byte[] netSignCipTxt = new byte[bcCipLen + 13];

        //keyX补位
        int wPos = 4;
        netSignCipTxt[0] = 0x30;
        netSignCipTxt[2] = 0x02;
        if ((keyX[0] & 0xFF) >= 128) {
            netSignCipTxt[wPos - 1] = 0x21;
            netSignCipTxt[wPos] = 0x00;
            wPos += 1;
        } else {
            netSignCipTxt[wPos - 1] = 0x20;
        }
        System.arraycopy(keyX, 0, netSignCipTxt, wPos, 32);
        wPos += 32;

        //keyY补位
        netSignCipTxt[wPos] = 0x02;
        wPos += 1;
        if ((keyY[0] & 0xFF) >= 128) {
            netSignCipTxt[wPos] = 0x21;
            wPos += 1;
            netSignCipTxt[wPos] = 0x00;
            wPos += 1;
        } else {
            netSignCipTxt[wPos] = 0x20;
            wPos += 1;
        }
        System.arraycopy(keyY, 0, netSignCipTxt, wPos, 32);
        wPos += 32;

        //copy C2
        netSignCipTxt[wPos] = 0x04;
        wPos += 1;
        netSignCipTxt[wPos] = 0x20;
        wPos += 1;
        System.arraycopy(C2, 0, netSignCipTxt, wPos, 32);
        wPos += 32;

        //copy C3
        netSignCipTxt[wPos] = 0x04;
        wPos += 1;
        netSignCipTxt[wPos] = (byte)C3Len;
        wPos += 1;
        System.arraycopy(C3, 0, netSignCipTxt, wPos, C3Len);
        wPos += C3Len;

        //总长度
        netSignCipTxt[1] = (byte)(wPos - 2);

        byte[] resultBytes = new byte[wPos];
        System.arraycopy(netSignCipTxt, 0, resultBytes, 0, wPos);

        return resultBytes;
    }

    /**
     * 格式转化：将ASN1格式C1C3C2密文转化为BC SM2 C1C2C3密文
     *
     * @return byte[] BC SM2 密文
     */
    private static byte[] convertC1C3C2ToC1C2C3(byte[] asn1CipTxt) throws Exception {

        //截取keyX
        int wPos = 3;
        if ((asn1CipTxt[wPos] & 0xFF) == 32) {
            wPos += 1;
        } else if ((asn1CipTxt[wPos] & 0xFF) == 33) {
            wPos += 2;
        } else {
            throw new Exception("keyX length Error!");
        }
        byte[] keyX = new byte[32];
        System.arraycopy(asn1CipTxt, wPos, keyX, 0, 32);
        wPos += 32;

        //截取keyY
        wPos += 1;
        if ((asn1CipTxt[wPos] & 0xFF) == 32) {
            wPos += 1;
        } else if ((asn1CipTxt[wPos] & 0xFF) == 33) {
            wPos += 2;
        } else {
            throw new Exception("keyY length Error!");
        }
        byte[] keyY = new byte[32];
        System.arraycopy(asn1CipTxt, wPos, keyY, 0, 32);
        wPos += 32;

        //截取C2
        wPos += 1;
        if ((asn1CipTxt[wPos] & 0xFF) == 32) {
            wPos += 1;
        } else {
            throw new Exception("C2 length Error!");
        }
        byte[] C2 = new byte[32];
        System.arraycopy(asn1CipTxt, wPos, C2, 0, 32);
        wPos += 32;

        //截取C3
        wPos += 1;
        int C3Len = asn1CipTxt[wPos] & 0xFF;
        if (0 < C3Len) {
            wPos += 1;
        } else {
            throw new Exception("C3 length Error!");
        }

        byte[] C3 = new byte[C3Len];
        System.arraycopy(asn1CipTxt, wPos, C3, 0, C3Len);
        wPos += C3Len;

        //组装
        byte[] resultBytes = new byte[97 + C3Len];

        resultBytes[0] = 0x04;
        System.arraycopy(keyX, 0, resultBytes, 1, 32);
        System.arraycopy(keyY, 0, resultBytes, 33, 32);
        System.arraycopy(C3, 0, resultBytes, 65, C3Len);
        System.arraycopy(C2, 0, resultBytes, 65 + C3Len, 32);

        return resultBytes;
    }


    private static BCECPublicKey getBCECPublicKeyByPublicKeyHex(String pubKeyHex) {
        //截取64字节有效的SM2公钥（如果公钥首个字节为0x04）
        if (pubKeyHex.length() > 128) {
            pubKeyHex = pubKeyHex.substring(pubKeyHex.length() - 128);
        }
        //将公钥拆分为x,y分量（各32字节）
        String stringX = pubKeyHex.substring(0, 64);
        String stringY = pubKeyHex.substring(stringX.length());
        //将公钥x、y分量转换为BigInteger类型
        BigInteger x = new BigInteger(stringX, 16);
        BigInteger y = new BigInteger(stringY, 16);
        //通过公钥x、y分量创建椭圆曲线公钥规范
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(x9ce.getCurve().createPoint(x, y), ecParameterSpec);
        //通过椭圆曲线公钥规范，创建出椭圆曲线公钥对象（可用于SM2加密及验签）
        return new BCECPublicKey("EC", ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }


    private static BCECPrivateKey getBCECPrivateKeyByPrivateKeyHex(String privateKeyHex) {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);
        return new BCECPrivateKey("EC", ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }


    private static byte[] format(byte[] value) {
        if (value.length == 32) {
            return value;
        } else {
            byte[] bytes = new byte[32];
            if (value.length > 32) {
                System.arraycopy(value, value.length - 32, bytes, 0, 32);
            } else {
                System.arraycopy(value, 0, bytes, 32 - value.length, value.length);
            }

            return bytes;
        }
    }

}