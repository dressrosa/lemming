/**
 * 
 */
package com.xiaoyu.lemming.core.api;

import java.util.List;

/**
 * @author xiaoyu
 * @date 2019-03
 * @description
 */
public interface Exchanger {

    void allocate(List<LemmingTask> tasks);

    // void receive(LemmingTask task);

    void execute(LemmingTask task);

    void start();

    void close();
}
