package com.xiaoyu.lemming.monitor.common.utils;

import javax.servlet.http.HttpServletRequest;

import com.xiaoyu.ribbon.core.StringUtil;


/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class Utils {

    public static boolean isPC(HttpServletRequest request) {
        final String userAgentInfo = request.getHeader("user-agent");
        final String[] agents = { "Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod" };
        boolean flag = true;
        for (int v = 0; v < agents.length; v++) {
            if (userAgentInfo.indexOf(agents[v]) > 0) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 获得用户远程地址
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Real-IP");
        if (StringUtil.isNotBlank(remoteAddr)) {
            remoteAddr = request.getHeader("X-Forwarded-For");
        } else if (StringUtil.isNotBlank(remoteAddr)) {
            remoteAddr = request.getHeader("Proxy-Client-IP");
        } else if (StringUtil.isNotBlank(remoteAddr)) {
            remoteAddr = request.getHeader("WL-Proxy-Client-IP");
        }
        return remoteAddr != null ? remoteAddr : request.getRemoteAddr();
    }

    public static boolean isSafeRequest(HttpServletRequest request) {
        final String userAgentInfo = request.getHeader("user-agent");
        if (StringUtil.isBlank(userAgentInfo)) {
            return false;
        }
        return true;
    }
}
