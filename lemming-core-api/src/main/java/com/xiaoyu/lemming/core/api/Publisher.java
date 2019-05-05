/**
 * 
 */
package com.xiaoyu.lemming.core.api;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public interface Publisher {

    public void publish(Task task);

    public void call(Task task);
}
