package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.enums.SystemOs;

import java.lang.management.ManagementFactory;

public class SystemUtils {

    private static String osHome;

    private static final String userHome = System.getProperty("user.home");

    private static final String userName = System.getProperty("user.name");

    private static SystemOs systemOs;

    private static String currentPid;

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String getOsHome(){
        if(osHome == null){
            if(getSystemOs() == SystemOs.WINDOWS){
                osHome = System.getProperty("user.home").substring(0, 2);
            } else {
                osHome = "/";
            }
        }
        return osHome;
    }

    public static String getUserHome(){
        return userHome;
    }

    public static String getUserName() {
        return userName;
    }

    public static SystemOs getSystemOs(){
        if(systemOs == null){
            systemOs = System.getProperty("os.name").toLowerCase().startsWith("win") ? SystemOs.WINDOWS : SystemOs.LINUX;
        }
        return systemOs;
    }

    public static String getCurrentPid(){
        if(currentPid == null){
            currentPid = ManagementFactory.getRuntimeMXBean().getSystemProperties().get("PID");
        }
        return currentPid;
    }

    public static boolean isWindows(){
        return getSystemOs() == SystemOs.WINDOWS;
    }

    public static boolean isLinux(){
        return getSystemOs() == SystemOs.LINUX;
    }
}
