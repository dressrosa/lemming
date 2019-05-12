/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final WorkerFactory workerFactory = new WorkerFactory();

    // app->taskList 存放server端要执行的任务 同一个任务可能存在多份
    private static final Map<String, Queue<LemmingTask>> Appending_Task_Map = new HashMap<>();

    // taskId_group->task 存放client启动时标注的任务
    // private static final Map<String, LemmingTask> All_Tasks = new HashMap<>();

    /**
     * 监测新的任务
     */
    private static ScheduledExecutorService Storage_Monitor = Executors.newSingleThreadScheduledExecutor();

    private void startInspect() {
        // 每5s检测是否新的task到来
        Storage_Monitor.scheduleAtFixedRate(() -> {
            try {
                Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                List<LemmingTask> tasks = storage.fetchUpdatedTasks();
                allocate(tasks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 拉取所有数据
     */
    private void init() {
        try {
            Storage storage = SpiManager.defaultSpiExtender(Storage.class);
            // 应该分页取
            List<LemmingTask> tasks = storage.fetchAllTasks();
            allocate(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void allocate(List<LemmingTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        for (LemmingTask task : tasks) {
            Queue<LemmingTask> tqueue = Appending_Task_Map.get(task.getApp());
            if (tqueue == null) {
                Appending_Task_Map.put(task.getApp(), new LinkedBlockingQueue<>());
            }
            Appending_Task_Map.get(task.getApp()).add(task);
        }
        int taskNum = 0;
        Iterator<Entry<String, Queue<LemmingTask>>> iter = Appending_Task_Map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Queue<LemmingTask>> en = iter.next();
            Worker worker = workerFactory.arrage(en.getKey());
            Iterator<LemmingTask> viter = en.getValue().iterator();
            while (viter.hasNext()) {
                LemmingTask t = viter.next();
                if (worker.accept(t)) {
                    taskNum++;
                }
                viter.remove();
            }
        }
        if (taskNum > 0) {
            logger.info("目前更新任务数:" + taskNum);
        }
    }

    @Override
    public void accept(LemmingTask task) {
        try {
            Storage storage = SpiManager.defaultSpiExtender(Storage.class);
            storage.insert(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        this.init();
        this.startInspect();
        workerFactory.startMonitor();
    }

    @Override
    public void close() {
        Storage_Monitor.shutdown();
        workerFactory.shutdown();
    }
}
