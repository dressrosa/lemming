package com.xiaoyu.lemming.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.xiaoyu.lemming.core.api.LemmingTask;

@Repository
public interface LemmingTaskMapper {

    LemmingTask getTask(@Param("app") String app, @Param("taskId") String taskId);

    int insert(LemmingTask task);

    int batchInsert(List<LemmingTask> list);

    int update(LemmingTask task);

}
