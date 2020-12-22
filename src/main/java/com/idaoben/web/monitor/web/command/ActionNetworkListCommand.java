package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class ActionNetworkListCommand {

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("进程PID")
    private String pid;

    @ApiModelProperty("用户名")
    private String user;

    @ApiModelProperty("目标IP")
    private String host;

    @ApiModelProperty("目标端口")
    private Integer port;

    @ApiModelProperty("类型：4096:发起网络连接, 4097:TCP数据发送, 4098：TCP数据接收")
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
