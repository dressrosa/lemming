package com.xiaoyu.lemming.storage.mysql.query;

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

    private String taskImpl;
    private Long startNum;
    private Long pageSize;

    public Long getStartNum() {
        return startNum;
    }

    public void setStartNum(Long startNum) {
        this.startNum = startNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
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