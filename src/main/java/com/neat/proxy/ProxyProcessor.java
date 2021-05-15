package com.neat.proxy;

import com.neat.util.IOHelper;
import com.neat.util.MultiThreadsPrint;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProxyProcessor implements Runnable {
    private static final List<String> ignoredHosts = Collections.synchronizedList(new LinkedList<>());
    private static String proxyHost;
    private static int proxyPort;
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

        boolean isIgnoredHost = isIgnoredHost(urlEntity.getHostName());
        try {
            if (isIgnoredHost) {
                this.northSocket = new Socket(urlEntity.getHostName(), urlEntity.getHostPort());
            } else {
                this.northSocket = new Socket(proxyHost, proxyPort);
//                this.northSocket = new Socket(createProxy(proxyHost, proxyPort));
//                this.northSocket.connect(new InetSocketAddress(urlEntity.getHostName(), urlEntity.getHostPort()));
            }
        } catch (IOException e) {
            MultiThreadsPrint.putFinished(firstLine + ": " + e.getMessage());
            return;
        }

        InputStream northInputStream = northSocket.getInputStream();
        OutputStream northOutputStream = northSocket.getOutputStream();

        /*
          1. For HTTP or HTTPS with Proxy: forward the message directly
          2. For HTTPS without Proxy: skipped HTTPS negotiation headers.
         */
        String transferredRequestLine = urlEntity.getTransferredRequestLine();
        if (!isIgnoredHost || urlEntity.getScheme().equals(UrlEntity.SCHEME_HTTP)) {
            if (urlEntity.getScheme().equals(UrlEntity.SCHEME_HTTP)) {
                IOHelper.writeln(northOutputStream, firstLine);
            } else {
                IOHelper.writeln(northOutputStream, transferredRequestLine);
            }
        } else { //HTTPS without proxy
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
                IOHelper.copy(firstLine, southInputStream, northOutputStream);
            } catch (IOException e) {
                MultiThreadsPrint.putFinished("Upload: " + e.getMessage());
            }
//            close();
        });
        Thread tB = new Thread(() -> {
            try {
                IOHelper.copy(firstLine, northInputStream, southOutputStream);
            } catch (IOException e) {
                MultiThreadsPrint.putFinished("Download: " + e.getMessage());
            }
//            close();
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

        System.out.println("[DONE] " + urlEntity.getUrl());
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

    private static boolean isIgnoredHost(String hostName) {
        if (StringUtils.isEmpty(hostName)) {
            return false;
        }

        for (String s : ignoredHosts) {
            if (hostName.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getIgnoredHosts() {
        return ignoredHosts;
    }

    public static void setIgnoredHosts(String ignoredHosts) {
        String[] items = ignoredHosts.split(";");
        for (String item : items) {
            ProxyProcessor.ignoredHosts.add(item.toLowerCase());
        }
    }

    private static Proxy createProxy(String proxyHost, int proxyPort) {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("leefr", "wangyang+116".toCharArray());
            }
        });
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
        return proxy;
    }

    public static String getProxyHost() {
        return proxyHost;
    }

    public static void setProxyHost(String proxyHost) {
        ProxyProcessor.proxyHost = proxyHost;
    }

    public static int getProxyPort() {
        return proxyPort;
    }

    public static void setProxyPort(int proxyPort) {
        ProxyProcessor.proxyPort = proxyPort;
    }
}
