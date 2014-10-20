package com.yookos.notifyserver.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by jome on 2014/09/10.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity("activities")
public class Activity {
    @Id
    private long activityID;
    private long targetObjectID;
    private int targetObjectType;
    private long userID;
    private int containerObjectID;
    private int containerObjectType;
    private String type;
    private long creationDate;
    private boolean processed;

    public long getActivityID() {
        return activityID;
    }

    public void setActivityID(long activityID) {
        this.activityID = activityID;
    }

    public long getTargetObjectID() {
        return targetObjectID;
    }

    public void setTargetObjectID(long targetObjectID) {
        this.targetObjectID = targetObjectID;
    }

    public int getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(int targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public int getContainerObjectID() {
        return containerObjectID;
    }

    public void setContainerObjectID(int containerObjectID) {
        this.containerObjectID = containerObjectID;
    }

    public int getContainerObjectType() {
        return containerObjectType;
    }

    public void setContainerObjectType(int containerObjectType) {
        this.containerObjectType = containerObjectType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "activityID:" + activityID +
                ", targetObjectID:" + targetObjectID +
                ", targetObjectType:" + targetObjectType +
                ", userID:" + userID +
                ", containerObjectID:" + containerObjectID +
                ", containerObjectType:" + containerObjectType +
                ", type:'" + type + '\'' +
                ", creationDate:" + creationDate +
                '}';
    }
}