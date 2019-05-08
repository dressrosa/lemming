package com.xiaoyu.lemming.monitor.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.api.TaskService;
import com.xiaoyu.lemming.monitor.common.entity.ResponseMapper;
import com.xiaoyu.ribbon.core.StringUtil;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "api/v1/task/update", method = RequestMethod.POST)
    public String squareList(HttpServletRequest request, LemmingTask task) {
        if (StringUtil.isBlank(task.getApp()) || StringUtil.isBlank(task.getTaskId())) {
            return ResponseMapper.createMapper().resultJson();
        }
        if (task.getSuspension() == null && task.getUsable() == null && StringUtil.isBlank(task.getRule())) {
            return ResponseMapper.createMapper().resultJson();
        }
        this.taskService.updateTask(task);
        return ResponseMapper.createMapper().resultJson();
    }

}
