/**
 * 
 */
package com.xiaoyu.lemming.core.api;

import java.util.concurrent.ThreadPoolExecutor;

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

    ThreadPoolExecutor getProcessor();
}
