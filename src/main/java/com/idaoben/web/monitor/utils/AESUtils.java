package com.idaoben.web.monitor.utils;

import org.springframework.util.Base64Utils;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

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
    public static String getCupId() throws IOException {
        String[] linux = {"dmidecode", "-t", "processor", "|", "grep", "ID"};
        String[] windows = {"wmic", "cpu", "get", "ProcessorId"};
        String property = System.getProperty("os.name");
        Process process = Runtime.getRuntime().exec(property.contains("Window")? windows : linux);
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream(), "utf-8");
        sc.next();
        return sc.next();
    }

    /**
     * 获取mac地址
     * @return
     * @throws IOException
     */
    public static String getMac() throws IOException {
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
}
