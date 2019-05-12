/**
 * 
 */
package com.xiaoyu.lemming.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.generic.GenericReference;
import com.xiaoyu.beacon.proxy.common.GenericRequestLauncher;
import com.xiaoyu.beacon.rpc.service.GenericService;
import com.xiaoyu.beacon.spring.config.BeaconExporter;
import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class BeaconTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(BeaconTransporter.class);

    @Override
    public void callback(LemmingTask task) {
        task.setRunning(false);
    }

    @Override
    public ExecuteResult call(LemmingTask task) throws Exception {
        GenericReference ref = new GenericReference();
        ref.setInterfaceName(CommonConstant.Lemming_Service);
        ref.setGroup(CommonConstant.Lemming_Group);
        ref.setTimeout(Integer.MAX_VALUE + "");
        try {
            GenericService generic = GenericRequestLauncher.launch(ref);
            Object result = generic.$_$invoke(CommonConstant.Task_Method, Boolean.class, new Object[] { task });
            if (result instanceof ExecuteResult) {
                ExecuteResult ret = (ExecuteResult) result;
                if (ret.isSuccess()) {
                    logger.info(" Task:" + task.getTaskId() + " do success:" + ret.getMessage());
                } else {
                    logger.info(" Task:" + task.getTaskId() + " do failed:" + ret.getMessage());
                }
                return ret;
            }
        } catch (Exception e) {
            logger.error(" Task[" + task.getTaskId() + "] Call task client failed:" + e);
            throw e;
        }
        return new ExecuteResult();
    }

    @Override
    public void export() {
        BeaconExporter exporter = new BeaconExporter();
        try {
            exporter.setGroup(CommonConstant.Lemming_Group)
                    .setInterfaceName(CommonConstant.Lemming_Service)
                    .setRef(CommonConstant.Lemming_Service_Impl)
                    .export();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
