package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.jni.FunctionHookLoaderJni;
import com.idaoben.web.monitor.jni.UtilityJni;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.utils.SystemUtils;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class JniServiceImpl implements JniService {

    private UtilityJni utilityJni;

    private FunctionHookLoaderJni functionHookLoaderJni;

    @PostConstruct
    public void init(){
        if(SystemUtils.isWindows()){
            utilityJni = new UtilityJni();
            functionHookLoaderJni = new FunctionHookLoaderJni();
        }
    }

    @Override
    public String listAllDevices() {
        int blockRef = utilityJni.ListAllDevices();
        int blockSize = utilityJni.GetBlockSize(blockRef);
        byte[] buffer = new byte[blockSize];
        utilityJni.ReadBlock(blockRef, buffer,0, blockSize);
        utilityJni.FreeBlock(blockRef);
        return new String(buffer);
    }

    @Override
    public String listAllProcesses() {
        int blockRef = utilityJni.ListAllProcesses();
        int blockSize = utilityJni.GetBlockSize(blockRef);
        byte[] buffer = new byte[blockSize];
        utilityJni.ReadBlock(blockRef, buffer,0, blockSize);
        utilityJni.FreeBlock(blockRef);
        return new String(buffer);
    }

    @Override
    public String queryProcessDetails(List<Integer> pids) {
        StringBuilder sb = new StringBuilder();
        StringUtils.join(pids, ',', Integer::toUnsignedString, sb);
        String pidsStr = sb.toString();
        int blockRef = utilityJni.QueryProcessDetails(pidsStr);
        int blockSize = utilityJni.GetBlockSize(blockRef);
        byte[] buffer = new byte[blockSize];
        utilityJni.ReadBlock(blockRef, buffer,0, blockSize);
        utilityJni.FreeBlock(blockRef);
        return new String(buffer);
    }

    @Override
    public String queryLinkInfos(List<String> links) {
        int blockRef = utilityJni.QueryLinkInfos(StringUtils.join(links, '|'));
        int blockSize = utilityJni.GetBlockSize(blockRef);
        byte[] buffer = new byte[blockSize];
        utilityJni.ReadBlock(blockRef, buffer,0, blockSize);
        utilityJni.FreeBlock(blockRef);
        return new String(buffer);
    }

    @Override
    public boolean attachAndInjectHooks(int pid) {
        return functionHookLoaderJni.AttachAndInjectHooks(pid) == 0;
    }

    @Override
    public boolean removeHooks(int pid) {
        return functionHookLoaderJni.RemoveHooks(pid) == 0;
    }

    @Override
    public int startProcessWithHooksA(String commandLine, String currentDirectory) {
        return functionHookLoaderJni.StartProcessWithHooksA(commandLine, currentDirectory);
    }

}
