package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.enums.SystemOs;

public class SystemUtils {

    private static String osHome;

    private static SystemOs systemOs;

    private static String sensitivityPath;

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String getOsHome(){
        if(osHome == null){
            osHome = System.getProperty("user.home").substring(0, 3);
        }
        return osHome;
    }

    public static SystemOs getSystemOs(){
        if(systemOs == null){
            systemOs = System.getProperty("os.name").toLowerCase().startsWith("win") ? SystemOs.WINDOWS : SystemOs.LINUX;
        }
        return systemOs;
    }

    public static String getSensitivityPath(){
        if(sensitivityPath == null){
            if(getSystemOs() == SystemOs.WINDOWS){
                sensitivityPath = getOsHome() + "WINDOWS\\";
            } else {
                //TODO: 待补充Linux的敏感目录
            }
        }
        return sensitivityPath;
    }
}
