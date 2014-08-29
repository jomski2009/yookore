package com.yookos.yookore.services.impl;

import com.mongodb.MongoClient;
import com.yookos.yookore.config.RabbitMQConfig;
import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.domain.notification.NotificationResource;
import com.yookos.yookore.helpers.PushNotificationHelper;
import com.yookos.yookore.rabbit.NotificationSender;
import com.yookos.yookore.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of NotificationService contract.
 * <p/>
 * Created by jome on 2014/08/27.
 */

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    NotificationSender sender;

    @Autowired
    PushNotificationHelper helper;

    @Autowired
    MongoClient client;

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
    public void addToUserRelationship(List<UserRelationship> recipients) {
        helper.addBatchRelationships(recipients);
    }
}
