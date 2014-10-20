package com.yookos.yookore.helpers;

import com.mongodb.Mongo;
import com.yookos.notifyserver.core.domain.Activity;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.neo4j.cypher.CypherExecutionException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestResultException;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by jome on 2014/09/10.
 */
@EnableScheduling
@Component
public class TransformActivityToGraphTask {
    private final static String cronServerIp = "10.223.243.14";
    //private final static String cronServerIp = "192.168.2.15";

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Datastore ds;

    @Autowired
    Mongo mongo;

    @Autowired
    RestCypherQueryEngine engine;

    @Autowired
    RestAPIFacade gd;

    @Autowired
    Environment env;


    /**
     * All this method does is to add a processed property to to the activity documents
     */
    @Scheduled(fixedDelay = 240000L)
    public void updateActivities() {

    }

    @Scheduled(fixedDelay = 60000L, initialDelay = 5000L)
    public void processActivities() {
        //This should only run on yookore-mcc
        if (env.getProperty("can.process.activities") != null && env.getProperty("can.process.activities").equals("true")) {
            log.info("Starting ActivityTransformation");
            Query<Activity> activities = ds.find(Activity.class, "processed", false);
            log.info("Number of activities to process: {}", activities.getBatchSize());

            for (Activity activity : activities) {
                StringBuilder query = new StringBuilder();
                log.info("Processing Activity: {}", activity);

                query.append("match (p:Person{userid:").append(activity.getUserID()).append("})");
                query.append(" create unique (p)-[:created]->(a:Activity{");
                query.append("activityid:").append(activity.getActivityID()).append(",");
                query.append("objecttype:").append(activity.getTargetObjectType()).append(",");
                query.append("objectid:").append(activity.getTargetObjectID()).append(",");
                query.append("containertype:").append(activity.getContainerObjectType()).append(",");
                query.append("containerid:").append(activity.getContainerObjectID()).append(",");
                query.append("activitytype:\"").append(activity.getType()).append("\",");
                query.append("creationdate:").append(activity.getCreationDate()).append("})");
                query.append(" return a");

                try (Transaction tx = gd.beginTx()) {

                    log.info(query.toString());

                    QueryResult<Map<String, Object>> query1 = engine.query(query.toString(), null);

                    RestNode node = (RestNode) query1.iterator().next().get("a");



                    //engine.query("match (p:Person{userid:{userid}}) create unique (p)-[:created]->(a:Activity{props})", params);

                    tx.success();
                    if (node != null) {
                        log.info("Node property: Activity id created - {}", node.getProperty("activityid"));
                        activity.setProcessed(true);
                        ds.save(activity);
                    }else{
                        log.info("The activity was not created. {}", activity.getActivityID());
                    }

                } catch (RestResultException rre) {
                    log.info(rre.getMessage());
                    activity.setProcessed(true);
                    ds.save(activity);
                } catch (CypherExecutionException cee) {
                    log.info(cee.getMessage());
                    activity.setProcessed(true);
                    ds.save(activity);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }

        }
    }
}