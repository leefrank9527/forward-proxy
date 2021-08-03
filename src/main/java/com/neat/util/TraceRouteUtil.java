package com.neat.util;

import com.neat.proxy.UrlEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class TraceRouteUtil {
    private static final List<String> ignoredHostsList = Collections.synchronizedList(new LinkedList<>());
    private static final String _CACHED_ROUTE = "conf/cached.txt";
    private static final Map<String, EntityProxyRoute> routes = new HashMap<>();

    private static final List<EntityProxyRoute> PROXY_ROUTES = new ArrayList<>();

    public static void init() {
        File cachedRouteFile = new File(_CACHED_ROUTE);
        if (!cachedRouteFile.exists()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(cachedRouteFile.toPath());
            lines.stream().filter(StringUtils::isNotEmpty).forEach(line -> {
                String[] items = line.split(" ");
                EntityProxyRoute entity = new EntityProxyRoute();
                entity.setHost(items[0]);
                entity.setPort(Integer.parseInt(items[1]));
                entity.setReachableType(ReachableType.valueOf(items[2]));

                routes.put(entity.getKey(), entity);
            });
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    synchronized private static void putEntityHostRoute(UrlEntity urlEntity, EntityProxyRoute proxyRoute) {
        String key = EntityProxyRoute.getKeyOfUrlEntity(urlEntity);

        if (routes.containsKey(key) || proxyRoute.getReachableType() == ReachableType.PROXY_UNKNOWN) {
            return;
        }

        routes.put(key, proxyRoute);

//        File cachedRouteFile = new File(_CACHED_ROUTE);
//        try {
//            FileUtils.write(cachedRouteFile, proxyRoute.getUnl() + System.lineSeparator(), Charset.defaultCharset(), true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static EntityProxyRoute getProxyRoute(UrlEntity urlEntity) {
        String key = EntityProxyRoute.getKeyOfUrlEntity(urlEntity);

        EntityProxyRoute proxyRoute = routes.get(key);

        if (proxyRoute != null && proxyRoute.getReachableType() != ReachableType.PROXY_UNKNOWN) {
            return proxyRoute;
        }

        proxyRoute = tryReachableType(urlEntity);

        putEntityHostRoute(urlEntity, proxyRoute);

        return proxyRoute;
    }

    public static EntityProxyRoute tryReachableType(UrlEntity urlEntity) {
        boolean ignoreReachable = isIgnoredHost(urlEntity.getHostName()); //isDirectReachable(urlEntity.getHostName(), urlEntity.getHostPort());
        if (ignoreReachable) {
            EntityProxyRoute proxyRoute = new EntityProxyRoute();
            proxyRoute.setReachableType(ReachableType.PROXY_IGNORE);
            return proxyRoute;
        }


        for (EntityProxyRoute proxyRoute : PROXY_ROUTES) {
            if (isProxyReachable(urlEntity.getUrl(), proxyRoute.getProxy())) {
                return proxyRoute;
            }
        }

        EntityProxyRoute proxyRoute = new EntityProxyRoute();
        proxyRoute.setReachableType(ReachableType.PROXY_UNKNOWN);
        return proxyRoute;
    }


    public static boolean isProxyReachable(String url, Proxy proxy) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection(proxy);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int rst = conn.getResponseCode();

            //System.out.println(url + "\t" + rst);

            return rst < 500;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }

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

    public static void setProxyServers(String[] proxyServers) {
        for (String proxyLink : proxyServers) {
            EntityProxyRoute proxyRoute = new EntityProxyRoute();
            String[] items = proxyLink.split(":");

            String host = items[0];
            int port = Integer.parseInt(items[1]);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

            proxyRoute.setHost(host);
            proxyRoute.setPort(port);
            proxyRoute.setProxy(proxy);
            proxyRoute.setReachableType(ReachableType.PROXY_REMOTE);
            PROXY_ROUTES.add(proxyRoute);
        }
    }
}
