package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.jni.FunctionHookLoaderJni;
import com.idaoben.web.monitor.jni.UtilityJni;
import com.idaoben.web.monitor.service.JniService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class JniServiceImpl implements JniService {

    private UtilityJni utilityJni;

    private FunctionHookLoaderJni functionHookLoaderJni;

    @PostConstruct
    public void init(){
        utilityJni = new UtilityJni();
        functionHookLoaderJni = new FunctionHookLoaderJni();
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
    public boolean attachAndInjectHooks(int pid) {
        return functionHookLoaderJni.AttachAndInjectHooks(pid) == 0;
    }

    @Override
    public boolean removeHooks(int pid) {
        return functionHookLoaderJni.RemoveHooks(pid) == 0;
    }

    @Override
    public boolean startProcessWithHooksA(String commandLine, String currentDirectory) {
        return functionHookLoaderJni.StartProcessWithHooksA(commandLine, currentDirectory) == 0;
    }

}
