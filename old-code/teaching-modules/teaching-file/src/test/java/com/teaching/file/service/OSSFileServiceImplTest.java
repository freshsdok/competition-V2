package com.teaching.file.service;

import com.teaching.file.config.OSSConfig;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OSSFileServiceImplTest {

    @Test
    public void normalizeManagedObjectKeySupportsRelativeAndManagedFullUrl() throws Exception {
        OSSFileServiceImpl service = service();

        assertEquals("certificate/file.jpg", normalize(service, "/certificate/file.jpg"));
        assertEquals("certificate/file.jpg", normalize(service,
                "https://bucket.example.com/certificate/file.jpg?signature=1"));
    }

    @Test
    public void normalizeManagedObjectKeyRejectsAnotherHost() throws Exception {
        OSSFileServiceImpl service = service();
        try {
            normalize(service, "https://bucket.example.com.evil.invalid/certificate/file.jpg");
        } catch (InvocationTargetException exception) {
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
            return;
        }
        throw new AssertionError("应拒绝非当前Bucket的完整URL");
    }

    private OSSFileServiceImpl service() throws Exception {
        OSSConfig config = new OSSConfig();
        config.setDomain("https://bucket.example.com/");
        OSSFileServiceImpl service = new OSSFileServiceImpl(null, config);
        Field retUrl = OSSFileServiceImpl.class.getDeclaredField("retUrl");
        retUrl.setAccessible(true);
        retUrl.set(service, "https://bucket.example.com/");
        return service;
    }

    private String normalize(OSSFileServiceImpl service, String value) throws Exception {
        Method method = OSSFileServiceImpl.class.getDeclaredMethod("normalizeManagedObjectKey", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }
}
