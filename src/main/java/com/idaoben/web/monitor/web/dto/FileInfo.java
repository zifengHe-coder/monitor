package com.idaoben.web.monitor.web.dto;

import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;

public class FileInfo {

    private String fd;

    private String fileName;

    private String path;

    private FileSensitivity sensitivity;

    public FileInfo(String fd, String fileName, String path, FileSensitivity sensitivity) {
        this.fd = fd;
        this.fileName = fileName;
        this.path = path;
        this.sensitivity = sensitivity;
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

    public FileSensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(FileSensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }
}
