package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String TRACER_CMD = System.getProperty("user.dir") + "/src/main/resources/exe/tracer";

    private Map<Integer, Process> pidTracerProcessMap = new ConcurrentHashMap<>();

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
            Process process = Runtime.getRuntime().exec("ps -e -o pid,ppid,pcpu,size,cmd");
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
                    if(temp != ' ' || params.size() == 5){
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
                processJson.setCpuTime(params.get(2).toString());
                processJson.setWsPrivateBytes(params.get(3).toString());
                processJson.setImageName(params.get(4).toString());
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
            Process process = Runtime.getRuntime().exec(String.format("%s %s", TRACER_CMD, commandLine));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                //Console will show the success pid, eg: {"pid":"18205"}
                String pidPrefix = "{\"pid\":\"";
                if(line.startsWith(pidPrefix)){
                    int pid = Integer.parseInt(line.substring(8, line.length() - 2));
                    pidTracerProcessMap.put(pid, process);
                    return pid;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean attachAndInjectHooks(int pid) {
        try {
            Process process = Runtime.getRuntime().exec(String.format("%s -a %d", TRACER_CMD, pid));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                //Console will show the success pid, eg: {"pid":"18205"}
                String pidPrefix = "{\"pid\":\"";
                if(line.startsWith(pidPrefix)){
                    pidTracerProcessMap.put(pid, process);
                    return true;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean removeHooks(int pid) {
        Process process = pidTracerProcessMap.get(pid);
        if(process != null){
            process.destroy();
        }
        return true;
    }
}
