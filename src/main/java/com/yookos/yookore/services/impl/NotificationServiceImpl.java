package com.yookos.yookore.services.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import com.yookos.yookore.rabbit.NotificationSender;
import com.yookos.yookore.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of NotificationService contract.
 * <p/>
 * Created by jome on 2014/08/27.
 */
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    NotificationSender sender;

    @Autowired
    PushNotificationHelper helper;

    @Autowired
    MongoClient client;

    @Override
    public void sendNotification(NotificationResource notificationResource) {
        //Queue up the message with the AMQP Broker (RabbitMQ in this case)
        sender.sendNotification(notificationResource);
    }

    @Override
    public void sendPublicFigureNotification(NotificationResource nr) {
        DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
                .find(new BasicDBObject("actorid", nr.getNotification().getContent().getAuthorId())
                        .append("hasdevice", true)).sort(new BasicDBObject("followerid", 1));

        for (DBObject obj : cursor) {
            int followerid = (int) obj.get("followerid");
            AndroidPushNotificationData data = new AndroidPushNotificationData(nr, followerid);
            helper.doPush(data);
        }
    }
}
