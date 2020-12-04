package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class MonitorApplicationService {

    private Set<String> monitoringIds = Collections.synchronizedSet(new HashSet<>());

    public void startMonitor(SoftwareIdCommand command){
        if(monitoringIds.contains(command)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //启动监听
        //TODO: 启动监听并根据result返回相关信息
        int result = 0;
        if(result == 0){
            monitoringIds.add(command.getId());
        }
    }

    public void stopMonitor(SoftwareIdCommand command){
        if(monitoringIds.contains(command.getId())){
            //TODO: 关闭监听
            monitoringIds.remove(command.getId());
        }
    }

    public void startAndMonitor(SoftwareIdCommand command){

    }
}
