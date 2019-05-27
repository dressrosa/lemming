/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
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
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.StringUtil;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.core.api.Worker;
import com.xiaoyu.lemming.storage.Storage;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class LemmingWorker implements Worker {

    private static final Logger logger = LoggerFactory.getLogger(LemmingWorker.class);

    /**
     * cron表达式解析器
     */
    private static final CronParser Rule_Parser = new CronParser(
            CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
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
    /**
     * 用于检测任务是否需要运行
     */
    private ScheduledExecutorService Run_Monitor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Run_Monitor");
        }
    });

    /**
     * 用于存放任务
     */
    private final ConcurrentMap<String, LemmingTask> Workbook = new ConcurrentHashMap<>();

    /**
     * 是否繁忙
     */
    private volatile boolean busy = false;

    /**
     * 是否暂停
     */
    private volatile boolean suspension = false;

    /**
     * 是否下岗停用
     */
    private volatile boolean laidOff = false;

    /**
     * worker name,用作标识
     */
    private String name;

    public LemmingWorker(String name) {
        this.name = name;
        // 一直检测任务是否需要执行了
        Run_Monitor.scheduleAtFixedRate(() -> {
            // 暂停
            if (this.suspension) {
                return;
            }
            int taskNum = 0;
            if (logger.isDebugEnabled()) {
                logger.debug(" Task_Monitor begin checking task need run.");
            }
            Iterator<LemmingTask> iter = Workbook.values().iterator();
            ZonedDateTime now = ZonedDateTime.now();
            while (iter.hasNext()) {
                LemmingTask task = iter.next();
                if (task.getUsable() == 0) {
                    iter.remove();
                }
                taskNum++;
                if (checkNeedRun(task, now)) {
                    handle(task);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(" worker[" + this.name + "]包含task数:" + taskNum);
            }

        }, 1, 1, TimeUnit.SECONDS);
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
        if (task.isRunning() || task.getSuspension() == 1) {
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
        Processor.submit(() -> {
            try {
                Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
                ExecuteResult callRet = transporter.call(task);
                // 记录调用
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                if (callRet.isSuccess()) {
                    storage.saveLog(task, callRet);
                } else {
                    storage.saveLog(task, callRet);
                }
            } catch (Exception e) {
                logger.error(" Call task[" + task.getTaskId() + "] failed:" + e);
                try {
                    // 记录调用失败次数
                    Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                    storage.saveLog(task, new ExecuteResult());
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
        // TODO 应该有个方法判断是否繁忙
        if (Processor.getQueue().size() / Runtime.getRuntime().availableProcessors() > 4) {
            this.busy = true;
        } else {
            this.busy = false;
        }
        task.setRunning(false);
    }

    @Override
    public boolean accept(LemmingTask task) {
        if (task == null) {
            return false;
        }
        if (busy || suspension) {
            return false;
        }
        LemmingTask t = Workbook.get(task.getTaskId());
        if (t == null) {
            if (task.getDelFlag() == 0) {
                Workbook.put(task.getTaskId(), task);
            }
        } else {
            if (task.getUsable() == 0 || task.getDelFlag() == 1) {
                Workbook.remove(task.getTaskId());
                return true;
            }
            boolean changed = false;
            if (!t.getRule().equals(task.getRule())) {
                t.setRule(task.getRule());
                changed = true;
            }
            if (t.getSuspension() != task.getSuspension()) {
                t.setSuspension(task.getSuspension());
                changed = true;
            }
            if (!changed) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public boolean isWorking() {
        if (Workbook.isEmpty()) {
            return false;
        }
        Iterator<LemmingTask> iter = Workbook.values().iterator();
        while (iter.hasNext()) {
            LemmingTask task = iter.next();
            if (task.getUsable() == 0) {
                iter.remove();
            }
            if (task.isRunning() || task.getUsable() == 1 || task.getDelFlag() == 0) {
                return true;
            }
        }
        return Processor.getTaskCount() > 0 ? true : false;
    }

    @Override
    public void suspend(boolean pause) {
        this.suspension = pause;
    }

    @Override
    public void laidOff() {
        laidOff = true;
        logger.info(" Begin worker[" + this.name + "] Run_Monitor shutdown.");
        Run_Monitor.shutdown();
        logger.info(" Completed worker[" + this.name + "] Run_Monitor shutdown.");
    }

    @Override
    public boolean isLaidOff() {
        return this.laidOff;
    }

    @Override
    public String name() {
        return this.name;
    }
}
