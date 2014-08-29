package com.yookos.yookore.rabbit;

import com.yookos.yookore.domain.notification.NotificationResource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jome on 2014/08/27.
 */

@Component
public class NotificationSender {
    @Autowired
    RabbitTemplate template;

    public void sendNotification(NotificationResource notification, String queueName){
        template.setExchange("yookos.notifications");
        template.convertAndSend(queueName, notification);
    }
}
