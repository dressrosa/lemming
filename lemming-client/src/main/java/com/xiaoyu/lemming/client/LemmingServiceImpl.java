/**
 * 
 */
package com.xiaoyu.lemming.client;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.utils.NetUtil;
import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
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
    public ExecuteResult handleTask(LemmingTask req) throws Exception {
        ExecuteResult ret = new ExecuteResult();
        Context context = SpiManager.defaultSpiExtender(Context.class);
        LemmingTask task = context.getLocalTask(req.getTaskId(), req.getGroup());
        if (task == null) {
            logger.error(" None task exist which taskId is '" + req.getTaskId() + "'");
            ret.setSuccess(false).setMessage(
                    " Task[" + req.getTaskId() + "] not exist  in mathine " + NetUtil.localIP());
            return ret;
        }
        if (task.isRunning()) {
            // TODO
            logger.warn(" Task->" + req.getTaskId() + " is running .");
            ret.setSuccess(true)
                    .setMessage(" Task[" + req.getTaskId() + "] is running in mathine " + NetUtil.localIP());
            return ret;
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
                        ret.setSuccess(true).setMessage("succes in mathine " + NetUtil.localIP());
                        return ret;
                    }
                }
            } else {
                Class<?> target = Class.forName(task.getTaskImpl());
                Method[] methods = target.getDeclaredMethods();
                for (Method d : methods) {
                    if (d.getName().equals(CommonConstant.Task_Method)) {
                        task.setRunning(true);
                        d.invoke(target.newInstance(), req.getParams());
                        ret.setSuccess(true).setMessage("success in mathine " + NetUtil.localIP());
                        return ret;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" Task->" + req.getTaskId() + " execute failed:" + e);
            ret.setSuccess(false).setMessage(" Task[" + req.getTaskId() + "] execute failed:" + e.getMessage());
            return ret;
        } finally {
            if (task.isRunning()) {
                task.setRunning(false);
            }
        }
        return ret;
    }

}
