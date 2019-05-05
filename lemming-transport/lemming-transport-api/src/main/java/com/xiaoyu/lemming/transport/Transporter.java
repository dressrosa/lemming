/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Transporter {

    void callback(LemmingTask task);

    void call(LemmingTask task);
    
    void export();
}
