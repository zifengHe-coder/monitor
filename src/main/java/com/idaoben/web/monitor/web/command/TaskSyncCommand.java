package com.idaoben.web.monitor.web.command;

import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;

public class TaskSyncCommand {

    @ApiModelProperty("上次同步时间，为空时取全部")
    private ZonedDateTime lastSyncTime;

    public ZonedDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(ZonedDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
