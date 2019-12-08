/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

import java.io.Serializable;

/**
 * @author xiaoyu
 * @date 2019-09
 * @description
 */
public class LemmingTaskClient implements Serializable {

    private static final long serialVersionUID = -3203697335106058866L;

    private long id;
    private String app;
    private String taskId;
    private String executionHost;

    private String params;
    private Integer delFlag;

    public String getParams() {
        return params;
    }

    public LemmingTaskClient setParams(String params) {
        this.params = params;
        return this;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public LemmingTaskClient setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
        return this;
    }

    public long getId() {
        return id;
    }

    public LemmingTaskClient setId(long id) {
        this.id = id;
        return this;
    }

    public String getApp() {
        return app;
    }

    public LemmingTaskClient setApp(String app) {
        this.app = app;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public LemmingTaskClient setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getExecutionHost() {
        return executionHost;
    }

    public LemmingTaskClient setExecutionHost(String executionHost) {
        this.executionHost = executionHost;
        return this;
    }

}
