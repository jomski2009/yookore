package com.yookos.yookore.domain.notification;

/**
 * Created by jome on 2014/08/27.
 */
public class AndroidPushNotificationData {

    private NotificationResource notificationResource;
    private long userid;
    private String regid;


    public AndroidPushNotificationData() {
    }

    public AndroidPushNotificationData(NotificationResource notificationResource, long userid) {
        this.notificationResource = notificationResource;
        this.userid = userid;
    }

    public NotificationResource getNotificationResource() {
        return notificationResource;
    }

    public void setNotificationResource(NotificationResource notificationResource) {
        this.notificationResource = notificationResource;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }
}
