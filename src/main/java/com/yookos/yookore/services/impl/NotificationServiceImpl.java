package com.yookos.yookore.services.impl;

import com.mongodb.*;
import com.yookos.yookore.config.RabbitMQConfig;
import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.CoreUserBlock;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import com.yookos.yookore.rabbit.NotificationSender;
import com.yookos.yookore.services.NotificationService;
import org.mongodb.morphia.Datastore;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of NotificationService contract.
 * <p/>
 * Created by jome on 2014/08/27.
 */

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final String BLOCKED_LIST_URL = "https://www.yookos.com/api/core/v3/people/preferences/admin/block/";
    private static final String ADMIN_AUTH = "Basic Y2VjaHVyY2gtYWRtaW46SnVsaWV0QDcwNzI=";
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NotificationSender sender;

    @Autowired
    Datastore ds;

    @Autowired
    PushNotificationHelper helper;

    @Autowired
    MongoClient client;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RestGraphDatabase gd;

    @Autowired
    RestCypherQueryEngine engine;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void sendNotification(NotificationResource notificationResource) {
        //Queue up the message with the AMQP Broker (RabbitMQ in this case)
        sender.sendNotification(notificationResource, RabbitMQConfig.notificationQueue);
    }

    @Override
    public void sendPublicFigureNotification(NotificationResource nr) {

        sender.sendNotification(nr, RabbitMQConfig.publicFigureNotificationQueue);
    }

    @Override
    public void sendGroupNotification(NotificationResource notificationResource) {
        sender.sendNotification(notificationResource, RabbitMQConfig.groupNotificationQueue);
    }

    @Override
    public String addOrUpdateDeviceRegistration(int userId, String regId) {
        return helper.addOrUpdateDeviceRegistration(userId, regId);
    }

    @Override
    public String removeDeviceRegistration(String regId, int userId) {
        return helper.removeDeviceRegistration(regId, userId);
    }

    @Override
    public void addDeviceToUserRelationship(List<AndroidDeviceRegistration> rows) {
        helper.addDeviceToUserRelationship(rows);
    }

    @Override
    public void addToUserRelationshipBatch(List<UserRelationship> recipients) {
        helper.addBatchRelationships(recipients);
    }

    @Override
    public void processBlockList() {
        DBCollection blockedlists;
        blockedlists = client.getDB("yookosreco").getCollection("blockedlists");
        if (blockedlists == null) {
            blockedlists = client.getDB("yookosreco").createCollection("blockedlists", null);
        }

        //Retrieve all users with android devices
        DBCollection deviceOwners = client.getDB("yookosreco").getCollection("androidusers");
        DBCursor owners = deviceOwners.find().sort(new BasicDBObject("userid", 1));

        //For each user, go to the core and get their blocked list
        for (DBObject owner : owners) {
            String tempId = owner.get("userid").toString();
            log.info(tempId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", ADMIN_AUTH);

            HttpEntity httpEntity = new HttpEntity(headers);

            String blocked_url = BLOCKED_LIST_URL + tempId;

            try {
                ResponseEntity<String> entity = restTemplate.exchange(blocked_url, HttpMethod.GET, httpEntity, String.class, Collections.EMPTY_MAP);

                //log.info("Blocked list for {}: {}", tempId, entity.getBody());

                //Create a blocked-list document for that user
                if (entity.getBody() != null) {
                    String[] blockedUsers = entity.getBody().split(",");
                    for (String blockedUser : blockedUsers) {
                        blockedlists.update(new BasicDBObject("userid", Integer.parseInt(tempId)),
                                new BasicDBObject("$addToSet",
                                        new BasicDBObject("blockedlist", Integer.parseInt(blockedUser))), true, false);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

    }

    @Override
    public void blockUsersFromSending(CoreUserBlock coreUserBlock) {
        ProcessBlockList pbl = new ProcessBlockList(coreUserBlock, "add");
        taskExecutor.execute(pbl);
    }

    class ProcessBlockList implements Runnable {
        CoreUserBlock coreUserBlock;
        String mode;

        public ProcessBlockList(CoreUserBlock coreUserBlock, String mode) {
            this.coreUserBlock = coreUserBlock;
            this.mode = mode;
        }

        @Override
        public void run() {
            if (mode.equals("add")) {
                DBCollection blockedLists = client.getDB("yookosreco").getCollection("blockedlists");

                //Process the list attribute
                String list = coreUserBlock.getList();

                //Break up the string is it is comma delimited..
                String[] splitList = list.split(",");

                for (String item : splitList) {
                    blockedLists.update(new BasicDBObject("userid", (int) coreUserBlock.getUserID()),
                            new BasicDBObject("$addToSet",
                                    new BasicDBObject("blockedlist", Integer.parseInt(item.trim()))), true, false);
                }
            }

            if (mode.equals("delete")) {
                DBCollection blockedLists = client.getDB("yookosreco").getCollection("blockedlists");

                //Process the list attribute
                String list = coreUserBlock.getList();

                //Break up the string is it is comma delimited..
                String[] splitList = list.split(",");

                for (String item : splitList) {
                    blockedLists.update(new BasicDBObject("userid", (int) coreUserBlock.getUserID()),
                            new BasicDBObject("$pull",
                                    new BasicDBObject("blockedlist", Integer.parseInt(item.trim()))), true, false);
                }
            }

        }
    }

    @Override
    public void unblockUsersFromSending(CoreUserBlock coreUserBlock) {
        ProcessBlockList pbl = new ProcessBlockList(coreUserBlock, "delete");
        taskExecutor.execute(pbl);
    }

    @Override
    public NotificationResource sendTestNotification(NotificationResource resource) {
        log.info("Initiating test notification processing");
        log.info("Received test notification: {}", resource);
        return resource;
    }

    @Override
    public UserRelationship addToUserRelationship(UserRelationship relationship) {
        if (relationship.getRelationshipType() == 1) {
            int actorid = relationship.getActorid();
            int followerid = relationship.getFollowerid();
            //Save for actor...
            ds.save(relationship);
            //Reverse the ids and clear the object id
            relationship.setFollowerid(actorid);
            relationship.setActorid(followerid);
            relationship.setId(null);

            try {
                ds.save(relationship);
            } catch (Exception e) {
                log.error("Morphia error: {}", e.getMessage());
            }

            try (Transaction tx = gd.beginTx()) {
                String cypherQuery = "match (p:Person{userid:{followerid}}), (q:Person{userid:{actorid}}) create unique (p)-[:friends_with{creationdate:{creationdate}}]->(q)";
                Map<String, Object> params = new HashMap<>();
                params.put("creationdate", relationship.getCreationdate());

                params.put("followerid", relationship.getFollowerid());
                params.put("actorid", relationship.getActorid());

               engine.query(cypherQuery, params);

                tx.success();
            } catch (Exception e) {
                log.error("Neo4j exception: {}", e.getMessage());
            }
        } else {
            ds.save(relationship);

            try (Transaction tx = gd.beginTx()) {
                String cypherQuery = "match (p:Person{userid:{followerid}}), (q:Person{userid:{actorid}}) create unique (p)-[:Follows{creationdate:{creationdate}}]->(q)";
                Map<String, Object> params = new HashMap<>();
                params.put("creationdate", relationship.getCreationdate());

                params.put("followerid", relationship.getFollowerid());
                params.put("actorid", relationship.getActorid());

                engine.query(cypherQuery, params);

                tx.success();
            } catch (Exception e) {
                log.error("Neo4j exception: {}", e.getMessage());
            }
        }

        return relationship;
    }

    @Override
    public void deleteUserRelationship(UserRelationship userRelationship) {
        DBCollection relationships = client.getDB("yookosreco").getCollection("relationships");
        relationships.remove(new BasicDBObject("actorid", userRelationship.getActorid())
                .append("followerid", userRelationship.getFollowerid()));

        //TODO: We also need to do this in Neo4j as well...
    }


}
