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

    private String app;
    private String taskId;
    private String host;
    private Integer state;
    private String message;
    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public LemmingTaskLog setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getHost() {
        return host;
    }

    public LemmingTaskLog setHost(String host) {
        this.host = host;
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
