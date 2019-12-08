/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.config;

/**
 * @author xiaoyu
 * @date 2019-04
 * @description
 */
public class LemmingContext {

    private String name;
    private String app;
    private String transport;

    public String getApp() {
        return app;
    }

    public LemmingContext setApp(String app) {
        this.app = app;
        return this;
    }

    public String getName() {
        return name;
    }

    public LemmingContext setName(String name) {
        this.name = name;
        return this;
    }

    public String getTransport() {
        return transport;
    }

    public LemmingContext setTransport(String transport) {
        this.transport = transport;
        return this;
    }

}
