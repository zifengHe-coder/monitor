package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class ActionProcessListCommand {

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("进程PID")
    private String pid;

    @ApiModelProperty("用户名")
    private String user;

    @ApiModelProperty("命令行")
    private String cmdLine;

    @ApiModelProperty("进程调用类型：16384:启动进程, 20480:进程注入(仅Windows), 20481：消息通讯(仅Windows), 20482: 进程间内存共享(仅Linux)")
    private Integer type;

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

    public String getCmdLine() {
        return cmdLine;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
