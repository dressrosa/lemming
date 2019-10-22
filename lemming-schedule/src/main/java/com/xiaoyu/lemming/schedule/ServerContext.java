package com.xiaoyu.lemming.schedule;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.Exchanger;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class ServerContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(ServerContext.class);

    private Registry registry;

    private Exchanger exchanger;

    /**
     * 所有worker共享一个执行线程池
     */
    private static ThreadPoolExecutor Processor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() << 1,
            Runtime.getRuntime().availableProcessors() << 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                private final AtomicInteger adder = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Worker_Processor_" + adder.incrementAndGet());
                }
            });

    @Override
    public String side() {
        return CommonConstant.Server;
    }

    @Override
    public void start() {
        try {
            exchanger = SpiManager.defaultSpiExtender(Exchanger.class);
            exchanger.start();
        } catch (Exception e) {
            logger.error(e + "");
        }
    }

    @Override
    public LemmingTask getLocalTask(String app, String taskId) {
        try {
            registry = SpiManager.defaultSpiExtender(Registry.class);
            final LemmingTask task = registry.getLocalTask(app, taskId);
            return task;
        } catch (Exception e) {
            logger.error(e + "");
        }
        return null;
    }

    @Override
    public void close() {
        logger.info(" Begin ServerContext close");
        if (exchanger != null) {
            exchanger.close();
        }
        if (registry != null) {
            registry.close();
        }
        logger.info(" Complete ServerContext close");
    }

    @Override
    public void initTransporter(String transporter) {
        try {
            Transporter trans = SpiManager.holder(Transporter.class).target(transporter);
            // 暴露公共service
            trans.export();
        } catch (Exception e) {
            logger.error("", e);
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
