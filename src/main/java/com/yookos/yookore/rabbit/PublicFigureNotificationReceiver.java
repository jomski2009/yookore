package com.yookos.yookore.rabbit;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * This class deals with handling notifications from Public Figure Actions
 * Created by jome on 2014/08/28.
 */
public class PublicFigureNotificationReceiver {
    @Autowired
    Environment environment;

    @Autowired
    MongoClient client;

    @Autowired
    PushNotificationHelper helper;

    public void handleMessage(NotificationResource notification) {
        if (!environment.containsProperty("can.receive.message") && environment.getProperty("can.receive.message").equals("false")) {
            //Throw an exception here so the message can be requeued
        }

        DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
                .find(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId())
                        .append("hasdevice", true));

        for (DBObject obj : cursor) {
            int followerid = (int) obj.get("followerid");
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, followerid);
            helper.doPush(data);
        }
    }
}
