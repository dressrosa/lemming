/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.listener;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.StringUtil;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.spring.handler.LemmingBeanDefinitionParser;

/**
 * @author hongyu
 * @date 2019-04
 * @description spring监听事件,对spring启动完成和结束进行监听
 */

public class LemmingSpringContextListener implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(LemmingSpringContextListener.class);

    private ApplicationContext springContext;

    private String app;
    private String transport;

    public LemmingSpringContextListener() {
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            doInitTask();
            doStartContext();
        } else if (event instanceof ContextClosedEvent) {
            LOG.info("spring close event");
            try {
                Context context = SpiManager.defaultSpiExtender(Context.class);
                context.close();
            } catch (Exception e) {
                LOG.error("" + e);
            }
        }
    }

    private void doStartContext() {
        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            context.start();
        } catch (Exception e) {
            LOG.error("" + e);
        }
    }

    /**
     * 
     */
    private void doInitTask() {
        // 更新所有task
        try {
            Set<LemmingTask> localTasks = LemmingBeanDefinitionParser.getTaskSet();
            for (LemmingTask t : localTasks) {
                t.setTransport(this.getTransport());
                t.setApp(this.getApp());
                Class<?> cls = Class.forName(t.getTaskImpl());
                Map<String, ?> proxyBeans = springContext.getBeansOfType(cls, true, true);
                if (proxyBeans.isEmpty()) {
                    String key = StringUtil.lowerFirstChar(cls.getSimpleName());
                    if (springContext.containsBean(key)) {
                        // p.setProxy(BeaconUtil.getOriginBean(springContext.getBean(key)));
                    } else {
                        throw new Exception(
                                "cannot find spring bean with name '" + cls.getName() + "'");
                    }
                } else {
                    // 设置spring bean
                    Iterator<?> iter = proxyBeans.values().iterator();
                    if (proxyBeans.size() == 1) {
                        t.setProxy(iter.next());
                    } else {
                        while (iter.hasNext()) {
                            Object bean = iter.next();
                            if (cls.isInstance(bean)) {
                                t.setProxy(bean);
                                break;
                            }
                        }
                    }
                }
                Registry reg = SpiManager.defaultSpiExtender(Registry.class);
                if (reg.isInit()) {
                    reg.registerTask(t);
                } else {
                    throw new Exception("Cannot inited Registry");
                }
            }
            Context context = SpiManager.defaultSpiExtender(Context.class);
            if ("server".equals(context.side())) {
                Registry reg = SpiManager.defaultSpiExtender(Registry.class);
                reg.initServerListener();
            }
            // 使命完成
            LemmingBeanDefinitionParser.removeTaskSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }

}
