package com.yookos.yookore.services.impl;

import com.yookos.yookore.domain.Activity;
import com.yookos.yookore.services.ActivityService;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jome on 2014/09/08.
 */

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    Datastore ds;

    @Override
    public void addActivityObjectToGraph(List<Activity> activities) {
        taskExecutor.execute(new ProcessActivity(activities));
    }

    //Runnable to process the activities as they come in..
    class ProcessActivity implements Runnable {
        private List<Activity> activities;

        public ProcessActivity() {
        }

        public ProcessActivity(List<Activity> activities) {
            this.activities = activities;
        }

        @Override
        public void run() {
            Map<String, Object> params = new HashMap<>();
            //log.info("Processing Activity Collection of size: " + activities.size());

            for (Activity activity : activities) {
                if (activity.getUserID() > 1001) {
                    activity.setProcessed(false);
                    //Thinking we should be dumping the activity data in mongo?
                    //And then run a scheduled task later to populate neo4j?
                    Key<Activity> key = ds.save(activity);
                }

            }
        }
    }
}
