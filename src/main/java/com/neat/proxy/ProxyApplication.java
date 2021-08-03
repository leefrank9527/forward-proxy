package com.neat.proxy;

import com.neat.util.MultiThreadsPrint;
import com.neat.util.TraceRouteUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.Arrays;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ProxyApplication implements CommandLineRunner {
    private static final String PRINTLN_SEPARATOR_LINE = "^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^";

    @Value("${proxy.this.port}")
    private int thisProxyPort;

    @Value("${proxy.this.ignore}")
    private String thisProxyIgnore;

    @Value("${proxy.origin.host}")
    private String originProxyHost;

    @Value("${proxy.origin.port}")
    private int originProxyPort;

    @Value("${proxy.origin.addr}")
    private String originProxyAddress;

    @Value("${proxy.domestic.host}")
    private String domesticProxyHost;

    @Value("${proxy.domestic.port}")
    private int domesticProxyPort;

    @Value("${proxy.domestic.addr}")
    private String domesticProxyAddress;


    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(args).forEach(System.out::println);

        if (args.length > 0) {
            thisProxyIgnore = args[0];
        }

        TraceRouteUtil.setIgnoredHosts(thisProxyIgnore);
        TraceRouteUtil.setLocalProxyHost(domesticProxyHost);
        TraceRouteUtil.setLocalProxyPort(domesticProxyPort);
        TraceRouteUtil.setRemoteProxyHost(originProxyHost);
        TraceRouteUtil.setRemoteProxyPort(originProxyPort);
        TraceRouteUtil.init();

        ProxyProcessor.setOriginProxyHost(originProxyHost);
        ProxyProcessor.setOriginProxyPort(originProxyPort);
        ProxyProcessor.setOriginProxyAddress(originProxyAddress);
        ProxyProcessor.setDomesticProxyHost(domesticProxyHost);
        ProxyProcessor.setDomesticProxyPort(domesticProxyPort);
        ProxyProcessor.setDomesticProxyAddress(domesticProxyAddress);

        ProxyDaemon daemon = new ProxyDaemon(thisProxyPort);
        Thread proxy = new Thread(daemon);
        proxy.start();

        printGreetingMessage();

        MultiThreadsPrint.init();

        proxy.join();
    }

    private void printGreetingMessage() {
        String printInfo = String.format("  Proxy Initialed, PORT:%d  ", thisProxyPort);
        int prefixLength = (PRINTLN_SEPARATOR_LINE.length() - printInfo.length()) / 2;
//        String prefixInfo = "-".repeat(prefixLength);
        String prefixInfo = "";
        System.out.println(PRINTLN_SEPARATOR_LINE);
        System.out.println(prefixInfo + printInfo + prefixInfo);
        System.out.println(PRINTLN_SEPARATOR_LINE);
    }
}
