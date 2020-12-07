package com.idaoben.web.monitor.web.dto;

import java.util.ArrayList;
import java.util.List;

public class MonitoringTask {

    private Long taskId;

    private String softwareId;

    /**
     * 正在运行监听的PID
     */
    private List<String> pids = new ArrayList<>();

    /**
     * 监听异常的PID
     */
    private List<String> errorPids = new ArrayList<>();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }

    public List<String> getPids() {
        return pids;
    }

    public void setPids(List<String> pids) {
        this.pids = pids;
    }

    public List<String> getErrorPids() {
        return errorPids;
    }

    public void setErrorPids(List<String> errorPids) {
        this.errorPids = errorPids;
    }
}
