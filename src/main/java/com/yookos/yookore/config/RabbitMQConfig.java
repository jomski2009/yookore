package com.yookos.yookore.config;

import com.yookos.yookore.rabbit.NotificationReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RabbitMQConfig {
    public final static String notificationQueue = "push.notifications";
    public final static String activityQueue = "activity.messages";

    @Autowired
    Environment environment;

    @Bean
    JsonMessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    CachingConnectionFactory connectionFactory(){
        CachingConnectionFactory factory =
                new CachingConnectionFactory(environment.getProperty("yookos.rabbitmq.host", "queue.yookos.com"));
        factory.setUsername("jomski");
        factory.setPassword("wordpass15");
        factory.setChannelCacheSize(20);
        factory.setVirtualHost(environment.getProperty("yookos.rabbitmq.vhost", "notifications"));

        return factory;
    }

    @Bean
    RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("yookos.activities");
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("yookos.notifications");
    }

    @Bean
    Queue notificationsQueue() {
        return new Queue(notificationQueue, false);
    }

    @Bean
    Queue activityQueue() {
        return new Queue(activityQueue, false);
    }

    @Bean
    NotificationReceiver notificationReceiver() {
        return new NotificationReceiver();
    }

    @Bean
    MessageListenerAdapter notificationsListenerAdapter() {
        return new MessageListenerAdapter(notificationReceiver(),jsonMessageConverter());
    }

    @Bean
    Binding topicBinding() {
        return BindingBuilder.bind(notificationsQueue()).to(topicExchange()).with(notificationQueue);
    }

    @Bean
    SimpleMessageListenerContainer notificationContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(notificationQueue);
        container.setMessageListener(notificationsListenerAdapter());
        return container;
    }


}