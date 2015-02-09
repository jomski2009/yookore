package com.yookos.yookore.services;

import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.CoreUserBlock;
import com.yookos.yookore.domain.CoreUserStatus;
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

    void sendGroupNotification(NotificationResource notificationResource);

    String addOrUpdateDeviceRegistration(int userId, String regId);

    String removeDeviceRegistration(String regId, int userId);

    void addDeviceToUserRelationship(List<AndroidDeviceRegistration> rows);

    void addToUserRelationshipBatch(List<UserRelationship> recipients);

    void processBlockList();

    void blockUsersFromSending(CoreUserBlock coreUserBlock);

    void unblockUsersFromSending(CoreUserBlock coreUserBlock);

    NotificationResource sendTestNotification(NotificationResource notificationResource);

    UserRelationship addToUserRelationship(UserRelationship userRelationship);

    void deleteUserRelationship(UserRelationship userRelationship);

    CoreUserStatus setNotificationStatus(CoreUserStatus coreUserStatus);

    boolean getNotificationStatus(long id);

    CoreUserBlock getListOfBlockedIDsForUser(long id);
}
