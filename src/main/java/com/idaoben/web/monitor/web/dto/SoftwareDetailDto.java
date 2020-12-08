package com.idaoben.web.monitor.web.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class SoftwareDetailDto extends SoftwareDto{

    @ApiModelProperty("是否监听中")
    private boolean monitoring;

    @ApiModelProperty("正在监控当前任务ID")
    private Long taskId;

    @ApiModelProperty("当前运行进程")
    private List<ProcessDto> processes;

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public List<ProcessDto> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessDto> processes) {
        this.processes = processes;
    }
}
