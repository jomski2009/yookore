package com.yookos.yookore.rabbit;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jome on 2014/08/27.
 */

public class NotificationReceiver {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PushNotificationHelper helper;

    @Autowired
    MongoClient client;
    @Autowired
    Environment environment;


    public void handleMessage(NotificationResource notification) {
        if (!environment.containsProperty("can.receive.message") && environment.getProperty("can.receive.message").equals("false")) {
            //Throw an exception here so the message can be requeued
        }
        if (notification.getNotification().getContent().getObjectType().equals("action")) {
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            helper.doPush(data);
        } else {
            //This will tag on the userid to the notification object and then execute doPush
            helper.processNotifications(notification);
        }
    }

    public void handleMessage(byte[] bytes){
        Gson gson = new Gson();
        NotificationResource notification = gson.fromJson(new String(bytes), NotificationResource.class);

        if (notification.getNotification().getContent().getObjectType().equals("action")) {
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            helper.doPush(data);
        } else {
            //This will tag on the userid to the notification object and then execute doPush
            helper.processNotifications(notification);
        }
    }
}
