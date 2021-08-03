package com.neat.util;

import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

public class TestSocketReachable {
    @Test
    public void testSocketReachableLocal() {
        String host = "www.qq.com";
        int port = 80;

        boolean reachable = isReachable(host, port);

        assert reachable;
    }

    @Test
    public void testSocketReachableInternet() {
        String host = "www.google.com";
        int port = 80;
        boolean reachable = isReachable(host, port);

        assert !reachable;
    }

    private boolean isReachable(String host, int port) {
        boolean reachable = false;
        try {
            Socket socket = new Socket(host, port);
            socket.getInetAddress().isReachable(200);
            reachable = true;
        } catch (IOException e) {
            reachable = false;
        }

        System.out.printf("%s:%d reachable=>%s \r\n", host, port, Boolean.toString(reachable));

        return reachable;
    }
}
