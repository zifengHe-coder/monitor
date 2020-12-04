package com.idaoben.web.monitor.web.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class SoftwareDetailDto extends SoftwareDto{

    @ApiModelProperty("当前运行进程")
    private List<ProcessDto> processes;

    public List<ProcessDto> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessDto> processes) {
        this.processes = processes;
    }
}
