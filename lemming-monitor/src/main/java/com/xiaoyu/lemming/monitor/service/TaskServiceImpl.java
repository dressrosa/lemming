/**
 * 
 */
package com.xiaoyu.lemming.monitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.api.TaskService;
import com.xiaoyu.lemming.monitor.dao.LemmingTaskMapper;
import com.xiaoyu.ribbon.core.StringUtil;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private LemmingTaskMapper taskDao;

    @Override
    public int updateTask(LemmingTask task) {
        LemmingTask t = this.taskDao.getTask(task.getApp(), task.getTaskId());
        if (t == null) {
            return 0;
        }
        if (StringUtil.isNotBlank(task.getRule())) {
            t.setRule(task.getRule());
        }
        if (task.getUsable() != null) {
            t.setUsable(task.getUsable());
        }
        if (task.getSuspension() != null) {
            t.setSuspension(task.getSuspension());
        }
        if (StringUtil.isNotBlank(task.getName())) {
            t.setName(task.getName());
        }
        this.taskDao.update(t);
        return 1;
    }

}
