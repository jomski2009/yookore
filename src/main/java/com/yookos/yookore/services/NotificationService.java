package com.yookos.yookore.services;

import com.yookos.yookore.domain.notification.NotificationResource;

/**
 * Interface for all push notification services
 * Created by jome on 2014/08/27.
 */

public interface NotificationService {
    public void sendNotification(NotificationResource notificationResource);
    public void sendPublicFigureNotification(NotificationResource notificationResource);
}
