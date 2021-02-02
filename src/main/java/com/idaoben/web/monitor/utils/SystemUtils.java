package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class SystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static String osHome;

    private static final String userHome = System.getProperty("user.home");

    private static final String userName = System.getProperty("user.name");

    private static SystemOs systemOs;

    private static String currentPid;

    private static Boolean isWindows7;

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

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static boolean isWindows(){
        return getSystemOs() == SystemOs.WINDOWS;
    }

    public static boolean isLinux(){
        return getSystemOs() == SystemOs.LINUX;
    }

    public static boolean isWindows7(){
        if(isWindows7 == null){
            isWindows7 = isWindows() && System.getProperty("os.version").startsWith("6.");
        }
        return isWindows7;
    }
}
