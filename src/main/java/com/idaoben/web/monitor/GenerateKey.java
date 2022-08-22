package com.idaoben.web.monitor;

import org.springframework.util.Base64Utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author hezifeng
 * @create 2022/8/22 15:18
 */
public class GenerateKey {
    private static final String encodeRules = "daoben";

    public static void main(String[] args) {
        System.out.println("请输入公司名称:");
        Scanner input = new Scanner(System.in);
        String companyName = input.next();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectTime = now.plusMinutes(1);
        String beginTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(now);
        String endTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(effectTime);
        String keyFormatter = "Company:%s&%s~%s&&Daoben";
        String key = AESEncode(String.format(keyFormatter, companyName, beginTime, endTime));
        System.out.println("生成激活码:" + key);
        try {
            URL url = GenerateKey.class.getResource("");
            String filePath = url.getPath().substring(10);
            System.out.println("文件路径"+ filePath);
            String activationCodePath = filePath + "register.key";
            File file = new File(activationCodePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(key);
            bufferedWriter.close();
            System.out.println("生成激活文件成功!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String AESEncode(String content) {
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
            String AES_encode = new String(Base64Utils.encode(byte_AES));
            return AES_encode;
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
        //如果有错就返加nulll
        return null;
    }
}
