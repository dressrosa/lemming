/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.client;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author xiaoyu
 * @date 2019-04
 * @description
 */
public class ClientContext implements Context {

    /**
     * 用于异步执行任务
     */
    private static final ThreadPoolExecutor Processor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                private final AtomicInteger adder = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Client_Worker_Processor_" + adder.incrementAndGet());
                }
            });

    public ClientContext() {
    }

    @Override
    public String side() {
        return CommonConstant.Client;
    }

    @Override
    public void start() {

    }

    @Override
    public LemmingTask getLocalTask(String group, String taskId) {
        LemmingTask task = null;
        try {
            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
            task = registry.getLocalTask(group, taskId);
        } catch (Exception e) {
        }
        return task;
    }

    @Override
    public void close() {
        try {
            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
            registry.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void initTransporter(String transporter) {
        try {
            Transporter trans = SpiManager.holder(Transporter.class).target(transporter);
            // 暴露公共service
            trans.export();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> runnable) {
        return Processor.submit(runnable);
    }

    @Override
    public int getActiveTaskCount() {
        return Processor.getActiveCount();
    }

}
