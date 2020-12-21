package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class ActionDeviceListCommand {

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("进程PID")
    private String pid;

    @ApiModelProperty("用户名")
    private String user;

    @ApiModelProperty("设备名称")
    private String deviceName;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
