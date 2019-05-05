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
public interface Exchanger {

    void allocate(List<LemmingTask> tasks);

    void accept(LemmingTask task);

    void start();

    void close();
}
