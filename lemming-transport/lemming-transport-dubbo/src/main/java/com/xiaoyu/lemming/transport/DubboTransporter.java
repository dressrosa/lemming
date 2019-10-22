/**
 * 
 */
package com.xiaoyu.lemming.transport;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.spring.listener.LemmingSpringContextListener;

/**
 * @author hongyu
 * @date 2019-10
 * @description 这里依赖spring获取dubbo相关的bean
 */
public class DubboTransporter extends AbstractTransporter {

    private static final Logger logger = LoggerFactory.getLogger(DubboTransporter.class);

    /**
     * client端服务callback方法,server端服务call方法
     */
    private static GenericService genericService;

    @Override
    public void callback(ExecuteResult result) throws Exception {
        if (genericService == null) {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setInterface(CommonConstant.Lemming_Server_Service);
            reference.setGroup(CommonConstant.Lemming_Group);
            reference.setTimeout(Timeout);
            reference.setGeneric(true);
            ApplicationContext springContext = LemmingSpringContextListener.getSpringContext();
            reference.setRegistry(springContext.getBean(RegistryConfig.class));
            reference.setApplication(springContext.getBean(ApplicationConfig.class));
            genericService = reference.get();
        }
        try {
            genericService.$invoke(CommonConstant.Task_Callback_Method,
                    new String[] { ExecuteResult.class.getName() },
                    new Object[] { result });
        } catch (Exception e) {
            logger.error(" Task[" + result.getTaskId() + "] Callback task client failed:", e);
            throw e;
        }
        return;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ExecuteResult doCall(LemmingTask task, LemmingTaskClient client) throws Exception {
        if (genericService == null) {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setInterface(CommonConstant.Lemming_Client_Service);
            reference.setGroup(CommonConstant.Lemming_Group);
            reference.setTimeout(Timeout);
            reference.setGeneric(true);
            ApplicationContext springContext = LemmingSpringContextListener.getSpringContext();
            reference.setRegistry(springContext.getBean(RegistryConfig.class));
            reference.setApplication(springContext.getBean(ApplicationConfig.class));
            genericService = reference.get();
        }
        try {
            Object result = genericService.$invoke(CommonConstant.Task_Call_Method,
                    new String[] { LemmingTask.class.getName() },
                    new Object[] { task });
            Map map = (HashMap) result;
            ExecuteResult ret = new ExecuteResult();
            ret.setApp(task.getApp());
            ret.setGroup(task.getTaskGroup());
            ret.setHost((String) map.get("host"));
            ret.setMessage((String) map.get("message"));
            ret.setSuccess((Boolean) map.get("success"));
            ret.setTaskId(task.getTaskId());
            ret.setTraceId((String) map.get("traceId"));
            return ret;
        } catch (Exception e) {
            logger.error(" Task[" + task.getTaskId() + "] Call task client failed:", e);
            throw e;
        }
    }

    @Override
    public void export() throws Exception {
        Context context = SpiManager.defaultSpiExtender(Context.class);
        ServiceConfig<Object> service = new ServiceConfig<>();
        ApplicationContext springContext = LemmingSpringContextListener.getSpringContext();
        ApplicationConfig appConfig = springContext.getBean(ApplicationConfig.class);
        appConfig.setQosEnable(false);
        service.setApplication(appConfig);
        service.setRegistry(springContext.getBean(RegistryConfig.class));
        service.setGroup(CommonConstant.Lemming_Group);
        if (CommonConstant.Client.equals(context.side())) {
            service.setProtocol(springContext.getBean(ProtocolConfig.class));
            service.setInterface(Class.forName(CommonConstant.Lemming_Client_Service));
            service.setRef(Class.forName(CommonConstant.Lemming_Service_Client_Impl).newInstance());
        } else {
            service.setInterface(Class.forName(CommonConstant.Lemming_Server_Service));
            service.setRef(Class.forName(CommonConstant.Lemming_Service_Server_Impl).newInstance());
        }
        service.export();
    }

}
