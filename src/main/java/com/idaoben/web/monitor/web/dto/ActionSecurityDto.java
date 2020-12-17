package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public interface ActionSecurityDto {

    @ApiModelProperty("唯一标识符")
    @Mapping
    String getUuid();

    @ApiModelProperty("关联的任务ID")
    @Mapping
    Long getTaskId();

    @ApiModelProperty("进程ID")
    @Mapping
    String getPid();

    @ApiModelProperty("日志时间戳")
    @Mapping
    ZonedDateTime getTimestamp();

    @ApiModelProperty("目标对象名")
    @Mapping
    String getTarget();

    @ApiModelProperty("目标用户")
    @Mapping
    String getOwner();

    @ApiModelProperty("目标用户组")
    @Mapping
    String getGroup();

    @ApiModelProperty("安全描述符")
    @Mapping
    String getDaclSdString();

}
