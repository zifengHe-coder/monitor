package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;
import com.idaoben.web.monitor.dao.entity.enums.ActionNetworkType;

import java.time.ZonedDateTime;

public class ActionNetworkExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private ActionNetworkType type;

    @CellNum(index = 3)
    private String host;

    @CellNum(index = 4)
    private Integer port;

    @CellNum(index = 5)
    private String protocol;

    @CellNum(index = 6)
    private Long bytes;

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

    public ActionNetworkType getType() {
        return type;
    }

    public void setType(ActionNetworkType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }
}
