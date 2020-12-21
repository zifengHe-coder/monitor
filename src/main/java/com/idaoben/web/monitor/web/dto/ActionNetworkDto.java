package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@EnableAssembling
public interface ActionNetworkDto extends ActionBaseDto{

    @ApiModelProperty("类型：4096:发起网络连接, 4097:TCP数据发送, 4098：TCP数据接收")
    @Mapping
    Integer getType();

    @ApiModelProperty("目标主机")
    @Mapping
    String getHost();

    @ApiModelProperty("目标端口")
    @Mapping
    Integer getPort();

    @ApiModelProperty("数据包大小")
    @Mapping
    Long getBytes();

    @ApiModelProperty("协议类型")
    String getProtocol();

    void setProtocol(String protocol);

    @ApiModelProperty("敏感数据字段")
    String getWarningParams();

    void settWarningParams(String warningParams);

}
