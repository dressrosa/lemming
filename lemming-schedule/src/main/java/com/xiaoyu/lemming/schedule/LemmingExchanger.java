/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Exchanger;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.core.api.Worker;
import com.xiaoyu.lemming.storage.Storage;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class LemmingExchanger implements Exchanger {

    private static final Logger logger = LoggerFactory.getLogger(LemmingExchanger.class);

    private static final WorkerFactory Worker_Factory = WorkerFactory.getFactory();

    /**
     * 监测新的任务
     */
    private static final ScheduledExecutorService Storage_Monitor = Executors.newSingleThreadScheduledExecutor();

    private void startInspect() {
        // 每60s检测是否新的task
        Storage_Monitor.scheduleAtFixedRate(() -> {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(" Start fetch task.");
                }
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                List<LemmingTask> tasks = storage.fetchUpdatedTasks();
                allocate(tasks);
                // 例检workers
                Worker_Factory.regularChecking();
            } catch (Exception e) {
                logger.error("", e);
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    /**
     * 拉取所有数据
     */
    private void initAllTasks() {
        try {
            Storage storage = SpiManager.defaultSpiExtender(Storage.class);
            // 内部实现需分页取
            List<LemmingTask> tasks = storage.fetchAllTasks();
            allocate(tasks);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    // 存放server端要执行的任务 同一个任务可能存在多份
    @Override
    public void allocate(List<LemmingTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        int taskNum = 0;
        for (LemmingTask task : tasks) {
            if (StringUtil.isBlank(task.getTaskGroup())) {
                continue;
            }
            Worker worker = Worker_Factory.arrage(task.getTaskGroup());
            if (worker == null) {
                continue;
            }
            if (worker.accept(task)) {
                taskNum++;
            }
        }
        if (taskNum > 0) {
            logger.info(" The updated tasks num->" + taskNum);
        }
    }

    @Override
    public void start() {
        this.initAllTasks();
        this.startInspect();
    }

    @Override
    public void close() {
        Storage_Monitor.shutdown();
        Worker_Factory.shutdown();
    }

    @Override
    public void execute(LemmingTask task) {
        if (task == null) {
            return;
        }
        if (StringUtil.isBlank(task.getTaskGroup())) {
            return;
        }
        Worker worker = Worker_Factory.arrage(task.getTaskGroup());
        if (worker == null) {
            return;
        }
        try {
            worker.handle(task);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
