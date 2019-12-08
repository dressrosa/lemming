/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.xiaoyu.lemming.cluster.LoadBalance;

public class RandomLoadBalance implements LoadBalance {

    private static final Random random = new Random();

    @Override
    public <T> T select(final List<T> clients) {
        int size = clients.size();
        if (size == 1) {
            return clients.get(0);
        }
        return clients.get(random.nextInt(size));
    }
}
