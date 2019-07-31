/**
 * 
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.common.entity.ExecuteResult;

/**
 * 通用rpc service
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public interface LemmingServerService {

    /**
     * client执行完成后,进行回调
     * 
     * @param result
     * @throws Exception
     */
    void callback(ExecuteResult result) throws Exception;
}
