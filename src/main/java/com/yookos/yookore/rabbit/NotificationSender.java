package com.yookos.yookore.rabbit;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.yookos.yookore.domain.notification.NotificationResource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jome on 2014/08/27.
 */

@Component
public class NotificationSender {
    @Autowired
    RabbitTemplate template;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MongoClient client;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    public void sendNotification(NotificationResource notification, String queueName) {
//        template.setExchange("yookos.notifications");
//        template.convertAndSend(queueName, notification);
        taskExecutor.execute(new SendToQueue(notification, queueName));
        //taskExecutor.execute(new ProcessNotificationSave(notification));

    }
    
    

    //This class is to counter the effect of sending username instead of displayName by Emile.
    class SendToQueue implements Runnable {
        NotificationResource resource;
        String queueName;

        public SendToQueue(NotificationResource resource, String queueName) {
            this.resource = resource;
            this.queueName = queueName;

        }

        @Override
        public void run() {
            //We need to fetch the displayName
            resource = fixDisplayName(resource);

            template.setExchange("yookos.notifications");
            if (!resource.getNotification().getContent().getObjectType().equals("discussion")){
                log.info("Sending notification resource: {}", resource.getNotification());
                template.convertAndSend(queueName, resource);
            }else{
                //log.info("Dropping discussion object type: {}", resource.getNotification().getContent());
            }
        }

        private NotificationResource fixDisplayName(NotificationResource resource) {
            long authorId = resource.getNotification().getContent().getAuthorId();
            String entity = restTemplate.getForEntity("https://www.yookos.com/api/core/v3/people/" + authorId, String.class).getBody();
            //Need to remove the throws stuff on top
            String newEntity = entity.replace("throw 'allowIllegalResourceCall is false.';", "");

            JSONParser parser = new JSONParser();
            try {
                JSONObject json = (JSONObject) parser.parse(newEntity);
                String displayName = (String)json.get("displayName");
                resource.getNotification().getContent().setSenderDisplayName(displayName);
                return resource;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return resource;
        }
    }

    class ProcessNotificationSave implements Runnable {
        NotificationResource notification;

        public ProcessNotificationSave(NotificationResource resource) {
            this.notification = resource;
        }

        @Override
        public void run() {
            DBCollection processednotifications = client.getDB("yookosreco").getCollection("processednotifications");

            try {
                processednotifications.insert(new BasicDBObject("userid", notification.getNotification().getContent().getAuthorId())
                        .append("objectid", notification.getNotification().getContent().getObjectId())
                        .append("processed", false));
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }
}
