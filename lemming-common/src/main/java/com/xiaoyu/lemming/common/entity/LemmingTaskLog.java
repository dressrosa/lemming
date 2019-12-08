/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

/**
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
public class LemmingTaskLog {

    private String app;
    private String taskId;
    private String executionHost;
    private String dispatchHost;
    private Integer state;
    private String message;
    private String traceId;
    private String createDate;

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getTraceId() {
        return traceId;
    }

    public LemmingTaskLog setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getExecutionHost() {
        return executionHost;
    }

    public LemmingTaskLog setExecutionHost(String executionHost) {
        this.executionHost = executionHost;
        return this;
    }

    public String getDispatchHost() {
        return dispatchHost;
    }

    public LemmingTaskLog setDispatchHost(String dispatchHost) {
        this.dispatchHost = dispatchHost;
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
