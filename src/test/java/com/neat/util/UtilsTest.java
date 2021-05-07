package com.neat.util;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UtilsTest {
    @Test
    public void testHttpConnectionDomestic() throws IOException {
//        setProxy();
        processTest("http://www.baidu.com/");
    }

    @Test
    public void testHttpConnectionInternational() throws IOException {
        setProxy();
        processTest("http://www.google.com/");
    }

    @Test
    public void testHttpsConnectionDomestic() throws IOException {
        setProxy();
        processTest("https://www.baidu.com/");
    }

    @Test
    public void testHttpsConnectionInternational() throws IOException {
        setProxy();
        processTest("https://www.google.com/");
    }

    private void setProxy() {
//        System.setProperty("java.net.useSystemProxies", "true");
//        System.setProperty("http.proxyHost", "wlgproxyforservers.dia.govt.nz");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "wlgproxyforservers.dia.govt.nz");
//        System.setProperty("https.proxyPort", "8080");
    }

    private void processTest(String link) throws IOException {

        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);

        IOUtils.readLines(conn.getInputStream(), "UTF-8").forEach(System.out::println);
    }
}
