package com.idaoben.web.monitor;

import com.idaoben.web.monitor.utils.DeviceFileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceFileUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(DeviceFileUtilsTest.class);

    @Test
    public void testGetFileAccess(){
        long access = 3222274176L;
        logger.info(DeviceFileUtils.getFileAccess(access).toString());
    }
}
