package com.yookos.yookore.domain.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yookos.yookore.domain.notification.Notification;

/**
 * Created by jome on 2014/08/27.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationResource {
    private String cmd;
    private Notification notification;


    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
