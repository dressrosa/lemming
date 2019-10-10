package com.xiaoyu.lemming.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;

@Repository
public interface LemmingTaskMapper {

    LemmingTask getTask(@Param("app") String app, @Param("taskId") String taskId);

    List<LemmingTask> getTasks(LemmingTaskQuery query);

    List<LemmingTask> queryTasksByIds(List<String> list);

    int insert(LemmingTask task);

    int batchInsert(List<LemmingTask> list);

    int update(LemmingTask task);

    List<LemmingTaskClient> getTaskClients(LemmingTaskQuery query);

    List<LemmingTaskLog> getLogs(LemmingTaskQuery query);

    int batchUpdate(List<LemmingTask> list);

}
