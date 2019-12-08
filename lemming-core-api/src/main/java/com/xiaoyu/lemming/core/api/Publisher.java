/**
 * 
 */
package com.xiaoyu.lemming.core.api;

/**
 * @author xiaoyu
 * @date 2019-03
 * @description
 */
public interface Publisher {

    public void publish(Task task);

    public void call(Task task);
}
