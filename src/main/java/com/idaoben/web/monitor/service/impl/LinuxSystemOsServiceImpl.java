package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.ActionType;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.DeviceFileUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.DeviceInfoJson;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
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

    private static final String TRACER_CMD = System.getProperty("user.dir") + "/res/exe/tracer";

    private Map<Integer, Pair<Boolean, Process>> pidTracerProcessMap = new ConcurrentHashMap<>();

    private static String[] sensitivityPaths;

    private static final String DEVICE_TYPE_SEPARATOR = "/dev/";

    private static final String PROCESS_TYPE_SEPARATOR = "/dev/shm/";

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
        BufferedReader reader = null;
        try {
            String[] cmdArray ={"/bin/bash", "-c", String.format("%s %s", TRACER_CMD, commandLine)};
            Process process = Runtime.getRuntime().exec(cmdArray);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
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
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return -1;
    }

    @Override
    public String attachAndInjectHooks(int pid) {
        BufferedReader reader = null;
        try {
            String[] cmdArray ={"/bin/bash", "-c", String.format("%s -a %d", TRACER_CMD, pid)};
            Process process = Runtime.getRuntime().exec(cmdArray);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
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
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return ErrorCode.MONITOR_PROCESS_ERROR_LINUX;
    }

    @Override
    public boolean removeHooks(int pid) {
        Pair<Boolean, Process> processPair = pidTracerProcessMap.remove(pid);
        if(processPair != null){
            if(processPair.getLeft()){
                //User kill -9 to kill the process
                try {
                    Runtime.getRuntime().exec(String.format("kill -9 %d", pid));
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
        return null;
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
        String path = action.getPath();
        if(path.startsWith(DEVICE_TYPE_SEPARATOR)){
            if(path.startsWith(PROCESS_TYPE_SEPARATOR)){
                action.setType(ActionType.PROCESS_SHARE_MEMORY);
                action.setCmdLine(path);
                //Path is no the same for process
                action.setPath("");
                return ActionGroup.PROCESS;
            } else {
                action.setDeviceName(path);
                return ActionGroup.DEVICE;
            }
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
