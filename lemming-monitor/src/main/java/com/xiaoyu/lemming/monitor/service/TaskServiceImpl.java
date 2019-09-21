/**
 * 
 */
package com.xiaoyu.lemming.monitor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Exchanger;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.api.TaskService;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;
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
        if (StringUtil.isNotBlank(task.getTaskGroup())) {
            t.setTaskGroup(task.getTaskGroup());
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

    @Override
    public List<LemmingTask> queryList(LemmingTaskQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<LemmingTask> list = this.taskDao.getTasks(query);
        return list;
    }

    @Override
    public LemmingTask queryDetail(LemmingTaskQuery query) {
        List<LemmingTask> list = this.taskDao.getTasks(query);
        if (list.isEmpty()) {
            return null;
        }
        LemmingTask task = list.get(0);
        List<LemmingTaskClient> clients = this.taskDao.getTaskClients(query);
        task.setClients(clients);
        return task;
    }

    @Override
    public int execute(LemmingTaskQuery query) {
        LemmingTask task = this.taskDao.getTask(query.getApp(), query.getTaskId());
        if (task == null) {
            return 0;
        }
        List<LemmingTaskClient> clients = this.taskDao.getTaskClients(query);
        task.setClients(clients);
        try {
            Exchanger exchanger = SpiManager.defaultSpiExtender(Exchanger.class);
            exchanger.execute(task);
        } catch (Exception e) {
        }
        return 1;
    }

}
