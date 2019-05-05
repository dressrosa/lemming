package com.xiaoyu.lemming.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xiaoyu.lemming.core.api.LemmingTask;

public interface LemmingTaskMapper {

    LemmingTask getOneTask(String taskId);

    List<LemmingTask> getTasks(@Param("app") String app);

    int batchInsert(List<LemmingTask> list);
}
