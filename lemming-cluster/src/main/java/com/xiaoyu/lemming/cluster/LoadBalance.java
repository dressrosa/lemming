/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.cluster;

import java.util.List;

public interface LoadBalance {

    public <T> T select(final List<T> clients);

}
