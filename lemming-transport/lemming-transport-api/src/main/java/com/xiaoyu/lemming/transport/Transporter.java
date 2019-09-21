/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
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
    void callback(ExecuteResult result) throws Exception;

    /**
     * server调用client
     * 
     * @param task
     * @param client
     * @return
     * @throws Exception
     */
    ExecuteResult call(LemmingTask task, LemmingTaskClient client) throws Exception;

    /**
     * server暴露给client回调接口
     * client暴露给server调用接口
     */
    void export() throws Exception;

}
