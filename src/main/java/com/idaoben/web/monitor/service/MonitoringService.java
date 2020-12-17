package com.idaoben.web.monitor.service;

import com.idaoben.web.monitor.service.impl.MonitoringTask;

import java.util.Collection;
import java.util.Set;

public interface MonitoringService {

    /**
     * 获取正在监控的任务
     * @param softwareId
     * @return
     */
    MonitoringTask getMonitoringTask(String softwareId);

    /**
     * 判断当前软件是否正在监控
     * @param softwareId
     * @return
     */
    boolean isMonitoring(String softwareId);

    /**
     * 判断当前进程是否正在监控
     * @param softwareId
     * @param pid
     * @return
     */
    boolean isPidMonitoring(String softwareId, String pid);

    /**
     * 判断当前进程是否监控失败
     * @param softwareId
     * @param pid
     * @return
     */
    boolean isPidMonitoringError(String softwareId, String pid);

    /**
     * 通过进程号反向获取正在监控的软件列表
     * @param pid
     * @return
     */
    String getMonitoringSoftwareIdByPid(String pid);

    /**
     * 获取所有正在监控的软件列表
     * @return
     */
    Set<String> getMonitoringSoftwareIds();

    /**
     * 增加正在监控的任务
     * @param softwareId
     * @param task
     */
    void putMonitoringTask(String softwareId, MonitoringTask task);

    /**
     * 移除正在监听的任务
     * @param softwareId
     */
    void removeMonitoringTask(String softwareId);

    /**
     * 通过softwareId获取正在监听的进程号
     * @param task
     */
    Set<String> getMonitoringPids(MonitoringTask task);

    /**
     * 添加正在监听的进程号
     * @param task
     * @param pid
     */
    void addMonitoringPid(MonitoringTask task, String pid);

    /**
     * 移除正在监听的进程号
     * @param task
     * @param pid
     */
    void removeMonitoringPid(MonitoringTask task, String pid);

    /**
     * 移除正在监听的进程号
     * @param task
     * @param pids
     */
    void removeMonitoringPids(MonitoringTask task, Collection pids);

    //void setMonitoringPidToError(String softwareId, );

}
