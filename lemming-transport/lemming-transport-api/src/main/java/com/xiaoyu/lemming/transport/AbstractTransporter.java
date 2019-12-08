/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.transport;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author xiaoyu
 * @date 2019-09
 * @description
 */
public abstract class AbstractTransporter implements Transporter {

    protected static final int Timeout = 60_000;

    @Override
    public ExecuteResult call(LemmingTask task, LemmingTaskClient client) throws Exception {
        return doCall(task, client);
    }

    @Override
    public void callback(ExecuteResult result) throws Exception {
        // do nothing
    }

    @Override
    public void export() throws Exception {
        // do nothing
    }

    public abstract ExecuteResult doCall(LemmingTask task, LemmingTaskClient client) throws Exception;
}
