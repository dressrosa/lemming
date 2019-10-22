/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemmingtest;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
@ComponentScan(basePackages = { "com.xiaoyu.lemming" })
public class SpringBizDubboServer {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-provider.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            context.start();
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
}
