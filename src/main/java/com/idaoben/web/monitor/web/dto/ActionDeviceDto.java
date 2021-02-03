package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@EnableAssembling
public interface ActionDeviceDto extends ActionBaseDto{

    @ApiModelProperty("设备名称")
    @Mapping
    String getDeviceName();

    @ApiModelProperty("设备ID")
    @Mapping("path")
    String getDeviceId();
}
