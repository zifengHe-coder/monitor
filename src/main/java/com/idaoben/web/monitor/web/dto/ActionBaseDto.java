package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

@ApiModel
@EnableAssembling
public interface ActionBaseDto {

    @ApiModelProperty("唯一标识符")
    @Mapping
    String getUuid();

    @ApiModelProperty("关联的任务ID")
    @Mapping
    Long getTaskId();

    @ApiModelProperty("进程ID")
    @Mapping
    String getPid();

    @ApiModelProperty("用户名")
    String getUser();

    void setUser(String user);

    @ApiModelProperty("日志时间戳")
    @Mapping
    ZonedDateTime getTimestamp();
}
