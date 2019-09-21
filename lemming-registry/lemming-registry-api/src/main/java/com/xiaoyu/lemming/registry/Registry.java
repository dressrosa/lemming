/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.registry;

import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Registry {

    /**
     * 注册地址
     * 
     * @param addr
     */
    void address(String addr);

    /**
     * 关闭注册中心
     */
    void close();

    /**
     * 是否连接
     * 
     * @param addr
     * @return
     */
    boolean isInit();

    /**
     * 注册任务
     * 
     * @param service
     */
    void registerTask(LemmingTask task);

    void initServerListener();

    void storeLocalTask(LemmingTask task);

    /**
     * @param group
     * @param taskId
     * @return
     */
    LemmingTask getLocalTask(String group, String taskId);
}
