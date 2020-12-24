package com.idaoben.web.monitor.exception;

import com.idaoben.web.common.api.bean.CommonErrorCode;

public class ErrorCode extends CommonErrorCode {

    public static final String MONITOR_ON_GOING = "2001";
    public static final String SOFTWARE_ALREADY_EXISTS = "2002";
    public static final String SOFTWARE_NOT_RUNING = "2003";
    public static final String SOFTWARE_RUNING = "2004";
    public static final String START_AND_MONITOR_ERROR = "2005";
    public static final String BACKUP_FILE_NOT_FOUND = "2006";
    public static final String MONITOR_PROCESS_ERROR_LINUX = "2007";
    public static final String MONITOR_PROCESS_ERROR_WINDOWS = "2008";
    public static final String SOFTWARE_REMOVE_MONITORING_ERROR = "2009";
    public static final String TASK_DELETE_RUNING_ERROR = "2010";
}
