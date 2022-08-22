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
import org.springframework.util.Base64Utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
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

    @Value("${monitor.activation-file-path}")
    private String activationFilePath;

    @Value("${monitor.encode-rules}")
    private String encodeRules;

    @Value("${monitor.valid-time-range:10}")
    private long validTimeRange;

    @EventListener({ApplicationReadyEvent.class})
    public void openBrowser() {
        try{
            boolean exit = false;
            if (StringUtils.isBlank(activationFilePath)) {
                exit = true;
            } else {
                File file = new File(activationFilePath);
                if (!file.exists()) {
                    exit = true;
                } else {
                    BufferedReader reader = null;
                    StringBuilder key = new StringBuilder();
                    try {
                        reader = new BufferedReader(new FileReader(file));
                        String tempStr = null;
                        while ((tempStr = reader.readLine()) != null) {
                            key.append(tempStr);
                        }
                        if (!validateKey(key.toString())) exit = true;
                        if (!exit) System.out.println("激活码校验通过!");
                        else System.out.println("激活码校验失败!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            while (exit) {
                System.out.println("10秒后自动关闭程序!");
                Thread.sleep(10000);
                System.exit(0);
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

    private boolean validateKey(String key) {
        String content = AESDecode(key);
        if (!content.startsWith("Company:")) return false;
        try {
            String beginStr = content.substring(content.indexOf("&") + 1, content.indexOf("~"));
            String endStr = content.substring(content.indexOf("~") + 1, content.lastIndexOf("&&"));
            LocalDateTime beginTime = LocalDateTime.parse(beginStr, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(endStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (LocalDateTime.now().isAfter(endTime)) {
                System.out.println("激活文件已过期，请重新生成激活文件");
                return false;
            }
            Duration duration = Duration.between(beginTime, endTime);
            if (duration.toMinutes() > validTimeRange) return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public String AESDecode(String content) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(encodeRules.getBytes());
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] byte_content = Base64Utils.decodeFromString(content);
            byte[] byte_decode = cipher.doFinal(byte_content);
            String AES_decode = new String(byte_decode, "utf-8");
            return AES_decode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        //如果有错就返加nulll
        return null;
    }

}
