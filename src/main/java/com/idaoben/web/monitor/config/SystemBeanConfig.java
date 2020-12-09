package com.idaoben.web.monitor.config;

import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.impl.LinuxSystemOsServiceImpl;
import com.idaoben.web.monitor.service.impl.WindowsSystemOsServiceImpl;
import com.idaoben.web.monitor.utils.SystemUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemBeanConfig {

    @Bean
    public SystemOsService systemOsService() {
        if(SystemUtils.getSystemOs() == SystemOs.WINDOWS){
            return new WindowsSystemOsServiceImpl();
        } else {
            return new LinuxSystemOsServiceImpl();
        }
    }
}
