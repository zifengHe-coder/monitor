package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;

public class DeviceFileUtils {

    private static final long GENERIC_WRITE = 0x40000000L;
    private static final long FILE_WRITE_DATA = 0x0002L;
    private static final long FILE_APPEND_DATA = 0x0004L;
    private static final long FILE_WRITE_ATTRIBUTES = 0x0100L;
    private static final long FILE_DELETE = 0x00010000L;
    private static final long FILE_WRITE_ACCESS = GENERIC_WRITE | FILE_WRITE_DATA | FILE_APPEND_DATA | FILE_WRITE_ATTRIBUTES | FILE_DELETE;
    private static final long FILE_READ_ACCESS = 0x80000000L;

    public static FileAccess getFileAccess(Long accessLong){
        if(accessLong != null){
            long access = accessLong.longValue();
            boolean read = (access & FILE_READ_ACCESS) > 0;
            boolean write = (access & FILE_WRITE_ACCESS) > 0;
            if(read && write){
                return FileAccess.READ_AND_WRITE;
            } else if(read){
                return FileAccess.READ;
            } else if(write){
                return FileAccess.WRITE;
            }
        }
        return null;
    }

}
