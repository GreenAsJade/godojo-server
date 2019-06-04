package com.greenasjade.godojo;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


@Component
public class PortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Autowired
    private Environment env;

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {

        String envPort = env.getProperty("GODOJO_PORT");

        if (envPort != null) {
            server.setPort(Integer.valueOf(envPort));
        }
    }

}