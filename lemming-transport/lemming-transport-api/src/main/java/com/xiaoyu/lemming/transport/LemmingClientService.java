/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * 通用rpc service
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public interface LemmingClientService {

    ExecuteResult handleTask(LemmingTask task) throws Exception;
}
