package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;

import java.time.ZonedDateTime;

public class ActionDeviceExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1)
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private String deviceName;

    @CellNum(index = 3)
    private String deviceId;

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
