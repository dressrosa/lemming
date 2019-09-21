/**
 * 
 */
package com.xiaoyu.lemming.monitor.common.api;

import java.util.List;

import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
public interface TaskService {

    public int updateTask(LemmingTask task);

    public List<LemmingTask> queryList(LemmingTaskQuery query);

    public LemmingTask queryDetail(LemmingTaskQuery query);

    public int execute(LemmingTaskQuery query);
}
