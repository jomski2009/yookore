package com.yookos.yookore.config;

import com.google.gson.Gson;
import org.json.simple.parser.JSONParser;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.MultipartConfigElement;

/**
 * Created by jome on 2014/08/27.
 */

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    /**
     * Enable support for multi-part form operations
     * @return configured MultipartConfigElement
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Bean
    Gson gson() {
        return new Gson();
    }

    @Bean
    JSONParser jsonParser(){
        return new JSONParser();
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor te = new  ThreadPoolTaskExecutor();
        te.setCorePoolSize(10);
        te.setMaxPoolSize(20);

        return te;
    }
}
