package com.teaching.system.integration.nativecollection;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
public class NativeCollectionIntentTest {
    private static final String SECRET="native-settlement-unit-test-secret-0001";
    @Test public void encryptedEntryHasSourceLifetimeRandomNonceAndNoPlainIdentity() throws Exception {
        var issuer=new NativeCollectionIntent(SECRET,Clock.fixed(Instant.ofEpochSecond(1800000000),ZoneOffset.UTC));
        var entry=issuer.issue(56469);
        assertTrue(entry.entryPath().matches("/v2-native/native-settlement#intent=n1\\.[A-Za-z0-9_-]{70}"));
        assertEquals(Instant.ofEpochSecond(1800001800),entry.expiresAt());
        assertFalse(entry.toString().contains(entry.entryPath()));
        assertNotEquals(entry.entryPath(),issuer.issue(56469).entryPath());
        byte[] packed=Base64.getUrlDecoder().decode(entry.entryPath().split("n1\\.")[1]);
        byte[] key=MessageDigest.getInstance("SHA-256").digest(("V1_NATIVE_SETTLEMENT_V1\n"+SECRET).getBytes(StandardCharsets.UTF_8));
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,Arrays.copyOf(packed,12)));
        cipher.updateAAD("V1_NATIVE_SETTLEMENT_V1".getBytes(StandardCharsets.UTF_8));
        ByteBuffer b=ByteBuffer.wrap(cipher.doFinal(Arrays.copyOfRange(packed,12,packed.length)));
        assertEquals(56469,b.getLong());assertEquals(1800000000,b.getLong());assertEquals(1800001800,b.getLong());
    }
    @Test public void missingKeyCannotIssueEntry() {
        assertThrows(IllegalStateException.class,()->new NativeCollectionIntent(null,Clock.systemUTC()).issue(1));
        assertThrows(IllegalStateException.class,()->new NativeCollectionIntent(SECRET,Clock.systemUTC()).issue(0));
    }
}
