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
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class BeaconTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(BeaconTransporter.class);

    private static final int Timeout = 60_000;

    @Override
    public void callback(ExecuteResult result) throws Exception {
        GenericReference ref = new GenericReference();
        ref.setInterfaceName(CommonConstant.Lemming_Server_Service);
        ref.setGroup(CommonConstant.Lemming_Group);
        ref.setTimeout(Timeout + "");
        try {
            GenericService generic = GenericRequestLauncher.launch(ref);
            generic.$_$invoke(CommonConstant.Task_Callback_Method, Void.class,
                    new Object[] { result },
                    new Class<?>[] { ExecuteResult.class });
        } catch (Exception e) {
            logger.error(" Task[" + result.getTaskId() + "] Callback task client failed:" + e);
            throw e;
        }
        return;
    }

    @Override
    public ExecuteResult call(LemmingTask task) throws Exception {
        GenericReference ref = new GenericReference();
        ref.setInterfaceName(CommonConstant.Lemming_Client_Service);
        ref.setGroup(CommonConstant.Lemming_Group);
        ref.setTimeout(Timeout + "");
        try {
            GenericService generic = GenericRequestLauncher.launch(ref);
            ExecuteResult ret = generic.$_$invoke(CommonConstant.Task_Call_Method, ExecuteResult.class,
                    new Object[] { task },
                    new Class<?>[] { LemmingTask.class });
            if (ret.isSuccess()) {
                logger.info(" Task:" + task.getTaskId() + " do success:" + ret.getMessage());
            } else {
                logger.info(" Task:" + task.getTaskId() + " do failed:" + ret.getMessage());
            }
            return ret;

        } catch (Exception e) {
            logger.error(" Task[" + task.getTaskId() + "] Call task client failed:" + e);
            throw e;
        }
    }

    @Override
    public void export() {
        try {
            BeaconExporter exporter = new BeaconExporter();
            Context context = SpiManager.defaultSpiExtender(Context.class);
            if (CommonConstant.Client.equals(context.side())) {
                exporter.setGroup(CommonConstant.Lemming_Group)
                        .setInterfaceName(CommonConstant.Lemming_Client_Service)
                        .setRef(CommonConstant.Lemming_Service_Client_Impl)
                        .setMethods(CommonConstant.Task_Call_Method)
                        .export();
            } else {
                exporter.setGroup(CommonConstant.Lemming_Group)
                        .setInterfaceName(CommonConstant.Lemming_Server_Service)
                        .setRef(CommonConstant.Lemming_Service_Server_Impl)
                        .setMethods(CommonConstant.Task_Callback_Method)
                        .export();
            }

        } catch (Exception e) {
            logger.error("" + e);
        }
    }

}
