package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

public class FileListCommand {

    @ApiModelProperty("当然访问文件夹路径，为空时是跟目录")
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
