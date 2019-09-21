/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemmingtest;

/**
 * for beacon
 * 
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class HelloServiceImpl implements IHelloService {

    @Override
    public String hello(String name) {
        return "hello";
    }

}
