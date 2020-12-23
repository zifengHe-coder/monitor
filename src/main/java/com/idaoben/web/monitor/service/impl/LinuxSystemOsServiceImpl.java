package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.ActionType;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.DeviceFileUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.DeviceInfoJson;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinuxSystemOsServiceImpl implements SystemOsService {

    private static final Logger logger = LoggerFactory.getLogger(LinuxSystemOsServiceImpl.class);

    public static final String TRACER_CMD = System.getProperty("user.dir") + "/res/exe/tracer";

    private Map<Integer, Pair<Boolean, Process>> pidTracerProcessMap = new ConcurrentHashMap<>();

    private static String[] sensitivityPaths;

    private static final String DEVICE_TYPE_SEPARATOR = "/dev/";

    private static final String PROCESS_TYPE_SEPARATOR = "/dev/shm/";

    private static final String SYS_DEVICE_TYPE_SEPARATOR = "/sys/devices/";

    private static final String PRINTER_DEVICE_KEYWORD = "cups.so";

    @Resource
    private MonitoringService monitoringService;

    @PostConstruct
    public void init(){
        sensitivityPaths = new String[]{"/bin", "/boot", "/data", "/root", "/sbin", "/sys"};
    }

    @Override
    public String getActionFolderPath() {
        return SystemUtils.getUserHome() + "/.tracer";
    }

    @Override
    public List<SoftwareDto> getSystemSoftware() {
        return new ArrayList<>();
    }

    @Override
    public String getIconBase64(File file) {
        return null;
    }

    @Override
    public List<ProcessJson> listAllProcesses() {
        List<ProcessJson> processJsons = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("ps -e -o pid,ppid,user,pcpu,size,cmd");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            //first line is title, ignore it.
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<StringBuilder> params = new ArrayList<>();
                StringBuilder builder = new StringBuilder();
                params.add(builder);
                for(int i = 0, size = line.length(); i < size; i++){
                    char temp  = line.charAt(i);
                    if(temp != ' ' || params.size() == 6){
                        builder.append(temp);
                    } else {
                        if(builder.length() > 0){
                            builder = new StringBuilder();
                            params.add(builder);
                        }
                    }
                }
                ProcessJson processJson = new ProcessJson();
                processJson.setPid(Integer.parseInt(params.get(0).toString()));
                processJson.setParentPid(Integer.parseInt(params.get(1).toString()));
                processJson.setUser(params.get(2).toString());
                processJson.setCpuTime(params.get(3).toString());
                processJson.setWsPrivateBytes(params.get(4).toString());
                processJson.setImageName(params.get(5).toString());
                processJsons.add(processJson);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return processJsons;
    }

    @Override
    public int startProcessWithHooks(String commandLine, String currentDirectory) {
        try {
            String cmd = String.format("%s -- %s", TRACER_CMD, commandLine);
            logger.info("startProcessWithHooks cmd: {}", cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                //Console will show the success pid, eg: {"pid":"18205"}
                String pidPrefix = "{\"pid\":\"";
                if(line.startsWith(pidPrefix)){
                    int pid = Integer.parseInt(line.substring(8, line.length() - 2));
                    pidTracerProcessMap.put(pid, Pair.of(true, process));
                    return pid;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public String attachAndInjectHooks(int pid) {
        try {
            String cmd = String.format("%s -a %d", TRACER_CMD, pid);
            logger.info("attachAndInjectHooks cmd: {}", cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                //Console will show the success pid, eg: {"pid":"18205"}
                String pidPrefix = "{\"pid\":\"";
                if(line.startsWith(pidPrefix)){
                    pidTracerProcessMap.put(pid, Pair.of(false, process));
                    return null;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return ErrorCode.MONITOR_PROCESS_ERROR_LINUX;
    }

    @Override
    public boolean removeHooks(int pid) {
        Pair<Boolean, Process> processPair = pidTracerProcessMap.remove(pid);
        if(processPair != null){
            if(processPair.getLeft()){
                //Use kill -9 to kill the tracer process and all app process
                try {
                    String softwareId = monitoringService.getMonitoringSoftwareIdByPid(String.valueOf(pid));
                    if(softwareId != null){
                        List<ProcessJson> processJsons = monitoringService.getProcessPids(softwareId);
                        if(processJsons != null){
                            for(ProcessJson processJson : processJsons){
                                Runtime.getRuntime().exec(String.format("kill -9 %d", processJson.getPid()));
                            }
                        }
                    }
                    Runtime.getRuntime().exec(String.format("kill -9 %d", processPair.getRight().pid()));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                processPair.getRight().destroy();
            }
        }
        return true;
    }

    @Override
    public boolean isAutoMonitorChildProcess() {
        return false;
    }

    @Override
    public DeviceInfoJson getDeviceInfo(String instanceId) {
        if(instanceId == null){
            return null;
        }
        DeviceInfoJson deviceInfo = new DeviceInfoJson();
        deviceInfo.setInstanceId(instanceId);
        if(instanceId.startsWith("/dev/video")){
            deviceInfo.setFriendlyName("摄像设备");
        } else if(instanceId.startsWith("/dev/audio")){
            deviceInfo.setFriendlyName("音频设备");
        } else if(instanceId.startsWith("/dev")){
            deviceInfo.setFriendlyName("未知设备");
        }
        if(deviceInfo.getFriendlyName() == null){
            logger.info("检测到设备控制行为，但设备路径不符合：{}", instanceId);
            return null;
        }
        return deviceInfo;
    }

    @Override
    public boolean isExeFile(File file) {
        String fileName = file.getName();
        if(!file.isDirectory() && (fileName.endsWith(".sh") || !fileName.contains("."))){
            return true;
        }
        return false;
    }

    @Override
    public FileSensitivity getFileSensitivity(String path) {
        if(StringUtils.startsWithAny(path.toLowerCase(), sensitivityPaths)){
            return FileSensitivity.HIGH;
        }
        return FileSensitivity.LOW;
    }

    @Override
    public ActionGroup setActionFromFileInfo(Action action) {
        //Linux device don't handler from file except printer
        String path = action.getPath();
        if(path.startsWith(DEVICE_TYPE_SEPARATOR)){
            if(path.startsWith(PROCESS_TYPE_SEPARATOR)){
                action.setType(ActionType.PROCESS_SHARE_MEMORY);
                action.setCmdLine(path);
                //Path is no the same for process
                action.setPath("");
                return ActionGroup.PROCESS;
            } else {
                return null;
            }
        } else if(path.startsWith(SYS_DEVICE_TYPE_SEPARATOR)){
            return null;
        } else if(path.contains(PRINTER_DEVICE_KEYWORD)){
            action.setDeviceName("打印设备");
            return ActionGroup.DEVICE;
        }
        return ActionGroup.FILE;
    }

    @Override
    public FileAccess getFileAccess(Long accessLong) {
        return DeviceFileUtils.getLinuxFileAccess(accessLong);
    }

    @Override
    public void setActionProcessInfo(Action action, String pid) {
        action.setActionGroup(ActionGroup.PROCESS);
        if(action.getType() == ActionType.PROCESS_OPEN_LINUX){
            action.setType(ActionType.PROCESS_OPEN);
            action.setCmdLine(String.format("%s %s", action.getCmd(), StringUtils.join(action.getArgs(), " ")));
        }
    }
}
