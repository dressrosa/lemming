package com.xiaoyu.lemming.client;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class ClientContext implements Context {

    /**
     * 用于异步执行任务
     */
    private static ThreadPoolExecutor Processor = new ThreadPoolExecutor(
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

    private Registry registry = null;

    public ClientContext() {
    }

    public ThreadPoolExecutor getProcessor() {
        return Processor;
    }

    @Override
    public String side() {
        return "client";
    }

    @Override
    public void start() {

    }

    @Override
    public LemmingTask getLocalTask(String app, String taskId) {
        try {
            registry = SpiManager.defaultSpiExtender(Registry.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final LemmingTask task = registry.getLocalTask(app, taskId);
        return task;
    }

    @Override
    public void close() {
        if (registry != null) {
            registry.close();
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

}
