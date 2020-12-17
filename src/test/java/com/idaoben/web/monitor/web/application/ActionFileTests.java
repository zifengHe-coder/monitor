package com.idaoben.web.monitor.web.application;

import com.idaoben.web.monitor.MonitorApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MonitorApplication.class)
public class ActionFileTests {

    @Resource
    private ActionApplicationService actionApplicationService;

    @Test
    public void testHandlePidAction(){
        actionApplicationService.handlePidAction("test", -1L, new ActionHanlderThread("test", -1L) {
            @Override
            void actionHandle(String pid, Long taskId) {

            }
        });
    }
}
