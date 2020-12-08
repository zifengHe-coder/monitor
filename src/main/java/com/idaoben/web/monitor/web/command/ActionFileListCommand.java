package com.idaoben.web.monitor.web.command;

import com.idaoben.web.monitor.dao.entity.enums.FileOpType;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class ActionFileListCommand {

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("进程PID")
    private String pid;

    @ApiModelProperty("读写类型")
    private FileOpType opType;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("文件敏感度")
    private FileSensitivity sensitivity;

    @ApiModelProperty("操作开始时间")
    private ZonedDateTime startTime;

    @ApiModelProperty("操作结束时间")
    private ZonedDateTime endTime;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public FileOpType getOpType() {
        return opType;
    }

    public void setOpType(FileOpType opType) {
        this.opType = opType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileSensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(FileSensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }
}
