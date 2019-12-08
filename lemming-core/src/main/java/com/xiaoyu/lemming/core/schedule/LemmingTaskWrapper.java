package com.xiaoyu.lemming.core.schedule;

import com.xiaoyu.lemming.common.entity.LemmingParam;
import com.xiaoyu.lemming.core.api.LemmingTask;

/**
 * @author xiaoyu
 * @param
 * @date 2019-04
 * @description 继承此类,实现handler,也可以加上注解来标注具体的信息
 */
public abstract class LemmingTaskWrapper extends LemmingTask {

    private static final long serialVersionUID = 1L;

    public void handleTask(LemmingParam lemmingParam) {
        try {
            System.out.println("params:" + lemmingParam.getParams());
            this.handle(lemmingParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void handle(LemmingParam lemmingParam);

}