package com.xiaoyu.lemmingtest;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.xiaoyu.lemming.common.entity.LemmingParam;
import com.xiaoyu.lemming.core.schedule.LemmingTaskWrapper;

@Component
public class HelloTask extends LemmingTaskWrapper {

    private static final long serialVersionUID = 1L;

    @Override
    public void handle(LemmingParam lemmingParam) {
        try {
            TimeUnit.MILLISECONDS.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("执行任务,打印字符串:helloTask");
    }

}
