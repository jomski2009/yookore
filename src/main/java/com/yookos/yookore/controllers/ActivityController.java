package com.yookos.yookore.controllers;

/**
 * Created by jome on 2014/08/29.
 */

import com.yookos.yookore.domain.Activity;
import com.yookos.yookore.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActivityController {
    @Autowired
    ActivityService activityService;

    @RequestMapping(value = "/activity/add", method = RequestMethod.POST)
    public ResponseEntity<Void> addActivitiesFromCore(@RequestBody List<Activity> activities) {
        //log.info("Processing Activities: {}", activities.toString());
        try {
            activityService.addActivityObjectToGraph(activities);

            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }
}
