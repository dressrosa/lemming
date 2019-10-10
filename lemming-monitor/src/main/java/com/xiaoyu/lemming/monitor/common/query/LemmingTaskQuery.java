package com.xiaoyu.lemming.monitor.common.query;

import java.util.List;

/**
 * @author hongyu
 * @param
 * @date 2019-05
 * @description
 */
public class LemmingTaskQuery {

    private String taskId;
    private String app;
    private String group;
    private List<String> taskIds;
    private List<String> apps;
    private String name;

    private String taskImpl;
    private Integer pageNum;
    private Integer pageSize;

    private String traceId;

    private String startCDate;
    private String endCDate;

    public String getStartCDate() {
        return startCDate;
    }

    public void setStartCDate(String startCDate) {
        this.startCDate = startCDate;
    }

    public String getEndCDate() {
        return endCDate;
    }

    public void setEndCDate(String endCDate) {
        this.endCDate = endCDate;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPageNum() {
        return pageNum == null ? 1 : pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null ? 10 : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getTaskImpl() {
        return taskImpl;
    }

    public void setTaskImpl(String taskImpl) {
        this.taskImpl = taskImpl;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;

    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;

    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;

    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;

    }

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;

    }

}