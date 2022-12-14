package com.idaoben.web.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.ActionType;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.DeviceFileUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static String[] sensitivityPaths;

    private static String[] ignoreSensitivityPaths;

    private static final String[] FILE_TYPE_SEPARATORS = new String[]{"\\??\\", "\\\\?\\"};

    private Map<Integer, Long> pidCpuTimeMap = new HashMap<>();

    private Map<Integer, String> pidUserMape = new HashMap<>();

    private List<ProcessJson> processCache;

    @PostConstruct
    public void init(){
        sensitivityPaths = new String[]{(SystemUtils.getOsHome() + "\\WINDOWS\\").toLowerCase()};
        ignoreSensitivityPaths = new String[]{(SystemUtils.getOsHome() + "\\WINDOWS\\Fonts\\").toLowerCase()};
    }

    @Override
    public String getActionFolderPath() {
        return SystemUtils.getOsHome() + "\\WinMonitor";
    }

    @Override
    public List<SoftwareDto> getSystemSoftware() {
        String[] startMenuHomes = new String[]{SystemUtils.getOsHome() + "\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs", SystemUtils.getUserHome() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs"};
        List<SoftwareDto> softwares = new ArrayList<>();
        List<File> linkFiles = new ArrayList<>();
        for(String startMenuHome : startMenuHomes){
            File startMenu = new File(startMenuHome);

            if(startMenu.exists() && startMenu.isDirectory()){
                for(File file : startMenu.listFiles()){
                    checkAndAddFile(file, linkFiles);
                }
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
            SoftwareDto software = getSoftwareInfo(linkFile, linkFileJsonMap.get(linkFile.getPath()));
            if(software != null){
                softwares.add(software);
            }
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
            //????????????exe?????????????????????
            if(!file.getName().endsWith(".exe")){
                return null;
            }
            softwareDto.setCommandLine(linkFileJson.getPath() + linkFileJson.getArguments());
            softwareDto.setExePath(linkFileJson.getPath());
            softwareDto.setBase64Icon(getIconBase64(file));
            softwareDto.setExecutePath(linkFileJson.getWorkingDirectory());
            softwareDto.setExeName(file.getName());
        }
        return softwareDto;
    }

    public String getIconBase64(File file){
        //????????????
        ByteArrayOutputStream os = null;
        try {
            ImageIcon icon = (ImageIcon) fileSystemView.getSystemIcon(file);
            if(icon == null){
                return null;
            }
            Image image = icon.getImage();

            // ?????? Image ????????????????????????
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.getGraphics();
           //?????? BufferedImage ????????????????????????????????????
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
                //??????CPU?????????
                //1??????????????????????????????cpu???????????????deltaCpuTime???pid???0????????????????????????
                //2????????????????????????cpu??????????????? pidDeltaCpuTime / deltaCpuTime * 100% ?????????????????????cpu?????????
                long deltaCpuTime = 0;
                Map<Integer, Long> pidDeltaCpuTimeMap = new HashMap<>();
                for(ProcessJson processJson : processListJson.getProcesses()){
                    long lastCpuTime = pidCpuTimeMap.getOrDefault(processJson.getPid(), 0l);
                    long thisCpuTime = Long.parseLong(processJson.getCpuTime());
                    long pidDeltaCpuTime =  thisCpuTime > lastCpuTime ? thisCpuTime - lastCpuTime : 0l;
                    deltaCpuTime += pidDeltaCpuTime;
                    pidCpuTimeMap.put(processJson.getPid(), thisCpuTime);
                    pidDeltaCpuTimeMap.put(processJson.getPid(), pidDeltaCpuTime);
                }

                //?????????windows 7???????????????????????????????????????????????????????????????????????????????????????
                List<Integer> needGetUserPids = new ArrayList<>();
                for(ProcessJson processJson : processListJson.getProcesses()){
                    if(StringUtils.isEmpty(processJson.getUser())){
                        String user = pidUserMape.get(processJson.getPid());
                        if(user == null){
                            needGetUserPids.add(processJson.getPid());
                        } else {
                            processJson.setUser(user);
                        }
                    }
                    processJson.setCpuTime(new BigDecimal(pidDeltaCpuTimeMap.get(processJson.getPid()) * 100).divide(new BigDecimal(deltaCpuTime), 1, RoundingMode.HALF_UP).toString());
                }

                if(!needGetUserPids.isEmpty()){
                    List<ProcessJson> processUsers = getProcessByPids(needGetUserPids);
                    if(processUsers != null){
                        for(ProcessJson process : processUsers){
                            pidUserMape.put(process.getPid(), process.getUser());
                        }
                    }
                    //????????????????????????
                    for(ProcessJson processJson : processListJson.getProcesses()){
                        if(StringUtils.isEmpty(processJson.getUser())){
                            String user = pidUserMape.get(processJson.getPid());
                            if(user != null){
                                processJson.setUser(user);
                            }
                        }
                    }
                }
                processCache = processListJson.getProcesses();
                return processCache;
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<ProcessJson> getProcessByPids(List<Integer> pids) {
        String processDetailContent = jniService.queryProcessDetails(pids);
        try {
            ProcessDetailsJson processDetailsJson = objectMapper.readValue(processDetailContent, ProcessDetailsJson.class);
            if(processDetailsJson != null){
                List<ProcessJson> processes = processDetailsJson.getDetails();
                return processes;
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
    public String attachAndInjectHooks(int pid) {
        boolean result = jniService.attachAndInjectHooks(pid);
        return result ? null : ErrorCode.MONITOR_PROCESS_ERROR_WINDOWS;
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
            //????????????????????????
            String allDevicesJson = jniService.listAllDevices();
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
                //????????????????????????????????????????????????????????????????????????
                deviceInfoMap.put(instanceId, null);
                logger.info("?????????????????????{}", instanceId);
            }
        } else {
            deviceInfoJson = deviceInfoMap.get(instanceId);
        }
        if(deviceInfoJson == null){
            deviceInfoJson = new DeviceInfoJson();
            deviceInfoJson.setInstanceId(instanceId);
            deviceInfoJson.setFriendlyName("????????????");
        }
        return deviceInfoJson;
    }

    @Override
    public boolean isExeFile(File file) {
        if(!file.isDirectory() && file.getName().endsWith(".exe")){
            return true;
        }
        return false;
    }

    @Override
    public FileSensitivity getFileSensitivity(String path) {
        if(StringUtils.startsWithAny(path.toLowerCase(), sensitivityPaths) && !StringUtils.startsWithAny(path.toLowerCase(), ignoreSensitivityPaths)){
            return FileSensitivity.HIGH;
        }
        return FileSensitivity.LOW;
    }

    @Override
    public ActionGroup setActionFromFileInfo(Action action) {
        String path = action.getPath();
        if(StringUtils.startsWithAny(path, FILE_TYPE_SEPARATORS)){
            for(String filePrefix : FILE_TYPE_SEPARATORS){
                if(path.startsWith(filePrefix)){
                    path = StringUtils.substringAfter(path, filePrefix);
                    action.setPath(path);
                }
            }
            //???????????????+??????????????????????????????????????????????????????????????????
            if(path.length() < 2 || path.charAt(1) != ':'){
                String instanceId = StringUtils.substringBeforeLast(path, "\\").replace("#", "\\").toUpperCase();
                //????????????????????????????????????????????????
                instanceId = StringUtils.substringBeforeLast(instanceId, "\\");
                action.setDeviceName(getDeviceInfo(instanceId).getFriendlyName());
                return ActionGroup.DEVICE;
            }

            //??????????????????????????????????????????????????? ?????????\??\C:\Windows\system32\spool\PRINTERS\00005.SPL
            if(action.getFileName().endsWith(".SPL") && action.getPath().contains("spool\\PRINTERS")){
                action.setDeviceName("???????????????");
                return ActionGroup.DEVICE;
            }
        } else {
            //???????????????????????????????????????
            action.setDeviceName(action.getPath());
            return ActionGroup.DEVICE;
        }
        return ActionGroup.FILE;
    }

    @Override
    public FileAccess getFileAccess(Long accessLong) {
        return DeviceFileUtils.getWindowsFileAccess(accessLong);
    }

    @Override
    public void setActionProcessInfo(Action action, String pid) {
        action.setActionGroup(ActionGroup.PROCESS);
        if(action.getType() == ActionType.PROCESS_MESSAGE_SEND){
            //???????????????????????????????????????action???
            List<ProcessJson> tempProcessCache = processCache;
            if(action.getDestPid() != null && tempProcessCache != null){
                for(ProcessJson process : tempProcessCache){
                    if(process.getPid().intValue() == action.getDestPid().intValue()){
                        action.setDestPName(process.getImageName());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getUserById(String userId) {
        return userId;
    }

    @Override
    public String getGroupById(String groupId) {
        return groupId;
    }
}
