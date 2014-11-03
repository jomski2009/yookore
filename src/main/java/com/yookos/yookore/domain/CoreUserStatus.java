package com.yookos.yookore.domain;

/**
 * @Author  :   Emile
 * @Date    :   14/10/31-13:12
 * @Description :
 */
public class CoreUserStatus {
    private long userID;
    private String username;
    private boolean enabled;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
