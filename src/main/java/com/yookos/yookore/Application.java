package com.yookos.yookore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

/**
 * Created by jome on 2014/08/27.
 */
@ComponentScan
@EnableAutoConfiguration
@PropertySources(value = {@PropertySource(value = {"file:/application.properties"})})
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}
