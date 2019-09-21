/**
 * 
 */
package com.xiaoyu.lemming.core.api;

import java.util.List;

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
    void handle(LemmingTask task) throws Exception;

    /**
     * 接受任务
     * 
     * @param tasks
     */
    boolean accept(LemmingTask task);

    boolean accept(List<LemmingTask> tasks);

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

    boolean isLaidOff();

    String name();

    LemmingTask getTask(LemmingTask query);
}
