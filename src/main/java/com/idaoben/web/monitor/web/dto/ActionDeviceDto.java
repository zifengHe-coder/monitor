package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

@ApiModel
@EnableAssembling
public interface ActionDeviceDto {

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

    @ApiModelProperty("进程调用类型：16384:启动进程, 20480:进程注入, 20481：消息通讯")
    @Mapping
    Integer getType();

    @ApiModelProperty("设备名称")
    @Mapping
    String getDeviceName();

    @ApiModelProperty("设备ID")
    @Mapping("path")
    String getDeviceId();
}
