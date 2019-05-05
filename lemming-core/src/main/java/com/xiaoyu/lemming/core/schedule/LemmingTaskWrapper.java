package com.xiaoyu.lemming.core.schedule;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author hongyu
 * @param
 * @date 2019-04
 * @description 继承此类,实现handler,也可以加上注解来标注具体的信息
 */
public abstract class LemmingTaskWrapper extends LemmingTask {

    private static final long serialVersionUID = 1L;

    public void handleTask() {
        Transporter transporter = null;
        try {
            this.handle();
            transporter = SpiManager.defaultSpiExtender(Transporter.class);
            // transporter.callback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void handle();

}