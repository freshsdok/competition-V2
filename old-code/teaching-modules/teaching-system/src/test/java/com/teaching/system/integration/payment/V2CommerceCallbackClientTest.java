package com.teaching.system.integration.payment;

import static org.junit.Assert.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class V2CommerceCallbackClientTest {
    private HttpServer server;
    private int status=200;
    private String response="{\"version\":\"0.0.1\",\"encoding\":\"UTF-8\",\"signMethod\":\"02\",\"returnCode\":\"SUCCESS\",\"respCode\":\"SUCCESS\",\"sign\":\"signed-ack\"}";
    private final AtomicReference<Map<String,String>> received = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    @Before public void start() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            path.set(exchange.getRequestURI().getPath());
            String body=new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
            Map<String,String> fields=new LinkedHashMap<>();
            for(String pair:body.split("&")) {
                String[] parts=pair.split("=",2);
                fields.put(URLDecoder.decode(parts[0],StandardCharsets.UTF_8),URLDecoder.decode(parts[1],StandardCharsets.UTF_8));
            }
            received.set(fields);
            byte[] output=response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Location","/redirected");
            exchange.sendResponseHeaders(status,output.length);
            try(var stream=exchange.getResponseBody()){stream.write(output);}
        });server.start();
    }
    @After public void stop(){server.stop(0);}
    private String base(){return "http://127.0.0.1:"+server.getAddress().getPort()+"/api/v1";}
    private V2CommerceCallbackClient client(){return new V2CommerceCallbackClient(base(),3,true,1000,1000);}
    @Test public void transportsDecodedFieldsUnchangedAndReturnsSignedAck(){
        var input=Map.of("biz_content","{\"body\":\"中文 + & =\"}","sign","a+b/c==");
        var ack=client().forwardPayment(input);
        assertEquals(input,received.get());assertEquals("/api/v1/public/commerce/cmb-callbacks/3",path.get());
        assertEquals("signed-ack",ack.get("sign"));assertEquals("SUCCESS",ack.get("respCode"));
    }
    @Test public void preservesSignedNegativeAck(){
        response="{\"version\":\"0.0.1\",\"encoding\":\"UTF-8\",\"signMethod\":\"02\",\"returnCode\":\"FAIL\",\"respMsg\":\"SIGNATURE_INVALID\",\"sign\":\"signed-failure\"}";
        assertEquals("FAIL",client().forwardPayment(Map.of("sign","x")).get("returnCode"));
    }
    @Test public void rejectsRedirectHttpFailureAndUnsignedAck(){
        status=302;assertThrows(IllegalStateException.class,()->client().forwardPayment(Map.of("sign","x")));
        assertEquals("/api/v1/public/commerce/cmb-callbacks/3",path.get());
        status=503;assertThrows(IllegalStateException.class,()->client().forwardPayment(Map.of("sign","x")));
        status=200;response="{\"returnCode\":\"SUCCESS\",\"respCode\":\"SUCCESS\"}";
        assertThrows(IllegalStateException.class,()->client().forwardPayment(Map.of("sign","x")));
    }
    @Test public void validatesExplicitHttpOptInAndProviderId(){
        assertThrows(IllegalArgumentException.class,()->new V2CommerceCallbackClient(base(),3,false,1000,1000));
        assertThrows(IllegalArgumentException.class,()->new V2CommerceCallbackClient(base(),0,true,1000,1000));
        assertThrows(IllegalArgumentException.class,()->new V2CommerceCallbackClient(base()+"?query=1",3,true,1000,1000));
    }
}
