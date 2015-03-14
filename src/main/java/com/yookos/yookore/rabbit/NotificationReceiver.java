package com.yookos.yookore.rabbit;

import com.google.gson.Gson;
import com.mongodb.*;
import com.rabbitmq.client.Channel;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jome on 2014/08/27.
 */

public class NotificationReceiver implements ChannelAwareMessageListener {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PushNotificationHelper helper;

    @Autowired
    MongoClient client;
    @Autowired
    Environment environment;

    @Autowired
    DB yookosdb;


    private boolean authorIsGloballyBlocked(long authorId) {
        List<Long> id = new ArrayList<Long>();
        id.add(authorId);

        DBCollection gbl = yookosdb.getCollection("globalblock");

        DBObject object = gbl.findOne(new BasicDBObject("blockid", 1).append("list", new BasicDBObject("$in", id.toArray())));
        if (object != null) {
            return true;
        }

        return false;
    }

    private boolean alreadyProcessed(NotificationResource notification) {
        long userid = notification.getNotification().getContent().getAuthorId();
        long objectid = notification.getNotification().getContent().getObjectId();
        DBCollection pn = client.getDB("yookosreco").getCollection("processednotifications");

        DBObject one = pn.findOne(new BasicDBObject("userid", userid).append("objectid", objectid));
        Boolean processed = (Boolean) one.get("processed");

        if (one != null && processed == false) {
            return false;
        }

        if (one != null && processed == true) {
            return true;
        }

        return false;
    }

    private boolean recipientInDeviceList(long userId) {
        DBCollection deviceOwners = client.getDB("yookosreco").getCollection("androidusers");
        DBObject user = deviceOwners.findOne(new BasicDBObject("userid", userId));

        if (user != null) {
            return true;
        }
        return false;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        boolean success = false;

        if (environment.containsProperty("can.receive.message") && environment.getProperty("can.receive.message").equals("false")) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            //log.info("Not handling received message: {}", message.getBody().toString());
            return;
        }

        Gson gson = new Gson();
        NotificationResource notification = gson.fromJson(new String(message.getBody()), NotificationResource.class);

        log.info("About to process notification: {}", notification);

        if (notification.getCmd() == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //Is this notification from a global blocked sender?
        if (authorIsGloballyBlocked(notification.getNotification().getContent().getAuthorId())) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //Has this notification already been processed?
//        if (alreadyProcessed(notification)) {
//            return;
//        }

        try {
            if (notification.getNotification().getContent().getObjectType().equals("action") || notification.getNotification().getContent().getObjectType().equals("directmessage")) {
                // Check if the recipient is on the android device list
                if (recipientInDeviceList(notification.getNotification().getUserId())) {
                    AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
                    helper.doPush(data);
                    //helper.sendToChatServer(notification);
                }
//            else {
//                helper.sendToChatServer(notification);
//            }
            } else {
                //This will tag on the userid to the notification object and then execute doPush
                DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
                        .find(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId()));

                for (DBObject obj : cursor) {
                    int followerid = (Integer) obj.get("followerid");

                    if ((boolean) obj.get("hasdevice")) {
                        notification.getNotification().setUserId(followerid);
                        try{
                            helper.processNotifications(notification);                            
                        }catch (Exception e){
                            e.printStackTrace();
                            log.error("Caught Exception cause is: {}", e.getMessage());
                            continue;
                        }
                        //helper.sendToChatServer(notification);
                    } else {
                        //Send to chat server. First set cmd to store. Turned off until we sort out chat server.
                        notification.getNotification().setUserId(followerid);
                        //helper.sendToChatServer(notification);
                    }
                }
            }

            long userid = notification.getNotification().getContent().getAuthorId();
            long objectid = notification.getNotification().getContent().getObjectId();


            WriteResult update = client.getDB("yookosreco").getCollection("processednotifications")
                    .update(new BasicDBObject("userid", userid).append("objectid", objectid), new BasicDBObject("$set", new BasicDBObject("processed", true)));
            //log.info(update.toString());

            log.info("Message received is: {}", notification.toString());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            //Something failed in the process, return the message back to the queue
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            log.info("Something failed");
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            log.error(e.getCause().getMessage());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

        }
    }
}
