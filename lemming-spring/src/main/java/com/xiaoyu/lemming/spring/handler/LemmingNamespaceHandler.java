/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.xiaoyu.lemming.spring.config.LemmingContext;
import com.xiaoyu.lemming.spring.config.LemmingRegistry;
import com.xiaoyu.lemming.spring.config.Task;

/**
 * @author hongyu
 * @date 2019-04
 * @description 解析xml
 */
public class LemmingNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        this.registerBeanDefinitionParser("registry", new LemmingBeanDefinitionParser(LemmingRegistry.class));
        this.registerBeanDefinitionParser("task", new LemmingBeanDefinitionParser(Task.class));
        this.registerBeanDefinitionParser("context", new LemmingBeanDefinitionParser(LemmingContext.class));
    }

}
