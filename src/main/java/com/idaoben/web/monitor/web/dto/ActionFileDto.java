package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import com.idaoben.web.monitor.dao.entity.enums.FileOpType;
import com.idaoben.web.monitor.dao.entity.enums.FileSensitivity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

@ApiModel
@EnableAssembling
public interface ActionFileDto {

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
