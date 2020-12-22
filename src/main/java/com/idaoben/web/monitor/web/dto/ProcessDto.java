package com.idaoben.web.monitor.web.dto;

import com.idaoben.web.monitor.dao.entity.enums.MonitorStatus;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

public class ProcessDto {

    @ApiModelProperty("进程号PID")
    private String pid;

    @ApiModelProperty("用户名")
    private String user;

    @ApiModelProperty("进程名称")
    private String name;

    @ApiModelProperty("cpu使用率%")
    private String cpu;

    @ApiModelProperty("内存使用量k")
    private Long memory;

    @ApiModelProperty("监控状态")
    private MonitorStatus monitorStatus;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public MonitorStatus getMonitorStatus() {
        return monitorStatus;
    }

    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
}
