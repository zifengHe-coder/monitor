package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class TaskListCommand {

    @ApiModelProperty("进程PID")
    private String pid;

    @ApiModelProperty("开始时间")
    private ZonedDateTime startTime;

    @ApiModelProperty("结束时间")
    private ZonedDateTime endTime;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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
