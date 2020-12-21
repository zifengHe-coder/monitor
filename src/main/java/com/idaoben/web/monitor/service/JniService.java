package com.idaoben.web.monitor.service;

import java.util.List;

public interface JniService {

    String listAllDevices();

    String listAllProcesses();

    String queryProcessDetails(List<Integer> pids);

    String queryLinkInfos(List<String> links);

    boolean attachAndInjectHooks(int pid);

    boolean removeHooks(int pid);

    int startProcessWithHooksA(String commandLine, String currentDirectory);
}
