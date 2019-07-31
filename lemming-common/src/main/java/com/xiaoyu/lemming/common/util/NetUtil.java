package com.xiaoyu.lemming.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
public class NetUtil {

    public static String localIP() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
        return addr.getHostAddress();
    }
}