package com.idaoben.web.monitor.web.dto;

import java.util.List;

public class DeviceListJson {

    private List<DeviceInfoJson> devices;

    public List<DeviceInfoJson> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfoJson> devices) {
        this.devices = devices;
    }
}
