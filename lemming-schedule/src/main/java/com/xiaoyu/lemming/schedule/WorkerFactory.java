/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.core.api.Worker;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class WorkerFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);

    private static final ConcurrentMap<String, List<Worker>> Workers = new ConcurrentHashMap<>();

    private static ScheduledExecutorService Worker_Monitor = Executors.newSingleThreadScheduledExecutor();

    public WorkerFactory() {
    }

    public void startMonitor() {
        // 每分钟检测是否有空闲的worker需要去掉
        Worker_Monitor.scheduleAtFixedRate(() -> {
            logger.info("当前worker数:" + Workers.size());
            Iterator<List<Worker>> iter = Workers.values().iterator();
            while (iter.hasNext()) {
                Iterator<Worker> workers = iter.next().iterator();
                workers.forEachRemaining(w -> {
                    if (!w.isBusy() && !w.isWorking()) {
                        w.suspend(true);
                        if (!w.isWorking()) {
                            w.laidOff();
                            workers.remove();
                            w = null;
                        } else {
                            w.suspend(false);
                        }
                    }
                });
            }

        }, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 按应用分配worker
     * 
     * @param app
     * @return
     */
    public Worker arrage(String app) {
        List<Worker> workers = Workers.get(app);
        Worker worker = null;
        if (workers == null) {
            workers = new ArrayList<>();
            Workers.put(app, workers);
        }
        for (Worker w : workers) {
            if (!w.isBusy()) {
                worker = w;
                break;
            }
        }
        if (worker == null) {
            worker = new LemmingWorker();
            workers.add(worker);
        }
        return worker;
    }

    public void shutdown() {
        logger.info(" Begin WorkerFactory shutdown");
        Worker_Monitor.shutdown();
        Iterator<List<Worker>> iter = Workers.values().iterator();
        while (iter.hasNext()) {
            Iterator<Worker> workers = iter.next().iterator();
            workers.forEachRemaining(w -> {
                w.laidOff();
            });
        }
        logger.info(" Complete WorkerFactory shutdown");
    }
}
