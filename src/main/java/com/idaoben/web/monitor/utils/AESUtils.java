package com.idaoben.web.monitor.utils;

import org.springframework.util.Base64Utils;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hezifeng
 * @create 2022/8/23 10:58
 */
public class AESUtils {

    public static String AESDecode(String encodeRules, String content) {
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
        //如果有错就返加null
        return null;
    }

    public static String AESDecodeByBytes(String encodeRules, byte[] bytes) {
        try {
            String AES_decode = new String(Base64.getEncoder().encode(bytes));
            return AESDecode(encodeRules, AES_decode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果有错就返加null
        return null;
    }

    public static String AESEncodeStr(String encodeRules, String content) {
        byte[] bytes = AESEncode(encodeRules, content);
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static byte[] AESEncode(String encodeRules, String content) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(encodeRules.getBytes());
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byte_encode = content.getBytes("utf-8");
            byte[] byte_AES = cipher.doFinal(byte_encode);
            return byte_AES;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //如果有错就返加null
        return null;
    }

    /**
     * 获取cpu序列
     * @return
     * @throws IOException
     */
//    public static String getCupId() throws IOException {
//        String[] linux = {"dmidecode", "-t", "processor", "|", "grep", "ID"};
//        String[] windows = {"wmic", "cpu", "get", "ProcessorId"};
//        String property = System.getProperty("os.name");
//        Process process = Runtime.getRuntime().exec(property.contains("Window")? windows : linux);
//        process.getOutputStream().close();
//        Scanner sc = new Scanner(process.getInputStream(), "utf-8");
//        sc.next();
//        String cpuId = sc.next();
//        return cpuId;
//    }

    public static String getCpuId() throws IOException {
        String property = System.getProperty("os.name");
        if (property.contains("Window")) return getCPUID_Windows();
        return getCPUID_linux();
    }

    /**
     * 获取CPU序列号 Windows
     *
     * @return
     */
    public static String getCPUID_Windows() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n" + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            System.out.print("获取cpu序列信息误");
        }
        return result.trim();
    }



    /**
     * 获取CPU序列号 linux
     *
     * @return
     */
    public static String getCPUID_linux() {
        String result = "";
        String CPU_ID_CMD = "dmidecode";
        BufferedReader bufferedReader = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[] { "sh", "-c", CPU_ID_CMD });// 管道
            bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[hwaddr]
                index = line.toLowerCase().indexOf("uuid");
                if (index >= 0) {// 找到了
                    // 取出mac地址并去除2边空格
                    result = line.substring(index + "uuid".length() + 1).trim();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.print("获取cpu序列信息错误");
        }
        return result.trim();
    }


    public static String getMac() throws IOException {
        String property = System.getProperty("os.name");
        if (property.contains("Window")) {
            return getWindowsMac();
        } else {
            return getLinuxMac();
        }
    }

    /**
     * 获取mac地址
     * @return
     * @throws IOException
     */
    public static String getWindowsMac() throws IOException {
        InetAddress ia = InetAddress.getLocalHost();
        InetAddress[] inetAddressArr = InetAddress.getAllByName(ia.getHostName());
        for (int i = 0; i < inetAddressArr.length; i++) {
            if (inetAddressArr[i].getHostAddress() != null) {
                String ip = inetAddressArr[i].getHostAddress();
                if (!(ip.endsWith(".1")|| ip.endsWith(".0") || ip.endsWith(".255"))) {
                    ia = inetAddressArr[i];
                    break;
                }
            }
        }

        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
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

    public static String getLinuxMac() throws IOException {
        String mac = "";
        Process p = new ProcessBuilder("ifconfig").start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            Pattern pat = Pattern.compile("\\b\\w+:\\w+:\\w+:\\w+:\\w+:\\w+\\b");
            Matcher mat = pat.matcher(line);
            if (mat.find()) {
                mac = mat.group();
            }
        }
        br.close();
        return mac;
    }
}
