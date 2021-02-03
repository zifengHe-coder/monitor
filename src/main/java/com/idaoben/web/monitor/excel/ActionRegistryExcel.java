package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;
import com.idaoben.web.monitor.dao.entity.enums.ActionRegistryType;

import java.time.ZonedDateTime;

public class ActionRegistryExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private ActionRegistryType type;

    @CellNum(index = 3)
    private String parent;

    @CellNum(index = 4)
    private String key;

    @CellNum(index = 5)
    private String valueName;

    @CellNum(index = 6)
    private String valueType;

    @CellNum(index = 7)
    private String data;

    @CellNum(index = 8)
    private String oldValueType;

    @CellNum(index = 9)
    private String oldData;

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

    public ActionRegistryType getType() {
        return type;
    }

    public void setType(ActionRegistryType type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOldValueType() {
        return oldValueType;
    }

    public void setOldValueType(String oldValueType) {
        this.oldValueType = oldValueType;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }
}
