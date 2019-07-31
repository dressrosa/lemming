/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.xiaoyu.lemming.core.api.Context;
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

    /**
     * 轮询是否执行
     */
    private AtomicBoolean Poll_Flag = new AtomicBoolean(false);

    public LemmingWorker(String name) {
        this.name = name;
        boolean isDebugEnabled = logger.isDebugEnabled();
        // 一直检测任务是否需要执行了
        Run_Monitor.scheduleAtFixedRate(() -> {
            // 暂停
            if (this.suspension) {
                return;
            }
            AtomicBoolean flag = Poll_Flag;
            if (!flag.compareAndSet(false, true)) {
                return;
            }
            int taskNum = 0;
            try {
                if (isDebugEnabled) {
                    logger.debug(" Task_Monitor begin checking task need run.");
                }
                Iterator<Entry<String, LemmingTask>> iter = Workbook.entrySet().iterator();
                ZonedDateTime now = ZonedDateTime.now();
                Entry<String, LemmingTask> en = null;
                LemmingTask t = null;
                while (iter.hasNext()) {
                    en = iter.next();
                    if ((t = en.getValue()).getUsable() == 0) {
                        // 不可并发
                        iter.remove();
                    }
                    taskNum++;
                    if (this.checkShouldRunNow(t, now)) {
                        this.handle(t);
                    }
                }
                if (isDebugEnabled) {
                    logger.debug(" worker[" + this.name + "]包含task数:" + taskNum);
                }
            } catch (Exception e) {
                // do nothing
            }
            /// 重置状态
            flag.set(false);
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 解析cron表达式
     * 
     * @param task
     * @param now
     * @return
     */
    private boolean checkShouldRunNow(LemmingTask task, ZonedDateTime now) {
        if (task.isRunning() || task.getSuspension() == 1) {
            return false;
        }
        if (StringUtil.isBlank(task.getRule())) {
            return false;
        }
        Cron cron = Rule_Parser.parse(task.getRule());
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        boolean shouldRun = executionTime.isMatch(now);
        if (shouldRun) {
            return true;
        }
        ZonedDateTime nextExecution = executionTime.nextExecution(now).get();
        // 检查是否超过了下次执行时机500ms,允许一定的误差
        shouldRun = now.isAfter(nextExecution) && now.minus(500, ChronoUnit.MILLIS).isBefore(nextExecution);
        return shouldRun;
    }

    @Override
    public void handle(LemmingTask task) {
        task.setRunning(true);
        try {
            String traceId = UUID.randomUUID().toString();
            Context context = SpiManager.defaultSpiExtender(Context.class);
            context.getProcessor().submit(() -> {
                try {
                    Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
                    task.setTraceId(traceId);
                    ExecuteResult callRet = transporter.call(task);
                    // 记录调用
                    Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                    storage.saveLog(task, callRet);
                } catch (Exception e) {
                    logger.error(" Call task[" + task.getTaskId() + "] failed:" + e);
                    try {
                        // 记录调用失败次数
                        Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                        storage.saveLog(task, new ExecuteResult().setTraceId(traceId)
                                .setMessage(e.getMessage()));
                    } catch (Exception e2) {
                        logger.error("" + e2);
                    }
                }
                task.setRunning(false);
            });
            // TODO 应该有个方法判断是否繁忙
            if (context.getProcessor().getQueue().size() / Runtime.getRuntime().availableProcessors() > 4) {
                this.busy = true;
            } else {
                this.busy = false;
            }
        } catch (Exception e1) {
            logger.error("" + e1);
        }

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
        return this.busy;
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
        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            return context.getProcessor().getTaskCount() > 0 ? true : false;
        } catch (Exception e1) {
            logger.error("" + e1);
        }
        return false;

    }

    @Override
    public void suspend(boolean pause) {
        this.suspension = pause;
    }

    @Override
    public void laidOff() {
        this.laidOff = true;
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
