/**
 * 
 */
package com.xiaoyu.lemming.monitor.common.api;

import java.util.List;

import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.entity.ResponseMapper;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;

/**
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
public interface TaskService {

    int updateTask(LemmingTask task);

    List<LemmingTask> queryList(LemmingTaskQuery query);

    LemmingTask queryDetail(LemmingTaskQuery query);

    ResponseMapper execute(LemmingTaskQuery query);

    List<LemmingTaskLog> queryLogList(LemmingTaskQuery query);

    ResponseMapper pauseTasks(List<String> idList);

    ResponseMapper disableTasks(List<String> idList);

    ResponseMapper remove(LemmingTaskQuery query);

}
