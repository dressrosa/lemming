/**
 * 
 */
package com.xiaoyu.lemming.client;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.NetUtil;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.transport.LemmingClientService;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * 这个需要在启动时,就暴露成rpc/http接口.
 * 
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class LemmingServiceClientImpl implements LemmingClientService {

    private static final Logger logger = LoggerFactory.getLogger(LemmingServiceClientImpl.class);

    @Override
    public ExecuteResult handleTask(LemmingTask req) throws Exception {
        Context context = SpiManager.defaultSpiExtender(Context.class);
        LemmingTask task = context.getLocalTask(req.getApp(), req.getTaskId());
        ExecuteResult ret = new ExecuteResult();
        if (task == null) {
            logger.error(" None task exist which taskId is '" + req.getTaskId() + "'");
            ret.setHost(NetUtil.localIP())
                    .setSuccess(false)
                    .setMessage(" Task[" + req.getApp() + ":" + req.getTaskId() + "] not exist  in mathine "
                            + NetUtil.localIP());
            return ret;
        }
        if (task.isRunning()) {
            // TODO
            logger.warn(" Task->" + req.getTaskId() + " is running.");
            ret.setHost(NetUtil.localIP())
                    .setSuccess(true)
                    .setMessage(" Task[" + req.getTaskId() + "] is running in mathine " + NetUtil.localIP());
            return ret;
        }
        return this.process(context, task, req);
    }

    private ExecuteResult process(Context context, LemmingTask task, LemmingTask req) throws Exception {
        Future<ExecuteResult> future = context.getProcessor().submit(() -> {
            ExecuteResult ret = new ExecuteResult();
            ret.setTaskId(req.getTaskId())
                    .setApp(req.getApp())
                    .setGroup(req.getGroup())
                    .setTraceId(req.getTraceId());
            try {
                Object proxy = task.getProxy();
                if (proxy != null) {
                    Class<?> cl1 = proxy.getClass();
                    Method d = null;
                    // spring java原生代理
                    if (cl1.getName().equals(task.getTaskImpl())) {
                        d = cl1.getMethod(CommonConstant.Task_Call_Method, new Class<?>[] {});
                    } else {
                        // spring cglib代理
                        d = cl1.getSuperclass().getMethod(CommonConstant.Task_Call_Method, new Class<?>[] {});
                    }
                    task.setRunning(true);
                    d.invoke(proxy, req.getParams());
                    ret.setHost(NetUtil.localIP())
                            .setSuccess(true)
                            .setMessage("Successfully execute in mathine " + NetUtil.localIP());
                } else {
                    Class<?> target = Class.forName(task.getTaskImpl());
                    Method d = target.getMethod(CommonConstant.Task_Call_Method, new Class<?>[] {});
                    task.setRunning(true);
                    d.invoke(target.newInstance(), req.getParams());
                    ret.setHost(NetUtil.localIP())
                            .setSuccess(true)
                            .setMessage("success in mathine " + NetUtil.localIP());
                }
            } catch (Exception e) {
                logger.error(" Task->" + req.getTaskId() + " execute failed:" + e);
                ret.setHost(NetUtil.localIP())
                        .setSuccess(false)
                        .setMessage(" Task[" + req.getTaskId() + "] execute failed:" + e.getMessage());
            } finally {
                if (task.isRunning()) {
                    task.setRunning(false);
                }
            }
            if (!task.isSync()) {
                doCallback(context, ret);
            }
            return ret;
        });
        if (task.isSync()) {
            return future.get();
        }
        return new ExecuteResult().setTaskId(req.getTaskId())
                .setApp(req.getApp())
                .setGroup(req.getGroup())
                .setTraceId(req.getTraceId())
                .setHost(NetUtil.localIP())
                .setSuccess(true)
                .setMessage("Accepted by client,wait asyn execute.");
    }

    private void doCallback(Context context, ExecuteResult result) {
        context.getProcessor().submit(() -> {
            try {
                Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
                transporter.callback(result);
            } catch (Exception e) {
                logger.error(e + "");
            }
        });
    }
}
