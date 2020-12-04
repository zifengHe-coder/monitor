package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

public class SoftwareIdCommand {

    @ApiModelProperty("软件ID")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
