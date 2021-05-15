package com.neat.proxy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@RestController
public class RelayController {
    @RequestMapping(path = "**", method = {RequestMethod.GET, RequestMethod.POST})
    public void receive(HttpServletRequest req, HttpServletResponse rsp) {
        forward(req, rsp);
    }

    public void forward(HttpServletRequest req, HttpServletResponse rsp) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = req.getHeader(key);
            System.out.println(key + ": " + value);
        }
    }
}
