package com.xiaoyu.lemming.storage.mysql.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.storage.mysql.query.LemmingTaskQuery;

public interface LemmingTaskMapper {

    LemmingTask getOneTask(@Param("group") String group, @Param("taskId") String taskId);

    List<LemmingTask> getTasks(LemmingTaskQuery query);

    int insert(LemmingTask task);

    int batchInsert(List<LemmingTask> list);

    List<LemmingTask> getUpdatedTasks(@Param("updateDate") String updateDate);

    int insertLog(LemmingTaskLog taskLog);
}
