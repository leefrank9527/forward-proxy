package com.neat.util;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class UtilsTest {
    //    @Ignore
    @Test
    public void testHttpConnectionDomestic() throws IOException {
        setProxy();
        processTest("http://www.baidu.com/");
//        processTest("http://dps.exlibris.com/repository/SipWebServices?wsdl");
    }

    @Test
    public void testHttpConnectionInternational() throws IOException {
        setProxy();
        processTest("http://www.google.com/");
//        processTest("http://builds.archive.org/maven2/org/apache/hadoop/hadoop-core/0.20.2-cdh3u4/hadoop-core-0.20.2-cdh3u4.jar");
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
        processTest("https://www.google.com/");
//        processTest("https://plugins.gradle.org/m2/org/springframework/boot/spring-boot-gradle-plugin/2.3.1.RELEASE/spring-boot-gradle-plugin-2.3.1.RELEASE.pom");

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

//        System.setProperty("http.proxyHost", "webaccess.dia.govt.nz");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "webaccess.dia.govt.nz");
//        System.setProperty("https.proxyPort", "8080");

        System.setProperty("http.proxyHost", "165.225.110.10");
        System.setProperty("http.proxyPort", "443");
        System.setProperty("https.proxyHost", "165.225.110.10");
        System.setProperty("https.proxyPort", "443");
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

    private void processHttpsTest(String link) throws IOException {
        URL url = new URL(link);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);

//        conn.setHostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String hostname, SSLSession session) {
//                /** if it necessarry get url verfication */
//                //return HttpsURLConnection.getDefaultHostnameVerifier().verify("your_domain.com", session);
//                return true;
//            }
//        });
        //conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

        conn.setSSLSocketFactory(getSSLSocketFactory());
        conn.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                        return true;
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify(hostname, session);
            }
        });


        conn.connect();

        IOUtils.readLines(conn.getInputStream(), "UTF-8").forEach(System.out::println);
    }


    private SSLSocketFactory getSSLSocketFactory() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            InputStream caInput = getResources().openRawResource(R.raw.your_cert);
            Resource resource = new ClassPathResource("zscalar.cer");
            InputStream caInput = resource.getInputStream();
            Certificate ca = cf.generateCertificate(caInput);
            caInput.close();

            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, getWrappedTrustManagers(tmf.getTrustManagers()), null);

            return sslContext.getSocketFactory();
        } catch (Exception e) {
            return HttpsURLConnection.getDefaultSSLSocketFactory();
        }
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            originalTrustManager.checkClientTrusted(certs, authType);
                        } catch (CertificateException ignored) {
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            originalTrustManager.checkServerTrusted(certs, authType);
                        } catch (CertificateException ignored) {
                        }
                    }
                }
        };
    }
}
