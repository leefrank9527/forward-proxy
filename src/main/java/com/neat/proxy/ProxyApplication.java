package com.neat.proxy;

import com.neat.util.MultiThreadsPrint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = {"com.neat.proxy.controller"})
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ProxyApplication {
    private static final String PRINTLN_SEPARATOR_LINE = "^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^";

    @Value("${proxy.this.port}")
    private int thisProxyPort;

    @Value("${proxy.this.ignore}")
    private String thisProxyIgnore;

    @Value("${proxy.origin.host}")
    private String originProxyHost;

    @Value("${proxy.origin.port}")
    private int originProxyPort;

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            // Note that this is just here for debugging purposes. It can be deleted at any time.
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        };
    }

//    @Override
//    public void run(String... args) throws Exception {
//        Arrays.stream(args).forEach(System.out::println);
//
//        if (args.length > 0) {
//            thisProxyIgnore = args[0];
//        }
//
//        ProxyProcessor.setIgnoredHosts(thisProxyIgnore);
//        ProxyProcessor.setProxyHost(originProxyHost);
//        ProxyProcessor.setProxyPort(originProxyPort);
//
//        ProxyDaemon daemon = new ProxyDaemon(thisProxyPort);
//        Thread proxy = new Thread(daemon);
//        proxy.start();
//
//        printGreetingMessage();
//
//        MultiThreadsPrint.init();
//
//        proxy.join();
//    }
//
//    private void printGreetingMessage() {
//        String printInfo = String.format("  Proxy Initialed, PORT:%d  ", thisProxyPort);
//        int prefixLength = (PRINTLN_SEPARATOR_LINE.length() - printInfo.length()) / 2;
////        String prefixInfo = "-".repeat(prefixLength);
//        String prefixInfo = "";
//        System.out.println(PRINTLN_SEPARATOR_LINE);
//        System.out.println(prefixInfo + printInfo + prefixInfo);
//        System.out.println(PRINTLN_SEPARATOR_LINE);
//    }
}
