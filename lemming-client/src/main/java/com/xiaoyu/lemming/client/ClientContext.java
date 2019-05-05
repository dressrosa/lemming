package com.xiaoyu.lemming.client;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class ClientContext implements Context {

    private Registry registry = null;

    public ClientContext() {
    }

    @Override
    public String side() {
        return "client";
    }

    @Override
    public void start() {

    }

    @Override
    public LemmingTask getLocalTask(String taskId, String group) {
        try {
            registry = SpiManager.defaultSpiExtender(Registry.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final LemmingTask task = registry.getLocalTask(taskId);
        return task;
    }

    @Override
    public void close() {
        if (registry != null) {
            registry.close();
        }
    }

    @Override
    public void initTransporter(String transporter) {
        try {
            Transporter trans = SpiManager.holder(Transporter.class).target(transporter);
            // 暴露公共service
            trans.export();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
