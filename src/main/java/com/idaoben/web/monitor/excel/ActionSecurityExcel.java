package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;

import java.time.ZonedDateTime;

public class ActionSecurityExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1)
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private String daclSdString;

    @CellNum(index = 3)
    private String group;

    @CellNum(index = 4)
    private String owner;

    @CellNum(index = 5)
    private String target;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDaclSdString() {
        return daclSdString;
    }

    public void setDaclSdString(String daclSdString) {
        this.daclSdString = daclSdString;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
