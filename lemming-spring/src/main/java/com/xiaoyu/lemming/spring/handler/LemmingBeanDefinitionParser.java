/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.handler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.common.util.NetUtil;
import com.xiaoyu.lemming.common.util.StringUtil;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;
import com.xiaoyu.lemming.spring.config.LemmingContext;
import com.xiaoyu.lemming.spring.config.LemmingRegistry;
import com.xiaoyu.lemming.spring.config.LemmingStorage;
import com.xiaoyu.lemming.spring.config.Task;
import com.xiaoyu.lemming.spring.constant.SpringConstant;
import com.xiaoyu.lemming.spring.listener.LemmingSpringContextListener;
import com.xiaoyu.lemming.storage.Storage;

/**
 * @author hongyu
 * @date 2019-04
 * @description 解析xml
 */
public class LemmingBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(LemmingBeanDefinitionParser.class);

    private static Set<LemmingTask> taskSet = new HashSet<>();

    private Class<?> cls;

    public LemmingBeanDefinitionParser(Class<?> cls) {
        this.cls = cls;
    }

    public static Set<LemmingTask> getTaskSet() {
        return taskSet;
    }

    public static void removeTaskSet() {
        taskSet = null;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return cls;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        try {
            if (cls == LemmingContext.class) {
                doParseContext(element, parserContext, builder);
            } else if (cls == LemmingRegistry.class) {
                doParseRegistry(element, parserContext, builder);
            } else if (cls == Task.class) {
                doParseTask(element, parserContext, builder);
            } else if (cls == LemmingStorage.class) {
                doParseStorage(element, parserContext, builder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doParseContext(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String name = element.getAttribute("name");
        String app = element.getAttribute("app");
        String transport = element.getAttribute("transport");
        element.setAttribute("id", "lemming_context_config");
        if (!"server".equals(name) && !"client".equals(name)) {
            throw new Exception(" Name is invalid in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(app)) {
            throw new Exception(" App cannot be null in xml tag->" + element.getTagName());
        }
        if (!"beacon".equals(transport) && !"dubbo".equals(transport)) {
            throw new Exception(" Transport is invalid in xml tag->" + element.getTagName());
        }

        try {
            //放在contextlistener里面启动
            Context context = SpiManager.holder(Context.class).target(name);
            context.initTransporter(transport);
            
            // 监听spring
            this.doRegisterLemmingListenerEvent(parserContext, app, transport);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void doParseRegistry(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String address = element.getAttribute("address");
        String protocol = element.getAttribute("protocol");
        element.setAttribute("id", "lemming_registry_config");
        if (protocol == null) {
            protocol = "zookeeper";
        }
        if (StringUtil.isBlank(address)) {
            throw new Exception("Address cannot be null in xml tag->" + element.getTagName());
        }
        String[] addr = address.split(":");
        if (addr.length != 2) {
            throw new Exception("Address->" + address + " is illegal in xml tag->" + element.getTagName());
        }
        if (!StringUtil.isIP(addr[0]) || !NumberUtils.isParsable(addr[1])) {
            throw new Exception("Address->" + address + " is illegal in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(protocol)) {
            throw new Exception("Protocol can ignore but not empty in xml tag->" + element.getTagName());
        }

        try {
            Registry reg = SpiManager.holder(Registry.class).target(protocol);
            if (reg == null) {
                throw new Exception("Cannot find protocol->" + protocol + " in xml tag->" + element.getTagName());
            }
            reg.address(address);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void doParseTask(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String taskId = element.getAttribute("taskId");
        element.setAttribute("id", "task_config_" + taskId);
        String taskImpl = element.getAttribute("taskImpl");
        String rule = element.getAttribute("rule");
        String name = element.getAttribute("name");
        if (StringUtil.isBlank(name)) {
            name = "";
        }
        if (StringUtil.isBlank(taskId)) {
            throw new Exception(" TaskId cannot be null in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(taskImpl)) {
            throw new Exception(" TaskImpl cannot be null in xml tag->" + element.getTagName());
        }
        // 检查合法性
        try {
            Class.forName(taskImpl);
        } catch (Exception e) {
            throw new Exception(" TaskImpl cannot be found " + e);
        }
        try {
            // 注册服务
            LemmingTask task = new LemmingTask();
            task.setTaskId(taskId)
                    .setName(name)
                    .setTaskGroup("")
                    .setTaskImpl(taskImpl)
                    .setRule(rule == null ? "" : rule)
                    .setParams(null)
                    .setHost(NetUtil.localIP())// TODO
                    .setCallType(0)
                    .setSide("client");
            taskSet.add(task);
            // 注册bean
            BeanDefinitionRegistry springBeanRegistry = parserContext.getRegistry();
            if (springBeanRegistry.containsBeanDefinition(taskId)) {
                LOG.warn("Repeat register. please check in xml with lemming-task ,taskImpl->{}",
                        task.getTaskImpl());
                return;
            }

            GenericBeanDefinition def = new GenericBeanDefinition();
            def.setBeanClass(task.getClass());
            def.getPropertyValues().add("taskId", task.getTaskId());
            def.getPropertyValues().add("taskGroup", task.getTaskGroup());
            def.getPropertyValues().add("taskImpl", task.getTaskImpl());
            def.getPropertyValues().add("params", task.getParams());
            def.setLazyInit(false);
            def.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            springBeanRegistry.registerBeanDefinition("lemming_task_" + taskId, def);

        } catch (Exception e) {
            LOG.error("Cannot resolve task,please check in xml tag lemming-task with id->{}", taskId);
            return;
        }
    }

    private void doParseStorage(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String user = element.getAttribute("user");
        String name = element.getAttribute("name");
        String password = element.getAttribute("password");
        String url = element.getAttribute("url");
        element.setAttribute("id", "lemming_storage_config");
        if (StringUtil.isBlank(url)) {
            throw new Exception(" Url cannot be null in xml tag->" + element.getTagName());
        }
        Storage storage = SpiManager.holder(Storage.class).target(name);
        storage.init(url, user, password);
    }

    private void doRegisterLemmingListenerEvent(ParserContext parserContext, String app, String transport) {
        if (!parserContext.getRegistry().containsBeanDefinition(SpringConstant.Listener_Event)) {
            GenericBeanDefinition def = new GenericBeanDefinition();
            def.setBeanClass(LemmingSpringContextListener.class);
            def.getPropertyValues().add("app", app);
            def.getPropertyValues().add("transport", transport);
            def.setLazyInit(false);
            parserContext.getRegistry().registerBeanDefinition(SpringConstant.Listener_Event, def);
        }
    }
}
