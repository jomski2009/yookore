package com.yookos.yookore.domain.notification;

/**
 * Created by jome on 2014/08/27.
 */
public class Notification {
    private long userId;
    private NotificationContent content;
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public NotificationContent getContent() {
        return content;
    }
    public void setContent(NotificationContent content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "userId=" + userId +
                ", content=" + content +
                '}';
    }
}
