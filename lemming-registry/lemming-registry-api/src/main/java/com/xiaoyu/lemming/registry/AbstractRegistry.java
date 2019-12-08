/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.registry;

import java.util.HashMap;
import java.util.Map;

import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author xiaoyu
 * @date 2019-03
 * @description
 */
public abstract class AbstractRegistry implements Registry {

    private static final Map<String, LemmingTask> Task_Map = new HashMap<>();

    @Override
    public LemmingTask getLocalTask(String app, String taskId) {
        final Map<String, LemmingTask> taskMap = Task_Map;
        LemmingTask task = taskMap.get(app + "_" + taskId);
        return task;
    }

    @Override
    public void storeLocalTask(LemmingTask task) {
        if (!Task_Map.containsKey(task.getApp() + "_" + task.getTaskId())) {
            Task_Map.put(task.getApp() + "_" + task.getTaskId(), task);
        }
    }

}
