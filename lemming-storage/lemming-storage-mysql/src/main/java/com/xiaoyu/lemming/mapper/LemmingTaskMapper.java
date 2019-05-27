package com.xiaoyu.lemming.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.core.api.LemmingTask;

public interface LemmingTaskMapper {

    LemmingTask getOneTask(@Param("group") String group, @Param("taskId") String taskId);

    List<LemmingTask> getTasks(@Param("group") String group);

    int insert(LemmingTask task);

    int batchInsert(List<LemmingTask> list);

    List<LemmingTask> getUpdatedTasks(@Param("updateDate") String updateDate);

    int insertLog(LemmingTaskLog taskLog);
}
