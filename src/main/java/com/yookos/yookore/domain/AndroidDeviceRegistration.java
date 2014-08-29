package com.yookos.yookore.domain;

/**
 * Created by jome on 2014/08/28.
 */
public class AndroidDeviceRegistration {
    private String gcm_regid;
    private long userid;

    public String getGcm_regid() {
        return gcm_regid;
    }

    public void setGcm_regid(String gcm_regid) {
        this.gcm_regid = gcm_regid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }
}
