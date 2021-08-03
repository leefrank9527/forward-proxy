package com.neat.util;

import com.neat.proxy.UrlEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class TraceRouteUtil {
    private static final List<String> ignoredHostsList = Collections.synchronizedList(new LinkedList<>());
    private static final String _CACHED_ROUTE = "conf/cached.txt";
    private static final Map<String, EntityHostRoute> routes = new HashMap<>();

    private static String localProxyHost, remoteProxyHost;
    private static int localProxyPort, remoteProxyPort;

    public static void init() {
        File cachedRouteFile = new File(_CACHED_ROUTE);
        if (!cachedRouteFile.exists()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(cachedRouteFile.toPath());
            lines.stream().filter(StringUtils::isNotEmpty).forEach(line -> {
                String[] items = line.split(" ");
                EntityHostRoute entity = new EntityHostRoute();
                entity.setHost(items[0]);
                entity.setPort(Integer.parseInt(items[1]));
                entity.setReachableType(ReachableType.valueOf(items[2]));

                routes.put(entity.getKey(), entity);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void putEntityHostRoute(UrlEntity urlEntity, ReachableType reachableType) {
        EntityHostRoute entity = new EntityHostRoute();
        entity.setHost(urlEntity.getHostName());
        entity.setPort(urlEntity.getHostPort());
        entity.setReachableType(reachableType);

        if (routes.containsKey(entity.getKey()) || reachableType == ReachableType.PROXY_UNKNOWN) {
            return;
        }

        routes.put(entity.getKey(), entity);

        File cachedRouteFile = new File(_CACHED_ROUTE);
        try {
            FileUtils.write(cachedRouteFile, entity.getUnl() + System.lineSeparator(), Charset.defaultCharset(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ReachableType getReachableType(UrlEntity urlEntity) {
        String key = EntityHostRoute.getKeyOfUrlEntity(urlEntity);

        EntityHostRoute entity = routes.get(key);

        if (entity != null && entity.getReachableType() != ReachableType.PROXY_UNKNOWN) {
            return entity.getReachableType();
        }

        ReachableType reachableType = tryReachableType(urlEntity);

        putEntityHostRoute(urlEntity, reachableType);

        return reachableType;
    }

    public static ReachableType tryReachableType(UrlEntity urlEntity) {
        boolean ignoreReachable = isIgnoredHost(urlEntity.getHostName()); //isDirectReachable(urlEntity.getHostName(), urlEntity.getHostPort());
        if (ignoreReachable) {
            return ReachableType.PROXY_IGNORE;
        }

        boolean directReachable = false;

        try {
            directReachable = isDirectReachable(urlEntity.getUrl());
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (directReachable) {
            return ReachableType.PROXY_IGNORE;
        }

        boolean localProxyReachable = false;

        try {
            localProxyReachable = isLocalProxyReachable(urlEntity.getUrl());
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (localProxyReachable) {
            return ReachableType.PROXY_LOCAL;
        }


        boolean remoteProxyReachable = false;

        try {
            remoteProxyReachable = isRemoteProxyReachable(urlEntity.getUrl());
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (remoteProxyReachable) {
            return ReachableType.PROXY_REMOTE;
        }

        return ReachableType.PROXY_UNKNOWN;
    }

//    public static boolean isDirectReachable(String host, int port) {
//        boolean reachable = false;
//        try {
//            Socket socket = new Socket(host, port);
//            socket.getInetAddress().isReachable(200);
//            reachable = true;
//        } catch (IOException e) {
//            reachable = false;
//        }
//
//        System.out.printf("%s:%d reachable=>%s \r\n", host, port, Boolean.toString(reachable));
//
//        return reachable;
//    }

    public static boolean isDirectReachable(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            int rst = conn.getResponseCode();

            System.out.println(url + "\t" + rst);

            return rst < 500;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLocalProxyReachable(String url) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(localProxyHost, localProxyPort));

            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection(proxy);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            int rst = conn.getResponseCode();

            System.out.println(url + "\t" + rst);

            return rst < 500;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static boolean isRemoteProxyReachable(String url) {
        try {

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(remoteProxyHost, remoteProxyPort));

            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection(proxy);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            int rst = conn.getResponseCode();

            System.out.println(url + "\t" + rst);

            return rst < 500;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }

    }

    public static String getLocalProxyHost() {
        return localProxyHost;
    }

    public static void setLocalProxyHost(String localProxyHost) {
        TraceRouteUtil.localProxyHost = localProxyHost;
    }

    public static int getLocalProxyPort() {
        return localProxyPort;
    }

    public static void setLocalProxyPort(int localProxyPort) {
        TraceRouteUtil.localProxyPort = localProxyPort;
    }

    private static boolean isIgnoredHost(String hostName) {
        if (StringUtils.isEmpty(hostName)) {
            return false;
        }

        for (String s : ignoredHostsList) {
            if (hostName.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static void setIgnoredHosts(String ignoredHosts) {
        String[] items = ignoredHosts.split(";");
        ignoredHostsList.addAll(Arrays.asList(items));
    }

    public static String getRemoteProxyHost() {
        return remoteProxyHost;
    }

    public static void setRemoteProxyHost(String remoteProxyHost) {
        TraceRouteUtil.remoteProxyHost = remoteProxyHost;
    }

    public static int getRemoteProxyPort() {
        return remoteProxyPort;
    }

    public static void setRemoteProxyPort(int remoteProxyPort) {
        TraceRouteUtil.remoteProxyPort = remoteProxyPort;
    }
}
