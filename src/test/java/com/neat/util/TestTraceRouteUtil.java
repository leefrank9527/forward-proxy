package com.neat.util;

import org.junit.Test;

public class TestTraceRouteUtil {
    @Test
    public void testIsReachableViaLocalProxy() {
        TraceRouteUtil.setLocalProxyHost("192.168.1.3");
        TraceRouteUtil.setLocalProxyPort(3128);

        boolean isReachable = TraceRouteUtil.isLocalProxyReachable("https://www.baidu.com");
        assert isReachable;

        isReachable = TraceRouteUtil.isLocalProxyReachable("https://www.google.com");
        assert !isReachable;
    }
}
