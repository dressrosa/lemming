/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.lemming.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
@SpringBootApplication
@EnableWebMvc
@ImportResource("classpath:http.xml")
public class LemmingMonitorApplication {

    public static void main(String args[]) {
        SpringApplication.run(LemmingMonitorApplication.class, args);
    }
}
