package com.yookos.yookore.rabbit;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.Notification;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * This class deals with handling notifications from Public Figure Actions
 * Created by jome on 2014/08/28.
 */
public class PublicFigureNotificationReceiver implements ChannelAwareMessageListener {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Environment environment;

    @Autowired
    MongoClient client;

    @Autowired
    PushNotificationHelper helper;

//    public void handleMessage(NotificationResource notification) {
//        log.info("Handling Public figure notification: {}", notification.getNotification());
//
//        DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
//                .find(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId())
//                        .append("hasdevice", true));
//
//        for (DBObject obj : cursor) {
//            int followerid = (int) obj.get("followerid");
//            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, followerid);
//            helper.doPush(data);
//        }
//    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        Gson gson = new Gson();
        NotificationResource notification = gson.fromJson(new String(message.getBody()), NotificationResource.class);

        log.info("Handling Public figure notification: {}", notification.getNotification());

        DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
                .find(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId())
                        .append("hasdevice", true));

        for (DBObject obj : cursor) {
            int followerid = (int) obj.get("followerid");
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, followerid);
            helper.doPush(data);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
