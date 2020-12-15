package com.idaoben.web.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WindowsSystemOsServiceImpl implements SystemOsService {

    private static final Logger logger = LoggerFactory.getLogger(WindowsSystemOsServiceImpl.class);

    @Resource
    private JniService jniService;

    @Resource
    private ObjectMapper objectMapper;

    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private Map<String, DeviceInfoJson> deviceInfoMap = new HashMap<>();

    @Override
    public String getActionFolderPath() {
        return SystemUtils.getOsHome() + "\\WinMonitor";
    }

    @Override
    public List<SoftwareDto> getSystemSoftware() {
        String startMenuHome = SystemUtils.getOsHome() + "\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs";
        File startMenu = new File(startMenuHome);
        List<SoftwareDto> softwares = new ArrayList<>();
        List<File> linkFiles = new ArrayList<>();
        if(startMenu.exists() && startMenu.isDirectory()){
            for(File file : startMenu.listFiles()){
                checkAndAddFile(file, linkFiles);
            }
        }
        List<String> linkPaths = linkFiles.stream().map(File::getPath).collect(Collectors.toList());
        String content = jniService.queryLinkInfos(linkPaths);
        Map<String, LinkFileJson> linkFileJsonMap = new HashMap<>();
        try {
            LinkListJson linkListJson = objectMapper.readValue(content, LinkListJson.class);
            linkListJson.getDetails().forEach(linkFileJson -> linkFileJsonMap.put(linkFileJson.getLinkPath(), linkFileJson));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        for(File linkFile : linkFiles){
            softwares.add(getSoftwareInfo(linkFile, linkFileJsonMap.get(linkFile.getPath())));
        }
        return softwares;
    }

    private void checkAndAddFile(File file, List<File> linkFiles){
        if(!file.isDirectory()){
            if(checklnkFile(file)){
                linkFiles.add(file);
            }
        } else {
            for(File fileChild : file.listFiles()){
                checkAndAddFile(fileChild, linkFiles);
            }
        }
    }

    private boolean checklnkFile(File file){
        return file.getName().endsWith(".lnk");
    }

    private SoftwareDto getSoftwareInfo(File lnkFile, LinkFileJson linkFileJson){
        SoftwareDto softwareDto = new SoftwareDto();
        softwareDto.setId(lnkFile.getPath());
        softwareDto.setLnkPath(softwareDto.getId());
        softwareDto.setSoftwareName(lnkFile.getName().replace(".lnk", ""));
        if(linkFileJson != null){
            File file = new File(linkFileJson.getPath());
            softwareDto.setCommandLine(linkFileJson.getPath() + linkFileJson.getArguments());
            softwareDto.setExePath(linkFileJson.getPath());
            softwareDto.setBase64Icon(getIconBase64(file));
            softwareDto.setExecutePath(linkFileJson.getWorkingDirectory());
            softwareDto.setExeName(file.getName());
        }
        return softwareDto;
    }

    public String getIconBase64(File file){
        //图标处理
        ByteArrayOutputStream os = null;
        try {
            ImageIcon icon = (ImageIcon) fileSystemView.getSystemIcon(file);
            Image image = icon.getImage();

            // 获取 Image 对象的高度和宽度
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.getGraphics();
           //通过 BufferedImage 绘制图像并保存在其对象中
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            os = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", os);
            return String.format("data:image/png;base64,%s", Base64.getEncoder().encodeToString(os.toByteArray()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(os);
        }
        return null;
    }

    @Override
    public List<ProcessJson> listAllProcesses() {
        String processContent = jniService.listAllProcesses();
        try {
            ProcessListJson processListJson = objectMapper.readValue(processContent, ProcessListJson.class);
            if(processListJson != null){
                return processListJson.getProcesses();
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int startProcessWithHooks(String commandLine, String currentDirectory) {
        return jniService.startProcessWithHooksA(commandLine, currentDirectory);
    }

    @Override
    public boolean attachAndInjectHooks(int pid) {
        return jniService.attachAndInjectHooks(pid);
    }

    @Override
    public boolean removeHooks(int pid) {
        return jniService.removeHooks(pid);
    }

    @Override
    public boolean isAutoMonitorChildProcess() {
        return true;
    }

    @Override
    public DeviceInfoJson getDeviceInfo(String instanceId) {
        DeviceInfoJson deviceInfoJson = null;
        if(!deviceInfoMap.containsKey(instanceId)){
            //重新获取一次信息
            String allDevicesJson = jniService.listAllDevices();
            logger.info("找不到对应设备{}，进行设备列表查询", instanceId);
            try {
                DeviceListJson deviceList = objectMapper.readValue(allDevicesJson, DeviceListJson.class);
                Map<String, DeviceInfoJson> deviceInfoJsonMap = new HashMap<>();
                deviceList.getDevices().forEach(deviceInfo -> {deviceInfoJsonMap.put(deviceInfo.getInstanceId(), deviceInfo);});
                deviceInfoMap = deviceInfoJsonMap;
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage(), e);
            }
            if(deviceInfoMap.containsKey(instanceId)){
                deviceInfoJson = deviceInfoMap.get(instanceId);
            } else {
                //找不到设备时传入一个空的缓存，避免下次重复查询了
                deviceInfoMap.put(instanceId, null);
            }
        } else {
            deviceInfoJson = deviceInfoMap.get(instanceId);
        }
        return deviceInfoJson;
    }
}
