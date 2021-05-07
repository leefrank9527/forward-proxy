package com.neat.proxy;

import com.neat.util.MultiThreadsPrint;

import java.net.URI;

public class UrlEntity {
    public static final String METHOD_CONNECT = "CONNECT";
    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_HTTP = "http";

    private String request;
    private String scheme;
    private String method;
    private String url;
    private String hostName;
    private int hostPort;

    public static UrlEntity getInstance(String request) {
        UrlEntity entity = new UrlEntity();
        entity.setRequest(request);

        String[] reqItems = request.split(" ");
        entity.setMethod(reqItems[0]);

        if (entity.getMethod().equalsIgnoreCase(METHOD_CONNECT)) {
            entity.setScheme(SCHEME_HTTPS);
        } else {
            entity.setScheme(SCHEME_HTTP);
        }

        if (reqItems.length < 2) {
            MultiThreadsPrint.putFinished("[WARN] Invalid request: " + request);
            return entity;
        }

        URI uri;
        if (reqItems[1].startsWith("http")) {
            uri = URI.create(reqItems[1]);
        } else {
            uri = URI.create(String.format("%s://%s", SCHEME_HTTPS, reqItems[1]));
        }
        entity.setHostName(uri.getHost());
        entity.setHostPort(uri.getPort() <= 0 ? 80 : uri.getPort());

        entity.setUrl(String.format("%s://%s:%d", entity.getMethod(), entity.getHostName(), entity.getHostPort()));

        return entity;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public String getTransferredRequestLine() {
        String[] reqItems = request.split(" ");
        String method = reqItems[0];
        String originalService = reqItems[1];
        String version = reqItems[2];

        if (!originalService.startsWith("http")) {
            return request;
        }

        String service = originalService;
        int idx = -1;
        if (originalService.startsWith("https://")) {
            idx = originalService.indexOf('/', "https://".length());
        } else if (originalService.startsWith("http://")) {
            idx = originalService.indexOf('/', "http://".length());
        }

        if (idx > 0) {
            service = originalService.substring(idx);
        }

        return String.format("%s %s %s", method, service, version);
    }
}
