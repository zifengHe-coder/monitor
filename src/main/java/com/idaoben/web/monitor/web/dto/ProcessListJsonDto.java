package com.idaoben.web.monitor.web.dto;

import java.util.List;

public class ProcessListJsonDto {

    private List<ProcessJsonDto> processes;

    public List<ProcessJsonDto> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessJsonDto> processes) {
        this.processes = processes;
    }
}
