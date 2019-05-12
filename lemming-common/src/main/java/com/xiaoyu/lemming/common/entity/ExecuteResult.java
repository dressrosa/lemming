/**
 * 
 */
package com.xiaoyu.lemming.common.entity;

/**
 * 执行结果
 * 
 * @author hongyu
 * @date 2019-05
 * @description
 */
public class ExecuteResult {

    private boolean isSuccess = false;
    private String message = "";

    public boolean isSuccess() {
        return isSuccess;
    }

    public ExecuteResult setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ExecuteResult setMessage(String message) {
        this.message = message;
        return this;
    }

}
