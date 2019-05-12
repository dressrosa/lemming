/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Transporter {

    void callback(LemmingTask task);

    ExecuteResult call(LemmingTask task) throws Exception;
    
    void export();
}
