package com.idaoben.web.monitor.service;

import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
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

    /**
     * 判断是否可执行程序
     * @param file
     * @return
     */
    boolean isExeFile(File file);

    /**
     * 判断当前文件路径判断文件系统敏感性
     * @param path
     * @return
     */
    FileSensitivity getFileSensitivity(String path);

    /**
     * 从文件行为中分析出设备/Process行为
     * @param action
     * @return
     */
    ActionGroup setActionFromFileInfo(Action action);

    /**
     * Get file access from access value
     * @param accessLong
     * @return
     */
    FileAccess getFileAccess(Long accessLong);
}
