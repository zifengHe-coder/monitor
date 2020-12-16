package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class SoftwareCmdAddCommand {

    @NotEmpty
    @ApiModelProperty("启动命令行")
    private String commandLine;

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }
}
