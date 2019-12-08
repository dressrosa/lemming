/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

import java.io.Serializable;

/**
 * 执行参数
 * 
 * @author xiaoyu
 * @date 2019-12
 * @description
 */
public class LemmingParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String params;

    public String getParams() {
        return params;
    }

    public LemmingParam setParams(String params) {
        this.params = params;
        return this;
    }
}
