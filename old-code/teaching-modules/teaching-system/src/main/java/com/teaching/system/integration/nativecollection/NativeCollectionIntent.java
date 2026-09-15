package com.teaching.system.integration.nativecollection;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** 独立于旧bootstrap：只证明V1办理人，不激活账号、不创建V2会话。来源ID在浏览器中保持加密。 */
public final class NativeCollectionIntent {
    public static final String PURPOSE = "V1_NATIVE_SETTLEMENT_V1";
    private final String secret;
    private final Clock clock;
    public NativeCollectionIntent(String secret, Clock clock) { this.secret=secret; this.clock=clock; }
    public record Entry(String entryPath, Instant expiresAt) {
        @Override public String toString() { return "NativeCollectionEntry[redacted]"; }
    }
    public Entry issue(long userId) {
        if (userId<=0 || secret==null || secret.length()<32) throw new IllegalStateException("Native settlement key unavailable");
        try {
            long now=clock.instant().getEpochSecond(), expires=now+1800;
            byte[] iv=new byte[12]; new SecureRandom().nextBytes(iv);
            byte[] key=MessageDigest.getInstance("SHA-256").digest((PURPOSE+"\n"+secret).getBytes(StandardCharsets.UTF_8));
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            cipher.updateAAD(PURPOSE.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted=cipher.doFinal(ByteBuffer.allocate(24).putLong(userId).putLong(now).putLong(expires).array());
            byte[] packed=ByteBuffer.allocate(iv.length+encrypted.length).put(iv).put(encrypted).array();
            java.util.Arrays.fill(key,(byte)0);
            String token="n1."+Base64.getUrlEncoder().withoutPadding().encodeToString(packed);
            return new Entry("/v2-native/native-settlement#intent="+token,Instant.ofEpochSecond(expires));
        } catch (java.security.GeneralSecurityException failure) { throw new IllegalStateException("Native settlement encryption unavailable",failure); }
    }
}
