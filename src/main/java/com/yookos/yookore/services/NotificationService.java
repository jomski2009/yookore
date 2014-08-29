package com.yookos.yookore.services;

import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.domain.notification.NotificationResource;

import java.util.List;

/**
 * Interface for all push notification services
 * Created by jome on 2014/08/27.
 */

public interface NotificationService {
    void sendNotification(NotificationResource notificationResource);
    void sendPublicFigureNotification(NotificationResource notificationResource);

    String addOrUpdateDeviceRegistration(int userId, String regId);

    String removeDeviceRegistration(String regId, int userId);

    void addDeviceToUserRelationship(List<AndroidDeviceRegistration> rows);

    void addToUserRelationship(List<UserRelationship> recipients);
}
