package com.idaoben.web.monitor.service;

import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;

import java.io.File;
import java.util.List;

public interface SystemOsService {

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
}
