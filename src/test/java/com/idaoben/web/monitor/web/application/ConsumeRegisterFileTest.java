package com.idaoben.web.monitor.web.application;

import com.idaoben.web.monitor.MonitorApplication;
import com.idaoben.web.monitor.utils.AESUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author hezifeng
 * @create 2022/8/24 10:18
 */

public class ConsumeRegisterFileTest {
    private static final String filePath = "C:\\Users\\daoben-pc\\Desktop\\x360\\registerCode.key";
    private static final String encodeRules = "daoben";

    @Test
    public void test() {
        consumeRegisterFile(encodeRules, filePath);
    }


    private void consumeRegisterFile(String rules,String filePath) {
        File registerFile = new File(filePath);
        if (!registerFile.exists()) {
            throw new RuntimeException("注册码文件不存在，请重启系统!");
        }
        try {
            long fileSize = registerFile.length();
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException("file too big");
            }
            FileInputStream file = new FileInputStream(registerFile);
            byte[] buffer= new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length && (numRead = file.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset != buffer.length) {
                file.close();
                throw new RuntimeException("could not completely read file" + filePath);
            }
            //注册码格式{companyName};{cpuId};{mac};{count};{number}
            String registerCode = AESUtils.AESDecodeByBytes(rules, buffer);
            String strPrefix = registerCode.substring(0, registerCode.lastIndexOf(";") + 1);
            String[] split = registerCode.split(";");
            int count = Integer.parseInt(split[3]);
            int number = Integer.parseInt(split[4]);
            //每次使用次数+1
            number++;
            if (count < number) {
                throw new RuntimeException("监控次数已用完，请重新申请激活码!");
            } else {
                strPrefix += number;
                System.out.println("更新后注册码:" + strPrefix);
                byte[] aesEncode = AESUtils.AESEncode(rules, strPrefix);
                OutputStream fos = new FileOutputStream(registerFile);
                fos.write(aesEncode);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
