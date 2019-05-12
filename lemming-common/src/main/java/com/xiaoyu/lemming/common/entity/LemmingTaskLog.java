/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
public class LemmingTaskLog {

    private String group;
    private String app;
    private String taskId;
    private Integer state;
    private String message;

    public String getGroup() {
        return group;
    }

    public LemmingTaskLog setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getApp() {
        return app;
    }

    public LemmingTaskLog setApp(String app) {
        this.app = app;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public LemmingTaskLog setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public Integer getState() {
        return state;
    }

    public LemmingTaskLog setState(Integer state) {
        this.state = state;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LemmingTaskLog setMessage(String message) {
        this.message = message;
        return this;
    }

}
