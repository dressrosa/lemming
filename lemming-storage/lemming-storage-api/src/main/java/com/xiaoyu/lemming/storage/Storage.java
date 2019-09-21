package com.xiaoyu.lemming.storage;

import java.util.List;

import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Storage {

    void init(String url, String user, String password);

    void insert(LemmingTask task);

    int batchSave(List<LemmingTask> tasks);

    LemmingTask fetch();

    List<LemmingTask> fetchTasks();

    int insertLog(String taskId, String msg, boolean isError);

    List<LemmingTask> fetchAllTasks();

    List<LemmingTask> fetchUpdatedTasks();

    int saveLog(LemmingTask task, ExecuteResult callRet);

    int removeTaskClientsByImpl(String string);
}
