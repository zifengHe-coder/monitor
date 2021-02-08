package com.idaoben.web.monitor.utils;

import com.idaoben.web.monitor.dao.entity.enums.FileAccess;

public class DeviceFileUtils {

    private static final long WINDOWS_GENERIC_WRITE = 0x40000000L;
    private static final long WINDOWS_FILE_WRITE_DATA = 0x0002L;
    private static final long WINDOWS_FILE_APPEND_DATA = 0x0004L;
    private static final long WINDOWS_FILE_WRITE_ATTRIBUTES = 0x0100L;
    private static final long WINDOWS_FILE_DELETE = 0x00010000L;
    private static final long WINDOWS_FILE_WRITE_ACCESS = WINDOWS_GENERIC_WRITE | WINDOWS_FILE_WRITE_DATA | WINDOWS_FILE_APPEND_DATA | WINDOWS_FILE_WRITE_ATTRIBUTES | WINDOWS_FILE_DELETE;
    private static final long WINDOWS_FILE_READ_ACCESS = 0x80000000L;

    private static final long LINUX_GENERIC_WRITE = 0X1L;
    private static final long LINUX_APPEND_WRITE = 0X400L;
    private static final long LINUX_READ_WRITE = 0x2L;
    private static final long LINUX_FILE_WRITE_ACCESS = LINUX_GENERIC_WRITE | LINUX_APPEND_WRITE | LINUX_READ_WRITE;


    public static FileAccess getWindowsFileAccess(Long accessLong){
        if(accessLong != null){
            long access = accessLong.longValue();
            boolean read = (access & WINDOWS_FILE_READ_ACCESS) > 0;
            boolean write = (access & WINDOWS_FILE_WRITE_ACCESS) > 0;
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

    public static FileAccess getLinuxFileAccess(Long accessLong){
        if(accessLong != null){
            long access = accessLong.longValue();
            //读权限不设置掩码位，意思是都可以读
            boolean read = true;
            boolean write = (access & LINUX_FILE_WRITE_ACCESS) > 0;
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
