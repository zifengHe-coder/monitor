package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.jni.UtilityJni;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class JniServiceImpl implements JniService {

    private UtilityJni utilityJni;

    @PostConstruct
    public void init(){
        utilityJni = new UtilityJni();
    }

    @Override
    public int listAllProcesses() {
        return utilityJni.ListAllProcesses();
    }

}
