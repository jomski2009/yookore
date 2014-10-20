package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jome on 2014/09/08.
 */

@RestController
@RequestMapping("relationships")
public class RelationshipController {
    @Autowired
    NotificationService notificationService;

    @RequestMapping(value = "pcl/add", method = RequestMethod.POST)
    public ResponseEntity<UserRelationship> addPclRelationship(@RequestBody UserRelationship userRelationship) {
        userRelationship.setHasdevice(false);
        UserRelationship relationship = notificationService.addToUserRelationship(userRelationship);
        if (relationship != null) {
            return new ResponseEntity<>(relationship, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "pcl/delete", method = RequestMethod.POST)
    public ResponseEntity<Boolean> deletePclRelationship(@RequestBody UserRelationship userRelationship) {
        notificationService.deleteUserRelationship(userRelationship);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
