package com.idaoben.web.monitor.config;

import com.idaoben.web.monitor.GenerateRegisterCode;
import com.idaoben.web.monitor.dao.entity.RegisterRecord;
import com.idaoben.web.monitor.service.RegisterRecordService;
import com.idaoben.web.monitor.utils.AESUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.application.ActionApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@Component
public class AfterApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ActionApplicationService.class);

    @Value("${monitor.auto-open-browser}")
    private boolean autoOpenBrowser;

    @Value("${server.port:8080}")
    private int port;

    @Value("${register-code-path}")
    private String registerCodePath;

    @Value("${encode-rules}")
    private String encodeRules;

    @Resource
    private RegisterRecordService registerRecordService;

    @EventListener({ApplicationReadyEvent.class})
    public void openBrowser() {
        try{
            boolean exit = false;
            try {
                String registerCode = readRegisterCode(registerCodePath);
                if (StringUtils.isNotBlank(registerCode)) {
                    //注册文件校验通过,开始解析并校验注册码
                    String[] split = registerCode.split(";");
                    if (split.length != 5) {
                        System.out.println("激活码校验失败!");
                        exit = true;
                    } else {
                        if (!GenerateRegisterCode.getCupId().equals(split[3])) {
                            System.out.println("cpuId validate error");
                            exit = true;
                        } else if (!GenerateRegisterCode.getMac().equals(split[4])) {
                            System.out.println("mac address validate error");
                            exit = true;
                        } else if (!consumeRegisterCode(split[0], split[1], split[2])){
                            //校验通过，数据库冗余当前使用次数
                            System.out.println("注册码剩余使用次数不足，请重新生成激活码以生成新注册文件");
                            exit = true;
                        }
                    }
                } else {
                    exit = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                exit = true;
            }
            if (exit) {
                System.out.println("10秒后自动关闭程序!");
                Thread.sleep(10000);
                System.exit(0);
            } else {
                System.out.println("注册码文件校验通过!");
            }


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

    private String readRegisterCode(String registerCodePath) throws Exception {
        File registerFile = new File(registerCodePath);
        if (!registerFile.exists()) {
            System.out.println("注册文件不存在，请先生成注册文件!");
            return null;
        } else {
            long fileSize = registerFile.length();
            if (fileSize > Integer.MAX_VALUE) {
                System.out.println("file too big...");
                return null;
            }
            FileInputStream file = new FileInputStream(registerFile);
            byte[] buffer= new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length && (numRead = file.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset != buffer.length) {
                System.out.println("could not completely read file" + registerCodePath);
                file.close();
                return null;
            }
            return AESUtils.AESDecodeByBytes(encodeRules, buffer);
        }
    }

    private boolean consumeRegisterCode(String companyName, String creationTime, String count) {
        int c = Integer.parseInt(count);
        RegisterRecord record = registerRecordService.findByCompanyAndCT(companyName, creationTime);
        if (record == null) {
            record = new RegisterRecord();
            record.setCompanyName(companyName);
            record.setCreationTime(creationTime);
            record.setModifyTime(LocalDateTime.now());
            record.setCount(c);
            record.setNumber(1);
            registerRecordService.save(record);
        } else {
            if (record.getCount() < record.getNumber() + 1) {
                return false;
            } else {
                record.setModifyTime(LocalDateTime.now());
                record.setNumber(record.getNumber() + 1);
                registerRecordService.save(record);
            }
        }
        return true;
    }

}
