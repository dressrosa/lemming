/**
 * 
 */
package com.xiaoyu.lemming.core.api;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public interface Context {

    String side();

    void start();

    LemmingTask getLocalTask(String app, String taskId);

    void initTransporter(String transporter);

    void close();
}
