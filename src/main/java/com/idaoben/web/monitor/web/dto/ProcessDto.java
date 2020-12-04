package com.idaoben.web.monitor.web.dto;

import io.swagger.annotations.ApiModelProperty;

public class ProcessDto {

    @ApiModelProperty("进程号")
    private int pid;

    @ApiModelProperty("进程名称")
    private String name;

    @ApiModelProperty("cpu实用率")
    private Float cpu;

    @ApiModelProperty("内存使用量")
    private Float memory;

    @ApiModelProperty("是否监控中")
    private boolean monitoring;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
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

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }
}
