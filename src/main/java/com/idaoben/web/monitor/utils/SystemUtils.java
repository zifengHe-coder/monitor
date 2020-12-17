package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.enums.SystemOs;

public class SystemUtils {

    private static String osHome;

    private static String userHome;

    private static SystemOs systemOs;

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
        if(userHome == null){
            userHome = System.getProperty("user.home");
        }
        return userHome;
    }

    public static SystemOs getSystemOs(){
        if(systemOs == null){
            systemOs = System.getProperty("os.name").toLowerCase().startsWith("win") ? SystemOs.WINDOWS : SystemOs.LINUX;
        }
        return systemOs;
    }

    public static boolean isWindows(){
        return getSystemOs() == SystemOs.WINDOWS;
    }

    public static boolean isLinux(){
        return getSystemOs() == SystemOs.LINUX;
    }
}
