/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.storage.Storage;
import com.xiaoyu.lemming.transport.LemmingServerService;

/**
 * 这个需要在启动时,就暴露成rpc/http接口.
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class LemmingServiceServerImpl implements LemmingServerService {

    private static final Logger logger = LoggerFactory.getLogger(LemmingServiceServerImpl.class);

    @Override
    public void callback(ExecuteResult result) throws Exception {
        Context context = SpiManager.defaultSpiExtender(Context.class);
        process(context, result);

    }

    private void process(Context context, ExecuteResult result) {
        context.getProcessor().submit(() -> {
            // 记录调用
            try {
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                LemmingTask task = new LemmingTask();
                task.setApp(result.getApp())
                        .setTaskId(result.getTaskId())
                        .setGroup(result.getGroup());
                storage.saveLog(task, result);
            } catch (Exception e) {
                logger.error(e + "");
            }
        });
    }
}
