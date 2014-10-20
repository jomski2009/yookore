package com.yookos.yookore.domain;

/**
 * Created by jome on 2014/09/08.
 */
public class CoreUserBlock {
    private long userID;
    private String username;
    private String list;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }
}
