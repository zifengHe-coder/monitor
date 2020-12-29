package com.idaoben.web.monitor.config;

import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.application.ActionApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class AfterApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ActionApplicationService.class);

    @Value("${monitor.auto-open-browser}")
    private boolean autoOpenBrowser;

    @Value("${server.port:8080}")
    private int port;

    @EventListener({ApplicationReadyEvent.class})
    public void openBrowser() {
        try{
            String url = String.format("http://127.0.0.01:%d/index.html", port);
            if(autoOpenBrowser && SystemUtils.isWindows()){
                if(Desktop.isDesktopSupported()){
                    Desktop.getDesktop().browse(new URI(url));
                }else{
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
            }
            String browserUrl = url;
            if(SystemUtils.isLinux()){
                String ip = SystemUtils.getIpAddress();
                if(StringUtils.isNotEmpty(ip)){
                    browserUrl = String.format("http://%s:%d/index.html", ip, port);
                }
            }
            String message = String.format("启动成功，请打开浏览器并访问：%s", browserUrl);
            System.out.println(message);
            logger.info(message);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }

    }

}
