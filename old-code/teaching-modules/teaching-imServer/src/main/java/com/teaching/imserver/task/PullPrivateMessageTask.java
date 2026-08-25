package com.teaching.imserver.task;

import com.teaching.imcommon.contant.IMRedisKey;
import com.teaching.imcommon.enums.IMCmdType;
import com.teaching.imcommon.model.IMRecvInfo;
import com.teaching.imcommon.mq.RedisMQListener;
import com.teaching.imserver.netty.processor.AbstractMessageProcessor;
import com.teaching.imserver.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, batchSize = 10)
public class PullPrivateMessageTask extends AbstractPullMessageTask<IMRecvInfo> {

    @Override
    public void onMessage(IMRecvInfo recvInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
        processor.process(recvInfo);
    }

}
