package com.idaoben.web.monitor;

import com.idaoben.web.monitor.utils.DeviceFileUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void testGetFileAccess(){
        long access = 3222274176L;
        logger.info(DeviceFileUtils.getFileAccess(access).toString());
    }

    @Test
    public void testGetSensitivityPath(){
        logger.info(SystemUtils.getSensitivityPath());
    }
}
