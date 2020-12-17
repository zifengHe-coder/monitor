package com.idaoben.web.monitor.service.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MonitoringTask {

    private Long taskId;

    private String softwareId;

    /**
     * 正在运行监听的PID
     */
    private Set<String> pids = new CopyOnWriteArraySet<>();

    /**
     * 监听异常的PID
     */
    private Set<String> errorPids = new CopyOnWriteArraySet<>();

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

    Set<String> getPids() {
        return pids;
    }

    public Set<String> getErrorPids() {
        return errorPids;
    }

}
