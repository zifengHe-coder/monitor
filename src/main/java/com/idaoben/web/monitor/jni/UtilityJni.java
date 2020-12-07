package com.idaoben.web.monitor.jni;

public class UtilityJni {

    static {
        System.load(System.getProperty("user.dir") + "\\src\\main\\resources\\dll\\Utility64.dll");
    }

    public native int ListAllDevices();

    public native int ResolveFilePathByFd(long fd);

    public native int ListAllProcesses();

    public native int QueryProcessDetails(String pids);

    public native int GetBlockSize(int ref);

    public native int ReadBlock(int ref, byte[] buffer, int offset, int size);

    public native void FreeBlock(int ref);
}
