package com.idaoben.web.monitor;

import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MonitorApplication.class)
public class LinuxProcessTests {

    private static final Logger logger = LoggerFactory.getLogger(LinuxProcessTests.class);

    @Resource
    private SystemOsService systemOsService;

    @Test
    public void testListProcess(){
        List<ProcessJson> processJsons = systemOsService.listAllProcesses();
        processJsons.forEach(processJson -> logger.info(processJson.toString()));
    }

    @Test
    public void testStartProcessWithHooks(){
        int pid = systemOsService.startProcessWithHooks("firefox -new-window", null);
        logger.info("PID: {}", pid);
    }

    @Test
    public void testGetActionFolderPath(){
        logger.info("Action folder: {}", systemOsService.getActionFolderPath());
    }
}
