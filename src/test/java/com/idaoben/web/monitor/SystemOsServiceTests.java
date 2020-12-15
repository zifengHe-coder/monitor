package com.idaoben.web.monitor;

import com.idaoben.web.monitor.service.SystemOsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MonitorApplication.class)
public class SystemOsServiceTests {

    @Resource
    private SystemOsService systemOsService;

    @Test
    public void testListAllDevices(){
        systemOsService.getDeviceInfo("test");
    }
}
