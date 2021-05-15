package com.neat.util;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class UtilsTest {
    //    @Ignore
    @Test
    public void testHttpConnectionDomestic() throws IOException {
        setProxy();
        processTest("http://www.baidu.com/");
    }

    @Test
    public void testHttpConnectionInternational() throws IOException {
        setProxy();
        processTest("http://www.google.com/");
    }

    //    @Ignore
    @Test
    public void testHttpsConnectionDomestic() throws IOException {
        setProxy();
        processTest("https://www.baidu.com/");
    }

    @Test
    public void testHttpsConnectionInternational() throws IOException {
        setProxy();
//        processTest("https://www.google.com/");
        processTest("https://plugins.gradle.org/m2/org/springframework/boot/spring-boot-gradle-plugin/2.3.1.RELEASE/spring-boot-gradle-plugin-2.3.1.RELEASE.pom");

    }

    @Test
    public void testProxySelector() throws URISyntaxException {
        ProxySelector defaultProxy = ProxySelector.getDefault();
        assert defaultProxy != null;
        System.out.println(defaultProxy);
        List<Proxy> proxyList = defaultProxy.select(new URI("http://127.0.0.1:9000/systemproxy-162104"));
        assert proxyList != null;
        proxyList.forEach(System.out::println);
    }

    @Test
    public void testSSLProxy() throws IOException {
        Proxy proxy = createProxy("165.225.110.10", 443);
//        Proxy proxy = createProxy("webaccess.dia.govt.nz", 8080);

        String url = "https://www.google.com/";

        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection(proxy);

        conn.setDoOutput(true);
        conn.setDoInput(true);

        IOUtils.readLines(conn.getInputStream(), "UTF-8").forEach(System.out::println);
    }

    private void setProxy() {
//        System.setProperty("java.net.useSystemProxies", "true");
//        System.setProperty("http.proxyHost", "wlgproxyforservers.dia.govt.nz");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "wlgproxyforservers.dia.govt.nz");
//        System.setProperty("https.proxyPort", "8080");

        System.setProperty("http.proxyHost", "webaccess.dia.govt.nz");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "webaccess.dia.govt.nz");
        System.setProperty("https.proxyPort", "8080");
    }

    private static Proxy createProxy(String proxyAddr, int proxyPort) {
//        Authenticator.setDefault(new Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("leefr", "wangyang+116".toCharArray());
//            }
//        });
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyAddr, proxyPort));
        return proxy;
    }

    private void processTest(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);

        IOUtils.readLines(conn.getInputStream(), "UTF-8").forEach(System.out::println);
    }
}
