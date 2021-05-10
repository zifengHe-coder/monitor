package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;
import com.idaoben.web.monitor.dao.entity.enums.ActionProcessType;

import java.time.ZonedDateTime;

public class ActionProcessExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private ActionProcessType type;

    @CellNum(index = 3)
    private String threadEntryAddress;

    @CellNum(index = 4)
    private String path;

    @CellNum(index = 5)
    private Long destPid;

    @CellNum(index = 6)
    private String destPName;

    @CellNum(index = 7)
    private Long destHwnd;

    @CellNum(index = 8)
    private String destTitle;

    @CellNum(index = 9)
    private String cmdLine;

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

    public ActionProcessType getType() {
        return type;
    }

    public void setType(ActionProcessType type) {
        this.type = type;
    }

    public String getThreadEntryAddress() {
        return threadEntryAddress;
    }

    public void setThreadEntryAddress(String threadEntryAddress) {
        this.threadEntryAddress = threadEntryAddress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getDestPid() {
        return destPid;
    }

    public void setDestPid(Long destPid) {
        this.destPid = destPid;
    }

    public String getDestPName() {
        return destPName;
    }

    public void setDestPName(String destPName) {
        this.destPName = destPName;
    }

    public Long getDestHwnd() {
        return destHwnd;
    }

    public void setDestHwnd(Long destHwnd) {
        this.destHwnd = destHwnd;
    }

    public String getDestTitle() {
        return destTitle;
    }

    public void setDestTitle(String destTitle) {
        this.destTitle = destTitle;
    }

    public String getCmdLine() {
        return cmdLine;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }
}
