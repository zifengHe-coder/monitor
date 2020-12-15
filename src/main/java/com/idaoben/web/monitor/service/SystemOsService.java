package com.idaoben.web.monitor.service;

import com.idaoben.web.monitor.web.dto.DeviceInfoJson;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;

import java.io.File;
import java.util.List;

public interface SystemOsService {

    /**
     * Get the action folder path
     * @return
     */
    String getActionFolderPath();

    /**
     * 获取系统已安装软件
     * @return
     */
    List<SoftwareDto>  getSystemSoftware();

    /**
     * 获取系统文件的图标
     * @param file
     * @return
     */
    String getIconBase64(File file);

    /**
     * 获取当前系统运行进程信息
     * @return
     */
    List<ProcessJson> listAllProcesses();

    /**
     * Start process with hook
     * @param commandLine
     * @param currentDirectory
     * @return
     */
    int startProcessWithHooks(String commandLine, String currentDirectory);

    /**
     * Attach and inject hooks
     * @param pid
     * @return
     */
    boolean attachAndInjectHooks(int pid);

    /**
     * Remove hooks
     * @param pid
     * @return
     */
    boolean removeHooks(int pid);

    /**
     * 是否需要自动监听子进程，暂时只有windows需要应用层自动监听子进程
     * @return
     */
    boolean isAutoMonitorChildProcess();

    /**
     * 根据instanceId获取设备信息
     * @param instanceId 设备ID
     * @return
     */
    DeviceInfoJson getDeviceInfo(String instanceId);
}
