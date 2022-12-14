package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitoringServiceImpl implements MonitoringService {

    private Map<String, MonitoringTask> monitoringSoftwareTaskMap = new ConcurrentHashMap<>();

    private Map<String, String> pidSoftwareIdMap = new ConcurrentHashMap<>();

    private Map<String, List<ProcessJson>> processMaps = new HashMap();

    public MonitoringTask getMonitoringTask(String softwareId){
        return monitoringSoftwareTaskMap.get(softwareId);
    }

    public boolean isMonitoring(String softwareId){
        return monitoringSoftwareTaskMap.containsKey(softwareId);
    }

    public boolean isPidMonitoring(String softwareId, String pid){
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask != null){
            return monitoringTask.getPids().contains(pid);
        }
        return false;
    }

    public boolean isPidMonitoringError(String softwareId, String pid){
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask != null){
            return monitoringTask.getErrorPids().contains(pid);
        }
        return false;
    }

    public String getMonitoringSoftwareIdByPid(String pid){
        return pidSoftwareIdMap.get(pid);
    }

    public Set<String> getMonitoringSoftwareIds(){
        return monitoringSoftwareTaskMap.keySet();
    }

    @Override
    public void putMonitoringTask(String softwareId, MonitoringTask task) {
        monitoringSoftwareTaskMap.put(softwareId, task);
    }

    @Override
    public void removeMonitoringTask(String softwareId) {
        MonitoringTask task = monitoringSoftwareTaskMap.remove(softwareId);
        if(task != null){
            task.getPids().forEach( pid -> pidSoftwareIdMap.remove(pid));
        }
    }

    @Override
    public Set<String> getMonitoringPids(MonitoringTask task) {
        return task.getPids();
    }

    @Override
    public void addMonitoringPid(MonitoringTask task, String pid) {
        task.getPids().add(pid);
        pidSoftwareIdMap.put(pid, task.getSoftwareId());
    }

    @Override
    public void removeMonitoringPid(MonitoringTask task, String pid) {
        task.getPids().remove(pid);
        pidSoftwareIdMap.remove(pid);
    }

    @Override
    public void removeMonitoringPids(MonitoringTask task, Collection pids) {
        task.getPids().removeAll(pids);
        pids.forEach(pid -> {pidSoftwareIdMap.remove(pid);});
    }

    @Override
    public void setMonitoringPidToError(String pid) {
        String softwareId = getMonitoringSoftwareIdByPid(pid);
        if(StringUtils.isNotEmpty(softwareId)){
            MonitoringTask monitoringTask = getMonitoringTask(softwareId);
            if(monitoringTask != null){
                monitoringTask.getPids().remove(pid);
                monitoringTask.getErrorPids().add(pid);
            }
        }
    }

    @Override
    public List<ProcessJson> getProcessPids(String softwareId){
        return processMaps.get(softwareId);
    }

    @Override
    public void setProcessMaps(Map<String, List<ProcessJson>> processMaps) {
        this.processMaps = processMaps;
    }
}
