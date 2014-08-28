package com.yookos.yookore.helpers;

import com.google.gson.Gson;
import com.mongodb.*;
import com.yookos.yookore.domain.notification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Created by jome on 2014/08/28.
 */

@Component
public class PushNotificationHelper {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String URL_JSON = "http://chat.yookos.com/mobileservices/push/notifications.php";
    private final static String GCM_URL = "https://android.googleapis.com/gcm/send";
    private static final String GOOGLE_API_KEY = "key=AIzaSyClDqYSytbUpuCJYq6JMMRrfLcVJbuiPPY";
    private static final String GOOGLE_PROJECT_ID = "355368739731";

    @Autowired
    MongoClient client;

    @Autowired
    RestTemplate restTemplate;


    public void doPush(AndroidPushNotificationData data){
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");

        NotificationResource resource = data.getNotificationResource();

        NotificationContent content = data.getNotificationResource().getNotification().getContent();
        Notification notification = new Notification();
        notification.setUserId(data.getUserid());

        notification.setContent(content);
        resource.setNotification(notification);

        //resource.getNotification().setUserId(userid);
        Gson gsonObject = new Gson();
        AndroidPushNotification pn = new AndroidPushNotification();
        PushMessageWrapper pd = new PushMessageWrapper();
        PushMessage msg = new PushMessage();

        msg.setM(resource.getNotification().getContent().getAlertMessage());
        msg.setOi(resource.getNotification().getContent().getObjectId());
        if (resource.getNotification().getContent().getObjectType().trim().equals("post")) {
            msg.setOt("blogpost");
        } else {
            msg.setOt(resource.getNotification().getContent().getObjectType());
        }
        msg.setS(resource.getNotification().getContent().getSenderDisplayName());
        msg.setSi(resource.getNotification().getContent().getAuthorId());
        msg.setU(resource.getNotification().getUserId());


        pd.setMsg(msg);
        pn.setData(pd);

        DBCursor reg = devices.find(new BasicDBObject("userid", msg.getU()));

        if (reg != null) {
            for (DBObject o : reg) {
                String notification_key = (String) o.get("notification_key");
                String notification_key_name = (String) o.get("notification_key_name");
                pn.setNotification_key_name(notification_key_name);
                pn.setNotification_key(notification_key);

                ArrayList<String> registration_ids = (ArrayList<String>) o.get("registration_ids");
                if (registration_ids != null) {
//                System.out.println("Registration id: " + regid);
                    pn.setRegistration_ids(registration_ids);

                    //log.info("Built up resource for {} is {}", t.getData().getUserid(), resource.toString());
                    //String requestObject = gsonObject.toJson(resource);
                    String pushObject = gsonObject.toJson(pn);

                    //log.info(userid + " is the passed id for " + requestObject);
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    headers.set("Authorization", GOOGLE_API_KEY);
                    //Add the project ID header here

                    //log.info(pushObject);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_URL, HttpMethod.POST, new HttpEntity<>(pushObject, headers), String.class);
                    log.info(responseEntity.getBody());
                }
            }
        } else {
            log.info("No valid recipients found");
        }
    }

    public void processNotifications(NotificationResource notification) {
        log.info(">>>>>>> Author id for notification: {}", notification.getNotification().getContent().getAuthorId());

        DBObject relationship = client.getDB("yookosreco").getCollection("relationships")
                .findOne(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId())
                        .append("followerid", notification.getNotification().getUserId())
                        .append("hasdevice", true));

        if (relationship != null) {
            log.info("Relationship: {}", relationship.toString());
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            doPush(data);
        }

    }

}
