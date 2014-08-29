package com.yookos.yookore.domain.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jome on 2014/08/28.
 */
public class NotificationKeyRequest {
    private String operation;
    private String notification_key_name;
    private List<String> registration_ids = new ArrayList<>();

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getNotification_key_name() {
        return notification_key_name;
    }

    public void setNotification_key_name(String notification_key_name) {
        this.notification_key_name = notification_key_name;
    }

    public List<String> getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }

    @Override
    public String toString() {
        return "NotificationKeyRequest{" +
                "operation='" + operation + '\'' +
                ", notification_key_name='" + notification_key_name + '\'' +
                ", registration_ids=" + registration_ids +
                '}';
    }
}
