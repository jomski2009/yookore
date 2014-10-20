package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.CoreUserBlock;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle all push notifications
 * requests and related methods.
 *
 * @author Jome Akpoduado
 * @version 1.0.1
 */

@RestController
@RequestMapping("notification")

public class NotificationController {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NotificationService notificationService;

    //Push notification endpoints

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity sendPublicFigureNotification(
            @RequestBody NotificationResource notificationResource) {

        log.debug("Received notification to send: {}", notificationResource);
        notificationService.sendPublicFigureNotification(notificationResource);
        return new ResponseEntity<>(notificationResource,
                HttpStatus.OK);
    }

    @RequestMapping(value = "push", method = RequestMethod.POST)
    public HttpEntity sendNotification(
            @RequestBody NotificationResource notificationResource) {
        log.debug("Received notification to send: {}", notificationResource);
        notificationService.sendNotification(notificationResource);
        return new ResponseEntity<>(notificationResource,
                HttpStatus.OK);
    }

    @RequestMapping(value = "test", method = RequestMethod.POST)
    public HttpEntity sendTestNotification(
            @RequestBody NotificationResource notificationResource) {
        log.info("Received notification to send: {}", notificationResource);
        //NotificationResource resource = notificationService.sendTestNotification(notificationResource);
        return new ResponseEntity<>(notificationResource,
                HttpStatus.OK);
    }


    //Device registration endpoints.

    @RequestMapping(value = "devices/add")
    public ResponseEntity<String> addUserAndroidDevice(@RequestParam String regId, @RequestParam int userId) {
        log.debug("Adding device with id: {}", regId);
        String result = notificationService.addOrUpdateDeviceRegistration(userId, regId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "devices/remove")
    public ResponseEntity<String> removeUserAndroidDevice(@RequestParam String regId, @RequestParam int userId) {
        log.debug("Removing device with id: {}", regId);
        String writeResult = notificationService.removeDeviceRegistration(regId, userId);
        return new ResponseEntity<>(writeResult, HttpStatus.OK);
    }

    //Notification Blocks
    @RequestMapping(value = "blocklist/add")
    public HttpEntity addToBlockList(@RequestBody CoreUserBlock coreUserBlock){
        log.info("Received request to block: {}", coreUserBlock);
        notificationService.blockUsersFromSending(coreUserBlock);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "blocklist/delete")
    public HttpEntity removeFromBlockList(@RequestBody CoreUserBlock coreUserBlock){
        log.info("Received request to unblock: {}", coreUserBlock);
        notificationService.unblockUsersFromSending(coreUserBlock);
        return new ResponseEntity(HttpStatus.OK);
    }




}
