package com.idaoben.web.monitor;

import com.idaoben.web.monitor.utils.AESUtils;
import io.swagger.models.auth.In;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

/**
 * @author hezifeng
 * @create 2022/8/23 11:05
 */
public class GenerateRegisterCode {

    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(GenerateRegisterCode.class.getClassLoader().getResourceAsStream("application.yaml"));
        if (!map.containsKey("activation-file-path")) {
            throw new RuntimeException("请配置: activation-file-path（激活码文件路径）");
        }
        if (!map.containsKey("encode-rules")) {
            throw new RuntimeException("请配置: encode-rules");
        }
        if (!map.containsKey("register-code-path")) {
            throw new RuntimeException("请配置: register-code-path（注册码文件路径）");
        }

        String activationPath = (String) map.get("activation-file-path");
        String registerPath = (String) map.get("register-code-path");
        String encodeRules = (String) map.get("encode-rules");
        String mac = null;
        String cpuId = null;
        String activationKey = null;
        try {
            mac = getMac();
            cpuId = getCupId();
            activationKey = getActivationKey(encodeRules, activationPath);
            if (!createRegisterCode(activationKey, cpuId, mac, encodeRules, registerPath)) {
                System.out.println("生成注册文件失败！");
                return;
            } else {
                System.out.println("成功生成注册文件!" + registerPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMac() throws IOException {
        InetAddress ip4 = Inet4Address.getLocalHost();
        byte[] mac = NetworkInterface.getByInetAddress(ip4).getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            if (i!=0) {
                sb.append("-");
            }
            //字节转整数
            int temp = mac[i]&0xff;
            //把无符号整数参数锁表示的值转换成十六进制表示的字符串
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0"+str);
            }else {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    public static String getCupId() throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream());
        String property = sc.next();
        String serial = sc.next();
        return serial;
    }

    public static String getActivationKey(String encodeRules, String activationPath) throws IOException {
        BufferedReader reader = null;
        StringBuilder key = new StringBuilder();
        //校验激活码
        File file = new File(activationPath);
        if (!file.exists()) {
            throw new RemoteException("激活码不存在!");
        }
        reader = new BufferedReader(new FileReader(file));
        String tempStr = null;
        while ((tempStr = reader.readLine()) != null) {
            key.append(tempStr);
        }
        reader.close();
        return AESUtils.AESDecode(encodeRules,key.toString());
    }

    public static boolean createRegisterCode(String key, String cpuId, String mac, String encodeRules, String registerPath) throws IOException {
        if (!key.startsWith("Company:")) {
            System.out.println("激活码校验失败!");
            return false;
        }
        String companyName = key.substring(key.indexOf(":") + 1, key.indexOf("&"));
        String endStr = key.substring(key.indexOf("&") + 1, key.indexOf("#"));
        LocalDate endDate = LocalDate.parse(endStr);
        if (LocalDate.now().isAfter(endDate)) {
            System.out.println("激活文件已过期，请重新生成激活文件");
            return false;
        }
        if (Integer.parseInt(key.substring(key.indexOf("@") + 1)) < 1) {
            System.out.println("激活文件使用次数不能小于1!");
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String creationTime = formatter.format(LocalDateTime.now());
        String registerCode = String.format("%s:%s:%s:%s:%s", companyName, creationTime, key.substring(key.indexOf("#") + 1), cpuId, mac);
        byte[] aesEncode = AESUtils.AESEncode(encodeRules, registerCode);
        OutputStream fos = null;
        File file = new File(registerPath);
        if(!file.getParentFile().exists() && !file.getParentFile().mkdir()) return false;
        fos = new FileOutputStream(file);
        fos.write(aesEncode);
        fos.flush();
        fos.close();
        return true;
    }
}
