/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

import java.io.Serializable;

/**
 * 执行结果
 * 
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
public class ExecuteResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean isSuccess;
    private String message = "";
    private String dispatchHost;
    private String executionHost;

    private String taskId;
    private String app;
    private String group;
    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public ExecuteResult setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ExecuteResult setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getApp() {
        return app;
    }

    public ExecuteResult setApp(String app) {
        this.app = app;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public ExecuteResult setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getDispatchHost() {
        return dispatchHost;
    }

    public ExecuteResult setDispatchHost(String dispatchHost) {
        this.dispatchHost = dispatchHost;
        return this;
    }

    public String getExecutionHost() {
        return executionHost;
    }

    public ExecuteResult setExecutionHost(String executionHost) {
        this.executionHost = executionHost;
        return this;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public ExecuteResult setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ExecuteResult setMessage(String message) {
        this.message = message;
        return this;
    }

}
