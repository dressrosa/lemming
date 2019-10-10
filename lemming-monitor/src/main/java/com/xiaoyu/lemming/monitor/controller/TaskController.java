package com.xiaoyu.lemming.monitor.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.monitor.common.api.TaskService;
import com.xiaoyu.lemming.monitor.common.entity.ResponseCode;
import com.xiaoyu.lemming.monitor.common.entity.ResponseMapper;
import com.xiaoyu.lemming.monitor.common.query.LemmingTaskQuery;
import com.xiaoyu.ribbon.core.StringUtil;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "api/v1/task/update", method = RequestMethod.POST)
    @ResponseBody
    public String update(HttpServletRequest request, LemmingTask task) {
        if (StringUtil.isBlank(task.getApp())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .message("所属应用不能为空")
                    .resultJson();
        }
        if (StringUtil.isBlank(task.getName())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .message("任务名称不能为空")
                    .resultJson();
        }
        if (StringUtil.isBlank(task.getTaskId())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .message("任务ID不能为空")
                    .resultJson();
        }
        if (StringUtil.isBlank(task.getTaskGroup())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .message("所属组织不能为空")
                    .resultJson();
        }
        if (StringUtil.isBlank(task.getRule())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .message("执行规则不能为空")
                    .resultJson();
        }
        if (task.getSuspension() == null || task.getUsable() == null) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .resultJson();
        }
        this.taskService.updateTask(task);
        return ResponseMapper.createMapper().resultJson();
    }

    @RequestMapping(value = "api/v1/task/pause", method = RequestMethod.POST)
    @ResponseBody
    public String pause(HttpServletRequest request, @RequestParam(value = "ids[]") String[] ids) {
        if (ids.length == 0) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode()).resultJson();
        }
        return this.taskService.pauseTasks(Arrays.asList(ids)).resultJson();
    }

    @RequestMapping(value = "api/v1/task/disable", method = RequestMethod.POST)
    @ResponseBody
    public String disable(HttpServletRequest request, @RequestParam(value = "ids[]") String[] ids) {
        if (ids.length == 0) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode()).resultJson();
        }
        return this.taskService.disableTasks(Arrays.asList(ids)).resultJson();
    }

    @RequestMapping(value = "api/v1/task/execute", method = RequestMethod.POST)
    @ResponseBody
    public String execute(HttpServletRequest request, LemmingTaskQuery query) {
        if (StringUtil.isBlank(query.getApp()) || StringUtil.isBlank(query.getTaskId())) {
            return ResponseMapper.createMapper()
                    .code(ResponseCode.ARGS_ERROR.statusCode())
                    .resultJson();
        }
        ResponseMapper resp = this.taskService.execute(query);
        return resp.resultJson();
    }

    @RequestMapping(value = "task/list")
    public String list(Model model, HttpServletRequest request, HttpServletResponse response, LemmingTaskQuery query) {
        int pageNum = query.getPageNum();
        int pageSize = query.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
            query.setPageNum(pageNum);
        }
        if (pageSize > 100) {
            pageSize = 100;
            query.setPageSize(pageSize);
        }
        List<LemmingTask> list = this.taskService.queryList(query);
        model.addAttribute("taskList", list);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("isEndPage", list.size() < pageSize);
        if (query.getName() == null) {
            return "task/taskList";
        }
        return "task/taskList::Task_List";
    }

    @RequestMapping(value = "task/detail")
    public String taskDetail(Model model, HttpServletRequest request, String taskId,
            String app, Integer isEdit) {
        LemmingTaskQuery query = new LemmingTaskQuery();
        query.setTaskId(taskId);
        query.setApp(app);
        LemmingTask task = this.taskService.queryDetail(query);
        model.addAttribute("taskDetail", task);
        if (isEdit == 1) {
            return "task/taskList::Task_Modal_Edit";
        }
        return "task/taskList::Task_Modal_Detail";
    }

    @RequestMapping(value = "taskLog/list")
    public String taskLog(Model model, HttpServletRequest request, HttpServletResponse response,
            LemmingTaskQuery query) {
        int pageNum = query.getPageNum();
        int pageSize = query.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
            query.setPageNum(pageNum);
        }
        if (pageSize > 100) {
            pageSize = 100;
            query.setPageSize(pageSize);
        }
        List<LemmingTaskLog> list = this.taskService.queryLogList(query);
        model.addAttribute("logList", list);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("isEndPage", list.size() < pageSize);
        if (query.getTraceId() == null) {
            return "task/logList";
        }
        return "task/logList::Log_List";
    }

    @RequestMapping(value = "taskLog/detail")
    public String taskLogDetail(Model model, HttpServletRequest request, String taskId,
            String app, String traceId) {
        LemmingTaskQuery query = new LemmingTaskQuery();
        query.setTaskId(taskId);
        query.setTraceId(traceId);
        query.setApp(app);
        List<LemmingTaskLog> logs = this.taskService.queryLogList(query);
        model.addAttribute("logDetails", logs);
        return "task/logList::Log_Modal_Detail";
    }
}
