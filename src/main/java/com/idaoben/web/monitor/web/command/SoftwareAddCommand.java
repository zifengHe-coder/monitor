package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class SoftwareAddCommand {

    @NotEmpty
    @ApiModelProperty("可执行程序路径")
    private String exePath;

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }
}
