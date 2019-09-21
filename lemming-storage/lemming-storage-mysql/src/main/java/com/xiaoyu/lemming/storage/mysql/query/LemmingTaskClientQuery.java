package com.xiaoyu.lemming.storage.mysql.query;

import java.util.List;

/**
 * @author hongyu
 * @param
 * @date 2019-09
 * @description
 */
public class LemmingTaskClientQuery {

    private String taskId;
    private String app;
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