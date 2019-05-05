/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.config;

public class LemmingRegistry {

    private String id;
    /**
     * 地址 格式ip:port
     */
    private String address;

    /**
     * 协议 zookeeper
     */
    private String protocol;

    /**
     * 端口,在xml中不显示
     */
    private String port;

    public String getId() {
        return id;
    }

    public LemmingRegistry setId(String id) {
        this.id = id;
        return this;
    }

    public String getPort() {
        return port;
    }

    public LemmingRegistry setPort(String port) {
        this.port = port;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public LemmingRegistry setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public LemmingRegistry setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

}
