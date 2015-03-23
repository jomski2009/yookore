package com.yookos.yookore.config;

import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.rabbit.GroupNotificationReceiver;
import com.yookos.yookore.rabbit.NotificationReceiver;
import com.yookos.yookore.rabbit.PublicFigureNotificationReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RabbitMQConfig {
    public final static String notificationQueue = "push.notifications.all.durable";
    public final static String activityQueue = "activity.messages.durable";
    public final static String publicFigureNotificationQueue = "push.notifications.publicfigures.durable";
    public final static String groupNotificationQueue = "push.notifications.groups.durable";

    @Autowired
    Environment environment;

    @Bean
    ClassMapper classMapper() {
        DefaultClassMapper mapper = new DefaultClassMapper();
        mapper.setDefaultType(NotificationResource.class);
        return mapper;
    }

    @Bean
    JsonMessageConverter jsonMessageConverter() {
        JsonMessageConverter converter = new JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return new JsonMessageConverter();
    }

    @Bean
    CachingConnectionFactory connectionFactory() {
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
        return new Queue(notificationQueue, true);
    }

    @Bean
    Queue activityQueue() {
        return new Queue(activityQueue, true);
    }

    @Bean
    Queue publicFigureQueue() {
        return new Queue(publicFigureNotificationQueue, true);
    }

    @Bean
    Queue groupNotificationQueue() {
        return new Queue(groupNotificationQueue, true);
    }

    @Bean
    NotificationReceiver notificationReceiver() {
        return new NotificationReceiver();
    }

    @Bean
    GroupNotificationReceiver groupNotificationReceiver() {
        return new GroupNotificationReceiver();
    }

    @Bean
    PublicFigureNotificationReceiver publicFigureNotificationReceiver() {
        return new PublicFigureNotificationReceiver();
    }


    @Bean
    MessageListenerAdapter notificationsListenerAdapter() {
        //return new MessageListenerAdapter(notificationReceiver(), jsonMessageConverter());
        return new MessageListenerAdapter(notificationReceiver());

    }

    @Bean
    MessageListenerAdapter publicFigureNotificationsListenerAdapter() {
        return new MessageListenerAdapter(publicFigureNotificationReceiver(), jsonMessageConverter());
    }

    @Bean
    MessageListenerAdapter groupNotificationsListenerAdapter() {
        return new MessageListenerAdapter(groupNotificationQueue(), jsonMessageConverter());
    }


    @Bean
    Binding topicBinding() {
        return BindingBuilder.bind(notificationsQueue()).to(topicExchange()).with(notificationQueue);
    }

    @Bean
    Binding pfTopicBinding() {
        return BindingBuilder.bind(publicFigureQueue()).to(topicExchange()).with(publicFigureNotificationQueue);
    }

    @Bean
    SimpleMessageListenerContainer notificationContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(notificationQueue);
        container.setMessageListener(notificationsListenerAdapter());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer publicFigureNotificationContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(publicFigureNotificationQueue);
        container.setMessageListener(publicFigureNotificationsListenerAdapter());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;

    }

    @Bean
    SimpleMessageListenerContainer groupNotificationContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(groupNotificationQueue);
        container.setMessageListener(groupNotificationsListenerAdapter());
        return container;

    }


}