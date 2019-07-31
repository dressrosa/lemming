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