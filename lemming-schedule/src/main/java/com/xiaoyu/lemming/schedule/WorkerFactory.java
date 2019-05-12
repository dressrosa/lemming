/**
 * 
 */
package com.xiaoyu.lemming.schedule;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
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

    private static ScheduledExecutorService Worker_Monitor = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Worker_Monitor");
                }
            });

    public WorkerFactory() {
    }

    public void startMonitor() {
        // 每分钟检测是否有空闲的worker需要去掉
        Worker_Monitor.scheduleAtFixedRate(() -> {
            logger.info("当前App数:" + Workers.size());
            int workerNum = 0;
            Iterator<List<Worker>> iter = Workers.values().iterator();
            while (iter.hasNext()) {
                List<Worker> workers = iter.next();
                Iterator<Worker> witer = workers.iterator();
                while (witer.hasNext()) {
                    Worker w = witer.next();
                    if (!w.isBusy() && !w.isWorking()) {
                        w.suspend(true);
                        if (!w.isWorking()) {
                            w.laidOff();
                            witer.remove();
                            logger.info(" worker[" + w.name() + "] is removed");
                        } else {
                            w.suspend(false);
                        }
                    }
                }
                if (workers.isEmpty()) {
                    iter.remove();
                }
                workerNum += workers.size();
            }
            logger.info("当前worker数:" + workerNum);
        }, 60, 60, TimeUnit.SECONDS);
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
            workers = new LinkedList<>();
            Workers.put(app, workers);
        }
        Iterator<Worker> iter = workers.iterator();
        while (iter.hasNext()) {
            Worker w = iter.next();
            if (!w.isBusy()) {
                worker = w;
                break;
            }
        }
        if (worker == null) {
            worker = new LemmingWorker(app);
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
