package com.yookos.yookore.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by jome on 2014/08/29.
 */

@Entity("relationships")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRelationship {
    @Id
    private ObjectId id;
    private int actorid;
    private int followerid;
    private String username;
    private String email;
    private long creationdate;
    private boolean hasdevice;
    private int relationshipType;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getActorid() {
        return actorid;
    }

    public void setActorid(int actorid) {
        this.actorid = actorid;
    }

    public int getFollowerid() {
        return followerid;
    }

    public void setFollowerid(int followerid) {
        this.followerid = followerid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(long creationdate) {
        this.creationdate = creationdate;
    }

    public boolean isHasdevice() {
        return hasdevice;
    }

    public void setHasdevice(boolean hasdevice) {
        this.hasdevice = hasdevice;
    }

    public int getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(int relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public String toString() {
        return "UserRelationship{" +
                "id=" + id +
                ", actorid=" + actorid +
                ", followerid=" + followerid +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", creationdate=" + creationdate +
                ", hasdevice=" + hasdevice +
                ", relationshipType=" + relationshipType +
                '}';
    }
}
