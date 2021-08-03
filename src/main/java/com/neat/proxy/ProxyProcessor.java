package com.neat.proxy;

import com.neat.util.IOHelper;
import com.neat.util.MultiThreadsPrint;
import com.neat.util.ReachableType;
import com.neat.util.TraceRouteUtil;
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
    private static final List<String> originProxyAddress = Collections.synchronizedList(new LinkedList<>());
    private static final List<String> domesticProxyAddress = Collections.synchronizedList(new LinkedList<>());

    private static String originProxyHost, domesticProxyHost;
    private static int originProxyPort, domesticProxyPort;
    //private static String originProxyAddress,domesticProxyAddress;

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

        ReachableType reachableType = TraceRouteUtil.getReachableType(urlEntity);
        if (reachableType == ReachableType.PROXY_IGNORE) {
            this.northSocket = new Socket(urlEntity.getHostName(), urlEntity.getHostPort());
        } else if (reachableType == ReachableType.PROXY_LOCAL) {
            this.northSocket = new Socket(domesticProxyHost, domesticProxyPort);
        } else if (reachableType == ReachableType.PROXY_REMOTE) {
            this.northSocket = new Socket(originProxyHost, originProxyPort);
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
        if (reachableType != ReachableType.PROXY_IGNORE || urlEntity.getScheme().equals(UrlEntity.SCHEME_HTTP)) {
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
        });
        Thread tB = new Thread(() -> {
            try {
                IOHelper.copy(firstLine, northInputStream, southOutputStream);
            } catch (IOException e) {
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


    private static boolean isDomesticHost(String hostName) {
        if (StringUtils.isEmpty(hostName)) {
            return false;
        }

        for (String s : domesticProxyAddress) {
            if (hostName.contains(s)) {
                return true;
            }
        }
        return false;
    }



//    private static Proxy createProxy(String proxyHost, int proxyPort) {
//        Authenticator.setDefault(new Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("leefr", "wangyang+116".toCharArray());
//            }
//        });
//        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
//        return proxy;
//    }

    public static void setOriginProxyHost(String originProxyHost) {
        ProxyProcessor.originProxyHost = originProxyHost;
    }

    public static void setDomesticProxyHost(String domesticProxyHost) {
        ProxyProcessor.domesticProxyHost = domesticProxyHost;
    }

    public static void setOriginProxyPort(int originProxyPort) {
        ProxyProcessor.originProxyPort = originProxyPort;
    }

    public static void setDomesticProxyPort(int domesticProxyPort) {
        ProxyProcessor.domesticProxyPort = domesticProxyPort;
    }

    public static void setOriginProxyAddress(String originProxyAddress) {
        String[] items = originProxyAddress.split(";");
        for (String item : items) {
            ProxyProcessor.originProxyAddress.add(item.toLowerCase());
        }

    }

    public static void setDomesticProxyAddress(String domesticProxyAddress) {
        String[] items = domesticProxyAddress.split(";");
        for (String item : items) {
            ProxyProcessor.domesticProxyAddress.add(item.toLowerCase());
        }
    }
}
