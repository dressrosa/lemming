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

    /**
     * client调用server
     * 
     * @param result
     * @throws Exception
     */
    public void callback(ExecuteResult result) throws Exception;

    /**
     * server调用client
     * 
     * @param task
     * @return
     * @throws Exception
     */
    ExecuteResult call(LemmingTask task) throws Exception;

    void export();

}
