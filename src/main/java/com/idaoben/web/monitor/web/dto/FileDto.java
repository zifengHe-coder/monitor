package com.idaoben.web.monitor.web.dto;

import io.swagger.annotations.ApiModelProperty;

public class FileDto {

    @ApiModelProperty("文件名")
    private String name;

    @ApiModelProperty("文件路径")
    private String path;

    @ApiModelProperty("是否目录")
    private boolean directory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
}
