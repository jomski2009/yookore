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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
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
    private static final String NOTIFICATION_KEY_PREFIX = "yookmobile_";

    @Autowired
    MongoClient client;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    Datastore ds;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;


    public void doPush(AndroidPushNotificationData data) {
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");

        NotificationResource resource = data.getNotificationResource();
        //log.info("Notification: {}", resource);

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
        //msg.setS("");
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
                if (registration_ids != null && !registration_ids.isEmpty()) {
                    pn.setRegistration_ids(registration_ids);

                    String pushObject = gsonObject.toJson(pn);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    headers.set("Authorization", GOOGLE_API_KEY);
                    //Add the project ID header here
                    log.info(pushObject);
                    try {
                        ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_URL, HttpMethod.POST, new HttpEntity<>(pushObject, headers), String.class);
                        log.info("Response from Google: {}", responseEntity.getBody());
                        log.info("Response Headers: {}", responseEntity.getHeaders().toString());
                        //We should do something with the data when 

                    } catch (HttpClientErrorException e) {
                        log.error("Error from Google: {}", e.getResponseBodyAsString());
                        continue;
                    }
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
        //log.info(">>>>>>> Author id for notification: {}", notification.getNotification().getContent().getAuthorId());

        long sender = notification.getNotification().getContent().getAuthorId();
        long recipient = notification.getNotification().getUserId();

        DBObject relationship = client.getDB("yookosreco").getCollection("relationships")
                .findOne(new BasicDBObject("actorid", sender)
                        .append("followerid", recipient)
                        .append("hasdevice", true));

        //We need to create a special case for 2017 (pastorchrislive) and 3791 (pastor tomisin) to receive notifications
        if (relationship != null && isNotBlocked(sender, recipient) && sender != recipient && hasRecipientEnabledPushNotifications(recipient)) {
            //log.info("Relationship: {}", relationship.toString());
            AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
            doPush(data);
        } else {
            if (sender == recipient && (sender == 2017 || sender == 3791)) {
                AndroidPushNotificationData data = new AndroidPushNotificationData(notification, notification.getNotification().getUserId());
                doPush(data);
            }
        }

    }

    /**
     * Function to check if a user has enabled notifications on the server (and by extension, the CORE)
     *
     * @param recipient
     * @return
     */
    private boolean hasRecipientEnabledPushNotifications(long recipient) {
        DBCollection blockedList = client.getDB("yookosreco").getCollection("blockedlists");
        DBObject result = blockedList.findOne(new BasicDBObject("userid", recipient));

        if (result == null) {
            log.info("Returning true. result was null");
            return true;
        } else {
            log.info("Checking value of result. result for recipient {} was found", recipient);
            return (boolean) result.get("notificationenabled");
        }
//        log.info("Checking notification enabled status for recipient id: {}", recipient);
//        try {
//            if (result.get("notificationenabled") != null) {
//                return (boolean) result.get("notificationenabled");
//            }
//
//            return true;
//        } catch (NullPointerException npe) {
//            log.error("A null pointer exception was caught.");
//            return true;
//        }

        //For now we will just be returning true as it seems that there is is a problem with the data 
        //validation for this method.
        //@Emile please investigate.

    }

    private boolean isNotBlocked(long sender, long recipient) {
        DBCollection blockedList = client.getDB("yookosreco").getCollection("blockedlists");

        List<Long> senderList = new ArrayList<>();
        senderList.add(sender);
        DBObject one = blockedList.findOne(new BasicDBObject("userid", recipient).append("blockedlist",
                new BasicDBObject("$in", senderList.toArray())));
        if (one != null) {
            //Sender is in the list
            return false;
        } else {
            return true;
        }
    }

    public String addOrUpdateDeviceRegistration(int userId, String regId) {
        Gson gson = new Gson();
        DBCollection devices = client.getDB("yookosreco").getCollection("androidusers");
        String notificationKeyName = NOTIFICATION_KEY_PREFIX + userId;
        String result = "";

        DBObject device = devices.findOne(new BasicDBObject("notification_key_name", notificationKeyName));

        if (device != null) {
            //Found an entry. Update regid array
            //log.info(device.toString());
            devices.update(device, new BasicDBObject("$addToSet", new BasicDBObject("registration_ids", regId)));
            updateHasDevice(userId, true);

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
            log.info("Google Response: {}", notificationKeyResponse);
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
                        .append("registration_ids", reg_ids));
                log.info(writeResult.toString());
                result = writeResult.toString();
                updateHasDevice(userId, true);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void updateHasDevice(int userId, boolean hasDevice) {
        DBCollection relationships = client.getDB("yookosreco").getCollection("relationships");
        WriteResult update = relationships.update(new BasicDBObject("userid", userId),
                new BasicDBObject("$set", new BasicDBObject("hasdevice", hasDevice)),
                false,
                true);

        log.info("Update results: {}", update.toString());
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

                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(GCM_KEY_URL, HttpMethod.POST, new HttpEntity<>(reqobj, headers), String.class);
                    String notificationKeyResponse = responseEntity.getBody();
                    //Convert to json to extract the notification key value

                    JSONObject key = (JSONObject) new JSONParser().parse(notificationKeyResponse);
                    String notificationKey = key.get("notification_key").toString();
                    List<String> reg_ids = new ArrayList<>();
                    reg_ids.add(reg.getGcm_regid());
                    WriteResult writeResult = devices.insert(new BasicDBObject("notification_key_name", notificationKeyName)
                            .append("notification_key", notificationKey)
                            .append("userid", reg.getUserid())
                            .append("type", "android")
                            .append("registration_ids", reg_ids));
                    log.info(writeResult.toString());

                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
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

    public void sendToChatServer(NotificationResource notification) {
        // Set the cmd value to 'store'
        SendToServer sendToServer = new SendToServer(notification);
        taskExecutor.execute(sendToServer);

    }

    public void addDeviceToUserRelationship() {
        DBCollection relationships = client.getDB("yookosreco").getCollection("relationships");
        DBCollection devices;
        if (!client.getDB("yookosreco").collectionExists("androidusers")) {
            client.getDB("yookosreco").createCollection("androidusers", null);
            devices = client.getDB("yookosreco").getCollection("androidusers");
        } else {
            devices = client.getDB("yookosreco").getCollection("androidusers");
        }

        log.info("Initiating adding devices");

        DBCursor deviceCursor = devices.find();

        for (DBObject device : deviceCursor) {
            if (device.get("userid").getClass() == Long.class) {
                long userid = (long) device.get("userid");
                updateHasDevice((int) userid, true);
            } else {
                int userid = (int) device.get("userid");
                updateHasDevice(userid, true);

            }
        }
    }

    class SendToServer implements Runnable {
        NotificationResource notification;

        public SendToServer(NotificationResource notification) {
            this.notification = notification;
        }

        @Override
        public void run() {
            notification.setCmd("store");
            Gson gson = new Gson();
            String json = gson.toJson(notification);

            HttpEntity postObject = new HttpEntity(json);

            long start = new Date().getTime();
            ResponseEntity<String> result = restTemplate.exchange(URL_JSON, HttpMethod.POST, postObject, String.class);
            long end = new Date().getTime();

            long duration = end - start;

            if (result != null) {
                log.info("Returned result from chat server: {}", result.getStatusCode());
                log.info("Operation took {} seconds", duration / 1000);
            }
        }
    }
}
