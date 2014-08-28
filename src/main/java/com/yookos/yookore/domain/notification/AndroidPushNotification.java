package com.yookos.yookore.domain.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jome on 2014/08/27.
 */
public class AndroidPushNotification {
    private String notification_key;
    private String notification_key_name;

    private PushMessageWrapper data;
    private List<String> registration_ids = new ArrayList<>();

    public String getNotification_key() {
        return notification_key;
    }

    public void setNotification_key(String notification_key) {
        this.notification_key = notification_key;
    }

    public String getNotification_key_name() {
        return notification_key_name;
    }

    public void setNotification_key_name(String notification_key_name) {
        this.notification_key_name = notification_key_name;
    }

    public PushMessageWrapper getData() {
        return data;
    }

    public void setData(PushMessageWrapper data) {
        this.data = data;
    }

    public List<String> getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }
}
