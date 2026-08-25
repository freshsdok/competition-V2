package com.teaching.imclient.listener;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.imclient.annotation.IMListener;
import com.teaching.imcommon.enums.IMListenerType;
import com.teaching.imcommon.model.IMSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Component
public class MessageListenerMulticaster {

    @Autowired(required = false)
    private List<MessageListener> messageListeners = Collections.emptyList();

    public void multicast(IMListenerType listenerType, List<IMSendResult> results) {
        if (CollUtil.isEmpty(results)) {
            return;
        }
        for (MessageListener listener : messageListeners) {
            IMListener annotation = listener.getClass().getAnnotation(IMListener.class);
            if (annotation != null && (annotation.type().equals(IMListenerType.ALL) || annotation.type().equals(listenerType))) {
                results.forEach(result -> {
                    // 将data转回对象类型
                    if (result.getData() instanceof JSONObject) {
                        Type superClass = listener.getClass().getGenericInterfaces()[0];
                        Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                        JSONObject data = (JSONObject) result.getData();
                        result.setData(data.toJavaObject(type));
                    }
                });
                // 回调到调用方处理
                listener.process(results);
            }
        }
    }


}
