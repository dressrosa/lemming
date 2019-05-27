package com.xiaoyu.lemming.core.api;

import java.io.Serializable;

/**
 * @author hongyu
 * @param
 * @date 2019-03
 * @description
 */
public class LemmingTask implements Task, Serializable {

    private static final long serialVersionUID = 8607614712966713060L;

    private Integer id;
    private int delFlag = 0;

    // 任务id
    private String taskId;
    // 名称
    private String name;
    // 任务所属组别
    private String group;
    // 任务所属应用
    private String app;
    // 执行参数
    private Object[] params;
    // 执行规则
    private String rule;
    // 协议 rpc or http
    private String transport;
    private String taskImpl;
    // 是否可用
    private Integer usable;

    // client or server
    private String side;
    // 执行次数
    private Integer count;
    // 延迟执行时间
    private Integer delayTime;
    private String protocol;
    // task的spring代理对象
    private Object proxy;
    // 是否正在运行
    private volatile boolean running;
    // 是否同步
    private boolean sync;
    // 是否暂停
    private Integer suspension;

    public String getName() {
        return name;
    }

    public LemmingTask setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getSuspension() {
        return suspension;
    }

    public LemmingTask setSuspension(Integer suspension) {
        this.suspension = suspension;
        return this;
    }

    public LemmingTask setUsable(int usable) {
        this.usable = usable;
        return this;
    }

    public int getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LemmingTask(String taskId) {
        this.taskId = taskId;
    }

    public String getSide() {
        return side;
    }

    public LemmingTask setSide(String side) {
        this.side = side;
        return this;
    }

    public Integer getUsable() {
        return usable;
    }

    public String getTransport() {
        return transport;
    }

    public LemmingTask setTransport(String transport) {
        this.transport = transport;
        return this;
    }

    public boolean isSync() {
        return sync;
    }

    public LemmingTask setSync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public String getTaskImpl() {
        return taskImpl;
    }

    public LemmingTask setTaskImpl(String taskImpl) {
        this.taskImpl = taskImpl;
        return this;
    }

    public Object getProxy() {
        return proxy;
    }

    public LemmingTask setProxy(Object proxy) {
        this.proxy = proxy;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public LemmingTask setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public boolean isRunning() {
        return running;
    }

    public LemmingTask setRunning(boolean running) {
        this.running = running;
        return this;
    }

    public String getApp() {
        return app;
    }

    public LemmingTask setApp(String app) {
        this.app = app;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public LemmingTask setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getRule() {
        return rule;
    }

    public LemmingTask setRule(String rule) {
        this.rule = rule;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public LemmingTask setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public LemmingTask setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    public LemmingTask() {
    }

    public Object[] getParams() {
        return params;
    }

    public LemmingTask setParams(Object[] params) {
        this.params = params;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public LemmingTask setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public int hashCode() {
        String key = this.toPath();
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LemmingTask)) {
            return false;
        }
        LemmingTask p = (LemmingTask) obj;
        if (this.toPath().equals(p.toPath())) {
            return true;
        }
        return false;
    }

    public String toPath() {
        final StringBuilder builder = new StringBuilder();
        builder.append("taskId=").append(this.getTaskId())
                .append("&taskImpl=").append(this.getTaskImpl())
                .append("&app=").append(this.getApp())
                .append("&group=").append(this.getGroup())
                .append("&rule=").append(this.getRule())
                .append("&protocol=").append(this.getProtocol())
                .append("&transport=").append(this.getTransport())
                .append("&usable=").append(this.getUsable() == null ? 0 : this.getUsable())
                .append("&suspension=").append(this.getSuspension() == null ? 0 : this.getSuspension())
                .append("&side=").append(this.getSide());
        return builder.toString();
    }

    public static LemmingTask toEntity(String path) {
        LemmingTask t = new LemmingTask();
        String[] arr = path.split("&");
        for (String str : arr) {
            if (str.startsWith("taskId")) {
                t.setTaskId(str.substring(7));
            } else if (str.startsWith("taskImpl")) {
                t.setTaskImpl(str.substring(9));
            } else if (str.startsWith("app")) {
                t.setApp(str.substring(4));
            } else if (str.startsWith("group")) {
                t.setGroup(str.substring(6));
            } else if (str.startsWith("rule")) {
                t.setRule(str.substring(5));
            } else if (str.startsWith("protocol")) {
                t.setProtocol(str.substring(9));
            } else if (str.startsWith("transport")) {
                t.setTransport(str.substring(10));
            } else if (str.startsWith("usable")) {
                t.setUsable(Integer.valueOf(str.substring(7)));
            } else if (str.startsWith("suspension")) {
                t.setSuspension(Integer.valueOf(str.substring(11)));
            } else if (str.startsWith("side")) {
                t.setSide(str.substring(5));
            }
        }
        return t;
    }
}