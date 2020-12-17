package com.idaoben.web.monitor.web.dto;

import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;

public class FileInfo {

    private String fd;

    private String fileName;

    private String path;

    private String backup;

    private FileSensitivity sensitivity;

    private String deviceName;

    private ActionGroup actionGroup;

    public FileInfo(String fd, String fileName, String path, String backup, FileSensitivity sensitivity, String deviceName, ActionGroup actionGroup) {
        this.fd = fd;
        this.fileName = fileName;
        this.path = path;
        this.backup = backup;
        this.sensitivity = sensitivity;
        this.deviceName = deviceName;
        this.actionGroup = actionGroup;
    }

    public String getFd() {
        return fd;
    }

    public void setFd(String fd) {
        this.fd = fd;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }

    public FileSensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(FileSensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }
}
