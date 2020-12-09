package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LinuxSystemOsServiceImpl implements SystemOsService {

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
        return new ArrayList<>();
    }
}
