package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class FolderOpenCommand {

    @NotNull
    @ApiModelProperty("打开文件位置的路径")
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
