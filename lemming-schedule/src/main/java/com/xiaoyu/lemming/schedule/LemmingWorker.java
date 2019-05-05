/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.StringUtil;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.core.api.Worker;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class LemmingWorker implements Worker {

    private static final Logger logger = LoggerFactory.getLogger(LemmingWorker.class);

    /**
     * 所有worker共享一个线程池
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

    private ScheduledExecutorService Run_Monitor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentMap<String, LemmingTask> Workbook = new ConcurrentHashMap<>();

    private volatile boolean busy = false;

    private volatile boolean suspension = false;

    /**
     * cron表达式解析器
     */
    private static final CronParser Rule_Parser = new CronParser(
            CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

    public LemmingWorker() {
        // 一直检测任务是否需要执行了
        Run_Monitor.scheduleAtFixedRate(() -> {
            logger.debug("Task_Monitor checking task need run.");
            Iterator<LemmingTask> iter = Workbook.values().iterator();
            ZonedDateTime now = ZonedDateTime.now();
            while (iter.hasNext()) {
                LemmingTask task = iter.next();
                if (checkNeedRun(task, now) && !task.isRunning()) {
                    handle(task);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 解析cron表达式
     * 
     * @param task
     * @param now
     * @return
     */
    private boolean checkNeedRun(LemmingTask task, ZonedDateTime now) {
        if (StringUtil.isBlank(task.getRule())) {
            return false;
        }
        Cron cron = Rule_Parser.parse(task.getRule());
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime nextExecution = executionTime.nextExecution(now).get();
        long nowTime = Timestamp.valueOf(now.toLocalDateTime()).getTime();
        long nextTime = Timestamp.valueOf(nextExecution.toLocalDateTime()).getTime();
        boolean needRun = now.isEqual(nextExecution) || Math.abs(nowTime - nextTime) <= 1000;
        return needRun;
    }

    @Override
    public void handle(LemmingTask task) {
        task.setRunning(true);
        Future<?> future = Processor.submit(() -> {
            try {
                Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
                transporter.call(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        if (task.isSync()) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // TODO 应该有个方法判断是否繁忙
        if (Processor.getQueue().size() / Runtime.getRuntime().availableProcessors() > 4) {
            this.busy = true;
        } else {
            this.busy = false;
        }
        task.setRunning(false);
    }

    @Override
    public void accept(LemmingTask task) {
        if (task == null) {
            return;
        }
        if (busy || suspension) {
            return;
        }
        if (!Workbook.containsKey(task.getTaskId())) {
            Workbook.put(task.getTaskId(), task);
        }
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public boolean isWorking() {
        return Processor.getActiveCount() > 0 ? true : false;
    }

    @Override
    public void suspend(boolean pause) {
        this.suspension = pause;
    }

    @Override
    public void laidOff() {
        Run_Monitor.shutdown();
        Processor.shutdown();
    }
}
