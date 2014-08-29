package com.yookos.yookore.helpers;

import com.google.gson.Gson;
import com.mongodb.*;
import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.domain.notification.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jome on 2014/08/28.
 */

@Component
public class PushNotificationHelper {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String URL_JSON = "http://chat.yookos.com/mobileservices/push/notifications.php";
    private final static String GCM_URL = "https://android.googleapis.com/gcm/send";
    private final static String GCM_KEY_URL = "https://android.googleapis.com/gcm/notification";
    private static final String GOOGLE_API_KEY = "key=AIzaSyClDqYSytbUpuCJYq6JMMRrfLcVJbuiPPY";
    private static final String GOOGLE_PROJECT_ID = "355368739731";
    private static final String NOTIFICATION_KEY_PREFIX= "ymAnd_";

    @Autowired
    MongoClient client;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    Datastore ds;


    public void doPush(AndroidPushNotificationData data){
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");

        NotificationResource resource = data.getNotificationResource();

        NotificationContent content = data.getNotificationResource().getNotification().getContent();
        Notification notification = new Notification();
        notification.setUserId(data.getUserid());

        notification.setContent(content);
        resource.setNotification(notification);

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
                    pn.setRegistration_ids(registration_ids);

                    String pushObject = gsonObject.toJson(pn);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    headers.set("Authorization", GOOGLE_API_KEY);
                    //Add the project ID header here
                    ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_URL, HttpMethod.POST, new HttpEntity<>(pushObject, headers), String.class);
                    log.info(responseEntity.getBody());
                }
            }
        } else {
            log.info("No valid recipients found");
        }
    }

    /**
     * Checks if the recipient has an android device which is determined by
     * 'hasdevice' with values of true or false.
     * If true, then we execute doPush
     *
     * @param notification
     */
    public void processNotifications(NotificationResource notification) {
        log.info(">>>>>>> Author id for notification: {}", notification.getNotification().getContent().getAuthorId());

        DBObject relationship = client.getDB("yookosreco").getCollection("relationships")
                .findOne(new BasicDBObject("actorid", notification.getNotification().getContent().getAuthorId())
                        .append("followerid", notification.getNotification().getUserId())
                        .append("hasdevice", true));

        if (relationship != null) {
            //log.info("Relationship: {}", relationship.toString());
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            doPush(data);
        }

    }

    public String addOrUpdateDeviceRegistration(int userId, String regId){
        Gson gson = new Gson();
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");
        String notificationKeyName = NOTIFICATION_KEY_PREFIX + userId;
        String result = "";

        DBObject device = devices.findOne(new BasicDBObject("notification_key_name", notificationKeyName));

        if (device != null) {
            //Found an entry. Update regid array
            log.info(device.toString());
            devices.update(device, new BasicDBObject("$addToSet", new BasicDBObject("registration_ids", regId)));

        } else {
            //No entry found. Create new

            //Build the request object
            NotificationKeyRequest request = new NotificationKeyRequest();
            request.setNotification_key_name(notificationKeyName);
            request.getRegistration_ids().add(regId);
            request.setOperation("create");
            String reqobj = gson.toJson(request);

            //First obtain a new notification_key
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.set("Authorization", GOOGLE_API_KEY);
            headers.set("project_id", GOOGLE_PROJECT_ID);

            ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_KEY_URL, HttpMethod.POST, new HttpEntity<>(reqobj, headers), String.class);
            String notificationKeyResponse = responseEntity.getBody();
            //Convert to json to extract the notification key value
            try {
                JSONObject key = (JSONObject) new JSONParser().parse(notificationKeyResponse);
                String notificationKey = key.get("notification_key").toString();
                List<String> reg_ids = new ArrayList<>();
                reg_ids.add(regId);
                WriteResult writeResult = devices.insert(new BasicDBObject("notification_key_name", notificationKeyName)
                        .append("notification_key", notificationKey)
                        .append("userid", userId)
                        .append("type", "android")
                        .append("registration_ids", reg_ids ));
                log.info(writeResult.toString());
                result = writeResult.toString();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public String removeDeviceRegistration(String regId, int userId) {
        //Remind Amu to send through the userid here as well.
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");
        WriteResult result = devices.update(new BasicDBObject("userid", userId), new BasicDBObject("$pull", new BasicDBObject("registration_ids", regId)), false, true);

        //WriteResult result = devices.remove(new BasicDBObject("gcm_regid", regId));
        log.info("Reg removal result: {}", result.toString());
        return result.toString();
    }

    public void addDeviceToUserRelationship(List<AndroidDeviceRegistration> rows) {
        DBCollection relationships = client.getDB("yookosreco").getCollection("relationships");
        DBCollection devices;
        if (!client.getDB("yookosreco").collectionExists("androidusers")) {
            client.getDB("yookosreco").createCollection("androidusers", null);
            devices = client.getDB("yookosreco").getCollection("androidusers");
        } else {
            devices = client.getDB("yookosreco").getCollection("androidusers");
        }

        log.info("Initiating adding devices");

        for (AndroidDeviceRegistration reg : rows) {
            WriteResult result = relationships.update(new BasicDBObject("followerid", reg.getUserid()), new BasicDBObject("$set", new BasicDBObject("hasdevice", true)), false, true);
            log.info(result.toString());
            //1. Check if the current userid already has a notification key entry
            //2. If there is an entry, add the reg id to the reg ids associated with the notification key
            //3. If there is no associated key, generate a new notification_key_name (ya_userid, guaranteed to be unique)
            //4. Retrieve a new notification key and store the data in mongo.
            String notificationKeyName = NOTIFICATION_KEY_PREFIX + reg.getUserid();
            DBObject device = devices.findOne(new BasicDBObject("notification_key_name", notificationKeyName));

            if (device != null) {
                //Found an entry. Update regid array
                log.info(device.toString());
                devices.update(device, new BasicDBObject("$addToSet", new BasicDBObject("registration_ids", reg.getGcm_regid())));

            } else {
                //No entry found. Create new

                //Build the request object
                NotificationKeyRequest request = new NotificationKeyRequest();
                request.setNotification_key_name(notificationKeyName);
                request.getRegistration_ids().add(reg.getGcm_regid());
                request.setOperation("create");
                String reqobj = new Gson().toJson(request);

                //First obtain a new notification_key
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.set("Authorization", GOOGLE_API_KEY);
                headers.set("project_id", GOOGLE_PROJECT_ID);

                ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_KEY_URL, HttpMethod.POST, new HttpEntity<>(reqobj, headers), String.class);
                String notificationKeyResponse = responseEntity.getBody();
                //Convert to json to extract the notification key value
                try {
                    JSONObject key = (JSONObject) new JSONParser().parse(notificationKeyResponse);
                    String notificationKey = key.get("notification_key").toString();
                    List<String> reg_ids = new ArrayList<>();
                    reg_ids.add(reg.getGcm_regid());
                    WriteResult writeResult = devices.insert(new BasicDBObject("notification_key_name", notificationKeyName)
                            .append("notification_key", notificationKey)
                            .append("userid", reg.getUserid())
                            .append("type", "android")
                            .append("registration_ids", reg_ids ));
                    log.info(writeResult.toString());

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void addBatchRelationships(List<UserRelationship> userRelationships) {
        DBCollection relationships = client.getDB("yookosreco").getCollection("relationships");

        log.info("Documents in collection: {}", relationships.count());
        for (UserRelationship rel : userRelationships) {
            try {
                //Check if the inverse relationship already exists
                DBObject object = relationships.findOne(new BasicDBObject("followerid", rel.getActorid()).append("actorid", rel.getFollowerid()));
                log.info("found object: {}", object);
                if (object != null) {
                    //Modify the db object and save it, setting type as 1 (friend)
                    log.info("Updating : {}", object);

                    relationships.update(object, new BasicDBObject("$set", new BasicDBObject("type", 1)));
                    //relationships.update(object, new BasicDBObject("type", 1));
                    rel.setRelationshipType(1);
                    ds.save(rel);
                } else {
                    log.info("Saving : {}", rel);
                    ds.save(rel);
                }

            } catch (Exception e) {
                log.info("Caught Exception: {}", e.getMessage());
            }
        }

    }
}
