package com.idaoben.web.monitor.web.application;

import com.idaoben.web.monitor.service.JniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SchedularApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SchedularApplicationService.class);

    @Resource
    private JniService jniService;

    //@Scheduled(cron = "*/1 * * * * ?")
    public void refreshProcess() {
        logger.info("" + jniService.listAllProcesses());
    }
}
