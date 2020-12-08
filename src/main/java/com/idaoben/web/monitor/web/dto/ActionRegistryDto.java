package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

@ApiModel
@EnableAssembling
public interface ActionRegistryDto {

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

    @ApiModelProperty("行为类型")
    @Mapping
    Integer getType();

    @ApiModelProperty("注册表父键")
    @Mapping
    String getParent();

    @ApiModelProperty("注册表目标")
    @Mapping
    String getKey();

    @ApiModelProperty("值键")
    @Mapping
    String getValueName();

    @ApiModelProperty("值键类型")
    @Mapping
    String getValueType();

    @ApiModelProperty("值键值/发送数据(HEX编码)")
    @Mapping
    String getData();

    @ApiModelProperty("值键原类型")
    @Mapping
    String getOldValueType();

    @ApiModelProperty("值键原有值")
    @Mapping
    String getOldData();
}
