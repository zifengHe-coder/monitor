package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@EnableAssembling
public interface ActionSecurityDto extends ActionBaseDto{

    @ApiModelProperty("目标对象名")
    @Mapping
    String getTarget();

    @ApiModelProperty("目标用户")
    @Mapping
    String getOwner();

    void setOwner(String owner);

    @ApiModelProperty("目标用户组")
    @Mapping
    String getGroup();

    void setGroup(String group);

    @ApiModelProperty("Windows：安全描述符；Linux：文件权限")
    @Mapping
    String getDaclSdString();

    void setDaclSdString(String daclSdString);

}
