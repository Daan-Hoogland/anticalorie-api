package io.hoogland.anticalorieapi.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpAddrUtils {

    public static String getIpAddressForRequest(HttpServletRequest request) {
        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isEmpty()) {
            ipAddr = request.getRemoteAddr();
        }
        return ipAddr;
    }
}
