package com.idaoben.web.monitor.web.dto;

import com.idaoben.utils.dto_assembler.annotation.EnableAssembling;
import com.idaoben.utils.dto_assembler.annotation.Mapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

@ApiModel
@EnableAssembling
public interface ActionProcessDto {

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

    @ApiModelProperty("启动进程的完整命令行数据")
    @Mapping
    String getCommandLine();

    @ApiModelProperty("创建远程线程时提供的入口函数地址 ")
    @Mapping
    String getThreadEntryAddress();

    @ApiModelProperty("待加载DLL的路径")
    @Mapping
    String getPath();

    @ApiModelProperty("消息发送目标窗口的句柄")
    @Mapping
    String getDestHwnd();

    @ApiModelProperty("消息发送源窗口的句柄")
    @Mapping
    String getSrcHwnd();

    @ApiModelProperty("发送数据(HEX编码)")
    @Mapping
    String getData();
}
