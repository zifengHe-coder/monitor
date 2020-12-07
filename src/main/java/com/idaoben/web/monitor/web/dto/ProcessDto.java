package com.idaoben.web.monitor.web.dto;

import com.idaoben.web.monitor.dao.entity.enums.MonitorStatus;
import io.swagger.annotations.ApiModelProperty;

public class ProcessDto {

    @ApiModelProperty("进程号PID")
    private String pid;

    @ApiModelProperty("进程名称")
    private String name;

    @ApiModelProperty("cpu使用率")
    private Float cpu;

    @ApiModelProperty("内存使用量")
    private Float memory;

    @ApiModelProperty("监控状态")
    private MonitorStatus monitorStatus;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public Float getMemory() {
        return memory;
    }

    public void setMemory(Float memory) {
        this.memory = memory;
    }

    public MonitorStatus getMonitorStatus() {
        return monitorStatus;
    }

    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
}
