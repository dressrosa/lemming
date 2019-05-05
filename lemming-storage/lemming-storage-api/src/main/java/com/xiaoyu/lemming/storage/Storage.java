package com.xiaoyu.lemming.storage;

import java.util.List;

import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Storage {

    void insert(LemmingTask task);

    int batchSave(List<LemmingTask> tasks);

    LemmingTask fetch();

    List<LemmingTask> fetchTasks();
}
