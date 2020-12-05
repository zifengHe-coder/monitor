package com.idaoben.web.monitor.utils;

public class SystemUtils {

    private static String osHome;

    public static String getOsHome(){
        if(osHome == null){
            osHome = System.getProperty("user.home").substring(0, 3);
        }
        return osHome;
    }
}
