package com.idaoben.web.monitor.service;

public interface JniService {

    String listAllProcesses();

    boolean attachAndInjectHooks(int pid);

    boolean removeHooks(int pid);

    boolean startProcessWithHooksA(String commandLine, String currentDirectory);
}
