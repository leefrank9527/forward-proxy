package com.neat.proxy;

import com.neat.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class ProxyProcessor implements Runnable {
    private final Socket southSocket;
    private Socket northSocket;

    public ProxyProcessor(Socket southSocket) {
        this.southSocket = southSocket;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (IOException e) {
            MultiThreadsPrint.putFinished(e.getMessage());
        } finally {
            close();
        }
    }

    private void process() throws IOException {
        InputStream southInputStream = southSocket.getInputStream();
        OutputStream southOutputStream = southSocket.getOutputStream();

        String firstLine = IOHelper.readln(southInputStream);
        UrlEntity urlEntity = UrlEntity.getInstance(firstLine);

        EntityProxyRoute proxyRoute = TraceRouteUtil.getProxyRoute(urlEntity);
        if (proxyRoute.getReachableType() == ReachableType.PROXY_IGNORE) {
            this.northSocket = new Socket(urlEntity.getHostName(), urlEntity.getHostPort());
        } else if (proxyRoute.getReachableType() == ReachableType.PROXY_LOCAL || proxyRoute.getReachableType() == ReachableType.PROXY_REMOTE) {
            this.northSocket = new Socket(proxyRoute.getHost(), proxyRoute.getPort());
        } else {
            MultiThreadsPrint.putFinished(firstLine + ": " + "Failed to get the reachable");
            close();
            return;
        }

        InputStream northInputStream = northSocket.getInputStream();
        OutputStream northOutputStream = northSocket.getOutputStream();

        /*
          1. For HTTP or HTTPS with Proxy: forward the message directly
          2. For HTTPS without Proxy: skipped HTTPS negotiation headers.
         */
        String transferredRequestLine = urlEntity.getTransferredRequestLine();
        if (proxyRoute.getReachableType() != ReachableType.PROXY_IGNORE || urlEntity.getScheme().equals(UrlEntity.SCHEME_HTTP)) {
            if (urlEntity.getScheme().equals(UrlEntity.SCHEME_HTTP)) {
                IOHelper.writeln(northOutputStream, firstLine);
            } else {
                IOHelper.writeln(northOutputStream, transferredRequestLine);
            }
        } else { //HTTPS without proxy: to skip the lines for proxy
            while (true) {
                String headerLine = IOHelper.readln(southInputStream);
                if (IOHelper.isNull(headerLine)) {
                    break;
                }
            }

            IOHelper.writeln(southOutputStream, "HTTP/1.1 200 Connection Established");
            IOHelper.writeln(southOutputStream, "Proxy-agent: Netscape-Proxy/1.1");
            IOHelper.writeln(southOutputStream, "");
        }


        Thread tA = new Thread(() -> {
            try {
                String prefixFormat = urlEntity.getHostName() + " [SEND] %d Bytes";
                IOHelper.copy(prefixFormat, southInputStream, northOutputStream);
            } catch (IOException | InterruptedException e) {
                MultiThreadsPrint.putFinished("Upload: " + e.getMessage());
            }
        });
        Thread tB = new Thread(() -> {
            try {
                String prefixFormat = urlEntity.getHostName() + " [RECV] %d Bytes";
                IOHelper.copy(prefixFormat, northInputStream, southOutputStream);
            } catch (IOException | InterruptedException e) {
                MultiThreadsPrint.putFinished("Download: " + e.getMessage());
            }
        });

        tA.start();
        tB.start();

        try {
            tA.join();
            tB.join();
        } catch (InterruptedException e) {
            MultiThreadsPrint.putFinished(e.getMessage());
        } finally {
            close();
        }

        //System.out.println("[DONE] " + urlEntity.getUrl());
    }

    private void close() {
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            MultiThreadsPrint.putFinished(e.getMessage());
        }

        if (this.southSocket != null && !this.southSocket.isClosed()) {
            try {
                this.southSocket.close();
            } catch (IOException e) {
                MultiThreadsPrint.putFinished(e.getMessage());
            }
        }
        if (this.northSocket != null && !northSocket.isClosed()) {
            try {
                this.northSocket.close();
            } catch (IOException e) {
                MultiThreadsPrint.putFinished(e.getMessage());
            }
        }
    }

}
