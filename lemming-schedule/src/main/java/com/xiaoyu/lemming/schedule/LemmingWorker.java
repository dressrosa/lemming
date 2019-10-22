/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
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
import com.xiaoyu.lemming.common.constant.CallTypeEnum;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.CycleList;
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
     * 存放解析过的cron
     */
    private static final WeakHashMap<String, Cron> Cron_Map = new WeakHashMap<>();

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
    private final CycleList<LemmingTask> Work_Book = new CycleList<>();

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
                Iterator<LemmingTask> iter = Work_Book.iterator();
                ZonedDateTime now = ZonedDateTime.now();
                LemmingTask t = null;
                while (iter.hasNext()) {
                    if ((t = iter.next()).getDelFlag() == 1) {
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
        if (task.getClients().isEmpty()) {
            return false;
        }
        if (StringUtil.isBlank(task.getRule())) {
            return false;
        }
        if (task.getUsable() == 0 || task.isRunning() || task.getSuspension() == 1) {
            return false;
        }
        final WeakHashMap<String, Cron> cmap = Cron_Map;
        Cron cron = cmap.get(task.getRule());
        if (cron == null) {
            cron = Rule_Parser.parse(task.getRule());
            cmap.put(task.getRule(), cron);
        }
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
    public void handle(LemmingTask task) throws Exception {
        task.setRunning(true);
        Context context = SpiManager.defaultSpiExtender(Context.class);
        context.submit(() -> {
            try {
                if (task.getCallType() == CallTypeEnum.Simple.ordinal()) {
                    this.doHandleSimple(task);
                } else if (task.getCallType() == CallTypeEnum.Broadcast.ordinal()) {
                    this.doHandleBroadcast(task);
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                task.setRunning(false);
            }
            return 1;
        });
        // TODO 应该有个方法判断是否繁忙
        // if (context.getProcessor().getQueue().size() /
        // Runtime.getRuntime().availableProcessors() > 4) {
        // this.busy = true;
        // } else {
        // this.busy = false;
        // }
    }

    private void doHandleSimple(LemmingTask task) throws Exception {
        Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
        String traceId = StringUtil.getUUID();
        task.setTraceId(traceId);
        try {
            ExecuteResult callRet = transporter.call(task, null);
            // 记录调用
            Storage storage = SpiManager.defaultSpiExtender(Storage.class);
            storage.saveLog(task, callRet);
        } catch (Exception e) {
            logger.error(" Call task[" + task.getTaskId() + "] failed:", e);
            try {
                // 记录调用失败次数
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                storage.saveLog(task, new ExecuteResult().setTraceId(traceId)
                        .setMessage(e.getMessage()));
            } catch (Exception e2) {
                logger.error("", e2);
            }
        }
    }

    private void doHandleBroadcast(LemmingTask task) throws Exception {
        Transporter transporter = SpiManager.defaultSpiExtender(Transporter.class);
        List<LemmingTaskClient> clients = task.getClients();
        // broadcast
        String traceId = "";
        for (LemmingTaskClient c : clients) {
            try {
                traceId = StringUtil.getUUID();
                task.setTraceId(traceId);
                ExecuteResult callRet = transporter.call(task, c);
                // 记录调用
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                storage.saveLog(task, callRet);
            } catch (Exception e) {
                logger.error(" Call task[" + task.getTaskId() + "] failed:", e);
                try {
                    // 记录调用失败次数
                    Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                    storage.saveLog(task, new ExecuteResult().setTraceId(traceId)
                            .setMessage(e.getMessage()));
                } catch (Exception e2) {
                    logger.error("", e2);
                }
            }
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
        Iterator<LemmingTask> iter = Work_Book.iterator();
        boolean isNew = true;
        while (iter.hasNext()) {
            LemmingTask t = iter.next();
            if (t.getTaskId().equals(task.getTaskId())) {
                isNew = false;
                if (task.getUsable() == 0 || task.getDelFlag() == 1) {
                    t.setUsable(task.getUsable());
                    t.setDelFlag(task.getDelFlag());
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
                if (t.getCallType() != task.getCallType()) {
                    t.setCallType(task.getCallType());
                    changed = true;
                }
                if (!changed) {
                    return false;
                }
            }
        }
        if (isNew) {
            if (task.getDelFlag() == 0) {
                return Work_Book.add(task);
            }
        }
        return false;
    }

    @Override
    public boolean accept(List<LemmingTask> tasks) {
        int size = tasks.size();
        if (size == 0) {
            return false;
        }
        if (busy || suspension) {
            return false;
        }
        CycleList<LemmingTask> wbs = Work_Book;
        for (LemmingTask t : tasks) {
            if (t.getUsable() == 0 || t.getDelFlag() == 1) {
                wbs.remove(t);
                continue;
            }
            if (wbs.contains(t)) {
                // 根据equals删除
                wbs.remove(t);
                wbs.add(t);
            } else {
                wbs.add(t);
            }
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return this.busy;
    }

    @Override
    public boolean isWorking() {
        if (Work_Book.isEmpty()) {
            return false;
        }
        Iterator<LemmingTask> iter = Work_Book.iterator();
        while (iter.hasNext()) {
            LemmingTask task = iter.next();
            if (task.getDelFlag() == 1) {
                continue;
            }
            if (task.isRunning() || task.getUsable() == 1) {
                return true;
            }
        }
        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            return context.getActiveTaskCount() > 0 ? true : false;
        } catch (Exception e1) {
            logger.error("", e1);
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

    @Override
    public LemmingTask getTask(LemmingTask query) {
        return Work_Book.get(query);
    }
}
