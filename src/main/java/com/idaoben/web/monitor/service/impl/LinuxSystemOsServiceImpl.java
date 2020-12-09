package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.service.SystemOsService;
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

public class LinuxSystemOsServiceImpl implements SystemOsService {

    private static final Logger logger = LoggerFactory.getLogger(LinuxSystemOsServiceImpl.class);

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
                    if(temp != ' '){
                        builder.append(temp);
                    } else {
                        if(builder.length() > 0 && params.size() <= 4){
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
}
