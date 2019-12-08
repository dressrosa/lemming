/**
 * 
 */
package com.xiaoyu.lemming.core.schedule;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Publisher;
import com.xiaoyu.lemming.core.api.Task;
import com.xiaoyu.lemming.transport.Transporter;

/**
 * @author xiaoyu
 * @date 2019-03
 * @description
 */
public class LemmingPublisher implements Publisher {

    private Transporter transporter;

    @Override
    public void publish(Task task) {
        try {
            transporter = SpiManager.defaultSpiExtender(Transporter.class);
            transporter.export();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void call(Task task) {
        // TODO Auto-generated method stub

    }

}
