package com.idaoben.web.monitor.dao.entity.enums;

public class ActionType {

    //监控开始
    public static final int START = 0;

    //监控结束
    public static final int STOP = 4095;

    //发起网络连接
    public static final int NETWORK_OPEN = 4096;

    //TCP数据发送
    public static final int NETWORK_TCP_SEND = 4097;

    //TCP数据接收
    public static final int NETWORK_TCP_RECEIVE = 4098;

    //UDP数据发送
    public static final int NETWORK_UDP_SEND = 4099;

    //UDP数据接收
    public static final int NETWORK_UDP_RECEIVE = 4100;

    //文件打开
    public static final int FILE_OPEN = 8192;

    //文件写入
    public static final int FILE_WRITE = 8193;

    //文件读写偏移定位
    public static final int FILE_SEEK = 8195;

    //文件删除
    public static final int FILE_DELETE_LINUX = 8196;

    public static final int FILE_DELETE_WINDOWS = 8198;

    //注册表打开或创建键
    public static final int REGISTRY_OPEN_KEY = 12288;

    //注册表删除键
    public static final int REGISTRY_DELETE_KEY = 12290;

    //注册表删除值键
    public static final int REGISTRY_DELETE_VALUE = 12291;

    //注册表设置值键
    public static final int REGISTRY_SET_VALUE = 12294;

    //启动进程
    public static final int PROCESS_OPEN = 16384;

    public static final int PROCESS_OPEN_LINUX = 16385;

    //进程注入
    public static final int PROCESS_INJECT = 20480;

    //进程间消息通讯
    public static final int PROCESS_MESSAGE_SEND = 20481;

    //进程间内存共享
    public static final int PROCESS_SHARE_MEMORY = 20482;

    //修改对象安全描述符
    public static final int SECURITY_UPDATE = 24576;

    //修改文件权限
    public static final int SECURITY_FILE_UPDATE = 8199;

    //修改文件拥有者
    public static final int SECURITY_FILE_OWNER_UPDATE = 8200;

    //设备控制
    public static final int DEVICE_CONTROL = 8201;

    public static boolean isFileType(int access){
        return access == FILE_OPEN || access == FILE_WRITE || access == FILE_DELETE_WINDOWS || access == FILE_DELETE_LINUX;
    }

    public static boolean isDeviceType(int access){
        return access == DEVICE_CONTROL;
    }

    public static boolean isNetworkType(int access){
        return access == NETWORK_OPEN || access == NETWORK_TCP_SEND || access == NETWORK_TCP_RECEIVE || access == NETWORK_UDP_SEND || access == NETWORK_UDP_RECEIVE;
    }

    public static boolean isRegistryType(int access){
        return access == REGISTRY_OPEN_KEY || access == REGISTRY_DELETE_KEY || access == REGISTRY_DELETE_VALUE || access == REGISTRY_SET_VALUE;
    }

    public static boolean isProcessType(int access){
        return access == PROCESS_OPEN || access == PROCESS_OPEN_LINUX || access == PROCESS_INJECT || access == PROCESS_MESSAGE_SEND;
    }

    public static boolean isSecurity(int access){
        return access == SECURITY_UPDATE || access == SECURITY_FILE_UPDATE || access == SECURITY_FILE_OWNER_UPDATE;
    }
}
