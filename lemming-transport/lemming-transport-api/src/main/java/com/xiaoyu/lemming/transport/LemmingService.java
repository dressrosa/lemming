/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * 通用rpc service
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public interface LemmingService {

    public boolean handleTask(LemmingTask task) throws Exception;
}
