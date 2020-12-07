package com.idaoben.web.monitor.service;

import java.util.List;

public interface JniService {

    String listAllProcesses();

    String queryLinkInfos(List<String> links);

    boolean attachAndInjectHooks(int pid);

    boolean removeHooks(int pid);

    int startProcessWithHooksA(String commandLine, String currentDirectory);
}
