package com.idaoben.web.monitor.jni;

public class FunctionHookLoaderJni {

    static {
        System.load(System.getProperty("user.dir") + "\\src\\main\\resources\\dll\\FunctionHookLoader64.dll");
    }

    public native int AttachAndInjectHooks(int pid);

    public native int RemoveHooks(int pid);

    public native int StartProcessWithHooksA(String commandLine, String currentDirectory);

}
