package com.teaching.system.integration.payment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.mapper.OrderInfoMapper;
import com.teaching.system.service.impl.OrderInfoServiceImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class V2CommerceCallbackDispatcherTest {
    private final OrderInfoMapper orders = mock(OrderInfoMapper.class);
    private final V2CommerceCallbackClient client = mock(V2CommerceCallbackClient.class);
    private final V2CommerceCallbackDispatcher dispatcher = new V2CommerceCallbackDispatcher(orders, client);
    private final String id = "43721258A9B545ABBE7C83F57C60B431";
    private Map<String,String> fields(String order) {
        return new LinkedHashMap<>(Map.of("version","0.0.1","encoding","UTF-8","signMethod","02",
                "sign","original+/signature=", "biz_content","{\"orderId\":\""+order+"\",\"txnAmt\":\"1\"}"));
    }

    @Test public void currentV2OrderReachesForwarderBeforeLegacyRemovesSignature() throws Exception {
        var input = fields(id); var original = new LinkedHashMap<>(input);
        var ack = Map.of("returnCode","SUCCESS","respCode","SUCCESS","sign","v2-ack-signature");
        when(client.forwardPayment(original)).thenReturn(ack);
        var service = new OrderInfoServiceImpl();
        var field = OrderInfoServiceImpl.class.getDeclaredField("v2CommerceCallbackDispatcher");
        field.setAccessible(true); field.set(service,dispatcher);
        assertSame(ack,service.paymentCallback(input));
        assertEquals(original,input);
        verify(client).forwardPayment(original);
        verify(orders).selectOrderInfoByOrderId(id);
    }

    @Test public void existingV1UuidNeverForwards() {
        when(orders.selectOrderInfoByOrderId(id)).thenReturn(new OrderInfo());
        assertTrue(dispatcher.forwardPaymentIfUnknown(fields(id)).isEmpty());
        verifyNoInteractions(client);
    }

    @Test public void supportsHistoricalPrefixAndFlatForm() {
        var prefixed = fields("V2"+"A".repeat(30));
        when(client.forwardPayment(anyMap())).thenReturn(Map.of("returnCode","FAIL","sign","signed-failure"));
        assertEquals("FAIL",dispatcher.forwardPaymentIfUnknown(prefixed).orElseThrow().get("returnCode"));
        assertTrue(dispatcher.forwardPaymentIfUnknown(Map.of("orderId",id,"sign","original")).isPresent());
    }

    @Test public void forwardingFailureNeverFallsBackToV1Settlement() {
        when(client.forwardPayment(anyMap())).thenThrow(new IllegalStateException("upstream details"));
        assertThrows(IllegalStateException.class,()->dispatcher.forwardPaymentIfUnknown(fields(id)));
        verify(client).forwardPayment(anyMap());
    }

    @Test public void malformedUnsignedAndRefundRequestsAreNotRoutedAsPayment() {
        assertTrue(dispatcher.forwardPaymentIfUnknown(null).isEmpty());
        assertTrue(dispatcher.forwardPaymentIfUnknown(fields("invalid")).isEmpty());
        assertTrue(dispatcher.forwardPaymentIfUnknown(Map.of("sign","x","biz_content","broken")).isEmpty());
        assertTrue(dispatcher.forwardPaymentIfUnknown(Map.of("sign","x","orderId",id,"refundAmt","1")).isEmpty());
        var unsigned=fields(id);unsigned.remove("sign");
        assertTrue(dispatcher.forwardPaymentIfUnknown(unsigned).isEmpty());
        verifyNoInteractions(orders,client);
    }
}
