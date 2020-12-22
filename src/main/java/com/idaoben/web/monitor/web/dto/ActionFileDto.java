package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import com.idaoben.web.monitor.dao.entity.enums.FileOpType;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@EnableAssembling
public interface ActionFileDto extends ActionBaseDto{

    @ApiModelProperty("文件路径")
    @Mapping
    String getPath();

    @ApiModelProperty("文件名称")
    @Mapping
    String getFileName();

    @ApiModelProperty("文件敏感度")
    @Mapping
    FileSensitivity getSensitivity();

    @ApiModelProperty("读写类型")
    FileOpType getOpType();

    void setOpType(FileOpType opType);
}
