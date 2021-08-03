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

    @Value("${Listen}")
    private int listenPort;

    @Value("${NoProxy}")
    private String ignoreHosts;

    @Value("${ProxyServers}")
    private String[] proxyServers;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(args).forEach(System.out::println);

        if (args.length > 0) {
            ignoreHosts = args[0];
        }

        TraceRouteUtil.setIgnoredHosts(ignoreHosts);
        TraceRouteUtil.setProxyServers(proxyServers);
        //TraceRouteUtil.init();

        ProxyDaemon daemon = new ProxyDaemon(listenPort);
        Thread proxy = new Thread(daemon);
        proxy.start();

        printGreetingMessage();

        MultiThreadsPrint.init();

        proxy.join();
    }

    private void printGreetingMessage() {
        String printInfo = String.format("  Proxy Initialed, PORT:%d  ", listenPort);
        int prefixLength = (PRINTLN_SEPARATOR_LINE.length() - printInfo.length()) / 2;
//        String prefixInfo = "-".repeat(prefixLength);
        String prefixInfo = "";
        System.out.println(PRINTLN_SEPARATOR_LINE);
        System.out.println(prefixInfo + printInfo + prefixInfo);
        System.out.println(PRINTLN_SEPARATOR_LINE);
    }
}
