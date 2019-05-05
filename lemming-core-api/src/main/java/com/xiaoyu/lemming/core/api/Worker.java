/**
 * 
 */
package com.xiaoyu.lemming.core.api;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Worker {

    /**
     * 处理任务
     * 
     * @param task
     */
    void handle(LemmingTask task);

    /**
     * 接受任务
     * 
     * @param tasks
     */
    void accept(LemmingTask task);

    /**
     * 是否繁忙
     * 
     * @return
     */
    boolean isBusy();

    /**
     * 是否在工作
     * 
     * @return
     */
    boolean isWorking();

    /**
     * 暂停工作
     */
    void suspend(boolean pause);

    /**
     * 下岗
     */
    void laidOff();

}
