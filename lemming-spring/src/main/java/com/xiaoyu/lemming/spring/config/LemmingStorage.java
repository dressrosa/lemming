/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.spring.config;

/**
 * @author xiaoyu
 * @date 2019-09
 * @description
 */
public class LemmingStorage {

    private String name;
    private String user;
    private String password;
    private String url;

    public String getUser() {
        return user;
    }

    public LemmingStorage setUser(String user) {
        this.user = user;
        return this;
    }

    public String getName() {
        return name;
    }

    public LemmingStorage setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LemmingStorage setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LemmingStorage setUrl(String url) {
        this.url = url;
        return this;
    }

}
