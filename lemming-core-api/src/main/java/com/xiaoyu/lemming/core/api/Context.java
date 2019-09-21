/**
 * 
 */
package com.xiaoyu.lemming.core.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public interface Context {

    String side();

    void start();

    LemmingTask getLocalTask(String group, String taskId);

    void initTransporter(String transporter);

    void close();

    <T> Future<T> submit(Callable<T> runnable);

    int getActiveTaskCount();

}
