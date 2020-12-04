package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import com.idaoben.utils.dto_assembler.annotation.TypeMapping;
import com.idaoben.web.monitor.dao.entity.Task;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.ZonedDateTime;

/**
*
* 任务 Dto接口
* Generated by DAOBEN CODE GENERATOR
* @author  Daoben Code Generator
*/
@ApiModel
@EnableAssembling(mappings = @TypeMapping(from = Task.class))
public interface TaskDto {

    @ApiModelProperty("ID")
    @Mapping
    Long getId();

    @ApiModelProperty("启动时间")
    @Mapping
    ZonedDateTime getStartTime();

    @ApiModelProperty("结束时间")
    @Mapping
    ZonedDateTime getEndTime();

    @ApiModelProperty("软件ID")
    @Mapping
    String getSoftwareId();

    @ApiModelProperty("是否已完成")
    @Mapping
    boolean isComplete();
}