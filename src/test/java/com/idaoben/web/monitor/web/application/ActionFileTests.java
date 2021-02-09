package com.idaoben.web.monitor.web.application;

import com.idaoben.web.monitor.MonitorApplication;
import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.utils.SystemUtils;
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
        //模拟Linux环境
        SystemUtils.setSystemOs(SystemOs.LINUX);
        String pid = "test";
        Long taskId = -2l;
        actionApplicationService.handlePidAction(pid, taskId, new ActionHanlderThread(pid, taskId) {
            @Override
            void actionHandle(String pid, Long taskId) {
                actionApplicationService.handlePidAction(pid, taskId, this);
            }
        });
    }
}
