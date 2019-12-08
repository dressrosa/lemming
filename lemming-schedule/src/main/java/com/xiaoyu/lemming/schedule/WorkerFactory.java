/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.schedule;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.lemming.core.api.Worker;

/**
 * worker工厂
 * 
 * @author xiaoyu
 * @date 2019-03
 * @description
 */
public class WorkerFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);

    private static final ConcurrentMap<String, List<Worker>> Workers = new ConcurrentHashMap<>();

    private WorkerFactory() {
    }

    private static class InnerWorkerFactory {
        public static final WorkerFactory instance = new WorkerFactory();
    }

    public static WorkerFactory getFactory() {
        return InnerWorkerFactory.instance;
    }

    /**
     * 检测是否有空闲的worker需要去掉
     */
    public void regularChecking() {
        if (logger.isDebugEnabled()) {
            logger.info(" At present the group count:" + Workers.size());
        }
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
                        logger.info(" Worker[" + w.name() + "] is removed");
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
        if (logger.isDebugEnabled()) {
            logger.info(" At present the worker count:" + workerNum);
        }
    }

    /**
     * 按组分配worker
     * 
     * @param group
     * @return
     */
    public Worker arrage(String group) {
        if (StringUtil.isBlank(group)) {
            return null;
        }
        List<Worker> workers = Workers.get(group);
        Worker worker = null;
        if (workers == null) {
            workers = new LinkedList<>();
            Workers.put(group, workers);
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
            worker = new LemmingWorker(group);
            workers.add(worker);
        }
        return worker;
    }

    public void shutdown() {
        logger.info(" Begin WorkerFactory shutdown");
        Iterator<List<Worker>> iter = Workers.values().iterator();
        while (iter.hasNext()) {
            Iterator<Worker> workers = iter.next().iterator();
            workers.forEachRemaining(w -> {
                try {
                    w.laidOff();
                } catch (Exception ignore) {
                }
            });
        }
        logger.info(" Complete WorkerFactory shutdown");
    }
}
