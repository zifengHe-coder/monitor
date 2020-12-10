package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class FilePathCommand {

    @NotNull
    @ApiModelProperty("文件路径")
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
