/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemmingtest;

/**
 * for http
 * 
 */
public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        return "hello htttp";
    }

}
