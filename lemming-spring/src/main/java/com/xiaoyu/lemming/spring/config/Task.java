/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.config;

public class Task {
    // 任务id
    private String taskId;
    // 任务所属应用
    private String app;
    // 执行规则
    private String rule;
    // 协议 rpc or http
    private String protocol;

    private String taskImpl;

    public String getTaskId() {
        return taskId;
    }

    public Task setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getApp() {
        return app;
    }

    public Task setApp(String app) {
        this.app = app;
        return this;
    }

    public String getRule() {
        return rule;
    }

    public Task setRule(String rule) {
        this.rule = rule;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public Task setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getTaskImpl() {
        return taskImpl;
    }

    public Task setTaskImpl(String taskImpl) {
        this.taskImpl = taskImpl;
        return this;
    }

}
