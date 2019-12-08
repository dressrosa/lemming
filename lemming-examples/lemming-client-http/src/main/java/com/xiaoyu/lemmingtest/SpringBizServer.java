/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemmingtest;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author xiaoyu
 * @date 2018-08
 * @description
 */
@ComponentScan(basePackages = { "com.xiaoyu" })
public class SpringBizServer {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
        // 跳过xsd检查
        context.setValidating(false);
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
