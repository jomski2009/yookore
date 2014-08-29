package com.yookos.yookore.domain.notification;

/**
 * Created by jome on 2014/08/27.
 */
public class NotificationContent {
    private String senderDisplayName;
    private long authorId;
    private long objectId;
    private String objectType;
    private String alertMessage;

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return "NotificationContent{" +
                "senderDisplayName='" + senderDisplayName + '\'' +
                ", authorId=" + authorId +
                ", objectId=" + objectId +
                ", objectType='" + objectType + '\'' +
                ", alertMessage='" + alertMessage + '\'' +
                '}';
    }
}
