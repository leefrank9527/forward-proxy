package com.neat.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class ProxyDaemon implements Runnable {
    private int serverPort;

    public ProxyDaemon(int serverPort) {
        this.serverPort = serverPort;
    }


    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            System.out.println("Failed to initial Proxy Server");
            return;
        }

        while (true) {
            try {
                Socket southSocket = serverSocket.accept();
                Thread processor = new Thread(new ProxyProcessor(southSocket));
                processor.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
