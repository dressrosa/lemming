package com.xiaoyu.lemming.spring.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <ul>
 * <li>author:xiaoyu</li>
 * <li>time:2019年11月25日</li>
 * </ul>
 */
@RestController
public class LemmingController {

    @RequestMapping("/helloworld")
    public String getHello() {
        System.out.println("hello");
        return "hello";
    }
}
