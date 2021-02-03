package com.idaoben.web.monitor.excel;

import com.idaoben.web.common.excel.CellNum;
import com.idaoben.web.monitor.dao.entity.enums.FileOpType;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;

import java.time.ZonedDateTime;

public class ActionFileExcel {

    @CellNum(index = 0)
    private String user;

    @CellNum(index = 1)
    private ZonedDateTime timestamp;

    @CellNum(index = 2)
    private String fileName;

    @CellNum(index = 3)
    private String path;

    @CellNum(index = 4)
    private FileOpType opType;

    @CellNum(index = 5)
    private FileSensitivity sensitivity;

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

    public FileOpType getOpType() {
        return opType;
    }

    public void setOpType(FileOpType opType) {
        this.opType = opType;
    }

    public FileSensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(FileSensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }
}
