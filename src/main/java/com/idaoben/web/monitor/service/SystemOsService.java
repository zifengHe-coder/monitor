package com.idaoben.web.monitor.service;

import com.idaoben.web.monitor.web.dto.SoftwareDto;

import java.io.File;
import java.util.List;

public interface SystemOsService {

    List<SoftwareDto>  getSystemSoftware();

    String getIconBase64(File file);
}
