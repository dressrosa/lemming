/**
 * 
 */
package com.xiaoyu.lemming.client;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.transport.LemmingService;

/**
 * 这个需要在启动时,就暴露成rpc/http接口.
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class LemmingServiceImpl implements LemmingService {

    private static final Logger logger = LoggerFactory.getLogger(LemmingServiceImpl.class);

    @Override
    public boolean handleTask(LemmingTask req) throws Exception {
        Context context = SpiManager.defaultSpiExtender(Context.class);
        LemmingTask task = context.getLocalTask(req.getTaskId(), req.getGroup());
        if (task == null) {
            logger.error("none task exist which taskId is '" + req.getTaskId() + "'");
            return false;
        }
        if (task.isRunning()) {
            logger.warn("task->" + req.getTaskId() + " is running .");
            return true;
            // TODO
        }
        try {
            Object proxy = task.getProxy();
            if (proxy != null) {
                Class<?> cl1 = proxy.getClass();
                Method[] methods = null;
                // spring java原生代理
                if (cl1.getName().equals(task.getTaskImpl())) {
                    methods = cl1.getMethods();
                } else {
                    // spring cglib代理
                    methods = cl1.getSuperclass().getDeclaredMethods();
                }
                for (Method d : methods) {
                    if (d.getName().equals(CommonConstant.Task_Method)) {
                        task.setRunning(true);
                        d.invoke(proxy, req.getParams());
                        return true;
                    }
                }
            } else {
                Class<?> target = Class.forName(task.getTaskImpl());
                Method[] methods = target.getDeclaredMethods();
                for (Method d : methods) {
                    if (d.getName().equals(CommonConstant.Task_Method)) {
                        task.setRunning(true);
                        d.invoke(target.newInstance(), req.getParams());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (task.isRunning()) {
                task.setRunning(false);
            }
        }
        return false;
    }

}
