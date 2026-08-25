package com.teaching.imclient.task;

import com.teaching.imclient.listener.MessageListenerMulticaster;
import com.teaching.imcommon.contant.IMRedisKey;
import com.teaching.imcommon.enums.IMListenerType;
import com.teaching.imcommon.model.IMSendResult;
import com.teaching.imcommon.mq.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_RESULT_SYSTEM_QUEUE, batchSize = 100)
public class SystemMessageResultResultTask extends AbstractMessageResultTask<IMSendResult> {

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void onMessage(List<IMSendResult> results) {
        listenerMulticaster.multicast(IMListenerType.SYSTEM_MESSAGE, results);
    }

}
