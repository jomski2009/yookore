package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all push notifications
 * requests
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

}
