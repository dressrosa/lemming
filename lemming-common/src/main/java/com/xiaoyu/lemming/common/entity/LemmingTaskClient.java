/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

import java.io.Serializable;

/**
 * @author hongyu
 * @date 2019-09
 * @description
 */
public class LemmingTaskClient implements Serializable {

    private static final long serialVersionUID = -3203697335106058866L;

    private long id;
    private String app;
    private String taskId;
    private String host;

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

    public String getHost() {
        return host;
    }

    public LemmingTaskClient setHost(String host) {
        this.host = host;
        return this;
    }

}
