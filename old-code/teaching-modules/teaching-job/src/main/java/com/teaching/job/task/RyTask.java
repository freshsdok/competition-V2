package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteContentService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 定时任务调度测试
 * 
 * @author cesoft
 */
@Service("ryTask")
public class RyTask {

    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }

    public void ryParams(String params) {

        System.out.println("执行有参方法：" + params);
    }

    public void ryNoParams() {
        System.out.println("执行无参方法");
    }
}
