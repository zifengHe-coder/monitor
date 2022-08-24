package com.idaoben.web.monitor;

import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.google.common.base.Predicates;
import com.idaoben.web.common.dao.impl.BaseRepositoryImpl;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.repository.ActionRepository;
import com.idaoben.web.monitor.utils.AESUtils;
import io.swagger.models.auth.In;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
@EnableSwagger2
@EnableJpaRepositories(basePackageClasses = {ActionRepository.class}, repositoryBaseClass = BaseRepositoryImpl.class)
@EntityScan(basePackageClasses = {Action.class})
@EnableScheduling
public class MonitorApplication {

    public static void main(String[] args) {
        String registerFilePath = "";
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("-registerFilePath")) {
                    registerFilePath = arg.substring(arg.indexOf("=")+1);
                    System.out.println("注册文件路径:" + registerFilePath);
                }
            }
        }

        readRegisterFile(registerFilePath);

        //校验通过后启动程序
        SpringApplication.run(MonitorApplication.class, args);
    }

    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.or(
                        PathSelectors.ant("/api/**")))
                .build();
        return docket;
    }

    @Bean
    public CachedUidGenerator cachedUidGenerator(){
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setTimeBits(29);
        cachedUidGenerator.setWorkerBits(21);
        cachedUidGenerator.setSeqBits(13);
        cachedUidGenerator.setEpochStr("2021-02-07");
        cachedUidGenerator.setBoostPower(3);
        cachedUidGenerator.setWorkerIdAssigner(new MyWorkerIdAssigner());
        return cachedUidGenerator;
    }

    private static void readRegisterFile(String registerFilePath) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(MonitorApplication.class.getClassLoader().getResourceAsStream("application.yaml"));
        if (!map.containsKey("encode-rules")) {
            throw new RuntimeException("请配置: encode-rules");
        }
        String encodeRules = (String) map.get("encode-rules");
        try {
            File file = new File(registerFilePath);
            if (!file.exists()) {
                //注册文件不存在,需要激活码激活生成注册文件
                Map<String, String> tmpMap = new HashMap<>();
                String instrumentCode = createInstrumentCode(encodeRules);
                System.out.println(String.format("本监控模块还未激活，机器码为：%s，请先通过机器码获取激活码。 请输入激活码:", instrumentCode));
                Scanner input = new Scanner(System.in);
                while (CollectionUtils.isEmpty(tmpMap = validateActivateCode(encodeRules, input.next()))) {
                    System.out.println("请重新输入激活码:");
                }

                //首次生成注册文件,格式{companyName};{cpuId};{mac};{count};{number} 其中number为使用次数,初始为0
                String registerCode = String.format("%s;%s;%s;%s;%s", tmpMap.get("companyName"), tmpMap.get("cpuId"), tmpMap.get("mac"), tmpMap.get("count"),  "0");
                System.out.println("生成注册内容:" + registerCode);

                byte[] aesEncode = AESUtils.AESEncode(encodeRules, registerCode);
                OutputStream fos = new FileOutputStream(file);
                fos.write(aesEncode);
                fos.flush();
                fos.close();
                System.out.println("注册文件创建成功!");
            } else if (!validateRegisterCode(encodeRules, file)){
                //仅作校验，不修改文件
                System.out.println("读取注册文件失败!");
                throw new RuntimeException("读取注册文件失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    //生成激活码 cpuId;mac;有效期
    private static String createInstrumentCode(String encodeRules) throws IOException {
        String cupId = AESUtils.getCupId();
        String mac = AESUtils.getMac();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String creationTime = formatter.format(LocalDateTime.now().plusDays(1));
        String key = String.format("%s;%s;%s", cupId, mac, creationTime);
        return AESUtils.AESEncodeStr(encodeRules, key);
    }

    //激活码格式 Company:{companyName}&{count}#Daoben@{endDate}*{cpuId};{mac}
    private static Map<String, String> validateActivateCode(String encodeRules, String input) {
        try {
            Map<String, String> map = new HashMap<>();
            String content = AESUtils.AESDecode(encodeRules, input);
            String txt = content.substring(content.indexOf("*") + 1);
            String[] split = txt.split(";");
            String cpuId = split[0];
            String mac = split[1];
            if (!AESUtils.getCupId().equals(cpuId) || !AESUtils.getMac().equals(mac)) {
                System.out.println("机器信息核对失败，激活码无效!");
                return null;
            }
            String companyName = content.substring(content.indexOf("Company:") + 8, content.indexOf("&"));
            String count = content.substring(content.indexOf("&") + 1, content.indexOf("#"));
            String endDate = content.substring(content.indexOf("@") + 1, content.indexOf("*"));
            if (StringUtils.isEmpty(companyName) || Integer.parseInt(count) < 1 || LocalDate.now().isAfter(LocalDate.parse(endDate))) {
                System.out.println("激活码校验失败!");
                return null;
            }
            map.put("companyName", companyName);
            map.put("count", count);
            map.put("endDate", endDate);
            map.put("cpuId", cpuId);
            map.put("mac", mac);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("激活码校验失败!");
            return null;
        }
    }

    //{companyName};{cpuId};{mac};{count};{number}
    private static boolean validateRegisterCode(String encodeRules, File registerFile) throws IOException {
        long fileSize = registerFile.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return false;
        }
        FileInputStream file = new FileInputStream(registerFile);
        byte[] buffer= new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length && (numRead = file.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset != buffer.length) {
            System.out.println("could not completely read file" + registerFile.getPath());
            file.close();
            return false;
        }
        String content = AESUtils.AESDecodeByBytes(encodeRules, buffer);
        String[] split = content.split(";");
        int count = Integer.parseInt(split[3]);
        int number = Integer.parseInt(split[4]);
        if (number + 1 > count) {
            System.out.println(String.format("当前监控使用次数已用完，请删除原有注册文件: %s 后重新申请激活码后重启系统", registerFile.getAbsolutePath()));
        }
        return true;
    }


}
