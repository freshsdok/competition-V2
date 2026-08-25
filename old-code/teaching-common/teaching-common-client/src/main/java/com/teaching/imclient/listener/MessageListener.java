package com.teaching.imclient.listener;


import com.teaching.imcommon.model.IMSendResult;

import java.util.List;

public interface MessageListener<T> {

    void process(List<IMSendResult<T>> result);

}
