package com.yookos.yookore.rabbit;

import com.google.gson.Gson;
import com.mongodb.*;
import com.yookos.yookore.domain.notification.AndroidPushNotificationData;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    DB yookosdb;


    public void handleMessage(NotificationResource notification) {
        if (!environment.containsProperty("can.receive.message") && environment.getProperty("can.receive.message").equals("false")) {
            //Throw an exception here so the message can be requeued
        }

        //Is this notification from a global blocked sender?
        if (authorIsGloballyBlocked(notification.getNotification().getContent().getAuthorId())) {
            return;
        }

        //Has this notification already been processed?
//        if (alreadyProcessed(notification)) {
//            return;
//        }

        if (notification.getNotification().getContent().getObjectType().equals("action")) {
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            helper.doPush(data);
        } else {
            //This will tag on the userid to the notification object and then execute doPush
            helper.processNotifications(notification);
        }
    }

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

    public void handleMessage(byte[] bytes) {
        if (environment.containsProperty("can.receive.message") && environment.getProperty("can.receive.message").equals("false")) {
            return;
        }
        Gson gson = new Gson();
        NotificationResource notification = gson.fromJson(new String(bytes), NotificationResource.class);

        log.info("Received notification: {}", notification);

        if (notification.getCmd() == null) {
            return;
        }

        //Is this notification from a global blocked sender?
        if (authorIsGloballyBlocked(notification.getNotification().getContent().getAuthorId())) {
            return;
        }

        //Has this notification already been processed?
//        if (alreadyProcessed(notification)) {
//            return;
//        }

        if (notification.getNotification().getContent().getObjectType().equals("action") || notification.getNotification().getContent().getObjectType().equals("directmessage")) {
            // Check if the recipient is on the android device list
            if(!recipientInDeviceList(notification.getNotification().getUserId())){
                helper.sendToChatServer(notification);
            }else{
                AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
                helper.doPush(data);
            }
        } else {
            //This will tag on the userid to the notification object and then execute doPush
            DBCursor cursor = client.getDB("yookosreco").getCollection("relationships")
                    .find(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId()));

            for (DBObject obj : cursor) {
                int followerid = (Integer) obj.get("followerid");
                if ((boolean)obj.get("hasdevice")){
                    notification.getNotification().setUserId(followerid);
                    helper.processNotifications(notification);
                }else{
                    //Send to chat server. First set cmd to store. Turned off until we sort out chat server.
                    //notification.getNotification().setUserId(followerid);
                    //helper.sendToChatServer(notification);
                }
            }
        }

        long userid = notification.getNotification().getContent().getAuthorId();
        long objectid = notification.getNotification().getContent().getObjectId();


        WriteResult update = client.getDB("yookosreco").getCollection("processednotifications")
                .update(new BasicDBObject("userid", userid).append("objectid", objectid), new BasicDBObject("$set", new BasicDBObject("processed", true)));
        //log.info(update.toString());
    }

    private boolean recipientInDeviceList(long userId) {
        DBCollection deviceOwners = client.getDB("yookosreco").getCollection("androidusers");
        DBObject user = deviceOwners.findOne(new BasicDBObject("userid", userId));

        if(user != null){
            return true;
        }
        return false;
    }
}
