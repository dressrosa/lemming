/**
 * 
 */
package com.xiaoyu.lemming.monitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.xiaoyu.lemming.common.constant.CallTypeEnum;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Exchanger;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.api.TaskService;
import com.xiaoyu.lemming.monitor.common.entity.ResponseCode;
import com.xiaoyu.lemming.monitor.common.entity.ResponseMapper;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;
import com.xiaoyu.lemming.monitor.dao.LemmingTaskMapper;
import com.xiaoyu.ribbon.core.StringUtil;

/**
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private LemmingTaskMapper taskDao;

    @Transactional
    @Override
    public int updateTask(LemmingTask task) {
        LemmingTask t = this.taskDao.getTask(task.getApp(), task.getTaskId());
        if (t == null) {
            return 0;
        }
        if (task.getCallType() == CallTypeEnum.Sharding.ordinal()
                && task.getClients() != null && !task.getClients().isEmpty()) {
            Map<String, LemmingTaskClient> cmap = task.getClients().stream()
                    .collect(Collectors.toMap(LemmingTaskClient::getExecutionHost, a -> a));
            LemmingTaskQuery cq = new LemmingTaskQuery();
            cq.setTaskId(t.getTaskId());
            cq.setApp(t.getApp());
            List<LemmingTaskClient> clients = this.taskDao.getTaskClients(cq);
            clients.forEach(a -> {
                LemmingTaskClient c = cmap.get(a.getExecutionHost());
                if (c != null) {
                    a.setParams(c.getParams());
                }
            });
            this.taskDao.batchUpdateTaskClients(clients);
        }
        t.setCallType(task.getCallType());
        if (StringUtil.isNotBlank(task.getRule())) {
            t.setRule(task.getRule().trim());
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
            t.setName(task.getName().trim());
        }
        if (StringUtil.isNotBlank(task.getParams())) {
            t.setParams(task.getParams().trim());
        }
        this.taskDao.update(t);
        return 1;
    }

    @Override
    public ResponseMapper add(LemmingTask param) {
        LemmingTask target = this.taskDao.getTask(param.getApp(), param.getTaskId());
        if (target != null) {
            return ResponseMapper.createMapper().code(ResponseCode.EXIST.statusCode());
        }
        LemmingTask t = new LemmingTask();
        t.setTaskId(param.getTaskId());
        t.setApp(param.getApp().trim());
        t.setTaskImpl(param.getTaskImpl().trim());
        t.setTaskGroup(param.getTaskGroup().trim());
        t.setCallType(param.getCallType());
        t.setRule(StringUtil.isNotBlank(param.getRule()) ? param.getRule().trim() : "");
        t.setUsable(param.getUsable());
        t.setSuspension(param.getSuspension());
        t.setName(param.getName().trim());
        t.setParams(StringUtil.isNotBlank(param.getParams()) ? param.getParams().trim() : "");
        t.setTransport(param.getTransport().trim());
        this.taskDao.insert(t);
        return ResponseMapper.createMapper();
    }

    @Override
    public List<LemmingTask> queryList(LemmingTaskQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<LemmingTask> list = this.taskDao.getTasks(query);
        LemmingTaskQuery cq = new LemmingTaskQuery();
        if (!list.isEmpty()) {
            List<String> apps = new ArrayList<>(list.size());
            List<String> taskIds = new ArrayList<>(list.size());
            list.forEach(a -> {
                apps.add(a.getApp());
                taskIds.add(a.getTaskId());
            });
            cq.setApps(apps);
            cq.setTaskIds(taskIds);
            List<LemmingTaskClient> clients = this.taskDao.getTaskClients(cq);
            Map<String, LemmingTask> taskMap = list.stream()
                    .collect(Collectors.toMap(a -> a.getApp() + "_" + a.getTaskId(), t -> t));
            clients.forEach(a -> {
                LemmingTask t = taskMap.get(a.getApp() + "_" + a.getTaskId());
                if (t != null) {
                    t.getClients().add(a);
                }
            });
        }
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
    public ResponseMapper execute(LemmingTaskQuery query) {
        LemmingTask task = this.taskDao.getTask(query.getApp(), query.getTaskId());
        if (task == null) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode())
                    .message("任务不存在");
        }
        if (StringUtil.isBlank(task.getTaskGroup())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode()).message("请先设置所属组别");
        }
        List<LemmingTaskClient> clients = this.taskDao.getTaskClients(query);
        if (clients.isEmpty()) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode())
                    .message("无可用任务机器");
        }
        task.setClients(clients);
        try {
            Exchanger exchanger = SpiManager.defaultSpiExtender(Exchanger.class);
            exchanger.execute(task);
        } catch (Exception e) {
        }
        return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode())
                .message("操作成功");
    }

    @Override
    public List<LemmingTaskLog> queryLogList(LemmingTaskQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<LemmingTaskLog> list = this.taskDao.getLogs(query);
        return list;
    }

    @Override
    public ResponseMapper pauseTasks(List<String> idList) {
        List<LemmingTask> tasks = this.taskDao.queryTasksByIds(idList);
        if (tasks.isEmpty()) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode());
        }
        for (LemmingTask a : tasks) {
            if (StringUtil.isBlank(a.getTaskGroup())) {
                return ResponseMapper.createMapper()
                        .code(ResponseCode.ARGS_ERROR.statusCode()).message("请先设置所属组别");
            }
            if (a.getSuspension() == 1) {
                a.setSuspension(0);
            } else {
                a.setSuspension(1);
            }
        }
        this.taskDao.batchUpdate(tasks);
        return ResponseMapper.createMapper();
    }

    @Override
    public ResponseMapper disableTasks(List<String> idList) {
        List<LemmingTask> tasks = this.taskDao.queryTasksByIds(idList);
        if (tasks.isEmpty()) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode());
        }
        for (LemmingTask a : tasks) {
            if (StringUtil.isBlank(a.getTaskGroup())) {
                return ResponseMapper.createMapper()
                        .code(ResponseCode.ARGS_ERROR.statusCode()).message("请先设置所属组别");
            }
            if (a.getUsable() == 1) {
                a.setUsable(0);
            } else {
                a.setUsable(1);
            }
        }
        this.taskDao.batchUpdate(tasks);
        return ResponseMapper.createMapper();
    }

    @Override
    public ResponseMapper remove(LemmingTaskQuery query) {
        LemmingTask task = this.taskDao.getTask(query.getApp(), query.getTaskId());
        if (task == null) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode())
                    .message("任务不存在");
        }
        List<LemmingTaskClient> clients = this.taskDao.getTaskClients(query);
        if (!clients.isEmpty()) {
            return ResponseMapper.createMapper().code(ResponseCode.NO_DATA.statusCode())
                    .message("有可用机器,无法删除");
        }
        this.taskDao.delete(task);
        return ResponseMapper.createMapper().message("删除成功");
    }

}
