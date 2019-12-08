package com.xiaoyu.lemming.core.api;

import java.util.LinkedList;
import java.util.List;

import com.xiaoyu.lemming.common.entity.LemmingTaskClient;

/**
 * @author xiaoyu
 * @param
 * @date 2019-03
 * @description
 */
public class LemmingTask implements Task {

    private static final long serialVersionUID = 8607614712966713060L;

    // 链路id
    private String traceId;
    private Integer id;
    private int delFlag = 0;

    // 任务id
    private String taskId;
    // 任务实现类
    private String taskImpl;
    // 任务类型 1 定时任务 2临时任务(只执行一次) TODO
    // private Integer taskType;
    // 调用类型 0 简单调用 1 广播调用
    private Integer callType;
    // 名称
    private String name;
    // 任务所属组别
    private String taskGroup;
    // 任务所属应用
    private String app;
    // 执行参数
    private String params;
    // 执行规则
    private String rule;
    // 协议 rpc or http
    private String transport;
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

    // 执行机器ip
    private String executionHost;

    // 调度机器ip
    private String dispatchHost;

    private List<LemmingTaskClient> clients = new LinkedList<>();

    @Override
    public String toString() {
        return "group:" + taskGroup + ";app:" + app + ";taskId:" + taskId;
    }

    public Integer getCallType() {
        return callType;
    }

    public LemmingTask setCallType(Integer callType) {
        this.callType = callType;
        return this;
    }

    public List<LemmingTaskClient> getClients() {
        return clients;
    }

    public LemmingTask setClients(List<LemmingTaskClient> clients) {
        this.clients = clients;
        return this;
    }

    public LemmingTask(String taskId) {
        this.taskId = taskId;
    }

    public String getTraceId() {
        return traceId;
    }

    public LemmingTask setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getExecutionHost() {
        return executionHost;
    }

    public LemmingTask setExecutionHost(String executionHost) {
        this.executionHost = executionHost;
        return this;
    }

    public String getDispatchHost() {
        return dispatchHost;
    }

    public LemmingTask setDispatchHost(String dispatchHost) {
        this.dispatchHost = dispatchHost;
        return this;
    }

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

    public String getTaskGroup() {
        return taskGroup;
    }

    public LemmingTask setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
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

    public String getParams() {
        return params;
    }

    public LemmingTask setParams(String params) {
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
        String key = this.getTaskGroup() + "_" + this.getTaskId();
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
        // if (this.toPath().equals(p.toPath())) {
        // return true;
        // }
        if (p.getTaskGroup().equals(this.getTaskGroup()) && p.getTaskId().equals(this.getTaskId())) {
            return true;
        }
        return false;
    }

    public String toPath() {
        final StringBuilder builder = new StringBuilder();
        builder.append("taskId=").append(this.getTaskId())
                .append("&taskImpl=").append(this.getTaskImpl())
                .append("&name=").append(this.getName())
                .append("&app=").append(this.getApp())
                .append("&group=").append(this.getTaskGroup())
                .append("&rule=").append(this.getRule().replace('/', '.'))
                .append("&protocol=").append(this.getProtocol())
                .append("&transport=").append(this.getTransport())
                .append("&usable=").append(this.getUsable() == null ? 0 : this.getUsable())
                .append("&suspension=").append(this.getSuspension() == null ? 0 : this.getSuspension())
                .append("&host=").append(this.getExecutionHost())
                .append("&callType=").append(this.getCallType() == null ? 0 : this.getCallType())
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
            } else if (str.startsWith("name")) {
                t.setName(str.substring(5));
            } else if (str.startsWith("app")) {
                t.setApp(str.substring(4));
            } else if (str.startsWith("group")) {
                t.setTaskGroup(str.substring(6));
            } else if (str.startsWith("rule")) {
                t.setRule(str.substring(5).replace('.', '/'));
            } else if (str.startsWith("protocol")) {
                t.setProtocol(str.substring(9));
            } else if (str.startsWith("transport")) {
                t.setTransport(str.substring(10));
            } else if (str.startsWith("usable")) {
                t.setUsable(Integer.valueOf(str.substring(7)));
            } else if (str.startsWith("suspension")) {
                t.setSuspension(Integer.valueOf(str.substring(11)));
            } else if (str.startsWith("host")) {
                t.setExecutionHost(str.substring(5));
            } else if (str.startsWith("callType")) {
                t.setCallType(Integer.valueOf(str.substring(9)));
            } else if (str.startsWith("side")) {
                t.setSide(str.substring(5));
            }
        }
        return t;
    }

    /**
     * 返回一个简易的task镜像
     * 
     */
    public LemmingTask portable() {
        LemmingTask copy = new LemmingTask();
        copy.setApp(app)
                .setCallType(callType)
                .setDelayTime(delayTime)
                .setName(name)
                .setParams(params)
                .setSync(sync)
                .setTaskGroup(taskGroup)
                .setTaskId(taskId)
                .setTaskImpl(taskImpl)
                .setTransport(transport)
                .setClients(clients);
        return copy;
    }
}